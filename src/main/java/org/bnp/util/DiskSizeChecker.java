package org.bnp.util;


import org.bnp.config.AppConfig;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class DiskSizeChecker {

    public static long getClickHouseTableSizeBytes() throws Exception {
        String url = AppConfig.getClickhouseUrl();
        try (Connection conn = DriverManager.getConnection(url);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT sum(bytes_on_disk) FROM system.parts WHERE table = 'shl_results' AND active = 1")) {
            if (rs.next()) {
                return rs.getLong(1);
            }
            return -1;
        }
    }

    public static long getPostgresTableSizeBytes() throws Exception {
        try (Connection conn = DriverManager.getConnection(
                AppConfig.getPostgresUrl(),
                AppConfig.getPostgresUser(),
                AppConfig.getPostgresPassword());
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT pg_total_relation_size('shl_results')")) {
            if (rs.next()) {
                return rs.getLong(1);
            }
            return -1;
        }
    }

    public static String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.1f MB", bytes / (1024.0 * 1024));
        return String.format("%.1f GB", bytes / (1024.0 * 1024 * 1024));
    }
}
