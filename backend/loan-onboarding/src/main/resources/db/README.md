# Flyway Database Resources

Folder này giữ cùng cách đặt tên với tài liệu database do BA/DA bàn giao.

```text
db/
├── migration/   # DDL/schema migration, giữ V1..V20 theo database/migrations và cập nhật theo dataflow mới
└── seed/        # Dữ liệu seed, giữ V1..V11 theo database/seed
```

## Cách chạy trong backend

Không dùng Spring Boot Flyway auto-config để đọc đồng thời cả hai folder, vì Flyway dùng chung version trên mọi location. Nếu để auto-config đọc `db/migration` và `db/seed` cùng lúc thì `migration/V1` sẽ trùng version với `seed/V1`.

Backend chạy Flyway theo chặng trong `DatabaseMigrationConfig`:

| Bước | Folder      | Target | Lý do                                                              |
| ---- | ----------- | -----: | ------------------------------------------------------------------ |
| 1    | `migration` |   `V1` | Tạo schema lõi                                                     |
| 2    | `seed`      |   `V1` | Seed lifecycle state/transition                                    |
| 3    | `migration` |   `V4` | Thêm loan term/purpose enum, branch, vehicle catalog, asset        |
| 4    | `internal`  |    `-` | Mở tạm status `BLACKLIST` để chạy seed demo đúng file BA/DA        |
| 5    | `seed`      |   `V3` | Seed demo V2 và vehicle catalog khi asset còn `customer_id`         |
| 6    | `migration` |   `V7` | Link asset vào loan application, valuation và status BLACKLIST     |
| 7    | `seed`      |   `V4` | Seed asset deduction type                                          |
| 8    | `migration` |   `V8` | Thêm loan purpose catalog và loan_purpose_id                       |
| 9    | `seed`      |   `V5` | Seed loan purpose                                                  |
| 10   | `migration` |   `V9` | Thêm loan term catalog                                             |
| 11   | `seed`      |   `V6` | Seed loan term                                                     |
| 12   | `migration` |  `V10` | Bổ sung customer status `LEAD` theo cập nhật BA/DA                 |
| 13   | `migration` |  `V11` | Thêm loan product catalog và các bảng mapping                      |
| 14   | `seed`      |   `V7` | Seed loan product và score grade                                   |
| 15   | `migration` |  `V12` | Thêm người tham chiếu của hồ sơ vay                                |
| 16   | `migration` |  `V13` | Thêm snapshot thông tin sơ bộ khách hàng trên hồ sơ vay            |
| 17   | `migration` |  `V14` | Thêm thông tin customer, bank, occupation và field bổ sung hồ sơ    |
| 18   | `seed`      |   `V8` | Seed bank và occupation                                            |
| 19   | `migration` |  `V15` | Thêm số khung, số máy, ngày cấp đăng ký xe cho asset               |
| 20   | `migration` |  `V16` | Thêm mock score grade rule theo tài liệu DA/BA                     |
| 21   | `seed`      |   `V9` | Seed mock score grade rule                                         |
| 22   | `migration` |  `V17` | Thêm document_type và loan_application_document                    |
| 23   | `seed`      |  `V10` | Seed document_type                                                 |
| 24   | `migration` |  `V19` | Thêm kyc_profile, loan_product_id và income_source                 |
| 25   | `seed`      |  `V11` | Seed income_source                                                 |
| 26   | `migration` |  `V20` | Thêm `registration_certificate_number` vào asset theo BA/DA V20    |
| 27   | `migration` |  `V21` | Thêm loan application draft step flow                              |
| 28   | `seed`      |  `V12` | Seed loan application step catalog                                 |
| 29   | `seed`      |  `V13` | Seed demo draft flow theo BA/DA                                    |
| 30   | `migration` |  `V22` | Đơn giản hóa draft step flow                                       |
| 31   | `migration` |  `V23` | Bỏ các field KYC không còn dùng                                    |
| 32   | `migration` |  `V24` | Refine review flow với `requires_review`                           |
| 33   | `seed`      |  `V14` | Seed draft review flow mới                                        |

Ghi chú: hai file BA/DA `V13_add_additional_customer_info_and_additional_loan_info.sql`
và `V14_add_extra_fields_customer_and_loan_application.sql` không đúng chuẩn tên Flyway
versioned migration. Nội dung của chúng đã được chuẩn hóa vào
`migration/V14__add_customer_bank_occupation_extra_fields.sql` để tránh trùng version.

## Schema history

- Migration dùng bảng `flyway_schema_history`.
- Seed dùng bảng `flyway_seed_schema_history`.
- Seed baseline ở version `0` để Flyway có thể tạo seed history trên schema đã có bảng từ migration, nhưng vẫn chạy đủ `seed/V1` trở lên.

Không đổi nội dung hoặc version file đã chạy trên database dùng chung nếu chưa thống nhất với team.
