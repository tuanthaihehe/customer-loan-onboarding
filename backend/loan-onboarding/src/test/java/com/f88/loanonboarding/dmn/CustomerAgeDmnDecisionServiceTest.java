package com.f88.loanonboarding.dmn;

import static org.assertj.core.api.Assertions.assertThat;

import org.camunda.bpm.dmn.engine.DmnEngineConfiguration;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

class CustomerAgeDmnDecisionServiceTest {

    private final CustomerAgeDmnDecisionService service = new CustomerAgeDmnDecisionService(
            DmnEngineConfiguration.createDefaultDmnEngineConfiguration().buildEngine(),
            new ObjectMapper(),
            null,
            "LOCAL",
            "customerAgeEligibility"
    );

    @Test
    void evaluateShouldRejectCustomerUnder18() {
        CustomerAgeDecisionResult result = service.evaluate(17);

        assertThat(result.eligible()).isFalse();
        assertThat(result.reasonCode()).isEqualTo("CUSTOMER_UNDER_AGE");
        assertThat(result.reasonMessage()).isNotBlank();
    }

    @Test
    void evaluateShouldAllowCustomerAtLeast18() {
        CustomerAgeDecisionResult result = service.evaluate(18);

        assertThat(result.eligible()).isTrue();
        assertThat(result.reasonCode()).isNull();
        assertThat(result.reasonMessage()).isNull();
    }
}
