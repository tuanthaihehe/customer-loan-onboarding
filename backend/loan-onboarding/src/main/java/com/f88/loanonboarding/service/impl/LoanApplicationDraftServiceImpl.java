package com.f88.loanonboarding.service.impl;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.f88.loanonboarding.common.error.ErrorCode;
import com.f88.loanonboarding.dto.request.draft.CreateLoanApplicationDraftRequest;
import com.f88.loanonboarding.dto.request.draft.SaveLoanApplicationDraftStepRequest;
import com.f88.loanonboarding.dto.response.draft.LoanApplicationDraftCustomerResponse;
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
    private static final String DRAFT_STATUS_COMPLETED = "COMPLETED";
    private static final String STEP_CUSTOMER_IDENTIFY = "CUSTOMER_IDENTIFY";
    private static final String STEP_PRELIMINARY_INFO = "PRELIMINARY_INFO";
    private static final String STEP_CUSTOMER_DETAIL = "CUSTOMER_DETAIL";
    private static final String STEP_UPLOAD_COMPLETE = "UPLOAD_COMPLETE";
    private static final List<String> REQUIRED_STEP_CODES = List.of(
            STEP_CUSTOMER_IDENTIFY,
            STEP_PRELIMINARY_INFO,
            STEP_CUSTOMER_DETAIL,
            "ASSET_DETAIL",
            "FINAL_LOAN_PROPOSAL",
            STEP_UPLOAD_COMPLETE
    );

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
            LoanApplicationStep preliminaryStep = stepByCode(steps, STEP_PRELIMINARY_INFO);

            LoanApplicationDraft draft = new LoanApplicationDraft();
            draft.setDraftCode(nextDraftCode());
            draft.setCustomer(customer);
            draft.setCurrentStep(preliminaryStep);
            draft.setStatus(DRAFT_STATUS);
            draft.setExpiredAt(LocalDateTime.now().plus(30, ChronoUnit.DAYS));

            LoanApplicationDraft savedDraft = draftRepository.save(draft);
            for (LoanApplicationStep step : steps) {
                LoanApplicationDraftStepData stepData = new LoanApplicationDraftStepData();
                stepData.setDraft(savedDraft);
                stepData.setStep(step);
                if (STEP_CUSTOMER_IDENTIFY.equals(step.getCode())) {
                    stepData.setStatus(STATUS_COMPLETED);
                    stepData.setCompletedAt(LocalDateTime.now());
                    stepData.setPayload(customerIdentifyPayload(customer));
                } else if (STEP_PRELIMINARY_INFO.equals(step.getCode())) {
                    stepData.setStatus(STATUS_IN_PROGRESS);
                    stepData.setPayload(emptyPayload());
                } else {
                    stepData.setStatus(STATUS_NOT_STARTED);
                    stepData.setPayload(emptyPayload());
                }
                stepDataRepository.save(stepData);
            }
            historyRepository.save(history(savedDraft, null, "CREATE_DRAFT", null, DRAFT_STATUS, "Tạo hồ sơ vay nháp"));
            historyRepository.save(history(savedDraft, firstStep, "COMPLETE_STEP", STATUS_IN_PROGRESS, STATUS_COMPLETED, "Hoàn thành định danh khách hàng"));
            historyRepository.save(history(savedDraft, preliminaryStep, "SAVE_STEP", STATUS_NOT_STARTED, STATUS_IN_PROGRESS, "Mở bước nhập thông tin sơ bộ"));

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
    public LoanApplicationDraftStepPayloadResponse getCurrentStepPayload(UUID draftId) {
        LoanApplicationDraft draft = findDraft(draftId);
        LoanApplicationDraftStepData stepData = findStepData(draft, draft.getCurrentStep().getCode());
        return new LoanApplicationDraftStepPayloadResponse(
                draft.getId(),
                stepData.getStep().getCode(),
                stepData.getStatus(),
                payloadOrEmpty(stepData.getPayload())
        );
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
            ensureObjectPayload(request.payload());

            LoanApplicationDraft draft = findDraft(draftId);
            ensureDraftStatus(draft);
            List<LoanApplicationStep> steps = activeSteps();

            LoanApplicationDraftStepData currentStepData = findStepData(draft, normalizedStepCode);
            String oldStatus = currentStepData.getStatus();
            if (STATUS_NOT_STARTED.equals(oldStatus)) {
                throw new BusinessException(
                        ErrorCode.INVALID_LOAN_APPLICATION_STATE,
                        "Chưa thể lưu bước " + normalizedStepCode + " vì bước này chưa được mở."
                );
            }
            currentStepData.setPayload(request.payload());
            currentStepData.setStatus(STATUS_COMPLETED);
            currentStepData.setCompletedAt(LocalDateTime.now());
            currentStepData.setRequiresReview(false);
            currentStepData.setReviewedAt(LocalDateTime.now());
            currentStepData.setInvalidatedAt(null);
            currentStepData.setInvalidatedByStep(null);
            stepDataRepository.save(currentStepData);
            markDownstreamForReview(draft, steps, currentStepData);

            LoanApplicationDraftStepData nextStepData = nextStepData(draft, steps, normalizedStepCode);
            String nextStepCode = null;
            if (nextStepData != null) {
                nextStepCode = nextStepData.getStep().getCode();
                String nextOldStatus = nextStepData.getStatus();
                if (STATUS_NOT_STARTED.equals(nextOldStatus)) {
                    nextStepData.setStatus(STATUS_IN_PROGRESS);
                    stepDataRepository.save(nextStepData);
                    historyRepository.save(history(
                            draft,
                            nextStepData.getStep(),
                            "SAVE_STEP",
                            nextOldStatus,
                            STATUS_IN_PROGRESS,
                            "Mở bước " + nextStepCode
                    ));
                }
                draft.setCurrentStep(nextStepData.getStep());
            } else {
                draft.setCurrentStep(currentStepData.getStep());
                draft.setStatus(DRAFT_STATUS_COMPLETED);
            }

            LoanApplicationDraft savedDraft = draftRepository.save(draft);
            historyRepository.save(history(
                    savedDraft,
                    currentStepData.getStep(),
                    "COMPLETE_STEP",
                    oldStatus,
                    STATUS_COMPLETED,
                    "Hoàn thành bước " + normalizedStepCode
            ));

            return new SaveLoanApplicationDraftStepResponse(
                    savedDraft.getId(),
                    currentStepData.getStep().getCode(),
                    currentStepData.getStatus(),
                    savedDraft.getCurrentStep().getCode(),
                    nextStepCode,
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

    private void ensureObjectPayload(JsonNode payload) {
        if (payload == null || !payload.isObject()) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "payload phải là JSON object");
        }
    }

    private String normalizeStepCode(String stepCode) {
        return stepCode == null ? null : stepCode.trim().toUpperCase();
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
                toCustomerResponse(draft.getCustomer()),
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

    private LoanApplicationDraftStepData nextStepData(
            LoanApplicationDraft draft,
            List<LoanApplicationStep> steps,
            String currentStepCode
    ) {
        for (int i = 0; i < steps.size(); i++) {
            if (currentStepCode.equals(steps.get(i).getCode()) && i + 1 < steps.size()) {
                return findStepData(draft, steps.get(i + 1).getCode());
            }
        }
        return null;
    }

    private void markDownstreamForReview(
            LoanApplicationDraft draft,
            List<LoanApplicationStep> steps,
            LoanApplicationDraftStepData changedStepData
    ) {
        int changedOrder = changedStepData.getStep().getStepOrder();
        for (LoanApplicationStep step : steps) {
            if (step.getStepOrder() <= changedOrder) {
                continue;
            }
            LoanApplicationDraftStepData downstream = findStepData(draft, step.getCode());
            if (STATUS_COMPLETED.equals(downstream.getStatus())) {
                downstream.setRequiresReview(true);
                downstream.setInvalidatedByStep(changedStepData.getStep());
                downstream.setInvalidatedAt(LocalDateTime.now());
                stepDataRepository.save(downstream);
                historyRepository.save(history(
                        draft,
                        downstream.getStep(),
                        "INVALIDATE_STEP",
                        downstream.getStatus(),
                        downstream.getStatus(),
                        "Cần rà soát lại do bước " + changedStepData.getStep().getCode() + " thay đổi"
                ));
            }
        }
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
        ObjectNode metadata = objectMapper.createObjectNode();
        if (step != null) {
            metadata.put("stepCode", step.getCode());
        }
        metadata.put("action", action);
        history.setMetadata(metadata);
        return history;
    }

    private LoanApplicationDraftCustomerResponse toCustomerResponse(Customer customer) {
        return new LoanApplicationDraftCustomerResponse(
                customer.getId(),
                customer.getCustomerCode(),
                customer.getFullName(),
                customer.getIdentityNumber(),
                customer.getPhoneNumber(),
                customer.getDateOfBirth(),
                customer.getStatus()
        );
    }

    private JsonNode customerIdentifyPayload(Customer customer) {
        ObjectNode payload = objectMapper.createObjectNode();
        payload.put("initialized", true);
        payload.put("identity_verified", true);
        payload.put("customer_id", customer.getId().toString());
        payload.put("customer_code", customer.getCustomerCode());
        payload.put("full_name", customer.getFullName());
        payload.put("identity_number", customer.getIdentityNumber());
        payload.put("phone_number", customer.getPhoneNumber());
        if (customer.getDateOfBirth() != null) {
            payload.put("date_of_birth", customer.getDateOfBirth().toString());
        }
        payload.put("status", customer.getStatus());
        return payload;
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
