import { PrismaClient } from '@prisma/client';

// Single connection pool per backend process, shared by all Fastify routes.
export const prisma = new PrismaClient();
