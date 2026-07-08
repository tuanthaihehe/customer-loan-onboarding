package com.f88.loanonboarding.dmn;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import io.camunda.client.CamundaClient;
import org.camunda.bpm.dmn.engine.DmnDecision;
import org.camunda.bpm.dmn.engine.DmnDecisionRuleResult;
import org.camunda.bpm.dmn.engine.DmnDecisionTableResult;
import org.camunda.bpm.dmn.engine.DmnEngine;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.f88.loanonboarding.common.error.ErrorCode;
import com.f88.loanonboarding.exception.BusinessException;

@Service
public class CustomerAgeDmnDecisionService {

    private static final String DECISION_KEY = "customerAgeEligibility";
    private static final String DMN_PATH = "dmn/customer-kyc-rules.dmn";
    private static final String SAAS_MODE = "SAAS";

    private final DmnDecision decision;
    private final DmnEngine dmnEngine;
    private final ObjectMapper objectMapper;
    private final CamundaClient camundaClient;
    private final String dmnMode;
    private final String customerAgeDecisionId;

    public CustomerAgeDmnDecisionService(
            DmnEngine dmnEngine,
            ObjectMapper objectMapper,
            @Nullable CamundaClient camundaClient,
            @Value("${app.camunda.dmn.mode}") String dmnMode,
            @Value("${app.camunda.dmn.customer-age-decision-id}") String customerAgeDecisionId
    ) {
        this.dmnEngine = dmnEngine;
        this.objectMapper = objectMapper;
        this.camundaClient = camundaClient;
        this.dmnMode = dmnMode;
        this.customerAgeDecisionId = customerAgeDecisionId;
        this.decision = parseDecision(dmnEngine);
    }

    public CustomerAgeDecisionResult evaluate(int age) {
        if (SAAS_MODE.equalsIgnoreCase(dmnMode)) {
            return evaluateRemote(age);
        }

        return evaluateLocal(age);
    }

    private CustomerAgeDecisionResult evaluateLocal(int age) {
        VariableMap variables = Variables.createVariables()
                .putValue("age", age);

        DmnDecisionTableResult result = dmnEngine.evaluateDecisionTable(decision, variables);
        if (result.isEmpty()) {
            throw new BusinessException(
                    ErrorCode.BUSINESS_RULE_VIOLATION,
                    "Khong tim thay ket qua DMN cho rule tuoi khach hang."
            );
        }

        DmnDecisionRuleResult ruleResult = result.getSingleResult();
        return new CustomerAgeDecisionResult(
                Boolean.TRUE.equals(ruleResult.getEntry("eligible")),
                (String) ruleResult.getEntry("reasonCode"),
                (String) ruleResult.getEntry("reasonMessage")
        );
    }

    private CustomerAgeDecisionResult evaluateRemote(int age) {
        if (camundaClient == null) {
            throw new IllegalStateException("Camunda client is not configured for SAAS mode.");
        }

        var response = camundaClient.newEvaluateDecisionCommand()
                .decisionId(customerAgeDecisionId)
                .variables(Map.of("age", age))
                .execute();

        return parseRemoteDecisionOutput(response.getDecisionOutput());
    }

    private CustomerAgeDecisionResult parseRemoteDecisionOutput(String decisionOutput) {
        try {
            JsonNode root = objectMapper.readTree(decisionOutput);
            JsonNode firstResult = root.isArray() ? root.path(0) : root;

            return new CustomerAgeDecisionResult(
                    firstResult.path("eligible").asBoolean(false),
                    textOrNull(firstResult.path("reasonCode")),
                    textOrNull(firstResult.path("reasonMessage"))
            );
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Cannot parse Camunda decision output: " + decisionOutput, exception);
        }
    }

    private String textOrNull(JsonNode node) {
        return node == null || node.isMissingNode() || node.isNull() ? null : node.asText();
    }

    private DmnDecision parseDecision(DmnEngine dmnEngine) {
        ClassPathResource resource = new ClassPathResource(DMN_PATH);
        try (InputStream inputStream = resource.getInputStream()) {
            return dmnEngine.parseDecision(DECISION_KEY, inputStream);
        } catch (IOException exception) {
            throw new IllegalStateException("Cannot load DMN resource: " + DMN_PATH, exception);
        }
    }
}
