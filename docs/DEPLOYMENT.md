# Microservices Deployment Guide

Deploying the UBC MMHC backend (Gateway + Auth, User, Membership, Newsletter services) to Railway and connecting the front-end.

---

## Architecture Overview

```
Frontend → Gateway (public) → Auth / User / Membership / Newsletter (internal)
                ↓
         Redis (rate limit) + PostgreSQL (shared) + RabbitMQ (events)
```

- **Gateway**: Single public entry point. Validates JWT, routes requests, rate limits.
- **Services**: Internal-only. Reachable via `SERVICE_NAME.railway.internal:PORT`.

---

## 1. Railway Infrastructure

### 1.0 Quick: Connect Database to Microservices

Your monolith used one Postgres. After splitting into microservices, **each service needs the database variables**.

1. **Add PostgreSQL** (if not already): Project Canvas → `+ New` → `Database` → `PostgreSQL`
2. **Link Postgres to each app service:**
   - Open **[Auth] Site-Back-End** → **Variables** → **+ New Variable**
   - Add variable references (replace `Postgres` with your Postgres service name):
     - `PGHOST` = `${{Postgres.PGHOST}}`
     - `PGPORT` = `${{Postgres.PGPORT}}`
     - `PGDATABASE` = `${{Postgres.PGDATABASE}}`
     - `PGUSER` = `${{Postgres.PGUSER}}`
     - `PGPASSWORD` = `${{Postgres.PGPASSWORD}}`
     - `SSL_MODE` = `require`
   - Repeat for **[User]**, **[Membership]**, **[Newsletter]** Site-Back-End services

3. **Redeploy** each service after adding variables.

---

### 1.1 Add Services (Project Canvas)

| Service | Source | Notes |
|---------|--------|-------|
| **PostgreSQL** | Template: `+ New` → PostgreSQL | You already have this |
| **Redis** | Template: `+ New` → Redis | For gateway rate limiting |
| **RabbitMQ** | Template: [RabbitMQ Starter](https://railway.com/deploy/rabbitmq) | For MembershipCreated events |

### 1.2 Deploy Application Services

Create **5 services** from this repo (one per app). Use **Root Directory** = repo root for all, so Maven can resolve the parent POM.

| Service Name | Maven Module | Port | Start Command |
|--------------|--------------|------|---------------|
| `gateway` | `gateway` | 8080 | `java -jar gateway/target/gateway-*.jar` |
| `auth-service` | `services/auth-service` | 8082 | `java -jar services/auth-service/target/auth-service-*.jar` |
| `user-service` | `services/user-service` | 8083 | `java -jar services/user-service/target/user-service-*.jar` |
| `membership-service` | `services/membership-service` | 8084 | `java -jar services/membership-service/target/membership-service-*.jar` |
| `newsletter-service` | `services/newsletter-service` | 8085 | `java -jar services/newsletter-service/target/newsletter-service-*.jar` |

**Build settings** (per service):

- **Root Directory**: `/` (repo root)
- **Build Command**: `mvn -pl <module> -am clean package -DskipTests` (e.g. `mvn -pl gateway -am clean package -DskipTests`)
- **Start Command**: See table above

**Dockerfile configuration** (each service uses its directory as build context):

| Service | Root Directory | Dockerfile Path |
|---------|----------------|-----------------|
| `gateway` | `gateway` | `Dockerfile` |
| `auth-service` | `services/auth-service` | `Dockerfile` |
| `user-service` | `services/user-service` | `Dockerfile` |
| `membership-service` | `services/membership-service` | `Dockerfile` |
| `newsletter-service` | `services/newsletter-service` | `Dockerfile` |

Set **Root Directory** to the service folder so the build context contains only that service's `pom.xml` and `src/`. The Dockerfile path is `Dockerfile` (relative to Root Directory).

---

## 2. Environment Variables

### 2.1 Shared Variables (Project Settings → Shared Variables)

Set once, share across services:

| Variable | Value | Used By |
|----------|-------|---------|
| `JWT_SECRET_TOKEN` | (secret, **min 32 chars** for HS256) | Gateway, Auth |
| `FRONTEND_URL` | `https://your-frontend.vercel.app` | Gateway, Auth, Membership |
| `INTERNAL_SERVICE_KEY` | (secret) | User, Membership |
| `APPLICATION_PROFILE` | `prod` | All |

### 2.2 PostgreSQL – Connect Each Service to the Database

**Step 1:** Add a PostgreSQL service to your project (`+ New` → `Database` → `PostgreSQL`).

**Step 2:** For each app service (auth, user, membership, newsletter), add a **variable reference** to the Postgres service:

1. Open the app service (e.g. **[Auth] Site-Back-End**)
2. Go to **Variables**
3. Click **+ New Variable** (or **Add Variable Reference**)
4. Add these variables, referencing your Postgres service (replace `Postgres` with your Postgres service name if different):

| Variable   | Value (Reference)        |
|-----------|---------------------------|
| `PGHOST`  | `${{Postgres.PGHOST}}`    |
| `PGPORT`  | `${{Postgres.PGPORT}}`   |
| `PGDATABASE` | `${{Postgres.PGDATABASE}}` |
| `PGUSER`  | `${{Postgres.PGUSER}}`   |
| `PGPASSWORD` | `${{Postgres.PGPASSWORD}}` |
| `SSL_MODE` | `require` (literal)      |

**Alternative:** Use **Connect** (or **Add Reference**) in the service settings to link the Postgres service—Railway may inject `PGHOST`, `PGPORT`, etc. automatically. If so, you only need to add `SSL_MODE=require`.

> All services can share the same Postgres instance. Railway creates one database by default; each service uses its own schema (Flyway tables are namespaced).

### 2.3 Redis (Gateway Only)

| Variable | Reference |
|----------|-----------|
| `REDIS_HOST` | `${{Redis.REDISHOST}}` |
| `REDIS_PORT` | `${{Redis.REDISPORT}}` |
| `REDIS_PASSWORD` | `${{Redis.REDISPASSWORD}}` (optional; default Redis has none) |

### 2.4 RabbitMQ (Membership, Newsletter)

Reference variables from your RabbitMQ service (name may vary, e.g. `rabbitmq`):

| Variable | Reference |
|----------|-----------|
| `RABBITMQ_HOST` | `${{RabbitMQ.RAILWAY_PRIVATE_DOMAIN}}` or service-specific host var |
| `RABBITMQ_PORT` | `5672` |
| `RABBITMQ_USER` | From RabbitMQ service vars |
| `RABBITMQ_PASSWORD` | From RabbitMQ service vars |

### 2.5 Per-Service Variables

**Gateway**

| Variable | Value |
|----------|-------|
| `AUTH_SERVICE_URI` | `http://auth-service.railway.internal:8082` |
| `USER_SERVICE_URI` | `http://user-service.railway.internal:8083` |
| `MEMBERSHIP_SERVICE_URI` | `http://membership-service.railway.internal:8084` |
| `NEWSLETTER_SERVICE_URI` | `http://newsletter-service.railway.internal:8085` |

**Auth Service**

| Variable | Value |
|----------|-------|
| `GOOGLE_CLIENT_ID` | (from Google OAuth) |
| `GOOGLE_CLIENT_SECRET` | (from Google OAuth) |
| `SMTP_SENDER_EMAIL` | (for password reset) |
| `BREVO_BASE_URL` | `https://api.brevo.com` |
| `BREVO_API_KEY` | (secret) |
| `JWT_COOKIE_SECURE` | `true` |
| `JWT_COOKIE_SAME_SITE` | `None` |

**User Service**

No additional vars beyond shared + Postgres.

**Membership Service**

| Variable | Value |
|----------|-------|
| `USER_SERVICE_URL` | `http://user-service.railway.internal:8083` |
| `STRIPE_SECRET_KEY` | (secret) |
| `STRIPE_WEBHOOK_SECRET` | (secret) |
| `STRIPE_PRICE_UBC_STUDENT` | (price ID) |
| `STRIPE_PRICE_NON_UBC_STUDENT` | (price ID) |
| `STRIPE_PRICE_NON_STUDENT` | (price ID) |

**Newsletter Service**

| Variable | Value |
|----------|-------|
| `BREVO_BASE_URL` | `https://api.brevo.com` |
| `BREVO_API_KEY` | (secret) |
| `BREVO_NEWSLETTER_LIST_ID` | (list ID) |

### 2.6 OAuth Redirect (Auth Service)

Update Google OAuth redirect URI to:

```
https://<GATEWAY_PUBLIC_DOMAIN>/login/oauth2/code/google
```

The gateway routes `/login/**` and `/oauth2/**` to the auth service.

### 2.7 Stripe Webhook

Set Stripe webhook URL to:

```
https://<GATEWAY_PUBLIC_DOMAIN>/api/stripe/webhook
```

---

## 3. Expose the Gateway

1. In the **gateway** service, enable **Public Networking**.
2. Add a **domain** (e.g. `api.yourdomain.com` or Railway-generated).
3. Use this URL as the backend base URL for the front-end.

---

## 4. Front-End Configuration

Point the front-end to the **gateway** URL only. All API calls go through it.

| Environment | Variable | Example |
|--------------|----------|---------|
| Production | `NEXT_PUBLIC_API_URL` or `VITE_API_URL` | `https://api.yourdomain.com` |
| Development | Same | `http://localhost:8080` |

**API paths** (unchanged from monolith):

- `POST /api/auth/register-user`
- `POST /api/auth/login-user`
- `GET /api/auth/me`
- `GET /api/user/info`
- `POST /api/membership/register`
- `GET /api/membership/my-status`
- `POST /api/newsletter/add-email`
- etc.

**CORS**: Gateway allows `FRONTEND_URL`. Ensure your front-end origin matches (e.g. `https://your-site.vercel.app`).

**Auth**: JWT in `Authorization: Bearer <token>` or `JWT` cookie. Gateway validates and forwards claims; front-end does not change.

---

## 5. Deployment Order

1. Deploy **Postgres, Redis, RabbitMQ** (if not already).
2. Deploy **auth-service** (runs Flyway; owns auth schema).
3. Deploy **user-service**, **membership-service**, **newsletter-service**.
4. Deploy **gateway** last (depends on all services).
5. Enable public domain on gateway.
6. Update front-end `API_URL` and OAuth/Stripe URLs.

---

## 6. Dockerfiles

All services have multi-stage Dockerfiles that build the JAR inside the image. Each Dockerfile expects its **Root Directory** to be the service folder (e.g. `services/auth-service`), so the build context contains `pom.xml` and `src/`. See table in §1.2.

---

## 7. Checklist

- [ ] Postgres, Redis, RabbitMQ running
- [ ] All 5 app services deployed with correct env vars
- [ ] Gateway has public domain
- [ ] `FRONTEND_URL` matches front-end origin
- [ ] Google OAuth redirect URI updated
- [ ] Stripe webhook URL updated
- [ ] Front-end `API_URL` points to gateway
- [ ] JWT cookie domain allows cross-origin (SameSite=None, Secure) if front-end and API are on different domains
