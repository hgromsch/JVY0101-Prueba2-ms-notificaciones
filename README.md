# ms-notificaciones

Envío de confirmaciones y recordatorios de citas y talleres por email, SMS o push.
Cubre **RF-10**. Servicio **stateless** respecto del resto del sistema: en AWS consume
eventos de las colas SQS `agenda-notificaciones-queue` y `talleres-notificaciones-queue`
y despacha por SES/SNS; localmente expone una API REST para simular esos eventos y
persiste el registro de cada notificación para la demostración.

**Puerto:** 8087 · **Base de datos:** `notificaciones_db` · **Swagger:** http://localhost:8087/swagger-ui.html

## Endpoints

| Método | Ruta | Descripción |
|---|---|---|
| POST | `/api/notificaciones` | Envía una notificación directa (201) |
| POST | `/api/notificaciones/eventos` | Simula el evento de dominio que en AWS llega por SQS (202) |
| GET | `/api/notificaciones?destinatario=&estado=` | Lista notificaciones con filtros opcionales |
| GET | `/api/notificaciones/{id}` | Obtiene una notificación (404 si no existe) |
| POST | `/api/notificaciones/{id}/reenviar` | Reintenta una notificación pendiente/fallida |
| DELETE | `/api/notificaciones/{id}` | Elimina el registro (204) |

Canales: `EMAIL`, `SMS`, `PUSH`. Estados: `PENDIENTE`, `ENVIADA`, `FALLIDA`.

## Cómo correr localmente

```bash
docker compose -f ../docker-compose.yml up -d   # Postgres + Redis (una sola vez)
mvn spring-boot:run
```

## Arquitectura

Capas `api` → `service` → `repository` → `domain`, con errores centralizados en
`GlobalExceptionHandler`.

## Despliegue

Pensado para **Amazon ECS Fargate** con autoescalado por profundidad de cola SQS.
Imagen: `docker build -t ms-notificaciones .`
