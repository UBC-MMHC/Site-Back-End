package com.ubcmmhcsoftware.platform;

import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RailwayDatabaseConfigTest {

    private final RailwayDatabaseConfig processor = new RailwayDatabaseConfig();

    @Test
    void parsesPostgresUrlIntoJdbc() {
        Map<String, Object> props = RailwayDatabaseConfig.parseDatabaseUrl(
                "postgresql://mmhc:s3cret@postgres.railway.internal:5432/site_db");
        assertThat(props).isNotNull();
        assertThat(props.get("spring.datasource.url"))
                .isEqualTo("jdbc:postgresql://postgres.railway.internal:5432/site_db?sslmode=require");
        assertThat(props.get("spring.datasource.username")).isEqualTo("mmhc");
        assertThat(props.get("spring.datasource.password")).isEqualTo("s3cret");
    }

    @Test
    void ignoresUnexpandedPlaceholders() {
        MockEnvironment env = new MockEnvironment();
        env.setProperty("DATABASE_URL", "postgresql://$PGHOST:5432/site_db");
        env.setProperty("PGHOST", "$PGHOST");
        env.setProperty("PGPORT", "$PGPORT");
        assertThat(RailwayDatabaseConfig.resolve(env)).isNull();
    }

    @Test
    void usesPgHostWhenDatabaseUrlMissing() {
        MockEnvironment env = new MockEnvironment();
        env.setProperty("PGHOST", "postgres.railway.internal");
        env.setProperty("PGPORT", "5432");
        env.setProperty("PGDATABASE", "site_db");
        env.setProperty("PGUSER", "postgres");
        env.setProperty("PGPASSWORD", "pw");
        Map<String, Object> props = RailwayDatabaseConfig.resolve(env);
        assertThat(props).isNotNull();
        assertThat(props.get("spring.datasource.url"))
                .isEqualTo("jdbc:postgresql://postgres.railway.internal:5432/site_db?sslmode=require");
    }

    @Test
    void prodWithoutDatabaseThrows() {
        MockEnvironment env = new MockEnvironment();
        env.setActiveProfiles("prod");
        assertThatThrownBy(() -> processor.postProcessEnvironment(env, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("DATABASE_URL");
    }
}
