# Newsletter Service

Brevo integration and newsletter subscription. Extracted as Phase 2.4 of the monolith-to-microservices migration.

## Responsibilities

- **POST /api/newsletter/add-email**: Direct newsletter signup (public endpoint)
- **Event consumer**: Consumes `MembershipCreated` events from RabbitMQ; when `newsletterOptIn` is true, adds email to Brevo and persists to `newsletter_subscriber`

## Database

- `newsletter_db` with `newsletter_subscriber` table (no user FK; decoupled from User/Auth)

## Configuration

| Variable | Description | Default |
|----------|-------------|---------|
| `NEWSLETTER_SERVICE_PORT` | Service port | `8085` |
| `BREVO_BASE_URL` | Brevo API base URL | - |
| `BREVO_API_KEY` | Brevo API key | - |
| `BREVO_NEWSLETTER_LIST_ID` | Brevo list ID for newsletter | - |
| `RABBITMQ_HOST` | RabbitMQ host | `localhost` |
| `RABBITMQ_PORT` | RabbitMQ port | `5672` |
| `RABBITMQ_USER` | RabbitMQ user | `guest` |
| `RABBITMQ_PASSWORD` | RabbitMQ password | `guest` |

## Gateway

Set `NEWSLETTER_SERVICE_URI=http://localhost:8085` (or `http://newsletter-service:8085` in Docker) to route `/api/newsletter/**` to this service.

## Local Development

1. Start infrastructure:
   ```bash
   docker compose up -d postgres-newsletter rabbitmq
   ```

2. Run the service:
   ```bash
   cd services/newsletter-service
   mvn spring-boot:run -Dspring-boot.run.profiles=dev
   ```

3. Ensure `BREVO_BASE_URL`, `BREVO_API_KEY`, and `BREVO_NEWSLETTER_LIST_ID` are set (e.g. in `local.properties` or env).

## Event Schema

Consumes `MembershipCreated` (CloudEvents-style) from `membership.events` exchange, routing key `membership.created`. See `contracts/schemas/events/membership-created.json`.
