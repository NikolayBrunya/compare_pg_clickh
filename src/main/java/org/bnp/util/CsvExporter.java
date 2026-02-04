package org.bnp.util;


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class CsvExporter {

    public static void exportResults(
            long chInsertMs,
            long chAnalyticalMs,
            long chDiskBytes,
            long pgInsertMs,
            long pgAnalyticalMs,
            long pgDiskBytes
    ) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
        File dir = new File("results");
        dir.mkdirs();
        String filename = "results/benchmark_results_" + timestamp + ".csv";

        try (FileWriter writer = new FileWriter(filename)) {
            writer.write("metric,clickhouse,postgresql,ratio\n");

            double insertRatio = chInsertMs > 0 ? (double) pgInsertMs / chInsertMs : -1;
            writer.write(String.format("insert_time_ms,%d,%d,%.2f\n", chInsertMs, pgInsertMs, insertRatio));

            double analyticRatio = chAnalyticalMs > 0 ? (double) pgAnalyticalMs / chAnalyticalMs : -1;
            writer.write(String.format("analytical_time_ms,%d,%d,%.2f\n", chAnalyticalMs, pgAnalyticalMs, analyticRatio));

            double sizeRatio = chDiskBytes > 0 ? (double) pgDiskBytes / chDiskBytes : -1;
            writer.write(String.format("disk_size_bytes,%d,%d,%.2f\n", chDiskBytes, pgDiskBytes, sizeRatio));

            long chTotal = chInsertMs + chAnalyticalMs;
            long pgTotal = pgInsertMs + pgAnalyticalMs;
            double totalRatio = chTotal > 0 ? (double) pgTotal / chTotal : -1;
            writer.write(String.format("total_time_ms,%d,%d,%.2f\n", chTotal, pgTotal, totalRatio));

            System.out.println("\nРезультаты экспортированы в: " + filename);
        } catch (IOException e) {
            System.err.println("Ошибка при записи CSV: " + e.getMessage());
        }
    }
}
