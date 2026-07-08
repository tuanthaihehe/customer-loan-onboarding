package com.f88.loanonboarding.service.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.f88.loanonboarding.common.error.ErrorCode;
import com.f88.loanonboarding.dto.request.loan.CancelLoanApplicationRequest;
import com.f88.loanonboarding.dto.request.loan.CreateLoanApplicationRequest;
import com.f88.loanonboarding.dto.request.loan.UpdateLoanApplicationRequest;
import com.f88.loanonboarding.dto.response.loan.LoanApplicationDetailResponse;
import com.f88.loanonboarding.dto.response.loan.LoanApplicationListItemResponse;
import com.f88.loanonboarding.dto.response.loan.LoanApplicationSummaryResponse;
import com.f88.loanonboarding.dto.response.loan.StepCompletionResponse;
import com.f88.loanonboarding.dto.response.loan.SubmitForApprovalResponse;
import com.f88.loanonboarding.entity.Asset;
import com.f88.loanonboarding.entity.Customer;
import com.f88.loanonboarding.entity.LoanApplication;
import com.f88.loanonboarding.entity.LoanPurpose;
import com.f88.loanonboarding.entity.LoanTerm;
import com.f88.loanonboarding.entity.LoanApplicationState;
import com.f88.loanonboarding.entity.LoanApplicationStateHistory;
import com.f88.loanonboarding.entity.LoanProduct;
import com.f88.loanonboarding.enums.AssetType;
import com.f88.loanonboarding.exception.BusinessException;
import com.f88.loanonboarding.repository.CustomerRepository;
import com.f88.loanonboarding.repository.LoanApplicationRepository;
import com.f88.loanonboarding.repository.LoanPurposeRepository;
import com.f88.loanonboarding.repository.LoanApplicationStateHistoryRepository;
import com.f88.loanonboarding.repository.LoanApplicationStateRepository;
import com.f88.loanonboarding.repository.LoanApplicationStateTransitionRepository;
import com.f88.loanonboarding.repository.LoanApplicationStepDataRepository;
import com.f88.loanonboarding.repository.LoanProductRepository;
import com.f88.loanonboarding.repository.LoanTermRepository;
import com.f88.loanonboarding.rule.RuleContext;
import com.f88.loanonboarding.rule.RuleEvaluationService;
import com.f88.loanonboarding.rule.loan.LoanPurposeRule;
import com.f88.loanonboarding.rule.loan.LoanTenureRule;
import com.f88.loanonboarding.rule.loan.RequestedAmountRule;
import com.f88.loanonboarding.service.LoanApplicationService;

@Service
public class LoanApplicationServiceImpl implements LoanApplicationService {

    private static final String STATE_CREATED = "APP_CREATED";
    private static final String STATE_SUBMITTED = "APP_SUBMITTED";
    private static final String STATE_CANCELLED = "APP_CANCELLED";
    private static final List<String> NON_DRAFT_STATES = List.of(
            "APP_SUBMITTED",
            "APP_IN_REVIEW",
            "APP_NEEDS_SUPPLEMENT",
            "APP_READY_FOR_CONTRACT",
            "APP_CONTRACTED",
            "APP_DISBURSED",
            "APP_CLOSED",
            "APP_CANCELLED",
            "APP_EXPIRED"
    );

    private final CustomerRepository customerRepository;
    private final LoanApplicationRepository loanApplicationRepository;
    private final LoanPurposeRepository loanPurposeRepository;
    private final LoanTermRepository loanTermRepository;
    private final LoanApplicationStateRepository stateRepository;
    private final LoanApplicationStateHistoryRepository historyRepository;
    private final LoanApplicationStateTransitionRepository transitionRepository;
    private final LoanApplicationStepDataRepository stepDataRepository;
    private final LoanProductRepository loanProductRepository;
    private final RuleEvaluationService ruleEvaluationService;
    private final ObjectMapper objectMapper;

    public LoanApplicationServiceImpl(
            CustomerRepository customerRepository,
            LoanApplicationRepository loanApplicationRepository,
            LoanPurposeRepository loanPurposeRepository,
            LoanTermRepository loanTermRepository,
            LoanApplicationStateRepository stateRepository,
            LoanApplicationStateHistoryRepository historyRepository,
            LoanApplicationStateTransitionRepository transitionRepository,
            LoanApplicationStepDataRepository stepDataRepository,
            LoanProductRepository loanProductRepository,
            RuleEvaluationService ruleEvaluationService,
            ObjectMapper objectMapper
    ) {
        this.customerRepository = customerRepository;
        this.loanApplicationRepository = loanApplicationRepository;
        this.loanPurposeRepository = loanPurposeRepository;
        this.loanTermRepository = loanTermRepository;
        this.stateRepository = stateRepository;
        this.historyRepository = historyRepository;
        this.transitionRepository = transitionRepository;
        this.stepDataRepository = stepDataRepository;
        this.loanProductRepository = loanProductRepository;
        this.ruleEvaluationService = ruleEvaluationService;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public LoanApplicationSummaryResponse createApplication(CreateLoanApplicationRequest request) {
        Customer customer = customerRepository.findByCustomerCode(request.customerCode())
                .orElseThrow(() -> new BusinessException(ErrorCode.CUSTOMER_NOT_FOUND));
        LoanApplicationState createdState = findState(STATE_CREATED);

        LoanApplication application = new LoanApplication();
        application.setLoanApplicationCode(nextApplicationCode());
        application.setCustomer(customer);
        application.setCurrentState(createdState);
        application.setBranch(request.branchCode());

        LoanApplication saved = loanApplicationRepository.save(application);
        historyRepository.save(history(saved, null, createdState, "CREATE", request.staffCode(), "Create application"));

        return toSummaryResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LoanApplicationListItemResponse> findLoanApplications() {
        return loanApplicationRepository.findByCurrentState_CodeInOrderByUpdatedAtDesc(NON_DRAFT_STATES)
                .stream()
                .map(this::toListItemResponse)
                .toList();
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
                        "loanPurpose", application.getLoanPurpose() == null ? null : application.getLoanPurpose().getCode(),
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
    public LoanApplicationSummaryResponse updateLoanRequest(String applicationCode, UpdateLoanApplicationRequest request) {
        ruleEvaluationService.validateOrThrow(
                RuleContext.loan(
                        request.loanRequest().requestedAmount(),
                        request.loanRequest().requestedTenure(),
                        request.loanRequest().loanPurpose()
                ),
                List.of(new RequestedAmountRule(), new LoanTenureRule(), new LoanPurposeRule())
        );

        LoanApplication application = findApplication(applicationCode);
        LoanPurpose loanPurpose = loanPurposeRepository.findByCode(request.loanRequest().loanPurpose())
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.RESOURCE_NOT_FOUND,
                        "Loan purpose is not configured: " + request.loanRequest().loanPurpose()
                ));
        LoanTerm loanTerm = loanTermRepository.findByTermMonths(request.loanRequest().requestedTenure())
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.RESOURCE_NOT_FOUND,
                        "Loan term is not configured: " + request.loanRequest().requestedTenure()
                ));
        application.setRequestedAmount(request.loanRequest().requestedAmount());
        application.setLoanPurpose(loanPurpose);
        application.setLoanTerm(loanTerm);
        application.setLoanTermMonths(request.loanRequest().requestedTenure());

        return toSummaryResponse(loanApplicationRepository.save(application));
    }

    @Override
    @Transactional
    public LoanApplicationSummaryResponse cancel(String applicationCode, CancelLoanApplicationRequest request) {
        LoanApplication application = findApplication(applicationCode);
        LoanApplicationState cancelledState = findState(STATE_CANCELLED);
        validateTransition(application.getCurrentState(), cancelledState, "CANCEL");

        LoanApplicationState previousState = application.getCurrentState();
        application.setCurrentState(cancelledState);
        LoanApplication saved = loanApplicationRepository.save(application);
        historyRepository.save(history(saved, previousState, cancelledState, "CANCEL", null, request.note()));

        return toSummaryResponse(saved);
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

    private LoanApplicationSummaryResponse toSummaryResponse(LoanApplication application) {
        return new LoanApplicationSummaryResponse(
                application.getLoanApplicationCode(),
                toStateEnum(application.getCurrentState()),
                application.getCustomer().getCustomerCode(),
                firstChangedAt(application),
                latestChangedAt(application)
        );
    }

    private LoanApplicationListItemResponse toListItemResponse(LoanApplication application) {
        var customer = application.getCustomer();
        var loanPurpose = application.getLoanPurpose();
        var loanProduct = application.getLoanProduct();
        var currentState = application.getCurrentState();
        var fallback = listFallback(application.getLoanApplicationCode());
        var requestedAmount = application.getRequestedAmount() != null
                ? application.getRequestedAmount()
                : fallback.requestedAmount();
        var loanTermMonths = application.getLoanTermMonths() != null
                ? application.getLoanTermMonths()
                : fallback.loanTermMonths();
        var loanPurposeCode = loanPurpose == null ? fallback.loanPurposeCode() : loanPurpose.getCode();
        var loanPurposeName = loanPurpose == null ? fallback.loanPurposeName() : loanPurpose.getName();
        var loanProductCode = loanProduct == null ? fallback.loanProductCode() : loanProduct.getProductCode();
        var loanProductName = loanProduct == null ? fallback.loanProductName() : loanProduct.getProductName();

        return new LoanApplicationListItemResponse(
                application.getLoanApplicationCode(),
                toStateEnum(currentState),
                stateDisplayName(currentState.getCode()),
                customer.getCustomerCode(),
                customer.getFullName(),
                customer.getPhoneNumber(),
                customer.getIdentityNumber(),
                requestedAmount,
                loanTermMonths,
                loanPurposeCode,
                loanPurposeName,
                loanProductCode,
                loanProductName,
                application.getBranch(),
                application.getCreatedAt(),
                application.getUpdatedAt()
        );
    }

    private ListFallback listFallback(String applicationCode) {
        JsonNode preliminary = payloadByStep(applicationCode, "PRELIMINARY_INFO");
        JsonNode proposal = payloadByStep(applicationCode, "CUSTOMER_ASSET_LOAN_PROPOSAL");

        BigDecimal requestedAmount = firstNumber(
                at(proposal, "selected_loan_offer", "final_requested_amount"),
                at(proposal, "selectedLoanOffer", "finalRequestedAmount"),
                at(preliminary, "preliminary_loan_package", "requested_amount"),
                at(preliminary, "preliminaryLoanPackage", "requestedAmount"),
                at(preliminary, "loanRequest", "requestedAmount"),
                at(preliminary, "requestedAmount")
        );
        Integer loanTermMonths = firstInteger(
                at(proposal, "selected_loan_offer", "final_loan_term_months"),
                at(proposal, "selectedLoanOffer", "finalLoanTermMonths"),
                at(preliminary, "preliminary_loan_package", "loan_term_months"),
                at(preliminary, "preliminaryLoanPackage", "loanTermMonths"),
                at(preliminary, "loanRequest", "termMonths"),
                at(preliminary, "loanRequest", "requestedTenure"),
                at(preliminary, "loanTermMonths")
        );
        String loanPurposeCode = firstText(
                at(proposal, "selected_loan_offer", "loan_purpose_code"),
                at(proposal, "selectedLoanOffer", "loanPurposeCode"),
                at(preliminary, "preliminary_loan_package", "loan_purpose_code"),
                at(preliminary, "preliminaryLoanPackage", "loanPurposeCode"),
                at(preliminary, "loanRequest", "loanPurposeCode"),
                at(preliminary, "loanRequest", "loanPurpose"),
                at(preliminary, "loanPurposeCode")
        );
        String loanPurposeName = loanPurposeCode == null
                ? null
                : loanPurposeRepository.findByCode(loanPurposeCode).map(LoanPurpose::getName).orElse(null);
        String loanProductCode = firstText(
                at(proposal, "selected_loan_offer", "loan_product_code"),
                at(proposal, "selectedLoanOffer", "loanProductCode"),
                at(proposal, "selectedLoanProduct", "productCode"),
                at(proposal, "selectedLoanProductCode"),
                at(proposal, "selectedProductCode"),
                at(preliminary, "selectedLoanProduct", "productCode"),
                at(preliminary, "selectedLoanProductCode"),
                at(preliminary, "selectedProductCode")
        );
        String loanProductName = firstText(
                at(proposal, "selected_loan_offer", "loan_product_name"),
                at(proposal, "selectedLoanOffer", "loanProductName"),
                at(proposal, "selectedLoanProduct", "productName"),
                at(preliminary, "selectedLoanProduct", "productName")
        );
        if (loanProductName == null && loanProductCode != null) {
            loanProductName = loanProductRepository.findByProductCode(loanProductCode)
                    .map(LoanProduct::getProductName)
                    .orElse(null);
        }

        return new ListFallback(
                requestedAmount,
                loanTermMonths,
                loanPurposeCode,
                loanPurposeName,
                loanProductCode,
                loanProductName
        );
    }

    private String stateDisplayName(String stateCode) {
        return switch (stateCode) {
            case "APP_CREATED" -> "Hồ sơ mới tạo";
            case "APP_IN_PROGRESS" -> "Đang hoàn thiện";
            case "APP_COMPLETED" -> "Đã hoàn thiện";
            case "APP_SUBMITTED" -> "Đã nộp hồ sơ";
            case "APP_IN_REVIEW" -> "Đang thẩm định/phê duyệt";
            case "APP_NEEDS_SUPPLEMENT" -> "Cần bổ sung hồ sơ";
            case "APP_READY_FOR_CONTRACT" -> "Sẵn sàng lập hợp đồng";
            case "APP_CONTRACTED" -> "Đã có hợp đồng";
            case "APP_DISBURSED" -> "Đã giải ngân";
            case "APP_CANCELLED" -> "Hồ sơ bị hủy";
            case "APP_EXPIRED" -> "Hồ sơ hết hạn";
            case "APP_CLOSED" -> "Hồ sơ đã đóng";
            default -> stateCode;
        };
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
                "assetType", AssetType.fromCode(vehicleType.getCode()),
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

    private JsonNode payloadByStep(String applicationCode, String stepCode) {
        return stepDataRepository.findByLoanApplication_LoanApplicationCodeAndStep_Code(applicationCode, stepCode)
                .map(step -> parseJson(step.getPayload()))
                .orElse(objectMapper.createObjectNode());
    }

    private JsonNode parseJson(String payload) {
        if (payload == null || payload.isBlank()) {
            return objectMapper.createObjectNode();
        }
        try {
            return objectMapper.readTree(payload);
        } catch (JsonProcessingException exception) {
            return objectMapper.createObjectNode();
        }
    }

    private JsonNode at(JsonNode node, String... path) {
        JsonNode current = node;
        for (String segment : path) {
            if (current == null || current.isMissingNode() || current.isNull()) {
                return objectMapper.missingNode();
            }
            current = current.path(segment);
        }
        return current == null ? objectMapper.missingNode() : current;
    }

    private String firstText(JsonNode... nodes) {
        for (JsonNode node : nodes) {
            if (node != null && !node.isMissingNode() && !node.isNull()) {
                String text = node.asText(null);
                if (text != null && !text.isBlank()) {
                    return text;
                }
            }
        }
        return null;
    }

    private Integer firstInteger(JsonNode... nodes) {
        for (JsonNode node : nodes) {
            if (node != null && !node.isMissingNode() && !node.isNull()) {
                if (node.isInt() || node.isLong()) {
                    return node.asInt();
                }
                String text = node.asText(null);
                if (text != null && !text.isBlank()) {
                    try {
                        return Integer.parseInt(text.replaceAll("[^0-9]", ""));
                    } catch (NumberFormatException ignored) {
                        // Try the next candidate.
                    }
                }
            }
        }
        return null;
    }

    private BigDecimal firstNumber(JsonNode... nodes) {
        for (JsonNode node : nodes) {
            if (node != null && !node.isMissingNode() && !node.isNull()) {
                if (node.isNumber()) {
                    return node.decimalValue();
                }
                String text = node.asText(null);
                if (text != null && !text.isBlank()) {
                    try {
                        return new BigDecimal(text.replaceAll("[^0-9.]", ""));
                    } catch (NumberFormatException ignored) {
                        // Try the next candidate.
                    }
                }
            }
        }
        return null;
    }

    private record ListFallback(
            BigDecimal requestedAmount,
            Integer loanTermMonths,
            String loanPurposeCode,
            String loanPurposeName,
            String loanProductCode,
            String loanProductName
    ) {
    }

    private static Map<String, Object> mapOf(Object... values) {
        Map<String, Object> map = new LinkedHashMap<>();
        for (int i = 0; i < values.length; i += 2) {
            map.put((String) values[i], values[i + 1]);
        }
        return map;
    }
}
