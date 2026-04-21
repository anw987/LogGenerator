/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package loggenerator;

/**
 *
 * @author alvin.wijaya
 */
import java.io.*;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

public class SLOCalculator {

    private static final String INPUT = "payment_logs.txt";
    private static final String OUTPUT = "sloreport.json";

    private static final Duration WINDOW_SIZE = Duration.ofHours(12);
    private static final double SLO_TARGET = 95.0;

    public static void main(String[] args) throws Exception {

        Map<Long, Stats> buckets = new TreeMap<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(INPUT))) {

            String line;
            while ((line = reader.readLine()) != null) {

                String[] parts = line.split("\\|");
                if (parts.length < 4) continue;

                String timestampStr = parts[0].trim();
                int httpStatus = Integer.parseInt(parts[3].trim());

                Instant instant = Instant.parse(timestampStr);
                long bucket = getBucketStart(instant);

                Stats s = buckets.computeIfAbsent(bucket, k -> new Stats());

                s.total++;

                if (httpStatus == 200) s.success++;
                else s.failure++;
            }
        }

        int totalWindows = buckets.size();
        int sloMet = 0;
        double totalRate = 0;

        StringBuilder json = new StringBuilder();
        json.append("{");

        // WINDOWS
        json.append("\"windows\":[");
        boolean first = true;

        for (Map.Entry<Long, Stats> entry : buckets.entrySet()) {

            if (!first) json.append(",");
            first = false;

            Stats s = entry.getValue();
            double rate = s.total == 0 ? 0 : (s.success * 100.0 / s.total);

            totalRate += rate;
            if (rate >= SLO_TARGET) sloMet++;

            json.append("{");
            json.append("\"start\":\"").append(Instant.ofEpochMilli(entry.getKey())).append("\",");
            json.append("\"total\":").append(s.total).append(",");
            json.append("\"success\":").append(s.success).append(",");
            json.append("\"failure\":").append(s.failure).append(",");
            json.append("\"successRate\":").append(String.format("%.2f", rate));
            json.append("}");
        }

        json.append("],");

        // SUMMARY CALC
        double avg = totalWindows == 0 ? 0 : totalRate / totalWindows;
        double compliance = totalWindows == 0 ? 0 :
                (sloMet * 100.0 / totalWindows);

        double errorBudget = 100 - SLO_TARGET;
        double actualError = 100 - avg;
        double burnRate = errorBudget == 0 ? 0 : actualError / errorBudget;

        double budgetUsed = (actualError / errorBudget) * 100;
        double budgetRemaining = 100 - budgetUsed;

        // TIME-BASED
        long windowMinutes = WINDOW_SIZE.toMinutes();
        long totalMinutes = totalWindows * windowMinutes;

        double errorBudgetMinutes = totalMinutes * errorBudget / 100.0;
        double actualErrorMinutes = totalMinutes * actualError / 100.0;
        double remainingMinutes = errorBudgetMinutes - actualErrorMinutes;

        double minutesToExhaust = burnRate <= 1 ? Double.POSITIVE_INFINITY :
                (remainingMinutes > 0 ? remainingMinutes / (burnRate - 1) : 0);

        // SUMMARY JSON
        json.append("\"summary\":{");
        json.append("\"sloTarget\":").append(SLO_TARGET).append(",");
        json.append("\"windowSize\":\"").append(WINDOW_SIZE).append("\",");
        json.append("\"totalWindows\":").append(totalWindows).append(",");
        json.append("\"windowsMeetingSLO\":").append(sloMet).append(",");
        json.append("\"compliance\":").append(String.format("%.2f", compliance)).append(",");
        json.append("\"averageSuccessRate\":").append(String.format("%.2f", avg)).append(",");
        json.append("\"burnRate\":").append(String.format("%.2f", burnRate)).append(",");
        json.append("\"errorBudget\":").append(String.format("%.2f", errorBudget)).append(",");
        json.append("\"errorBudgetUsed\":").append(String.format("%.2f", budgetUsed)).append(",");
        json.append("\"errorBudgetRemaining\":").append(String.format("%.2f", budgetRemaining));
        json.append("},");

        // TIME JSON
        json.append("\"time\":{");
        json.append("\"totalMinutes\":").append(totalMinutes).append(",");
        json.append("\"errorBudgetMinutes\":").append(String.format("%.2f", errorBudgetMinutes)).append(",");
        json.append("\"errorConsumedMinutes\":").append(String.format("%.2f", actualErrorMinutes)).append(",");
        json.append("\"remainingMinutes\":").append(String.format("%.2f", remainingMinutes)).append(",");
        json.append("\"minutesToExhaust\":").append(
                Double.isInfinite(minutesToExhaust) ? "\"INF\"" : String.format("%.2f", minutesToExhaust)
        );
        json.append("}");

        json.append("}");

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(OUTPUT))) {
            writer.write(json.toString());
        }

        System.out.println("Generated: " + OUTPUT);
    }

    private static long getBucketStart(Instant instant) {
        long millis = instant.toEpochMilli();
        long windowMillis = WINDOW_SIZE.toMillis();
        return (millis / windowMillis) * windowMillis;
    }

    static class Stats {
        int total = 0;
        int success = 0;
        int failure = 0;
    }
}