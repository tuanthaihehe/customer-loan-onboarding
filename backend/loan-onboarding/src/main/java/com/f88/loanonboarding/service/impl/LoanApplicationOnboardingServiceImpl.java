package com.f88.loanonboarding.service.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
import com.f88.loanonboarding.dto.response.loan.LoanApplicationDocumentListResponse;
import com.f88.loanonboarding.dto.response.loan.LoanApplicationOnboardingAssetResponse;
import com.f88.loanonboarding.dto.response.loan.LoanApplicationOnboardingCustomerResponse;
import com.f88.loanonboarding.dto.response.loan.LoanApplicationOnboardingDetailResponse;
import com.f88.loanonboarding.dto.response.loan.LoanApplicationOnboardingLoanInfoResponse;
import com.f88.loanonboarding.dto.response.loan.LoanApplicationOnboardingReferencePersonResponse;
import com.f88.loanonboarding.dto.response.loan.LoanApplicationStepActionResponse;
import com.f88.loanonboarding.dto.response.loan.LoanApplicationStepResponse;
import com.f88.loanonboarding.dto.response.loan.LoanApplicationSubmitResponse;
import com.f88.loanonboarding.dto.response.loan.LoanApplicationOnboardingSummaryResponse;
import com.f88.loanonboarding.dto.response.loan.LoanApplicationOnboardingValuationResponse;
import com.f88.loanonboarding.entity.Asset;
import com.f88.loanonboarding.entity.AssetValuation;
import com.f88.loanonboarding.entity.Customer;
import com.f88.loanonboarding.entity.LoanApplication;
import com.f88.loanonboarding.entity.LoanApplicationDocument;
import com.f88.loanonboarding.entity.LoanApplicationReferencePerson;
import com.f88.loanonboarding.entity.LoanApplicationState;
import com.f88.loanonboarding.entity.LoanApplicationStateHistory;
import com.f88.loanonboarding.entity.LoanApplicationStep;
import com.f88.loanonboarding.entity.LoanApplicationStepData;
import com.f88.loanonboarding.entity.LoanApplicationStepHistory;
import com.f88.loanonboarding.entity.LoanProduct;
import com.f88.loanonboarding.entity.LoanPurpose;
import com.f88.loanonboarding.entity.LoanTerm;
import com.f88.loanonboarding.entity.VehicleBrand;
import com.f88.loanonboarding.entity.VehicleColor;
import com.f88.loanonboarding.entity.VehicleModel;
import com.f88.loanonboarding.entity.VehicleType;
import com.f88.loanonboarding.entity.VehicleVariant;
import com.f88.loanonboarding.entity.VehicleVersion;
import com.f88.loanonboarding.entity.VehicleYear;
import com.f88.loanonboarding.enums.LoanApplicationOnboardingStatus;
import com.f88.loanonboarding.enums.LoanApplicationStepDataStatus;
import com.f88.loanonboarding.enums.LoanApplicationStepHistoryAction;
import com.f88.loanonboarding.enums.LoanApplicationStepStatus;
import com.f88.loanonboarding.enums.AssetStatus;
import com.f88.loanonboarding.enums.Gender;
import com.f88.loanonboarding.enums.MaritalStatus;
import com.f88.loanonboarding.enums.ReferenceRelationshipType;
import com.f88.loanonboarding.exception.BusinessException;
import com.f88.loanonboarding.repository.AssetRepository;
import com.f88.loanonboarding.repository.AssetValuationRepository;
import com.f88.loanonboarding.repository.BankRepository;
import com.f88.loanonboarding.repository.CustomerRepository;
import com.f88.loanonboarding.repository.IncomeSourceRepository;
import com.f88.loanonboarding.repository.LoanApplicationDocumentRepository;
import com.f88.loanonboarding.repository.LoanApplicationRepository;
import com.f88.loanonboarding.repository.LoanApplicationReferencePersonRepository;
import com.f88.loanonboarding.repository.LoanApplicationStateHistoryRepository;
import com.f88.loanonboarding.repository.LoanApplicationStateRepository;
import com.f88.loanonboarding.repository.LoanApplicationStateTransitionRepository;
import com.f88.loanonboarding.repository.LoanApplicationStepDataRepository;
import com.f88.loanonboarding.repository.LoanApplicationStepHistoryRepository;
import com.f88.loanonboarding.repository.LoanApplicationStepRepository;
import com.f88.loanonboarding.repository.LoanProductRepository;
import com.f88.loanonboarding.repository.LoanPurposeRepository;
import com.f88.loanonboarding.repository.LoanTermRepository;
import com.f88.loanonboarding.repository.OccupationRepository;
import com.f88.loanonboarding.repository.VehicleVariantRepository;
import com.f88.loanonboarding.service.DocumentStorageService;
import com.f88.loanonboarding.service.LoanApplicationOnboardingService;

@Service
public class LoanApplicationOnboardingServiceImpl implements LoanApplicationOnboardingService {

    private static final String STEP_CUSTOMER_IDENTIFY = "CUSTOMER_IDENTIFY";
    private static final String STEP_UPLOAD_COMPLETE = "UPLOAD_COMPLETE";
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
    private final OccupationRepository occupationRepository;
    private final IncomeSourceRepository incomeSourceRepository;
    private final BankRepository bankRepository;
    private final AssetRepository assetRepository;
    private final VehicleVariantRepository vehicleVariantRepository;
    private final LoanApplicationReferencePersonRepository referencePersonRepository;
    private final LoanApplicationDocumentRepository documentRepository;
    private final AssetValuationRepository assetValuationRepository;
    private final DocumentStorageService documentStorageService;
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
            OccupationRepository occupationRepository,
            IncomeSourceRepository incomeSourceRepository,
            BankRepository bankRepository,
            AssetRepository assetRepository,
            VehicleVariantRepository vehicleVariantRepository,
            LoanApplicationReferencePersonRepository referencePersonRepository,
            LoanApplicationDocumentRepository documentRepository,
            AssetValuationRepository assetValuationRepository,
            DocumentStorageService documentStorageService,
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
        this.occupationRepository = occupationRepository;
        this.incomeSourceRepository = incomeSourceRepository;
        this.bankRepository = bankRepository;
        this.assetRepository = assetRepository;
        this.vehicleVariantRepository = vehicleVariantRepository;
        this.referencePersonRepository = referencePersonRepository;
        this.documentRepository = documentRepository;
        this.assetValuationRepository = assetValuationRepository;
        this.documentStorageService = documentStorageService;
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
        clearReviewMetadata(stepData);
        stepData.setReviewedAt(now);
        stepDataRepository.saveAndFlush(stepData);

        applyPayloadToLoanApplication(application, applicationCode);

        List<LoanApplicationStep> steps = activeSteps();
        LoanApplicationStep nextStep = nextStep(steps, stepData.getStep());
        boolean allCompleted = allStepsCompleted(applicationCode);
        LoanApplicationState previousState = application.getCurrentState();
        boolean alreadyCompleted = STATE_COMPLETED.equals(previousState.getCode());
        if (nextStep != null) {
            application.setCurrentStep(nextStep);
        }
        if (STATE_CREATED.equals(previousState.getCode()) && STEP_CUSTOMER_IDENTIFY.equals(stepCode)) {
            transitionState(application, STATE_IN_PROGRESS, "IDENTIFY_COMPLETE", "Customer identify completed");
        }
        if (allCompleted && !alreadyCompleted) {
            transitionState(application, STATE_COMPLETED, "COMPLETE_APPLICATION", "All application steps completed");
        }
        application.setUpdatedAt(now);
        loanApplicationRepository.save(application);

        markDownstreamRequiresReview(application, stepData.getStep(), now);
        saveStepHistory(application, stepData.getStep(), LoanApplicationStepHistoryAction.COMPLETE_STEP, oldStatus.name(), "COMPLETED", "Complete application step", toJson(request.payload()));
        if (allCompleted && !alreadyCompleted) {
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

    private void clearReviewMetadata(LoanApplicationStepData stepData) {
        stepData.setRequiresReview(false);
        stepData.setInvalidatedAt(null);
        stepData.setInvalidatedByStep(null);
        stepData.setInvalidatedReason(null);
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
        normalizeCompletedUploadStepReview(steps);
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

    private void normalizeCompletedUploadStepReview(List<LoanApplicationStepData> steps) {
        steps.stream()
                .filter(step -> STEP_UPLOAD_COMPLETE.equals(step.getStep().getCode()))
                .filter(step -> step.getStatus() == LoanApplicationStepStatus.COMPLETED)
                .filter(LoanApplicationStepData::isRequiresReview)
                .findFirst()
                .ifPresent(step -> {
                    LocalDateTime now = LocalDateTime.now();
                    clearReviewMetadata(step);
                    step.setReviewedAt(now);
                    step.setUpdatedAt(now);
                    stepDataRepository.saveAndFlush(step);
                });
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

        applyCustomerPayload(application.getCustomer(), preliminary, mergedProposal);
        applyCustomerDetailPayload(application, preliminary, mergedProposal);

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

        applyAssetPayload(application, preliminary, mergedProposal);
        applyReferencePersonsPayload(application, mergedProposal);
        applyValuationPayload(application, preliminary, mergedProposal);
        loanApplicationRepository.save(application);
    }

    private void applyCustomerPayload(Customer customer, JsonNode preliminary, JsonNode mergedProposal) {
        JsonNode applicantSnapshot = at(preliminary, "applicantSnapshot");
        JsonNode customerDetail = at(mergedProposal, "customerDetail");

        String fullName = firstText(at(customerDetail, "fullName"), at(applicantSnapshot, "fullName"));
        if (fullName != null) {
            customer.setFullName(fullName);
        }
        String identityNumber = firstText(at(customerDetail, "identityNumber"), at(applicantSnapshot, "identifierNumber"));
        if (identityNumber != null && !customerRepository.existsByIdentityNumberAndIdNot(identityNumber, customer.getId())) {
            customer.setIdentityNumber(identityNumber);
        }
        String phoneNumber = firstText(at(customerDetail, "phoneNumber"), at(applicantSnapshot, "phoneNumber"));
        if (phoneNumber != null && !customerRepository.existsByPhoneNumberAndIdNot(phoneNumber, customer.getId())) {
            customer.setPhoneNumber(phoneNumber);
        }
        LocalDate dateOfBirth = firstDate(at(customerDetail, "dateOfBirth"), at(applicantSnapshot, "dateOfBirth"));
        if (dateOfBirth != null) {
            customer.setDateOfBirth(dateOfBirth);
        }
        Gender gender = enumValue(Gender.class, firstText(at(customerDetail, "gender"), at(applicantSnapshot, "gender")));
        if (gender != null) {
            customer.setGender(gender);
        }
        String email = firstText(at(customerDetail, "email"));
        if (email != null && !customerRepository.existsByEmailAndIdNot(email, customer.getId())) {
            customer.setEmail(email);
        }
        MaritalStatus maritalStatus = enumValue(MaritalStatus.class, firstText(at(customerDetail, "maritalStatus")));
        if (maritalStatus != null) {
            customer.setMaritalStatus(maritalStatus);
        }
        String permanentAddress = firstText(at(customerDetail, "permanentAddress"));
        if (permanentAddress != null) {
            customer.setPermanentAddress(permanentAddress);
        }
        customerRepository.save(customer);
    }

    private void applyCustomerDetailPayload(LoanApplication application, JsonNode preliminary, JsonNode mergedProposal) {
        JsonNode customerDetail = at(mergedProposal, "customerDetail");
        JsonNode applicantSnapshot = at(preliminary, "applicantSnapshot");

        String currentAddress = firstText(at(customerDetail, "currentAddress"));
        if (currentAddress != null) {
            application.setCurrentAddress(currentAddress);
        }
        String workplaceName = firstText(at(customerDetail, "workplaceName"));
        if (workplaceName != null) {
            application.setWorkplaceName(workplaceName);
        }
        String workplaceAddress = firstText(at(customerDetail, "workplaceAddress"));
        if (workplaceAddress != null) {
            application.setWorkplaceAddress(workplaceAddress);
        }
        BigDecimal monthlyIncome = firstNumber(
                at(customerDetail, "monthlyIncomeAmount"),
                at(applicantSnapshot, "monthlyIncome")
        );
        if (monthlyIncome != null) {
            application.setMonthlyIncomeAmount(monthlyIncome);
        }
        String occupationCode = firstText(at(customerDetail, "occupationCode"), at(applicantSnapshot, "occupation"));
        if (occupationCode != null) {
            occupationRepository.findByCode(occupationCode).ifPresent(application::setOccupation);
        }
        String incomeSourceCode = firstText(at(customerDetail, "incomeSourceCode"));
        if (incomeSourceCode != null) {
            incomeSourceRepository.findByCode(incomeSourceCode).ifPresent(application::setIncomeSource);
        }
        String bankCode = firstText(at(customerDetail, "disbursementBankCode"));
        if (bankCode != null) {
            bankRepository.findByCode(bankCode).ifPresent(application::setDisbursementBank);
        }
        String accountNumber = firstText(at(customerDetail, "disbursementAccountNumber"));
        if (accountNumber != null) {
            application.setDisbursementAccountNumber(accountNumber);
        }
        String accountName = firstText(at(customerDetail, "disbursementAccountName"));
        if (accountName != null) {
            application.setDisbursementAccountName(accountName);
        }
    }

    private void applyAssetPayload(LoanApplication application, JsonNode preliminary, JsonNode mergedProposal) {
        JsonNode assetDetail = at(mergedProposal, "assetDetail");
        JsonNode assetSnapshot = at(preliminary, "assetSnapshot");
        String licensePlate = normalizeLicensePlate(firstText(at(assetDetail, "licensePlate")));
        String vehicleVariantCode = firstText(at(assetDetail, "vehicleVariant"), at(assetSnapshot, "vehicleVariant"));

        if (licensePlate == null || vehicleVariantCode == null) {
            return;
        }

        VehicleVariant variant = vehicleVariantRepository.findByCode(vehicleVariantCode)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.RESOURCE_NOT_FOUND,
                        "Không tìm thấy phiên bản xe trong database: " + vehicleVariantCode
                ));
        Asset asset = assetRepository.findByLicensePlate(licensePlate)
                .orElseGet(() -> createAsset(licensePlate, variant));
        asset.setVehicleVariant(variant);

        String frameNumber = normalizeIdentifier(firstText(at(assetDetail, "frameNumber")));
        if (canUseUniqueAssetIdentifier(frameNumber, findAssetByFrameNumber(frameNumber), asset)) {
            asset.setFrameNumber(frameNumber);
        }
        String engineNumber = normalizeIdentifier(firstText(at(assetDetail, "engineNumber")));
        if (canUseUniqueAssetIdentifier(engineNumber, findAssetByEngineNumber(engineNumber), asset)) {
            asset.setEngineNumber(engineNumber);
        }
        String registrationNumber = normalizeIdentifier(firstText(at(assetDetail, "registrationNumber")));
        if (canUseUniqueAssetIdentifier(registrationNumber, findAssetByRegistrationNumber(registrationNumber), asset)) {
            asset.setRegistrationNumber(registrationNumber);
        }
        LocalDate registrationIssueDate = firstDate(at(assetDetail, "registrationIssueDate"));
        if (registrationIssueDate != null) {
            asset.setRegistrationIssueDate(registrationIssueDate);
        }

        application.setAsset(assetRepository.save(asset));
    }

    private void applyReferencePersonsPayload(LoanApplication application, JsonNode mergedProposal) {
        JsonNode references = at(mergedProposal, "referencePersons");
        if (references == null || !references.isArray()) {
            return;
        }

        referencePersonRepository.deleteByLoanApplicationId(application.getId());
        referencePersonRepository.flush();
        LocalDateTime now = LocalDateTime.now();
        Set<String> seenPhoneNumbers = new HashSet<>();
        for (JsonNode item : references) {
            String fullName = firstText(at(item, "fullName"));
            String phoneNumber = firstText(at(item, "phoneNumber"));
            ReferenceRelationshipType relationshipType = enumValue(
                    ReferenceRelationshipType.class,
                    firstText(at(item, "relationshipType"))
            );
            if (fullName == null || phoneNumber == null || relationshipType == null) {
                continue;
            }
            String normalizedPhoneNumber = phoneNumber.trim();
            if (!seenPhoneNumbers.add(normalizedPhoneNumber)) {
                continue;
            }

            LoanApplicationReferencePerson referencePerson = new LoanApplicationReferencePerson();
            referencePerson.setLoanApplication(application);
            referencePerson.setFullName(fullName);
            referencePerson.setPhoneNumber(normalizedPhoneNumber);
            referencePerson.setRelationshipType(relationshipType);
            referencePerson.setAddress(firstText(at(item, "address")));
            referencePerson.setNote(firstText(at(item, "note")));
            referencePerson.setCreatedAt(now);
            referencePerson.setUpdatedAt(now);
            referencePersonRepository.save(referencePerson);
        }
    }

    private void applyValuationPayload(LoanApplication application, JsonNode preliminary, JsonNode mergedProposal) {
        Asset asset = application.getAsset();
        if (asset == null) {
            return;
        }

        BigDecimal marketValue = firstNumber(
                at(mergedProposal, "valuation", "marketValue"),
                at(mergedProposal, "valuation", "marketPriceAmount"),
                at(mergedProposal, "valuation", "marketPrice", "priceAmount"),
                at(mergedProposal, "valuation", "preview", "marketValue"),
                at(preliminary, "valuation", "marketValue"),
                at(preliminary, "valuation", "marketPrice", "priceAmount")
        );
        BigDecimal totalDeduction = firstNumber(
                at(mergedProposal, "valuation", "totalDeductionAmount"),
                at(mergedProposal, "valuation", "preview", "totalDeductionAmount"),
                at(preliminary, "valuation", "totalDeductionAmount")
        );
        BigDecimal finalValue = firstNumber(
                at(mergedProposal, "valuation", "finalValue"),
                at(mergedProposal, "valuation", "finalValueAmount"),
                at(mergedProposal, "valuation", "preview", "finalValue"),
                at(preliminary, "valuation", "finalValue")
        );

        if (marketValue == null || finalValue == null) {
            return;
        }

        AssetValuation valuation = assetValuationRepository.findTopByAssetOrderByValuedAtDesc(asset)
                .orElseGet(AssetValuation::new);
        valuation.setAsset(asset);
        valuation.setMarketPriceAmount(marketValue);
        valuation.setTotalDeductionAmount(totalDeduction == null ? BigDecimal.ZERO : totalDeduction);
        valuation.setFinalValueAmount(finalValue);
        valuation.setCurrencyCode("VND");
        valuation.setValuationSource("ONBOARDING_STEP_PAYLOAD");
        valuation.setValuedAt(LocalDateTime.now());
        assetValuationRepository.save(valuation);
    }

    private Asset createAsset(String licensePlate, VehicleVariant variant) {
        Asset asset = new Asset();
        asset.setAssetCode(nextAssetCode());
        asset.setLicensePlate(licensePlate);
        asset.setVehicleVariant(variant);
        asset.setStatus(AssetStatus.AVAILABLE);
        return asset;
    }

    private String nextAssetCode() {
        String prefix = "AST-" + Year.now().getValue() + "-";
        long sequence = assetRepository.countByAssetCodeStartingWith(prefix) + 1;
        return prefix + String.format("%06d", sequence);
    }

    private boolean canUseUniqueAssetIdentifier(String value, Asset existing, Asset current) {
        return value != null && (existing == null || existing.getId().equals(current.getId()));
    }

    private Asset findAssetByFrameNumber(String frameNumber) {
        return frameNumber == null ? null : assetRepository.findByFrameNumber(frameNumber).orElse(null);
    }

    private Asset findAssetByEngineNumber(String engineNumber) {
        return engineNumber == null ? null : assetRepository.findByEngineNumber(engineNumber).orElse(null);
    }

    private Asset findAssetByRegistrationNumber(String registrationNumber) {
        return registrationNumber == null ? null : assetRepository.findByRegistrationNumber(registrationNumber).orElse(null);
    }

    private String normalizeLicensePlate(String value) {
        String normalized = normalizeIdentifier(value);
        return normalized == null ? null : normalized.replaceAll("\\s+", "");
    }

    private String normalizeIdentifier(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim().toUpperCase();
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
                steps,
                toLoanInfoResponse(application),
                toAssetResponse(application.getAsset()),
                toValuationResponse(application.getAsset()),
                toReferenceResponses(application),
                toDocumentResponses(application)
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
                customer.getDateOfBirth(),
                customer.getGender() == null ? null : customer.getGender().name(),
                customer.getEmail(),
                customer.getMaritalStatus() == null ? null : customer.getMaritalStatus().name(),
                customer.getPermanentAddress(),
                customer.getStatus() == null ? null : customer.getStatus().name()
        );
    }

    private LoanApplicationOnboardingLoanInfoResponse toLoanInfoResponse(LoanApplication application) {
        LoanPurpose loanPurpose = application.getLoanPurpose();
        LoanTerm loanTerm = application.getLoanTerm();
        LoanProduct loanProduct = application.getLoanProduct();

        return new LoanApplicationOnboardingLoanInfoResponse(
                application.getRequestedAmount(),
                application.getLoanTermMonths(),
                application.getBranch(),
                application.getCurrentAddress(),
                application.getWorkplaceName(),
                application.getWorkplaceAddress(),
                application.getMonthlyIncomeAmount(),
                loanPurpose == null ? null : loanPurpose.getCode(),
                loanPurpose == null ? null : loanPurpose.getName(),
                loanTerm == null ? null : loanTerm.getCode(),
                loanTerm == null ? null : loanTerm.getName(),
                application.getOccupation() == null ? null : application.getOccupation().getCode(),
                application.getOccupation() == null ? null : application.getOccupation().getName(),
                application.getIncomeSource() == null ? null : application.getIncomeSource().getCode(),
                application.getIncomeSource() == null ? null : application.getIncomeSource().getName(),
                application.getDisbursementBank() == null ? null : application.getDisbursementBank().getCode(),
                application.getDisbursementBank() == null ? null : application.getDisbursementBank().getName(),
                application.getDisbursementAccountNumber(),
                application.getDisbursementAccountName(),
                loanProduct == null ? null : loanProduct.getProductCode(),
                loanProduct == null ? null : loanProduct.getProductName(),
                loanProduct == null ? null : loanProduct.getMonthlyInterestRatePercent(),
                loanProduct == null ? null : loanProduct.getMaxLtvPercent()
        );
    }

    private LoanApplicationOnboardingAssetResponse toAssetResponse(Asset asset) {
        if (asset == null) {
            return null;
        }

        VehicleVariant variant = asset.getVehicleVariant();
        VehicleColor color = variant == null ? null : variant.getVehicleColor();
        VehicleYear year = variant == null ? null : variant.getVehicleYear();
        VehicleVersion version = year == null ? null : year.getVehicleVersion();
        VehicleModel model = version == null ? null : version.getVehicleModel();
        VehicleBrand brand = model == null ? null : model.getVehicleBrand();
        VehicleType type = brand == null ? null : brand.getVehicleType();

        return new LoanApplicationOnboardingAssetResponse(
                asset.getId(),
                asset.getAssetCode(),
                asset.getLicensePlate(),
                asset.getStatus() == null ? null : asset.getStatus().name(),
                asset.getFrameNumber(),
                asset.getEngineNumber(),
                asset.getRegistrationNumber(),
                asset.getRegistrationIssueDate(),
                type == null ? null : type.getCode(),
                type == null ? null : type.getName(),
                brand == null ? null : brand.getCode(),
                brand == null ? null : brand.getName(),
                model == null ? null : model.getCode(),
                model == null ? null : model.getName(),
                version == null ? null : version.getCode(),
                version == null ? null : version.getName(),
                year == null ? null : year.getManufactureYear(),
                color == null ? null : color.getCode(),
                color == null ? null : color.getName(),
                variant == null ? null : variant.getCode(),
                variant == null ? null : variant.getName()
        );
    }

    private LoanApplicationOnboardingValuationResponse toValuationResponse(Asset asset) {
        if (asset == null) {
            return null;
        }

        return assetValuationRepository.findTopByAssetOrderByValuedAtDesc(asset)
                .map(this::toValuationResponse)
                .orElse(null);
    }

    private LoanApplicationOnboardingValuationResponse toValuationResponse(AssetValuation valuation) {
        return new LoanApplicationOnboardingValuationResponse(
                valuation.getId(),
                valuation.getMarketPriceAmount(),
                valuation.getTotalDeductionAmount(),
                valuation.getFinalValueAmount(),
                valuation.getCurrencyCode(),
                valuation.getValuationSource(),
                valuation.getValuedAt(),
                valuation.getValuedBy(),
                valuation.getNote()
        );
    }

    private List<LoanApplicationOnboardingReferencePersonResponse> toReferenceResponses(LoanApplication application) {
        return referencePersonRepository.findByLoanApplicationId(application.getId())
                .stream()
                .map(this::toReferenceResponse)
                .toList();
    }

    private LoanApplicationOnboardingReferencePersonResponse toReferenceResponse(LoanApplicationReferencePerson referencePerson) {
        return new LoanApplicationOnboardingReferencePersonResponse(
                referencePerson.getId(),
                referencePerson.getFullName(),
                referencePerson.getPhoneNumber(),
                referencePerson.getAddress(),
                referencePerson.getRelationshipType() == null ? null : referencePerson.getRelationshipType().name(),
                referencePerson.getNote()
        );
    }

    private List<LoanApplicationDocumentListResponse.DocumentItem> toDocumentResponses(LoanApplication application) {
        return documentRepository.findByLoanApplicationIdOrderByUploadedAtDesc(application.getId())
                .stream()
                .map(this::toDocumentResponse)
                .toList();
    }

    private LoanApplicationDocumentListResponse.DocumentItem toDocumentResponse(LoanApplicationDocument document) {
        return new LoanApplicationDocumentListResponse.DocumentItem(
                document.getId(),
                document.getDocumentType().getCode(),
                document.getDocumentType().getName(),
                resolveDocumentReadUrl(document.getFileUrl()),
                document.getFileName(),
                document.getUploadedAt(),
                document.getUploadedBy()
        );
    }

    private String resolveDocumentReadUrl(String storedUrl) {
        String readUrl = documentStorageService.createReadUrl(storedUrl);
        return readUrl == null || readUrl.isBlank() ? storedUrl : readUrl;
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

    private LocalDate firstDate(JsonNode... nodes) {
        String value = firstText(nodes);
        if (value == null) {
            return null;
        }
        try {
            return LocalDate.parse(value);
        } catch (RuntimeException ignored) {
            return null;
        }
    }

    private <T extends Enum<T>> T enumValue(Class<T> enumClass, String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Enum.valueOf(enumClass, value.trim().toUpperCase());
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }
}
