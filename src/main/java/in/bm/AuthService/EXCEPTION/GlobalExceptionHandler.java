package in.bm.AuthService.EXCEPTION;

import in.bm.AuthService.RESPONSEDTO.ApiError;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {


    @ExceptionHandler(InvalidOtpException.class)
    public ResponseEntity<ApiError> handleInvalidOtp(InvalidOtpException ex) {
        return build(ex, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(OauthAuthenticationException.class)
    public ResponseEntity<ApiError> handleOauth(OauthAuthenticationException ex) {
        return build(ex, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiError> handleUserNotFound(UserNotFoundException ex) {
        return build(ex, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(OtpSendException.class)
    public ResponseEntity<ApiError> handleOtpSend(OtpSendException ex) {
        return build(ex, HttpStatus.SERVICE_UNAVAILABLE);
    }

    @ExceptionHandler(OtpVerifyException.class)
    public ResponseEntity<ApiError> handleOtpVerify(OtpVerifyException ex) {
        return build(ex, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(AuthException.class)
    public ResponseEntity<ApiError> handleAuthFallback(AuthException ex) {
        return build(ex, HttpStatus.UNAUTHORIZED);
    }

    private ResponseEntity<ApiError> build(Exception ex, HttpStatus status) {

        ApiError error = new ApiError(
                status.getReasonPhrase(),
                ex.getMessage(),
                status.value(),
                LocalDateTime.now()
        );
        return ResponseEntity.status(status).body(error);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {

        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .findFirst()
                .orElse("Validation failed");

        ApiError error = new ApiError(
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                message,
                HttpStatus.BAD_REQUEST.value(),
                LocalDateTime.now()
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(error);
    }

    @ExceptionHandler(AdminNotFoundException.class)
    public ResponseEntity<ApiError> handleAdminNotFound(AdminNotFoundException ex){
        return build(ex,HttpStatus.NOT_FOUND);
    }


}
