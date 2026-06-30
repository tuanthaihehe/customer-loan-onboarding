package com.f88.loanonboarding.service.impl;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.f88.loanonboarding.common.error.ErrorCode;
import com.f88.loanonboarding.dto.request.loan.CancelLoanApplicationRequest;
import com.f88.loanonboarding.dto.request.loan.CreateLoanApplicationRequest;
import com.f88.loanonboarding.dto.request.loan.SaveLoanApplicationDraftRequest;
import com.f88.loanonboarding.dto.response.loan.LoanApplicationDetailResponse;
import com.f88.loanonboarding.dto.response.loan.LoanApplicationDraftResponse;
import com.f88.loanonboarding.dto.response.loan.StepCompletionResponse;
import com.f88.loanonboarding.dto.response.loan.SubmitForApprovalResponse;
import com.f88.loanonboarding.entity.CustomerEntity;
import com.f88.loanonboarding.entity.LoanApplicationEntity;
import com.f88.loanonboarding.entity.LoanApplicationStateEntity;
import com.f88.loanonboarding.entity.LoanApplicationStateHistoryEntity;
import com.f88.loanonboarding.entity.LoanApplicationStateTransitionEntity;
import com.f88.loanonboarding.entity.LoanPurposeEntity;
import com.f88.loanonboarding.entity.LoanTermEntity;
import com.f88.loanonboarding.enums.LoanApplicationState;
import com.f88.loanonboarding.exception.BusinessException;
import com.f88.loanonboarding.repository.CustomerRepository;
import com.f88.loanonboarding.repository.LoanApplicationRepository;
import com.f88.loanonboarding.repository.LoanApplicationStateHistoryRepository;
import com.f88.loanonboarding.repository.LoanApplicationStateRepository;
import com.f88.loanonboarding.repository.LoanApplicationStateTransitionRepository;
import com.f88.loanonboarding.repository.LoanPurposeRepository;
import com.f88.loanonboarding.repository.LoanTermRepository;
import com.f88.loanonboarding.rule.RuleContext;
import com.f88.loanonboarding.rule.RuleEvaluationService;
import com.f88.loanonboarding.rule.loan.LoanPurposeRule;
import com.f88.loanonboarding.rule.loan.LoanTenureRule;
import com.f88.loanonboarding.rule.loan.RequestedAmountRule;
import com.f88.loanonboarding.service.LoanApplicationService;

@Service
public class LoanApplicationServiceDbImpl implements LoanApplicationService {

    private static final String STATE_DRAFT = "APP_DRAFT";
    private static final String STATE_SUBMITTED = "APP_SUBMITTED";
    private static final String APPLICATION_CODE_PREFIX = "APP-2026-";

    private final CustomerRepository customerRepository;
    private final LoanApplicationRepository loanApplicationRepository;
    private final LoanApplicationStateRepository stateRepository;
    private final LoanApplicationStateTransitionRepository transitionRepository;
    private final LoanApplicationStateHistoryRepository historyRepository;
    private final LoanPurposeRepository loanPurposeRepository;
    private final LoanTermRepository loanTermRepository;
    private final RuleEvaluationService ruleEvaluationService;

    public LoanApplicationServiceDbImpl(
            CustomerRepository customerRepository,
            LoanApplicationRepository loanApplicationRepository,
            LoanApplicationStateRepository stateRepository,
            LoanApplicationStateTransitionRepository transitionRepository,
            LoanApplicationStateHistoryRepository historyRepository,
            LoanPurposeRepository loanPurposeRepository,
            LoanTermRepository loanTermRepository,
            RuleEvaluationService ruleEvaluationService
    ) {
        this.customerRepository = customerRepository;
        this.loanApplicationRepository = loanApplicationRepository;
        this.stateRepository = stateRepository;
        this.transitionRepository = transitionRepository;
        this.historyRepository = historyRepository;
        this.loanPurposeRepository = loanPurposeRepository;
        this.loanTermRepository = loanTermRepository;
        this.ruleEvaluationService = ruleEvaluationService;
    }

    @Override
    @Transactional
    public LoanApplicationDraftResponse createDraft(CreateLoanApplicationRequest request) {
        CustomerEntity customer = customerRepository.findByCustomerCode(request.customerCode())
                .orElseThrow(() -> new BusinessException(ErrorCode.CUSTOMER_NOT_FOUND, "Không tìm thấy khách hàng trong database"));
        LoanApplicationStateEntity draftState = findState(STATE_DRAFT);

        LoanApplicationEntity application = new LoanApplicationEntity();
        application.setLoanApplicationCode(nextApplicationCode());
        application.setCustomer(customer);
        application.setCurrentState(draftState);
        application.setBranch(request.branchCode());
        application = loanApplicationRepository.saveAndFlush(application);

        insertHistory(application, null, draftState, "CREATE", request.staffCode(), "Tạo hồ sơ vay nháp");
        LocalDateTime createdAt = findHistoryTime(application, "CREATE").orElse(LocalDateTime.now());

        return new LoanApplicationDraftResponse(
                application.getLoanApplicationCode(),
                LoanApplicationState.APP_DRAFT,
                customer.getCustomerCode(),
                createdAt,
                createdAt
        );
    }

    @Override
    @Transactional(readOnly = true)
    public LoanApplicationDetailResponse getDetail(String applicationCode) {
        LoanApplicationEntity application = findApplication(applicationCode);
        LocalDateTime updatedAt = findLastHistoryTime(application).orElse(null);

        Map<String, Object> applicantSnapshot = new LinkedHashMap<>();
        applicantSnapshot.put("customerCode", application.getCustomer().getCustomerCode());
        applicantSnapshot.put("fullName", application.getCustomer().getFullName());
        applicantSnapshot.put("dateOfBirth", application.getCustomer().getDateOfBirth());
        applicantSnapshot.put("identityNumber", application.getCustomer().getIdentityNumber());
        applicantSnapshot.put("phoneNumber", application.getCustomer().getPhoneNumber());

        Map<String, Object> loanRequest = new LinkedHashMap<>();
        loanRequest.put("requestedAmount", application.getRequestedAmount());
        loanRequest.put("loanPurpose", application.getLoanPurpose() == null ? null : application.getLoanPurpose().getCode());
        loanRequest.put("requestedTenure", application.getLoanTermMonths());
        loanRequest.put("branchCode", application.getBranch());

        Map<String, Object> stepStatus = new LinkedHashMap<>();
        stepStatus.put("currentState", application.getCurrentState().getCode());
        stepStatus.put("latestActionAt", updatedAt);

        return new LoanApplicationDetailResponse(
                application.getLoanApplicationCode(),
                LoanApplicationState.valueOf(application.getCurrentState().getCode()),
                application.getCustomer().getCustomerCode(),
                applicantSnapshot,
                loanRequest,
                Map.of(),
                Map.of(),
                stepStatus,
                updatedAt
        );
    }

    @Override
    @Transactional
    public LoanApplicationDraftResponse saveDraft(String applicationCode, SaveLoanApplicationDraftRequest request) {
        ruleEvaluationService.validateOrThrow(
                RuleContext.loan(
                        request.loanRequest().requestedAmount(),
                        request.loanRequest().requestedTenure(),
                        request.loanRequest().loanPurpose()
                ),
                List.of(new RequestedAmountRule(), new LoanTenureRule(), new LoanPurposeRule())
        );

        LoanApplicationEntity application = findApplication(applicationCode);
        ensureState(application.getCurrentState().getCode(), STATE_DRAFT, "Chỉ hồ sơ nháp mới được lưu thông tin khoản vay");

        LoanPurposeEntity loanPurpose = loanPurposeRepository.findByCodeAndActiveTrue(request.loanRequest().loanPurpose())
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_LOAN_PURPOSE, "Mục đích vay không tồn tại hoặc đã ngừng áp dụng trong database"));
        LoanTermEntity loanTerm = loanTermRepository.findByTermMonthsAndActiveTrue(request.loanRequest().requestedTenure())
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_LOAN_TERM, "Kỳ hạn vay không tồn tại hoặc đã ngừng áp dụng trong database"));

        application.setRequestedAmount(request.loanRequest().requestedAmount());
        application.setLoanPurpose(loanPurpose);
        application.setLoanTerm(loanTerm);
        application.setLoanTermMonths(request.loanRequest().requestedTenure());
        loanApplicationRepository.save(application);

        insertHistory(application, null, application.getCurrentState(), "SAVE_DRAFT", "system", "Lưu thông tin nháp");
        LocalDateTime createdAt = findHistoryTime(application, "CREATE").orElse(null);
        LocalDateTime savedAt = findHistoryTime(application, "SAVE_DRAFT").orElse(LocalDateTime.now());

        return new LoanApplicationDraftResponse(
                application.getLoanApplicationCode(),
                LoanApplicationState.valueOf(application.getCurrentState().getCode()),
                application.getCustomer().getCustomerCode(),
                createdAt,
                savedAt
        );
    }

    @Override
    @Transactional
    public LoanApplicationDraftResponse cancel(String applicationCode, CancelLoanApplicationRequest request) {
        LoanApplicationEntity application = findApplication(applicationCode);
        LoanApplicationStateEntity cancelledState = findTransitionToState(application.getCurrentState(), "CANCEL");
        LoanApplicationStateEntity fromState = application.getCurrentState();

        application.setCurrentState(cancelledState);
        loanApplicationRepository.save(application);
        insertHistory(application, fromState, cancelledState, "CANCEL", "system", request.note());

        return new LoanApplicationDraftResponse(
                application.getLoanApplicationCode(),
                LoanApplicationState.valueOf(application.getCurrentState().getCode()),
                application.getCustomer().getCustomerCode(),
                findHistoryTime(application, "CREATE").orElse(null),
                findHistoryTime(application, "CANCEL").orElse(LocalDateTime.now())
        );
    }

    @Override
    @Transactional(readOnly = true)
    public StepCompletionResponse completePreliminaryStep(String applicationCode) {
        LoanApplicationEntity application = findApplication(applicationCode);
        List<String> errors = new java.util.ArrayList<>();
        if (application.getRequestedAmount() == null) {
            errors.add("Số tiền vay chưa được nhập");
        }
        if (application.getLoanPurpose() == null) {
            errors.add("Mục đích vay chưa được nhập");
        }
        if (application.getLoanTermMonths() == null) {
            errors.add("Kỳ hạn vay chưa được nhập");
        }

        return new StepCompletionResponse(
                applicationCode,
                "PRELIMINARY",
                errors.isEmpty(),
                errors.isEmpty() ? "SUBMIT_FOR_APPROVAL" : "PRELIMINARY",
                errors
        );
    }

    @Override
    @Transactional
    public SubmitForApprovalResponse submitForApproval(String applicationCode) {
        LoanApplicationEntity application = findApplication(applicationCode);
        ensureState(application.getCurrentState().getCode(), STATE_DRAFT, "Chỉ hồ sơ nháp mới được gửi phê duyệt");

        if (application.getRequestedAmount() == null || application.getLoanPurpose() == null || application.getLoanTermMonths() == null) {
            throw new BusinessException(ErrorCode.INVALID_REQUESTED_AMOUNT, "Hồ sơ chưa đủ thông tin khoản vay để gửi phê duyệt");
        }

        LoanApplicationStateEntity submittedState = findTransitionToState(application.getCurrentState(), "SUBMIT");
        LoanApplicationStateEntity fromState = application.getCurrentState();
        application.setCurrentState(submittedState);
        loanApplicationRepository.save(application);
        insertHistory(application, fromState, submittedState, "SUBMIT", "system", "Gửi hồ sơ vào luồng xử lý");

        LocalDateTime submittedAt = findHistoryTime(application, "SUBMIT").orElse(LocalDateTime.now());
        return new SubmitForApprovalResponse(
                applicationCode,
                LoanApplicationState.APP_SUBMITTED,
                null,
                "LoanApplicationSubmitted",
                submittedAt,
                "Hồ sơ đã được gửi vào luồng xử lý theo lifecycle trong database"
        );
    }

    private LoanApplicationEntity findApplication(String applicationCode) {
        return loanApplicationRepository.findByLoanApplicationCode(applicationCode)
                .orElseThrow(() -> new BusinessException(ErrorCode.LOAN_APPLICATION_NOT_FOUND, "Không tìm thấy hồ sơ vay trong database"));
    }

    private LoanApplicationStateEntity findState(String stateCode) {
        return stateRepository.findByCode(stateCode)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_LOAN_APPLICATION_STATE, "Không tìm thấy trạng thái hồ sơ trong database"));
    }

    private LoanApplicationStateEntity findTransitionToState(LoanApplicationStateEntity fromState, String actionCode) {
        return transitionRepository.findByFromStateAndActionCode(fromState, actionCode)
                .map(LoanApplicationStateTransitionEntity::getToState)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_LOAN_APPLICATION_STATE, "Lifecycle trong database không cho phép thao tác này"));
    }

    private Optional<LocalDateTime> findHistoryTime(LoanApplicationEntity application, String actionCode) {
        return historyRepository.findTopByLoanApplicationAndActionCodeOrderByChangedAtDesc(application, actionCode)
                .map(LoanApplicationStateHistoryEntity::getChangedAt);
    }

    private Optional<LocalDateTime> findLastHistoryTime(LoanApplicationEntity application) {
        return historyRepository.findTopByLoanApplicationOrderByChangedAtDesc(application)
                .map(LoanApplicationStateHistoryEntity::getChangedAt);
    }

    private void insertHistory(
            LoanApplicationEntity application,
            LoanApplicationStateEntity fromState,
            LoanApplicationStateEntity toState,
            String actionCode,
            String changedBy,
            String note
    ) {
        LoanApplicationStateHistoryEntity history = new LoanApplicationStateHistoryEntity();
        history.setLoanApplication(application);
        history.setFromState(fromState);
        history.setToState(toState);
        history.setActionCode(actionCode);
        history.setChangedAt(LocalDateTime.now());
        history.setChangedBy(changedBy);
        history.setNote(note);
        historyRepository.save(history);
    }

    private String nextApplicationCode() {
        String lastCode = loanApplicationRepository
                .findTopByLoanApplicationCodeStartingWithOrderByLoanApplicationCodeDesc(APPLICATION_CODE_PREFIX)
                .map(LoanApplicationEntity::getLoanApplicationCode)
                .orElse(null);
        int next = lastCode == null ? 1 : parseApplicationSequence(lastCode) + 1;
        return APPLICATION_CODE_PREFIX + "%06d".formatted(next);
    }

    private int parseApplicationSequence(String applicationCode) {
        int delimiter = applicationCode.lastIndexOf('-');
        if (delimiter < 0 || delimiter == applicationCode.length() - 1) {
            return 0;
        }
        try {
            return Integer.parseInt(applicationCode.substring(delimiter + 1));
        } catch (NumberFormatException ex) {
            return 0;
        }
    }

    private void ensureState(String actualState, String expectedState, String message) {
        if (!expectedState.equals(actualState)) {
            throw new BusinessException(ErrorCode.INVALID_LOAN_APPLICATION_STATE, message);
        }
    }
}
