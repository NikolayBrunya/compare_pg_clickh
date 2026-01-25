package org.bnp.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class AppConfig {

    // ClickHouse
    public static String getClickhouseUrl() {
        String env = System.getenv("CLICKHOUSE_URL");
        if (env != null && !env.trim().isEmpty()) {
            return env;
        }
        return getProperty("clickhouse.url", "jdbc:clickhouse://localhost:8123/default");
    }

    // PostgreSQL
    public static String getPostgresUrl() {
        String env = System.getenv("POSTGRES_URL");
        if (env != null && !env.trim().isEmpty()) {
            return env;
        }
        return getProperty("postgres.url", "jdbc:postgresql://localhost:5433/postgres");
    }

    public static String getPostgresUser() {
        String env = System.getenv("POSTGRES_USER");
        if (env != null && !env.trim().isEmpty()) {
            return env;
        }
        return getProperty("postgres.user", "admin");
    }

    public static String getPostgresPassword() {
        String env = System.getenv("POSTGRES_PASSWORD");
        if (env != null && !env.trim().isEmpty()) {
            return env;
        }
        return getProperty("postgres.password", "admin123");
    }

    // Вспомогательный метод
    private static String getProperty(String key, String defaultValue) {
        try (InputStream is = AppConfig.class.getClassLoader().getResourceAsStream("application.properties")) {
            if (is != null) {
                Properties props = new Properties();
                props.load(is);
                String value = props.getProperty(key);
                return value != null ? value : defaultValue;
            }
        } catch (IOException ignored) {
            // Игнорируем — используем defaultValue
        }
        return defaultValue;
    }
}
