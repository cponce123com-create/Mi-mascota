import type { FastifyInstance } from 'fastify';
import { PrismaClient } from '@prisma/client';
import { z } from 'zod';
import { authenticatedUser } from '../auth.js';

const db = new PrismaClient();
const idSchema = z.object({ petId: z.string().uuid() });
const vaccineSchema = z.object({ name: z.string().trim().min(1).max(120), administeredAt: z.string().datetime().optional(), nextDueAt: z.string().datetime().optional() });
const reminderSchema = z.object({ title: z.string().trim().min(1).max(160), dueAt: z.string().datetime() });

export async function healthRoutes(app: FastifyInstance) {
  app.get('/v1/pets/:petId/health', async (req, reply) => {
    let uid: string;
    try { uid = await authenticatedUser(req); } catch { return reply.code(401).send({ error: 'Unauthorized' }); }
    const params = idSchema.safeParse(req.params);
    if (!params.success) return reply.code(400).send({ error: 'Invalid pet id' });
    const pet = await db.pet.findFirst({ where: { id: params.data.petId, owner: { googleSubject: uid } } });
    if (!pet) return reply.code(404).send({ error: 'Pet not found' });
    const [vaccines, reminders] = await Promise.all([
      db.vaccine.findMany({ where: { petId: pet.id }, take: 200 }),
      db.reminder.findMany({ where: { petId: pet.id }, take: 200 })
    ]);
    return { vaccines, reminders };
  });
  app.post('/v1/pets/:petId/vaccines', async (req, reply) => {
    let uid: string;
    try { uid = await authenticatedUser(req); } catch { return reply.code(401).send({ error: 'Unauthorized' }); }
    const params = idSchema.safeParse(req.params);
    const body = vaccineSchema.safeParse(req.body);
    if (!params.success || !body.success) return reply.code(400).send({ error: 'Invalid data' });
    const pet = await db.pet.findFirst({ where: { id: params.data.petId, owner: { googleSubject: uid } } });
    if (!pet) return reply.code(404).send({ error: 'Pet not found' });
    return reply.code(201).send(await db.vaccine.create({ data: {
      petId: pet.id, name: body.data.name,
      administeredAt: body.data.administeredAt ? new Date(body.data.administeredAt) : null,
      nextDueAt: body.data.nextDueAt ? new Date(body.data.nextDueAt) : null
    } }));
  });
  app.post('/v1/pets/:petId/reminders', async (req, reply) => {
    let uid: string;
    try { uid = await authenticatedUser(req); } catch { return reply.code(401).send({ error: 'Unauthorized' }); }
    const params = idSchema.safeParse(req.params);
    const body = reminderSchema.safeParse(req.body);
    if (!params.success || !body.success) return reply.code(400).send({ error: 'Invalid data' });
    const pet = await db.pet.findFirst({ where: { id: params.data.petId, owner: { googleSubject: uid } } });
    if (!pet) return reply.code(404).send({ error: 'Pet not found' });
    return reply.code(201).send(await db.reminder.create({ data: { petId: pet.id, title: body.data.title, dueAt: new Date(body.data.dueAt) } }));
  });
}
