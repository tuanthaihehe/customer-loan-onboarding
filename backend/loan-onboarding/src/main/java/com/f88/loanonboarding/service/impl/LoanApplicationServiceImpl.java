package com.f88.loanonboarding.service.impl;

import java.time.LocalDateTime;
import java.time.Year;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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
import com.f88.loanonboarding.entity.Asset;
import com.f88.loanonboarding.entity.Customer;
import com.f88.loanonboarding.entity.LoanApplication;
import com.f88.loanonboarding.entity.LoanApplicationState;
import com.f88.loanonboarding.entity.LoanApplicationStateHistory;
import com.f88.loanonboarding.exception.BusinessException;
import com.f88.loanonboarding.repository.CustomerRepository;
import com.f88.loanonboarding.repository.LoanApplicationRepository;
import com.f88.loanonboarding.repository.LoanApplicationStateHistoryRepository;
import com.f88.loanonboarding.repository.LoanApplicationStateRepository;
import com.f88.loanonboarding.repository.LoanApplicationStateTransitionRepository;
import com.f88.loanonboarding.rule.RuleContext;
import com.f88.loanonboarding.rule.RuleEvaluationService;
import com.f88.loanonboarding.rule.loan.LoanPurposeRule;
import com.f88.loanonboarding.rule.loan.LoanTenureRule;
import com.f88.loanonboarding.rule.loan.RequestedAmountRule;
import com.f88.loanonboarding.service.LoanApplicationService;

@Service
public class LoanApplicationServiceImpl implements LoanApplicationService {

    private static final String STATE_DRAFT = "APP_DRAFT";
    private static final String STATE_SUBMITTED = "APP_SUBMITTED";
    private static final String STATE_CANCELLED = "APP_CANCELLED";

    private final CustomerRepository customerRepository;
    private final LoanApplicationRepository loanApplicationRepository;
    private final LoanApplicationStateRepository stateRepository;
    private final LoanApplicationStateHistoryRepository historyRepository;
    private final LoanApplicationStateTransitionRepository transitionRepository;
    private final RuleEvaluationService ruleEvaluationService;

    public LoanApplicationServiceImpl(
            CustomerRepository customerRepository,
            LoanApplicationRepository loanApplicationRepository,
            LoanApplicationStateRepository stateRepository,
            LoanApplicationStateHistoryRepository historyRepository,
            LoanApplicationStateTransitionRepository transitionRepository,
            RuleEvaluationService ruleEvaluationService
    ) {
        this.customerRepository = customerRepository;
        this.loanApplicationRepository = loanApplicationRepository;
        this.stateRepository = stateRepository;
        this.historyRepository = historyRepository;
        this.transitionRepository = transitionRepository;
        this.ruleEvaluationService = ruleEvaluationService;
    }

    @Override
    @Transactional
    public LoanApplicationDraftResponse createDraft(CreateLoanApplicationRequest request) {
        Customer customer = customerRepository.findByCustomerCode(request.customerCode())
                .orElseThrow(() -> new BusinessException(ErrorCode.CUSTOMER_NOT_FOUND));
        LoanApplicationState draftState = findState(STATE_DRAFT);

        LoanApplication application = new LoanApplication();
        application.setLoanApplicationCode(nextApplicationCode());
        application.setCustomer(customer);
        application.setCurrentState(draftState);
        application.setBranch(request.branchCode());

        LoanApplication saved = loanApplicationRepository.save(application);
        historyRepository.save(history(saved, null, draftState, "CREATE", request.staffCode(), "Create draft"));

        return toDraftResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public LoanApplicationDetailResponse getDetail(String applicationCode) {
        LoanApplication application = findApplication(applicationCode);
        return new LoanApplicationDetailResponse(
                application.getLoanApplicationCode(),
                toStateEnum(application.getCurrentState()),
                application.getCustomer().getCustomerCode(),
                mapOf(
                        "fullName", application.getCustomer().getFullName(),
                        "dateOfBirth", application.getCustomer().getDateOfBirth(),
                        "identifierNumber", application.getCustomer().getIdentityNumber(),
                        "phoneNumber", application.getCustomer().getPhoneNumber()
                ),
                mapOf(
                        "requestedAmount", application.getRequestedAmount(),
                        "loanPurpose", application.getLoanPurpose(),
                        "requestedTenure", application.getLoanTermMonths()
                ),
                toAssetSnapshot(application.getAsset()),
                Map.of(),
                Map.of("source", "database"),
                latestChangedAt(application)
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

        LoanApplication application = findApplication(applicationCode);
        application.setRequestedAmount(request.loanRequest().requestedAmount());
        application.setLoanPurpose(request.loanRequest().loanPurpose());
        application.setLoanTermMonths(request.loanRequest().requestedTenure());

        return toDraftResponse(loanApplicationRepository.save(application));
    }

    @Override
    @Transactional
    public LoanApplicationDraftResponse cancel(String applicationCode, CancelLoanApplicationRequest request) {
        LoanApplication application = findApplication(applicationCode);
        LoanApplicationState cancelledState = findState(STATE_CANCELLED);
        validateTransition(application.getCurrentState(), cancelledState, "CANCEL");

        LoanApplicationState previousState = application.getCurrentState();
        application.setCurrentState(cancelledState);
        LoanApplication saved = loanApplicationRepository.save(application);
        historyRepository.save(history(saved, previousState, cancelledState, "CANCEL", null, request.note()));

        return toDraftResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public StepCompletionResponse completePreliminaryStep(String applicationCode) {
        LoanApplication application = findApplication(applicationCode);
        List<String> errors = hasCompleteLoanRequest(application)
                ? List.of()
                : List.of("Loan request data is incomplete");

        return new StepCompletionResponse(
                applicationCode,
                "PRELIMINARY",
                errors.isEmpty(),
                errors.isEmpty() ? "ASSET" : null,
                errors
        );
    }

    @Override
    @Transactional
    public SubmitForApprovalResponse submitForApproval(String applicationCode) {
        LoanApplication application = findApplication(applicationCode);
        LoanApplicationState submittedState = findState(STATE_SUBMITTED);
        validateTransition(application.getCurrentState(), submittedState, "SUBMIT");

        LoanApplicationState previousState = application.getCurrentState();
        application.setCurrentState(submittedState);
        LoanApplication saved = loanApplicationRepository.save(application);
        LoanApplicationStateHistory history = history(saved, previousState, submittedState, "SUBMIT", null, "Submit for approval");
        historyRepository.save(history);

        return new SubmitForApprovalResponse(
                saved.getLoanApplicationCode(),
                com.f88.loanonboarding.enums.LoanApplicationState.APP_SUBMITTED,
                "APR-" + saved.getLoanApplicationCode(),
                "LoanApplicationSubmittedForApproval",
                history.getChangedAt(),
                "Loan application submitted for approval"
        );
    }

    private LoanApplication findApplication(String applicationCode) {
        return loanApplicationRepository.findByLoanApplicationCode(applicationCode)
                .orElseThrow(() -> new BusinessException(ErrorCode.LOAN_APPLICATION_NOT_FOUND));
    }

    private LoanApplicationState findState(String stateCode) {
        return stateRepository.findByCode(stateCode)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.INVALID_LOAN_APPLICATION_STATE,
                        "Loan application state is not configured: " + stateCode
                ));
    }

    private void validateTransition(LoanApplicationState fromState, LoanApplicationState toState, String actionCode) {
        if (!transitionRepository.existsByFromStateAndToStateAndActionCode(fromState, toState, actionCode)) {
            throw new BusinessException(ErrorCode.INVALID_LOAN_APPLICATION_STATE);
        }
    }

    private LoanApplicationStateHistory history(
            LoanApplication application,
            LoanApplicationState fromState,
            LoanApplicationState toState,
            String actionCode,
            String changedBy,
            String note
    ) {
        LoanApplicationStateHistory history = new LoanApplicationStateHistory();
        history.setLoanApplication(application);
        history.setFromState(fromState);
        history.setToState(toState);
        history.setActionCode(actionCode);
        history.setChangedBy(changedBy);
        history.setNote(note);
        return history;
    }

    private LoanApplicationDraftResponse toDraftResponse(LoanApplication application) {
        return new LoanApplicationDraftResponse(
                application.getLoanApplicationCode(),
                toStateEnum(application.getCurrentState()),
                application.getCustomer().getCustomerCode(),
                firstChangedAt(application),
                latestChangedAt(application)
        );
    }

    private LocalDateTime firstChangedAt(LoanApplication application) {
        return historyRepository.findFirstByLoanApplicationOrderByChangedAtAsc(application)
                .map(LoanApplicationStateHistory::getChangedAt)
                .orElse(null);
    }

    private LocalDateTime latestChangedAt(LoanApplication application) {
        return historyRepository.findFirstByLoanApplicationOrderByChangedAtDesc(application)
                .map(LoanApplicationStateHistory::getChangedAt)
                .orElse(null);
    }

    private com.f88.loanonboarding.enums.LoanApplicationState toStateEnum(LoanApplicationState state) {
        return com.f88.loanonboarding.enums.LoanApplicationState.valueOf(state.getCode());
    }

    private boolean hasCompleteLoanRequest(LoanApplication application) {
        return application.getRequestedAmount() != null
                && application.getLoanPurpose() != null
                && application.getLoanTermMonths() != null;
    }

    private Map<String, Object> toAssetSnapshot(Asset asset) {
        if (asset == null) {
            return Map.of();
        }
        var variant = asset.getVehicleVariant();
        var vehicleYear = variant.getVehicleYear();
        var vehicleVersion = vehicleYear.getVehicleVersion();
        var vehicleModel = vehicleVersion.getVehicleModel();
        var vehicleBrand = vehicleModel.getVehicleBrand();
        var vehicleType = vehicleBrand.getVehicleType();

        return mapOf(
                "assetCode", asset.getAssetCode(),
                "assetType", vehicleType.getCode(),
                "licensePlate", asset.getLicensePlate(),
                "brand", vehicleBrand.getCode(),
                "model", vehicleModel.getCode(),
                "vehicleVariant", variant.getCode(),
                "manufactureYear", vehicleYear.getManufactureYear(),
                "vehicleColor", variant.getVehicleColor().getCode(),
                "assetState", asset.getStatus()
        );
    }

    private String nextApplicationCode() {
        String applicationCode;
        do {
            applicationCode = "APP-" + Year.now() + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        } while (loanApplicationRepository.existsByLoanApplicationCode(applicationCode));
        return applicationCode;
    }

    private static Map<String, Object> mapOf(Object... values) {
        Map<String, Object> map = new LinkedHashMap<>();
        for (int i = 0; i < values.length; i += 2) {
            map.put((String) values[i], values[i + 1]);
        }
        return map;
    }
}
