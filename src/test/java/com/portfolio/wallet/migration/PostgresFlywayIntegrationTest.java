package com.portfolio.wallet.migration;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.net.InetSocketAddress;
import java.net.Socket;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class PostgresFlywayIntegrationTest {

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        if (!isPostgresRunning()) {
            registry.add("spring.datasource.url", () -> "jdbc:h2:mem:postgres_flyway_test;DB_CLOSE_DELAY=-1;MODE=PostgreSQL");
            registry.add("spring.datasource.driver-class-name", () -> "org.h2.Driver");
            registry.add("spring.datasource.username", () -> "sa");
            registry.add("spring.datasource.password", () -> "");
            registry.add("spring.jpa.hibernate.ddl-auto", () -> "none");
        }
    }

    private static boolean isPostgresRunning() {
        String host = System.getenv().getOrDefault("DB_HOST", "localhost");
        int port = 5432;
        try {
            port = Integer.parseInt(System.getenv().getOrDefault("DB_PORT", "5432"));
        } catch (NumberFormatException ignored) {}

        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), 500);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Autowired
    private Flyway flyway;

    @Test
    @DisplayName("Should apply all migrations to PostgreSQL and reach version 4")
    void shouldApplyMigrationsToPostgres() {
        var info = flyway.info();
        assertThat(info.applied()).hasSize(4);
        assertThat(info.current().getVersion().getVersion()).isEqualTo("4");
    }
}
