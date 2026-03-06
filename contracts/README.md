# Shared API Contracts

This module contains OpenAPI 3.0 specifications and JSON schemas for all domain APIs. It enables any language (Java, Rust, etc.) to implement or consume APIs consistently.

## Structure

```
contracts/
├── openapi/           # OpenAPI 3.0 specs per domain
│   ├── auth.yaml
│   ├── user.yaml
│   ├── membership.yaml
│   ├── newsletter.yaml
│   ├── stripe-webhook.yaml
│   └── admin.yaml
├── schemas/           # Shared DTOs and event schemas
│   ├── dto/           # Request/response DTOs
│   └── jwt-claims.json
└── README.md
```

## JWT Claims

All services validate JWTs using the same secret (`JWT_SECRET_TOKEN`). See `schemas/jwt-claims.json` for the canonical claims schema.

- **Java**: Use the `jwt-validation` library (see `../jwt-validation/README.md`)
- **Rust**: Use `jsonwebtoken` crate—see `docs/rust-jwt-validation.md`

## Usage

- **Java/Spring**: Use OpenAPI Generator or springdoc-openapi to generate clients
- **Rust**: Use `openapi-generator` or manual serde structs from schemas
- **Frontend**: Generate TypeScript clients from OpenAPI specs
