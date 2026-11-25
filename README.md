# BFF CampusLab

Backend for Frontend de CampusLab. Recibe las solicitudes de API Gateway, valida los tokens y enruta las llamadas a los microservicios internos.

## Tecnologías

- Java 21.
- Spring Boot.
- Spring Cloud Gateway.
- Spring Security OAuth2 Resource Server.
- Maven y Docker.

## Responsabilidades

- Validar la firma, el emisor, la audiencia y la vigencia del Access Token.
- Exigir el scope configurado para la API.
- Convertir los roles del token en autoridades de Spring Security.
- Aplicar autorización por rol.
- Gestionar CORS.
- Enrutar solicitudes a los microservicios.

El BFF no utiliza una base de datos.

## Enrutamiento

| Prefijo | Destino |
|---|---|
| `/api/catalog` | Microservicio de catálogo |
| `/api/bookings` | Microservicio de reservas |
| `/api/notifications` | Microservicio notifier |
| `/api/audit` | Microservicio notifier |
| `/api/admin` | Microservicio de administración RabbitMQ y Kafka |

Las rutas de administración de Kafka y RabbitMQ se encuentran bajo `/api/admin` y requieren el rol autorizado.

## Autorización

- Administración de RabbitMQ y Kafka: Admin.
- Consulta de auditoría: Admin o Auditor.
- Administración del catálogo: Admin.
- Gestión de estados de reserva: Admin u Operador, según la operación.
- Confirmación de llegada: Cliente propietario de la reserva, sujeto a las reglas del servicio.

Las solicitudes `OPTIONS` se permiten para atender las solicitudes preflight de CORS.

La seguridad también se valida en los microservicios; el BFF no reemplaza las validaciones de propiedad ni las reglas de negocio.

## Configuración

Variables principales:

```text
CATALOG_URL
BOOKINGS_URL
NOTIFIER_URL
RABBIT_ADMIN_URL
```

Orígenes permitidos se configuran mediante la propiedad de seguridad del servicio o `SPRING_APPLICATION_JSON` en Docker Compose.

Los valores de producción deben configurarse en el entorno y no guardarse como secretos en el repositorio.

## Compilación

En PowerShell, desde la carpeta del repositorio:

```powershell
.\mvnw.cmd clean package -DskipTests
```

Este comando compila y empaqueta el servicio, pero omite la ejecución de pruebas.

## Despliegue

El BFF se ejecuta en el puerto `8080` mediante Docker Compose en `infra-campuslab`. API Gateway integra las rutas de API con este servicio.
