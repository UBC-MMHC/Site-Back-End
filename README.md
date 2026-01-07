# UBC MMHC Site Back-End

The backend API for the UBC Men's Mental Health Club (MMHC) website. Built with Java 21 and Spring Boot, this application provides secure authentication, user management, and core functionality for the MMHC platform.

## Key Features

- **Secure Authentication**: Integration with Google OAuth2 for seamless login, backed by JWT (JSON Web Tokens) for stateless session management.
- **Role-Based Access Control**: Granular permissions for Users and Admins.
- **Security**: 
  - CSRF Protection (specialized for React/SPA frontends).
  - Rate Limiting using Bucket4j to prevent abuse.
- **Data Management**: Robust user and content management using Spring Data JPA and PostgreSQL.
- **Newsletter**: Backend support for newsletter operations (Integrated with Brevo).

## Tech Stack

- **Language**: Java 21
- **Framework**: Spring Boot 3
- **Database**: PostgreSQL (Production), H2 (In-Memory/Test)
- **Security**: Spring Security 6, OAuth2 Client, JWT
- **Build Tool**: Maven

## Prerequisites

Ensure you have the following installed:
- [Java 21 SDK](https://www.oracle.com/java/technologies/downloads/#java21)
- [Maven](https://maven.apache.org/download.cgi) (or use the included `mvnw` wrapper)
- [PostgreSQL](https://www.postgresql.org/download/)

## Configuration

The application requires several environment variables to function correctly. You can set these in your IDE run configuration or in a `.env` file if you are using a loader.

Refer to `src/main/resources/example.local.properties` for a template.

**Required Variables:**

| Variable | Description |
|----------|-------------|
| `POSTGRES_DB` | Database name (e.g., `mmhc`) |
| `POSTGRES_USER` | Database user |
| `POSTGRES_PASSWORD` | Database password |
| `GOOGLE_CLIENT_ID` | Google OAuth2 Client ID |
| `GOOGLE_CLIENT_SECRET` | Google OAuth2 Client Secret |
| `JWT_SECRET_TOKEN` | Secret key for signing JWTs (256-bit min) |
| `BREVO_API_KEY` | API Key for Brevo email service |
| `FRONTEND_URL` | URL of the frontend application (for CORS/Redirects) |
| `APPLICATION_PROFILE` | `dev` or `prod` |

**Database Connection (Railway/Remote):**
If using a remote DB like Railway, configure:
- `RAILWAY_TCP_PROXY_DOMAIN`
- `RAILWAY_TCP_PROXY_PORT`

## Getting Started

1.  **Clone the repository:**
    ```bash
    git clone https://github.com/UBC-MMHC/Site-Back-End.git
    cd Site-Back-End
    ```

2.  **Configure the application:**
    Set the required environment variables as described in the Configuration section.

3.  **Build the project:**
    ```bash
    ./mvnw clean install
    ```

4.  **Run the application:**
    ```bash
    ./mvnw spring-boot:run
    ```
    The server will start on port `8080` (default).

## Useful Commands

```bash
# Full Rebuild
./mvnw clean install

# Compile Only
./mvnw -q compile

# Run with Debugging
./mvnw spring-boot:run --debug

# Run Tests
./mvnw test
```
