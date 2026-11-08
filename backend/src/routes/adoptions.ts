import type { FastifyInstance } from 'fastify';
import { PrismaClient } from '@prisma/client';
import { z } from 'zod';
import { authenticatedUser } from '../auth.js';

const db = new PrismaClient();
const listingInput = z.object({
  petName: z.string().trim().min(1).max(80),
  species: z.enum(['CAT', 'DOG', 'OTHER']),
  description: z.string().trim().min(20).max(1200),
  district: z.string().trim().min(2).max(100).optional()
});
const applicationInput = z.object({ message: z.string().trim().min(20).max(1000) });
const idParam = z.object({ id: z.string().uuid() });
const publishingLimit = { config: { rateLimit: { max: 5, timeWindow: '1 minute' } } };

async function uidOrNull(req: Parameters<typeof authenticatedUser>[0]): Promise<string | null> {
  try { return await authenticatedUser(req); } catch { return null; }
}
async function account(uid: string) {
  return db.user.upsert({ where: { googleSubject: uid }, create: { googleSubject: uid }, update: {} });
}

export async function adoptionRoutes(app: FastifyInstance) {
  app.get('/v1/adoptions', async () => db.adoptionListing.findMany({
    where: { status: 'APPROVED' }, orderBy: { createdAt: 'desc' }, take: 50,
    select: { id: true, petName: true, species: true, description: true, district: true, createdAt: true }
  }));

  app.post('/v1/adoptions', publishingLimit, async (req, reply) => {
    const uid = await uidOrNull(req);
    if (!uid) return reply.code(401).send({ error: 'Unauthorized' });
    const body = listingInput.safeParse(req.body);
    if (!body.success) return reply.code(400).send({ error: 'Invalid adoption listing' });
    const user = await account(uid);
    const listing = await db.adoptionListing.create({ data: {
      ...body.data, authorId: user.id, status: 'PENDING'
    } });
    return reply.code(201).send({ id: listing.id, status: listing.status });
  });

  app.get('/v1/adoptions/mine', async (req, reply) => {
    const uid = await uidOrNull(req);
    if (!uid) return reply.code(401).send({ error: 'Unauthorized' });
    const user = await db.user.findUnique({ where: { googleSubject: uid } });
    if (!user) return { listings: [], applications: [] };
    const [listings, applications] = await Promise.all([
      db.adoptionListing.findMany({
        where: { authorId: user.id }, take: 50, orderBy: { createdAt: 'desc' },
        select: { id: true, petName: true, status: true, createdAt: true }
      }),
      db.adoptionApplication.findMany({
        where: { applicantId: user.id }, take: 50, orderBy: { createdAt: 'desc' },
        select: { id: true, status: true, listing: { select: { petName: true } } }
      })
    ]);
    return { listings, applications };
  });

  app.post('/v1/adoptions/:id/apply', publishingLimit, async (req, reply) => {
    const uid = await uidOrNull(req);
    if (!uid) return reply.code(401).send({ error: 'Unauthorized' });
    const params = idParam.safeParse(req.params);
    const body = applicationInput.safeParse(req.body);
    if (!params.success || !body.success) return reply.code(400).send({ error: 'Invalid application' });
    const listing = await db.adoptionListing.findUnique({ where: { id: params.data.id } });
    if (!listing || listing.status !== 'APPROVED') return reply.code(404).send({ error: 'Listing unavailable' });
    const user = await account(uid);
    if (listing.authorId === user.id) return reply.code(403).send({ error: 'Cannot apply to own listing' });
    const existing = await db.adoptionApplication.findUnique({
      where: { listingId_applicantId: { listingId: listing.id, applicantId: user.id } }
    });
    if (existing) return reply.code(409).send({ error: 'Already applied' });
    const application = await db.adoptionApplication.create({ data: {
      listingId: listing.id, applicantId: user.id, message: body.data.message
    } });
    return reply.code(201).send({ id: application.id, status: application.status });
  });

  app.get('/v1/adoptions/:id/applications', async (req, reply) => {
    const uid = await uidOrNull(req);
    if (!uid) return reply.code(401).send({ error: 'Unauthorized' });
    const params = idParam.safeParse(req.params);
    if (!params.success) return reply.code(400).send({ error: 'Invalid id' });
    const listing = await db.adoptionListing.findFirst({
      where: { id: params.data.id, author: { googleSubject: uid } }
    });
    if (!listing) return reply.code(404).send({ error: 'Not found' });
    return db.adoptionApplication.findMany({
      where: { listingId: listing.id }, orderBy: { createdAt: 'desc' }, take: 100,
      select: { id: true, message: true, status: true, createdAt: true }
    });
  });

  app.patch('/v1/adoption-applications/:id/respond', async (req, reply) => {
    const uid = await uidOrNull(req);
    if (!uid) return reply.code(401).send({ error: 'Unauthorized' });
    const params = idParam.safeParse(req.params);
    const body = z.object({ status: z.enum(['ACCEPTED', 'REJECTED']) }).safeParse(req.body);
    if (!params.success || !body.success) return reply.code(400).send({ error: 'Invalid response' });
    const updated = await db.adoptionApplication.updateMany({
      where: { id: params.data.id, listing: { author: { googleSubject: uid }, status: 'APPROVED' } },
      data: { status: body.data.status }
    });
    return updated.count ? { updated: true } : reply.code(404).send({ error: 'Not found' });
  });

  app.patch('/v1/adoptions/:id/close', async (req, reply) => {
    const uid = await uidOrNull(req);
    if (!uid) return reply.code(401).send({ error: 'Unauthorized' });
    const params = idParam.safeParse(req.params);
    if (!params.success) return reply.code(400).send({ error: 'Invalid id' });
    const updated = await db.adoptionListing.updateMany({
      where: { id: params.data.id, author: { googleSubject: uid } },
      data: { status: 'CLOSED' }
    });
    return updated.count ? { closed: true } : reply.code(404).send({ error: 'Not found' });
  });
}
