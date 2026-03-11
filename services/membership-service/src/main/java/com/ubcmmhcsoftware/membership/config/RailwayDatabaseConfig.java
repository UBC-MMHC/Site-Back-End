package com.ubcmmhcsoftware.membership.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.util.HashMap;
import java.util.Map;

/**
 * Parses Railway's DATABASE_URL (postgresql://user:pass@host:port/db) and sets
 * spring.datasource.url, username, password. The PostgreSQL JDBC driver does not
 * accept user:password@host in the URL, so we parse and set them separately.
 */
public class RailwayDatabaseConfig implements EnvironmentPostProcessor {

    private static final String DATABASE_URL = "DATABASE_URL";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        String databaseUrl = environment.getProperty(DATABASE_URL);
        if (databaseUrl != null && !databaseUrl.isBlank()) {
            Map<String, Object> props = parseDatabaseUrl(databaseUrl.trim());
            if (props != null) {
                environment.getPropertySources().addFirst(
                    new MapPropertySource("railwayDatabase", props));
            }
        }
    }

    private static Map<String, Object> parseDatabaseUrl(String url) {
        try {
            if (url.startsWith("jdbc:")) {
                return null;
            }
            String normalized = url.startsWith("postgres://") ? "postgresql://" + url.substring(11) : url;
            String afterProtocol = normalized.replaceFirst("^postgresql://", "");
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
}
