# UBC Men's Mental Health Club Site Back-End

Single Spring Boot 3.5 process on port **8080**. Domain code lives in packages (`identity`, `auth`, `user`, `membership`, `newsletter`) so a service can be split out later without changing public HTTP paths.

The Next.js frontend still calls `NEXT_PUBLIC_BASE_URL_API` (default `http://localhost:8080`). Path prefixes are unchanged: `/api/auth`, `/api/user`, `/api/membership`, `/api/newsletter`, `/api/blog`, `/api/admin`, `/api/stripe/webhook`, `/login`, `/oauth2`.

## Run locally

```bash
docker compose up -d postgres
# dev profile (default) talks to localhost:5432/site_db
mvn -pl app spring-boot:run
```

In-memory H2 (no Docker):

```bash
mvn -pl app spring-boot:run -Dspring-boot.run.profiles=local
```

## Tests

```bash
mvn test
```

| Test | What it covers |
|------|----------------|
| `ModulithStructureTests` | Package boundaries are acyclic (Spring Modulith `verify()`). |
| `SiteApplicationTests.contextLoads` | Full Spring context on the `local` profile (H2). |
| `registerLoginAndFetchProfile` | `/api/auth/register-user`, `/login-user`, JWT cookie, `/api/user/info`. |
| `duplicateRegisterReturnsConflict` | Existing email returns 409. |
| `newsletterSubscribePersistsEvenIfBrevoIsUnreachable` | `/api/newsletter/add-email` writes `newsletter_subscriber` when Brevo is down. |
| `cashMembershipRegisterAndPublicCheck` | `/api/membership/register` with `CASH` (no Stripe) and public `/check`. |
| `userInfoWithoutTokenIsUnauthorized` / `membershipStatusRequiresAuth` / `adminRoutesRequireAdminRole` | Protected routes return 401 without a JWT. |

GitHub Actions runs `mvn -B test` on every push (`.github/workflows/test.yml`).

## Railway (Railpack)

Point the backend service at branch `feat/modular-monolith` (or `main` after merge). Root Directory must be the repo root, not `gateway` or `services/...`. `main` still has the old microservices tree and no root `pom.xml`, so Railpack cannot detect Java.

`railpack.json` builds the `app` module and starts `target/app.jar` on `$PORT`.

Cutover: deploy onto the current **gateway** service so the public hostname, Stripe webhook, and Google redirect URI stay the same. Then delete the old auth/user/membership/newsletter/Redis/RabbitMQ services.

Production Flyway uses table `flyway_schema_history` and `baseline-version: 5`. Do not replay V1–V5 against the existing Railway database; those tables already exist from `flyway_auth_schema_history`, `flyway_membership_schema_history`, and `flyway_newsletter_schema_history`.
