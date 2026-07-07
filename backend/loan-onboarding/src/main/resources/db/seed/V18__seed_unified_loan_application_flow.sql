-- Customer Loan Onboarding - Seed Unified Loan Application Flow
-- PostgreSQL dialect
-- Version: V18 seed
--
-- Scope:
-- - Canonicalize loan_application lifecycle states after removing
--   loan_application_draft.
-- - Seed allowed transitions from pre-submission onboarding through the
--   existing processing lifecycle.

UPDATE loan_application_state
SET is_initial = FALSE
WHERE is_initial = TRUE;

INSERT INTO loan_application_state
(id, code, name, description, is_initial, is_terminal, sort_order)
VALUES
    (
        '00000000-0000-0000-0000-000000000101',
        'APP_CREATED',
        'Đã tạo',
        'Hồ sơ vay đã được tạo và chưa bắt đầu hoàn thiện thông tin.',
        TRUE,
        FALSE,
        1
    ),
    (
        '00000000-0000-0000-0000-000000000108',
        'APP_IN_PROGRESS',
        'Đang hoàn thiện',
        'Hồ sơ vay đang được bổ sung và hoàn thiện thông tin theo các bước onboarding.',
        FALSE,
        FALSE,
        2
    ),
    (
        '00000000-0000-0000-0000-000000000109',
        'APP_COMPLETED',
        'Hoàn thành',
        'Hồ sơ vay đã hoàn thành thông tin onboarding và sẵn sàng nộp vào luồng xử lý.',
        FALSE,
        FALSE,
        3
    ),
    (
        '00000000-0000-0000-0000-000000000102',
        'APP_SUBMITTED',
        'Đã nộp hồ sơ',
        'Hồ sơ đã được gửi vào luồng xử lý.',
        FALSE,
        FALSE,
        4
    ),
    (
        '00000000-0000-0000-0000-000000000103',
        'APP_NEEDS_SUPPLEMENT',
        'Cần bổ sung hồ sơ',
        'Hồ sơ thiếu thông tin hoặc giấy tờ và cần được bổ sung.',
        FALSE,
        FALSE,
        5
    ),
    (
        '00000000-0000-0000-0000-000000000104',
        'APP_IN_REVIEW',
        'Đang thẩm định/phê duyệt',
        'Hồ sơ đang được kiểm tra, thẩm định hoặc phê duyệt.',
        FALSE,
        FALSE,
        6
    ),
    (
        '00000000-0000-0000-0000-000000000105',
        'APP_READY_FOR_CONTRACT',
        'Sẵn sàng lập hợp đồng',
        'Hồ sơ đã đủ điều kiện để tạo hợp đồng.',
        FALSE,
        FALSE,
        7
    ),
    (
        '00000000-0000-0000-0000-000000000106',
        'APP_CONTRACTED',
        'Đã có hợp đồng',
        'Hồ sơ đã được tạo hợp đồng và kết thúc lifecycle trong module hiện tại.',
        FALSE,
        TRUE,
        8
    ),
    (
        '00000000-0000-0000-0000-000000000107',
        'APP_CANCELLED',
        'Hồ sơ bị hủy',
        'Hồ sơ bị hủy và không tiếp tục xử lý.',
        FALSE,
        TRUE,
        9
    ),
    (
        '00000000-0000-0000-0000-000000000110',
        'APP_EXPIRED',
        'Hồ sơ hết hạn',
        'Hồ sơ vay hết hạn hoàn thiện và không tiếp tục xử lý.',
        FALSE,
        TRUE,
        10
    )
ON CONFLICT (code) DO UPDATE
SET
    name = EXCLUDED.name,
    description = EXCLUDED.description,
    is_initial = EXCLUDED.is_initial,
    is_terminal = EXCLUDED.is_terminal,
    sort_order = EXCLUDED.sort_order;

DELETE FROM loan_application_state_transition;

INSERT INTO loan_application_state_transition
(id, from_state_id, to_state_id, action_code, action_name, description)
VALUES
    (
        '00000000-0000-0000-0000-000000000211',
        '00000000-0000-0000-0000-000000000101',
        '00000000-0000-0000-0000-000000000108',
        'START_COMPLETION',
        'Bắt đầu hoàn thiện',
        'Bắt đầu bổ sung thông tin cho hồ sơ vay.'
    ),
    (
        '00000000-0000-0000-0000-000000000212',
        '00000000-0000-0000-0000-000000000108',
        '00000000-0000-0000-0000-000000000109',
        'COMPLETE_APPLICATION',
        'Hoàn thành hồ sơ',
        'Hoàn thành thông tin onboarding của hồ sơ vay.'
    ),
    (
        '00000000-0000-0000-0000-000000000213',
        '00000000-0000-0000-0000-000000000109',
        '00000000-0000-0000-0000-000000000102',
        'SUBMIT',
        'Nộp hồ sơ',
        'Nộp hồ sơ đã hoàn thành vào luồng xử lý.'
    ),
    (
        '00000000-0000-0000-0000-000000000214',
        '00000000-0000-0000-0000-000000000101',
        '00000000-0000-0000-0000-000000000107',
        'CANCEL',
        'Hủy hồ sơ',
        'Hủy hồ sơ sau khi tạo.'
    ),
    (
        '00000000-0000-0000-0000-000000000215',
        '00000000-0000-0000-0000-000000000108',
        '00000000-0000-0000-0000-000000000107',
        'CANCEL',
        'Hủy hồ sơ',
        'Hủy hồ sơ trong lúc đang hoàn thiện.'
    ),
    (
        '00000000-0000-0000-0000-000000000216',
        '00000000-0000-0000-0000-000000000109',
        '00000000-0000-0000-0000-000000000107',
        'CANCEL',
        'Hủy hồ sơ',
        'Hủy hồ sơ đã hoàn thành nhưng chưa nộp.'
    ),
    (
        '00000000-0000-0000-0000-000000000217',
        '00000000-0000-0000-0000-000000000101',
        '00000000-0000-0000-0000-000000000110',
        'EXPIRE',
        'Đánh dấu hết hạn',
        'Đánh dấu hồ sơ mới tạo là hết hạn.'
    ),
    (
        '00000000-0000-0000-0000-000000000218',
        '00000000-0000-0000-0000-000000000108',
        '00000000-0000-0000-0000-000000000110',
        'EXPIRE',
        'Đánh dấu hết hạn',
        'Đánh dấu hồ sơ đang hoàn thiện là hết hạn.'
    ),
    (
        '00000000-0000-0000-0000-000000000219',
        '00000000-0000-0000-0000-000000000109',
        '00000000-0000-0000-0000-000000000110',
        'EXPIRE',
        'Đánh dấu hết hạn',
        'Đánh dấu hồ sơ đã hoàn thành nhưng chưa nộp là hết hạn.'
    ),
    (
        '00000000-0000-0000-0000-000000000203',
        '00000000-0000-0000-0000-000000000102',
        '00000000-0000-0000-0000-000000000104',
        'START_REVIEW',
        'Bắt đầu thẩm định/phê duyệt',
        'Chuyển hồ sơ đã nộp sang bước thẩm định/phê duyệt.'
    ),
    (
        '00000000-0000-0000-0000-000000000204',
        '00000000-0000-0000-0000-000000000102',
        '00000000-0000-0000-0000-000000000103',
        'REQUEST_SUPPLEMENT',
        'Yêu cầu bổ sung hồ sơ',
        'Yêu cầu bổ sung thông tin hoặc giấy tờ sau khi hồ sơ đã nộp.'
    ),
    (
        '00000000-0000-0000-0000-000000000205',
        '00000000-0000-0000-0000-000000000102',
        '00000000-0000-0000-0000-000000000107',
        'CANCEL',
        'Hủy hồ sơ',
        'Hủy hồ sơ sau khi đã nộp.'
    ),
    (
        '00000000-0000-0000-0000-000000000206',
        '00000000-0000-0000-0000-000000000103',
        '00000000-0000-0000-0000-000000000102',
        'RESUBMIT',
        'Nộp lại hồ sơ',
        'Nộp lại hồ sơ sau khi đã bổ sung thông tin hoặc giấy tờ.'
    ),
    (
        '00000000-0000-0000-0000-000000000207',
        '00000000-0000-0000-0000-000000000104',
        '00000000-0000-0000-0000-000000000103',
        'REQUEST_SUPPLEMENT',
        'Yêu cầu bổ sung hồ sơ',
        'Yêu cầu bổ sung thông tin hoặc giấy tờ trong quá trình thẩm định/phê duyệt.'
    ),
    (
        '00000000-0000-0000-0000-000000000208',
        '00000000-0000-0000-0000-000000000104',
        '00000000-0000-0000-0000-000000000105',
        'DRAFT_CONTRACT',
        'Chuyển sang lập hợp đồng',
        'Hồ sơ đủ điều kiện để chuyển sang bước lập hợp đồng.'
    ),
    (
        '00000000-0000-0000-0000-000000000209',
        '00000000-0000-0000-0000-000000000105',
        '00000000-0000-0000-0000-000000000106',
        'CREATE_CONTRACT',
        'Tạo hợp đồng',
        'Tạo hợp đồng từ hồ sơ vay đã đủ điều kiện.'
    ),
    (
        '00000000-0000-0000-0000-000000000210',
        '00000000-0000-0000-0000-000000000105',
        '00000000-0000-0000-0000-000000000107',
        'CANCEL',
        'Hủy hồ sơ',
        'Hủy hồ sơ trước khi lập hợp đồng.'
    )
ON CONFLICT (from_state_id, to_state_id, action_code) DO NOTHING;
