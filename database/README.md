# Database

Thư mục này hiện chỉ là placeholder.

## Trạng thái hiện tại

Chưa tạo schema/migration thật vì ERD và DB schema chưa được chốt.

## Vì sao không tạo DB sớm?

Nếu tự tạo bảng/entity trước, backend có thể lệch với thiết kế chính thức của team ERD/BA. Hiện tại project đi theo hướng API-first/mock-first.

## Khi nào dùng thư mục này?

Chỉ thêm file vào đây sau khi có:

- ERD chính thức;
- danh sách bảng/field;
- khóa chính/khóa ngoại;
- quy ước naming;
- quyết định dùng Flyway/Liquibase hay cách migration khác.
