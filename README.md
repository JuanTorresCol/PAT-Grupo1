# PAT-Grupo1
Projecto final de Programación de aplicaciones telemáticas, Grupo 1
## PRIMERA ENTREGA
### Descripción
Este proyecto consiste en el desarrollo de una API REST para la gestión completa de reservas de pistas de pádel, siguiendo las especificaciones definidas en la guía oficial de la asignatura. En esta primera entrega se ha implementado parte del backend en Spring Boot con todos los endpoints definidos en el documento oficial del proyecto.

Actualmente:
- Todos los endpoints están implementados.
- Las reglas de negocio están operativas.
- Se gestionan correctamente los códigos HTTP esperados.

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

## SEGUNDA ENTREGA
### Descripción
En esta segunda entrega se ha implementado la persistencia en base de datos del sistema de reservas de pistas de pádel.
La aplicación ahora utiliza Spring Data JPA junto con una base de datos H2 para almacenar la información del sistema de forma persistente. Las entidades principales del dominio (usuarios, pistas y reservas) se guardan en la base de datos y son gestionadas mediante repositorios.
La API sigue el patrón CRUD (Create, Read, Update, Delete) para la gestión de los recursos del sistema, permitiendo crear, consultar, modificar y cancelar reservas a través de endpoints REST.

### Organización de la base de datos

La base de datos está compuesta por tres tablas principales que representan las entidades del sistema.

**Tabla `USERS`**

Almacena la información de los usuarios del sistema.

Campos principales:

- `ID_USUARIO` – identificador del usuario
- `EMAIL` – correo electrónico del usuario
- `PASSWORD` – contraseña del usuario
- `NOMBRE` – nombre del usuario
- `APELLIDOS` – apellidos del usuario
- `TELEFONO` – teléfono de contacto
- `ROL` – rol del usuario (`USER` o `ADMIN`)
- `ACTIVO` – indica si el usuario está activo
- `FECHA_REGISTRO` – fecha de registro en el sistema

Esta tabla permite distinguir entre usuarios normales y administradores.

---

**Tabla `PISTA`**

Almacena la información de las pistas de pádel disponibles.

Campos principales:

- `ID_PISTA` – identificador de la pista
- `NOMBRE` – nombre de la pista
- `PRECIO_HORA` – precio por hora de uso
- `UBICACION` – ubicación de la pista
- `ACTIVA` – indica si la pista está disponible para reservas
- `FECHA_ALTA` – fecha de alta en el sistema

Solo las pistas activas pueden ser reservadas por los usuarios.

---

**Tabla `RESERVA`**

Contiene las reservas realizadas por los usuarios.

Campos principales:

- `ID_RESERVA` – identificador de la reserva
- `USERNAME` – usuario que realiza la reserva
- `PISTA` – pista reservada
- `DATE` – fecha de la reserva
- `START_TIME` – hora de inicio
- `END_TIME` – hora de finalización
- `DURATION_MINS` – duración en minutos
- `ESTADO` – estado de la reserva (`CONFIRMADA`, `CANCELADA`, `PASADA`)
- `CREATED_AT` – momento de creación de la reserva

Esta tabla permite controlar la disponibilidad de las pistas y evitar solapamientos de reservas.

### Notas adicionales 

- El funcionamiento de los endpoints se ha comprobado utilizando Postman, realizando todas las pruebas necesarias.
  
- Las operaciones se ejecutan correctamente y los datos quedan almacenados en la base de datos.
  
- Para la autenticación se ha implementado un método llamado `autentica`, que genera un token para identificar al usuario en las peticiones. Actualmente este token no está cifrado ni generado mediante un sistema de seguridad real, sino que corresponde simplemente al string del email del usuario. En futuras versiones se implementaran tokens cifrados.
  
- El sistema incluye endpoints para consultar el mapa de disponibilidad de pistas que actualmente existe en la aplicacion pero no se actualiza dinámicamente en función de las reservas existentes. A pesar de ello, el sistema sí controla correctamente los solapamientos, por lo que no es posible realizar reservas en franjas ya ocupadas. Este comportamiento será completado en futuras versiones del proyecto.

- Las reservas tienen distintos estados (`CONFIRMADA`, `CANCELADA`, `PASADA`).  Cuando una reserva confirmada ya ha finalizado y es consultada por la API, su estado se actualiza a `PASADA` y este cambio se persiste en la base de datos. Las reservas en estado `PASADA` no pueden modificarse ni cancelarse.

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

- [README.md](./README.md) – Documento descriptivo de las distintas entregas.



