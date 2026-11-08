# Reutilización de proyectos similares

Se revisó la disponibilidad de repositorios públicos de adopción de mascotas en GitHub. No se copió código de terceros en esta iteración porque no se verificó una licencia compatible ni se seleccionó un módulo con pruebas de integración.

Criterios obligatorios antes de incorporar código externo:
1. Verificar licencia y preservar avisos de atribución.
2. Comprobar mantenimiento, dependencias, permisos y vulnerabilidades.
3. Adaptar al diseño offline-first y a las capas existentes.
4. Añadir pruebas de integración y comprobar que la funcionalidad esté realmente cableada.
5. Evitar SDKs que obliguen a publicar datos privados o aumenten costos sin necesidad.

Patrones reutilizables implementados con código propio: tarjetas de mascota, navegación tipada, confirmación de borrado, API pública de publicaciones y alertas.
