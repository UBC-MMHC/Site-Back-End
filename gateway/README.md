# API Gateway

Spring Cloud Gateway serving as the single entry point for the UBC MMHC backend.

## Features

- **Routing**: Proxies requests to microservices (auth, user, membership, newsletter)
- **JWT validation**: Validates JWT from Bearer header or cookie, forwards claims (`X-User-Id`, `X-User-Email`, `X-User-Roles`) to downstream
- **Rate limiting**: Redis-backed per-IP rate limiting (100 req/5 min)
- **CORS**: Configured for frontend origin

## Requirements

- Java 21
- Redis (for rate limiting; optional in dev with `dev-no-redis` profile)
- Microservices running: auth (8082), user (8083), membership (8084), newsletter (8085)

## Configuration

| Variable | Description | Default |
|----------|-------------|---------|
| `GATEWAY_PORT` | Gateway server port | `8080` |
| `AUTH_SERVICE_URI` | Auth service URL | `http://localhost:8082` |
| `USER_SERVICE_URI` | User service URL | `http://localhost:8083` |
| `MEMBERSHIP_SERVICE_URI` | Membership service URL | `http://localhost:8084` |
| `NEWSLETTER_SERVICE_URI` | Newsletter service URL | `http://localhost:8085` |
| `FRONTEND_URL` | Allowed CORS origin | `http://localhost:3000` |
| `JWT_SECRET_TOKEN` | Shared JWT secret (same as Auth Service) | required |
| `JWT_COOKIE_NAME` | Cookie name for JWT | `JWT` |
| `REDIS_HOST` | Redis host for rate limiting | `localhost` |
| `REDIS_PORT` | Redis port | `6379` |
| `REDIS_PASSWORD` | Redis password | (empty) |

## Running Locally

### With Redis (recommended)

```bash
# Terminal 1: Start infrastructure
docker compose up -d redis postgres rabbitmq

# Start each service (in separate terminals)
cd services/auth-service && mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=8082"
cd services/user-service && mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=8083"
cd services/membership-service && mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=8084"
cd services/newsletter-service && mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=8085"

# Start gateway
cd gateway && mvn spring-boot:run
```

### Without Redis (dev profile)

The `dev` profile (default) omits rate limiting, so Redis is optional for local development:

```bash
cd gateway && mvn spring-boot:run
```

## Route Mapping

| Path | Service |
|------|---------|
| `/api/auth/**`, `/login/**`, `/oauth2/**` | Auth (8082) |
| `/api/user/**` | User (8083) |
| `/api/blog/**` | User (8083) |
| `/api/membership/**` | Membership (8084) |
| `/api/stripe/webhook` | Membership (8084, no rate limit) |
| `/api/newsletter/**` | Newsletter (8085) |
| `/api/admin/memberships/**` | Membership (8084) |
| `/api/admin/users/**` | User (8083) |
