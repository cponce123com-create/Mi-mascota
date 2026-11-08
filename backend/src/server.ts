import Fastify from 'fastify';
import rateLimit from '@fastify/rate-limit';
import { petRoutes } from './routes/pets.js';
import { healthRoutes } from './routes/health.js';
import { communityRoutes } from './routes/community.js';
import { adoptionRoutes } from './routes/adoptions.js';
import { moderationRoutes } from './routes/moderation.js';
import { accountRoutes } from './routes/account.js';

export function buildApp() {
  const app = Fastify({ logger: process.env.NODE_ENV !== 'test', bodyLimit: 1_000_000 });
  app.get('/health', async () => ({ status: 'ok', service: 'mi-mascota' }));
  app.register(rateLimit, { max: 60, timeWindow: '1 minute' });
  app.register(petRoutes);
  app.register(communityRoutes);
  app.register(healthRoutes);
  app.register(adoptionRoutes);
  app.register(moderationRoutes);
  app.register(accountRoutes);
  return app;
}

if (process.env.NODE_ENV !== 'test') {
  const app = buildApp();
  const port = Number(process.env.PORT ?? 3000);
  app.listen({ port, host: '0.0.0.0' }).catch((error) => {
    app.log.error(error);
    process.exit(1);
  });
}
