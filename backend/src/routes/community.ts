import type { FastifyInstance } from 'fastify';
import { z } from 'zod';
import { prisma } from '../db.js';
import { authenticatedUser } from '../auth.js';

const postInput = z.object({ title: z.string().trim().min(3).max(120), body: z.string().trim().min(5).max(3000) });
const commentInput = z.object({ body: z.string().trim().min(2).max(1000) });
const commentId = z.object({ id: z.string().uuid() });
const alertInput = z.object({ petId: z.string().uuid(), district: z.string().trim().min(2).max(100).optional(), details: z.string().trim().max(1200).optional() });

export async function communityRoutes(app: FastifyInstance) {
  app.get('/v1/community/posts', async (_req, reply) => {
    const posts = await prisma.post.findMany({ where: { status: 'APPROVED' }, orderBy: { createdAt: 'desc' }, take: 30, select: { id: true, title: true, body: true, createdAt: true } });
    return reply.send(posts);
  });
  app.post('/v1/community/posts', async (req, reply) => {
    let uid: string;
    try { uid = await authenticatedUser(req); } catch { return reply.code(401).send({ error: 'Unauthorized' }); }
    const parsed = postInput.safeParse(req.body);
    if (!parsed.success) return reply.code(400).send({ error: 'Invalid post' });
    const user = await prisma.user.upsert({ where: { googleSubject: uid }, create: { googleSubject: uid }, update: {} });
    return reply.code(201).send(await prisma.post.create({ data: { authorId: user.id, ...parsed.data, status: 'PENDING' } }));
  });
  app.get('/v1/community/posts/:id/comments', async (req, reply) => {
    const params = commentId.safeParse(req.params);
    if (!params.success) return reply.code(400).send({ error: 'Invalid post id' });
    const post = await prisma.post.findFirst({ where: { id: params.data.id, status: 'APPROVED' } });
    if (!post) return reply.code(404).send({ error: 'Post not found' });
    return prisma.comment.findMany({
      where: { postId: params.data.id, status: 'APPROVED' },
      take: 100, orderBy: { createdAt: 'asc' },
      select: { id: true, body: true, createdAt: true }
    });
  });
  app.post('/v1/community/posts/:id/comments', { config: { rateLimit: { max: 5, timeWindow: '1 minute' } } },
    async (req, reply) => {
      let uid: string;
      try { uid = await authenticatedUser(req); }
      catch { return reply.code(401).send({ error: 'Unauthorized' }); }
      const params = commentId.safeParse(req.params);
      const parsed = commentInput.safeParse(req.body);
      if (!params.success || !parsed.success) return reply.code(400).send({ error: 'Invalid comment' });
      const post = await prisma.post.findFirst({ where: { id: params.data.id, status: 'APPROVED' } });
      if (!post) return reply.code(404).send({ error: 'Post not found' });
      const user = await prisma.user.upsert({
        where: { googleSubject: uid }, create: { googleSubject: uid }, update: {}
      });
      const comment = await prisma.comment.create({ data: {
        postId: post.id, authorId: user.id, body: parsed.data.body, status: 'PENDING'
      } });
      return reply.code(201).send({ id: comment.id, status: comment.status });
    });
  app.get('/v1/lost-pets', async (req, reply) => {
    const query = z.object({ district: z.string().max(100).optional() }).safeParse(req.query);
    if (!query.success) return reply.code(400).send({ error: 'Invalid query' });
    return prisma.lostPetAlert.findMany({
      where: { status: 'OPEN', visibility: 'APPROVED', ...(query.data.district ? { district: query.data.district } : {}) },
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
    return reply.code(201).send(await prisma.lostPetAlert.create({ data: { ...parsed.data, visibility: 'PENDING' } }));
  });
  app.get('/v1/lost-pets/mine', async (req, reply) => {
    let uid: string;
    try { uid = await authenticatedUser(req); } catch { return reply.code(401).send({ error: 'Unauthorized' }); }
    return prisma.lostPetAlert.findMany({
      where: { pet: { owner: { googleSubject: uid } } }, orderBy: { createdAt: 'desc' }, take: 100,
      select: { id: true, status: true, visibility: true, district: true, details: true,
        pet: { select: { name: true } } }
    });
  });
  app.get('/v1/community/posts/mine', async (req, reply) => {
    let uid: string;
    try { uid = await authenticatedUser(req); } catch { return reply.code(401).send({ error: 'Unauthorized' }); }
    return prisma.post.findMany({
      where: { author: { googleSubject: uid } }, orderBy: { createdAt: 'desc' }, take: 100,
      select: { id: true, title: true, status: true, createdAt: true }
    });
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
