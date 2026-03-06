package com.ubcmmhcsoftware.jwt;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("JwtTokenExtractor")
class JwtTokenExtractorTest {

    @Test
    @DisplayName("extracts from Bearer header")
    void extractsFromBearer() {
        assertThat(JwtTokenExtractor.extract("Bearer my-token", null)).isEqualTo("my-token");
        assertThat(JwtTokenExtractor.extract("Bearer  abc ", null)).isEqualTo("abc");
    }

    @Test
    @DisplayName("prefers Bearer over cookie")
    void prefersBearerOverCookie() {
        assertThat(JwtTokenExtractor.extract("Bearer token", "cookie-token")).isEqualTo("token");
    }

    @Test
    @DisplayName("falls back to cookie when no Bearer")
    void fallsBackToCookie() {
        assertThat(JwtTokenExtractor.extract(null, "cookie-token")).isEqualTo("cookie-token");
        assertThat(JwtTokenExtractor.extract("", "cookie-token")).isEqualTo("cookie-token");
    }

    @Test
    @DisplayName("returns null when neither present")
    void returnsNullWhenNeitherPresent() {
        assertThat(JwtTokenExtractor.extract(null, null)).isNull();
        assertThat(JwtTokenExtractor.extract("", "")).isNull();
        assertThat(JwtTokenExtractor.extract("NotBearer x", null)).isNull();
    }
}
