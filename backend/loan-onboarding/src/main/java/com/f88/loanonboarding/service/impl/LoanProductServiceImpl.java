package com.f88.loanonboarding.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
import com.f88.loanonboarding.rule.BusinessRule;
import com.f88.loanonboarding.rule.RuleContext;
import com.f88.loanonboarding.rule.RuleEvaluationService;
import com.f88.loanonboarding.rule.loanproduct.LoanProductAssetTypeRule;
import com.f88.loanonboarding.rule.loanproduct.LoanProductMinimumAmountRule;
import com.f88.loanonboarding.rule.loanproduct.LoanProductPurposeRule;
import com.f88.loanonboarding.rule.loanproduct.LoanProductScoreGradeRule;
import com.f88.loanonboarding.rule.loanproduct.LoanProductTenorRule;
import com.f88.loanonboarding.service.LoanProductService;

@Service
public class LoanProductServiceImpl implements LoanProductService {

    private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);
    private static final int TOP_RECOMMENDATION_LIMIT = 3;
    private static final List<BusinessRule> PRODUCT_RECOMMENDATION_RULES = List.of(
            new LoanProductPurposeRule(),
            new LoanProductAssetTypeRule(),
            new LoanProductTenorRule(),
            new LoanProductScoreGradeRule(),
            new LoanProductMinimumAmountRule()
    );

    private final LoanProductRepository loanProductRepository;
    private final RuleEvaluationService ruleEvaluationService;

    public LoanProductServiceImpl(
            LoanProductRepository loanProductRepository,
            RuleEvaluationService ruleEvaluationService
    ) {
        this.loanProductRepository = loanProductRepository;
        this.ruleEvaluationService = ruleEvaluationService;
    }

    @Override
    @Transactional(readOnly = true)
    public LoanProductRecommendationResponse recommend(LoanProductRecommendationRequest request) {
        List<LoanProductQuoteResponse> products = loanProductRepository.findByActiveTrueOrderBySortOrderAsc()
                .stream()
                .map(product -> new ProductQuote(product, calculateQuote(product, request)))
                .filter(item -> passesProductRules(item.product(), item.quote(), request))
                .map(ProductQuote::quote)
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
        LoanProductQuoteResponse quote = calculateQuote(product, request);
        validateProductRules(product, quote, request);
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
                product.getLoanTerms().stream().map(item -> item.getTermMonths()).sorted().toList(),
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

    private boolean passesProductRules(
            LoanProduct product,
            LoanProductQuoteResponse quote,
            LoanProductRecommendationRequest request
    ) {
        return ruleEvaluationService.evaluate(productRuleContext(product, quote, request), PRODUCT_RECOMMENDATION_RULES)
                .stream()
                .allMatch(result -> result.passed());
    }

    private void validateProductRules(
            LoanProduct product,
            LoanProductQuoteResponse quote,
            LoanProductRecommendationRequest request
    ) {
        ruleEvaluationService.validateOrThrow(productRuleContext(product, quote, request), PRODUCT_RECOMMENDATION_RULES);
    }

    private RuleContext productRuleContext(
            LoanProduct product,
            LoanProductQuoteResponse quote,
            LoanProductRecommendationRequest request
    ) {
        return RuleContext.loanProduct(
                request.selectedLoanPurpose(),
                request.selectedAssetType(),
                request.selectedTenor(),
                request.scoreGrade(),
                product.isAppliesToAllLoanPurposes(),
                product.getLoanPurposes().stream().map(item -> item.getCode()).collect(Collectors.toSet()),
                product.getVehicleTypes().stream().map(item -> item.getCode()).collect(Collectors.toSet()),
                Set.copyOf(quote.allowedTenors()),
                product.getScoreGrades().stream().map(item -> item.getCode()).collect(Collectors.toSet()),
                quote.minLoanAmount(),
                quote.effectiveMaxLoanAmount()
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

    private record ProductQuote(
            LoanProduct product,
            LoanProductQuoteResponse quote
    ) {
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
                source.allowedTenors(),
                source.minLoanAmount(),
                source.maxLoanAmount(),
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
