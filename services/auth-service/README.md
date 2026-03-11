# Auth Service

Phase 2.1 extracted service for user identity, JWT issuance, OAuth2, and password reset.

## Responsibilities

- **User identity**: id, email, googleId, password, name
- **JWT issuance**: Token generation and signing
- **OAuth2**: Google login with JIT provisioning
- **Password reset**: Verification tokens and Brevo email

## API Endpoints

| Method | Path | Description |
|--------|------|-------------|
| POST | /api/auth/register-user | Register with email/password |
| POST | /api/auth/login-user | Login with email/password |
| POST | /api/auth/forgot-password | Request password reset email |
| POST | /api/auth/reset-password | Reset password with token |
| POST | /api/auth/logout | Clear JWT cookie |
| POST | /api/auth/set-token | Set JWT cookie (OAuth2 callback) |
| GET | /api/auth/me | Current user email |

## Database (auth_db)

- `mmhc_user` - Identity fields only
- `role` - Security roles
- `user_role` - User-role mapping
- `verification_token` - Password reset tokens

## Configuration

| Variable | Description |
|----------|-------------|
| AUTH_SERVICE_PORT | Port (default: 8082) |
| JWT_SECRET_TOKEN | Shared secret for JWT signing |
| GOOGLE_CLIENT_ID | OAuth2 client ID |
| GOOGLE_CLIENT_SECRET | OAuth2 client secret |
| FRONTEND_URL | Frontend base URL for redirects |
| BREVO_API_KEY | Brevo API key for emails |
| SMTP_SENDER_EMAIL | Sender email for Brevo |

## Running Locally

```bash
# With PostgreSQL (create auth_db first)
export POSTGRES_DB=auth_db
export POSTGRES_USER=postgres
export POSTGRES_PASSWORD=postgres
export JWT_SECRET_TOKEN=your-256-bit-secret-at-least-32-chars
export FRONTEND_URL=http://localhost:3000
export GOOGLE_CLIENT_ID=...
export GOOGLE_CLIENT_SECRET=...
export BREVO_API_KEY=...
export SMTP_SENDER_EMAIL=...
mvn spring-boot:run

# With H2 in-memory (no DB setup)
APPLICATION_PROFILE=local mvn spring-boot:run
```

## Gateway Integration

Set `AUTH_SERVICE_URI=http://auth-service:8082` (or `http://localhost:8082` for local) to route auth traffic from the gateway to this service.
