package org.bnp.db;

import org.bnp.config.AppConfig;
import org.bnp.model.ShlTestResult;

import java.sql.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ClickHouseManager implements AutoCloseable {
    private Connection conn;

    public ClickHouseManager() throws SQLException {
        String url = AppConfig.getClickhouseUrl();
        this.conn = DriverManager.getConnection(url);
        createTable();
    }

    private void createTable() throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("DROP TABLE IF EXISTS shl_results");
            stmt.execute("""
                CREATE TABLE shl_results (
                    employee_id UInt64,
                    department_id String,
                    test_date Date,
                    score UInt8,
                    cognitive_score UInt8,
                    behavioral_score UInt8,
                    region String,
                    position String,
                    teamwork_score UInt8,
                    stress_resistance_score UInt8,
                    leadership_potential UInt8,
                    learning_agility UInt8,
                    test_version String,
                    test_duration_minutes UInt16,
                    is_passed UInt8,
                    railway_direction String
                ) ENGINE = MergeTree()
                PARTITION BY toYYYYMM(test_date)
                ORDER BY (railway_direction, department_id, test_date)
                """);
        }
    }

    public void insertBatch(List<ShlTestResult> data) throws SQLException {
        String sql = """
            INSERT INTO shl_results
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            conn.setAutoCommit(false);
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            for (ShlTestResult r : data) {
                ps.setLong(1, r.getEmployeeId());
                ps.setString(2, r.getDepartmentId());
                ps.setString(3, r.getTestDate().format(fmt));
                ps.setInt(4, r.getScore());
                ps.setInt(5, r.getCognitiveScore());
                ps.setInt(6, r.getBehavioralScore());
                ps.setString(7, r.getRegion());
                ps.setString(8, r.getPosition());
                ps.setInt(9, r.getTeamworkScore());
                ps.setInt(10, r.getStressResistanceScore());
                ps.setInt(11, r.getLeadershipPotential());
                ps.setInt(12, r.getLearningAgility());
                ps.setString(13, r.getTestVersion());
                ps.setInt(14, r.getTestDurationMinutes());
                ps.setByte(15, (byte) (r.isPassed() ? 1 : 0));
                ps.setString(16, r.getRailwayDirection());
                ps.addBatch();
            }
            ps.executeBatch();
            conn.commit();
        }
    }

    public void runAnalyticalQuery1() throws SQLException {
        String sql = """
            SELECT department_id, avg(score) as avg_score, count(*) as total
            FROM shl_results
            WHERE test_date >= '2023-01-01'
            GROUP BY department_id
            ORDER BY avg_score DESC
            """;
        try (var s = conn.createStatement(); var rs = s.executeQuery(sql)) {
            while (rs.next()) {}
        }
    }

    public void runAnalyticalQuery2() throws SQLException {
        String sql = """
            SELECT railway_direction,
                   avg(leadership_potential) as avg_lead,
                   avg(stress_resistance_score) as avg_stress
            FROM shl_results
            GROUP BY railway_direction
            ORDER BY avg_lead DESC
            """;
        try (var s = conn.createStatement(); var rs = s.executeQuery(sql)) {
            while (rs.next()) {}
        }
    }

    public void runAnalyticalQuery3() throws SQLException {
        String sql = """
            SELECT region, count(*) as high_potential
            FROM shl_results
            WHERE score > 85 AND leadership_potential > 80
            GROUP BY region
            ORDER BY high_potential DESC
            LIMIT 10
            """;
        try (var s = conn.createStatement(); var rs = s.executeQuery(sql)) {
            while (rs.next()) {}
        }
    }

    public void runAnalyticalQuery4() throws SQLException {
        String sql = """
            SELECT test_version,
                   avg(test_duration_minutes) as avg_duration,
                   avg(is_passed) as pass_rate
            FROM shl_results
            GROUP BY test_version
            ORDER BY pass_rate DESC
            """;
        try (var s = conn.createStatement(); var rs = s.executeQuery(sql)) {
            while (rs.next()) {}
        }
    }

    public void runAllAnalyticalQueries() throws SQLException {
        runAnalyticalQuery1();
        runAnalyticalQuery2();
        runAnalyticalQuery3();
        runAnalyticalQuery4();
    }

    @Override
    public void close() throws SQLException {
        if (conn != null) conn.close();
    }
}
