import Fastify from 'fastify';
import { petRoutes } from './routes/pets.js';
const app = Fastify({ logger: true, bodyLimit: 1_000_000 });
app.get('/health', async () => ({ status: 'ok', service: 'mi-mascota' }));
app.register(petRoutes);
const port = Number(process.env.PORT ?? 3000);
app.listen({ port, host: '0.0.0.0' }).catch((error) => { app.log.error(error); process.exit(1); });
