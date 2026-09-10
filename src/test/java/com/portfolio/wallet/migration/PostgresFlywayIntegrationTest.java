package com.portfolio.wallet.migration;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(properties = {
    "spring.flyway.enabled=true",
    "spring.jpa.hibernate.ddl-auto=none"
})
@ActiveProfiles("test")
class PostgresFlywayIntegrationTest {

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
