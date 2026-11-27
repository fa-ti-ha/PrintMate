package com.printmate.common.exception;

import com.printmate.common.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private boolean isApiRequest(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String accept = request.getHeader("Accept");
        return uri.startsWith("/api/") || (accept != null && accept.contains("application/json"));
    }

    private void status(int code) {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes) {
            HttpServletResponse response = attributes.getResponse();
            if (response != null) response.setStatus(code);
        }
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class,
            MissingServletRequestParameterException.class})
    public Object handleMalformed(Exception ex, HttpServletRequest request, Model model) {
        return handleInvalidOperation(new InvalidOperationException("Please check the request fields and their values."), request, model);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public Object handleDenied(AccessDeniedException ex, HttpServletRequest request, Model model) {
        return handleUnauthorized(new UnauthorizedAccessException("You do not have permission to perform this action."), request, model);
    }

    @ExceptionHandler({DataIntegrityViolationException.class, ObjectOptimisticLockingFailureException.class})
    public Object handleConflict(Exception ex, HttpServletRequest request, Model model) {
        status(409);
        String message = "This record is in use, already exists, or was changed. Refresh and try again.";
        if (isApiRequest(request)) return ResponseEntity.status(409).body(ApiResponse.error(message));
        model.addAttribute("status", 409);
        model.addAttribute("error", "Conflict");
        model.addAttribute("message", message);
        return "errors/error";
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public Object handleNotFound(ResourceNotFoundException ex, HttpServletRequest request, Model model) {
        status(404);
        log.warn("Resource not found: {}", ex.getMessage());
        if (isApiRequest(request)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(ex.getMessage()));
        }
        model.addAttribute("status", 404);
        model.addAttribute("error", "Not Found");
        model.addAttribute("message", ex.getMessage());
        return "errors/error";
    }

    @ExceptionHandler(InvalidOperationException.class)
    public Object handleInvalidOperation(InvalidOperationException ex, HttpServletRequest request, Model model) {
        status(400);
        log.warn("Invalid operation: {}", ex.getMessage());
        if (isApiRequest(request)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(ex.getMessage()));
        }
        model.addAttribute("status", 400);
        model.addAttribute("error", "Bad Request");
        model.addAttribute("message", ex.getMessage());
        return "errors/error";
    }

    @ExceptionHandler(UnauthorizedAccessException.class)
    public Object handleUnauthorized(UnauthorizedAccessException ex, HttpServletRequest request, Model model) {
        status(403);
        log.warn("Unauthorized access: {}", ex.getMessage());
        if (isApiRequest(request)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error(ex.getMessage()));
        }
        model.addAttribute("status", 403);
        model.addAttribute("error", "Access Denied");
        model.addAttribute("message", ex.getMessage());
        return "errors/error";
    }

    @ExceptionHandler(FileProcessingException.class)
    public Object handleFileProcessing(FileProcessingException ex, HttpServletRequest request, Model model) {
        status(400);
        log.error("File processing error: {}", ex.getMessage());
        if (isApiRequest(request)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(ex.getMessage()));
        }
        model.addAttribute("status", 400);
        model.addAttribute("error", "File Upload Error");
        model.addAttribute("message", ex.getMessage());
        return "errors/error";
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public Object handleMaxSize(MaxUploadSizeExceededException ex, HttpServletRequest request, Model model) {
        status(413);
        String msg = "Uploaded file size exceeds the allowed limit (max 25MB).";
        if (isApiRequest(request)) {
            return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(ApiResponse.error(msg));
        }
        model.addAttribute("status", 413);
        model.addAttribute("error", "Payload Too Large");
        model.addAttribute("message", msg);
        return "errors/error";
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Object handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request, Model model) {
        status(400);
        Map<String, String> errors = new HashMap<>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            errors.put(fe.getField(), fe.getDefaultMessage());
        }
        if (isApiRequest(request)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, "Validation failed", errors));
        }
        model.addAttribute("status", 400);
        model.addAttribute("error", "Validation Failed");
        model.addAttribute("message", "Validation errors: " + errors);
        return "errors/error";
    }

    @ExceptionHandler(Exception.class)
    public Object handleGeneral(Exception ex, HttpServletRequest request, Model model) {
        if (ex instanceof org.springframework.web.ErrorResponse httpError && httpError.getStatusCode().is4xxClientError()) {
            int code = httpError.getStatusCode().value();
            status(code);
            String message = switch (code) {
                case 404 -> "The requested page or resource was not found.";
                case 405 -> "This HTTP method is not supported for the requested resource.";
                case 415 -> "Use the content type required by this endpoint.";
                default -> "Please check the required request fields and try again.";
            };
            if (isApiRequest(request)) return ResponseEntity.status(code).body(ApiResponse.error(message));
            model.addAttribute("status", code);
            model.addAttribute("error", "Request Error");
            model.addAttribute("message", message);
            return "errors/error";
        }
        status(500);
        log.error("Unhandled exception occurred", ex);
        String msg = "An unexpected error occurred. Please try again later.";
        if (isApiRequest(request)) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error(msg));
        }
        model.addAttribute("status", 500);
        model.addAttribute("error", "Internal Server Error");
        model.addAttribute("message", msg);
        return "errors/error";
    }
}
