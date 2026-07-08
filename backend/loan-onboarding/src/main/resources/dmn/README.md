# DMN Decision Files

Updated from `Rule.xlsx` on 2026-07-08.

The old per-rule DMN files were removed. The current structure groups all rules by business stage so each file can be copied/imported into Camunda Modeler as one DMN diagram with multiple decision tables.

## Files

| File | Business stage | Decisions |
| --- | --- | --- |
| `customer-kyc-rules.dmn` | Customer identification and KYC | `customerAgeEligibility`, `existingCustomerSuggestion`, `blacklistedCustomerSelectionBlock`, `otpRequirementDecision`, `otpVerification` |
| `asset-loan-product-rules.dmn` | Asset, loan request, loan product recommendation | `assetNotPledged`, `supportedAssetType`, `supportedLoanPurpose`, `activeLoanProduct`, `proposedAmountWithinEligibleLimit`, `productAmountRange`, `productTenorAllowed`, `productScoringMatch` |
| `application-completion-rules.dmn` | Application completion | `referencePersonRequired`, `referencePhoneUniqueness`, `identityIssueDateValid`, `assetRegistrationIssueDateValid` |
| `submission-operational-rules.dmn` | Submission and operational controls | `ekycVerificationPassed`, `applicationEditable` |

## Important Decision IDs

`customerAgeEligibility` is still used by backend local DMN evaluation through `CustomerAgeDmnDecisionService`.

When deploying to Camunda SaaS, keep the decision ID exactly:

```text
customerAgeEligibility
```

The configured property is:

```properties
app.camunda.dmn.customer-age-decision-id=customerAgeEligibility
```

## Input Naming Convention

The DMN files use lower camel case input variables:

| Rule Code | Decision ID | Main inputs |
| --- | --- | --- |
| `CUS_AGE_ELIGIBLE` | `customerAgeEligibility` | `age` |
| `CUS_EXISTING_CUSTOMER_SUGGESTION` | `existingCustomerSuggestion` | `matchedFieldCount`, `phoneMatched`, `identifierMatched` |
| `CUS_BLACKLISTED_SELECTION_BLOCK` | `blacklistedCustomerSelectionBlock` | `customerStatus` |
| `KYC_OTP_REQUIREMENT` | `otpRequirementDecision` | `customerType`, `inputPhoneNumber`, `storedPhoneNumber` |
| `KYC_OTP_VERIFIED` | `otpVerification` | `otpRequired`, `otpVerificationResult` |
| `AST_NOT_PLEDGED` | `assetNotPledged` | `assetState` |
| `AST_SUPPORTED_TYPE` | `supportedAssetType` | `assetType` |
| `APP_SUPPORTED_LOAN_PURPOSE` | `supportedLoanPurpose` | `loanPurpose` |
| `PRD_ACTIVE_PRODUCT` | `activeLoanProduct` | `productState` |
| `APP_PROPOSED_AMOUNT_WITHIN_ELIGIBLE_LIMIT` | `proposedAmountWithinEligibleLimit` | `proposedLoanAmount`, `finalValuationAmount`, `productLtvRatio` |
| `PRD_AMOUNT_RANGE` | `productAmountRange` | `proposedLoanAmount`, `productMinLoanAmount`, `productMaxLoanAmount` |
| `PRD_TENOR_ALLOWED` | `productTenorAllowed` | `loanTermMonths`, `productTenorSupported` |
| `APP_REFERENCE_PERSON_REQUIRED` | `referencePersonRequired` | `referencePersonCount` |
| `APP_REFERENCE_PHONE_UNIQUE` | `referencePhoneUniqueness` | `referencePhoneDuplicateExists`, `borrowerPhoneInReferences` |
| `CUS_IDENTITY_ISSUE_DATE_VALID` | `identityIssueDateValid` | `identityDocumentIssueDate`, `currentDate` |
| `AST_REGISTRATION_ISSUE_DATE_VALID` | `assetRegistrationIssueDateValid` | `assetRegistrationIssueDate`, `currentDate` |
| `PRD_SCORING_MATCH` | `productScoringMatch` | `productScoreMatched` |
| `KYC_EKYC_VERIFICATION_PASSED` | `ekycVerificationPassed` | `faceMatchResult`, `livenessCheckResult`, `fraudCheckResult` |
| `APP_EDITABLE` | `applicationEditable` | `applicationState` |

For list matching rules such as product tenor and score grade, backend should calculate boolean inputs before calling DMN:

```text
productTenorSupported = loanTermMonths IN ProductAllowedTenors
productScoreMatched = scoreGrade IN ProductAllowedScoreGrades
```

This keeps the DMN easy to evaluate from both backend and Camunda SaaS without ambiguity around list serialization.
