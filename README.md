# PAT-Grupo1
Projecto final de Programación de aplicaciones telemáticas, Grupo 1
## PRIMERA ENTREGA
### Descripción
Este proyecto consiste en el desarrollo de una API REST para la gestión completa de reservas de pistas de pádel, siguiendo las especificaciones definidas en la guía oficial de la asignatura. En esta primera entrega se ha implementado parte del backend en Spring Boot con todos los endpoints definidos en el documento oficial del proyecto.

Actualmente:
- Todos los endpoints están implementados.
- Las reglas de negocio están operativas.
- Se gestionan correctamente los códigos HTTP esperados.
### Arquitectura
- [src/main/java/edu/comillas/icai/git/pat/spring/ReservaPadel_PAT_G1/](./src/main/java/edu/comillas/icai/git/pat/spring/ReservaPadel_PAT_G1) – Código fuente de la aplicación Spring Boot.
    - configuration → Clases de configuración de la aplicación.
    - controllers → Controladores REST que definen todos los endpoints del sistema (auth, users, courts, reservations, availability, admin).
    - services → Implementación de la lógica de negocio.
    - repositories → Simulación de la capa de persistencia.
    - domain → Modelo de dominio (Usuario, User, Pista, Reserva, Rol, ReservaStatus y DTOs como Create/Patch/Login).
    - ReservaPadelPatG1Application → Clase principal que arranca la aplicación Spring Boot.

- [src/main/resources/](./src/main/resources) – Recursos y configuración de la aplicación.

- [src/test/](./src/test) – Estructura destinada a pruebas.

- [pom.xml](./pom.xml) – Archivo de configuración Maven con las dependencias del proyecto.

- [README.md](./README.md) – Documento descriptivo de la primera entrega.


### Endpoints implementados
Se han implementado todos los endpoints definidos en la guía:
- Autenticación (/auth)
- Gestión de usuarios (/users)
- Gestión de pistas (/courts)
- Disponibilidad (/availability)
- Reservas (/reservations)
- Administración (/admin/reservations)
- Healthcheck (/health)
Todos gestionan correctamente los códigos HTTP esperados (200, 201, 204, 400, 401, 403, 404, 409).

### Notas adicionales 
Actualmente existen dos clases para usuario:
- Usuario: utilizada en esta fase sin base de datos.
- User: preparada para la futura integración con persistencia y seguridad.


