package com.f88.loanonboarding.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.f88.loanonboarding.common.error.ErrorCode;
import com.f88.loanonboarding.dto.request.loan.ApplicantSnapshotRequest;
import com.f88.loanonboarding.dto.request.loan.CancelLoanApplicationRequest;
import com.f88.loanonboarding.dto.request.loan.CompleteLoanApplicationDocumentUploadRequest;
import com.f88.loanonboarding.dto.request.loan.CreateLoanApplicationRequest;
import com.f88.loanonboarding.dto.request.loan.ReferencePersonRequest;
import com.f88.loanonboarding.dto.request.loan.SaveCustomerDetailRequest;
import com.f88.loanonboarding.dto.request.loan.SaveLoanApplicationDraftRequest;
import com.f88.loanonboarding.dto.request.loan.SaveReferencePersonsRequest;
import com.f88.loanonboarding.dto.response.loan.CompleteLoanApplicationDocumentUploadResponse;
import com.f88.loanonboarding.dto.response.loan.CustomerDetailResponse;
import com.f88.loanonboarding.dto.response.loan.DeleteLoanApplicationDocumentUploadResponse;
import com.f88.loanonboarding.dto.response.loan.LoanApplicationDetailResponse;
import com.f88.loanonboarding.dto.response.loan.LoanApplicationDocumentUploadResponse;
import com.f88.loanonboarding.dto.response.loan.LoanApplicationDraftResponse;
import com.f88.loanonboarding.dto.response.loan.ReferencePersonResponse;
import com.f88.loanonboarding.dto.response.loan.ReferencePersonsResponse;
import com.f88.loanonboarding.dto.response.loan.StepCompletionResponse;
import com.f88.loanonboarding.dto.response.loan.SubmitForApprovalResponse;
import com.f88.loanonboarding.entity.Asset;
import com.f88.loanonboarding.entity.Bank;
import com.f88.loanonboarding.entity.Customer;
import com.f88.loanonboarding.entity.DocumentType;
import com.f88.loanonboarding.entity.IncomeSource;
import com.f88.loanonboarding.entity.LoanApplication;
import com.f88.loanonboarding.entity.LoanApplicationDocument;
import com.f88.loanonboarding.entity.LoanApplicationReferencePerson;
import com.f88.loanonboarding.entity.LoanApplicationStateTransition;
import com.f88.loanonboarding.entity.LoanApplicationState;
import com.f88.loanonboarding.entity.LoanApplicationStateHistory;
import com.f88.loanonboarding.entity.LoanPurpose;
import com.f88.loanonboarding.entity.LoanTerm;
import com.f88.loanonboarding.entity.Occupation;
import com.f88.loanonboarding.enums.AssetType;
import com.f88.loanonboarding.enums.Gender;
import com.f88.loanonboarding.enums.MaritalStatus;
import com.f88.loanonboarding.enums.ReferenceRelationshipType;
import com.f88.loanonboarding.exception.BusinessException;
import com.f88.loanonboarding.repository.BankRepository;
import com.f88.loanonboarding.repository.CustomerRepository;
import com.f88.loanonboarding.repository.DocumentTypeRepository;
import com.f88.loanonboarding.repository.IncomeSourceRepository;
import com.f88.loanonboarding.repository.AssetValuationRepository;
import com.f88.loanonboarding.repository.LoanApplicationDocumentRepository;
import com.f88.loanonboarding.repository.LoanApplicationReferencePersonRepository;
import com.f88.loanonboarding.repository.LoanApplicationRepository;
import com.f88.loanonboarding.repository.LoanPurposeRepository;
import com.f88.loanonboarding.repository.LoanApplicationStateHistoryRepository;
import com.f88.loanonboarding.repository.LoanApplicationStateRepository;
import com.f88.loanonboarding.repository.LoanApplicationStateTransitionRepository;
import com.f88.loanonboarding.repository.LoanTermRepository;
import com.f88.loanonboarding.repository.OccupationRepository;
import com.f88.loanonboarding.rule.RuleContext;
import com.f88.loanonboarding.rule.RuleEvaluationService;
import com.f88.loanonboarding.rule.loan.LoanPurposeRule;
import com.f88.loanonboarding.rule.loan.LoanTenureRule;
import com.f88.loanonboarding.rule.loan.RequestedAmountRule;
import com.f88.loanonboarding.service.DocumentStorageService;
import com.f88.loanonboarding.service.LoanApplicationService;

@Service
public class LoanApplicationServiceImpl implements LoanApplicationService {

    private static final String STATE_DRAFT = "APP_DRAFT";
    private static final String STATE_SUBMITTED = "APP_SUBMITTED";
    private static final String STATE_CANCELLED = "APP_CANCELLED";
    private static final String APPLICATION_CODE_PREFIX = "APP-2026-";
    private static final long MAX_DOCUMENT_FILE_SIZE_MB = 5L;
    private static final long MAX_DOCUMENT_FILE_SIZE_BYTES = MAX_DOCUMENT_FILE_SIZE_MB * 1024L * 1024L;
    private static final Map<String, String> DOCUMENT_TYPE_CODE_BY_DOCUMENT_CODE = Map.ofEntries(
            Map.entry("CITIZEN_ID_FRONT", "CITIZEN_ID_FRONT"),
            Map.entry("CITIZEN_ID_BACK", "CITIZEN_ID_BACK"),
            Map.entry("VEHICLE_REGISTRATION_FRONT", "VEHICLE_REGISTRATION_FRONT"),
            Map.entry("VEHICLE_REGISTRATION_BACK", "VEHICLE_REGISTRATION_BACK"),
            Map.entry("ASSET_FRONT", "ASSET_FRONT_IMAGE"),
            Map.entry("ASSET_REAR", "ASSET_BACK_IMAGE"),
            Map.entry("ASSET_LEFT", "ASSET_LEFT_IMAGE"),
            Map.entry("ASSET_RIGHT", "ASSET_RIGHT_IMAGE"),
            Map.entry("ASSET_FRAME_NUMBER", "ASSET_FRAME_NUMBER_IMAGE"),
            Map.entry("ASSET_ENGINE_NUMBER", "ASSET_ENGINE_NUMBER_IMAGE"),
            Map.entry("ASSET_ODO", "ASSET_ODOMETER_IMAGE"),
            Map.entry("CUSTOMER_PORTRAIT", "BORROWER_PORTRAIT_IMAGE"),
            Map.entry("CUSTOMER_HOLDING_ID", "BORROWER_HOLDING_CITIZEN_ID_IMAGE"),
            Map.entry("CUSTOMER_PORTRAIT_VIDEO", "BORROWER_PORTRAIT_VIDEO"),
            Map.entry("INCOME_PROOF", "INCOME_PROOF"),
            Map.entry("RESIDENCE_PROOF", "RESIDENCE_PROOF_DOCUMENT"),
            Map.entry("SIGNED_CUSTOMER_CONTRACT", "SIGNED_CUSTOMER_CONTRACT"),
            Map.entry("REFERENCE_VERIFICATION_FORM", "REFERENCE_VERIFICATION_FORM")
    );
    private static final List<String> IMAGE_OR_PDF_EXTENSIONS = List.of("jpg", "jpeg", "png", "webp", "pdf");
    private static final List<String> VIDEO_EXTENSIONS = List.of("mp4", "webm", "mov");
    private static final List<String> SUPPORTED_GENDERS = List.of("MALE", "FEMALE");
    private static final List<String> SUPPORTED_MARITAL_STATUSES = List.of("SINGLE", "MARRIED");
    private static final List<String> SUPPORTED_RELATIONSHIP_TYPES = List.of(
            "FATHER",
            "MOTHER",
            "SPOUSE",
            "SIBLING",
            "RELATIVE",
            "FRIEND",
            "COLLEAGUE",
            "OTHER"
    );

    private final CustomerRepository customerRepository;
    private final LoanApplicationRepository loanApplicationRepository;
    private final LoanPurposeRepository loanPurposeRepository;
    private final LoanTermRepository loanTermRepository;
    private final OccupationRepository occupationRepository;
    private final BankRepository bankRepository;
    private final IncomeSourceRepository incomeSourceRepository;
    private final AssetValuationRepository assetValuationRepository;
    private final DocumentTypeRepository documentTypeRepository;
    private final LoanApplicationDocumentRepository loanApplicationDocumentRepository;
    private final LoanApplicationReferencePersonRepository referencePersonRepository;
    private final LoanApplicationStateRepository stateRepository;
    private final LoanApplicationStateHistoryRepository historyRepository;
    private final LoanApplicationStateTransitionRepository transitionRepository;
    private final RuleEvaluationService ruleEvaluationService;
    private final DocumentStorageService documentStorageService;

    public LoanApplicationServiceImpl(
            CustomerRepository customerRepository,
            LoanApplicationRepository loanApplicationRepository,
            LoanPurposeRepository loanPurposeRepository,
            LoanTermRepository loanTermRepository,
            OccupationRepository occupationRepository,
            BankRepository bankRepository,
            IncomeSourceRepository incomeSourceRepository,
            AssetValuationRepository assetValuationRepository,
            DocumentTypeRepository documentTypeRepository,
            LoanApplicationDocumentRepository loanApplicationDocumentRepository,
            LoanApplicationReferencePersonRepository referencePersonRepository,
            LoanApplicationStateRepository stateRepository,
            LoanApplicationStateHistoryRepository historyRepository,
            LoanApplicationStateTransitionRepository transitionRepository,
            RuleEvaluationService ruleEvaluationService,
            DocumentStorageService documentStorageService
    ) {
        this.customerRepository = customerRepository;
        this.loanApplicationRepository = loanApplicationRepository;
        this.loanPurposeRepository = loanPurposeRepository;
        this.loanTermRepository = loanTermRepository;
        this.occupationRepository = occupationRepository;
        this.bankRepository = bankRepository;
        this.incomeSourceRepository = incomeSourceRepository;
        this.assetValuationRepository = assetValuationRepository;
        this.documentTypeRepository = documentTypeRepository;
        this.loanApplicationDocumentRepository = loanApplicationDocumentRepository;
        this.referencePersonRepository = referencePersonRepository;
        this.stateRepository = stateRepository;
        this.historyRepository = historyRepository;
        this.transitionRepository = transitionRepository;
        this.ruleEvaluationService = ruleEvaluationService;
        this.documentStorageService = documentStorageService;
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
                toApplicantSnapshot(application),
                mapOf(
                        "requestedAmount", application.getRequestedAmount(),
                        "loanPurposeId", application.getLoanPurpose() == null ? null : application.getLoanPurpose().getId(),
                        "loanPurpose", application.getLoanPurpose() == null ? null : application.getLoanPurpose().getCode(),
                        "loanPurposeName", application.getLoanPurpose() == null ? null : application.getLoanPurpose().getName(),
                        "loanTermId", application.getLoanTerm() == null ? null : application.getLoanTerm().getId(),
                        "loanTermMonths", application.getLoanTermMonths(),
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
        ensureState(application.getCurrentState().getCode(), STATE_DRAFT, "Chỉ hồ sơ nháp mới được lưu thông tin khoản vay");
        LoanPurpose loanPurpose = loanPurposeRepository.findByCodeAndActiveTrue(request.loanRequest().loanPurpose())
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_LOAN_PURPOSE, "Mục đích vay không tồn tại hoặc đã ngừng áp dụng trong database"));
        LoanTerm loanTerm = loanTermRepository.findByTermMonthsAndActiveTrue(request.loanRequest().requestedTenure())
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_LOAN_TERM, "Kỳ hạn vay không tồn tại hoặc đã ngừng áp dụng trong database"));

        saveApplicantSnapshot(application, request.applicantSnapshot());
        application.setRequestedAmount(request.loanRequest().requestedAmount());
        application.setLoanPurpose(loanPurpose);
        application.setLoanTerm(loanTerm);
        application.setLoanTermMonths(request.loanRequest().requestedTenure());

        LoanApplication saved = loanApplicationRepository.save(application);
        historyRepository.save(history(saved, null, saved.getCurrentState(), "SAVE_DRAFT", "system", "Lưu thông tin nháp"));
        return toDraftResponse(saved);
    }

    @Override
    @Transactional
    public CustomerDetailResponse saveCustomerDetail(String applicationCode, SaveCustomerDetailRequest request) {
        LoanApplication application = findApplication(applicationCode);
        ensureState(application.getCurrentState().getCode(), STATE_DRAFT, "Chỉ hồ sơ nháp mới được lưu thông tin chi tiết khách hàng");

        Customer customer = application.getCustomer();
        customer.setGender(validateGender(request.gender()));
        customer.setEmail(normalizeNullableText(request.email()));
        customer.setMaritalStatus(validateMaritalStatus(request.maritalStatus()));
        customer.setPermanentAddress(normalizeText(request.permanentAddress()));

        Occupation occupation = occupationRepository.findByCode(normalizeCode(request.occupationCode()))
                .filter(Occupation::isActive)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Không tìm thấy nghề nghiệp đang hoạt động: " + request.occupationCode()));
        IncomeSource incomeSource = incomeSourceRepository.findByCode(normalizeCode(request.incomeSourceCode()))
                .filter(IncomeSource::isActive)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Không tìm thấy nguồn thu nhập đang hoạt động: " + request.incomeSourceCode()));
        Bank bank = bankRepository.findByCode(normalizeCode(request.disbursementBankCode()))
                .filter(Bank::isActive)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Không tìm thấy ngân hàng đang hoạt động: " + request.disbursementBankCode()));

        application.setOccupation(occupation);
        application.setIncomeSource(incomeSource);
        application.setMonthlyIncomeAmount(request.monthlyIncomeAmount());
        application.setDisbursementBank(bank);
        application.setDisbursementAccountNumber(normalizeText(request.disbursementAccountNumber()));
        application.setDisbursementAccountName(normalizeText(request.disbursementAccountName()));
        application.setWorkplaceName(normalizeNullableText(request.workplaceName()));
        application.setWorkplaceAddress(normalizeNullableText(request.workplaceAddress()));
        application.setCurrentAddress(normalizeText(request.currentAddress()));

        customerRepository.save(customer);
        LoanApplication saved = loanApplicationRepository.save(application);
        historyRepository.save(history(saved, null, saved.getCurrentState(), "SAVE_CUSTOMER_DETAIL", "system", "Lưu thông tin chi tiết khách hàng"));
        return toCustomerDetailResponse(saved);
    }

    @Override
    @Transactional
    public ReferencePersonsResponse saveReferencePersons(String applicationCode, SaveReferencePersonsRequest request) {
        LoanApplication application = findApplication(applicationCode);
        ensureState(application.getCurrentState().getCode(), STATE_DRAFT, "Chỉ hồ sơ nháp mới được lưu người tham chiếu");

        validateReferencePhones(request.referencePersons());
        referencePersonRepository.deleteByLoanApplicationId(application.getId());

        List<LoanApplicationReferencePerson> savedPersons = new ArrayList<>();
        for (ReferencePersonRequest item : request.referencePersons()) {
            LoanApplicationReferencePerson referencePerson = new LoanApplicationReferencePerson();
            referencePerson.setLoanApplication(application);
            referencePerson.setFullName(normalizeText(item.fullName()));
            referencePerson.setPhoneNumber(normalizeText(item.phoneNumber()));
            referencePerson.setRelationshipType(validateRelationshipType(item.relationshipType()));
            referencePerson.setAddress(normalizeNullableText(item.address()));
            referencePerson.setNote(normalizeNullableText(item.note()));
            LocalDateTime now = LocalDateTime.now();
            referencePerson.setCreatedAt(now);
            referencePerson.setUpdatedAt(now);
            savedPersons.add(referencePersonRepository.save(referencePerson));
        }

        historyRepository.save(history(application, null, application.getCurrentState(), "SAVE_REFERENCE_PERSONS", "system", "Lưu người tham chiếu"));
        return toReferencePersonsResponse(application.getLoanApplicationCode(), savedPersons);
    }

    @Override
    @Transactional
    public List<LoanApplicationDocumentUploadResponse> uploadDocuments(
            String applicationCode,
            String documentCode,
            List<MultipartFile> files
    ) {
        LoanApplication application = findApplication(applicationCode);
        ensureState(application.getCurrentState().getCode(), STATE_DRAFT, "Chỉ hồ sơ nháp mới được upload chứng từ");
        String normalizedDocumentCode = normalizeDocumentCode(documentCode);
        if (files == null || files.isEmpty()) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "Danh sách file chứng từ không được để trống");
        }
        return files.stream()
                .map(file -> uploadSingleDocument(application, normalizedDocumentCode, file))
                .toList();
    }

    private LoanApplicationDocumentUploadResponse uploadSingleDocument(
            LoanApplication application,
            String normalizedDocumentCode,
            MultipartFile file
    ) {
        validateDocumentFile(file, normalizedDocumentCode);

        String originalFileName = StringUtils.cleanPath(file.getOriginalFilename() == null ? "upload" : file.getOriginalFilename());
        String extension = extension(originalFileName);
        String fileId = UUID.randomUUID().toString();
        DocumentStorageService.StoredObject storedObject = documentStorageService.store(
                "loan-applications",
                application.getLoanApplicationCode(),
                normalizedDocumentCode,
                fileId,
                extension,
                file.getContentType(),
                file
        );

        LocalDateTime uploadedAt = LocalDateTime.now();
        return new LoanApplicationDocumentUploadResponse(
                application.getLoanApplicationCode(),
                normalizedDocumentCode,
                resolveDocumentTypeCode(normalizedDocumentCode),
                fileId,
                originalFileName,
                file.getContentType(),
                file.getSize(),
                storedObject.fileUrl(),
                uploadedAt
        );
    }

    @Override
    @Transactional
    public DeleteLoanApplicationDocumentUploadResponse deleteUploadedDocument(String applicationCode, String fileUrl) {
        LoanApplication application = findApplication(applicationCode);
        ensureState(application.getCurrentState().getCode(), STATE_DRAFT, "Chỉ hồ sơ nháp mới được xóa chứng từ");
        if (fileUrl == null || fileUrl.isBlank()) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "fileUrl là bắt buộc khi xóa chứng từ");
        }

        documentStorageService.delete(fileUrl);
        List<LoanApplicationDocument> savedDocuments =
                loanApplicationDocumentRepository.findByLoanApplication_IdAndFileUrl(application.getId(), fileUrl);
        loanApplicationDocumentRepository.deleteAll(savedDocuments);

        return new DeleteLoanApplicationDocumentUploadResponse(
                application.getLoanApplicationCode(),
                fileUrl,
                true,
                "Đã xóa ảnh chứng từ"
        );
    }

    @Override
    @Transactional
    public CompleteLoanApplicationDocumentUploadResponse completeDocumentUpload(
            String applicationCode,
            CompleteLoanApplicationDocumentUploadRequest request
    ) {
        LoanApplication application = findApplication(applicationCode);
        ensureState(application.getCurrentState().getCode(), STATE_DRAFT, "Chỉ hồ sơ nháp mới được hoàn tất upload chứng từ");
        validateCompleteDocumentUploadRequest(request);

        loanApplicationDocumentRepository.deleteByLoanApplication_Id(application.getId());
        for (CompleteLoanApplicationDocumentUploadRequest.Document item : request.documents()) {
            String documentTypeCode = resolveDocumentTypeCode(item);
            DocumentType documentType = documentTypeRepository.findByCode(documentTypeCode)
                    .orElseThrow(() -> new BusinessException(ErrorCode.SCHEMA_NOT_READY, "Document type is not configured: " + documentTypeCode));

            LoanApplicationDocument document = new LoanApplicationDocument();
            document.setLoanApplication(application);
            document.setDocumentType(documentType);
            document.setFileUrl(item.fileUrl().trim());
            document.setFileName(normalizeNullableText(item.fileName()));
            document.setUploadedAt(item.uploadedAt() == null ? LocalDateTime.now() : item.uploadedAt());
            document.setUploadedBy(normalizeNullableText(item.uploadedBy()) == null ? "SYSTEM" : item.uploadedBy().trim());
            document.setNote(normalizeNullableText(item.note()));
            loanApplicationDocumentRepository.save(document);
        }

        return new CompleteLoanApplicationDocumentUploadResponse(
                application.getLoanApplicationCode(),
                request.documents().size(),
                "Đã lưu metadata chứng từ vào hồ sơ vay"
        );
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
        historyRepository.save(history(saved, previousState, cancelledState, "CANCEL", null, cancellationNote(request)));

        return toDraftResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public StepCompletionResponse completePreliminaryStep(String applicationCode) {
        LoanApplication application = findApplication(applicationCode);
        List<String> errors = hasCompletePreliminaryInfo(application)
                ? List.of()
                : List.of("Thông tin sơ bộ khách hàng hoặc nhu cầu vay chưa đầy đủ");

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
        List<String> validationErrors = validateReadyForSubmission(application);
        if (!validationErrors.isEmpty()) {
            throw new BusinessException(
                    ErrorCode.BUSINESS_RULE_VIOLATION,
                    "Hồ sơ chưa đủ điều kiện gửi phê duyệt: " + String.join("; ", validationErrors)
            );
        }
        ensureState(application.getCurrentState().getCode(), STATE_DRAFT, "Chỉ hồ sơ nháp mới được gửi phê duyệt");
        if (!hasCompletePreliminaryInfo(application)) {
            throw new BusinessException(ErrorCode.INVALID_REQUESTED_AMOUNT, "Hồ sơ chưa đủ thông tin sơ bộ khách hàng và nhu cầu vay để gửi phê duyệt");
        }
        LoanApplicationState submittedState = findState(STATE_SUBMITTED);
        validateTransition(application.getCurrentState(), submittedState, "SUBMIT");

        LoanApplicationState previousState = application.getCurrentState();
        application.setCurrentState(submittedState);
        LoanApplication saved = loanApplicationRepository.save(application);
        LoanApplicationStateHistory history = history(saved, previousState, submittedState, "SUBMIT", null, "Submit for approval");
        history.setChangedAt(LocalDateTime.now());
        historyRepository.save(history);

        return new SubmitForApprovalResponse(
                saved.getLoanApplicationCode(),
                com.f88.loanonboarding.enums.LoanApplicationState.APP_SUBMITTED,
                "APR-" + saved.getLoanApplicationCode(),
                "LoanApplicationSubmittedForApproval",
                history.getChangedAt(),
                "Hồ sơ vay đã được gửi phê duyệt"
        );
    }

    private List<String> validateReadyForSubmission(LoanApplication application) {
        List<String> errors = new ArrayList<>();
        if (!hasCompletePreliminaryInfo(application)) {
            errors.add("thiếu thông tin sơ bộ khách hàng hoặc nhu cầu vay");
        }
        if (!hasCompleteCustomerDetail(application)) {
            errors.add("thiếu thông tin chi tiết khách hàng");
        }
        if (referencePersonRepository.findByLoanApplicationId(application.getId()).size() < 3) {
            errors.add("thiếu tối thiểu 3 người tham chiếu");
        }
        Asset asset = application.getAsset();
        if (asset == null) {
            errors.add("thiếu thông tin tài sản");
        } else {
            if (isBlank(asset.getFrameNumber()) || isBlank(asset.getEngineNumber())) {
                errors.add("thiếu thông tin pháp lý xe");
            }
            if (isBlank(asset.getRegistrationNumber()) || asset.getRegistrationIssueDate() == null) {
                errors.add("thiếu thông tin giấy tờ xe");
            }
            if (assetValuationRepository.findTopByAssetOrderByValuedAtDesc(asset).isEmpty()) {
                errors.add("chưa lưu định giá tài sản");
            }
        }
        if (application.getLoanProduct() == null) {
            errors.add("chưa chọn gói vay cuối cùng");
        }
        return errors;
    }

    private boolean hasCompleteCustomerDetail(LoanApplication application) {
        Customer customer = application.getCustomer();
        return customer.getGender() != null
                && customer.getMaritalStatus() != null
                && isNotBlank(customer.getPermanentAddress())
                && application.getOccupation() != null
                && application.getIncomeSource() != null
                && application.getMonthlyIncomeAmount() != null
                && application.getDisbursementBank() != null
                && isNotBlank(application.getDisbursementAccountNumber())
                && isNotBlank(application.getDisbursementAccountName())
                && isNotBlank(application.getCurrentAddress());
    }

    private String cancellationNote(CancelLoanApplicationRequest request) {
        String reason = normalizeText(request.cancellationReasonCode());
        String note = normalizeNullableText(request.note());
        return note == null ? "Reason: " + reason : "Reason: " + reason + " - " + note;
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

    private LoanApplicationState findTransitionToState(LoanApplicationState fromState, String actionCode) {
        return transitionRepository.findByFromStateAndActionCode(fromState, actionCode)
                .map(LoanApplicationStateTransition::getToState)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_LOAN_APPLICATION_STATE, "Lifecycle trong database không cho phép thao tác này"));
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

    private boolean hasCompletePreliminaryInfo(LoanApplication application) {
        Customer customer = application.getCustomer();
        return hasCompleteLoanRequest(application)
                && isNotBlank(customer.getFullName())
                && isNotBlank(customer.getIdentityNumber())
                && isNotBlank(customer.getPhoneNumber())
                && customer.getDateOfBirth() != null
                && customer.getGender() != null
                && application.getOccupation() != null
                && application.getMonthlyIncomeAmount() != null;
    }

    private void saveApplicantSnapshot(LoanApplication application, ApplicantSnapshotRequest request) {
        Customer customer = application.getCustomer();
        customer.setGender(validateGender(request.gender()));
        application.setOccupation(occupationRepository.findByCode(normalizeCode(request.occupation()))
                .filter(Occupation::isActive)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Không tìm thấy nghề nghiệp đang hoạt động: " + request.occupation())));
        application.setMonthlyIncomeAmount(request.monthlyIncome());
        customerRepository.save(customer);
    }

    private Map<String, Object> toApplicantSnapshot(LoanApplication application) {
        Customer customer = application.getCustomer();
        return mapOf(
                "fullName", customer.getFullName(),
                "dateOfBirth", customer.getDateOfBirth(),
                "identifierNumber", customer.getIdentityNumber(),
                "phoneNumber", customer.getPhoneNumber(),
                "gender", customer.getGender() == null ? null : customer.getGender().name(),
                "occupation", application.getOccupation() == null ? null : application.getOccupation().getCode(),
                "occupationName", application.getOccupation() == null ? null : application.getOccupation().getName(),
                "monthlyIncome", application.getMonthlyIncomeAmount()
        );
    }

    private CustomerDetailResponse toCustomerDetailResponse(LoanApplication application) {
        Customer customer = application.getCustomer();
        Occupation occupation = application.getOccupation();
        IncomeSource incomeSource = application.getIncomeSource();
        Bank bank = application.getDisbursementBank();
        return new CustomerDetailResponse(
                application.getLoanApplicationCode(),
                customer.getCustomerCode(),
                customer.getFullName(),
                customer.getIdentityNumber(),
                customer.getPhoneNumber(),
                customer.getDateOfBirth(),
                customer.getGender() == null ? null : customer.getGender().name(),
                customer.getEmail(),
                customer.getMaritalStatus() == null ? null : customer.getMaritalStatus().name(),
                occupation == null ? null : occupation.getCode(),
                occupation == null ? null : occupation.getName(),
                incomeSource == null ? null : incomeSource.getCode(),
                incomeSource == null ? null : incomeSource.getName(),
                application.getMonthlyIncomeAmount(),
                bank == null ? null : bank.getCode(),
                bank == null ? null : bank.getName(),
                application.getDisbursementAccountNumber(),
                application.getDisbursementAccountName(),
                application.getWorkplaceName(),
                application.getWorkplaceAddress(),
                customer.getPermanentAddress(),
                application.getCurrentAddress()
        );
    }

    private ReferencePersonsResponse toReferencePersonsResponse(String applicationCode, List<LoanApplicationReferencePerson> persons) {
        List<ReferencePersonResponse> items = persons.stream()
                .map(item -> new ReferencePersonResponse(
                        item.getFullName(),
                        item.getPhoneNumber(),
                        item.getRelationshipType() == null ? null : item.getRelationshipType().name(),
                        item.getAddress(),
                        item.getNote()
                ))
                .toList();
        return new ReferencePersonsResponse(applicationCode, items.size(), items);
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
        String lastCode = loanApplicationRepository
                .findTopByLoanApplicationCodeStartingWithOrderByLoanApplicationCodeDesc(APPLICATION_CODE_PREFIX)
                .map(LoanApplication::getLoanApplicationCode)
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

    private String normalizeText(String value) {
        return value == null ? null : value.trim();
    }

    private String normalizeNullableText(String value) {
        String normalized = normalizeText(value);
        return normalized == null || normalized.isBlank() ? null : normalized;
    }

    private String normalizeCode(String value) {
        String normalized = normalizeText(value);
        return normalized == null ? null : normalized.toUpperCase();
    }

    private Gender validateGender(String value) {
        String gender = normalizeCode(value);
        if (!SUPPORTED_GENDERS.contains(gender)) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "Giới tính không hợp lệ. Giá trị hợp lệ: MALE, FEMALE");
        }
        return Gender.valueOf(gender);
    }

    private MaritalStatus validateMaritalStatus(String value) {
        String status = normalizeCode(value);
        if (!SUPPORTED_MARITAL_STATUSES.contains(status)) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "Tình trạng hôn nhân không hợp lệ. Giá trị hợp lệ: SINGLE, MARRIED");
        }
        return MaritalStatus.valueOf(status);
    }

    private ReferenceRelationshipType validateRelationshipType(String value) {
        String relationshipType = normalizeCode(value);
        if (!SUPPORTED_RELATIONSHIP_TYPES.contains(relationshipType)) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "Mối quan hệ người tham chiếu không hợp lệ");
        }
        return ReferenceRelationshipType.valueOf(relationshipType);
    }

    private void validateReferencePhones(List<ReferencePersonRequest> referencePersons) {
        Set<String> phones = new HashSet<>();
        for (ReferencePersonRequest item : referencePersons) {
            String phone = normalizeText(item.phoneNumber());
            if (!phones.add(phone)) {
                throw new BusinessException(ErrorCode.VALIDATION_ERROR, "Số điện thoại người tham chiếu bị trùng trong hồ sơ: " + phone);
            }
        }
    }

    private String normalizeDocumentCode(String documentCode) {
        String normalized = normalizeCode(documentCode);
        if (normalized == null || !DOCUMENT_TYPE_CODE_BY_DOCUMENT_CODE.containsKey(normalized)) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "Loại chứng từ không hợp lệ: " + documentCode);
        }
        return normalized;
    }

    private String resolveDocumentTypeCode(String documentCode) {
        String documentTypeCode = DOCUMENT_TYPE_CODE_BY_DOCUMENT_CODE.get(documentCode);
        if (documentTypeCode == null) {
            throw new BusinessException(ErrorCode.SCHEMA_NOT_READY, "Document type mapping is not configured: " + documentCode);
        }
        return documentTypeCode;
    }

    private String resolveDocumentTypeCode(CompleteLoanApplicationDocumentUploadRequest.Document document) {
        String explicitTypeCode = normalizeCode(document.documentTypeCode());
        if (explicitTypeCode != null) {
            return explicitTypeCode;
        }
        String documentCode = normalizeDocumentCode(document.documentCode());
        return resolveDocumentTypeCode(documentCode);
    }

    private void validateCompleteDocumentUploadRequest(CompleteLoanApplicationDocumentUploadRequest request) {
        if (request == null || request.documents() == null || request.documents().isEmpty()) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "documents[] là bắt buộc khi hoàn tất upload chứng từ");
        }
        for (CompleteLoanApplicationDocumentUploadRequest.Document item : request.documents()) {
            if ((item.documentCode() == null || item.documentCode().isBlank())
                    && (item.documentTypeCode() == null || item.documentTypeCode().isBlank())) {
                throw new BusinessException(ErrorCode.VALIDATION_ERROR, "Mỗi chứng từ cần có documentCode hoặc documentTypeCode");
            }
            if (item.fileUrl() == null || item.fileUrl().isBlank()) {
                throw new BusinessException(ErrorCode.VALIDATION_ERROR, "Mỗi chứng từ cần có fileUrl");
            }
            resolveDocumentTypeCode(item);
        }
    }

    private void validateDocumentFile(MultipartFile file, String documentCode) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "File chứng từ không được để trống");
        }
        if (file.getSize() > MAX_DOCUMENT_FILE_SIZE_BYTES) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "File chứng từ không được vượt quá " + MAX_DOCUMENT_FILE_SIZE_MB + "MB");
        }
        String extension = extension(file.getOriginalFilename());
        if (extension == null || !allowedExtensions(documentCode).contains(extension)) {
            throw new BusinessException(
                    ErrorCode.VALIDATION_ERROR,
                    "Định dạng file không hợp lệ. Chỉ hỗ trợ: " + String.join(", ", allowedExtensions(documentCode))
            );
        }
    }

    private List<String> allowedExtensions(String documentCode) {
        return "CUSTOMER_PORTRAIT_VIDEO".equals(documentCode) ? VIDEO_EXTENSIONS : IMAGE_OR_PDF_EXTENSIONS;
    }

    private String extension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return null;
        }
        return fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
    }

    private boolean isNotBlank(String value) {
        return value != null && !value.isBlank();
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private Object coalesce(Object first, Object fallback) {
        return first == null ? fallback : first;
    }

    private static Map<String, Object> mapOf(Object... values) {
        Map<String, Object> map = new LinkedHashMap<>();
        for (int i = 0; i < values.length; i += 2) {
            map.put((String) values[i], values[i + 1]);
        }
        return map;
    }
}
