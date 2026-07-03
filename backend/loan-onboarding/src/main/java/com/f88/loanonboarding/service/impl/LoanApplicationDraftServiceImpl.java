package com.f88.loanonboarding.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.f88.loanonboarding.common.error.ErrorCode;
import com.f88.loanonboarding.dto.request.draft.CreateLoanApplicationDraftRequest;
import com.f88.loanonboarding.dto.request.draft.SaveLoanApplicationDraftStepRequest;
import com.f88.loanonboarding.dto.response.draft.LoanApplicationDraftOverviewResponse;
import com.f88.loanonboarding.dto.response.draft.LoanApplicationDraftStepPayloadResponse;
import com.f88.loanonboarding.dto.response.draft.LoanApplicationDraftStepResponse;
import com.f88.loanonboarding.dto.response.draft.SaveLoanApplicationDraftStepResponse;
import com.f88.loanonboarding.entity.Customer;
import com.f88.loanonboarding.entity.LoanApplicationDraft;
import com.f88.loanonboarding.entity.LoanApplicationDraftHistory;
import com.f88.loanonboarding.entity.LoanApplicationDraftStepData;
import com.f88.loanonboarding.entity.LoanApplicationStep;
import com.f88.loanonboarding.exception.BusinessException;
import com.f88.loanonboarding.repository.CustomerRepository;
import com.f88.loanonboarding.repository.LoanApplicationDraftHistoryRepository;
import com.f88.loanonboarding.repository.LoanApplicationDraftRepository;
import com.f88.loanonboarding.repository.LoanApplicationDraftStepDataRepository;
import com.f88.loanonboarding.repository.LoanApplicationStepRepository;
import com.f88.loanonboarding.service.LoanApplicationDraftService;

@Service
public class LoanApplicationDraftServiceImpl implements LoanApplicationDraftService {

    private static final Logger log = LoggerFactory.getLogger(LoanApplicationDraftServiceImpl.class);

    private static final String DRAFT_CODE_PREFIX = "DRF-2026-";
    private static final String DRAFT_STATUS = "DRAFT";
    private static final String STATUS_NOT_STARTED = "NOT_STARTED";
    private static final String STATUS_IN_PROGRESS = "IN_PROGRESS";
    private static final String STATUS_COMPLETED = "COMPLETED";
    private static final String STEP_CUSTOMER_IDENTIFY = "CUSTOMER_IDENTIFY";
    private static final String STEP_PRELIMINARY_INFO = "PRELIMINARY_INFO";
    private static final String STEP_CUSTOMER_DETAIL = "CUSTOMER_DETAIL";
    private static final List<String> REQUIRED_STEP_CODES = List.of(
            STEP_CUSTOMER_IDENTIFY,
            STEP_PRELIMINARY_INFO,
            STEP_CUSTOMER_DETAIL,
            "ASSET_DETAIL",
            "FINAL_LOAN_PROPOSAL",
            "UPLOAD_COMPLETE"
    );
    private static final Set<String> SUPPORTED_SAVE_STEPS = Set.of(STEP_CUSTOMER_IDENTIFY, STEP_PRELIMINARY_INFO);

    private final CustomerRepository customerRepository;
    private final LoanApplicationStepRepository stepRepository;
    private final LoanApplicationDraftRepository draftRepository;
    private final LoanApplicationDraftStepDataRepository stepDataRepository;
    private final LoanApplicationDraftHistoryRepository historyRepository;
    private final ObjectMapper objectMapper;

    public LoanApplicationDraftServiceImpl(
            CustomerRepository customerRepository,
            LoanApplicationStepRepository stepRepository,
            LoanApplicationDraftRepository draftRepository,
            LoanApplicationDraftStepDataRepository stepDataRepository,
            LoanApplicationDraftHistoryRepository historyRepository,
            ObjectMapper objectMapper
    ) {
        this.customerRepository = customerRepository;
        this.stepRepository = stepRepository;
        this.draftRepository = draftRepository;
        this.stepDataRepository = stepDataRepository;
        this.historyRepository = historyRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public LoanApplicationDraftOverviewResponse createDraft(CreateLoanApplicationDraftRequest request) {
        try {
            Customer customer = customerRepository.findById(request.customerId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.CUSTOMER_NOT_FOUND));
            List<LoanApplicationStep> steps = activeSteps();
            LoanApplicationStep firstStep = stepByCode(steps, STEP_CUSTOMER_IDENTIFY);

            LoanApplicationDraft draft = new LoanApplicationDraft();
            draft.setDraftCode(nextDraftCode());
            draft.setCustomer(customer);
            draft.setCurrentStep(firstStep);
            draft.setStatus(DRAFT_STATUS);

            LoanApplicationDraft savedDraft = draftRepository.save(draft);
            for (LoanApplicationStep step : steps) {
                LoanApplicationDraftStepData stepData = new LoanApplicationDraftStepData();
                stepData.setDraft(savedDraft);
                stepData.setStep(step);
                stepData.setStatus(STEP_CUSTOMER_IDENTIFY.equals(step.getCode()) ? STATUS_IN_PROGRESS : STATUS_NOT_STARTED);
                stepData.setPayload(emptyPayload());
                stepDataRepository.save(stepData);
            }
            historyRepository.save(history(savedDraft, firstStep, "CREATE_DRAFT", null, DRAFT_STATUS, "Create loan application draft"));

            return toOverview(savedDraft);
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Failed to create loan application draft for customerId={}", request.customerId(), ex);
            throw ex;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public LoanApplicationDraftOverviewResponse getOverview(UUID draftId) {
        LoanApplicationDraft draft = findDraft(draftId);
        return toOverview(draft);
    }

    @Override
    @Transactional(readOnly = true)
    public LoanApplicationDraftStepPayloadResponse getStepPayload(UUID draftId, String stepCode) {
        LoanApplicationDraft draft = findDraft(draftId);
        LoanApplicationDraftStepData stepData = findStepData(draft, normalizeStepCode(stepCode));
        return new LoanApplicationDraftStepPayloadResponse(
                draft.getId(),
                stepData.getStep().getCode(),
                stepData.getStatus(),
                payloadOrEmpty(stepData.getPayload())
        );
    }

    @Override
    @Transactional
    public SaveLoanApplicationDraftStepResponse saveStep(
            UUID draftId,
            String stepCode,
            SaveLoanApplicationDraftStepRequest request
    ) {
        try {
            String normalizedStepCode = normalizeStepCode(stepCode);
            ensureSupportedSaveStep(normalizedStepCode);
            ensureObjectPayload(request.payload());

            LoanApplicationDraft draft = findDraft(draftId);
            ensureDraftStatus(draft);

            LoanApplicationDraftStepData currentStepData = findStepData(draft, normalizedStepCode);
            String oldStatus = currentStepData.getStatus();
            currentStepData.setPayload(request.payload());
            currentStepData.setStatus(STATUS_COMPLETED);
            currentStepData.setCompletedAt(LocalDateTime.now());
            stepDataRepository.save(currentStepData);

            String nextStepCode = nextStepCode(normalizedStepCode);
            LoanApplicationDraftStepData nextStepData = findStepData(draft, nextStepCode);
            if (STATUS_NOT_STARTED.equals(nextStepData.getStatus())) {
                nextStepData.setStatus(STATUS_IN_PROGRESS);
                stepDataRepository.save(nextStepData);
            }

            draft.setCurrentStep(nextStepData.getStep());
            LoanApplicationDraft savedDraft = draftRepository.save(draft);
            historyRepository.save(history(
                    savedDraft,
                    currentStepData.getStep(),
                    "SAVE_STEP",
                    oldStatus,
                    STATUS_COMPLETED,
                    "Save draft step " + normalizedStepCode
            ));

            return new SaveLoanApplicationDraftStepResponse(
                    savedDraft.getId(),
                    currentStepData.getStep().getCode(),
                    currentStepData.getStatus(),
                    savedDraft.getCurrentStep().getCode(),
                    nextStepData.getStep().getCode(),
                    savedDraft.getStatus()
            );
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Failed to save loan application draft step draftId={}, stepCode={}", draftId, stepCode, ex);
            throw ex;
        }
    }

    private List<LoanApplicationStep> activeSteps() {
        List<LoanApplicationStep> steps = stepRepository.findByActiveTrueOrderByStepOrderAsc();
        Map<String, LoanApplicationStep> stepByCode = steps.stream()
                .collect(Collectors.toMap(LoanApplicationStep::getCode, Function.identity()));
        for (String requiredStepCode : REQUIRED_STEP_CODES) {
            if (!stepByCode.containsKey(requiredStepCode)) {
                throw new BusinessException(
                        ErrorCode.SCHEMA_NOT_READY,
                        "Loan application draft step is not configured: " + requiredStepCode
                );
            }
        }
        return steps;
    }

    private LoanApplicationStep stepByCode(List<LoanApplicationStep> steps, String stepCode) {
        return steps.stream()
                .filter(step -> stepCode.equals(step.getCode()))
                .findFirst()
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.SCHEMA_NOT_READY,
                        "Loan application draft step is not configured: " + stepCode
                ));
    }

    private LoanApplicationDraft findDraft(UUID draftId) {
        return draftRepository.findById(draftId)
                .orElseThrow(() -> new BusinessException(ErrorCode.LOAN_APPLICATION_DRAFT_NOT_FOUND));
    }

    private LoanApplicationDraftStepData findStepData(LoanApplicationDraft draft, String stepCode) {
        return stepDataRepository.findByDraftAndStep_Code(draft, stepCode)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.RESOURCE_NOT_FOUND,
                        "Không tìm thấy dữ liệu step " + stepCode + " của hồ sơ vay nháp"
                ));
    }

    private void ensureDraftStatus(LoanApplicationDraft draft) {
        if (!DRAFT_STATUS.equals(draft.getStatus())) {
            throw new BusinessException(
                    ErrorCode.INVALID_LOAN_APPLICATION_STATE,
                    "Chỉ hồ sơ vay nháp trạng thái DRAFT mới được lưu step"
            );
        }
    }

    private void ensureSupportedSaveStep(String stepCode) {
        if (!SUPPORTED_SAVE_STEPS.contains(stepCode)) {
            throw new BusinessException(
                    ErrorCode.VALIDATION_ERROR,
                    "This demo flow only supports CUSTOMER_IDENTIFY and PRELIMINARY_INFO"
            );
        }
    }

    private void ensureObjectPayload(JsonNode payload) {
        if (payload == null || !payload.isObject()) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "payload phải là JSON object");
        }
    }

    private String normalizeStepCode(String stepCode) {
        return stepCode == null ? null : stepCode.trim().toUpperCase();
    }

    private String nextStepCode(String stepCode) {
        return switch (stepCode) {
            case STEP_CUSTOMER_IDENTIFY -> STEP_PRELIMINARY_INFO;
            case STEP_PRELIMINARY_INFO -> STEP_CUSTOMER_DETAIL;
            default -> throw new BusinessException(
                    ErrorCode.VALIDATION_ERROR,
                    "This demo flow only supports CUSTOMER_IDENTIFY and PRELIMINARY_INFO"
            );
        };
    }

    private LoanApplicationDraftOverviewResponse toOverview(LoanApplicationDraft draft) {
        List<LoanApplicationDraftStepData> stepData = stepDataRepository.findByDraftOrderByStep_StepOrderAsc(draft);
        JsonNode currentStepPayload = stepData.stream()
                .filter(item -> draft.getCurrentStep().getCode().equals(item.getStep().getCode()))
                .findFirst()
                .map(LoanApplicationDraftStepData::getPayload)
                .map(this::payloadOrEmpty)
                .orElseGet(this::emptyPayload);

        return new LoanApplicationDraftOverviewResponse(
                draft.getId(),
                draft.getDraftCode(),
                draft.getCustomer().getId(),
                draft.getStatus(),
                draft.getCurrentStep().getCode(),
                currentStepPayload,
                stepData.stream().map(this::toStepResponse).toList()
        );
    }

    private LoanApplicationDraftStepResponse toStepResponse(LoanApplicationDraftStepData stepData) {
        LoanApplicationStep step = stepData.getStep();
        return new LoanApplicationDraftStepResponse(
                step.getCode(),
                step.getName(),
                step.getStepOrder(),
                stepData.getStatus(),
                payloadOrEmpty(stepData.getPayload())
        );
    }

    private LoanApplicationDraftHistory history(
            LoanApplicationDraft draft,
            LoanApplicationStep step,
            String action,
            String oldStatus,
            String newStatus,
            String note
    ) {
        LoanApplicationDraftHistory history = new LoanApplicationDraftHistory();
        history.setDraft(draft);
        history.setStep(step);
        history.setAction(action);
        history.setOldStatus(oldStatus);
        history.setNewStatus(newStatus);
        history.setNote(note);
        return history;
    }

    private JsonNode emptyPayload() {
        return objectMapper.createObjectNode();
    }

    private JsonNode payloadOrEmpty(JsonNode payload) {
        return payload == null ? emptyPayload() : payload;
    }

    private String nextDraftCode() {
        String lastCode = draftRepository
                .findTopByDraftCodeStartingWithOrderByDraftCodeDesc(DRAFT_CODE_PREFIX)
                .map(LoanApplicationDraft::getDraftCode)
                .orElse(null);
        int next = lastCode == null ? 1 : parseDraftSequence(lastCode) + 1;
        return DRAFT_CODE_PREFIX + "%06d".formatted(next);
    }

    private int parseDraftSequence(String draftCode) {
        int delimiter = draftCode.lastIndexOf('-');
        if (delimiter < 0 || delimiter == draftCode.length() - 1) {
            return 0;
        }
        try {
            return Integer.parseInt(draftCode.substring(delimiter + 1));
        } catch (NumberFormatException ex) {
            return 0;
        }
    }
}
