package com.lockerline.door;

import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

@Component
public class WallSeeder implements SmartInitializingSingleton {

    private final JdbcClient jdbcClient;

    public WallSeeder(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    @Override
    public void afterSingletonsInstantiated() {
        jdbcClient.sql("""
            CREATE TABLE IF NOT EXISTS doors (
                no VARCHAR(50) PRIMARY KEY,
                size_class VARCHAR(10) NOT NULL,
                current_parcel_id VARCHAR(255)
            )
        """).update();

        jdbcClient.sql("""
            CREATE TABLE IF NOT EXISTS residents (
                id INT AUTO_INCREMENT PRIMARY KEY,
                name VARCHAR(255)
            )
        """).update();

        Integer doorCount = jdbcClient.sql("SELECT count(*) FROM doors")
                .query(Integer.class)
                .single();

        if (doorCount == 0) {
            for (int i = 1; i <= 20; i++) {
                jdbcClient.sql("INSERT INTO doors (no, size_class, current_parcel_id) VALUES (?, ?, NULL)")
                        .params(String.format("D%02d", i), "S").update();
            }
            for (int i = 21; i <= 40; i++) {
                jdbcClient.sql("INSERT INTO doors (no, size_class, current_parcel_id) VALUES (?, ?, NULL)")
                        .params(String.format("D%02d", i), "M").update();
            }
            for (int i = 41; i <= 48; i++) {
                jdbcClient.sql("INSERT INTO doors (no, size_class, current_parcel_id) VALUES (?, ?, NULL)")
                        .params(String.format("D%02d", i), "L").update();
            }
        }

        Integer resCount = jdbcClient.sql("SELECT count(*) FROM residents")
                .query(Integer.class)
                .single();

        if (resCount == 0) {
            InputStream is = findResidentsCsvStream();
            if (is != null) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                    String line;
                    boolean isFirstLine = true;
                    while ((line = reader.readLine()) != null) {
                        // Strip BOM, quotes, and edge whitespace
                        line = line.replace("\uFEFF", "").replace("\"", "").trim();
                        if (line.isEmpty()) continue;

                        String lower = line.toLowerCase();
                        if (isFirstLine) {
                            isFirstLine = false;
                            if (lower.startsWith("id") || lower.contains("name")) {
                                continue;
                            }
                        }

                        if (lower.startsWith("id") || lower.contains("name")) {
                            continue;
                        }

                        String[] parts = line.split(",");
                        String name = parts.length > 1 ? parts[1].trim() : parts[0].trim();
                        if (!name.isEmpty() && !name.equalsIgnoreCase("name")) {
                            jdbcClient.sql("INSERT INTO residents (name) VALUES (?)")
                                    .param(name)
                                    .update();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private InputStream findResidentsCsvStream() {
        try {
            File f1 = new File("seed/residents.csv");
            if (f1.exists()) return new FileInputStream(f1);

            File f2 = new File("../seed/residents.csv");
            if (f2.exists()) return new FileInputStream(f2);

            ClassPathResource r1 = new ClassPathResource("seed/residents.csv");
            if (r1.exists()) return r1.getInputStream();

            ClassPathResource r2 = new ClassPathResource("residents.csv");
            if (r2.exists()) return r2.getInputStream();
        } catch (Exception ignored) {}
        return null;
    }
}