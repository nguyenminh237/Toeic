# Biên soạn câu hỏi theo cụm ngữ liệu

Listening, Reading và Đề thi dùng một trình soạn tại `/admin/content`. Nút **Thêm câu hỏi** giữ Part và bộ nhận đang chọn; **Sửa** mở ngữ cảnh chứa câu trong cùng trình soạn. Không còn form thêm câu đơn riêng, nút thêm hàng loạt riêng hay tab tạo Passage.

Màn hình bắt đầu với một câu. Nhấn **+ Thêm câu** để chọn **Cùng ngữ cảnh** (dùng lại bài đọc/audio) hoặc **Ngữ cảnh mới** (một ngữ cảnh trống với câu mới độc lập). Thanh ngữ cảnh phía trên cho chuyển qua lại và giữ nội dung đang nhập. Nút **Lưu câu hỏi** lưu tất cả ngữ cảnh trong một transaction. Có thể bỏ ngữ cảnh mới chưa lưu.

## Soạn Part 6

1. Chọn Part 6. Giao diện bắt đầu với một bài đọc và một câu; dùng **+ → Cùng ngữ cảnh** để thêm đủ bốn câu.
2. Nhập bài đọc một lần; đánh dấu chỗ trống bằng `(1) ____`, `(2) ____`, `(3) ____`, `(4) ____`.
3. Ở mỗi câu bên phải, điền A/B/C/D và chọn đáp án đúng bằng radio. Nhãn “Chỗ trống (n)” liên hệ câu với vị trí trong bài. Có thể nhập lựa chọn là cả câu văn.
4. Lời giải tùy chọn: mở “Lời giải / Tạo bằng AI”. AI nhận ngữ liệu đang soạn, kể cả khi cụm chưa được lưu.
5. Dùng “Xem trước cả cụm” để kiểm tra bài đọc cạnh các câu hỏi.
6. Chọn mục luyện tập hoặc Part đề nếu muốn gắn cả cụm ngay; để trống số câu bắt đầu để tự thêm ở cuối bộ, hoặc chỉ định vị trí khi cần. Số thứ tự trong bộ độc lập với nhãn chỗ trống nội bộ của bài.
7. Nhấn **Lưu câu hỏi**. Server chỉ nhận ngữ cảnh Part 6 đủ bốn câu và một bài đọc; mỗi câu đủ bốn lựa chọn và đúng một đáp án. Không cần tạo Passage trước. Khi đủ bốn câu, vẫn có thể chọn **+ → Ngữ cảnh mới** để nhập bài đọc khác.

## Các Part khác

| Part | Luồng biên soạn |
| --- | --- |
| 1 | Ảnh và audio ở ngữ liệu chung; các lựa chọn ở câu hỏi |
| 2 | Audio chung, câu hỏi và ba lựa chọn A/B/C |
| 3–4 | Audio/transcript dùng chung; dùng dấu cộng thêm đủ ba câu |
| 5 | Chỉ hiện câu hỏi, ẩn khung ngữ liệu; thêm nhiều câu độc lập trong một lần lưu |
| 7 | Thêm nhiều tài liệu trong một cụm, rồi thêm các câu dùng chung các tài liệu đó |

Lần lưu đầy đủ kiểm tra context giống luồng bắt đầu thi: bài nghe cần audio, Part 1 cần ảnh, các câu cần đủ lựa chọn tương ứng. Một lần lưu tối đa 20 ngữ cảnh, tổng 100 câu; mỗi ngữ cảnh tối đa 50 câu. Không cần nhập lại cùng bài đọc/audio cho từng câu.

## Dán nhiều câu

Mở “Dán nhiều câu từ tài liệu”, dán văn bản theo mẫu:

```text
Câu 1: The team responded _____.
A. quick
B. quickly
C. quickness
D. quicker
Đáp án: B

Câu 2: Please _____ your receipt.
A. retain
B. invent
C. borrow
D. disturb
Đáp án: A
```

Chấp nhận `A.`, `A)`, `A:`, dòng đáp án tiếng Việt hoặc `Answer: B`, nội dung nhiều dòng. Part 2 chỉ có A/B/C. Hệ thống kiểm tra rồi điền vào các thẻ để chỉnh sửa; chưa lưu database ngay. Không dùng JSON, không tự đoán đáp án thiếu. Với Part 6, dán đủ bốn câu; có thể ghi nhãn “Chỗ trống (n)” làm nội dung câu để khớp bài đọc.

Nội dung dán thay các câu mới chưa lưu, giữ các câu đã lưu. Các cụm cố định ba/bốn câu không có nút xóa thẻ làm thiếu cấu trúc; các cụm khác có thể thêm/bỏ thẻ chưa lưu.

## Tính toàn vẹn khi lưu

- Tạo/cập nhật group, documents, questions, options và gắn vào bộ cho tất cả ngữ cảnh thực hiện trong cùng transaction. Một ngữ cảnh lỗi làm rollback toàn bộ lần lưu.
- Ngữ cảnh mới độc lập dùng group riêng, không sao chép hoặc gộp bài đọc với ngữ cảnh trước. Khi sửa và mọi câu đã có trong bộ, để trống vị trí bắt đầu sẽ giữ thứ tự hiện hữu.
- Khi sửa cụm, giữ ID câu và vị trí hiện hữu. Không tự xóa câu bị bỏ khỏi payload hoặc chuyển câu từ cụm khác. Lưu trữ nội dung tiếp tục dùng cơ chế archive để giữ lịch sử.
- Kiểm tra `version` của group và từng câu; bản soạn lỗi thời trả conflict thay vì ghi đè thay đổi của người khác.
- Bộ nhận phải cùng Part. Vị trí câu trong bộ không được trùng; lỗi gắn vào bộ cũng hủy lần lưu cụm.
- Nếu hai người cùng thêm ở cuối một bộ, constraint vị trí vẫn bảo vệ dữ liệu; yêu cầu xung đột được hủy toàn bộ và có thể thử lại.
- Việc lưu vẫn dùng quy tắc version/progress hiện có; snapshot của lượt thi cũ được giữ nguyên.
- Nội dung chưa lưu nằm trong bộ nhớ màn hình, chưa phải bản nháp trên server. Chuyển giữa các ngữ cảnh giữ bản đang nhập; có xác nhận khi đổi Part và cảnh báo khi tải lại/đóng trang. Cần lưu trước khi chuyển sang màn hình khác.

## API cho frontend

- `POST /api/v1/admin/content/batches`: `{groups:[{id?,bundle:{group,version?,questions:[...],destination?}}]}`; có `id` để sửa ngữ cảnh hiện hữu. Response là mảng `{id,version,questions}` theo thứ tự yêu cầu, trong envelope chuẩn. Toàn bộ batch là một transaction.
- `POST /api/v1/admin/content/bundles`: tạo cụm và nhiều câu.
- `PUT /api/v1/admin/content/groups/{id}/bundle`: cập nhật đầy đủ cụm, giữ các câu hiện hữu.
- Body: `{group, version?, questions:[{id?,version?,position,questionText,promptText,hideQuestion,explanation,options}], destination?}`.
- `group` theo ContentRequests.Group. `destination`: `{type:"sets"|"test-parts",id,position?}`; bỏ trống vị trí để tự thêm cuối hoặc giữ vị trí đã có.
- Response: `{id,version,questions}` trong envelope chuẩn. Chỉ ADMIN được gọi.
- Các API đơn lẻ hiện hữu vẫn được giữ. Database không cần thêm bảng/migration cho luồng này.

Kiểm chứng: 47 test backend, 4 test parser frontend đạt; lint/build frontend thành công. Các test backend kiểm tra hai bài đọc độc lập, giữ vị trí khi sửa và rollback toàn bộ batch khi ngữ cảnh thứ hai lỗi.

Đã kiểm tra trên Chrome: menu dấu cộng, thêm cùng/ngữ cảnh mới, giữ nội dung khi chuyển ngữ cảnh và Reading chỉ còn một nút thêm câu, không có tab Passage. Đã xem giao diện desktop và điện thoại; ở chiều rộng 390px không bị tràn ngang. Kiểm tra trình duyệt không lưu câu mẫu vào database.

## Học liệu riêng

USER mở `/workspace/content` từ Học liệu của tôi → Listening/Reading. Dùng cùng trình soạn và API `/api/v1/workspace/content/*`, chỉ thấy/gắn/sửa câu và mục do mình tạo. ADMIN có alias `/admin/content` nhưng cũng chỉ truy cập nội dung của mình. Part dùng chung, USER không có tab hoặc quyền thay đổi Part. Quyền sở hữu áp dụng cả bài đọc/audio và lời giải AI có groupId. Chi tiết: [PRIVATE_WORKSPACE.md](PRIVATE_WORKSPACE.md).
