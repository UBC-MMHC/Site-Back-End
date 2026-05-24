# Membership Service

Membership lifecycle, Stripe checkout, and webhook handling. Extracted as Phase 2.3 of the monolith-to-microservices migration.

## Responsibilities

- Membership registration (guest and linked to user)
- Stripe Checkout Session creation
- Stripe webhook (`checkout.session.completed`)
- Manual admin approval (cash/e-transfer)
- **MembershipCreated** event publishing (newsletter opt-in) via RabbitMQ

## Database

- **membership_db** with `membership` table
- No FK to `mmhc_user`; links via optional `user_id` (UUID) and required `email`

## API

| Endpoint | Auth | Description |
|----------|------|-------------|
| `POST /api/membership/register` | Public | Register membership, get Stripe URL |
| `GET /api/membership/status` | JWT | Current user's membership details |
| `GET /api/membership/check?email=` | Public | Check if email has active membership |
| `GET /api/membership/my-status` | JWT | Summary for frontend gating |
| `POST /api/membership/retry-payment` | JWT | Create retry checkout session |
| `POST /api/stripe/webhook` | None | Stripe webhook (no JWT) |
| `GET /api/admin/memberships/pending` | Admin | Pending memberships |
| `POST /api/admin/memberships/{email}/approve` | Admin | Manual approval |

## Events

Publishes `MembershipCreated` (CloudEvents format) to RabbitMQ when a membership is created with `newsletterOptIn=true`. Newsletter Service (Phase 2.4) subscribes.

## Run Locally

1. Start infra: `docker compose up -d redis postgres rabbitmq`
2. Build: `mvn -f services/membership-service/pom.xml package`
3. Run: `MEMBERSHIP_SERVICE_PORT=8084 java -jar services/membership-service/target/membership-service-*.jar`

## Environment

- `MEMBERSHIP_SERVICE_PORT` (default 8084)
- `FRONTEND_URL`, `STRIPE_*`, `POSTGRES_*`, `RABBITMQ_*`
- `USER_SERVICE_URL` — **required in production** (e.g. `http://user-service.railway.internal:8083`). Defaults to `http://localhost:8083`. When logged in, register attempts a best-effort user-service exists check; if this points at `localhost` inside Railway or is otherwise unreachable, the check is skipped/logged and registration continues.
- `INTERNAL_SERVICE_KEY` — shared secret with user-service for `/api/user/internal/exists/{userId}`. Must match user-service’s `INTERNAL_SERVICE_KEY`. If missing or invalid on user-service, that internal exists check may fail (for example with **503**), and membership-service logs/skips the verification rather than failing registration.
