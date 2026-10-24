# Import từ vựng bằng AI

Vào **Học liệu của tôi → Từ vựng → chọn bộ → tab Từ → Thêm từ** (USER và ADMIN đều dùng được). Form mặc định **Nhập thủ công**: tự điền từ/nghĩa, các mục phụ có thể trống, bấm Lưu từ không gọi Groq. Chọn **Nhập bằng AI** để dùng ảnh/PDF/TXT hoặc danh sách từ. Nút Thêm từ ở trang học mở cùng form. Chỉ người tạo xem và quản lý học liệu; xem [PRIVATE_WORKSPACE.md](PRIVATE_WORKSPACE.md).

## Luồng giao diện

1. **Nguồn từ:** chọn ảnh PNG/JPEG, PDF hoặc TXT UTF-8; có thể nhập danh sách từ sẵn có thay cho file.
2. **Chọn từ và aim:** chỉ giữ từ/cụm tiếng Anh để học, bỏ nghĩa tiếng Việt trong tài liệu, số, nhãn câu và phần nhiễu. Có thể sửa lỗi nhận diện, bỏ chọn từ hoặc chọn tối đa 30 từ đầu. Từ đã có trong bộ được bỏ chọn và không tự ghi đè.
3. **Duyệt và thêm:** AI đề xuất các nghĩa cần ưu tiên cho aim, từ loại, ví dụ tiếng Anh đơn giản, dịch ví dụ, đồng nghĩa, trái nghĩa, collocation theo nghĩa và họ từ. Chuyển giữa từng từ để sửa và đánh dấu đã duyệt. Sửa trường làm mất dấu duyệt của từ đó; chỉ lưu khi toàn bộ từ được chọn đã được duyệt.

Mọi trường bổ sung có thể trống. Nghĩa và từ loại là dữ liệu cốt lõi; không ép AI bịa trái nghĩa/họ từ/collocation. Bộ lọc họ từ bỏ các biến thể chia động từ đơn giản. Nội dung AI vẫn cần người biên soạn kiểm tra nghĩa, ví dụ và cách dùng.

Aim là định hướng học tập, **không phải bảng từ vựng theo điểm chính thức của ETS**. Khi biên soạn aim 650, AI ưu tiên nghĩa thông dụng phù hợp và gán mốc học gợi ý ≤650. Trang học đã bỏ thanh chọn aim và hiển thị toàn bộ nghĩa được biên soạn; aim chỉ còn dùng trong bước sinh/biên tập nội dung. Flashcard và Luyện nhớ không gọi AI hay tự bổ sung nghĩa mới. Có thể sửa từ để bổ sung nghĩa và mốc học sau này.

## Đọc tài liệu

- File tối đa 10 MB. PDF đọc từ 1 đến 5 trang mỗi lần, mặc định trang 1–3. Kết quả báo rõ phạm vi và tổng số trang; không giả vờ đã xử lý toàn tài liệu.
- PDF có văn bản: Apache PDFBox đọc chữ rồi Groq lọc từ. Trang ít/không có chữ: render thành ảnh rồi gọi Groq Vision. Với trang trộn nhiều chữ và hình, nội dung nằm trong ảnh có thể cần nhập lại dưới dạng ảnh riêng.
- PNG/JPEG: kiểm tra định dạng thực, tối đa 20 megapixel, chuẩn hóa ảnh trước khi gọi Vision.
- TXT: UTF-8, tối đa 30.000 ký tự; không nhận file nhị phân đổi đuôi thành TXT.
- Tối đa 80 từ được trích, chọn tối đa 30 từ để soạn một lần. Frontend gọi sinh nội dung theo nhóm ba từ; backend nhận tối đa sáu từ/yêu cầu để kiểm soát kích thước phản hồi. Nếu một nhóm AI lỗi, những nhóm đã thành công được giữ để duyệt.
- Nội dung cần nhận diện được gửi tới Groq. File gốc không được lưu lên Cloudinary hoặc vào database. Bản đề xuất nằm trong bộ nhớ màn hình, mất khi rời/tải lại trang; có cảnh báo khi đóng/tải lại sau khi đã sinh nội dung.

## Cấu hình

Trong `toeic_api/local.properties` hoặc biến môi trường:

```properties
GROQ_API_KEY=<API key riêng>
GROQ_MODEL=openai/gpt-oss-20b
GROQ_VOCABULARY_MODEL=openai/gpt-oss-120b
GROQ_VISION_MODEL=qwen/qwen3.8-27b
```

Model lời giải câu hỏi, từ vựng và nhận diện ảnh được cấu hình riêng. Từ vựng mặc định dùng GPT-OSS 120B: trong mẫu đối chiếu invoice/charge, nghĩa tiếng Việt và cách dùng chính xác hơn mẫu 20B đã thử. Có thể cấu hình GROQ_VOCABULARY_MODEL thành model văn bản khác theo ngân sách/quota. Đã đối chiếu model từ vựng và Vision với danh sách model mà tài khoản local có quyền truy cập. Hướng dẫn chính thức: [Groq Vision](https://console.groq.com/docs/vision), [Qwen 3.8](https://console.groq.com/docs/model/qwen/qwen3.8-27b), [Apache PDFBox](https://pdfbox.apache.org/download.cgi).

Client có timeout, giới hạn đồng thời, kiểm tra JSON/truncation và ánh xạ lỗi cấu hình/quota. Không đưa API key vào frontend, prompt, lỗi hoặc log. Kết quả sinh phải khớp từ được yêu cầu, đúng cấu trúc, nghĩa tiếng Việt, có ví dụ và mốc không vượt aim. Đây là kiểm tra định dạng/ngôn ngữ, không thay thế việc duyệt độ chính xác học thuật.

## Database và lưu

V3 thêm ba bảng, không sửa V1/V2:

| Bảng | Vai trò |
| --- | --- |
| `vocabulary_senses` | Nghĩa/từ loại, mốc ưu tiên theo aim, ví dụ, bản dịch và ghi chú |
| `vocabulary_sense_phrases` | SYNONYM, ANTONYM, COLLOCATION theo từng nghĩa |
| `vocabulary_word_family` | Họ từ, từ loại và nghĩa tùy chọn |

V3 tạo một sense từ dữ liệu nghĩa/ví dụ cũ, để mốc aim NULL. Bảng đồng nghĩa cũ được giữ; các cột nghĩa đầu tiên trên word tiếp tục phục vụ hợp đồng API cũ. Từ vẫn tái sử dụng giữa nhiều bộ qua `vocabulary_word_sets`, không nhân bản vì chọn aim khác.

Chỉ bước xác nhận mới ghi từ, nghĩa và các quan hệ. Toàn bộ danh sách được lưu trong một transaction; lỗi bất kỳ từ nào sẽ rollback. Khóa bộ nhận trong lúc import để tuần tự hóa các lần import cùng bộ, kiểm tra trùng trước khi thêm. Không tự sửa từ đã có. Các API thêm/sửa từ thủ công cũ vẫn được giữ.

Đã áp dụng V3 trên MySQL `toeic_clean`, Hibernate validate thành công. Metadata sau V3: **31 bảng, 210 cột**. Danh sách field: [DATABASE_SCHEMA.md](DATABASE_SCHEMA.md).

## API

| Endpoint người đăng nhập | Input | Kết quả |
| --- | --- | --- |
| `POST /api/v1/workspace/vocabulary/import/extract` | Multipart `file`, `fromPage`, `toPage` | `{terms,totalPages,fromPage,toPage,limited}` |
| `POST /api/v1/workspace/vocabulary/import/generate` | `{targetScore,terms:[...]}` | Danh sách đề xuất chứa `word,pronunciation,senses,family`; chưa có ID đích |
| `POST /api/v1/workspace/vocabulary/import/confirm` | `{setId,reviewed:true,words:[...]}` | Các từ đã lưu với ID, senses và family |

Alias `/api/v1/admin/vocabulary/import/*` vẫn dành cho ADMIN. Cả hai đường dẫn đều kiểm tra chủ bộ nhận khi xác nhận.

Một sense gồm `meaning,partOfSpeech,minimumTargetScore?,exampleSentence?,exampleTranslation?,studyNote?,synonyms?,antonyms?,collocations?`. Family gồm `word,partOfSpeech?,meaning?`. Mảng phụ có thể rỗng. Body xác nhận mỗi từ có `setId` khớp bộ nhận và các trường gốc tương thích `meaning,partOfSpeech`; frontend lấy chúng từ sense đầu tiên.

## Kiểm chứng

- 63 test backend: migration từ V2, bảo toàn nghĩa/đồng nghĩa cũ, phân tách sense/quan hệ/family, cập nhật giảm số nghĩa, trường phụ trống, quyền admin, bắt buộc xác nhận, trùng từ, rollback batch, PDF văn bản/scan/ảnh/TXT, lọc nhiễu, kiểm tra AI và lỗi provider.
- 7 test frontend đạt, lint và build đạt; kiểm tra lọc nghĩa theo aim và payload tương thích.
- Đã gọi qua backend + Groq thật với ảnh song ngữ, PDF văn bản và PDF scan. Đã duyệt/sửa kết quả thật trên Chrome desktop và điện thoại 390px; không tràn ngang. Xác minh sửa nội dung làm mất dấu duyệt, nút lưu bị khóa trước khi duyệt đủ.
- Bước lưu của kiểm tra trình duyệt được chặn bằng fixture để không ghi từ mẫu vào MySQL. Transaction ghi dữ liệu được kiểm tra bằng integration test H2; chưa benchmark tài liệu lớn hoặc chạy test ghi đồng thời đầy đủ trên MySQL.
