# API Gateway

Spring Cloud Gateway serving as the single entry point for the UBC MMHC backend. Part of Phase 1.2 of the monolith-to-microservices migration.

## Features

- **Routing**: Proxies requests to backend services (monolith during Phase 1)
- **JWT validation**: Validates JWT from Bearer header or cookie, forwards claims (`X-User-Id`, `X-User-Email`, `X-User-Roles`) to downstream
- **Rate limiting**: Redis-backed per-IP rate limiting (100 req/5 min)
- **CORS**: Configured for frontend origin

## Requirements

- Java 21
- Redis (for rate limiting; optional in dev with `dev-no-redis` profile)
- Backend monolith running (default: `http://localhost:8081`)

## Configuration

| Variable | Description | Default |
|----------|-------------|---------|
| `GATEWAY_PORT` | Gateway server port | `8080` |
| `BACKEND_URI` | Backend monolith URL | `http://localhost:8081` |
| `FRONTEND_URL` | Allowed CORS origin | `http://localhost:3000` |
| `JWT_SECRET_TOKEN` | Shared JWT secret (same as Auth Service) | required |
| `JWT_COOKIE_NAME` | Cookie name for JWT | `JWT` |
| `REDIS_HOST` | Redis host for rate limiting | `localhost` |
| `REDIS_PORT` | Redis port | `6379` |
| `REDIS_PASSWORD` | Redis password | (empty) |

## Running Locally

### With Redis (recommended)

```bash
# Terminal 1: Start Redis (Docker)
docker run -d -p 6379:6379 redis:7-alpine

# Terminal 2: Start monolith on 8081
cd .. && mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=8081"

# Terminal 3: Start gateway on 8080
cd gateway && mvn spring-boot:run
```

### Without Redis (dev profile)

The `dev` profile (default) omits rate limiting, so Redis is optional for local development:

```bash
mvn spring-boot:run
```

## Route Mapping

| Path | Backend |
|------|---------|
| `/api/auth/**`, `/login/**`, `/oauth2/**` | Auth |
| `/api/user/**` | User |
| `/api/membership/**` | Membership |
| `/api/stripe/webhook` | Membership (no rate limit) |
| `/api/newsletter/**` | Newsletter |
| `/api/admin/memberships/**` | Membership |
| `/api/admin/users/**` | User |
| `/api/blog/**` | Blog |
