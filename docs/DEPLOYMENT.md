# Despliegue (no ejecutado)

## Backend
1. Crear base PostgreSQL en Neon y copiar DATABASE_URL como secreto de Render.
2. Crear proyecto Firebase, habilitar Google y registrar Android SHA-1/SHA-256.
3. En Render, instalar dependencias con npm install y compilar con npm run build.
4. Ejecutar migraciones con npm run prisma:migrate después de generar una migración versionada.
5. Configurar credenciales de Firebase Admin por secretos, nunca en Git.
6. Configurar HTTPS, dominio y observabilidad antes de abrir al público.

## Android
1. Instalar Android Studio y JDK 17.
2. Abrir la carpeta android como proyecto.
3. Configurar Firebase y OAuth según docs/GOOGLE_AUTH_SETUP.md.
4. Compilar y ejecutar pruebas. No hay APK firmado publicado.

## Privacidad
No sincronizar registros médicos ni fotografías privadas sin consentimiento. La galería usa URIs locales y no equivale a un respaldo.
