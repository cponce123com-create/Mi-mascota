import type { FastifyInstance } from 'fastify';
import { PrismaClient } from '@prisma/client';
import { z } from 'zod';
import { authenticatedUser } from '../auth.js';

const prisma = new PrismaClient();
const postInput = z.object({ title: z.string().trim().min(3).max(120), body: z.string().trim().min(5).max(3000) });
const alertInput = z.object({ petId: z.string().uuid(), district: z.string().trim().min(2).max(100).optional() });

export async function communityRoutes(app: FastifyInstance) {
  app.get('/v1/community/posts', async (_req, reply) => {
    const posts = await prisma.post.findMany({ orderBy: { createdAt: 'desc' }, take: 30, select: { id: true, title: true, body: true, createdAt: true } });
    return reply.send(posts);
  });
  app.post('/v1/community/posts', async (req, reply) => {
    let uid: string;
    try { uid = await authenticatedUser(req); } catch { return reply.code(401).send({ error: 'Unauthorized' }); }
    const parsed = postInput.safeParse(req.body);
    if (!parsed.success) return reply.code(400).send({ error: 'Invalid post' });
    const user = await prisma.user.upsert({ where: { googleSubject: uid }, create: { googleSubject: uid }, update: {} });
    return reply.code(201).send(await prisma.post.create({ data: { authorId: user.id, ...parsed.data } }));
  });
  app.get('/v1/lost-pets', async (req, reply) => {
    const query = z.object({ district: z.string().max(100).optional() }).safeParse(req.query);
    if (!query.success) return reply.code(400).send({ error: 'Invalid query' });
    return prisma.lostPetAlert.findMany({
      where: { status: 'OPEN', ...(query.data.district ? { district: query.data.district } : {}) },
      select: { id: true, district: true, createdAt: true, pet: { select: { name: true, species: true } } },
      orderBy: { createdAt: 'desc' }, take: 50
    });
  });
  app.post('/v1/lost-pets', async (req, reply) => {
    let uid: string;
    try { uid = await authenticatedUser(req); } catch { return reply.code(401).send({ error: 'Unauthorized' }); }
    const parsed = alertInput.safeParse(req.body);
    if (!parsed.success) return reply.code(400).send({ error: 'Invalid alert' });
    const pet = await prisma.pet.findFirst({ where: { id: parsed.data.petId, owner: { googleSubject: uid } } });
    if (!pet) return reply.code(404).send({ error: 'Pet not found' });
    return reply.code(201).send(await prisma.lostPetAlert.create({ data: parsed.data }));
  });
  app.patch('/v1/lost-pets/:id/resolve', async (req, reply) => {
    let uid: string;
    try { uid = await authenticatedUser(req); } catch { return reply.code(401).send({ error: 'Unauthorized' }); }
    const params = z.object({ id: z.string().uuid() }).safeParse(req.params);
    if (!params.success) return reply.code(400).send({ error: 'Invalid id' });
    const result = await prisma.lostPetAlert.updateMany({ where: { id: params.data.id, pet: { owner: { googleSubject: uid } } }, data: { status: 'RESOLVED' } });
    return result.count ? reply.send({ resolved: true }) : reply.code(404).send({ error: 'Alert not found' });
  });
}
