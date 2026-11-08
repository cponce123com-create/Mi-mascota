import type { FastifyInstance } from 'fastify';
import { z } from 'zod';
import { authenticatedUser } from '../auth.js';
import { prisma } from '../db.js';

const deviceInput = z.object({ token: z.string().trim().min(20).max(4096) });

export async function deviceRoutes(app: FastifyInstance) {
  app.post('/v1/devices', { config: { rateLimit: { max: 5, timeWindow: '1 minute' } } },
    async (req, reply) => {
      let uid: string;
      try { uid = await authenticatedUser(req); }
      catch { return reply.code(401).send({ error: 'Unauthorized' }); }
      const parsed = deviceInput.safeParse(req.body);
      if (!parsed.success) return reply.code(400).send({ error: 'Invalid device token' });
      const user = await prisma.user.upsert({
        where: { googleSubject: uid }, create: { googleSubject: uid }, update: {}
      });
      const alreadyRegistered = await prisma.pushDevice.findUnique({
        where: { token: parsed.data.token }, select: { ownerId: true }
      });
      if (alreadyRegistered && alreadyRegistered.ownerId !== user.id) {
        return reply.code(409).send({ error: 'Device belongs to another account' });
      }
      await prisma.pushDevice.upsert({
        where: { token: parsed.data.token },
        create: { ownerId: user.id, token: parsed.data.token },
        update: { updatedAt: new Date() }
      });
      return reply.code(201).send({ registered: true });
    });

  app.delete('/v1/devices', async (req, reply) => {
    let uid: string;
    try { uid = await authenticatedUser(req); }
    catch { return reply.code(401).send({ error: 'Unauthorized' }); }
    const parsed = deviceInput.safeParse(req.body);
    if (!parsed.success) return reply.code(400).send({ error: 'Invalid token' });
    const removed = await prisma.pushDevice.deleteMany({
      where: { token: parsed.data.token, owner: { googleSubject: uid } }
    });
    return { removed: removed.count > 0 };
  });
}
