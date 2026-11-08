import type { FastifyRequest } from 'fastify';
import { getAuth } from 'firebase-admin/auth';
import { initializeApp, getApps, applicationDefault } from 'firebase-admin/app';

export async function authenticatedUser(request: FastifyRequest): Promise<string> {
  const header = request.headers.authorization;
  if (!header?.startsWith('Bearer ')) throw new Error('UNAUTHORIZED');
  if (!getApps().length) initializeApp({ credential: applicationDefault() });
  const decoded = await getAuth().verifyIdToken(header.slice(7), true);
  return decoded.uid;
}
