# User Service

User profile, roles, newsletter subscription flag. Extracted in Phase 2.2.

## API

- `GET /api/user/info` - Returns user profile (sub, email, name, newsletterSubscription, roles). Requires JWT (gateway validates and forwards X-User-Id).

## Configuration

- **Database**: Shares `auth_db` with Auth Service (same `mmhc_user`, `role`, `user_role` tables).
- **Port**: `USER_SERVICE_PORT` (default 8083).
- **Gateway**: Set `USER_SERVICE_URI=http://localhost:8083` (or `http://user-service:8083` in Docker) to route `/api/user/**` from the gateway to this service.

## Local Development

1. Start infrastructure: `docker compose up -d redis postgres-auth`
2. Run auth-service first (owns schema, runs migrations).
3. Run user-service: `cd services/user-service && mvn spring-boot:run`
4. Set `USER_SERVICE_URI=http://localhost:8083` when starting the gateway.
