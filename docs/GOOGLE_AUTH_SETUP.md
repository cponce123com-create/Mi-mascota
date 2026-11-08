# Inicio de sesión con Google: configuración pendiente

Para habilitar Google Sign-In:
1. Crear proyecto en Firebase Console y registrar el paquete `pe.mimascota.app`.
2. Registrar las huellas SHA-1/SHA-256 de firma de Android.
3. Habilitar proveedor Google en Firebase Authentication.
4. Incorporar `google-services.json` **solo en el equipo local** (excluido de Git).
5. Integrar Android Credential Manager y validar el ID token con Firebase Auth.
6. En backend, verificar los tokens Firebase antes de aceptar cualquier escritura.

Google Drive requiere autorización OAuth independiente y opcional. No pedir permisos de Gmail.
