# Migraciones de base de datos

El esquema Prisma y la migración inicial SQL están versionados. La migración inicial es para una base nueva; si ya existen tablas, verificar el estado antes de aplicarla.

Desde backend:
1. npm install
2. npm run prisma:generate
3. npm run prisma:migrate
4. npm run build

Las credenciales se configuran en el servicio de despliegue, no en el repositorio. Verificar la compatibilidad del SQL con PostgreSQL y ejecutar primero en una base de prueba.
