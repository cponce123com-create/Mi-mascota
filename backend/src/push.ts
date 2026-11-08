import { getMessaging } from 'firebase-admin/messaging';
import { prisma } from './db.js';

/** Best-effort push: never blocks an adoption or moderation transaction. */
export async function notifyAccount(ownerId: string, title: string, body: string): Promise<void> {
  try {
    const devices = await prisma.pushDevice.findMany({
      where: { ownerId }, select: { token: true }, take: 100
    });
    if (!devices.length) return;
    const response = await getMessaging().sendEachForMulticast({
      tokens: devices.map(d => d.token),
      notification: { title, body },
      android: { priority: 'normal' }
    });
    const invalid = response.responses.flatMap((item, index) => {
      const code = item.error?.code;
      return code === 'messaging/registration-token-not-registered' ||
        code === 'messaging/invalid-registration-token'
        ? [devices[index].token] : [];
    });
    if (invalid.length)
      await prisma.pushDevice.deleteMany({ where: { token: { in: invalid } } });
  } catch (error) {
    // Push delivery is optional; logging avoids leaking tokens to API clients.
    console.warn('Push delivery unavailable:', error instanceof Error ? error.name : 'unknown');
  }
}
