package org.bnp.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class AppConfig {

     /**
     * Количество записей для генерации
     * @return Количество записей для генерации
     */

    public static int getRecordCount() {
        // 1. Из переменной окружения
        String env = System.getenv("RECORD_COUNT");
        if (env != null && !env.trim().isEmpty()) {
            try {
                return Integer.parseInt(env.trim());
            } catch (NumberFormatException e) {
                System.err.println("Некорректное значение RECORD_COUNT: " + env + ". Используется значение по умолчанию.");
            }
        }

        // 2. Из application.properties
        try (InputStream is = AppConfig.class.getClassLoader().getResourceAsStream("application.properties")) {
            if (is != null) {
                Properties props = new Properties();
                props.load(is);
                String prop = props.getProperty("record.count");
                if (prop != null && !prop.trim().isEmpty()) {
                    try {
                        return Integer.parseInt(prop.trim());
                    } catch (NumberFormatException e) {
                        System.err.println("Некорректное значение record.count в application.properties. Используется значение по умолчанию.");
                    }
                }
            }
        } catch (IOException ignored) { }

        // 3. Значение по умолчанию
        return 1_000;
    }
    /**
     * URL ClickHouse
     * @return URL ClickHouse
     */
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
