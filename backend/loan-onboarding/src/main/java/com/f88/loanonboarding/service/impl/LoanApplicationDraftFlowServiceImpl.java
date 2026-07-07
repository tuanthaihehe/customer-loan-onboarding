package com.f88.loanonboarding.service.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.f88.loanonboarding.common.error.ErrorCode;
import com.f88.loanonboarding.dto.request.loan.CancelLoanApplicationDraftRequest;
import com.f88.loanonboarding.dto.request.loan.CompleteLoanApplicationDraftStepRequest;
import com.f88.loanonboarding.dto.request.loan.CreateLoanApplicationDraftFlowRequest;
import com.f88.loanonboarding.dto.request.loan.SaveLoanApplicationDraftStepRequest;
import com.f88.loanonboarding.dto.response.loan.LoanApplicationDraftCustomerResponse;
import com.f88.loanonboarding.dto.response.loan.LoanApplicationDraftDetailResponse;
import com.f88.loanonboarding.dto.response.loan.LoanApplicationDraftStepActionResponse;
import com.f88.loanonboarding.dto.response.loan.LoanApplicationDraftStepResponse;
import com.f88.loanonboarding.dto.response.loan.LoanApplicationDraftSubmitResponse;
import com.f88.loanonboarding.dto.response.loan.LoanApplicationDraftSummaryResponse;
import com.f88.loanonboarding.entity.Customer;
import com.f88.loanonboarding.entity.LoanApplication;
import com.f88.loanonboarding.entity.LoanApplicationDraft;
import com.f88.loanonboarding.entity.LoanApplicationDraftHistory;
import com.f88.loanonboarding.entity.LoanApplicationDraftStepData;
import com.f88.loanonboarding.entity.LoanApplicationState;
import com.f88.loanonboarding.entity.LoanApplicationStep;
import com.f88.loanonboarding.entity.LoanPurpose;
import com.f88.loanonboarding.entity.LoanTerm;
import com.f88.loanonboarding.enums.LoanApplicationDraftHistoryAction;
import com.f88.loanonboarding.enums.LoanApplicationDraftStatus;
import com.f88.loanonboarding.enums.LoanApplicationDraftStepStatus;
import com.f88.loanonboarding.exception.BusinessException;
import com.f88.loanonboarding.repository.CustomerRepository;
import com.f88.loanonboarding.repository.LoanApplicationDraftHistoryRepository;
import com.f88.loanonboarding.repository.LoanApplicationDraftRepository;
import com.f88.loanonboarding.repository.LoanApplicationDraftStepDataRepository;
import com.f88.loanonboarding.repository.LoanApplicationRepository;
import com.f88.loanonboarding.repository.LoanApplicationStateRepository;
import com.f88.loanonboarding.repository.LoanApplicationStepRepository;
import com.f88.loanonboarding.repository.LoanProductRepository;
import com.f88.loanonboarding.repository.LoanPurposeRepository;
import com.f88.loanonboarding.repository.LoanTermRepository;
import com.f88.loanonboarding.service.LoanApplicationDraftFlowService;

@Service
public class LoanApplicationDraftFlowServiceImpl implements LoanApplicationDraftFlowService {

    private static final String STEP_CUSTOMER_IDENTIFY = "CUSTOMER_IDENTIFY";
    private static final String STATE_APP_DRAFT = "APP_DRAFT";
    private static final String EMPTY_JSON = "{}";

    private final CustomerRepository customerRepository;
    private final LoanApplicationDraftRepository draftRepository;
    private final LoanApplicationStepRepository stepRepository;
    private final LoanApplicationDraftStepDataRepository stepDataRepository;
    private final LoanApplicationDraftHistoryRepository historyRepository;
    private final LoanApplicationRepository loanApplicationRepository;
    private final LoanApplicationStateRepository loanApplicationStateRepository;
    private final LoanPurposeRepository loanPurposeRepository;
    private final LoanTermRepository loanTermRepository;
    private final LoanProductRepository loanProductRepository;
    private final ObjectMapper objectMapper;

    public LoanApplicationDraftFlowServiceImpl(
            CustomerRepository customerRepository,
            LoanApplicationDraftRepository draftRepository,
            LoanApplicationStepRepository stepRepository,
            LoanApplicationDraftStepDataRepository stepDataRepository,
            LoanApplicationDraftHistoryRepository historyRepository,
            LoanApplicationRepository loanApplicationRepository,
            LoanApplicationStateRepository loanApplicationStateRepository,
            LoanPurposeRepository loanPurposeRepository,
            LoanTermRepository loanTermRepository,
            LoanProductRepository loanProductRepository,
            ObjectMapper objectMapper
    ) {
        this.customerRepository = customerRepository;
        this.draftRepository = draftRepository;
        this.stepRepository = stepRepository;
        this.stepDataRepository = stepDataRepository;
        this.historyRepository = historyRepository;
        this.loanApplicationRepository = loanApplicationRepository;
        this.loanApplicationStateRepository = loanApplicationStateRepository;
        this.loanPurposeRepository = loanPurposeRepository;
        this.loanTermRepository = loanTermRepository;
        this.loanProductRepository = loanProductRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public LoanApplicationDraftDetailResponse createDraft(CreateLoanApplicationDraftFlowRequest request) {
        Customer customer = findCustomer(request);
        List<LoanApplicationStep> steps = activeSteps();
        LoanApplicationStep firstStep = firstStep(steps);
        LoanApplicationStep nextStep = nextStep(steps, firstStep);
        JsonNode identifyPayload = request.customerIdentifyPayload();
        boolean completeIdentify = identifyPayload != null && !identifyPayload.isNull() && !identifyPayload.isMissingNode();
        LocalDateTime now = LocalDateTime.now();

        LoanApplicationDraft draft = new LoanApplicationDraft();
        draft.setDraftCode(nextDraftCode());
        draft.setCustomer(customer);
        draft.setCurrentStep(completeIdentify && nextStep != null ? nextStep : firstStep);
        draft.setStatus(LoanApplicationDraftStatus.DRAFT.name());
        draft.setExpiredAt(now.plusDays(30));
        draft.setCreatedAt(now);
        draft.setUpdatedAt(now);

        LoanApplicationDraft savedDraft = draftRepository.save(draft);
        for (LoanApplicationStep step : steps) {
            LoanApplicationDraftStepData stepData = new LoanApplicationDraftStepData();
            stepData.setDraft(savedDraft);
            stepData.setStep(step);
            stepData.setStatus(initialStepStatus(step, identifyPayload, completeIdentify).name());
            stepData.setPayload(STEP_CUSTOMER_IDENTIFY.equals(step.getCode()) ? normalizedPayload(identifyPayload) : emptyPayload());
            stepData.setCompletedAt(STEP_CUSTOMER_IDENTIFY.equals(step.getCode()) && completeIdentify ? now : null);
            stepData.setCreatedAt(now);
            stepData.setUpdatedAt(now);
            stepData.setRequiresReview(false);
            stepDataRepository.save(stepData);
        }

        saveHistory(savedDraft, null, LoanApplicationDraftHistoryAction.CREATE_DRAFT, null, "DRAFT", "Create loan application draft", EMPTY_JSON);
        if (completeIdentify) {
            saveHistory(savedDraft, firstStep, LoanApplicationDraftHistoryAction.COMPLETE_STEP, "NOT_STARTED", "COMPLETED", "Complete customer identify step on draft creation", toJsonString(identifyPayload));
        }

        return toDetailResponse(savedDraft.getDraftCode());
    }

    @Override
    @Transactional(readOnly = true)
    public List<LoanApplicationDraftSummaryResponse> findDrafts(LoanApplicationDraftStatus status) {
        List<LoanApplicationDraft> drafts = status == null
                ? draftRepository.findAllByOrderByUpdatedAtDesc()
                : draftRepository.findByStatusOrderByUpdatedAtDesc(status.name());
        return drafts.stream().map(this::toSummaryResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public LoanApplicationDraftDetailResponse getDraft(String draftCode) {
        return toDetailResponse(draftCode);
    }

    @Override
    @Transactional
    public LoanApplicationDraftStepActionResponse saveStep(
            String draftCode,
            String stepCode,
            SaveLoanApplicationDraftStepRequest request
    ) {
        LoanApplicationDraft draft = editableDraft(draftCode);
        LoanApplicationDraftStepData stepData = findStepData(draftCode, stepCode);
        String oldStatus = stepData.getStatus();
        LoanApplicationDraftStepStatus newStatus = request.status() == null
                ? LoanApplicationDraftStepStatus.IN_PROGRESS
                : request.status();
        if (newStatus == LoanApplicationDraftStepStatus.COMPLETED) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "Use complete step API to mark a draft step as COMPLETED.");
        }

        LocalDateTime now = LocalDateTime.now();
        stepData.setStatus(newStatus.name());
        stepData.setPayload(normalizedPayload(request.payload()));
        stepData.setCompletedAt(null);
        stepData.setUpdatedAt(now);
        stepData.setRequiresReview(false);
        stepData.setInvalidatedAt(null);
        stepData.setInvalidatedReason(null);
        stepData.setInvalidatedByStep(null);
        stepData.setReviewedAt(now);
        stepDataRepository.save(stepData);

        draft.setCurrentStep(stepData.getStep());
        draft.setUpdatedAt(now);
        draftRepository.save(draft);

        markDownstreamRequiresReview(draft, stepData.getStep(), now);
        saveHistory(draft, stepData.getStep(), LoanApplicationDraftHistoryAction.SAVE_STEP, oldStatus, newStatus.name(), "Save draft step payload", toJsonString(request.payload()));

        return new LoanApplicationDraftStepActionResponse(
                draft.getDraftCode(),
                stepCode,
                newStatus,
                draft.getCurrentStep().getCode(),
                false,
                "Draft step saved"
        );
    }

    @Override
    @Transactional
    public LoanApplicationDraftStepActionResponse completeStep(
            String draftCode,
            String stepCode,
            CompleteLoanApplicationDraftStepRequest request
    ) {
        LoanApplicationDraft draft = editableDraft(draftCode);
        LoanApplicationDraftStepData stepData = findStepData(draftCode, stepCode);
        String oldStatus = stepData.getStatus();
        LocalDateTime now = LocalDateTime.now();

        stepData.setStatus(LoanApplicationDraftStepStatus.COMPLETED.name());
        stepData.setPayload(normalizedPayload(request.payload()));
        stepData.setCompletedAt(now);
        stepData.setUpdatedAt(now);
        stepData.setRequiresReview(false);
        stepData.setInvalidatedAt(null);
        stepData.setInvalidatedReason(null);
        stepData.setInvalidatedByStep(null);
        stepData.setReviewedAt(now);
        stepDataRepository.save(stepData);

        List<LoanApplicationStep> steps = activeSteps();
        LoanApplicationStep nextStep = nextStep(steps, stepData.getStep());
        boolean allCompleted = allStepsCompleted(draftCode);
        if (nextStep != null) {
            draft.setCurrentStep(nextStep);
        }
        if (allCompleted) {
            draft.setStatus(LoanApplicationDraftStatus.COMPLETED.name());
        }
        draft.setUpdatedAt(now);
        draftRepository.save(draft);

        markDownstreamRequiresReview(draft, stepData.getStep(), now);
        saveHistory(draft, stepData.getStep(), LoanApplicationDraftHistoryAction.COMPLETE_STEP, oldStatus, "COMPLETED", "Complete draft step", toJsonString(request.payload()));
        if (allCompleted) {
            saveHistory(draft, null, LoanApplicationDraftHistoryAction.COMPLETE_DRAFT, "DRAFT", "COMPLETED", "All draft steps completed", EMPTY_JSON);
        }

        return new LoanApplicationDraftStepActionResponse(
                draft.getDraftCode(),
                stepCode,
                LoanApplicationDraftStepStatus.COMPLETED,
                draft.getCurrentStep().getCode(),
                allCompleted,
                allCompleted ? "Draft completed and ready to submit" : "Draft step completed"
        );
    }

    @Override
    @Transactional
    public LoanApplicationDraftSubmitResponse submit(String draftCode) {
        LoanApplicationDraft draft = findDraft(draftCode);
        if (statusEquals(draft, LoanApplicationDraftStatus.CONVERTED)) {
            String applicationCode = draft.getConvertedLoanApplication() == null
                    ? null
                    : draft.getConvertedLoanApplication().getLoanApplicationCode();
            return new LoanApplicationDraftSubmitResponse(draft.getDraftCode(), draftStatus(draft), applicationCode, "Draft was already converted");
        }
        if (statusEquals(draft, LoanApplicationDraftStatus.CANCELLED) || statusEquals(draft, LoanApplicationDraftStatus.EXPIRED)) {
            throw new BusinessException(ErrorCode.INVALID_LOAN_APPLICATION_STATE, "Only active or completed draft can be submitted.");
        }
        ensureReadyToSubmit(draftCode);

        LoanApplication application = new LoanApplication();
        application.setLoanApplicationCode(nextApplicationCode());
        application.setCustomer(draft.getCustomer());
        application.setCurrentState(findApplicationState(STATE_APP_DRAFT));
        applyPayloadToLoanApplication(application, draftCode);

        LoanApplication savedApplication = loanApplicationRepository.save(application);
        String oldStatus = draft.getStatus();
        draft.setStatus(LoanApplicationDraftStatus.CONVERTED.name());
        draft.setConvertedLoanApplication(savedApplication);
        draft.setUpdatedAt(LocalDateTime.now());
        draftRepository.save(draft);

        saveHistory(
                draft,
                null,
                LoanApplicationDraftHistoryAction.CONVERT_DRAFT,
                oldStatus,
                "CONVERTED",
                "Convert draft to loan application",
                "{\"loanApplicationCode\":\"" + savedApplication.getLoanApplicationCode() + "\"}"
        );

        return new LoanApplicationDraftSubmitResponse(
                draft.getDraftCode(),
                LoanApplicationDraftStatus.CONVERTED,
                savedApplication.getLoanApplicationCode(),
                "Draft converted to loan application"
        );
    }

    @Override
    @Transactional
    public LoanApplicationDraftSubmitResponse cancel(String draftCode, CancelLoanApplicationDraftRequest request) {
        LoanApplicationDraft draft = editableDraft(draftCode);
        String oldStatus = draft.getStatus();
        draft.setStatus(LoanApplicationDraftStatus.CANCELLED.name());
        draft.setUpdatedAt(LocalDateTime.now());
        draftRepository.save(draft);

        String note = request == null || request.note() == null || request.note().isBlank()
                ? "Cancel draft"
                : request.note();
        saveHistory(draft, null, LoanApplicationDraftHistoryAction.CANCEL_DRAFT, oldStatus, "CANCELLED", note, EMPTY_JSON);
        return new LoanApplicationDraftSubmitResponse(draft.getDraftCode(), LoanApplicationDraftStatus.CANCELLED, null, "Draft cancelled");
    }

    private Customer findCustomer(CreateLoanApplicationDraftFlowRequest request) {
        if (request.customerId() != null) {
            return customerRepository.findById(request.customerId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.CUSTOMER_NOT_FOUND));
        }
        if (request.customerCode() != null && !request.customerCode().isBlank()) {
            return customerRepository.findByCustomerCode(request.customerCode())
                    .orElseThrow(() -> new BusinessException(ErrorCode.CUSTOMER_NOT_FOUND));
        }
        throw new BusinessException(ErrorCode.VALIDATION_ERROR, "customerId or customerCode is required to create loan application draft.");
    }

    private LoanApplicationDraft editableDraft(String draftCode) {
        LoanApplicationDraft draft = findDraft(draftCode);
        if (statusEquals(draft, LoanApplicationDraftStatus.CONVERTED)
                || statusEquals(draft, LoanApplicationDraftStatus.CANCELLED)
                || statusEquals(draft, LoanApplicationDraftStatus.EXPIRED)) {
            throw new BusinessException(ErrorCode.INVALID_LOAN_APPLICATION_STATE, "Draft is not editable in status " + draft.getStatus());
        }
        return draft;
    }

    private LoanApplicationDraft findDraft(String draftCode) {
        return draftRepository.findByDraftCode(draftCode)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Loan application draft not found: " + draftCode));
    }

    private LoanApplicationDraftStepData findStepData(String draftCode, String stepCode) {
        return stepDataRepository.findByDraft_DraftCodeAndStep_Code(draftCode, stepCode)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Draft step not found: " + stepCode));
    }

    private List<LoanApplicationStep> activeSteps() {
        List<LoanApplicationStep> steps = stepRepository.findByActiveTrueOrderByStepOrderAsc();
        if (steps.isEmpty()) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Loan application steps are not configured.");
        }
        return steps;
    }

    private LoanApplicationStep firstStep(List<LoanApplicationStep> steps) {
        return steps.get(0);
    }

    private LoanApplicationStep nextStep(List<LoanApplicationStep> steps, LoanApplicationStep currentStep) {
        return steps.stream()
                .filter(step -> step.getStepOrder() > currentStep.getStepOrder())
                .min(Comparator.comparingInt(LoanApplicationStep::getStepOrder))
                .orElse(null);
    }

    private LoanApplicationDraftStepStatus initialStepStatus(
            LoanApplicationStep step,
            JsonNode identifyPayload,
            boolean completeIdentify
    ) {
        if (!STEP_CUSTOMER_IDENTIFY.equals(step.getCode())) {
            return LoanApplicationDraftStepStatus.NOT_STARTED;
        }
        if (completeIdentify) {
            return LoanApplicationDraftStepStatus.COMPLETED;
        }
        return identifyPayload == null ? LoanApplicationDraftStepStatus.NOT_STARTED : LoanApplicationDraftStepStatus.IN_PROGRESS;
    }

    private void markDownstreamRequiresReview(
            LoanApplicationDraft draft,
            LoanApplicationStep changedStep,
            LocalDateTime now
    ) {
        List<LoanApplicationDraftStepData> downstreamSteps =
                stepDataRepository.findByDraftAndStep_StepOrderGreaterThan(draft, changedStep.getStepOrder());
        for (LoanApplicationDraftStepData downstreamStep : downstreamSteps) {
            if (stepStatusEquals(downstreamStep, LoanApplicationDraftStepStatus.NOT_STARTED)
                    && isEmptyPayload(downstreamStep.getPayload())) {
                continue;
            }
            downstreamStep.setRequiresReview(true);
            downstreamStep.setInvalidatedAt(now);
            downstreamStep.setInvalidatedReason("Upstream step changed: " + changedStep.getCode());
            downstreamStep.setInvalidatedByStep(changedStep);
            downstreamStep.setUpdatedAt(now);
            stepDataRepository.save(downstreamStep);
            saveHistory(
                    draft,
                    downstreamStep.getStep(),
                    LoanApplicationDraftHistoryAction.INVALIDATE_STEP,
                    downstreamStep.getStatus(),
                    downstreamStep.getStatus(),
                    "Mark downstream step for review because upstream payload changed",
                    "{\"changedStepCode\":\"" + changedStep.getCode() + "\"}"
            );
        }
    }

    private boolean allStepsCompleted(String draftCode) {
        return stepDataRepository.findByDraft_DraftCodeOrderByStep_StepOrderAsc(draftCode)
                .stream()
                .allMatch(step -> stepStatusEquals(step, LoanApplicationDraftStepStatus.COMPLETED));
    }

    private void ensureReadyToSubmit(String draftCode) {
        List<LoanApplicationDraftStepData> steps = stepDataRepository.findByDraft_DraftCodeOrderByStep_StepOrderAsc(draftCode);
        List<String> incompleteSteps = steps.stream()
                .filter(step -> !stepStatusEquals(step, LoanApplicationDraftStepStatus.COMPLETED))
                .map(step -> step.getStep().getCode())
                .toList();
        if (!incompleteSteps.isEmpty()) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "Draft has incomplete steps: " + String.join(", ", incompleteSteps));
        }
        List<String> reviewSteps = steps.stream()
                .filter(LoanApplicationDraftStepData::isRequiresReview)
                .map(step -> step.getStep().getCode())
                .toList();
        if (!reviewSteps.isEmpty()) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "Draft has steps requiring review: " + String.join(", ", reviewSteps));
        }
    }

    private void applyPayloadToLoanApplication(LoanApplication application, String draftCode) {
        List<LoanApplicationDraftStepData> steps = stepDataRepository.findByDraft_DraftCodeOrderByStep_StepOrderAsc(draftCode);
        JsonNode preliminary = payloadByStep(steps, "PRELIMINARY_INFO");
        JsonNode finalProposal = payloadByStep(steps, "FINAL_LOAN_PROPOSAL");
        JsonNode mergedProposal = payloadByStep(steps, "CUSTOMER_ASSET_LOAN_PROPOSAL");

        BigDecimal requestedAmount = firstNumber(
                at(mergedProposal, "selected_loan_offer", "final_requested_amount"),
                at(mergedProposal, "selectedLoanOffer", "finalRequestedAmount"),
                at(finalProposal, "proposal", "approvedAmount"),
                at(finalProposal, "approvedAmount"),
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
                at(preliminary, "loanPurposeCode"),
                at(preliminary, "loanPurpose")
        );
        if (purposeCode != null) {
            LoanPurpose purpose = loanPurposeRepository.findByCode(purposeCode)
                    .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Loan purpose is not configured: " + purposeCode));
            application.setLoanPurpose(purpose);
        }

        Integer termMonths = firstInteger(
                at(mergedProposal, "selected_loan_offer", "final_loan_term_months"),
                at(mergedProposal, "selectedLoanOffer", "finalLoanTermMonths"),
                at(finalProposal, "proposal", "termMonths"),
                at(finalProposal, "termMonths"),
                at(preliminary, "preliminary_loan_package", "loan_term_months"),
                at(preliminary, "preliminaryLoanPackage", "loanTermMonths"),
                at(preliminary, "loanRequest", "termMonths"),
                at(preliminary, "loanRequest", "requestedTenure"),
                at(preliminary, "loanTermMonths"),
                at(preliminary, "requestedTenure")
        );
        if (termMonths != null) {
            LoanTerm term = loanTermRepository.findByTermMonths(termMonths)
                    .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Loan term is not configured: " + termMonths));
            application.setLoanTerm(term);
            application.setLoanTermMonths(termMonths);
        }

        String productCode = firstText(
                at(mergedProposal, "selected_loan_offer", "loan_product_code"),
                at(mergedProposal, "selected_loan_offer", "loan_product_id"),
                at(mergedProposal, "selectedLoanOffer", "loanProductCode"),
                at(mergedProposal, "selectedLoanOffer", "loanProductId"),
                at(finalProposal, "proposal", "selectedProductCode"),
                at(finalProposal, "selectedProductCode"),
                at(finalProposal, "loanProductCode")
        );
        if (productCode != null) {
            loanProductRepository.findByProductCode(productCode).ifPresent(application::setLoanProduct);
        }
    }

    private JsonNode payloadByStep(List<LoanApplicationDraftStepData> steps, String stepCode) {
        return steps.stream()
                .filter(step -> stepCode.equals(step.getStep().getCode()))
                .findFirst()
                .map(step -> payloadOrEmpty(step.getPayload()))
                .orElse(objectMapper.createObjectNode());
    }

    private LoanApplicationState findApplicationState(String stateCode) {
        return loanApplicationStateRepository.findByCode(stateCode)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_LOAN_APPLICATION_STATE, "Loan application state is not configured: " + stateCode));
    }

    private LoanApplicationDraftDetailResponse toDetailResponse(String draftCode) {
        LoanApplicationDraft draft = findDraft(draftCode);
        List<LoanApplicationDraftStepResponse> steps = stepDataRepository.findByDraft_DraftCodeOrderByStep_StepOrderAsc(draftCode)
                .stream()
                .map(this::toStepResponse)
                .toList();
        return new LoanApplicationDraftDetailResponse(
                draft.getId(),
                draft.getDraftCode(),
                draftStatus(draft),
                draft.getCurrentStep().getCode(),
                draft.getCurrentStep().getName(),
                draft.getExpiredAt(),
                draft.getCreatedAt(),
                draft.getUpdatedAt(),
                draft.getConvertedLoanApplication() == null ? null : draft.getConvertedLoanApplication().getLoanApplicationCode(),
                toCustomerResponse(draft.getCustomer()),
                steps
        );
    }

    private LoanApplicationDraftSummaryResponse toSummaryResponse(LoanApplicationDraft draft) {
        Customer customer = draft.getCustomer();
        return new LoanApplicationDraftSummaryResponse(
                draft.getDraftCode(),
                draftStatus(draft),
                customer.getCustomerCode(),
                customer.getFullName(),
                customer.getPhoneNumber(),
                draft.getCurrentStep().getCode(),
                draft.getCurrentStep().getName(),
                draft.getExpiredAt(),
                draft.getUpdatedAt(),
                draft.getConvertedLoanApplication() == null ? null : draft.getConvertedLoanApplication().getLoanApplicationCode()
        );
    }

    private LoanApplicationDraftCustomerResponse toCustomerResponse(Customer customer) {
        return new LoanApplicationDraftCustomerResponse(
                customer.getId(),
                customer.getCustomerCode(),
                customer.getFullName(),
                customer.getPhoneNumber(),
                customer.getIdentityNumber(),
                customer.getDateOfBirth()
        );
    }

    private LoanApplicationDraftStepResponse toStepResponse(LoanApplicationDraftStepData stepData) {
        return new LoanApplicationDraftStepResponse(
                stepData.getStep().getCode(),
                stepData.getStep().getName(),
                stepData.getStep().getStepOrder(),
                stepStatus(stepData),
                stepData.isRequiresReview(),
                stepData.getInvalidatedByStep() == null ? null : stepData.getInvalidatedByStep().getCode(),
                stepData.getInvalidatedReason(),
                stepData.getCompletedAt(),
                stepData.getReviewedAt(),
                stepData.getUpdatedAt(),
                payloadOrEmpty(stepData.getPayload())
        );
    }

    private void saveHistory(
            LoanApplicationDraft draft,
            LoanApplicationStep step,
            LoanApplicationDraftHistoryAction action,
            String oldStatus,
            String newStatus,
            String note,
            String metadata
    ) {
        LoanApplicationDraftHistory history = new LoanApplicationDraftHistory();
        history.setDraft(draft);
        history.setStep(step);
        history.setAction(action.name());
        history.setOldStatus(oldStatus);
        history.setNewStatus(newStatus);
        history.setNote(note);
        history.setChangedAt(LocalDateTime.now());
        history.setMetadata(parseJson(metadata));
        historyRepository.save(history);
    }

    private String nextDraftCode() {
        String draftCode;
        do {
            draftCode = "DRF-" + Year.now() + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        } while (draftRepository.existsByDraftCode(draftCode));
        return draftCode;
    }

    private String nextApplicationCode() {
        String applicationCode;
        do {
            applicationCode = "APP-" + Year.now() + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        } while (loanApplicationRepository.existsByLoanApplicationCode(applicationCode));
        return applicationCode;
    }

    private JsonNode normalizedPayload(JsonNode payload) {
        if (payload == null || payload.isNull() || payload.isMissingNode()) {
            return emptyPayload();
        }
        return payload;
    }

    private String toJsonString(JsonNode payload) {
        if (payload == null || payload.isNull() || payload.isMissingNode()) {
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
            return emptyPayload();
        }
        try {
            return objectMapper.readTree(payload);
        } catch (JsonProcessingException ex) {
            ObjectNode node = objectMapper.createObjectNode();
            node.put("raw", payload);
            return node;
        }
    }

    private JsonNode payloadOrEmpty(JsonNode payload) {
        return payload == null || payload.isNull() || payload.isMissingNode() ? emptyPayload() : payload;
    }

    private JsonNode emptyPayload() {
        return objectMapper.createObjectNode();
    }

    private boolean isEmptyPayload(JsonNode payload) {
        return payload == null || payload.isNull() || payload.isMissingNode() || (payload.isObject() && payload.isEmpty());
    }

    private boolean statusEquals(LoanApplicationDraft draft, LoanApplicationDraftStatus expected) {
        return expected.name().equals(draft.getStatus());
    }

    private LoanApplicationDraftStatus draftStatus(LoanApplicationDraft draft) {
        return LoanApplicationDraftStatus.valueOf(draft.getStatus());
    }

    private boolean stepStatusEquals(LoanApplicationDraftStepData stepData, LoanApplicationDraftStepStatus expected) {
        return expected.name().equals(stepData.getStatus());
    }

    private LoanApplicationDraftStepStatus stepStatus(LoanApplicationDraftStepData stepData) {
        return LoanApplicationDraftStepStatus.valueOf(stepData.getStatus());
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
