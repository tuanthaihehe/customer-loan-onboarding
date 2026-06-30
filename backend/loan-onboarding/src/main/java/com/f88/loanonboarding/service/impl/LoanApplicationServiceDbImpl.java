package com.f88.loanonboarding.service.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
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
import com.f88.loanonboarding.enums.LoanApplicationState;
import com.f88.loanonboarding.exception.BusinessException;
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

    private final JdbcTemplate jdbcTemplate;
    private final RuleEvaluationService ruleEvaluationService;

    public LoanApplicationServiceDbImpl(JdbcTemplate jdbcTemplate, RuleEvaluationService ruleEvaluationService) {
        this.jdbcTemplate = jdbcTemplate;
        this.ruleEvaluationService = ruleEvaluationService;
    }

    @Override
    @Transactional
    public LoanApplicationDraftResponse createDraft(CreateLoanApplicationRequest request) {
        UUID customerId = findCustomerId(request.customerCode());
        UUID draftStateId = findStateId(STATE_DRAFT);
        String applicationCode = nextApplicationCode();

        jdbcTemplate.update(
                """
                INSERT INTO loan_application
                    (loan_application_code, customer_id, current_state_id, branch)
                VALUES (?, ?, ?, ?)
                """,
                applicationCode,
                customerId,
                draftStateId,
                request.branchCode()
        );

        LoanApplicationRow application = findApplication(applicationCode);
        insertHistory(application.id(), null, draftStateId, "CREATE", request.staffCode(), "Tạo hồ sơ vay nháp");

        LocalDateTime createdAt = findHistoryTime(application.id(), "CREATE").orElse(LocalDateTime.now());
        return new LoanApplicationDraftResponse(
                application.applicationCode(),
                LoanApplicationState.APP_DRAFT,
                request.customerCode(),
                createdAt,
                createdAt
        );
    }

    @Override
    public LoanApplicationDetailResponse getDetail(String applicationCode) {
        LoanApplicationRow application = findApplication(applicationCode);
        LocalDateTime updatedAt = findLastHistoryTime(application.id()).orElse(null);

        Map<String, Object> applicantSnapshot = new LinkedHashMap<>();
        applicantSnapshot.put("customerCode", application.customerCode());
        applicantSnapshot.put("fullName", application.customerName());
        applicantSnapshot.put("dateOfBirth", application.customerDateOfBirth());
        applicantSnapshot.put("identityNumber", application.identityNumber());
        applicantSnapshot.put("phoneNumber", application.phoneNumber());

        Map<String, Object> loanRequest = new LinkedHashMap<>();
        loanRequest.put("requestedAmount", application.requestedAmount());
        loanRequest.put("loanPurpose", application.loanPurpose());
        loanRequest.put("requestedTenure", application.loanTermMonths());
        loanRequest.put("branchCode", application.branch());

        Map<String, Object> stepStatus = new LinkedHashMap<>();
        stepStatus.put("currentState", application.stateCode());
        stepStatus.put("latestActionAt", updatedAt);

        return new LoanApplicationDetailResponse(
                application.applicationCode(),
                LoanApplicationState.valueOf(application.stateCode()),
                application.customerCode(),
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

        LoanApplicationRow application = findApplication(applicationCode);
        ensureState(application.stateCode(), STATE_DRAFT, "Chỉ hồ sơ nháp mới được lưu thông tin khoản vay");

        UUID loanPurposeId = findLoanPurposeId(request.loanRequest().loanPurpose());
        UUID loanTermId = findLoanTermId(request.loanRequest().requestedTenure());

        jdbcTemplate.update(
                """
                UPDATE loan_application
                SET requested_amount = ?, loan_purpose_id = ?, loan_term_id = ?, loan_term_months = ?
                WHERE id = ?
                """,
                request.loanRequest().requestedAmount(),
                loanPurposeId,
                loanTermId,
                request.loanRequest().requestedTenure(),
                application.id()
        );

        insertHistory(application.id(), null, application.stateId(), "SAVE_DRAFT", "system", "Lưu thông tin nháp");
        LoanApplicationRow updated = findApplication(applicationCode);
        LocalDateTime createdAt = findHistoryTime(updated.id(), "CREATE").orElse(null);
        LocalDateTime savedAt = findHistoryTime(updated.id(), "SAVE_DRAFT").orElse(LocalDateTime.now());

        return new LoanApplicationDraftResponse(
                updated.applicationCode(),
                LoanApplicationState.valueOf(updated.stateCode()),
                updated.customerCode(),
                createdAt,
                savedAt
        );
    }

    @Override
    @Transactional
    public LoanApplicationDraftResponse cancel(String applicationCode, CancelLoanApplicationRequest request) {
        LoanApplicationRow application = findApplication(applicationCode);
        UUID cancelledStateId = findTransitionToState(application.stateId(), "CANCEL");

        jdbcTemplate.update(
                "UPDATE loan_application SET current_state_id = ? WHERE id = ?",
                cancelledStateId,
                application.id()
        );
        insertHistory(application.id(), application.stateId(), cancelledStateId, "CANCEL", "system", request.note());

        LoanApplicationRow updated = findApplication(applicationCode);
        return new LoanApplicationDraftResponse(
                updated.applicationCode(),
                LoanApplicationState.valueOf(updated.stateCode()),
                updated.customerCode(),
                findHistoryTime(updated.id(), "CREATE").orElse(null),
                findHistoryTime(updated.id(), "CANCEL").orElse(LocalDateTime.now())
        );
    }

    @Override
    public StepCompletionResponse completePreliminaryStep(String applicationCode) {
        LoanApplicationRow application = findApplication(applicationCode);
        List<String> errors = new java.util.ArrayList<>();
        if (application.requestedAmount() == null) {
            errors.add("Số tiền vay chưa được nhập");
        }
        if (application.loanPurpose() == null || application.loanPurpose().isBlank()) {
            errors.add("Mục đích vay chưa được nhập");
        }
        if (application.loanTermMonths() == null) {
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
        LoanApplicationRow application = findApplication(applicationCode);
        ensureState(application.stateCode(), STATE_DRAFT, "Chỉ hồ sơ nháp mới được gửi phê duyệt");

        if (application.requestedAmount() == null || application.loanPurpose() == null || application.loanTermMonths() == null) {
            throw new BusinessException(ErrorCode.INVALID_REQUESTED_AMOUNT, "Hồ sơ chưa đủ thông tin khoản vay để gửi phê duyệt");
        }

        UUID submittedStateId = findTransitionToState(application.stateId(), "SUBMIT");
        jdbcTemplate.update(
                "UPDATE loan_application SET current_state_id = ? WHERE id = ?",
                submittedStateId,
                application.id()
        );
        insertHistory(application.id(), application.stateId(), submittedStateId, "SUBMIT", "system", "Gửi hồ sơ vào luồng xử lý");

        LocalDateTime submittedAt = findHistoryTime(application.id(), "SUBMIT").orElse(LocalDateTime.now());
        return new SubmitForApprovalResponse(
                applicationCode,
                LoanApplicationState.APP_SUBMITTED,
                null,
                "LoanApplicationSubmitted",
                submittedAt,
                "Hồ sơ đã được gửi vào luồng xử lý theo lifecycle trong database"
        );
    }

    private UUID findCustomerId(String customerCode) {
        try {
            return jdbcTemplate.queryForObject(
                    "SELECT id FROM customer WHERE customer_code = ?",
                    UUID.class,
                    customerCode
            );
        } catch (EmptyResultDataAccessException ex) {
            throw new BusinessException(ErrorCode.CUSTOMER_NOT_FOUND, "Không tìm thấy khách hàng trong database");
        }
    }

    private UUID findStateId(String stateCode) {
        return jdbcTemplate.queryForObject(
                "SELECT id FROM loan_application_state WHERE code = ?",
                UUID.class,
                stateCode
        );
    }

    private UUID findTransitionToState(UUID fromStateId, String actionCode) {
        try {
            return jdbcTemplate.queryForObject(
                    """
                    SELECT to_state_id
                    FROM loan_application_state_transition
                    WHERE from_state_id = ? AND action_code = ?
                    """,
                    UUID.class,
                    fromStateId,
                    actionCode
            );
        } catch (EmptyResultDataAccessException ex) {
            throw new BusinessException(ErrorCode.INVALID_LOAN_APPLICATION_STATE, "Lifecycle trong database không cho phép thao tác này");
        }
    }

    private LoanApplicationRow findApplication(String applicationCode) {
        try {
            return jdbcTemplate.queryForObject(
                    """
                    SELECT la.id,
                           la.loan_application_code,
                           la.customer_id,
                           c.customer_code,
                           c.full_name,
                           c.phone_number,
                           c.identity_number,
                           c.date_of_birth,
                           la.current_state_id,
                           las.code AS state_code,
                           la.requested_amount,
                           lp.code AS loan_purpose,
                           la.loan_term_months,
                           la.branch
                    FROM loan_application la
                    JOIN customer c ON c.id = la.customer_id
                    JOIN loan_application_state las ON las.id = la.current_state_id
                    LEFT JOIN loan_purpose lp ON lp.id = la.loan_purpose_id
                    WHERE la.loan_application_code = ?
                    """,
                    this::mapApplication,
                    applicationCode
            );
        } catch (EmptyResultDataAccessException ex) {
            throw new BusinessException(ErrorCode.LOAN_APPLICATION_NOT_FOUND, "Không tìm thấy hồ sơ vay trong database");
        }
    }

    private LoanApplicationRow mapApplication(ResultSet rs, int rowNum) throws SQLException {
        return new LoanApplicationRow(
                rs.getObject("id", UUID.class),
                rs.getString("loan_application_code"),
                rs.getObject("customer_id", UUID.class),
                rs.getString("customer_code"),
                rs.getString("full_name"),
                rs.getString("phone_number"),
                rs.getString("identity_number"),
                rs.getDate("date_of_birth") == null ? null : rs.getDate("date_of_birth").toLocalDate(),
                rs.getObject("current_state_id", UUID.class),
                rs.getString("state_code"),
                rs.getBigDecimal("requested_amount"),
                rs.getString("loan_purpose"),
                (Integer) rs.getObject("loan_term_months"),
                rs.getString("branch")
        );
    }

    private Optional<LocalDateTime> findHistoryTime(UUID applicationId, String actionCode) {
        List<LocalDateTime> times = jdbcTemplate.query(
                """
                SELECT changed_at
                FROM loan_application_state_history
                WHERE loan_application_id = ? AND action_code = ?
                ORDER BY changed_at DESC
                LIMIT 1
                """,
                (rs, rowNum) -> toLocalDateTime(rs.getTimestamp("changed_at")),
                applicationId,
                actionCode
        );
        return times.stream().findFirst();
    }

    private Optional<LocalDateTime> findLastHistoryTime(UUID applicationId) {
        List<LocalDateTime> times = jdbcTemplate.query(
                """
                SELECT changed_at
                FROM loan_application_state_history
                WHERE loan_application_id = ?
                ORDER BY changed_at DESC
                LIMIT 1
                """,
                (rs, rowNum) -> toLocalDateTime(rs.getTimestamp("changed_at")),
                applicationId
        );
        return times.stream().findFirst();
    }

    private void insertHistory(UUID applicationId, UUID fromStateId, UUID toStateId, String actionCode, String changedBy, String note) {
        jdbcTemplate.update(
                """
                INSERT INTO loan_application_state_history
                    (loan_application_id, from_state_id, to_state_id, action_code, changed_by, note)
                VALUES (?, ?, ?, ?, ?, ?)
                """,
                applicationId,
                fromStateId,
                toStateId,
                actionCode,
                changedBy,
                note
        );
    }

    private String nextApplicationCode() {
        Integer next = jdbcTemplate.queryForObject(
                """
                SELECT COALESCE(MAX(CAST(substring(loan_application_code FROM 'APP-[0-9]{4}-([0-9]+)') AS INT)), 0) + 1
                FROM loan_application
                WHERE loan_application_code LIKE ?
                """,
                Integer.class,
                "APP-2026-%"
        );
        return "APP-2026-%06d".formatted(next == null ? 1 : next);
    }

    private void ensureState(String actualState, String expectedState, String message) {
        if (!expectedState.equals(actualState)) {
            throw new BusinessException(ErrorCode.INVALID_LOAN_APPLICATION_STATE, message);
        }
    }

    private UUID findLoanPurposeId(String loanPurposeCode) {
        try {
            return jdbcTemplate.queryForObject(
                    """
                    SELECT id
                    FROM loan_purpose
                    WHERE code = ? AND is_active = true
                    """,
                    UUID.class,
                    loanPurposeCode
            );
        } catch (EmptyResultDataAccessException ex) {
            throw new BusinessException(ErrorCode.INVALID_LOAN_PURPOSE, "Mục đích vay không tồn tại hoặc đã ngừng áp dụng trong database");
        }
    }

    private UUID findLoanTermId(Integer loanTermMonths) {
        try {
            return jdbcTemplate.queryForObject(
                    """
                    SELECT id
                    FROM loan_term
                    WHERE term_months = ? AND is_active = true
                    """,
                    UUID.class,
                    loanTermMonths
            );
        } catch (EmptyResultDataAccessException ex) {
            throw new BusinessException(ErrorCode.INVALID_LOAN_TERM, "Kỳ hạn vay không tồn tại hoặc đã ngừng áp dụng trong database");
        }
    }

    private LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }

    private record LoanApplicationRow(
            UUID id,
            String applicationCode,
            UUID customerId,
            String customerCode,
            String customerName,
            String phoneNumber,
            String identityNumber,
            java.time.LocalDate customerDateOfBirth,
            UUID stateId,
            String stateCode,
            java.math.BigDecimal requestedAmount,
            String loanPurpose,
            Integer loanTermMonths,
            String branch
    ) {
    }
}
