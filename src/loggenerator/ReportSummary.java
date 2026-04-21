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
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.TreeMap;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import java.io.*;
import java.time.*;
import java.util.*;

public class ReportSummary {

    private static final String INPUT_FILE = "payment_logs.txt";
    private static final String OUTPUT_FILE = "summary.json";

    private static final Duration WINDOW_SIZE = Duration.ofHours(8);

    public static void main(String[] args) throws Exception {

        Map<Long, Stats> buckets = new TreeMap<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(INPUT_FILE))) {

            String line;
            while ((line = reader.readLine()) != null) {

                String[] parts = line.split("\\|");
                if (parts.length < 4) continue;

                String timestampStr = parts[0].trim();
                int httpStatus = Integer.parseInt(parts[3].trim());

                Instant instant = Instant.parse(timestampStr);
                long bucketKey = getBucketStart(instant);

                Stats stats = buckets.computeIfAbsent(bucketKey, k -> new Stats());

                stats.total++;

                if (httpStatus == 200) {
                    stats.success++;
                } else {
                    stats.failure++;
                    stats.errors.put(httpStatus,
                            stats.errors.getOrDefault(httpStatus, 0) + 1);
                }
            }
        }

        // Build JSON manually (no external libs)
        StringBuilder json = new StringBuilder();
        json.append("{\"windows\":[");

        boolean firstWindow = true;

        for (Map.Entry<Long, Stats> entry : buckets.entrySet()) {

            if (!firstWindow) json.append(",");
            firstWindow = false;

            long ts = entry.getKey();
            Stats s = entry.getValue();

            double rate = s.total == 0 ? 0 :
                    (s.success * 100.0 / s.total);

            json.append("{");
            json.append("\"start\":\"").append(Instant.ofEpochMilli(ts)).append("\",");
            json.append("\"total\":").append(s.total).append(",");
            json.append("\"success\":").append(s.success).append(",");
            json.append("\"failure\":").append(s.failure).append(",");
            json.append("\"successRate\":").append(String.format("%.2f", rate)).append(",");
            json.append("\"errors\":{");

            boolean firstErr = true;
            for (Map.Entry<Integer, Integer> err : s.errors.entrySet()) {
                if (!firstErr) json.append(",");
                firstErr = false;
                json.append("\"").append(err.getKey()).append("\":").append(err.getValue());
            }

            json.append("}}");
        }

        json.append("]}");

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(OUTPUT_FILE))) {
            writer.write(json.toString());
        }

        System.out.println("Generated: " + OUTPUT_FILE);
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
        Map<Integer, Integer> errors = new HashMap<>();
    }
}