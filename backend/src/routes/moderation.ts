import type { FastifyInstance } from 'fastify';
import { PrismaClient } from '@prisma/client';
import { z } from 'zod';
import { authenticatedUser } from '../auth.js';

const db = new PrismaClient();
const paramsSchema = z.object({ kind: z.enum(['post', 'lost', 'adoption']), id: z.string().uuid() });
const decisionSchema = z.object({ approve: z.boolean() });

/** Explicit service-side admin allowlist, never a client-supplied role. */
async function isModerator(request: Parameters<typeof authenticatedUser>[0]) {
  const uid = await authenticatedUser(request);
  const admins = (process.env.MODERATOR_UIDS ?? '').split(',').map(v => v.trim()).filter(Boolean);
  return admins.includes(uid);
}

export async function moderationRoutes(app: FastifyInstance) {
  app.get('/v1/moderation/queue', async (req, reply) => {
    try { if (!await isModerator(req)) return reply.code(403).send({ error: 'Forbidden' }); }
    catch { return reply.code(401).send({ error: 'Unauthorized' }); }
    const [posts, lost, adoptions] = await Promise.all([
      db.post.findMany({ where: { status: 'PENDING' }, take: 50, select: { id: true, title: true, body: true } }),
      db.lostPetAlert.findMany({ where: { visibility: 'PENDING' }, take: 50,
        select: { id: true, district: true, details: true, pet: { select: { name: true } } } }),
      db.adoptionListing.findMany({ where: { status: 'PENDING' }, take: 50,
        select: { id: true, petName: true, description: true, district: true } })
    ]);
    return { posts, lost, adoptions };
  });

  app.patch('/v1/moderation/:kind/:id', async (req, reply) => {
    try { if (!await isModerator(req)) return reply.code(403).send({ error: 'Forbidden' }); }
    catch { return reply.code(401).send({ error: 'Unauthorized' }); }
    const params = paramsSchema.safeParse(req.params);
    const body = decisionSchema.safeParse(req.body);
    if (!params.success || !body.success) return reply.code(400).send({ error: 'Invalid moderation action' });
    const { kind, id } = params.data;
    const approved = body.data.approve;
    const result = kind === 'post'
      ? await db.post.updateMany({ where: { id, status: 'PENDING' }, data: { status: approved ? 'APPROVED' : 'REJECTED' } })
      : kind === 'lost'
      ? await db.lostPetAlert.updateMany({ where: { id, visibility: 'PENDING' }, data: { visibility: approved ? 'APPROVED' : 'REJECTED' } })
      : await db.adoptionListing.updateMany({ where: { id, status: 'PENDING' }, data: { status: approved ? 'APPROVED' : 'REJECTED' } });
    return result.count ? { moderated: true } : reply.code(404).send({ error: 'Not pending or not found' });
  });
}
