# Rust JWT Validation

Auth Service is the **sole JWT issuer**. Rust services validate JWTs using the same `JWT_SECRET_TOKEN`—no service-to-service call needed.

## Dependencies

Add to `Cargo.toml`:

```toml
[dependencies]
jsonwebtoken = "9"
serde = { version = "1", features = ["derive"] }
```

## Claims struct (aligns with `schemas/jwt-claims.json`)

```rust
use serde::{Deserialize, Serialize};

#[derive(Debug, Serialize, Deserialize)]
pub struct JwtClaims {
    pub sub: String,           // User ID (UUID)
    #[serde(default)]
    pub email: Option<String>,
    #[serde(default)]
    pub roles: Vec<String>,
    pub exp: i64,               // Expiration (Unix timestamp)
    pub iat: i64,               // Issued at (Unix timestamp)
}
```

## Validation

```rust
use jsonwebtoken::{decode, DecodingKey, Validation};

fn validate_token(token: &str, secret: &str) -> Result<JwtClaims, jsonwebtoken::errors::Error> {
    let key = DecodingKey::from_secret(secret.as_bytes());
    let mut validation = Validation::default();
    validation.set_algorithm(jsonwebtoken::Algorithm::HS256);

    let token_data = decode::<JwtClaims>(token, &key, &validation)?;
    Ok(token_data.claims)
}
```

## Extraction from request

Extract from `Authorization: Bearer <token>` or from the JWT cookie:

```rust
// Axum example
fn extract_token(headers: &HeaderMap, cookies: Option<&str>) -> Option<&str> {
    if let Some(auth) = headers.get("Authorization") {
        if let Ok(s) = auth.to_str() {
            if let Some(token) = s.strip_prefix("Bearer ") {
                return Some(token.trim());
            }
        }
    }
    cookies
}
```

## Environment

Set `JWT_SECRET_TOKEN` (same value as Auth Service). Secret must be at least 256 bits (32 chars) for HS256.
