package com.lockerline;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.web.client.RestTemplate;

import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class SeedTwiceTest {

    @Test
    void appStartsTwiceAndReadsWall(@TempDir Path tempDir) {
        String dbPath = tempDir.resolve("testdb").toAbsolutePath().toString();
        String jdbcUrl = "jdbc:h2:file:" + dbPath + ";DB_CLOSE_DELAY=-1";

        ConfigurableApplicationContext ctx1 = new SpringApplicationBuilder(LockerLineApplication.class)
                .properties("server.port=0", "spring.datasource.url=" + jdbcUrl)
                .run();
        ctx1.close();

        ConfigurableApplicationContext ctx2 = new SpringApplicationBuilder(LockerLineApplication.class)
                .properties("server.port=0", "spring.datasource.url=" + jdbcUrl)
                .run();

        try {
            Integer port = ctx2.getEnvironment().getProperty("local.server.port", Integer.class);
            assertNotNull(port);
            RestTemplate restTemplate = new RestTemplate();
            Map<?, ?> response = restTemplate.getForObject("http://localhost:" + port + "/towers/A/doors", Map.class);
            assertNotNull(response);
        } finally {
            ctx2.close();
        }
    }
}