package com.ubcmmhcsoftware.user.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.util.Map;

/**
 * Converts Railway's DATABASE_URL (postgresql://...) to JDBC format (jdbc:postgresql://...)
 * when DATABASE_URL is set. This allows using ${{Postgres.DATABASE_URL}} as a single variable.
 */
public class RailwayDatabaseConfig implements EnvironmentPostProcessor {

    private static final String DATABASE_URL = "DATABASE_URL";
    private static final String SPRING_DATASOURCE_URL = "spring.datasource.url";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        String databaseUrl = environment.getProperty(DATABASE_URL);
        if (databaseUrl != null && !databaseUrl.isBlank()) {
            String jdbcUrl = toJdbcUrl(databaseUrl);
            environment.getPropertySources().addFirst(
                new MapPropertySource("railwayDatabase", Map.of(SPRING_DATASOURCE_URL, jdbcUrl)));
        }
    }

    private static String toJdbcUrl(String url) {
        String trimmed = url.trim();
        if (trimmed.startsWith("jdbc:")) {
            return trimmed;
        }
        if (trimmed.startsWith("postgres://")) {
            return "jdbc:postgresql://" + trimmed.substring(11);
        }
        if (trimmed.startsWith("postgresql://")) {
            return "jdbc:" + trimmed;
        }
        return "jdbc:postgresql://" + trimmed;
    }
}
