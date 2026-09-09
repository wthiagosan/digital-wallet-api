package com.portfolio.wallet.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Digital Wallet & Transfers API")
                        .version("1.0.0")
                        .description("API RESTful corporativa para gestão de carteiras digitais, custódia de saldo " +
                                "e transferências financeiras P2P atômicas. Desenvolvida com Java 21, Spring Boot 3.3.5, " +
                                "Spring Data JPA, PostgreSQL 16, Flyway e tratamento de erros RFC 7807 Problem Details.")
                        .contact(new Contact()
                                .name("Digital Wallet Team")
                                .email("contact@wallet.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")));
    }
}
