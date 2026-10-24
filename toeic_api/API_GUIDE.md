# API backend hiện tại

Nguồn tài liệu hiện hành: [BACKEND_ARCHITECTURE.md](../BACKEND_ARCHITECTURE.md).

Backend đã chuyển sang ngân hàng nội dung chung, progress có version/bookmark độc lập và attempt có snapshot. Tài liệu trên mô tả endpoint, cấu hình, migration legacy, cấu trúc response và giới hạn kiểm chứng.

OpenAPI được sinh từ controller ở `/v3/api-docs`, giao diện tại `/swagger-ui.html` khi backend chạy.

Các endpoint admin yêu cầu ADMIN. API hồ sơ/progress/kết quả dùng danh tính JWT; không lấy userId người khác từ query để đọc/ghi. API đang làm bài không trả đáp án; admin có endpoint đọc nội dung riêng.

Mục luyện tập dùng `/api/v1/{listening,reading}/sections/{partNumber}` để liệt kê và `/questions/section/{sectionId}` để lấy câu hỏi. CRUD quản trị dùng `/api/v1/admin/{listening,reading}/sections`, body `{partId,name,description,audioUrl?}`. Các request câu hỏi/bài đọc, summary và response dùng `sectionId`. Không còn field số level; mỗi Part có thể có nhiều mục có tên và mô tả riêng.
