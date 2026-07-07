package com.f88.loanonboarding.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.f88.loanonboarding.common.error.ErrorCode;
import com.f88.loanonboarding.dto.request.loan.CompleteLoanApplicationDraftStepRequest;
import com.f88.loanonboarding.dto.request.loan.SaveLoanApplicationDraftStepRequest;
import com.f88.loanonboarding.dto.response.loan.DraftDocumentResponses;
import com.f88.loanonboarding.dto.response.loan.LoanApplicationDraftStepActionResponse;
import com.f88.loanonboarding.entity.LoanApplicationDraft;
import com.f88.loanonboarding.entity.LoanApplicationDraftStepData;
import com.f88.loanonboarding.enums.LoanApplicationDraftStatus;
import com.f88.loanonboarding.enums.LoanApplicationDraftStepStatus;
import com.f88.loanonboarding.exception.BusinessException;
import com.f88.loanonboarding.repository.LoanApplicationDraftRepository;
import com.f88.loanonboarding.repository.LoanApplicationDraftStepDataRepository;
import com.f88.loanonboarding.service.DocumentStorageService;
import com.f88.loanonboarding.service.LoanApplicationDraftDocumentService;
import com.f88.loanonboarding.service.LoanApplicationDraftFlowService;

@Service
public class LoanApplicationDraftDocumentServiceImpl implements LoanApplicationDraftDocumentService {

    private static final String STEP_UPLOAD_COMPLETE = "UPLOAD_COMPLETE";
    private static final String PAYLOAD_UPLOADED_DOCUMENTS = "uploaded_documents";
    private static final String PAYLOAD_EKYC = "ekyc";
    private static final long MAX_FILE_SIZE_MB = 5L;
    private static final long MAX_FILE_SIZE_BYTES = MAX_FILE_SIZE_MB * 1024L * 1024L;

    private static final List<String> IMAGE_OR_PDF_EXTENSIONS = List.of("jpg", "jpeg", "png", "webp", "pdf");
    private static final List<String> VIDEO_EXTENSIONS = List.of("mp4", "webm", "mov");

    private final LoanApplicationDraftRepository draftRepository;
    private final LoanApplicationDraftStepDataRepository stepDataRepository;
    private final LoanApplicationDraftFlowService draftFlowService;
    private final ObjectMapper objectMapper;
    private final DocumentStorageService documentStorageService;

    public LoanApplicationDraftDocumentServiceImpl(
            LoanApplicationDraftRepository draftRepository,
            LoanApplicationDraftStepDataRepository stepDataRepository,
            LoanApplicationDraftFlowService draftFlowService,
            ObjectMapper objectMapper,
            DocumentStorageService documentStorageService
    ) {
        this.draftRepository = draftRepository;
        this.stepDataRepository = stepDataRepository;
        this.draftFlowService = draftFlowService;
        this.objectMapper = objectMapper;
        this.documentStorageService = documentStorageService;
    }

    @Override
    public List<DraftDocumentResponses.RequirementGroup> getRequirements() {
        return documentDefinitions().stream()
                .collect(Collectors.groupingBy(DocumentDefinition::groupCode, LinkedHashMap::new, Collectors.toList()))
                .entrySet()
                .stream()
                .map(entry -> {
                    List<DocumentDefinition> items = entry.getValue();
                    DocumentDefinition first = items.get(0);
                    return new DraftDocumentResponses.RequirementGroup(
                            first.groupCode(),
                            first.groupName(),
                            (int) items.stream().filter(DocumentDefinition::required).count(),
                            items.size(),
                            items.stream()
                                    .map(item -> new DraftDocumentResponses.RequirementItem(
                                            item.documentCode(),
                                            item.documentName(),
                                            item.required(),
                                            item.allowedExtensions(),
                                            item.maxSizeMb()
                                    ))
                                    .toList()
                    );
                })
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public DraftDocumentResponses.State getState(String draftCode) {
        ensureDraftExists(draftCode);
        LoanApplicationDraftStepData stepData = findUploadStep(draftCode);
        return toState(draftCode, payloadOrEmpty(stepData.getPayload()));
    }

    @Override
    @Transactional
    public DraftDocumentResponses.UploadResult upload(String draftCode, String documentCode, MultipartFile file) {
        ensureEditableDraft(draftCode);
        DocumentDefinition definition = definitionByCode(documentCode);
        validateFile(file, definition);

        LoanApplicationDraftStepData stepData = findUploadStep(draftCode);
        ObjectNode payload = payloadObject(stepData.getPayload());
        Map<String, UploadedDocument> uploadedDocuments = uploadedDocuments(payload);
        UploadedDocument oldDocument = uploadedDocuments.get(definition.documentCode());

        UploadedDocument newDocument = storeFile(draftCode, definition, file);
        uploadedDocuments.put(definition.documentCode(), newDocument);

        ObjectNode updatedPayload = buildPayload(uploadedDocuments);
        draftFlowService.saveStep(
                draftCode,
                STEP_UPLOAD_COMPLETE,
                new SaveLoanApplicationDraftStepRequest(LoanApplicationDraftStepStatus.IN_PROGRESS, updatedPayload)
        );
        deleteStoredFileQuietly(oldDocument);

        DraftDocumentResponses.State state = toState(draftCode, updatedPayload);
        return new DraftDocumentResponses.UploadResult(
                documentFile(definition, newDocument),
                state.checklist(),
                state.ekycResult()
        );
    }

    @Override
    @Transactional
    public DraftDocumentResponses.State delete(String draftCode, String documentCode) {
        ensureEditableDraft(draftCode);
        DocumentDefinition definition = definitionByCode(documentCode);
        LoanApplicationDraftStepData stepData = findUploadStep(draftCode);
        ObjectNode payload = payloadObject(stepData.getPayload());
        Map<String, UploadedDocument> uploadedDocuments = uploadedDocuments(payload);
        UploadedDocument removedDocument = uploadedDocuments.remove(definition.documentCode());

        ObjectNode updatedPayload = buildPayload(uploadedDocuments);
        draftFlowService.saveStep(
                draftCode,
                STEP_UPLOAD_COMPLETE,
                new SaveLoanApplicationDraftStepRequest(LoanApplicationDraftStepStatus.IN_PROGRESS, updatedPayload)
        );
        deleteStoredFileQuietly(removedDocument);
        return toState(draftCode, updatedPayload);
    }

    @Override
    @Transactional
    public LoanApplicationDraftStepActionResponse complete(String draftCode) {
        ensureEditableDraft(draftCode);
        LoanApplicationDraftStepData stepData = findUploadStep(draftCode);
        ObjectNode payload = payloadObject(stepData.getPayload());
        DraftDocumentResponses.Checklist checklist = checklist(uploadedDocuments(payload));
        if (!checklist.canComplete()) {
            throw new BusinessException(
                    ErrorCode.VALIDATION_ERROR,
                    "Chưa upload đủ chứng từ bắt buộc: " + String.join(", ", checklist.missingRequiredDocumentCodes())
            );
        }
        return draftFlowService.completeStep(
                draftCode,
                STEP_UPLOAD_COMPLETE,
                new CompleteLoanApplicationDraftStepRequest(buildPayload(uploadedDocuments(payload)))
        );
    }

    private UploadedDocument storeFile(String draftCode, DocumentDefinition definition, MultipartFile file) {
        String originalFileName = StringUtils.cleanPath(file.getOriginalFilename() == null ? "upload" : file.getOriginalFilename());
        String extension = extension(originalFileName);
        String fileId = UUID.randomUUID().toString();
        DocumentStorageService.StoredObject storedObject = documentStorageService.store(
                "loan-application-drafts",
                draftCode,
                definition.documentCode(),
                fileId,
                extension,
                file.getContentType(),
                file
        );
        return new UploadedDocument(
                definition.documentCode(),
                definition.groupCode(),
                fileId,
                originalFileName,
                file.getContentType(),
                file.getSize(),
                storedObject.storagePath(),
                storedObject.fileUrl(),
                LocalDateTime.now()
        );
    }

    private void validateFile(MultipartFile file, DocumentDefinition definition) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "File chứng từ không được để trống");
        }
        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "File chứng từ không được vượt quá " + MAX_FILE_SIZE_MB + "MB");
        }
        String fileName = file.getOriginalFilename();
        String extension = extension(fileName);
        if (extension == null || !definition.allowedExtensions().contains(extension)) {
            throw new BusinessException(
                    ErrorCode.VALIDATION_ERROR,
                    "Định dạng file không hợp lệ. Chỉ hỗ trợ: " + String.join(", ", definition.allowedExtensions())
            );
        }
    }

    private DraftDocumentResponses.State toState(String draftCode, JsonNode payload) {
        Map<String, UploadedDocument> uploadedDocuments = uploadedDocuments(payload);
        List<DraftDocumentResponses.GroupState> groups = documentDefinitions().stream()
                .collect(Collectors.groupingBy(DocumentDefinition::groupCode, LinkedHashMap::new, Collectors.toList()))
                .values()
                .stream()
                .map(items -> {
                    DocumentDefinition first = items.get(0);
                    List<DraftDocumentResponses.DocumentFile> files = items.stream()
                            .map(item -> documentFile(item, uploadedDocuments.get(item.documentCode())))
                            .toList();
                    return new DraftDocumentResponses.GroupState(
                            first.groupCode(),
                            first.groupName(),
                            (int) files.stream().filter(DraftDocumentResponses.DocumentFile::uploaded).count(),
                            files.size(),
                            (int) files.stream().filter(file -> file.required() && file.uploaded()).count(),
                            (int) items.stream().filter(DocumentDefinition::required).count(),
                            files
                    );
                })
                .toList();
        return new DraftDocumentResponses.State(draftCode, groups, checklist(uploadedDocuments), ekycResult(payload));
    }

    private DraftDocumentResponses.DocumentFile documentFile(DocumentDefinition definition, UploadedDocument uploadedDocument) {
        if (uploadedDocument == null) {
            return new DraftDocumentResponses.DocumentFile(
                    definition.documentCode(),
                    definition.documentName(),
                    definition.groupCode(),
                    definition.required(),
                    false,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
            );
        }
        return new DraftDocumentResponses.DocumentFile(
                definition.documentCode(),
                definition.documentName(),
                definition.groupCode(),
                definition.required(),
                true,
                uploadedDocument.fileId(),
                uploadedDocument.fileName(),
                uploadedDocument.contentType(),
                uploadedDocument.size(),
                uploadedDocument.fileUrl(),
                uploadedDocument.uploadedAt()
        );
    }

    private DraftDocumentResponses.Checklist checklist(Map<String, UploadedDocument> uploadedDocuments) {
        List<String> missingRequiredDocumentCodes = documentDefinitions().stream()
                .filter(DocumentDefinition::required)
                .map(DocumentDefinition::documentCode)
                .filter(code -> !uploadedDocuments.containsKey(code))
                .toList();
        int totalRequired = (int) documentDefinitions().stream().filter(DocumentDefinition::required).count();
        return new DraftDocumentResponses.Checklist(
                totalRequired,
                totalRequired - missingRequiredDocumentCodes.size(),
                missingRequiredDocumentCodes.isEmpty(),
                missingRequiredDocumentCodes
        );
    }

    private DraftDocumentResponses.EkycResult ekycResult(JsonNode payload) {
        JsonNode ekyc = payload == null ? null : payload.get(PAYLOAD_EKYC);
        String faceMatchStatus = textOrDefault(ekyc, "face_match_status", "NO_DATA");
        String livenessStatus = textOrDefault(ekyc, "liveness_status", "NO_DATA");
        String note = textOrDefault(
                ekyc,
                "note",
                "Chưa có API eKYC/face match để đối chiếu tự động. Màn hình giữ vị trí kết quả để ghép BE sau."
        );
        return new DraftDocumentResponses.EkycResult(faceMatchStatus, livenessStatus, note);
    }

    private ObjectNode buildPayload(Map<String, UploadedDocument> uploadedDocuments) {
        ObjectNode payload = objectMapper.createObjectNode();
        ArrayNode documentArray = objectMapper.createArrayNode();
        uploadedDocuments.values().forEach(document -> {
            ObjectNode item = objectMapper.createObjectNode();
            item.put("document_code", document.documentCode());
            item.put("group_code", document.groupCode());
            item.put("file_id", document.fileId());
            item.put("file_name", document.fileName());
            item.put("content_type", document.contentType());
            item.put("size", document.size());
            item.put("storage_path", document.storagePath());
            item.put("file_url", document.fileUrl());
            item.put("uploaded_at", document.uploadedAt().toString());
            documentArray.add(item);
        });
        payload.set(PAYLOAD_UPLOADED_DOCUMENTS, documentArray);

        DraftDocumentResponses.Checklist checklist = checklist(uploadedDocuments);
        ObjectNode checklistNode = objectMapper.createObjectNode();
        checklistNode.put("total_required", checklist.totalRequired());
        checklistNode.put("uploaded_required", checklist.uploadedRequired());
        checklistNode.put("can_complete", checklist.canComplete());
        ArrayNode missingNode = objectMapper.createArrayNode();
        checklist.missingRequiredDocumentCodes().forEach(missingNode::add);
        checklistNode.set("missing_required_document_codes", missingNode);
        payload.set("checklist", checklistNode);

        ObjectNode ekycNode = objectMapper.createObjectNode();
        ekycNode.put("face_match_status", "NO_DATA");
        ekycNode.put("liveness_status", "NO_DATA");
        ekycNode.put("note", "Chưa có API eKYC/face match để đối chiếu tự động. Màn hình giữ vị trí kết quả để ghép BE sau.");
        payload.set(PAYLOAD_EKYC, ekycNode);
        return payload;
    }

    private Map<String, UploadedDocument> uploadedDocuments(JsonNode payload) {
        Map<String, UploadedDocument> result = new LinkedHashMap<>();
        JsonNode uploadedNode = payload == null ? null : payload.get(PAYLOAD_UPLOADED_DOCUMENTS);
        if (uploadedNode == null || !uploadedNode.isArray()) {
            return result;
        }
        for (JsonNode item : uploadedNode) {
            String documentCode = text(item, "document_code");
            if (documentCode == null) {
                continue;
            }
            result.put(documentCode, new UploadedDocument(
                    documentCode,
                    text(item, "group_code"),
                    text(item, "file_id"),
                    text(item, "file_name"),
                    text(item, "content_type"),
                    item.path("size").isNumber() ? item.path("size").asLong() : null,
                    text(item, "storage_path"),
                    textOrDefault(item, "file_url", text(item, "storage_path")),
                    parseDateTime(text(item, "uploaded_at"))
            ));
        }
        return result;
    }

    private LoanApplicationDraft ensureDraftExists(String draftCode) {
        return draftRepository.findByDraftCode(draftCode)
                .orElseThrow(() -> new BusinessException(ErrorCode.LOAN_APPLICATION_DRAFT_NOT_FOUND, "Không tìm thấy hồ sơ vay nháp: " + draftCode));
    }

    private LoanApplicationDraft ensureEditableDraft(String draftCode) {
        LoanApplicationDraft draft = ensureDraftExists(draftCode);
        if (LoanApplicationDraftStatus.CONVERTED.name().equals(draft.getStatus())
                || LoanApplicationDraftStatus.CANCELLED.name().equals(draft.getStatus())
                || LoanApplicationDraftStatus.EXPIRED.name().equals(draft.getStatus())) {
            throw new BusinessException(ErrorCode.INVALID_LOAN_APPLICATION_STATE, "Hồ sơ vay nháp không còn được phép chỉnh sửa");
        }
        return draft;
    }

    private LoanApplicationDraftStepData findUploadStep(String draftCode) {
        return stepDataRepository.findByDraft_DraftCodeAndStep_Code(draftCode, STEP_UPLOAD_COMPLETE)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Chưa cấu hình bước UPLOAD_COMPLETE cho hồ sơ vay nháp"));
    }

    private ObjectNode payloadObject(JsonNode payload) {
        if (payload == null || payload.isNull() || payload.isMissingNode() || !payload.isObject()) {
            return objectMapper.createObjectNode();
        }
        return ((ObjectNode) payload).deepCopy();
    }

    private JsonNode payloadOrEmpty(JsonNode payload) {
        return payload == null || payload.isNull() || payload.isMissingNode() ? objectMapper.createObjectNode() : payload;
    }

    private DocumentDefinition definitionByCode(String documentCode) {
        String normalizedCode = documentCode == null ? "" : documentCode.trim().toUpperCase(Locale.ROOT);
        return documentDefinitions().stream()
                .filter(definition -> definition.documentCode().equals(normalizedCode))
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.VALIDATION_ERROR, "Loại chứng từ không hợp lệ: " + documentCode));
    }

    private List<DocumentDefinition> documentDefinitions() {
        List<DocumentDefinition> definitions = new ArrayList<>();
        definitions.add(new DocumentDefinition("CITIZEN_ID_FRONT", "CCCD mặt trước", "CITIZEN_ID", "CCCD", true, IMAGE_OR_PDF_EXTENSIONS, MAX_FILE_SIZE_MB));
        definitions.add(new DocumentDefinition("CITIZEN_ID_BACK", "CCCD mặt sau", "CITIZEN_ID", "CCCD", true, IMAGE_OR_PDF_EXTENSIONS, MAX_FILE_SIZE_MB));

        definitions.add(new DocumentDefinition("VEHICLE_REGISTRATION_FRONT", "Cà vẹt mặt trước", "VEHICLE_REGISTRATION", "Cà vẹt xe", true, IMAGE_OR_PDF_EXTENSIONS, MAX_FILE_SIZE_MB));
        definitions.add(new DocumentDefinition("VEHICLE_REGISTRATION_BACK", "Cà vẹt mặt sau", "VEHICLE_REGISTRATION", "Cà vẹt xe", true, IMAGE_OR_PDF_EXTENSIONS, MAX_FILE_SIZE_MB));

        definitions.add(new DocumentDefinition("ASSET_FRONT", "Ảnh xe - Góc trước", "ASSET_IMAGES", "Ảnh tài sản", true, IMAGE_OR_PDF_EXTENSIONS, MAX_FILE_SIZE_MB));
        definitions.add(new DocumentDefinition("ASSET_REAR", "Ảnh xe - Góc sau", "ASSET_IMAGES", "Ảnh tài sản", true, IMAGE_OR_PDF_EXTENSIONS, MAX_FILE_SIZE_MB));
        definitions.add(new DocumentDefinition("ASSET_LEFT", "Ảnh xe - Góc trái", "ASSET_IMAGES", "Ảnh tài sản", true, IMAGE_OR_PDF_EXTENSIONS, MAX_FILE_SIZE_MB));
        definitions.add(new DocumentDefinition("ASSET_RIGHT", "Ảnh xe - Góc phải", "ASSET_IMAGES", "Ảnh tài sản", true, IMAGE_OR_PDF_EXTENSIONS, MAX_FILE_SIZE_MB));
        definitions.add(new DocumentDefinition("ASSET_FRAME_NUMBER", "Ảnh số khung", "ASSET_IMAGES", "Ảnh tài sản", false, IMAGE_OR_PDF_EXTENSIONS, MAX_FILE_SIZE_MB));
        definitions.add(new DocumentDefinition("ASSET_ENGINE_NUMBER", "Ảnh số máy", "ASSET_IMAGES", "Ảnh tài sản", false, IMAGE_OR_PDF_EXTENSIONS, MAX_FILE_SIZE_MB));
        definitions.add(new DocumentDefinition("ASSET_ODO", "Ảnh đồng hồ ODO", "ASSET_IMAGES", "Ảnh tài sản", false, IMAGE_OR_PDF_EXTENSIONS, MAX_FILE_SIZE_MB));

        definitions.add(new DocumentDefinition("CUSTOMER_PORTRAIT", "Ảnh chân dung khách hàng", "CUSTOMER_PORTRAIT", "Chân dung Khách hàng", true, IMAGE_OR_PDF_EXTENSIONS, MAX_FILE_SIZE_MB));
        definitions.add(new DocumentDefinition("CUSTOMER_HOLDING_ID", "Ảnh chân dung cầm CCCD", "CUSTOMER_PORTRAIT", "Chân dung Khách hàng", false, IMAGE_OR_PDF_EXTENSIONS, MAX_FILE_SIZE_MB));
        definitions.add(new DocumentDefinition("CUSTOMER_PORTRAIT_VIDEO", "Video chân dung Khách hàng", "CUSTOMER_PORTRAIT", "Chân dung Khách hàng", true, VIDEO_EXTENSIONS, MAX_FILE_SIZE_MB));

        definitions.add(new DocumentDefinition("INCOME_PROOF", "Chứng minh thu nhập", "OTHER_DOCUMENTS", "Chứng từ khác", false, IMAGE_OR_PDF_EXTENSIONS, MAX_FILE_SIZE_MB));
        definitions.add(new DocumentDefinition("RESIDENCE_PROOF", "Sổ hộ khẩu / Giấy tạm trú", "OTHER_DOCUMENTS", "Chứng từ khác", false, IMAGE_OR_PDF_EXTENSIONS, MAX_FILE_SIZE_MB));
        definitions.add(new DocumentDefinition("SIGNED_CUSTOMER_CONTRACT", "Hợp đồng có chữ ký KH", "OTHER_DOCUMENTS", "Chứng từ khác", false, IMAGE_OR_PDF_EXTENSIONS, MAX_FILE_SIZE_MB));
        definitions.add(new DocumentDefinition("REFERENCE_VERIFICATION_FORM", "Phiếu xác minh người tham chiếu", "OTHER_DOCUMENTS", "Chứng từ khác", false, IMAGE_OR_PDF_EXTENSIONS, MAX_FILE_SIZE_MB));
        return definitions;
    }

    private void deleteStoredFileQuietly(UploadedDocument document) {
        if (document == null || document.storagePath() == null || document.storagePath().isBlank()) {
            return;
        }
        documentStorageService.deleteQuietly(document.storagePath());
    }

    private String extension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return null;
        }
        return fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
    }

    private String text(JsonNode node, String field) {
        JsonNode value = node == null ? null : node.get(field);
        return value == null || value.isNull() ? null : value.asText();
    }

    private String textOrDefault(JsonNode node, String field, String defaultValue) {
        String value = text(node, field);
        return value == null || value.isBlank() ? defaultValue : value;
    }

    private LocalDateTime parseDateTime(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return LocalDateTime.parse(value);
    }

    private record DocumentDefinition(
            String documentCode,
            String documentName,
            String groupCode,
            String groupName,
            boolean required,
            List<String> allowedExtensions,
            long maxSizeMb
    ) {
    }

    private record UploadedDocument(
            String documentCode,
            String groupCode,
            String fileId,
            String fileName,
            String contentType,
            Long size,
            String storagePath,
            String fileUrl,
            LocalDateTime uploadedAt
    ) {
    }
}
