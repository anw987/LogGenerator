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
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Random;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Random;

public class LogGenerator {

    private static final int TOTAL_RECORDS = 10000;
    private static final double SUCCESS_RATE = 0.97;

    private static final Random random = new Random();

    // Failure mappings
    private static final int[] FAILURE_HTTP = {402, 404, 409, 503, 504};
    private static final String[] FAILURE_RC = {"51", "14", "94", "91", "68"};

    public static void main(String[] args) {
        String fileName = "payment_logs.txt";

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {

            for (int i = 0; i < TOTAL_RECORDS; i++) {

                LocalDateTime dateTime = randomDateWithin7Days();
                String timestamp = dateTime.atZone(ZoneId.systemDefault())
                        .toInstant().toString();

                String stan = generateSTAN();
                String transmissionDateTime = formatTransmissionDateTime(dateTime);

                boolean isSuccess = random.nextDouble() < SUCCESS_RATE;

                int httpStatus;
                String responseCode;

                if (isSuccess) {
                    httpStatus = 200;
                    responseCode = "00";
                } else {
                    int idx = random.nextInt(FAILURE_HTTP.length);
                    httpStatus = FAILURE_HTTP[idx];
                    responseCode = FAILURE_RC[idx];
                }

                String requestJson = String.format(
                        "{\"HealthcheckRQ\":{\"counter\":\"%s\",\"transDateTime\":\"%s\"}}",
                        stan, transmissionDateTime
                );

                String responseJson = String.format(
                        "{\"HealthcheckRS\":{\"transDateTime\":\"%s\",\"counter\":\"%s\",\"responseCode\":\"%s\"}}",
                        transmissionDateTime, stan, responseCode
                );

                String logLine = String.format(
                        "%s | %s | %s | %d",
                        timestamp, requestJson, responseJson, httpStatus
                );

                writer.write(logLine);
                writer.newLine();
            }

            System.out.println("Log file generated: " + fileName);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String generateSTAN() {
        int num = 100000 + random.nextInt(900000);
        return String.valueOf(num);
    }

    private static String formatTransmissionDateTime(LocalDateTime dt) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMddHHmmss");
        return dt.format(formatter);
    }

    private static LocalDateTime randomDateWithin7Days() {
        long now = System.currentTimeMillis();
        long sevenDaysAgo = now - (30L * 24 * 60 * 60 * 1000);

        long randomMillis = sevenDaysAgo + (long) (random.nextDouble() * (now - sevenDaysAgo));
        return Instant.ofEpochMilli(randomMillis)
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
    }
}