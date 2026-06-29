package com.f88.loanonboarding.service.impl;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.f88.loanonboarding.common.error.ErrorCode;
import com.f88.loanonboarding.dto.response.customer.OcrExtractResponse;
import com.f88.loanonboarding.exception.BusinessException;
import com.f88.loanonboarding.service.OcrService;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@Service
public class FptAiOcrServiceImpl implements OcrService {

    private static final Logger log = LoggerFactory.getLogger(FptAiOcrServiceImpl.class);
    private static final String NA = "N/A";

    private final RestTemplate restTemplate;
    private final String apiUrl;
    private final String apiKey;

    public FptAiOcrServiceImpl(
            RestTemplate restTemplate,
            @Value("${fptai.ocr.api-url}") String apiUrl,
            @Value("${fptai.ocr.api-key}") String apiKey
    ) {
        this.restTemplate = restTemplate;
        this.apiUrl = apiUrl;
        this.apiKey = apiKey;
    }

    @Override
    public OcrExtractResponse extract(MultipartFile frontImage, MultipartFile backImage) {
        FptAiData frontData = callFptAi(frontImage);

        FptAiData backData = null;
        if (backImage != null && !backImage.isEmpty()) {
            backData = callFptAi(backImage);
        }

        String issueDate = (backData != null && !NA.equals(backData.getIssueDate()))
                ? backData.getIssueDate()
                : null;

        return new OcrExtractResponse(
                frontData.getName(),
                frontData.getDob(),
                frontData.getId(),
                resolveDocumentType(frontData),
                NA.equals(frontData.getSex()) ? null : frontData.getSex(),
                NA.equals(frontData.getNationality()) ? null : frontData.getNationality(),
                NA.equals(frontData.getDoe()) ? null : frontData.getDoe(),
                issueDate,
                true,
                backData != null
        );
    }

    private String resolveDocumentType(FptAiData data) {
        if (data.getTypeNew() != null && !data.getTypeNew().isBlank()) {
            return data.getTypeNew();
        }
        return data.getType();
    }

    private FptAiData callFptAi(MultipartFile image) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("api-key", apiKey);
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            String filename = (image.getOriginalFilename() != null)
                    ? image.getOriginalFilename()
                    : "image.jpg";

            byte[] imageBytes = image.getBytes();

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("image", new ByteArrayResource(imageBytes) {
                @Override
                public String getFilename() {
                    return filename;
                }
            });

            HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);
            ResponseEntity<FptAiResponse> response = restTemplate.postForEntity(apiUrl, request, FptAiResponse.class);

            FptAiResponse fptResponse = response.getBody();

            if (fptResponse == null) {
                throw new BusinessException(ErrorCode.OCR_SERVICE_ERROR, "Không nhận được phản hồi từ dịch vụ OCR");
            }

            if (fptResponse.getErrorCode() != 0) {
                log.warn("FPT AI OCR error: code={}, message={}", fptResponse.getErrorCode(), fptResponse.getErrorMessage());
                throw new BusinessException(
                        mapFptErrorCode(fptResponse.getErrorCode()),
                        fptResponse.getErrorMessage()
                );
            }

            if (fptResponse.getData() == null || fptResponse.getData().isEmpty()) {
                throw new BusinessException(ErrorCode.OCR_ID_NOT_FOUND, "Không tìm thấy thông tin giấy tờ trong ảnh");
            }

            return fptResponse.getData().get(0);

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("OCR service call failed: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.OCR_SERVICE_ERROR, "Lỗi kết nối dịch vụ OCR: " + e.getMessage());
        }
    }

    private ErrorCode mapFptErrorCode(int fptErrorCode) {
        return switch (fptErrorCode) {
            case 2 ->
                ErrorCode.OCR_IMAGE_CROP_FAILED;
            case 3 ->
                ErrorCode.OCR_ID_NOT_FOUND;
            case 7, 8 ->
                ErrorCode.OCR_INVALID_IMAGE;
            default ->
                ErrorCode.OCR_SERVICE_ERROR;
        };
    }

    // =========================================================
    // Internal DTOs — dùng để parse response từ FPT AI
    // =========================================================
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class FptAiResponse {

        @JsonProperty("errorCode")
        private int errorCode;

        @JsonProperty("errorMessage")
        private String errorMessage;

        @JsonProperty("data")
        private List<FptAiData> data;

        public int getErrorCode() {
            return errorCode;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public List<FptAiData> getData() {
            return data;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class FptAiData {

        @JsonProperty("id")
        private String id;
        @JsonProperty("name")
        private String name;
        @JsonProperty("dob")
        private String dob;
        @JsonProperty("sex")
        private String sex;
        @JsonProperty("nationality")
        private String nationality;
        @JsonProperty("doe")
        private String doe;
        @JsonProperty("type")
        private String type;
        @JsonProperty("type_new")
        private String typeNew;
        @JsonProperty("issue_date")
        private String issueDate;

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getDob() {
            return dob;
        }

        public String getSex() {
            return sex;
        }

        public String getNationality() {
            return nationality;
        }

        public String getDoe() {
            return doe;
        }

        public String getType() {
            return type;
        }

        public String getTypeNew() {
            return typeNew;
        }

        public String getIssueDate() {
            return issueDate;
        }
    }
}
