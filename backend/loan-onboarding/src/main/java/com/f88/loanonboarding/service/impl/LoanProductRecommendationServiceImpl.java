package com.f88.loanonboarding.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.f88.loanonboarding.common.error.ErrorCode;
import com.f88.loanonboarding.dto.request.asset.ValuationDeductionItemRequest;
import com.f88.loanonboarding.dto.request.loan.LoanProductRecommendationRequest;
import com.f88.loanonboarding.dto.response.loan.LoanProductRecommendationResponse;
import com.f88.loanonboarding.dto.response.loan.LoanProductValuationSummaryResponse;
import com.f88.loanonboarding.dto.response.loan.RecommendedLoanProductResponse;
import com.f88.loanonboarding.entity.Asset;
import com.f88.loanonboarding.entity.AssetDeductionType;
import com.f88.loanonboarding.entity.LoanApplication;
import com.f88.loanonboarding.entity.LoanProduct;
import com.f88.loanonboarding.entity.LoanTerm;
import com.f88.loanonboarding.entity.VehicleMarketPrice;
import com.f88.loanonboarding.entity.VehicleType;
import com.f88.loanonboarding.entity.VehicleVariant;
import com.f88.loanonboarding.exception.BusinessException;
import com.f88.loanonboarding.repository.AssetDeductionTypeRepository;
import com.f88.loanonboarding.repository.LoanApplicationRepository;
import com.f88.loanonboarding.repository.LoanProductRepository;
import com.f88.loanonboarding.repository.VehicleMarketPriceRepository;
import com.f88.loanonboarding.service.LoanProductRecommendationService;

@Service
public class LoanProductRecommendationServiceImpl implements LoanProductRecommendationService {

    private static final int DEFAULT_LIMIT = 3;
    private static final String DEFAULT_SCORE_GRADE = "B";
    private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);

    private final LoanApplicationRepository loanApplicationRepository;
    private final LoanProductRepository loanProductRepository;
    private final VehicleMarketPriceRepository vehicleMarketPriceRepository;
    private final AssetDeductionTypeRepository assetDeductionTypeRepository;

    public LoanProductRecommendationServiceImpl(
            LoanApplicationRepository loanApplicationRepository,
            LoanProductRepository loanProductRepository,
            VehicleMarketPriceRepository vehicleMarketPriceRepository,
            AssetDeductionTypeRepository assetDeductionTypeRepository
    ) {
        this.loanApplicationRepository = loanApplicationRepository;
        this.loanProductRepository = loanProductRepository;
        this.vehicleMarketPriceRepository = vehicleMarketPriceRepository;
        this.assetDeductionTypeRepository = assetDeductionTypeRepository;
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
}
