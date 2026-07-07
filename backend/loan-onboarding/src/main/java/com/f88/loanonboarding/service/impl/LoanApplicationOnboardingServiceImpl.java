package com.f88.loanonboarding.service.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.f88.loanonboarding.common.error.ErrorCode;
import com.f88.loanonboarding.dto.request.loan.CancelLoanApplicationOnboardingRequest;
import com.f88.loanonboarding.dto.request.loan.CompleteLoanApplicationStepRequest;
import com.f88.loanonboarding.dto.request.loan.CreateLoanApplicationOnboardingRequest;
import com.f88.loanonboarding.dto.response.loan.LoanApplicationOnboardingCustomerResponse;
import com.f88.loanonboarding.dto.response.loan.LoanApplicationOnboardingDetailResponse;
import com.f88.loanonboarding.dto.response.loan.LoanApplicationStepActionResponse;
import com.f88.loanonboarding.dto.response.loan.LoanApplicationStepResponse;
import com.f88.loanonboarding.dto.response.loan.LoanApplicationSubmitResponse;
import com.f88.loanonboarding.dto.response.loan.LoanApplicationOnboardingSummaryResponse;
import com.f88.loanonboarding.entity.Customer;
import com.f88.loanonboarding.entity.LoanApplication;
import com.f88.loanonboarding.entity.LoanApplicationState;
import com.f88.loanonboarding.entity.LoanApplicationStateHistory;
import com.f88.loanonboarding.entity.LoanApplicationStep;
import com.f88.loanonboarding.entity.LoanApplicationStepData;
import com.f88.loanonboarding.entity.LoanApplicationStepHistory;
import com.f88.loanonboarding.entity.LoanPurpose;
import com.f88.loanonboarding.entity.LoanTerm;
import com.f88.loanonboarding.enums.LoanApplicationOnboardingStatus;
import com.f88.loanonboarding.enums.LoanApplicationStepDataStatus;
import com.f88.loanonboarding.enums.LoanApplicationStepHistoryAction;
import com.f88.loanonboarding.enums.LoanApplicationStepStatus;
import com.f88.loanonboarding.exception.BusinessException;
import com.f88.loanonboarding.repository.CustomerRepository;
import com.f88.loanonboarding.repository.LoanApplicationRepository;
import com.f88.loanonboarding.repository.LoanApplicationStateHistoryRepository;
import com.f88.loanonboarding.repository.LoanApplicationStateRepository;
import com.f88.loanonboarding.repository.LoanApplicationStateTransitionRepository;
import com.f88.loanonboarding.repository.LoanApplicationStepDataRepository;
import com.f88.loanonboarding.repository.LoanApplicationStepHistoryRepository;
import com.f88.loanonboarding.repository.LoanApplicationStepRepository;
import com.f88.loanonboarding.repository.LoanProductRepository;
import com.f88.loanonboarding.repository.LoanPurposeRepository;
import com.f88.loanonboarding.repository.LoanTermRepository;
import com.f88.loanonboarding.service.LoanApplicationOnboardingService;

@Service
public class LoanApplicationOnboardingServiceImpl implements LoanApplicationOnboardingService {

    private static final String STEP_CUSTOMER_IDENTIFY = "CUSTOMER_IDENTIFY";
    private static final String STATE_CREATED = "APP_CREATED";
    private static final String STATE_IN_PROGRESS = "APP_IN_PROGRESS";
    private static final String STATE_COMPLETED = "APP_COMPLETED";
    private static final String STATE_SUBMITTED = "APP_SUBMITTED";
    private static final String STATE_CANCELLED = "APP_CANCELLED";
    private static final String EMPTY_JSON = "{}";

    private final CustomerRepository customerRepository;
    private final LoanApplicationRepository loanApplicationRepository;
    private final LoanApplicationStepRepository stepRepository;
    private final LoanApplicationStepDataRepository stepDataRepository;
    private final LoanApplicationStepHistoryRepository stepHistoryRepository;
    private final LoanApplicationStateRepository stateRepository;
    private final LoanApplicationStateTransitionRepository transitionRepository;
    private final LoanApplicationStateHistoryRepository stateHistoryRepository;
    private final LoanPurposeRepository loanPurposeRepository;
    private final LoanTermRepository loanTermRepository;
    private final LoanProductRepository loanProductRepository;
    private final ObjectMapper objectMapper;

    public LoanApplicationOnboardingServiceImpl(
            CustomerRepository customerRepository,
            LoanApplicationRepository loanApplicationRepository,
            LoanApplicationStepRepository stepRepository,
            LoanApplicationStepDataRepository stepDataRepository,
            LoanApplicationStepHistoryRepository stepHistoryRepository,
            LoanApplicationStateRepository stateRepository,
            LoanApplicationStateTransitionRepository transitionRepository,
            LoanApplicationStateHistoryRepository stateHistoryRepository,
            LoanPurposeRepository loanPurposeRepository,
            LoanTermRepository loanTermRepository,
            LoanProductRepository loanProductRepository,
            ObjectMapper objectMapper
    ) {
        this.customerRepository = customerRepository;
        this.loanApplicationRepository = loanApplicationRepository;
        this.stepRepository = stepRepository;
        this.stepDataRepository = stepDataRepository;
        this.stepHistoryRepository = stepHistoryRepository;
        this.stateRepository = stateRepository;
        this.transitionRepository = transitionRepository;
        this.stateHistoryRepository = stateHistoryRepository;
        this.loanPurposeRepository = loanPurposeRepository;
        this.loanTermRepository = loanTermRepository;
        this.loanProductRepository = loanProductRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public LoanApplicationOnboardingDetailResponse createApplication(CreateLoanApplicationOnboardingRequest request) {
        Customer customer = findCustomer(request);
        List<LoanApplicationStep> steps = activeSteps();
        LoanApplicationStep firstStep = steps.get(0);
        LoanApplicationStep nextStep = nextStep(steps, firstStep);
        JsonNode identifyPayload = request.customerIdentifyPayload();
        boolean completeIdentify = hasPayload(identifyPayload);
        LocalDateTime now = LocalDateTime.now();

        LoanApplication application = new LoanApplication();
        application.setLoanApplicationCode(nextApplicationCode());
        application.setCustomer(customer);
        application.setCurrentState(findState(completeIdentify ? STATE_IN_PROGRESS : STATE_CREATED));
        application.setCurrentStep(completeIdentify && nextStep != null ? nextStep : firstStep);
        application.setExpiredAt(now.plusDays(30));
        application.setCreatedAt(now);
        application.setUpdatedAt(now);

        LoanApplication saved = loanApplicationRepository.save(application);
        saveStateHistory(saved, null, saved.getCurrentState(), "CREATE", "Create loan application");

        for (LoanApplicationStep step : steps) {
            LoanApplicationStepData stepData = new LoanApplicationStepData();
            stepData.setLoanApplication(saved);
            stepData.setStep(step);
            stepData.setStatus(initialStepStatus(step, completeIdentify));
            stepData.setPayload(STEP_CUSTOMER_IDENTIFY.equals(step.getCode()) ? toJson(identifyPayload) : EMPTY_JSON);
            stepData.setCompletedAt(STEP_CUSTOMER_IDENTIFY.equals(step.getCode()) && completeIdentify ? now : null);
            stepData.setCreatedAt(now);
            stepData.setUpdatedAt(now);
            stepData.setRequiresReview(false);
            stepDataRepository.save(stepData);
        }

        saveStepHistory(saved, null, LoanApplicationStepHistoryAction.CREATE_APPLICATION, null, saved.getCurrentState().getCode(), "Create application", EMPTY_JSON);
        if (completeIdentify) {
            saveStepHistory(saved, firstStep, LoanApplicationStepHistoryAction.COMPLETE_STEP, "NOT_STARTED", "COMPLETED", "Complete customer identify step on application creation", toJson(identifyPayload));
        }

        return toDetailResponse(saved.getLoanApplicationCode());
    }

    @Override
    @Transactional(readOnly = true)
    public List<LoanApplicationOnboardingSummaryResponse> findApplications(LoanApplicationOnboardingStatus status) {
        List<LoanApplication> applications = loanApplicationRepository.findAllByOrderByUpdatedAtDesc();
        return applications.stream()
                .filter(application -> status == null || toCompatibilityStatus(application).equals(status))
                .map(this::toSummaryResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public LoanApplicationOnboardingDetailResponse getApplication(String applicationCode) {
        return toDetailResponse(applicationCode);
    }

    @Override
    @Transactional
    public LoanApplicationStepActionResponse completeStep(
            String applicationCode,
            String stepCode,
            CompleteLoanApplicationStepRequest request
    ) {
        LoanApplication application = editableApplication(applicationCode);
        LoanApplicationStepData stepData = findStepData(applicationCode, stepCode);
        LoanApplicationStepStatus oldStatus = stepData.getStatus();
        LocalDateTime now = LocalDateTime.now();

        stepData.setStatus(LoanApplicationStepStatus.COMPLETED);
        stepData.setPayload(toJson(request.payload()));
        stepData.setCompletedAt(now);
        stepData.setUpdatedAt(now);
        stepData.setRequiresReview(false);
        stepData.setReviewedAt(now);
        stepDataRepository.saveAndFlush(stepData);

        applyPayloadToLoanApplication(application, applicationCode);

        List<LoanApplicationStep> steps = activeSteps();
        LoanApplicationStep nextStep = nextStep(steps, stepData.getStep());
        boolean allCompleted = allStepsCompleted(applicationCode);
        LoanApplicationState previousState = application.getCurrentState();
        if (nextStep != null) {
            application.setCurrentStep(nextStep);
        }
        if (STATE_CREATED.equals(previousState.getCode()) && STEP_CUSTOMER_IDENTIFY.equals(stepCode)) {
            transitionState(application, STATE_IN_PROGRESS, "IDENTIFY_COMPLETE", "Customer identify completed");
        }
        if (allCompleted) {
            transitionState(application, STATE_COMPLETED, "COMPLETE_APPLICATION", "All application steps completed");
        }
        application.setUpdatedAt(now);
        loanApplicationRepository.save(application);

        markDownstreamRequiresReview(application, stepData.getStep(), now);
        saveStepHistory(application, stepData.getStep(), LoanApplicationStepHistoryAction.COMPLETE_STEP, oldStatus.name(), "COMPLETED", "Complete application step", toJson(request.payload()));
        if (allCompleted) {
            saveStepHistory(application, null, LoanApplicationStepHistoryAction.COMPLETE_APPLICATION, previousState.getCode(), STATE_COMPLETED, "Application completed", EMPTY_JSON);
        }

        return actionResponse(application, stepCode, LoanApplicationStepStatus.COMPLETED, allCompleted, allCompleted ? "Application completed" : "Application step completed");
    }

    @Override
    @Transactional
    public LoanApplicationSubmitResponse submit(String applicationCode) {
        LoanApplication application = findApplication(applicationCode);
        if (STATE_SUBMITTED.equals(application.getCurrentState().getCode())) {
            return submitResponse(application, "Application was already submitted");
        }
        ensureReadyToSubmit(applicationCode);
        transitionState(application, STATE_SUBMITTED, "SUBMIT", "Submit application for appraisal");
        application.setUpdatedAt(LocalDateTime.now());
        loanApplicationRepository.save(application);
        saveStepHistory(application, null, LoanApplicationStepHistoryAction.SUBMIT_APPLICATION, STATE_COMPLETED, STATE_SUBMITTED, "Submit application", EMPTY_JSON);
        return submitResponse(application, "Application submitted");
    }

    @Override
    @Transactional
    public LoanApplicationSubmitResponse cancel(String applicationCode, CancelLoanApplicationOnboardingRequest request) {
        LoanApplication application = editableApplication(applicationCode);
        transitionState(application, STATE_CANCELLED, "CANCEL", request == null ? "Cancel application" : request.note());
        application.setUpdatedAt(LocalDateTime.now());
        loanApplicationRepository.save(application);
        saveStepHistory(application, null, LoanApplicationStepHistoryAction.CANCEL_APPLICATION, null, STATE_CANCELLED, request == null ? "Cancel application" : request.note(), EMPTY_JSON);
        return submitResponse(application, "Application cancelled");
    }

    private Customer findCustomer(CreateLoanApplicationOnboardingRequest request) {
        if (request.customerId() != null) {
            return customerRepository.findById(request.customerId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.CUSTOMER_NOT_FOUND));
        }
        if (request.customerCode() != null && !request.customerCode().isBlank()) {
            return customerRepository.findByCustomerCode(request.customerCode())
                    .orElseThrow(() -> new BusinessException(ErrorCode.CUSTOMER_NOT_FOUND));
        }
        throw new BusinessException(ErrorCode.VALIDATION_ERROR, "customerId or customerCode is required to create loan application.");
    }

    private LoanApplication editableApplication(String applicationCode) {
        LoanApplication application = findApplication(applicationCode);
        String stateCode = application.getCurrentState().getCode();
        if (STATE_SUBMITTED.equals(stateCode) || STATE_CANCELLED.equals(stateCode) || "APP_EXPIRED".equals(stateCode)) {
            throw new BusinessException(ErrorCode.INVALID_LOAN_APPLICATION_STATE, "Application is not editable in state " + stateCode);
        }
        return application;
    }

    private LoanApplication findApplication(String applicationCode) {
        return loanApplicationRepository.findByLoanApplicationCode(applicationCode)
                .orElseThrow(() -> new BusinessException(ErrorCode.LOAN_APPLICATION_NOT_FOUND));
    }

    private LoanApplicationStepData findStepData(String applicationCode, String stepCode) {
        return stepDataRepository.findByLoanApplication_LoanApplicationCodeAndStep_Code(applicationCode, stepCode)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Application step not found: " + stepCode));
    }

    private List<LoanApplicationStep> activeSteps() {
        List<LoanApplicationStep> steps = stepRepository.findByActiveTrueOrderByStepOrderAsc();
        if (steps.isEmpty()) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Loan application steps are not configured.");
        }
        return steps;
    }

    private LoanApplicationStep nextStep(List<LoanApplicationStep> steps, LoanApplicationStep currentStep) {
        return steps.stream()
                .filter(step -> step.getStepOrder() > currentStep.getStepOrder())
                .min(Comparator.comparingInt(LoanApplicationStep::getStepOrder))
                .orElse(null);
    }

    private LoanApplicationStepStatus initialStepStatus(LoanApplicationStep step, boolean completeIdentify) {
        if (STEP_CUSTOMER_IDENTIFY.equals(step.getCode()) && completeIdentify) {
            return LoanApplicationStepStatus.COMPLETED;
        }
        return LoanApplicationStepStatus.NOT_STARTED;
    }

    private void markDownstreamRequiresReview(LoanApplication application, LoanApplicationStep changedStep, LocalDateTime now) {
        for (LoanApplicationStepData downstreamStep : stepDataRepository.findByLoanApplicationAndStep_StepOrderGreaterThan(application, changedStep.getStepOrder())) {
            if (downstreamStep.getStatus() == LoanApplicationStepStatus.NOT_STARTED
                    && (downstreamStep.getPayload() == null || EMPTY_JSON.equals(downstreamStep.getPayload()))) {
                continue;
            }
            downstreamStep.setRequiresReview(true);
            downstreamStep.setInvalidatedAt(now);
            downstreamStep.setInvalidatedReason("Upstream step changed: " + changedStep.getCode());
            downstreamStep.setInvalidatedByStep(changedStep);
            downstreamStep.setUpdatedAt(now);
            stepDataRepository.save(downstreamStep);
            saveStepHistory(application, downstreamStep.getStep(), LoanApplicationStepHistoryAction.INVALIDATE_STEP, downstreamStep.getStatus().name(), downstreamStep.getStatus().name(), "Mark downstream step for review", "{\"changedStepCode\":\"" + changedStep.getCode() + "\"}");
        }
    }

    private boolean allStepsCompleted(String applicationCode) {
        return stepDataRepository.findByLoanApplication_LoanApplicationCodeOrderByStep_StepOrderAsc(applicationCode)
                .stream()
                .allMatch(step -> step.getStatus() == LoanApplicationStepStatus.COMPLETED);
    }

    private void ensureReadyToSubmit(String applicationCode) {
        LoanApplication application = findApplication(applicationCode);
        if (!STATE_COMPLETED.equals(application.getCurrentState().getCode())) {
            throw new BusinessException(ErrorCode.INVALID_LOAN_APPLICATION_STATE, "Application must be APP_COMPLETED before submit.");
        }
        List<LoanApplicationStepData> steps = stepDataRepository.findByLoanApplication_LoanApplicationCodeOrderByStep_StepOrderAsc(applicationCode);
        List<String> incompleteSteps = steps.stream()
                .filter(step -> step.getStatus() != LoanApplicationStepStatus.COMPLETED)
                .map(step -> step.getStep().getCode())
                .toList();
        if (!incompleteSteps.isEmpty()) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "Application has incomplete steps: " + String.join(", ", incompleteSteps));
        }
        List<String> reviewSteps = steps.stream()
                .filter(LoanApplicationStepData::isRequiresReview)
                .map(step -> step.getStep().getCode())
                .toList();
        if (!reviewSteps.isEmpty()) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "Application has steps requiring review: " + String.join(", ", reviewSteps));
        }
    }

    private void transitionState(LoanApplication application, String toStateCode, String actionCode, String note) {
        LoanApplicationState fromState = application.getCurrentState();
        LoanApplicationState toState = findState(toStateCode);
        if (!fromState.getCode().equals(toStateCode)
                && !transitionRepository.existsByFromStateAndToStateAndActionCode(fromState, toState, actionCode)) {
            throw new BusinessException(ErrorCode.INVALID_LOAN_APPLICATION_STATE, "Invalid transition " + fromState.getCode() + " -> " + toStateCode + " by " + actionCode);
        }
        application.setCurrentState(toState);
        saveStateHistory(application, fromState, toState, actionCode, note);
    }

    private LoanApplicationState findState(String stateCode) {
        return stateRepository.findByCode(stateCode)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_LOAN_APPLICATION_STATE, "Loan application state is not configured: " + stateCode));
    }

    private void applyPayloadToLoanApplication(LoanApplication application, String applicationCode) {
        List<LoanApplicationStepData> steps = stepDataRepository.findByLoanApplication_LoanApplicationCodeOrderByStep_StepOrderAsc(applicationCode);
        JsonNode preliminary = payloadByStep(steps, "PRELIMINARY_INFO");
        JsonNode mergedProposal = payloadByStep(steps, "CUSTOMER_ASSET_LOAN_PROPOSAL");

        BigDecimal requestedAmount = firstNumber(
                at(mergedProposal, "selected_loan_offer", "final_requested_amount"),
                at(mergedProposal, "selectedLoanOffer", "finalRequestedAmount"),
                at(preliminary, "preliminary_loan_package", "requested_amount"),
                at(preliminary, "preliminaryLoanPackage", "requestedAmount"),
                at(preliminary, "loanRequest", "requestedAmount"),
                at(preliminary, "requestedAmount")
        );
        if (requestedAmount != null) {
            application.setRequestedAmount(requestedAmount);
        }

        String purposeCode = firstText(
                at(mergedProposal, "selected_loan_offer", "loan_purpose_code"),
                at(mergedProposal, "selectedLoanOffer", "loanPurposeCode"),
                at(preliminary, "preliminary_loan_package", "loan_purpose_code"),
                at(preliminary, "preliminaryLoanPackage", "loanPurposeCode"),
                at(preliminary, "loanRequest", "loanPurposeCode"),
                at(preliminary, "loanRequest", "loanPurpose"),
                at(preliminary, "loanPurposeCode")
        );
        if (purposeCode != null) {
            LoanPurpose purpose = loanPurposeRepository.findByCode(purposeCode)
                    .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Loan purpose is not configured: " + purposeCode));
            application.setLoanPurpose(purpose);
        }

        Integer termMonths = firstInteger(
                at(mergedProposal, "selected_loan_offer", "final_loan_term_months"),
                at(mergedProposal, "selectedLoanOffer", "finalLoanTermMonths"),
                at(preliminary, "preliminary_loan_package", "loan_term_months"),
                at(preliminary, "preliminaryLoanPackage", "loanTermMonths"),
                at(preliminary, "loanRequest", "termMonths"),
                at(preliminary, "loanRequest", "requestedTenure"),
                at(preliminary, "loanTermMonths")
        );
        if (termMonths != null) {
            LoanTerm term = loanTermRepository.findByTermMonths(termMonths)
                    .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Loan term is not configured: " + termMonths));
            application.setLoanTerm(term);
            application.setLoanTermMonths(termMonths);
        }

        String productCode = firstText(
                at(mergedProposal, "selected_loan_offer", "loan_product_code"),
                at(mergedProposal, "selectedLoanOffer", "loanProductCode"),
                at(mergedProposal, "selectedLoanProduct", "productCode"),
                at(mergedProposal, "selectedLoanProductCode"),
                at(mergedProposal, "selectedProductCode"),
                at(preliminary, "selectedLoanProduct", "productCode"),
                at(preliminary, "selectedLoanProductCode"),
                at(preliminary, "selectedProductCode")
        );
        if (productCode != null) {
            loanProductRepository.findByProductCode(productCode).ifPresent(application::setLoanProduct);
        }
    }

    private JsonNode payloadByStep(List<LoanApplicationStepData> steps, String stepCode) {
        return steps.stream()
                .filter(step -> stepCode.equals(step.getStep().getCode()))
                .findFirst()
                .map(step -> parseJson(step.getPayload()))
                .orElse(objectMapper.createObjectNode());
    }

    private LoanApplicationOnboardingDetailResponse toDetailResponse(String applicationCode) {
        LoanApplication application = findApplication(applicationCode);
        List<LoanApplicationStepResponse> steps = stepDataRepository.findByLoanApplication_LoanApplicationCodeOrderByStep_StepOrderAsc(applicationCode)
                .stream()
                .map(this::toStepResponse)
                .toList();
        return new LoanApplicationOnboardingDetailResponse(
                application.getId(),
                application.getLoanApplicationCode(),
                toStateEnum(application),
                toCompatibilityStatus(application),
                application.getCurrentStep() == null ? null : application.getCurrentStep().getCode(),
                application.getCurrentStep() == null ? null : application.getCurrentStep().getName(),
                application.getExpiredAt(),
                application.getCreatedAt(),
                application.getUpdatedAt(),
                toCustomerResponse(application.getCustomer()),
                steps
        );
    }

    private LoanApplicationOnboardingSummaryResponse toSummaryResponse(LoanApplication application) {
        Customer customer = application.getCustomer();
        return new LoanApplicationOnboardingSummaryResponse(
                application.getLoanApplicationCode(),
                toStateEnum(application),
                toCompatibilityStatus(application),
                customer.getCustomerCode(),
                customer.getFullName(),
                customer.getPhoneNumber(),
                application.getCurrentStep() == null ? null : application.getCurrentStep().getCode(),
                application.getCurrentStep() == null ? null : application.getCurrentStep().getName(),
                application.getExpiredAt(),
                application.getUpdatedAt()
        );
    }

    private LoanApplicationOnboardingCustomerResponse toCustomerResponse(Customer customer) {
        return new LoanApplicationOnboardingCustomerResponse(
                customer.getId(),
                customer.getCustomerCode(),
                customer.getFullName(),
                customer.getPhoneNumber(),
                customer.getIdentityNumber(),
                customer.getDateOfBirth()
        );
    }

    private LoanApplicationStepResponse toStepResponse(LoanApplicationStepData stepData) {
        return new LoanApplicationStepResponse(
                stepData.getStep().getCode(),
                stepData.getStep().getName(),
                stepData.getStep().getStepOrder(),
                LoanApplicationStepDataStatus.valueOf(stepData.getStatus().name()),
                stepData.isRequiresReview(),
                stepData.getInvalidatedByStep() == null ? null : stepData.getInvalidatedByStep().getCode(),
                stepData.getInvalidatedReason(),
                stepData.getCompletedAt(),
                stepData.getReviewedAt(),
                stepData.getUpdatedAt(),
                parseJson(stepData.getPayload())
        );
    }

    private LoanApplicationStepActionResponse actionResponse(
            LoanApplication application,
            String stepCode,
            LoanApplicationStepStatus stepStatus,
            boolean applicationCompleted,
            String message
    ) {
        return new LoanApplicationStepActionResponse(
                application.getLoanApplicationCode(),
                stepCode,
                LoanApplicationStepDataStatus.valueOf(stepStatus.name()),
                application.getCurrentStep() == null ? null : application.getCurrentStep().getCode(),
                applicationCompleted,
                message
        );
    }

    private LoanApplicationSubmitResponse submitResponse(LoanApplication application, String message) {
        return new LoanApplicationSubmitResponse(
                application.getLoanApplicationCode(),
                toStateEnum(application),
                toCompatibilityStatus(application),
                message
        );
    }

    private com.f88.loanonboarding.enums.LoanApplicationState toStateEnum(LoanApplication application) {
        return com.f88.loanonboarding.enums.LoanApplicationState.valueOf(application.getCurrentState().getCode());
    }

    private LoanApplicationOnboardingStatus toCompatibilityStatus(LoanApplication application) {
        return switch (application.getCurrentState().getCode()) {
            case STATE_CREATED -> LoanApplicationOnboardingStatus.CREATED;
            case STATE_IN_PROGRESS -> LoanApplicationOnboardingStatus.IN_PROGRESS;
            case STATE_COMPLETED -> LoanApplicationOnboardingStatus.COMPLETED;
            case STATE_SUBMITTED -> LoanApplicationOnboardingStatus.SUBMITTED;
            case STATE_CANCELLED -> LoanApplicationOnboardingStatus.CANCELLED;
            case "APP_EXPIRED" -> LoanApplicationOnboardingStatus.EXPIRED;
            default -> LoanApplicationOnboardingStatus.CREATED;
        };
    }

    private void saveStateHistory(LoanApplication application, LoanApplicationState fromState, LoanApplicationState toState, String actionCode, String note) {
        LoanApplicationStateHistory history = new LoanApplicationStateHistory();
        history.setLoanApplication(application);
        history.setFromState(fromState);
        history.setToState(toState);
        history.setActionCode(actionCode);
        history.setNote(note);
        history.setChangedAt(LocalDateTime.now());
        stateHistoryRepository.save(history);
    }

    private void saveStepHistory(
            LoanApplication application,
            LoanApplicationStep step,
            LoanApplicationStepHistoryAction action,
            String oldStatus,
            String newStatus,
            String note,
            String metadata
    ) {
        LoanApplicationStepHistory history = new LoanApplicationStepHistory();
        history.setLoanApplication(application);
        history.setStep(step);
        history.setAction(action);
        history.setOldStatus(oldStatus);
        history.setNewStatus(newStatus);
        history.setNote(note);
        history.setChangedAt(LocalDateTime.now());
        history.setMetadata(metadata == null || metadata.isBlank() ? EMPTY_JSON : metadata);
        stepHistoryRepository.save(history);
    }

    private String nextApplicationCode() {
        String applicationCode;
        do {
            applicationCode = "APP-" + Year.now() + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        } while (loanApplicationRepository.existsByLoanApplicationCode(applicationCode));
        return applicationCode;
    }

    private boolean hasPayload(JsonNode payload) {
        return payload != null && !payload.isNull() && !payload.isMissingNode();
    }

    private String toJson(JsonNode payload) {
        if (!hasPayload(payload)) {
            return EMPTY_JSON;
        }
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException ex) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "Invalid JSON payload");
        }
    }

    private JsonNode parseJson(String payload) {
        if (payload == null || payload.isBlank()) {
            return objectMapper.createObjectNode();
        }
        try {
            return objectMapper.readTree(payload);
        } catch (JsonProcessingException ex) {
            ObjectNode node = objectMapper.createObjectNode();
            node.put("raw", payload);
            return node;
        }
    }

    private JsonNode at(JsonNode root, String... path) {
        JsonNode current = root;
        for (String item : path) {
            if (current == null || current.isMissingNode() || current.isNull()) {
                return null;
            }
            current = current.get(item);
        }
        return current;
    }

    private String firstText(JsonNode... nodes) {
        for (JsonNode node : nodes) {
            if (node != null && !node.isNull() && !node.isMissingNode() && node.isTextual() && !node.asText().isBlank()) {
                return node.asText();
            }
        }
        return null;
    }

    private Integer firstInteger(JsonNode... nodes) {
        for (JsonNode node : nodes) {
            if (node == null || node.isNull() || node.isMissingNode()) {
                continue;
            }
            if (node.isInt() || node.isLong()) {
                return node.asInt();
            }
            if (node.isTextual() && node.asText().matches("\\d+")) {
                return Integer.parseInt(node.asText());
            }
        }
        return null;
    }

    private BigDecimal firstNumber(JsonNode... nodes) {
        for (JsonNode node : nodes) {
            if (node == null || node.isNull() || node.isMissingNode()) {
                continue;
            }
            if (node.isNumber()) {
                return node.decimalValue();
            }
            if (node.isTextual() && node.asText().matches("\\d+(\\.\\d+)?")) {
                return new BigDecimal(node.asText());
            }
        }
        return null;
    }
}
