# TOEICUP frontend

React/Vite/Tailwind, learner và admin có layout riêng. Vite proxy `/api` tới `localhost:8080`; production có thể dùng reverse proxy cùng origin hoặc FRONTEND_URL được backend cho phép qua CORS.

## Hợp đồng backend

- Envelope `{code,message,result}`, code 1000 là thành công.
- Hồ sơ từ `/users/me`. httpClient refresh JWT một lần và phát sự kiện cập nhật phiên; không giữ hai token cũ/mới độc lập.
- Đổi mật khẩu yêu cầu mật khẩu hiện tại. Forgot/reset dùng liên kết có token; không dùng userId làm bằng chứng quyền reset.
- Listening/Reading lấy câu hỏi từ ngân hàng chung và nhận `group` gồm ngữ liệu. Summary theo Part trả số đếm theo mục luyện tập; frontend không tải tất cả câu hỏi để đếm.
- Mục luyện tập hiển thị `name` và `description`, không giới hạn 5 mục mỗi Part. URL luyện là `/listening/:partNumber/sections/:sectionId` hoặc `/reading/:partNumber/sections/:sectionId`; form quản trị tạo/sửa tên và mô tả thay cho số cấp độ.
- Progress có status trả lời và `bookmarked` riêng. Lời giải chỉ được trả khi đã trả lời phiên bản hiện tại; đổi nội dung làm trạng thái hiện tại cần được làm lại.
- OptionList dùng mảng options thực tế, Part 2 có ba lựa chọn. AudioPlayer hỗ trợ đoạn startMs/endMs.
- Trang thi dùng start/resume attempt và snapshot từ server, lưu nháp lựa chọn nối tiếp, deadline từ expiresAt. Reload khôi phục cùng attempt. Part 6/7 hiển thị documents dùng chung.
- Kết quả dùng `{items,page,size,totalElements,totalPages}`. Learner có điều khiển chuyển trang. Review dùng snapshot, hiển thị accuracyPercent.
- `/admin/content` biên soạn group, tài liệu, audio, câu hỏi và tái sử dụng nguyên cụm. Những trang admin hiện hữu là adapter vào cùng ngân hàng nội dung.
- API học không trả đáp án thi; người biên soạn dùng GET riêng dưới `/workspace` hoặc `/admin` để xem câu của chính mình. Backend kiểm tra quyền sở hữu ở cả API đọc và ghi.

## Kiểm tra

Thêm từ tại `/workspace/vocabulary/import?setId=...` (hoặc alias admin): mặc định nhập thủ công, không gọi AI; chọn Nhập bằng AI để chọn ảnh/PDF/TXT → chọn từ và aim → duyệt từng từ. Có thể sửa nghĩa, ví dụ, các quan hệ và họ từ; sửa nội dung làm mất dấu đã duyệt của từ đó. Bước trích và sinh nội dung gọi backend/Groq, chưa ghi từ vào database. Bước xác nhận mới lưu. Chi tiết: [VOCABULARY_IMPORT.md](../VOCABULARY_IMPORT.md).

Trang học từ có ba tab: Flashcard, Luyện nhớ, Danh sách. Mỗi lần hiện một thẻ, click/Enter/Space để lật; nội dung chia Nghĩa, Cách dùng, Liên hệ từ. Tab chuyển focus và mở mục tương ứng, phím mũi tên/Home/End cũng hoạt động, không khóa focus. Nút phát âm và các tab tách khỏi vùng lật thẻ. Không còn thanh aim hay lọc theo aim ở trang học; hiển thị toàn bộ nghĩa đã biên soạn. Luyện nhớ chọn nghĩa chính, lặp lại từ sai đến khi nhớ đủ, có tổng kết và ôn riêng từ từng nhầm. Bộ một từ/không có nghĩa nhiễu dùng tự đánh giá. Giữ lượt học khi chuyển tab; lượt học chỉ tồn tại trong phiên màn hình, chưa đồng bộ tiến độ ôn lên backend. Danh sách có tìm từ/nghĩa và mở đúng thẻ. Thành phần nằm trong `components/vocabulary`; thuật toán lượt học trong `utils/vocabularyStudy.js`.

Kiểm tra UI flashcard: 11 test frontend đạt, lint/build đạt. Chrome desktop/mobile đã kiểm tra click/Space lật thẻ, Tab đổi nội dung, bỏ aim, từ sai được lặp lại, hoàn tất lượt, giữ lượt khi đổi tab, tìm kiếm/trộn thẻ, bộ một từ và bộ trống. Không gọi AI hoặc ghi database cho chế độ học này.

Màn `/admin/content` là trình soạn duy nhất: bắt đầu với một câu, dấu cộng mở hai lựa chọn thêm cùng ngữ cảnh hoặc tạo ngữ cảnh mới độc lập. Chuyển giữa các ngữ cảnh giữ nội dung đang nhập; lưu toàn bộ bằng API batch. Bài đọc/audio nằm ngay trong form, Part 5 ẩn ngữ liệu. Part 6 cần đủ bốn câu, Part 3/4 cần đủ ba câu trước khi lưu. Listening/Reading/Đề thi mở cùng trình soạn khi thêm hoặc sửa, không còn tab Passage hay form thêm câu riêng. Vẫn có xem trước, dán nhiều câu và lời giải AI. Hướng dẫn: [CONTENT_AUTHORING.md](../CONTENT_AUTHORING.md).

`npm.cmd test` kiểm tra parser nhập nhiều câu bằng Node test runner; không cần cài thêm thư viện kiểm thử.

```powershell
npm.cmd run lint
npm.cmd run build
npm.cmd run dev
```

Mô hình dữ liệu, danh sách API và hướng dẫn chuyển database: [BACKEND_ARCHITECTURE.md](../BACKEND_ARCHITECTURE.md).

## Không gian học liệu riêng

`/workspace` nằm trong ProtectedRoute/AppLayout, dùng WorkspaceLayout với ba mục Listening/Reading/Từ vựng. `useAuthoringScope` chọn API và đường dẫn theo workspace/admin; các form biên soạn được tái sử dụng. USER không có tab quản lý Part; backend vẫn chặn thao tác ghi Part. Navbar và trang học có liên kết đến học liệu riêng. `PrivateImage`/`PrivateAudio` đọc file qua Authorization, tạo blob URL và thu hồi khi thôi dùng; không đưa token vào URL.

Đã kiểm tra Chrome bằng tài khoản USER: lưu từ thủ công không gọi AI, chuyển nhập AI, tạo mục, mở trình soạn câu hỏi, ẩn quản lý Part và bố cục mobile. Chính sách và migration: [PRIVATE_WORKSPACE.md](../PRIVATE_WORKSPACE.md).
