package com.f88.loanonboarding.config;

import io.camunda.client.CamundaClient;

import org.camunda.bpm.dmn.engine.DmnEngine;
import org.camunda.bpm.dmn.engine.DmnEngineConfiguration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CamundaDmnConfig {

    @Bean
    public DmnEngine dmnEngine() {
        return DmnEngineConfiguration
                .createDefaultDmnEngineConfiguration()
                .buildEngine();
    }

    @Bean
    @ConditionalOnProperty(name = "app.camunda.dmn.mode", havingValue = "SAAS")
    public CamundaClient camundaClient(
            @Value("${camunda.client.auth.client-id}") String clientId,
            @Value("${camunda.client.auth.client-secret}") String clientSecret,
            @Value("${camunda.client.cloud.cluster-id}") String clusterId,
            @Value("${camunda.client.cloud.region}") String region
    ) {
        return CamundaClient.newCloudClientBuilder()
                .withClusterId(clusterId)
                .withClientId(clientId)
                .withClientSecret(clientSecret)
                .withRegion(region)
                .build();
    }
}
