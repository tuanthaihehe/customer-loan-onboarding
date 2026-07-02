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
| 2    | `seed`      |   `V2` | Seed lifecycle và demo loan application khi còn cột `loan_purpose` |
| 3    | `migration` |   `V4` | Thêm loan term/purpose enum, branch, vehicle catalog, asset        |
| 4    | `seed`      |   `V3` | Seed vehicle catalog và asset trước khi bỏ `asset.customer_id`     |
| 5    | `migration` |   `V6` | Link asset vào loan application, thêm valuation/deduction schema   |
| 6    | `seed`      |   `V4` | Seed asset deduction type                                          |
| 7    | `migration` |   `V8` | Update customer status enum và thêm loan purpose catalog           |
| 8    | `seed`      |   `V5` | Seed loan purpose                                                  |
| 9    | `migration` |   `V9` | Thêm loan term catalog                                             |
| 10   | `seed`      |   `V6` | Seed loan term                                                     |
| 11   | `migration` |  `V10` | Bổ sung customer status `LEAD` theo cập nhật BA/DA                 |
| 12   | `migration` |  `V11` | Thêm loan product catalog và các bảng mapping                      |
| 13   | `seed`      |   `V7` | Seed loan product và score grade                                   |
| 14   | `migration` |  `V12` | Thêm người tham chiếu của hồ sơ vay                                |
| 15   | `migration` |  `V13` | Thêm snapshot thông tin sơ bộ khách hàng trên hồ sơ vay            |
| 16   | `migration` |  `V14` | Thêm thông tin customer, bank, occupation và field bổ sung hồ sơ    |
| 17   | `seed`      |   `V8` | Seed bank và occupation                                            |
| 18   | `migration` |  `V15` | Thêm số khung, số máy, ngày cấp đăng ký xe cho asset               |
| 19   | `migration` |  `V16` | Thêm mock score grade rule theo tài liệu DA/BA                     |
| 20   | `seed`      |   `V9` | Seed mock score grade rule                                         |
| 21   | `migration` |  `V17` | Thêm document_type và loan_application_document                    |
| 22   | `seed`      |  `V10` | Seed document_type                                                 |
| 23   | `migration` |  `V19` | Thêm kyc_profile, loan_product_id và income_source                 |
| 24   | `seed`      |  `V11` | Seed income_source                                                 |
| 25   | `migration` |  `V20` | Thêm số đăng ký xe vào asset theo dataflow bổ sung tài sản         |

Ghi chú: hai file BA/DA `V13_add_additional_customer_info_and_additional_loan_info.sql`
và `V14_add_extra_fields_customer_and_loan_application.sql` không đúng chuẩn tên Flyway
versioned migration. Nội dung của chúng đã được chuẩn hóa vào
`migration/V14__add_customer_bank_occupation_extra_fields.sql` để tránh trùng version.

## Schema history

- Migration dùng bảng `flyway_schema_history`.
- Seed dùng bảng `flyway_seed_schema_history`.
- Seed baseline ở version `0` để Flyway có thể tạo seed history trên schema đã có bảng từ migration, nhưng vẫn chạy đủ `seed/V1` trở lên.

Không đổi nội dung hoặc version file đã chạy trên database dùng chung nếu chưa thống nhất với team.
