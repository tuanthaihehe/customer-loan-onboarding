package com.f88.loanonboarding.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.f88.loanonboarding.common.error.ErrorCode;
import com.f88.loanonboarding.dto.request.loanproduct.LoanProductRecommendationRequest;
import com.f88.loanonboarding.dto.response.loanproduct.LoanProductDetailResponse;
import com.f88.loanonboarding.dto.response.loanproduct.LoanProductQuoteResponse;
import com.f88.loanonboarding.dto.response.loanproduct.LoanProductRecommendationResponse;
import com.f88.loanonboarding.entity.LoanProduct;
import com.f88.loanonboarding.exception.BusinessException;
import com.f88.loanonboarding.repository.LoanProductRepository;
import com.f88.loanonboarding.service.LoanProductService;

@Service
public class LoanProductServiceImpl implements LoanProductService {

    private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);
    private static final int TOP_RECOMMENDATION_LIMIT = 3;

    private final LoanProductRepository loanProductRepository;

    public LoanProductServiceImpl(LoanProductRepository loanProductRepository) {
        this.loanProductRepository = loanProductRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public LoanProductRecommendationResponse recommend(LoanProductRecommendationRequest request) {
        List<LoanProductQuoteResponse> products = loanProductRepository.findByActiveTrueOrderBySortOrderAsc()
                .stream()
                .filter(product -> matchesLoanPurpose(product, request.selectedLoanPurpose()))
                .filter(product -> matchesAssetType(product, request.selectedAssetType().code()))
                .filter(product -> matchesTenor(product, request.selectedTenor()))
                .filter(product -> matchesScoreGrade(product, request.scoreGrade()))
                .map(product -> calculateQuote(product, request))
                .filter(quote -> quote.effectiveMaxLoanAmount().compareTo(quote.minLoanAmount()) >= 0)
                .sorted(quoteComparator())
                .limit(TOP_RECOMMENDATION_LIMIT)
                .toList();

        List<LoanProductQuoteResponse> rankedProducts = rank(products);
        String recommendedProductCode = rankedProducts.isEmpty() ? null : rankedProducts.get(0).productCode();
        return new LoanProductRecommendationResponse(recommendedProductCode, rankedProducts);
    }

    @Override
    @Transactional(readOnly = true)
    public LoanProductDetailResponse getDetail(String productCode) {
        LoanProduct product = findProduct(productCode);
        return new LoanProductDetailResponse(
                product.getProductCode(),
                product.getProductName(),
                product.isAppliesToAllLoanPurposes(),
                product.getMinLoanAmount(),
                product.getMaxLoanAmount(),
                product.getMaxLtvPercent(),
                product.getMonthlyInterestRatePercent(),
                product.getLoanPurposes().stream().map(item -> item.getCode()).sorted().toList(),
                product.getVehicleTypes().stream().map(item -> item.getCode()).sorted().toList(),
                product.getLoanTerms().stream().map(item -> item.getTermMonths()).sorted().toList(),
                product.getScoreGrades().stream().map(item -> item.getCode()).sorted().toList()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public LoanProductQuoteResponse quote(String productCode, LoanProductRecommendationRequest request) {
        LoanProduct product = findProduct(productCode);
        validateProductEligibility(product, request);
        LoanProductQuoteResponse quote = calculateQuote(product, request);
        if (quote.effectiveMaxLoanAmount().compareTo(quote.minLoanAmount()) < 0) {
            throw new BusinessException(
                    ErrorCode.BUSINESS_RULE_VIOLATION,
                    "Sản phẩm không đạt số tiền vay tối thiểu với giá trị tài sản hiện tại."
            );
        }
        return withRankAndRecommended(quote, 1, true);
    }

    private LoanProduct findProduct(String productCode) {
        return loanProductRepository.findByProductCode(productCode)
                .filter(LoanProduct::isActive)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.RESOURCE_NOT_FOUND,
                        "Loan product is not configured or inactive: " + productCode
                ));
    }

    private void validateProductEligibility(LoanProduct product, LoanProductRecommendationRequest request) {
        if (!matchesLoanPurpose(product, request.selectedLoanPurpose())) {
            throw new BusinessException(ErrorCode.BUSINESS_RULE_VIOLATION, "Sản phẩm không áp dụng cho mục đích vay đã chọn.");
        }
        if (!matchesAssetType(product, request.selectedAssetType().code())) {
            throw new BusinessException(ErrorCode.BUSINESS_RULE_VIOLATION, "Sản phẩm không áp dụng cho loại tài sản đã chọn.");
        }
        if (!matchesTenor(product, request.selectedTenor())) {
            throw new BusinessException(ErrorCode.BUSINESS_RULE_VIOLATION, "Sản phẩm không hỗ trợ kỳ hạn vay đã chọn.");
        }
        if (!matchesScoreGrade(product, request.scoreGrade())) {
            throw new BusinessException(ErrorCode.BUSINESS_RULE_VIOLATION, "Sản phẩm không áp dụng cho hạng điểm đã chọn.");
        }
    }

    private boolean matchesLoanPurpose(LoanProduct product, String selectedLoanPurpose) {
        return product.isAppliesToAllLoanPurposes()
                || product.getLoanPurposes().stream().anyMatch(item -> item.getCode().equals(selectedLoanPurpose));
    }

    private boolean matchesAssetType(LoanProduct product, String selectedAssetType) {
        return product.getVehicleTypes().stream().anyMatch(item -> item.getCode().equals(selectedAssetType));
    }

    private boolean matchesTenor(LoanProduct product, Integer selectedTenor) {
        return product.getLoanTerms().stream().anyMatch(item -> item.getTermMonths().equals(selectedTenor));
    }

    private boolean matchesScoreGrade(LoanProduct product, String scoreGrade) {
        if (scoreGrade == null || scoreGrade.isBlank()) {
            return true;
        }
        return product.getScoreGrades().stream().anyMatch(item -> item.getCode().equals(scoreGrade));
    }

    private LoanProductQuoteResponse calculateQuote(LoanProduct product, LoanProductRecommendationRequest request) {
        BigDecimal maxLoanByLtv = request.adjustedAssetValue()
                .multiply(product.getMaxLtvPercent())
                .divide(ONE_HUNDRED, 2, RoundingMode.HALF_UP);
        BigDecimal effectiveMaxLoanAmount = maxLoanByLtv.min(product.getMaxLoanAmount());
        BigDecimal suggestedLoanAmount = product.getMinLoanAmount()
                .max(request.requestedLoanAmount().min(effectiveMaxLoanAmount));
        BigDecimal loanAmountGap = suggestedLoanAmount.subtract(request.requestedLoanAmount()).abs();
        BigDecimal monthlyRate = product.getMonthlyInterestRatePercent()
                .divide(ONE_HUNDRED, 10, RoundingMode.HALF_UP);
        BigDecimal principalPerMonth = suggestedLoanAmount.divide(
                BigDecimal.valueOf(request.selectedTenor()),
                2,
                RoundingMode.HALF_UP
        );
        BigDecimal interestPerMonth = suggestedLoanAmount.multiply(monthlyRate).setScale(2, RoundingMode.HALF_UP);
        BigDecimal estimatedMonthlyPayment = principalPerMonth.add(interestPerMonth).setScale(2, RoundingMode.HALF_UP);

        return new LoanProductQuoteResponse(
                null,
                product.getProductCode(),
                product.getProductName(),
                product.getMinLoanAmount(),
                product.getMaxLoanAmount(),
                product.getMaxLtvPercent(),
                maxLoanByLtv,
                effectiveMaxLoanAmount,
                suggestedLoanAmount,
                loanAmountGap,
                product.getMonthlyInterestRatePercent(),
                principalPerMonth,
                interestPerMonth,
                estimatedMonthlyPayment,
                false
        );
    }

    private Comparator<LoanProductQuoteResponse> quoteComparator() {
        return Comparator
                .comparing(LoanProductQuoteResponse::loanAmountGap)
                .thenComparing(LoanProductQuoteResponse::estimatedMonthlyPayment)
                .thenComparing(LoanProductQuoteResponse::productCode);
    }

    private List<LoanProductQuoteResponse> rank(List<LoanProductQuoteResponse> products) {
        java.util.ArrayList<LoanProductQuoteResponse> ranked = new java.util.ArrayList<>();
        for (int i = 0; i < products.size(); i++) {
            ranked.add(withRankAndRecommended(products.get(i), i + 1, i == 0));
        }
        return ranked;
    }

    private LoanProductQuoteResponse withRankAndRecommended(
            LoanProductQuoteResponse source,
            int rank,
            boolean recommended
    ) {
        return new LoanProductQuoteResponse(
                rank,
                source.productCode(),
                source.productName(),
                source.minLoanAmount(),
                source.productMaxLoanAmount(),
                source.maxLtvPercent(),
                source.maxLoanByLtv(),
                source.effectiveMaxLoanAmount(),
                source.suggestedLoanAmount(),
                source.loanAmountGap(),
                source.monthlyInterestRatePercent(),
                source.principalPerMonth(),
                source.interestPerMonth(),
                source.estimatedMonthlyPayment(),
                recommended
        );
    }
}
