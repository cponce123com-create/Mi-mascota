import type { FastifyInstance } from 'fastify';
import { z } from 'zod';
import { authenticatedUser } from '../auth.js';
import { prisma } from '../db.js';
import { notifyAccount } from '../push.js';

const params = z.object({ id: z.string().uuid() });
const messageInput = z.object({ body: z.string().trim().min(1).max(1000) });

export async function adoptionChatRoutes(app: FastifyInstance) {
  async function authorize(req: Parameters<typeof authenticatedUser>[0], id: string) {
    const uid = await authenticatedUser(req);
    const user = await prisma.user.findUnique({ where: { googleSubject: uid } });
    if (!user) return null;
    const application = await prisma.adoptionApplication.findFirst({
      where: {
        id, status: 'ACCEPTED',
        OR: [{ applicantId: user.id }, { listing: { authorId: user.id } }]
      },
      include: { listing: { select: { authorId: true } } }
    });
    return application ? { user, application } : null;
  }

  app.get('/v1/adoption-applications/:id/messages', async (req, reply) => {
    const path = params.safeParse(req.params);
    if (!path.success) return reply.code(400).send({ error: 'Invalid application id' });
    let auth;
    try { auth = await authorize(req, path.data.id); }
    catch { return reply.code(401).send({ error: 'Unauthorized' }); }
    if (!auth) return reply.code(404).send({ error: 'Conversation not found' });
    const rows = await prisma.adoptionMessage.findMany({
      where: { applicationId: path.data.id }, orderBy: { createdAt: 'asc' },
      take: 100, select: { id: true, body: true, authorId: true, createdAt: true }
    });
    return rows.map(row => ({
      id: row.id, body: row.body, mine: row.authorId === auth.user.id, createdAt: row.createdAt
    }));
  });

  app.post('/v1/adoption-applications/:id/messages',
    { config: { rateLimit: { max: 10, timeWindow: '1 minute' } } }, async (req, reply) => {
      const path = params.safeParse(req.params);
      const body = messageInput.safeParse(req.body);
      if (!path.success || !body.success) return reply.code(400).send({ error: 'Invalid message' });
      let auth;
      try { auth = await authorize(req, path.data.id); }
      catch { return reply.code(401).send({ error: 'Unauthorized' }); }
      if (!auth) return reply.code(404).send({ error: 'Conversation not found' });
      const message = await prisma.adoptionMessage.create({ data: {
        applicationId: path.data.id, authorId: auth.user.id, body: body.data.body
      } });
      const recipient = auth.user.id === auth.application.applicantId
        ? auth.application.listing.authorId : auth.application.applicantId;
      await notifyAccount(recipient, 'Mi Mascota', 'Tienes un mensaje sobre una adopción.');
      return reply.code(201).send({ id: message.id, sent: true });
    });
}
