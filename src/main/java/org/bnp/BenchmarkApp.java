package org.bnp;

import org.bnp.config.AppConfig;
import org.bnp.db.ClickHouseManager;
import org.bnp.db.PostgresManager;
import org.bnp.generator.DataGenerator;
import org.bnp.util.CsvExporter;
import org.bnp.util.DiskSizeChecker;

import java.sql.SQLException;


public class BenchmarkApp {


    public static void main(String[] args) throws Exception {

        // Количество записей для генерации
        System.out.println("Стартуем!");
        System.out.println("Читаем количество записей для генерации теста.");
        int RECORD_COUNT = AppConfig.getRecordCount();

        System.out.println("Генерация " + RECORD_COUNT + " записей...");
        var data = DataGenerator.generate(RECORD_COUNT);

        // Явно загружаем драйвер
        try {
            Class.forName("com.clickhouse.jdbc.ClickHouseDriver");
            System.out.println("ClickHouse driver loaded successfully");
        } catch (ClassNotFoundException e) {
            System.err.println("Failed to load ClickHouse driver: " + e.getMessage());
            throw new SQLException("ClickHouse driver not found", e);
        }

        // ClickHouse
        System.out.println("\n=== ClickHouse ===");
        long chInsert = 0, chAnalytical = 0;
        try (var ch = new ClickHouseManager()) {
            long t0 = System.currentTimeMillis();
            ch.insertBatch(data);
            chInsert = System.currentTimeMillis() - t0;

            long t1 = System.currentTimeMillis();
            ch.runAllAnalyticalQueries();
            chAnalytical = System.currentTimeMillis() - t1;
        }

        // PostgreSQL
        System.out.println("\n=== PostgreSQL ===");
        long pgInsert = 0, pgAnalytical = 0;
        try (var pg = new PostgresManager()) {
            long t0 = System.currentTimeMillis();
            pg.insertBatch(data);
            pgInsert = System.currentTimeMillis() - t0;

            long t1 = System.currentTimeMillis();
            pg.runAllAnalyticalQueries();
            pgAnalytical = System.currentTimeMillis() - t1;
        }

        // Размеры на диске
        System.out.println("\n=== Размеры на диске ===");
        long chDisk = DiskSizeChecker.getClickHouseTableSizeBytes();
        long pgDisk = DiskSizeChecker.getPostgresTableSizeBytes();

        System.out.println("ClickHouse: " + DiskSizeChecker.formatBytes(chDisk));
        System.out.println("PostgreSQL: " + DiskSizeChecker.formatBytes(pgDisk));
        if (chDisk > 0) {
            System.out.printf("ClickHouse компактнее в %.2f раз\n", (double) pgDisk / chDisk);
        }

        // Экспорт
        CsvExporter.exportResults(chInsert, chAnalytical, chDisk, pgInsert, pgAnalytical, pgDisk);

        // Итог
        System.out.println("\n" + "=".repeat(60));
        System.out.printf("%-25s | %-15s | %-15s | %s%n", "Метрика", "ClickHouse", "PostgreSQL", "Выигрыш");
        System.out.println("-".repeat(60));
        System.out.printf("%-25s | %-15d | %-15d | %.2fx%n", "Вставка (мс)", chInsert, pgInsert, (double) pgInsert / chInsert);
        System.out.printf("%-25s | %-15d | %-15d | %.2fx%n", "Аналитика (мс)", chAnalytical, pgAnalytical, (double) pgAnalytical / chAnalytical);
        System.out.printf("%-25s | %-15s | %-15s | %.2fx%n",
                "Размер на диске",
                DiskSizeChecker.formatBytes(chDisk),
                DiskSizeChecker.formatBytes(pgDisk),
                (double) pgDisk / chDisk
        );
        long chTotal = chInsert + chAnalytical;
        long pgTotal = pgInsert + pgAnalytical;
        System.out.printf("%-25s | %-15d | %-15d | %.2fx%n", "ИТОГО (мс)", chTotal, pgTotal, (double) pgTotal / chTotal);
    }

}