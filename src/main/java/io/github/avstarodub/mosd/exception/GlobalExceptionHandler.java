package io.github.avstarodub.mosd.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.util.HtmlUtils;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOG = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private static final String VALIDATION_FAILED_MESSAGE = "Validation failed";

    private static final String CONSTRAINT_VIOLATION_MESSAGE = "Constraint violation";

    private static final String RESOURCE_NOT_FOUND_LOG = "Resource not found";

    private static final String DUPLICATE_RESOURCE_LOG = "Duplicate resource";

    private static final String INVALID_ARGUMENT_LOG = "Invalid argument";

    private static final String UNEXPECTED_ERROR_LOG = "Unexpected error";

    private static final String UNEXPECTED_ERROR_MESSAGE = "An unexpected error occurred";

    private static String sanitizePath(HttpServletRequest request) {
        return HtmlUtils.htmlEscape(request.getRequestURI());
    }

    private ResponseEntity<ErrorResponse> buildValidationErrorResponse(
            String message, java.util.List<ErrorResponse.FieldError> fieldErrors, HttpServletRequest request) {
        LOG.warn("Validation error ({}): {} errors", message, fieldErrors.size());
        var response = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                message,
                sanitizePath(request),
                fieldErrors);
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(
            ResourceNotFoundException ex, HttpServletRequest request) {
        LOG.warn("{}: {}", RESOURCE_NOT_FOUND_LOG, ex.getMessage());
        var response = new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                HttpStatus.NOT_FOUND.getReasonPhrase(),
                ex.getMessage(),
                sanitizePath(request));
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler({DuplicateResourceException.class, DuplicateKeyException.class, ResourceInUseException.class})
    public ResponseEntity<ErrorResponse> handleDuplicateResource(Exception ex, HttpServletRequest request) {
        LOG.warn("{}: {}", DUPLICATE_RESOURCE_LOG, ex.getMessage());
        var response = new ErrorResponse(
                HttpStatus.CONFLICT.value(),
                HttpStatus.CONFLICT.getReasonPhrase(),
                ex.getMessage(),
                sanitizePath(request));
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        var fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> new ErrorResponse.FieldError(error.getField(), error.getDefaultMessage()))
                .toList();
        return buildValidationErrorResponse(VALIDATION_FAILED_MESSAGE, fieldErrors, request);
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<ErrorResponse> handleBindException(BindException ex, HttpServletRequest request) {
        var fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> new ErrorResponse.FieldError(error.getField(), error.getDefaultMessage()))
                .toList();
        return buildValidationErrorResponse(VALIDATION_FAILED_MESSAGE, fieldErrors, request);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(
            ConstraintViolationException ex, HttpServletRequest request) {
        var fieldErrors = ex.getConstraintViolations().stream()
                .map(violation ->
                        new ErrorResponse.FieldError(violation.getPropertyPath().toString(), violation.getMessage()))
                .toList();
        return buildValidationErrorResponse(CONSTRAINT_VIOLATION_MESSAGE, fieldErrors, request);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(
            IllegalArgumentException ex, HttpServletRequest request) {
        LOG.warn("{}: {}", INVALID_ARGUMENT_LOG, ex.getMessage());
        var response = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                ex.getMessage(),
                sanitizePath(request));
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex, HttpServletRequest request) {
        LOG.error(UNEXPECTED_ERROR_LOG, ex);
        var response = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                UNEXPECTED_ERROR_MESSAGE,
                sanitizePath(request));
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
