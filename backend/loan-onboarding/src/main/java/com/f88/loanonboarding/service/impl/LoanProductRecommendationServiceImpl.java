package com.f88.loanonboarding.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.f88.loanonboarding.common.error.ErrorCode;
import com.f88.loanonboarding.dto.request.asset.ValuationDeductionItemRequest;
import com.f88.loanonboarding.dto.request.loan.FinalLoanOfferPreviewRequest;
import com.f88.loanonboarding.dto.request.loan.LoanProductRecommendationRequest;
import com.f88.loanonboarding.dto.request.loan.SelectFinalLoanOfferRequest;
import com.f88.loanonboarding.dto.response.loan.FinalLoanOfferResponse;
import com.f88.loanonboarding.dto.response.loan.LoanProductRecommendationResponse;
import com.f88.loanonboarding.dto.response.loan.LoanProductValuationSummaryResponse;
import com.f88.loanonboarding.dto.response.loan.LoanScoringResponse;
import com.f88.loanonboarding.dto.response.loan.RecommendedLoanProductResponse;
import com.f88.loanonboarding.entity.Asset;
import com.f88.loanonboarding.entity.AssetDeductionType;
import com.f88.loanonboarding.entity.AssetValuation;
import com.f88.loanonboarding.entity.LoanApplication;
import com.f88.loanonboarding.entity.LoanProduct;
import com.f88.loanonboarding.entity.LoanTerm;
import com.f88.loanonboarding.entity.MockScoreGradeRule;
import com.f88.loanonboarding.entity.VehicleMarketPrice;
import com.f88.loanonboarding.entity.VehicleType;
import com.f88.loanonboarding.entity.VehicleVariant;
import com.f88.loanonboarding.exception.BusinessException;
import com.f88.loanonboarding.repository.AssetDeductionTypeRepository;
import com.f88.loanonboarding.repository.AssetValuationRepository;
import com.f88.loanonboarding.repository.LoanApplicationRepository;
import com.f88.loanonboarding.repository.LoanProductRepository;
import com.f88.loanonboarding.repository.LoanTermRepository;
import com.f88.loanonboarding.repository.MockScoreGradeRuleRepository;
import com.f88.loanonboarding.repository.VehicleMarketPriceRepository;
import com.f88.loanonboarding.service.LoanProductRecommendationService;

@Service
public class LoanProductRecommendationServiceImpl implements LoanProductRecommendationService {

    private static final int DEFAULT_LIMIT = 3;
    private static final String DEFAULT_SCORE_GRADE = "B";
    private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);
    private static final String STATE_DRAFT = "APP_DRAFT";

    private final LoanApplicationRepository loanApplicationRepository;
    private final LoanProductRepository loanProductRepository;
    private final VehicleMarketPriceRepository vehicleMarketPriceRepository;
    private final AssetDeductionTypeRepository assetDeductionTypeRepository;
    private final AssetValuationRepository assetValuationRepository;
    private final MockScoreGradeRuleRepository mockScoreGradeRuleRepository;
    private final LoanTermRepository loanTermRepository;

    public LoanProductRecommendationServiceImpl(
            LoanApplicationRepository loanApplicationRepository,
            LoanProductRepository loanProductRepository,
            VehicleMarketPriceRepository vehicleMarketPriceRepository,
            AssetDeductionTypeRepository assetDeductionTypeRepository,
            AssetValuationRepository assetValuationRepository,
            MockScoreGradeRuleRepository mockScoreGradeRuleRepository,
            LoanTermRepository loanTermRepository
    ) {
        this.loanApplicationRepository = loanApplicationRepository;
        this.loanProductRepository = loanProductRepository;
        this.vehicleMarketPriceRepository = vehicleMarketPriceRepository;
        this.assetDeductionTypeRepository = assetDeductionTypeRepository;
        this.assetValuationRepository = assetValuationRepository;
        this.mockScoreGradeRuleRepository = mockScoreGradeRuleRepository;
        this.loanTermRepository = loanTermRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public LoanProductRecommendationResponse recommend(String applicationCode, LoanProductRecommendationRequest request) {
        LoanApplication application = loanApplicationRepository.findByLoanApplicationCode(applicationCode)
                .orElseThrow(() -> new BusinessException(ErrorCode.LOAN_APPLICATION_NOT_FOUND));
        ensureApplicationReady(application);

        Asset asset = application.getAsset();
        VehicleVariant variant = asset.getVehicleVariant();
        VehicleType vehicleType = variant.getVehicleYear()
                .getVehicleVersion()
                .getVehicleModel()
                .getVehicleBrand()
                .getVehicleType();
        VehicleMarketPrice marketPrice = resolveCurrentMarketPrice(variant);
        List<AssetDeductionType> deductionTypes = resolveDeductionTypes(request == null ? null : request.deductionItems());
        BigDecimal totalDeductionAmount = deductionTypes.stream()
                .map(AssetDeductionType::getDeductionAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (totalDeductionAmount.compareTo(marketPrice.getPriceAmount()) > 0) {
            throw new BusinessException(
                    ErrorCode.INVALID_VALUATION_VALUE,
                    "Tổng giảm trừ không được lớn hơn giá thị trường của tài sản."
            );
        }
        BigDecimal finalValue = marketPrice.getPriceAmount().subtract(totalDeductionAmount);
        String scoreGrade = normalizeScoreGrade(request == null ? null : request.scoreGrade());
        int limit = request == null || request.limit() == null ? DEFAULT_LIMIT : request.limit();

        List<LoanProduct> products = loanProductRepository.findMatchingProducts(
                application.getLoanPurpose().getCode(),
                vehicleType.getCode(),
                application.getLoanTermMonths(),
                scoreGrade
        );
        List<RecommendedLoanProductResponse> recommendations = products.stream()
                .map(product -> toRecommendation(product, application.getRequestedAmount(), application.getLoanTermMonths(), finalValue))
                .filter(item -> item.effectiveMaxLoanAmount().compareTo(item.minLoanAmount()) >= 0)
                .sorted(Comparator
                        .comparing(RecommendedLoanProductResponse::loanAmountGap)
                        .thenComparing(RecommendedLoanProductResponse::estimatedMonthlyPayment)
                        .thenComparing(RecommendedLoanProductResponse::productCode))
                .limit(limit)
                .toList();
        recommendations = markRecommended(recommendations);

        return new LoanProductRecommendationResponse(
                application.getLoanApplicationCode(),
                application.getLoanPurpose().getCode(),
                vehicleType.getCode(),
                application.getLoanTermMonths(),
                application.getRequestedAmount(),
                scoreGrade,
                new LoanProductValuationSummaryResponse(
                        marketPrice.getPriceAmount(),
                        totalDeductionAmount,
                        finalValue,
                        deductionTypes.stream().map(AssetDeductionType::getCode).toList()
                ),
                recommendations.isEmpty() ? null : recommendations.getFirst().productCode(),
                recommendations
        );
    }

    @Override
    @Transactional(readOnly = true)
    public FinalLoanOfferResponse previewFinalOffer(String applicationCode, FinalLoanOfferPreviewRequest request) {
        LoanApplication application = loanApplicationRepository.findByLoanApplicationCode(applicationCode)
                .orElseThrow(() -> new BusinessException(ErrorCode.LOAN_APPLICATION_NOT_FOUND));
        ensureApplicationReady(application);

        FinalOfferInput input = resolveFinalOfferInput(application, request);
        FinalOfferCalculation calculation = calculateFinalOffer(application, input);
        return toFinalOfferResponse(application, input, calculation, application.getLoanProduct() == null ? null : application.getLoanProduct().getProductCode(), null);
    }

    @Override
    @Transactional
    public FinalLoanOfferResponse selectFinalOffer(String applicationCode, SelectFinalLoanOfferRequest request) {
        LoanApplication application = loanApplicationRepository.findByLoanApplicationCode(applicationCode)
                .orElseThrow(() -> new BusinessException(ErrorCode.LOAN_APPLICATION_NOT_FOUND));
        ensureDraftApplication(application);
        ensureApplicationReady(application);

        FinalOfferInput input = resolveFinalOfferInput(application, request);
        FinalOfferCalculation calculation = calculateFinalOffer(application, input);
        String productCode = normalizeCode(request.productCode());
        RecommendedLoanProductResponse selected = calculation.products().stream()
                .filter(item -> item.productCode().equals(productCode))
                .findFirst()
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.BUSINESS_RULE_VIOLATION,
                        "San pham vay da chon khong con phu hop voi nhu cau vay, scoring hoac gia tri tai san hien tai."
                ));
        LoanProduct product = loanProductRepository.findByProductCode(productCode)
                .filter(LoanProduct::isActive)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Khong tim thay san pham vay: " + productCode));

        application.setLoanProduct(product);
        application.setRequestedAmount(input.requestedAmount());
        application.setLoanTermMonths(input.loanTermMonths());
        loanTermRepository.findByTermMonthsAndActiveTrue(input.loanTermMonths())
                .ifPresent(application::setLoanTerm);
        if (input.processingBranch() != null) {
            application.setBranch(input.processingBranch());
        }
        application.setFinalRequestedAmount(input.requestedAmount());
        application.setFinalLoanTermMonths(input.loanTermMonths());
        application.setFinalPaymentMethod(input.paymentMethod());
        application.setFinalMonthlyPaymentDay(input.monthlyPaymentDay());
        application.setFinalSelectedAmount(selected.suggestedLoanAmount());
        application.setFinalEstimatedMonthlyPayment(selected.estimatedMonthlyPayment());
        application.setFinalScoreGrade(calculation.scoring().scoreGrade());
        application.setFinalSelectedAt(LocalDateTime.now());
        LoanApplication saved = loanApplicationRepository.save(application);

        return toFinalOfferResponse(saved, input, calculation, productCode, selected);
    }

    private void ensureApplicationReady(LoanApplication application) {
        if (application.getLoanPurpose() == null
                || application.getLoanTermMonths() == null
                || application.getRequestedAmount() == null) {
            throw new BusinessException(
                    ErrorCode.BUSINESS_RULE_VIOLATION,
                    "Hồ sơ chưa có đủ mục đích vay, kỳ hạn vay và số tiền mong muốn vay."
            );
        }
        if (application.getAsset() == null) {
            throw new BusinessException(
                    ErrorCode.BUSINESS_RULE_VIOLATION,
                    "Hồ sơ chưa gắn tài sản. Hãy lưu thông tin tài sản trước khi đề xuất gói vay."
            );
        }
    }

    private void ensureDraftApplication(LoanApplication application) {
        if (application.getCurrentState() == null || !STATE_DRAFT.equals(application.getCurrentState().getCode())) {
            throw new BusinessException(
                    ErrorCode.INVALID_LOAN_APPLICATION_STATE,
                    "Chi ho so nhap moi duoc luu goi vay cuoi cung."
            );
        }
    }

    private FinalOfferInput resolveFinalOfferInput(LoanApplication application, FinalLoanOfferPreviewRequest request) {
        BigDecimal requestedAmount = request == null || request.requestedAmount() == null
                ? application.getRequestedAmount()
                : request.requestedAmount();
        Integer loanTermMonths = request == null || request.loanTermMonths() == null
                ? application.getLoanTermMonths()
                : request.loanTermMonths();
        if (requestedAmount == null || requestedAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(ErrorCode.INVALID_REQUESTED_AMOUNT, "So tien vay cuoi cung phai lon hon 0.");
        }
        if (loanTermMonths == null || loanTermMonths <= 0) {
            throw new BusinessException(ErrorCode.INVALID_LOAN_TERM, "Ky han vay cuoi cung khong hop le.");
        }
        String paymentMethod = normalizeNullableCode(request == null ? null : request.paymentMethod());
        Integer monthlyPaymentDay = request == null ? null : request.monthlyPaymentDay();
        String processingBranch = normalizeNullableText(request == null ? null : request.processingBranch());
        int limit = request == null || request.limit() == null ? DEFAULT_LIMIT : request.limit();
        return new FinalOfferInput(requestedAmount, loanTermMonths, paymentMethod, monthlyPaymentDay, processingBranch, limit);
    }

    private FinalOfferInput resolveFinalOfferInput(LoanApplication application, SelectFinalLoanOfferRequest request) {
        BigDecimal requestedAmount = request.requestedAmount() == null
                ? application.getRequestedAmount()
                : request.requestedAmount();
        Integer loanTermMonths = request.loanTermMonths() == null
                ? application.getLoanTermMonths()
                : request.loanTermMonths();
        if (requestedAmount == null || requestedAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(ErrorCode.INVALID_REQUESTED_AMOUNT, "So tien vay cuoi cung phai lon hon 0.");
        }
        if (loanTermMonths == null || loanTermMonths <= 0) {
            throw new BusinessException(ErrorCode.INVALID_LOAN_TERM, "Ky han vay cuoi cung khong hop le.");
        }
        return new FinalOfferInput(
                requestedAmount,
                loanTermMonths,
                normalizeNullableCode(request.paymentMethod()),
                request.monthlyPaymentDay(),
                normalizeNullableText(request.processingBranch()),
                DEFAULT_LIMIT
        );
    }

    private FinalOfferCalculation calculateFinalOffer(LoanApplication application, FinalOfferInput input) {
        Asset asset = application.getAsset();
        VehicleVariant variant = asset.getVehicleVariant();
        VehicleType vehicleType = variant.getVehicleYear()
                .getVehicleVersion()
                .getVehicleModel()
                .getVehicleBrand()
                .getVehicleType();
        ValuationSnapshot valuation = resolveLatestValuation(asset, variant);
        LoanScoringResponse scoring = calculateScoring(application, input.requestedAmount(), valuation.finalValue());

        List<LoanProduct> products = loanProductRepository.findMatchingProducts(
                application.getLoanPurpose().getCode(),
                vehicleType.getCode(),
                input.loanTermMonths(),
                scoring.scoreGrade()
        );
        List<RecommendedLoanProductResponse> recommendations = products.stream()
                .map(product -> toRecommendation(product, input.requestedAmount(), input.loanTermMonths(), valuation.finalValue()))
                .filter(item -> item.effectiveMaxLoanAmount().compareTo(item.minLoanAmount()) >= 0)
                .sorted(Comparator
                        .comparing(RecommendedLoanProductResponse::loanAmountGap)
                        .thenComparing(RecommendedLoanProductResponse::estimatedMonthlyPayment)
                        .thenComparing(RecommendedLoanProductResponse::productCode))
                .limit(input.limit())
                .toList();
        recommendations = markRecommended(recommendations);
        return new FinalOfferCalculation(
                scoring,
                new LoanProductValuationSummaryResponse(
                        valuation.marketValue(),
                        valuation.totalDeductionAmount(),
                        valuation.finalValue(),
                        List.of()
                ),
                recommendations.isEmpty() ? null : recommendations.getFirst().productCode(),
                recommendations
        );
    }

    private ValuationSnapshot resolveLatestValuation(Asset asset, VehicleVariant variant) {
        return assetValuationRepository.findTopByAssetOrderByValuedAtDesc(asset)
                .map(valuation -> new ValuationSnapshot(
                        money(valuation.getMarketPriceAmount()),
                        money(valuation.getTotalDeductionAmount()),
                        money(valuation.getFinalValueAmount())
                ))
                .orElseGet(() -> {
                    VehicleMarketPrice marketPrice = resolveCurrentMarketPrice(variant);
                    return new ValuationSnapshot(
                            money(marketPrice.getPriceAmount()),
                            BigDecimal.ZERO,
                            money(marketPrice.getPriceAmount())
                    );
                });
    }

    private LoanScoringResponse calculateScoring(LoanApplication application, BigDecimal requestedAmount, BigDecimal finalAssetValue) {
        BigDecimal ltvPercent = requestedAmount
                .multiply(ONE_HUNDRED)
                .divide(finalAssetValue, 2, RoundingMode.HALF_UP);
        MockScoreGradeRule matchedRule = mockScoreGradeRuleRepository.findByActiveTrueOrderBySortOrderAsc()
                .stream()
                .filter(rule -> matchesRule(rule, application.getMonthlyIncomeAmount(), requestedAmount, ltvPercent))
                .findFirst()
                .orElse(null);
        String scoreGrade = matchedRule == null ? DEFAULT_SCORE_GRADE : matchedRule.getScoreGrade().getCode();
        int aScore = scoreToNumber(scoreGrade);
        int bScore = 0;
        int overallScore = aScore;
        return new LoanScoringResponse(
                scoreGrade,
                overallScore,
                aScore,
                bScore,
                1,
                0,
                ltvPercent,
                matchedRule == null ? null : matchedRule.getRuleCode(),
                matchedRule == null ? null : matchedRule.getRuleName()
        );
    }

    private boolean matchesRule(MockScoreGradeRule rule, BigDecimal monthlyIncome, BigDecimal requestedAmount, BigDecimal ltvPercent) {
        return matchesRange(monthlyIncome, rule.getMinMonthlyIncomeAmount(), rule.getMaxMonthlyIncomeAmount())
                && matchesRange(requestedAmount, rule.getMinRequestedAmount(), rule.getMaxRequestedAmount())
                && matchesRange(ltvPercent, rule.getMinLtvPercent(), rule.getMaxLtvPercent());
    }

    private boolean matchesRange(BigDecimal value, BigDecimal min, BigDecimal max) {
        if (value == null) {
            return min == null && max == null;
        }
        return (min == null || value.compareTo(min) >= 0)
                && (max == null || value.compareTo(max) <= 0);
    }

    private int scoreToNumber(String scoreGrade) {
        return switch (scoreGrade) {
            case "A" -> 95;
            case "B" -> 80;
            case "C" -> 65;
            case "D" -> 50;
            default -> 40;
        };
    }

    private FinalLoanOfferResponse toFinalOfferResponse(
            LoanApplication application,
            FinalOfferInput input,
            FinalOfferCalculation calculation,
            String selectedProductCode,
            RecommendedLoanProductResponse selectedProduct
    ) {
        RecommendedLoanProductResponse selected = selectedProduct == null
                ? calculation.products().stream()
                        .filter(item -> item.productCode().equals(selectedProductCode))
                        .findFirst()
                        .orElse(null)
                : selectedProduct;
        return new FinalLoanOfferResponse(
                application.getLoanApplicationCode(),
                input.requestedAmount(),
                input.loanTermMonths(),
                input.paymentMethod(),
                input.monthlyPaymentDay(),
                input.processingBranch() == null ? application.getBranch() : input.processingBranch(),
                calculation.scoring(),
                calculation.valuation(),
                calculation.recommendedProductCode(),
                selectedProductCode,
                selected == null ? application.getFinalSelectedAmount() : selected.suggestedLoanAmount(),
                selected == null ? application.getFinalEstimatedMonthlyPayment() : selected.estimatedMonthlyPayment(),
                application.getFinalSelectedAt(),
                calculation.products()
        );
    }

    private VehicleMarketPrice resolveCurrentMarketPrice(VehicleVariant variant) {
        return vehicleMarketPriceRepository
                .findEffectivePrices(variant, LocalDate.now())
                .stream()
                .findFirst()
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.RESOURCE_NOT_FOUND,
                        "Không tìm thấy giá thị trường hiện hành cho tài sản đã chọn."
                ));
    }

    private List<AssetDeductionType> resolveDeductionTypes(List<ValuationDeductionItemRequest> deductionItems) {
        if (deductionItems == null || deductionItems.isEmpty()) {
            return List.of();
        }
        Set<String> seenCodes = new HashSet<>();
        List<AssetDeductionType> result = new ArrayList<>();
        for (ValuationDeductionItemRequest item : deductionItems) {
            String code = item.type() == null ? "" : item.type().trim();
            if (!seenCodes.add(code)) {
                throw new BusinessException(
                        ErrorCode.BUSINESS_RULE_VIOLATION,
                        "Không được chọn trùng yếu tố giảm trừ: " + code
                );
            }
            AssetDeductionType deductionType = assetDeductionTypeRepository.findByCode(code)
                    .filter(AssetDeductionType::isActive)
                    .orElseThrow(() -> new BusinessException(
                            ErrorCode.RESOURCE_NOT_FOUND,
                            "Không tìm thấy yếu tố giảm trừ trong database: " + code
                    ));
            result.add(deductionType);
        }
        return result;
    }

    private String normalizeScoreGrade(String scoreGrade) {
        if (scoreGrade == null || scoreGrade.isBlank()) {
            return DEFAULT_SCORE_GRADE;
        }
        return scoreGrade.trim().toUpperCase();
    }

    private RecommendedLoanProductResponse toRecommendation(
            LoanProduct product,
            BigDecimal requestedAmount,
            Integer loanTermMonths,
            BigDecimal assetFinalValue
    ) {
        BigDecimal maxLoanByLtv = money(assetFinalValue
                .multiply(product.getMaxLtvPercent())
                .divide(ONE_HUNDRED, 2, RoundingMode.HALF_UP));
        BigDecimal effectiveMaxLoanAmount = min(maxLoanByLtv, product.getMaxLoanAmount());
        BigDecimal suggestedLoanAmount = max(product.getMinLoanAmount(), min(requestedAmount, effectiveMaxLoanAmount));
        BigDecimal loanAmountGap = requestedAmount.subtract(suggestedLoanAmount).abs();
        BigDecimal estimatedMonthlyPayment = calculateEstimatedMonthlyPayment(
                suggestedLoanAmount,
                loanTermMonths,
                product.getMonthlyInterestRatePercent()
        );

        return new RecommendedLoanProductResponse(
                product.getProductCode(),
                product.getProductName(),
                money(product.getMinLoanAmount()),
                money(product.getMaxLoanAmount()),
                product.getMaxLtvPercent(),
                product.getMonthlyInterestRatePercent(),
                product.getLoanTerms().stream()
                        .map(LoanTerm::getTermMonths)
                        .sorted()
                        .toList(),
                maxLoanByLtv,
                money(effectiveMaxLoanAmount),
                money(suggestedLoanAmount),
                money(loanAmountGap),
                estimatedMonthlyPayment,
                false
        );
    }

    private BigDecimal calculateEstimatedMonthlyPayment(
            BigDecimal suggestedLoanAmount,
            Integer loanTermMonths,
            BigDecimal monthlyInterestRatePercent
    ) {
        BigDecimal principalPerMonth = suggestedLoanAmount.divide(BigDecimal.valueOf(loanTermMonths), 2, RoundingMode.HALF_UP);
        BigDecimal interestPerMonth = suggestedLoanAmount
                .multiply(monthlyInterestRatePercent)
                .divide(ONE_HUNDRED, 2, RoundingMode.HALF_UP);
        return money(principalPerMonth.add(interestPerMonth));
    }

    private List<RecommendedLoanProductResponse> markRecommended(List<RecommendedLoanProductResponse> recommendations) {
        if (recommendations.isEmpty()) {
            return recommendations;
        }
        List<RecommendedLoanProductResponse> marked = new ArrayList<>();
        for (int i = 0; i < recommendations.size(); i++) {
            RecommendedLoanProductResponse item = recommendations.get(i);
            marked.add(new RecommendedLoanProductResponse(
                    item.productCode(),
                    item.productName(),
                    item.minLoanAmount(),
                    item.maxLoanAmount(),
                    item.maxLtvPercent(),
                    item.monthlyInterestRatePercent(),
                    item.supportedTermMonths(),
                    item.maxLoanByLtv(),
                    item.effectiveMaxLoanAmount(),
                    item.suggestedLoanAmount(),
                    item.loanAmountGap(),
                    item.estimatedMonthlyPayment(),
                    i == 0
            ));
        }
        return marked;
    }

    private BigDecimal min(BigDecimal first, BigDecimal second) {
        return first.compareTo(second) <= 0 ? first : second;
    }

    private BigDecimal max(BigDecimal first, BigDecimal second) {
        return first.compareTo(second) >= 0 ? first : second;
    }

    private BigDecimal money(BigDecimal value) {
        return value.setScale(0, RoundingMode.HALF_UP);
    }

    private String normalizeCode(String value) {
        return value == null ? null : value.trim().toUpperCase();
    }

    private String normalizeNullableCode(String value) {
        String normalized = normalizeCode(value);
        return normalized == null || normalized.isBlank() ? null : normalized;
    }

    private String normalizeNullableText(String value) {
        String normalized = value == null ? null : value.trim();
        return normalized == null || normalized.isBlank() ? null : normalized;
    }

    private record FinalOfferInput(
            BigDecimal requestedAmount,
            Integer loanTermMonths,
            String paymentMethod,
            Integer monthlyPaymentDay,
            String processingBranch,
            int limit
    ) {
    }

    private record ValuationSnapshot(
            BigDecimal marketValue,
            BigDecimal totalDeductionAmount,
            BigDecimal finalValue
    ) {
    }

    private record FinalOfferCalculation(
            LoanScoringResponse scoring,
            LoanProductValuationSummaryResponse valuation,
            String recommendedProductCode,
            List<RecommendedLoanProductResponse> products
    ) {
    }
}
