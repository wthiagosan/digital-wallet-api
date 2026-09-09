package com.portfolio.wallet.migration;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
    "spring.flyway.enabled=true",
    "spring.jpa.hibernate.ddl-auto=none"
})
@ActiveProfiles("test")
class FlywayMigrationTest {

    @Autowired
    private Flyway flyway;

    @Test
    @DisplayName("Should successfully execute all Flyway migrations (V1 to V4)")
    void shouldExecuteAllMigrations() {
        var info = flyway.info();
        assertThat(info.applied()).isNotEmpty();
        assertThat(info.applied()).hasSize(4);
        assertThat(info.current().getVersion().getVersion()).isEqualTo("4");
    }
}
