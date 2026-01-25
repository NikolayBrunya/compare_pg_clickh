package org.bnp.generator;

import org.bnp.model.ShlTestResult;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DataGenerator {

    private static final String[] DEPARTMENTS = {
            "MOSCOW_DIR", "ST_PETERSBURG_DIR", "SIBERIAN_DIR", "URAL_DIR", "VOLGA_DIR"
    };
    private static final String[] REGIONS = {
            "Moscow", "St. Petersburg", "Novosibirsk", "Yekaterinburg", "Kazan"
    };
    private static final String[] POSITIONS = {
            "Engineer", "Dispatcher", "Technician", "Manager", "Operator"
    };
    private static final String[] RAILWAY_DIRS = {
            "Oktiabrskaya", "Severo-Kavkazskaya", "Moskovskaya", "Sverdlovskaya", "Krasnoyarskaya"
    };
    private static final String[] TEST_VERSIONS = {"SHL_v3.1", "SHL_v3.2", "SHL_v4.0"};

    public static List<ShlTestResult> generate(int count) {
        Random r = new Random(42);
        LocalDate start = LocalDate.of(2020, 1, 1);
        LocalDate end = LocalDate.now();

        List<ShlTestResult> results = new ArrayList<>(count);
        for (long i = 0; i < count; i++) {
            LocalDate date = start.plusDays(r.nextInt((int) ChronoUnit.DAYS.between(start, end)));
            String dept = DEPARTMENTS[r.nextInt(DEPARTMENTS.length)];
            String region = REGIONS[r.nextInt(REGIONS.length)];
            String pos = POSITIONS[r.nextInt(POSITIONS.length)];
            String railway = RAILWAY_DIRS[r.nextInt(RAILWAY_DIRS.length)];
            String version = TEST_VERSIONS[r.nextInt(TEST_VERSIONS.length)];

            int score = r.nextInt(101);
            int cog = clamp(score + r.nextInt(21) - 10);
            int beh = clamp(score + r.nextInt(21) - 10);
            int team = clamp(cog + r.nextInt(15) - 7);
            int stress = clamp(beh + r.nextInt(15) - 7);
            int lead = clamp(r.nextInt(101));
            int learn = clamp(r.nextInt(101));
            int duration = 20 + r.nextInt(25); // 20–45 мин
            boolean passed = score >= 60;

            results.add(new ShlTestResult(
                    i, dept, date, score, cog, beh, region, pos,
                    team, stress, lead, learn, version, duration, passed, railway
            ));
        }
        return results;
    }

    private static int clamp(int v) {
        return Math.min(100, Math.max(0, v));
    }
}
