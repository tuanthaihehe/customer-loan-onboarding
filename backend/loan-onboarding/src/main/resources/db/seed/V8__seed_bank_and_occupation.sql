-- Customer Loan Onboarding - Seed Bank and Occupation
-- PostgreSQL dialect
-- Version: V8 seed
--
-- Scope:
-- - Seed bank catalog.
-- - Seed occupation catalog.
--
-- This file is idempotent and can be run multiple times safely.
-- It must be run after the migration that creates bank and occupation tables.

INSERT INTO bank (
    code,
    name,
    short_name,
    is_active,
    sort_order
)
VALUES
    ('VCB', 'Ngân hàng TMCP Ngoại thương Việt Nam', 'Vietcombank', TRUE, 10),
    ('BIDV', 'Ngân hàng TMCP Đầu tư và Phát triển Việt Nam', 'BIDV', TRUE, 20),
    ('CTG', 'Ngân hàng TMCP Công Thương Việt Nam', 'VietinBank', TRUE, 30),
    ('TCB', 'Ngân hàng TMCP Kỹ Thương Việt Nam', 'Techcombank', TRUE, 40),
    ('MB', 'Ngân hàng TMCP Quân đội', 'MB Bank', TRUE, 50),
    ('ACB', 'Ngân hàng TMCP Á Châu', 'ACB', TRUE, 60),
    ('VPB', 'Ngân hàng TMCP Việt Nam Thịnh Vượng', 'VPBank', TRUE, 70),
    ('TPB', 'Ngân hàng TMCP Tiên Phong', 'TPBank', TRUE, 80),
    ('VIB', 'Ngân hàng TMCP Quốc tế Việt Nam', 'VIB', TRUE, 90),
    ('STB', 'Ngân hàng TMCP Sài Gòn Thương Tín', 'Sacombank', TRUE, 100),
    ('SHB', 'Ngân hàng TMCP Sài Gòn - Hà Nội', 'SHB', TRUE, 110),
    ('EIB', 'Ngân hàng TMCP Xuất Nhập khẩu Việt Nam', 'Eximbank', TRUE, 120),
    ('HDB', 'Ngân hàng TMCP Phát triển Thành phố Hồ Chí Minh', 'HDBank', TRUE, 130),
    ('MSB', 'Ngân hàng TMCP Hàng Hải Việt Nam', 'MSB', TRUE, 140),
    ('OCB', 'Ngân hàng TMCP Phương Đông', 'OCB', TRUE, 150)
ON CONFLICT (code) DO UPDATE
SET
    name = EXCLUDED.name,
    short_name = EXCLUDED.short_name,
    is_active = EXCLUDED.is_active,
    sort_order = EXCLUDED.sort_order;

INSERT INTO occupation (
    code,
    name,
    description,
    is_active,
    sort_order
)
VALUES
    ('OFFICE_WORKER', 'Nhân viên văn phòng', 'Khách hàng làm việc tại văn phòng, công ty hoặc tổ chức.', TRUE, 10),
    ('BUSINESS_OWNER', 'Chủ kinh doanh', 'Khách hàng là chủ hộ kinh doanh, cửa hàng hoặc doanh nghiệp nhỏ.', TRUE, 20),
    ('SELF_EMPLOYED', 'Tự kinh doanh', 'Khách hàng tự kinh doanh hoặc làm việc độc lập có thu nhập tự khai.', TRUE, 30),
    ('FREELANCER', 'Lao động tự do', 'Khách hàng làm việc tự do, không có hợp đồng lao động cố định.', TRUE, 40),
    ('WORKER', 'Công nhân', 'Khách hàng làm công nhân tại nhà máy, xưởng hoặc khu công nghiệp.', TRUE, 50),
    ('DRIVER', 'Tài xế', 'Khách hàng làm tài xế công nghệ, tài xế taxi, xe tải hoặc vận tải.', TRUE, 60),
    ('SALES_STAFF', 'Nhân viên bán hàng', 'Khách hàng làm nhân viên bán hàng, tư vấn bán hàng hoặc kinh doanh.', TRUE, 70),
    ('SERVICE_STAFF', 'Nhân viên dịch vụ', 'Khách hàng làm trong lĩnh vực dịch vụ, nhà hàng, khách sạn hoặc chăm sóc khách hàng.', TRUE, 80),
    ('TEACHER', 'Giáo viên', 'Khách hàng làm giáo viên, giảng viên hoặc công việc đào tạo.', TRUE, 90),
    ('HEALTHCARE_WORKER', 'Nhân viên y tế', 'Khách hàng làm bác sĩ, y tá, điều dưỡng hoặc công việc y tế.', TRUE, 100),
    ('GOVERNMENT_EMPLOYEE', 'Cán bộ công chức', 'Khách hàng làm trong cơ quan nhà nước hoặc đơn vị hành chính sự nghiệp.', TRUE, 110),
    ('STUDENT', 'Sinh viên', 'Khách hàng đang là sinh viên hoặc học viên.', TRUE, 120),
    ('RETIRED', 'Đã nghỉ hưu', 'Khách hàng đã nghỉ hưu.', TRUE, 130),
    ('UNEMPLOYED', 'Không có việc làm', 'Khách hàng hiện chưa có việc làm.', TRUE, 140),
    ('OTHER', 'Khác', 'Nghề nghiệp khác không thuộc các nhóm trên.', TRUE, 999)
ON CONFLICT (code) DO UPDATE
SET
    name = EXCLUDED.name,
    description = EXCLUDED.description,
    is_active = EXCLUDED.is_active,
    sort_order = EXCLUDED.sort_order;
