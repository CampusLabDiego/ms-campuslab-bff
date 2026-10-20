# BFF CampusLab

Gateway Spring Boot que recibe las peticiones de API Gateway,
valida el Access Token y enruta hacia los microservicios.

## Tecnologías

- Java 21.
- Spring Boot 3.3.5.
- Spring Cloud Gateway.
- Spring Security OAuth2 Resource Server.
- Maven y Docker.

## Responsabilidades

- Validar firma, issuer, audience y vigencia del JWT.
- Exigir el scope access_as_user.
- Convertir el claim roles en autoridades ROLE_*.
- Aplicar autorización por rol.
- Gestionar CORS.
- Enrutar solicitudes a los servicios correspondientes.

El BFF no se conecta a la base de datos.

## Rutas

| Prefijo | Destino |
|---|---|
| /api/catalog | Catálogo |
| /api/bookings | Reservas |
| /api/notifications | Notifier |
| /api/audit | Notifier |
| /api/admin | Administrador RabbitMQ |

## Variables de entorno

| Variable | Destino habitual en Docker |
|---|---|
| CATALOG_URL | http://catalog:8081 |
| BOOKINGS_URL | http://bookings:8082 |
| NOTIFIER_URL | http://notifier:8083 |
| RABBIT_ADMIN_URL | http://rabbit-admin:8084 |

La propiedad campuslab.security.allowed-origins define los orígenes
permitidos. En el despliegue se configura mediante
SPRING_APPLICATION_JSON desde Docker Compose.

Orígenes utilizados:

- http://localhost:4200
- https://wktaaga6j4.execute-api.us-east-1.amazonaws.com

## Seguridad

- Administración RabbitMQ: Admin.
- Auditoría: Admin o Auditor.
- Escritura de catálogo: Admin.
- Gestión de estados de reservas: Admin u Operador.
- Confirmación de llegada: Cliente; el servicio valida la propiedad.
- Consultas y creación de reservas: roles autorizados del sistema.

Las solicitudes OPTIONS están permitidas para atender el preflight.

## Compilación

En PowerShell:

```powershell
.\mvnw.cmd clean package -DskipTests
```

Este comando compila y empaqueta; omite la ejecución de pruebas.

## Ejecución

Puerto: 8080.

El servicio se despliega mediante el Compose de infra-campuslab.
API Gateway mantiene su integración de backend hacia este puerto.