package org.bnp.db;


import org.bnp.config.AppConfig;
import org.bnp.model.ShlTestResult;

import java.sql.*;
import java.util.List;

public class PostgresManager implements AutoCloseable {
    private Connection conn;

    public PostgresManager() throws SQLException {

        this.conn = DriverManager.getConnection(AppConfig.getPostgresUrl(),
                AppConfig.getPostgresUser(),
                AppConfig.getPostgresPassword());
        createTable();
    }

    private void createTable() throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("DROP TABLE IF EXISTS shl_results");
            stmt.execute("""
                CREATE TABLE shl_results (
                    employee_id BIGINT,
                    department_id VARCHAR(50),
                    test_date DATE,
                    score SMALLINT,
                    cognitive_score SMALLINT,
                    behavioral_score SMALLINT,
                    region VARCHAR(50),
                    position VARCHAR(50),
                    teamwork_score SMALLINT,
                    stress_resistance_score SMALLINT,
                    leadership_potential SMALLINT,
                    learning_agility SMALLINT,
                    test_version VARCHAR(20),
                    test_duration_minutes SMALLINT,
                    is_passed BOOLEAN,
                    railway_direction VARCHAR(50)
                )
                """);
            stmt.execute("CREATE INDEX idx_railway ON shl_results(railway_direction)");
            stmt.execute("CREATE INDEX idx_region_score ON shl_results(region, score, leadership_potential)");
            stmt.execute("CREATE INDEX idx_test_version ON shl_results(test_version)");
        }
    }

    public void insertBatch(List<ShlTestResult> data) throws SQLException {
        String sql = """
            INSERT INTO shl_results
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            conn.setAutoCommit(false);
            for (ShlTestResult r : data) {
                ps.setLong(1, r.getEmployeeId());
                ps.setString(2, r.getDepartmentId());
                ps.setDate(3, Date.valueOf(r.getTestDate()));
                ps.setShort(4, (short) r.getScore());
                ps.setShort(5, (short) r.getCognitiveScore());
                ps.setShort(6, (short) r.getBehavioralScore());
                ps.setString(7, r.getRegion());
                ps.setString(8, r.getPosition());
                ps.setShort(9, (short) r.getTeamworkScore());
                ps.setShort(10, (short) r.getStressResistanceScore());
                ps.setShort(11, (short) r.getLeadershipPotential());
                ps.setShort(12, (short) r.getLearningAgility());
                ps.setString(13, r.getTestVersion());
                ps.setShort(14, (short) r.getTestDurationMinutes());
                ps.setBoolean(15, r.isPassed());
                ps.setString(16, r.getRailwayDirection());
                ps.addBatch();
            }
            ps.executeBatch();
            conn.commit();
        }
    }

    public void runAnalyticalQuery1() throws SQLException {
        String sql = """
            SELECT department_id, AVG(score::NUMERIC) as avg_score, COUNT(*) as total
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
                   AVG(leadership_potential::NUMERIC) as avg_lead,
                   AVG(stress_resistance_score::NUMERIC) as avg_stress
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
            SELECT region, COUNT(*) as high_potential
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
                   AVG(test_duration_minutes::NUMERIC) as avg_duration,
                   AVG(CASE WHEN is_passed THEN 1 ELSE 0 END::NUMERIC) as pass_rate
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
