# Học liệu riêng của người dùng

Từ 24/10/2026, mỗi tài khoản có không gian biên soạn tại **Học liệu của tôi** (`/workspace`). Học liệu chỉ hiển thị với người tạo. Vai trò ADMIN không bỏ qua kiểm tra chủ sở hữu.

| Thành phần | USER | ADMIN |
| --- | --- | --- |
| Danh mục Part 1–7 | Xem, chọn Part | Xem, cập nhật mô tả Part mặc định; không xóa Part |
| Mục luyện Listening/Reading | Tạo, xem, sửa, lưu trữ của mình | Tương tự, chỉ học liệu của mình |
| Ngữ cảnh, bài đọc, audio, câu hỏi, đáp án | Soạn riêng, thêm nhiều câu dùng chung ngữ cảnh hoặc độc lập | Tương tự |
| Category, Set, từ vựng | Tạo, sửa, lưu trữ của mình | Tương tự |
| Thêm từ thủ công | Có; mặc định khi bấm Thêm từ, không gọi Groq | Có |
| Nhập từ ảnh/PDF/TXT bằng AI | Có; chọn từ, aim, sửa và duyệt trước khi lưu | Có |
| File ảnh/audio tải lên | Chỉ chủ file đọc được | Chỉ chủ file đọc được |
| Quản trị tài khoản | Không | Có |

## Cách sử dụng

1. Vào **Học liệu của tôi → Listening/Reading → Mục luyện tập**, chọn Part và tạo mục có tên/mô tả.
2. Sang **Câu hỏi**, chọn mục rồi bấm **Thêm câu hỏi**. Trình soạn dùng chung hỗ trợ bài đọc/audio và nhiều câu; Part 6 dùng một bài đọc cho bốn câu. Dấu cộng chọn thêm cùng ngữ cảnh hoặc tạo ngữ cảnh độc lập.
3. Vào **Từ vựng**, tạo Category → Set → Thêm từ. **Nhập thủ công** cho phép tự điền từ/nghĩa; ví dụ, họ từ và các quan hệ là tùy chọn. **Nhập bằng AI** giữ luồng trích từ và duyệt đề xuất trước khi thêm.
4. Vào **Nghe/Đọc/Từ vựng** trên thanh điều hướng để học nội dung của mình.

## Quyền sở hữu và API

- Backend lấy người tạo từ JWT đã xác thực; request không được chỉ định/chuyển người sở hữu.
- `question_groups`, `practice_sets`, `vocabulary_words`, `test_suites` có `owner_id` tham chiếu `users.id`, kèm index cho truy vấn theo tài khoản và trạng thái archive.
- Category dùng `created_by` hiện có làm chủ sở hữu. Set kế thừa quyền từ Category; câu hỏi/document kế thừa từ group. Không nhân đôi cột chủ sở hữu ở các bảng con.
- Một câu/từ chỉ được gắn vào bộ của cùng tài khoản. Cùng tài khoản vẫn có thể tái sử dụng giữa nhiều bộ.
- Tên Category duy nhất trong một tài khoản: `UNIQUE(created_by, category_name)`. Hai tài khoản có thể đặt cùng tên Category.
- List, chi tiết, sửa, xóa, membership, summary và lời giải với ngữ cảnh đã lưu đều kiểm tra chủ sở hữu. ID không thuộc tài khoản được xử lý như không tồn tại (404).
- USER dùng `/api/v1/workspace/{listening,reading,content,vocabulary,...}`. Alias `/api/v1/admin/...` được giữ cho giao diện ADMIN và vẫn yêu cầu ADMIN. Các thao tác ghi Part yêu cầu ADMIN ở controller và service.
- API học hiện có (`/reading`, `/listening`, `/vocabulary`) cũng trả học liệu của chính người đăng nhập. Category không còn endpoint công khai.
- Các bộ đề và lịch sử thi cũng được giới hạn theo chủ sở hữu để không lộ câu hỏi/ngữ liệu riêng qua module thi. Lần thay đổi này không mở quyền tạo bộ đề cho USER.

## File cá nhân

`POST /api/v1/workspace/uploads` nhận ảnh/audio và trả đường dẫn `/api/v1/workspace/media/{id}`. Database lưu metadata trong `private_media`; dữ liệu file nằm trong `MEDIA_STORAGE_PATH`, mặc định `./.private-media` tính theo thư mục chạy backend. Ảnh tối đa 10 MB, audio tối đa 50 MB; server kiểm tra chữ ký định dạng.

GET media yêu cầu JWT của chủ file, dùng `Cache-Control: no-store`. Frontend tải qua httpClient có Authorization, tạo blob URL và giải phóng khi thôi dùng. Không gắn JWT vào URL. Khi lưu ngữ liệu, backend cũng kiểm tra các tham chiếu media riêng.

Khi triển khai, đặt `MEDIA_STORAGE_PATH` vào volume bền vững, sao lưu cùng database và không expose thư mục đó bằng static server. Với nhiều backend instance, cần kho file chung có kiểm tra quyền tương ứng. File không bị xóa khi archive câu để giữ tài liệu trong snapshot lịch sử. URL ngoài hoặc file từng công khai qua Cloudinary trước đây giữ chính sách truy cập của nhà cung cấp; thay đổi này bảo vệ các file tải lên bằng luồng mới.

## Migration và dữ liệu cũ

V4 bổ sung quyền sở hữu và đổi ràng buộc tên Category; V5 tạo metadata file riêng. V1/V2/V3 không thay đổi. Đã áp dụng lên MySQL `toeic_clean`, Hibernate validate thành công: **32 bảng, 221 cột**.

Những bản ghi cũ không có bằng chứng xác định người tạo giữ `owner_id`/`created_by = NULL` và không được hiển thị trong học liệu. Không xóa nội dung, ID, membership hay lịch sử và không tự gán cho Admin hoặc người đăng nhập đầu tiên. Nếu cần khôi phục hiển thị, phải có danh sách xác nhận người sở hữu rồi cập nhật nhất quán group/set/word/suite và các membership liên quan. Category cũ có `created_by` hợp lệ vẫn thuộc người đó; từ cũ chưa xác định chủ sở hữu vẫn bị ẩn.

## Kiểm chứng

71 test backend đạt, gồm tạo bài Reading/Listening bằng USER, Part chỉ đọc, nhập từ thủ công/xác nhận batch, chặn User khác và Admin, kiểm tra membership, media riêng, lời giải AI theo chủ sở hữu và migration bảo toàn dữ liệu. 11 test frontend, lint và build đạt. Chrome desktop/mobile kiểm tra tài khoản USER, form thủ công mặc định lưu không gọi AI, chuyển luồng AI, tạo mục, mở trình soạn câu hỏi và không có tab quản lý Part.
