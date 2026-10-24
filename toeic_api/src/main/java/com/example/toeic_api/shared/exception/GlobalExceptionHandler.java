package com.example.toeic_api.shared.exception;

import com.example.toeic_api.shared.api.ApiResponse;

import jakarta.validation.ConstraintViolation;

import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.Map;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    private static final String MIN_ATTRIBUTE = "min";

    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiResponse<?>> handlingRuntimeException(Exception exception) {
        ApiResponse<?> apiResponse = new ApiResponse<>();
        log.error("Lỗi:", exception);
        ErrorCode errorCode = ErrorCode.UNCATEGORIZED_EXCEPTION;
        apiResponse.setCode(errorCode.getCode());
        apiResponse.setMessage(errorCode.getMessage());
        return ResponseEntity.status(errorCode.getHttpStatusCode()).body(apiResponse);
    }

    @ExceptionHandler(AppException.class)
    ResponseEntity<ApiResponse<?>> handlingAppException(AppException exception) {
        ErrorCode errorCode = exception.getErrorCode();
        ApiResponse<?> apiResponse = new ApiResponse<>();
        apiResponse.setCode(errorCode.getCode());
        apiResponse.setMessage(
                exception.getMessage() != null ? exception.getMessage() : errorCode.getMessage());
        return ResponseEntity.status(errorCode.getHttpStatusCode()).body(apiResponse);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiResponse<?>> handlingValidationException(
            MethodArgumentNotValidException exception) {
        var fieldError = exception.getFieldError();
        String enumKey = (fieldError != null) ? fieldError.getDefaultMessage() : null;

        ErrorCode errorCode = ErrorCode.INVALID_CONTENT;
        if (enumKey != null) {
            try {
                errorCode = ErrorCode.valueOf(enumKey);
            } catch (IllegalArgumentException e) {
                log.debug("Standard constraint validation: {}", enumKey);
            }
        }

        String finalMessage = errorCode.getMessage();

        try {
            var allErrors = exception.getBindingResult().getAllErrors();
            if (!allErrors.isEmpty()) {
                var constraintViolation = allErrors.getFirst().unwrap(ConstraintViolation.class);
                var attributes = constraintViolation.getConstraintDescriptor().getAttributes();

                if (attributes != null) {
                    log.info("Thuộc tính Validation: {}", attributes);
                    finalMessage = mapAttribute(finalMessage, attributes);
                }
            }
        } catch (Exception e) {
            log.warn("Không thể unwrap ConstraintViolation hoặc map attribute: {}", e.getMessage());
        }

        ApiResponse<?> apiResponse = new ApiResponse<>();
        apiResponse.setCode(errorCode.getCode());
        apiResponse.setMessage(finalMessage);
        return ResponseEntity.status(errorCode.getHttpStatusCode()).body(apiResponse);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    ResponseEntity<ApiResponse<?>> handlingMaxUploadSize(MaxUploadSizeExceededException exception) {
        ErrorCode errorCode = ErrorCode.FILE_TOO_LARGE;
        return ResponseEntity.status(errorCode.getHttpStatusCode())
                .body(
                        ApiResponse.builder()
                                .code(errorCode.getCode())
                                .message(errorCode.getMessage())
                                .build());
    }

    @ExceptionHandler(AccessDeniedException.class)
    ResponseEntity<ApiResponse<?>> handlingAccessDeniedException(AccessDeniedException exception) {
        ErrorCode errorCode = ErrorCode.UNAUTHORIZED;
        ApiResponse<?> apiResponse =
                ApiResponse.builder()
                        .code(errorCode.getCode())
                        .message(errorCode.getMessage())
                        .build();
        return ResponseEntity.status(errorCode.getHttpStatusCode()).body(apiResponse);
    }

    private String mapAttribute(String message, Map<String, Object> attributes) {
        if (attributes.containsKey(MIN_ATTRIBUTE) && attributes.get(MIN_ATTRIBUTE) != null) {
            String minValue = attributes.get(MIN_ATTRIBUTE).toString();
            return message.replace("{" + MIN_ATTRIBUTE + "}", minValue);
        }
        return message;
    }

    @ExceptionHandler({
        org.springframework.dao.DataIntegrityViolationException.class,
        org.springframework.orm.ObjectOptimisticLockingFailureException.class
    })
    ResponseEntity<ApiResponse<?>> conflict(Exception ex) {
        return ResponseEntity.status(409)
                .body(
                        ApiResponse.builder()
                                .code(ErrorCode.DATA_CONFLICT.getCode())
                                .message(ErrorCode.DATA_CONFLICT.getMessage())
                                .build());
    }
}
