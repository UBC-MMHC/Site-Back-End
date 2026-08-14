package com.ubcmmhcsoftware.platform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.Profiles;

import java.util.HashMap;
import java.util.Map;

/**
 * Resolves Railway Postgres into spring.datasource.*. Prefers DATABASE_URL
 * (or DATABASE_PRIVATE_URL). Falls back to PGHOST/PGPORT/PGDATABASE/PGUSER/PGPASSWORD.
 * Values that still contain ${...} are treated as unset.
 */
public class RailwayDatabaseConfig implements EnvironmentPostProcessor {

    static final String PROPERTY_SOURCE = "railwayDatabase";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        Map<String, Object> props = resolve(environment);
        if (props != null) {
            environment.getPropertySources().addFirst(new MapPropertySource(PROPERTY_SOURCE, props));
            return;
        }
        if (environment.acceptsProfiles(Profiles.of("prod"))) {
            throw new IllegalStateException(
                    "Postgres is not configured. On the Railway backend service, add a variable "
                            + "reference to the Postgres plugin (DATABASE_URL) or set PGHOST, PGPORT, "
                            + "PGDATABASE, PGUSER, and PGPASSWORD to the real host/user/password. "
                            + "Do not use ${PGHOST} or ${{Postgres.PGHOST}} as the stored value unless "
                            + "Railway actually interpolates that reference.");
        }
    }

    static Map<String, Object> resolve(ConfigurableEnvironment environment) {
        for (String key : new String[] {"DATABASE_PRIVATE_URL", "DATABASE_URL", "POSTGRES_URL"}) {
            Map<String, Object> fromUrl = parseDatabaseUrl(usable(environment.getProperty(key)));
            if (fromUrl != null) {
                return fromUrl;
            }
        }

        String host = firstUsable(environment, "PGHOST", "POSTGRES_HOST");
        if (host == null) {
            return null;
        }
        String port = firstUsable(environment, "PGPORT", "POSTGRES_PORT");
        if (port == null) {
            port = "5432";
        }
        String database = firstUsable(environment, "PGDATABASE", "POSTGRES_DB", "POSTGRES_DATABASE");
        if (database == null) {
            database = "railway";
        }
        String user = firstUsable(environment, "PGUSER", "POSTGRES_USER");
        String password = firstUsable(environment, "PGPASSWORD", "POSTGRES_PASSWORD");
        String sslMode = firstUsable(environment, "SSL_MODE");
        if (sslMode == null) {
            sslMode = "require";
        }

        String jdbcUrl = "jdbc:postgresql://" + host + ":" + port + "/" + database + "?sslmode=" + sslMode;
        Map<String, Object> props = new HashMap<>();
        props.put("spring.datasource.url", jdbcUrl);
        if (user != null) {
            props.put("spring.datasource.username", user);
        }
        if (password != null) {
            props.put("spring.datasource.password", password);
        }
        return props;
    }

    static Map<String, Object> parseDatabaseUrl(String url) {
        if (url == null) {
            return null;
        }
        try {
            if (url.startsWith("jdbc:postgresql://") || url.startsWith("jdbc:postgres://")) {
                Map<String, Object> props = new HashMap<>();
                props.put("spring.datasource.url", url.replace("jdbc:postgres://", "jdbc:postgresql://"));
                return props;
            }
            String normalized = url.startsWith("postgres://") ? "postgresql://" + url.substring(11) : url;
            if (!normalized.startsWith("postgresql://")) {
                return null;
            }
            String afterProtocol = normalized.substring("postgresql://".length());
            int lastAt = afterProtocol.lastIndexOf('@');
            if (lastAt < 0) {
                return null;
            }
            String userInfo = afterProtocol.substring(0, lastAt);
            String hostPart = afterProtocol.substring(lastAt + 1);
            int firstColon = userInfo.indexOf(':');
            String user = firstColon >= 0 ? userInfo.substring(0, firstColon) : userInfo;
            String password = firstColon >= 0 ? userInfo.substring(firstColon + 1) : null;

            String host;
            int port = 5432;
            String database = "railway";
            int slash = hostPart.indexOf('/');
            String hostPort = slash >= 0 ? hostPart.substring(0, slash) : hostPart;
            if (slash >= 0 && slash + 1 < hostPart.length()) {
                database = hostPart.substring(slash + 1).split("\\?")[0];
            }
            int colon = hostPort.lastIndexOf(':');
            if (colon >= 0) {
                host = hostPort.substring(0, colon);
                try {
                    port = Integer.parseInt(hostPort.substring(colon + 1).split("/")[0]);
                } catch (NumberFormatException ignored) {
                }
            } else {
                host = hostPort;
            }

            String jdbcUrl = "jdbc:postgresql://" + host + ":" + port + "/" + database;
            if (hostPart.contains("?")) {
                jdbcUrl += "?" + hostPart.substring(hostPart.indexOf('?') + 1);
            } else {
                jdbcUrl += "?sslmode=require";
            }

            Map<String, Object> props = new HashMap<>();
            props.put("spring.datasource.url", jdbcUrl);
            props.put("spring.datasource.username", user);
            props.put("spring.datasource.password", password != null ? password : "");
            return props;
        } catch (Exception e) {
            return null;
        }
    }

    private static String firstUsable(ConfigurableEnvironment environment, String... keys) {
        for (String key : keys) {
            String value = usable(environment.getProperty(key));
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    private static String usable(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.contains("${") || trimmed.contains("$PG") || trimmed.contains("$DATABASE")) {
            return null;
        }
        return trimmed;
    }
}
