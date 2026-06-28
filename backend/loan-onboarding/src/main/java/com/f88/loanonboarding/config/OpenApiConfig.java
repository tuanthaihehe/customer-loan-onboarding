package com.f88.loanonboarding.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI loanOnboardingOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Customer & Loan Onboarding API")
                        .version("v1.0.0")
                        .description("API documentation for Customer & Loan Onboarding backend service.")
                        .contact(new Contact()
                                .name("F88 IT Fresher Team")
                                .email("dev-team@example.com")));
    }
}
