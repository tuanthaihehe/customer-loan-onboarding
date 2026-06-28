package com.f88.loanonboarding.mock;

import java.util.List;
import java.util.stream.IntStream;

import org.springframework.stereotype.Component;

import com.f88.loanonboarding.dto.response.common.ReferenceDataItemResponse;

@Component
public class DemoReferenceDataMockDataProvider {

    public List<ReferenceDataItemResponse> genders() {
        return List.of(item("MALE", "Nam"), item("FEMALE", "Nữ"), item("OTHER", "Khác"));
    }

    public List<ReferenceDataItemResponse> occupations() {
        return List.of(item("EMPLOYEE", "Nhân viên"), item("BUSINESS_OWNER", "Chủ kinh doanh"), item("FREELANCER", "Lao động tự do"));
    }

    public List<ReferenceDataItemResponse> loanPurposes() {
        return List.of(item("BUSINESS", "Bổ sung vốn kinh doanh"), item("PERSONAL", "Chi tiêu cá nhân"), item("EMERGENCY", "Nhu cầu cấp thiết"));
    }

    public List<ReferenceDataItemResponse> assetTypes() {
        return List.of(item("MOTORBIKE", "Xe máy"), item("CAR", "Ô tô"));
    }

    public List<ReferenceDataItemResponse> vehicleBrands() {
        return List.of(item("HONDA", "Honda"), item("YAMAHA", "Yamaha"), item("VINFAST", "VinFast"));
    }

    public List<ReferenceDataItemResponse> vehicleModels() {
        return List.of(item("SH", "SH"), item("VISION", "Vision"), item("AIR_BLADE", "Air Blade"));
    }

    public List<ReferenceDataItemResponse> vehicleVariants() {
        return List.of(item("STANDARD", "Tiêu chuẩn"), item("ABS", "ABS"), item("SPECIAL", "Đặc biệt"));
    }

    public List<ReferenceDataItemResponse> manufactureYears() {
        return IntStream.rangeClosed(2015, 2026)
                .mapToObj(year -> item(String.valueOf(year), String.valueOf(year)))
                .toList();
    }

    public List<ReferenceDataItemResponse> vehicleColors() {
        return List.of(item("BLACK", "Đen"), item("WHITE", "Trắng"), item("RED", "Đỏ"), item("BLUE", "Xanh"));
    }

    public List<ReferenceDataItemResponse> valuationDeductionFactors() {
        return List.of(
                item("SCRATCH", "Trầy xước", "-3%"),
                item("DENTED", "Móp méo", "-5%"),
                item("REPAINTED", "Sơn lại", "-4%"),
                item("MISSING_DOCUMENT", "Thiếu giấy tờ", "-8%"),
                item("REPAIRED", "Xe đã sửa chữa", "-6%"),
                item("HIGH_ODO", "ODO cao (>50,000km)", "-7%")
        );
    }

    private ReferenceDataItemResponse item(String code, String name) {
        return item(code, name, null);
    }

    private ReferenceDataItemResponse item(String code, String name, String description) {
        return new ReferenceDataItemResponse(code, name, description);
    }
}
