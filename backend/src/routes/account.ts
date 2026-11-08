import type { FastifyInstance } from 'fastify';
import { PrismaClient } from '@prisma/client';
import { z } from 'zod';
import { authenticatedUser } from '../auth.js';

const db = new PrismaClient();
const confirmation = z.object({ confirm: z.literal('DELETE_MY_ACCOUNT') });

export async function accountRoutes(app: FastifyInstance) {
  /** Delete the authenticated user's remote data, including posts, pets, alerts and adoption records. */
  app.delete('/v1/account', async (req, reply) => {
    let uid: string;
    try { uid = await authenticatedUser(req); }
    catch { return reply.code(401).send({ error: 'Unauthorized' }); }
    if (!confirmation.safeParse(req.body).success)
      return reply.code(400).send({ error: 'Explicit confirmation required' });

    await db.$transaction(async tx => {
      const user = await tx.user.findUnique({ where: { googleSubject: uid } });
      if (!user) return;
      // Remove dependent records before the owner to respect non-cascade relations.
      await tx.comment.deleteMany({ where: { authorId: user.id } });
      await tx.adoptionApplication.deleteMany({
        where: { OR: [{ applicantId: user.id }, { listing: { authorId: user.id } }] }
      });
      await tx.adoptionListing.deleteMany({ where: { authorId: user.id } });
      await tx.post.deleteMany({ where: { authorId: user.id } });
      await tx.pet.deleteMany({ where: { ownerId: user.id } });
      await tx.user.delete({ where: { id: user.id } });
    });
    return { deleted: true };
  });
}
