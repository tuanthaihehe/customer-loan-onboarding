package com.f88.loanonboarding.service.impl;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;

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
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.multipart.MultipartFile;

import com.f88.loanonboarding.common.error.ErrorCode;
import com.f88.loanonboarding.dto.response.customer.OcrExtractResponse;
import com.f88.loanonboarding.exception.BusinessException;
import com.f88.loanonboarding.service.OcrService;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class FptAiOcrServiceImpl implements OcrService {

    private static final Logger log = LoggerFactory.getLogger(FptAiOcrServiceImpl.class);
    private static final String NA = "N/A";
    private static final int MIN_WIDTH = 640;
    private static final int MIN_HEIGHT = 480;
    private static final double MIN_LAPLACIAN_VARIANCE = 45.0;
    private static final double MIN_BRIGHTNESS = 35.0;
    private static final double MAX_BRIGHTNESS = 225.0;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final String apiUrl;
    private final String apiKey;

    public FptAiOcrServiceImpl(
            RestTemplate restTemplate,
            ObjectMapper objectMapper,
            @Value("${fptai.ocr.api-url}") String apiUrl,
            @Value("${fptai.ocr.api-key}") String apiKey
    ) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
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
        ImageQualityReport qualityReport = inspectImageQuality(image);
        validateBeforeCallingOcr(qualityReport);

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

            validateFptResponse(fptResponse, qualityReport);

            if (fptResponse.getData() == null || fptResponse.getData().isEmpty()) {
                throw ocrNotFoundException(qualityReport);
            }

            return fptResponse.getData().get(0);

        } catch (BusinessException e) {
            throw e;
        } catch (HttpStatusCodeException e) {
            throw handleFptHttpError(e, qualityReport);
        } catch (Exception e) {
            log.error("OCR service call failed: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.OCR_SERVICE_ERROR, "Không gọi được dịch vụ OCR, vui lòng thử lại sau");
        }
    }

    private void validateFptResponse(FptAiResponse fptResponse, ImageQualityReport qualityReport) {
        if (fptResponse.getErrorCode() == 0) {
            return;
        }

        log.warn("FPT AI OCR business error: code={}, message={}", fptResponse.getErrorCode(), fptResponse.getErrorMessage());
        throw mapFptError(fptResponse.getErrorCode(), qualityReport);
    }

    private BusinessException handleFptHttpError(HttpStatusCodeException e, ImageQualityReport qualityReport) {
        FptAiResponse fptResponse = parseFptErrorBody(e.getResponseBodyAsString());
        if (fptResponse != null) {
            log.warn(
                    "FPT AI OCR HTTP error: status={}, code={}, message={}",
                    e.getStatusCode(),
                    fptResponse.getErrorCode(),
                    fptResponse.getErrorMessage()
            );
            return mapFptError(fptResponse.getErrorCode(), qualityReport);
        }

        log.error("FPT AI OCR HTTP error without parseable body: status={}, body={}", e.getStatusCode(), e.getResponseBodyAsString());
        return new BusinessException(ErrorCode.OCR_SERVICE_ERROR, "Dịch vụ OCR trả lỗi không đọc được, vui lòng thử lại sau");
    }

    private FptAiResponse parseFptErrorBody(String responseBody) {
        if (responseBody == null || responseBody.isBlank()) {
            return null;
        }

        try {
            return objectMapper.readValue(responseBody, FptAiResponse.class);
        } catch (Exception ex) {
            return null;
        }
    }

    private BusinessException mapFptError(int fptErrorCode, ImageQualityReport qualityReport) {
        return switch (fptErrorCode) {
            case 2 -> new BusinessException(
                    ErrorCode.OCR_IMAGE_CROP_FAILED,
                    "Ảnh bị thiếu góc hoặc giấy tờ không nằm trọn trong khung hình, vui lòng chụp lại toàn bộ CCCD/CMND"
            );
            case 3 -> ocrNotFoundException(qualityReport);
            case 7, 8 -> new BusinessException(
                    ErrorCode.OCR_INVALID_IMAGE,
                    "File không phải ảnh hợp lệ hoặc ảnh bị lỗi, vui lòng chọn ảnh JPG/PNG/WEBP khác"
            );
            default -> new BusinessException(
                    ErrorCode.OCR_SERVICE_ERROR,
                    "Dịch vụ OCR chưa xử lý được ảnh này, vui lòng thử lại hoặc chụp ảnh khác"
            );
        };
    }

    private BusinessException ocrNotFoundException(ImageQualityReport qualityReport) {
        if (qualityReport.tooSmall()) {
            return new BusinessException(
                    ErrorCode.OCR_IMAGE_TOO_FAR,
                    "Ảnh có độ phân giải thấp hoặc giấy tờ chụp quá xa, vui lòng chụp gần hơn để CCCD/CMND chiếm phần lớn khung hình"
            );
        }

        if (qualityReport.blurry()) {
            return new BusinessException(
                    ErrorCode.OCR_IMAGE_BLURRY,
                    "Ảnh CCCD/CMND bị mờ hoặc rung tay, vui lòng chụp lại rõ nét hơn"
            );
        }

        if (qualityReport.badExposure()) {
            return new BusinessException(
                    ErrorCode.OCR_INVALID_IMAGE,
                    "Ảnh quá tối hoặc bị lóa sáng, vui lòng chụp lại ở nơi đủ sáng và không phản chiếu"
            );
        }

        return new BusinessException(
                ErrorCode.OCR_ID_NOT_FOUND,
                "Không phát hiện CCCD/CMND trong ảnh, vui lòng chụp đúng mặt trước hoặc mặt sau giấy tờ"
        );
    }

    private void validateBeforeCallingOcr(ImageQualityReport qualityReport) {
        if (qualityReport.tooSmall()) {
            throw new BusinessException(
                    ErrorCode.OCR_IMAGE_TOO_FAR,
                    "Ảnh có độ phân giải thấp hoặc giấy tờ chụp quá xa, vui lòng chụp gần hơn"
            );
        }

        if (qualityReport.badExposure()) {
            throw new BusinessException(
                    ErrorCode.OCR_INVALID_IMAGE,
                    "Ảnh quá tối hoặc bị lóa sáng, vui lòng chụp lại ở nơi đủ sáng và không phản chiếu"
            );
        }
    }

    private ImageQualityReport inspectImageQuality(MultipartFile image) {
        try {
            byte[] bytes = image.getBytes();
            BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(bytes));
            if (bufferedImage == null) {
                throw new BusinessException(ErrorCode.OCR_INVALID_IMAGE, "File không phải ảnh hợp lệ, vui lòng chọn ảnh JPG/PNG/WEBP");
            }

            int width = bufferedImage.getWidth();
            int height = bufferedImage.getHeight();
            double brightness = averageBrightness(bufferedImage);
            double sharpness = laplacianVariance(bufferedImage);

            return new ImageQualityReport(
                    width,
                    height,
                    brightness,
                    sharpness,
                    width < MIN_WIDTH || height < MIN_HEIGHT,
                    sharpness < MIN_LAPLACIAN_VARIANCE,
                    brightness < MIN_BRIGHTNESS || brightness > MAX_BRIGHTNESS
            );
        } catch (BusinessException e) {
            throw e;
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.OCR_INVALID_IMAGE, "Không đọc được file ảnh, vui lòng chọn ảnh khác");
        }
    }

    private double averageBrightness(BufferedImage image) {
        long sum = 0;
        int width = image.getWidth();
        int height = image.getHeight();

        for (int y = 0; y < height; y += 2) {
            for (int x = 0; x < width; x += 2) {
                sum += grayAt(image, x, y);
            }
        }

        int sampleCount = ((height + 1) / 2) * ((width + 1) / 2);
        return sampleCount == 0 ? 0 : (double) sum / sampleCount;
    }

    private double laplacianVariance(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        if (width < 3 || height < 3) {
            return 0;
        }

        double sum = 0;
        double sumSquare = 0;
        long count = 0;

        for (int y = 1; y < height - 1; y += 2) {
            for (int x = 1; x < width - 1; x += 2) {
                int center = grayAt(image, x, y);
                int laplacian = -4 * center
                        + grayAt(image, x - 1, y)
                        + grayAt(image, x + 1, y)
                        + grayAt(image, x, y - 1)
                        + grayAt(image, x, y + 1);

                sum += laplacian;
                sumSquare += (double) laplacian * laplacian;
                count++;
            }
        }

        if (count == 0) {
            return 0;
        }

        double mean = sum / count;
        return (sumSquare / count) - (mean * mean);
    }

    private int grayAt(BufferedImage image, int x, int y) {
        int rgb = image.getRGB(x, y);
        int red = (rgb >> 16) & 0xff;
        int green = (rgb >> 8) & 0xff;
        int blue = rgb & 0xff;
        return (red * 299 + green * 587 + blue * 114) / 1000;
    }

    private record ImageQualityReport(
            int width,
            int height,
            double brightness,
            double sharpness,
            boolean tooSmall,
            boolean blurry,
            boolean badExposure
    ) {
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
