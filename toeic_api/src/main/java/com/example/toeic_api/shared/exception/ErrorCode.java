package com.example.toeic_api.shared.exception;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public enum ErrorCode {
    UNCATEGORIZED_EXCEPTION(9999, "Lỗi không xác định", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_KEY(1001, "Khóa không hợp lệ", HttpStatus.BAD_REQUEST),
    USER_EXISTED(1002, "Người dùng đã tồn tại", HttpStatus.BAD_REQUEST),
    INVALID_USERNAME(1003, "Tên đăng nhập phải ít nhất {min} ký tự", HttpStatus.BAD_REQUEST),
    INVALID_PASSWORD(1004, "Mật khẩu phải ít nhất {min} ký tự", HttpStatus.BAD_REQUEST),
    INVALID_EMAIL(1005, "Email không hợp lệ", HttpStatus.BAD_REQUEST),
    USER_NOT_EXISTED(1006, "Người dùng không tồn tại", HttpStatus.NOT_FOUND),
    UNAUTHENTICATED(1007, "Chưa xác thực", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(1008, "Bạn không có quyền truy cập", HttpStatus.FORBIDDEN),
    USER_DISABLED(1009, "Tài khoản đã bị khóa", HttpStatus.FORBIDDEN),

    INVALID_CONTENT(
            7001, "Nội dung hoặc quan hệ giữa các Part không hợp lệ", HttpStatus.BAD_REQUEST),
    CONTENT_NOT_FOUND(7002, "Không tìm thấy nội dung", HttpStatus.NOT_FOUND),
    INVALID_ANSWER(
            7003,
            "Lựa chọn không thuộc câu hỏi hoặc câu hỏi không thuộc bài làm",
            HttpStatus.BAD_REQUEST),
    INCOMPLETE_CONTEXT(
            7004,
            "Cụm câu hỏi chưa đầy đủ audio, tài liệu hoặc các câu liên quan",
            HttpStatus.BAD_REQUEST),
    FIXED_PART(
            7005,
            "Bảy Part TOEIC là danh mục cố định; chỉ có thể sửa mô tả",
            HttpStatus.BAD_REQUEST),
    DATA_CONFLICT(
            7006, "Dữ liệu trùng hoặc đã thay đổi. Hãy tải lại và thử lại", HttpStatus.CONFLICT),
    INVALID_RESET_TOKEN(
            7007, "Liên kết đặt lại mật khẩu không hợp lệ hoặc đã hết hạn", HttpStatus.BAD_REQUEST),
    RESET_UNAVAILABLE(
            7008, "Tính năng gửi email chưa được cấu hình", HttpStatus.SERVICE_UNAVAILABLE),

    USE_CONTENT_BANK(
            7009,
            "Cụm có nhiều tài liệu; hãy chỉnh sửa trong Ngân hàng nội dung",
            HttpStatus.BAD_REQUEST),

    // Listening errors
    LISTENING_PART_NOT_FOUND(2001, "Không tìm thấy Part nghe", HttpStatus.NOT_FOUND),
    LISTENING_SECTION_NOT_FOUND(2002, "Không tìm thấy Section nghe", HttpStatus.NOT_FOUND),
    LISTENING_QUESTION_NOT_FOUND(2003, "Không tìm thấy câu hỏi nghe", HttpStatus.NOT_FOUND),

    // Reading errors
    READING_PART_NOT_FOUND(3001, "Không tìm thấy Part đọc", HttpStatus.NOT_FOUND),
    READING_SECTION_NOT_FOUND(3002, "Không tìm thấy Section đọc", HttpStatus.NOT_FOUND),
    READING_QUESTION_NOT_FOUND(3003, "Không tìm thấy câu hỏi đọc", HttpStatus.NOT_FOUND),
    READING_PASSAGE_NOT_FOUND(3004, "Không tìm thấy đoạn đọc", HttpStatus.NOT_FOUND),

    // Vocabulary errors
    VOCABULARY_CATEGORY_NOT_FOUND(4001, "Không tìm thấy danh mục từ vựng", HttpStatus.NOT_FOUND),
    VOCABULARY_SET_NOT_FOUND(4002, "Không tìm thấy bộ từ vựng", HttpStatus.NOT_FOUND),
    VOCABULARY_WORD_NOT_FOUND(4003, "Không tìm thấy từ vựng", HttpStatus.NOT_FOUND),

    // Test errors
    TEST_SUITE_NOT_FOUND(5001, "Không tìm thấy đề thi", HttpStatus.NOT_FOUND),
    TEST_PART_NOT_FOUND(5002, "Không tìm thấy Part đề thi", HttpStatus.NOT_FOUND),
    TEST_QUESTION_NOT_FOUND(5003, "Không tìm thấy câu hỏi đề thi", HttpStatus.NOT_FOUND),
    TEST_RESULT_NOT_FOUND(5004, "Không tìm thấy bài làm", HttpStatus.NOT_FOUND),
    TEST_ALREADY_SUBMITTED(5005, "Bài này đã nộp rồi", HttpStatus.BAD_REQUEST),

    FILE_REQUIRED(6001, "Hãy chọn file để tải lên", HttpStatus.BAD_REQUEST),
    FILE_TYPE_INVALID(6002, "Định dạng file không hợp lệ", HttpStatus.BAD_REQUEST),
    FILE_TOO_LARGE(6003, "File vượt quá dung lượng cho phép", HttpStatus.BAD_REQUEST),
    CLOUDINARY_NOT_CONFIGURED(
            6004,
            "Chưa cấu hình Cloudinary. Hãy điền cloud-name, api-key, api-secret trong"
                    + " application.yml",
            HttpStatus.SERVICE_UNAVAILABLE),
    UPLOAD_FAILED(6005, "Không tải được file lên Cloudinary", HttpStatus.BAD_GATEWAY),
    CLOUDINARY_INVALID_CLOUD_NAME(
            6006,
            "Cloud name không đúng. Hãy copy Cloud name từ Cloudinary Dashboard → API Keys (viết"
                    + " thường), không dùng Product name.",
            HttpStatus.BAD_REQUEST),
    AI_NOT_CONFIGURED(
            8001, "Chưa cấu hình GROQ_API_KEY ở backend.", HttpStatus.SERVICE_UNAVAILABLE),
    AI_BUSY(
            8002,
            "AI đang bận hoặc hết hạn mức. Vui lòng thử lại sau.",
            HttpStatus.TOO_MANY_REQUESTS),
    AI_FAILED(
            8003,
            "Không tạo được nội dung từ Groq. Kiểm tra cấu hình hoặc thử lại sau.",
            HttpStatus.BAD_GATEWAY),
    AI_PROVIDER_AUTH_ERROR(
            8004,
            "Groq từ chối API key hoặc quyền truy cập. Kiểm tra GROQ_API_KEY và quyền model của"
                    + " project Groq.",
            HttpStatus.SERVICE_UNAVAILABLE),
    AI_MODEL_UNAVAILABLE(
            8005,
            "Model Groq không tồn tại hoặc tài khoản chưa có quyền sử dụng. Kiểm tra"
                + " GROQ_MODEL/GROQ_VISION_MODEL và chọn model khả dụng và khởi động lại backend.",
            HttpStatus.SERVICE_UNAVAILABLE),
    AI_TIMEOUT(
            8006,
            "Groq phản hồi quá thời gian chờ. Vui lòng thử lại sau.",
            HttpStatus.GATEWAY_TIMEOUT),
    AI_CONNECTION_FAILED(
            8007,
            "Không kết nối được Groq. Kiểm tra mạng, DNS, proxy hoặc kết nối HTTPS của backend.",
            HttpStatus.BAD_GATEWAY),
    AI_INVALID_RESPONSE(
            8008,
            "Groq trả nội dung thiếu phần hoặc sai định dạng/ngôn ngữ. Hãy thử tạo lại.",
            HttpStatus.BAD_GATEWAY),
    AI_RESPONSE_TRUNCATED(
            8009,
            "Nội dung Groq bị cắt vì vượt giới hạn đầu ra. Hãy chọn ít nội dung hơn rồi tạo lại.",
            HttpStatus.BAD_GATEWAY),
    AI_REQUEST_REJECTED(
            8010,
            "Groq từ chối nội dung hoặc tham số yêu cầu. Kiểm tra GROQ_MODEL có hỗ trợ JSON mode và"
                    + " độ dài ngữ liệu.",
            HttpStatus.BAD_GATEWAY),
    INVALID_PROGRESS_STATUS(
            6007, "Chỉ có thể đặt lại chưa làm hoặc đánh dấu", HttpStatus.BAD_REQUEST);

    ErrorCode(int code, String message, HttpStatusCode httpStatusCode) {
        this.code = code;
        this.message = message;
        this.httpStatusCode = httpStatusCode;
    }

    int code;
    String message;
    HttpStatusCode httpStatusCode;
}
