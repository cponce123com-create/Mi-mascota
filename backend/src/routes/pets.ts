import type { FastifyInstance } from 'fastify';
import { PrismaClient } from '@prisma/client';
import { z } from 'zod';
import { authenticatedUser } from '../auth.js';

const prisma = new PrismaClient();
const createPet = z.object({
  id: z.string().uuid().optional(),
  name: z.string().trim().min(1).max(80),
  species: z.enum(['CAT', 'DOG', 'OTHER']),
  birthDate: z.string().date().optional()
});

export async function petRoutes(app: FastifyInstance) {
  app.get('/v1/pets', async (request, reply) => {
    let uid: string;
    try { uid = await authenticatedUser(request); }
    catch { return reply.code(401).send({ error: 'Unauthorized' }); }
    const user = await prisma.user.findUnique({ where: { googleSubject: uid } });
    if (!user) return [];
    return prisma.pet.findMany({ where: { ownerId: user.id }, orderBy: { createdAt: 'desc' }, take: 100 });
  });

  app.post('/v1/pets', async (request, reply) => {
    let uid: string;
    try { uid = await authenticatedUser(request); }
    catch { return reply.code(401).send({ error: 'Unauthorized' }); }
    const parsed = createPet.safeParse(request.body);
    if (!parsed.success) return reply.code(400).send({ error: 'Invalid pet', details: parsed.error.flatten() });
    const user = await prisma.user.upsert({
      where: { googleSubject: uid },
      create: { googleSubject: uid },
      update: {}
    });
    const birthday = parsed.data.birthDate ? new Date(parsed.data.birthDate + 'T00:00:00Z') : null;
    if (parsed.data.id) {
      const existing = await prisma.pet.findUnique({ where: { id: parsed.data.id } });
      if (existing && existing.ownerId !== user.id)
        return reply.code(409).send({ error: 'Pet ID already belongs to another account' });
      const data = { name: parsed.data.name, species: parsed.data.species, birthDate: birthday };
      const pet = await prisma.pet.upsert({
        where: { id: parsed.data.id },
        create: { id: parsed.data.id, ownerId: user.id, ...data },
        update: data
      });
      return reply.send(pet);
    }
    return reply.code(201).send(await prisma.pet.create({
      data: { ownerId: user.id, name: parsed.data.name, species: parsed.data.species,
        birthDate: birthday }
    }));
  });
}
