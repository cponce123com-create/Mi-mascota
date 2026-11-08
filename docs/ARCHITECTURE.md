# Arquitectura

Android offline-first, con módulos previstos: auth, pets, health, gallery, reminders, lostpets, community, veterinarymap y backup.

Backend modular con Fastify + PostgreSQL + Prisma. El esquema es inicial y requiere migraciones, controles de acceso y validaciones antes de producción.

Google Sign-In y Drive deben pedir consentimientos separados. Nunca subir fotos privadas a un feed público sin autorización.
