import test from 'node:test';
import assert from 'node:assert/strict';
import { buildApp } from '../src/server.js';

test('health endpoint responds without database connection', async () => {
  const app = buildApp();
  try {
    const response = await app.inject({ method: 'GET', url: '/health' });
    assert.equal(response.statusCode, 200);
    assert.deepEqual(response.json(), { status: 'ok', service: 'mi-mascota' });
  } finally { await app.close(); }
});

test('protected publishing requires a Firebase bearer token', async () => {
  const app = buildApp();
  try {
    for (const url of ['/v1/community/posts', '/v1/adoptions', '/v1/lost-pets']) {
      const response = await app.inject({ method: 'POST', url, payload: {} });
      assert.equal(response.statusCode, 401, url);
    }
  } finally { await app.close(); }
});

test('private moderation is not accessible anonymously', async () => {
  const app = buildApp();
  try {
    const response = await app.inject({ method: 'GET', url: '/v1/moderation/queue' });
    assert.equal(response.statusCode, 401);
  } finally { await app.close(); }
});
