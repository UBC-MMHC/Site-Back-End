# JWT Validation Library

Shared JWT validation for UBC MMHC microservices. **Auth Service is the sole JWT issuer**; all downstream services validate tokens using the same `JWT_SECRET_TOKEN`.

## Features

- **JwtValidator**: Validates signature and expiry, extracts claims (userId, email, roles)
- **JwtClaims**: Immutable DTO aligned with `contracts/schemas/jwt-claims.json`
- **JwtTokenExtractor**: Extracts token from Bearer header or cookie
- **JwtValidationFilter**: Spring Servlet filter for downstream services

## Usage

### Core (no Spring)

```java
JwtValidator validator = new JwtValidator(secret);
String token = JwtTokenExtractor.extract(authHeader, cookieValue);
JwtClaims claims = validator.validateAndExtractOrNull(token);
if (claims != null) {
    String userId = claims.userId();
    String email = claims.email();
    List<String> roles = claims.roles();
}
```

### Spring Boot (Servlet services)

1. Add dependency:

```xml
<dependency>
    <groupId>com.ubcmmhcsoftware</groupId>
    <artifactId>jwt-validation</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

2. Configure in `application.yml`:

```yaml
app:
  jwt:
    secret: ${JWT_SECRET_TOKEN}
  jwt-cookie:
    name: ${JWT_COOKIE_NAME:JWT}
```

3. Import config and add filter to SecurityFilterChain:

```java
@Import(JwtValidationConfig.class)
@Configuration
public class SecurityConfig {
    @Bean
    public SecurityFilterChain apiFilterChain(HttpSecurity http, JwtValidationFilter jwtFilter) {
        return http
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
            // ... other config
            .build();
    }
}
```

4. Access claims in controllers:

```java
@GetMapping("/me")
public UserInfo me(Authentication auth) {
    JwtClaims claims = (JwtClaims) auth.getPrincipal();
    return userService.getInfo(claims.userId());
}
```

## Gateway vs Direct Validation

- **Via Gateway**: The gateway validates JWT and forwards `X-User-Id`, `X-User-Email`, `X-User-Roles` to downstream services. Services can trust these headers when traffic comes only through the gateway.
- **Direct / Defense-in-depth**: Use `JwtValidationFilter` when services may be called directly or for extra validation.

## Rust

See `contracts/docs/rust-jwt-validation.md` for using `jsonwebtoken` with the same secret.
