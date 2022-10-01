package account.controller.exception;

import account.dto.response.ResponseErrorEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.LockedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolationException;
import java.util.HashMap;
import java.util.NoSuchElementException;

@RestControllerAdvice
public class ControllerExceptionHandler {
    @ExceptionHandler(value = {ConstraintViolationException.class})
    public ResponseEntity<Object> handleConstrainValidationException(
            ConstraintViolationException ex,
            HttpServletRequest request
    ) {
        var errors = new HashMap<String, String>();
        ex.getConstraintViolations().forEach(violation -> {
            errors.put(violation.getPropertyPath().toString(), violation.getMessage());
        });

        var response = new ResponseErrorEntity<HashMap<String, String>>();
        response.setStatus(HttpStatus.BAD_REQUEST.value());
        response.setError("Bad request");
        response.setMessage(errors);
        response.setPath(request.getRequestURI());

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(value = {LockedException.class})
    public ResponseEntity<ResponseErrorEntity<String>> handleConstrainLockedException(
            LockedException ex,
            HttpServletRequest request
    ) {
        return buildErrorResponse(
            HttpStatus.UNAUTHORIZED,
            HttpStatus.UNAUTHORIZED.value(),
            HttpStatus.UNAUTHORIZED.getReasonPhrase(),
            ex.getMessage(),
            request.getRequestURI()
        );
    }

    @ExceptionHandler(value = {NoSuchElementException.class})
    public ResponseEntity<ResponseErrorEntity<String>> handleNoSuchElementException(
            NoSuchElementException ex,
            HttpServletRequest request
    ) {
        return buildErrorResponse(
            HttpStatus.NOT_FOUND,
            HttpStatus.NOT_FOUND.value(),
            HttpStatus.NOT_FOUND.getReasonPhrase(),
            ex.getMessage(),
            request.getRequestURI()
        );
    }

    @ExceptionHandler(value = InvalidElementException.class)
    public ResponseEntity<ResponseErrorEntity<String>> handleInvalidElementException(
            InvalidElementException ex,
            HttpServletRequest request
    ) {
        return buildErrorResponse(
            HttpStatus.BAD_REQUEST,
            HttpStatus.BAD_REQUEST.value(),
            HttpStatus.BAD_REQUEST.getReasonPhrase(),
            ex.getMessage(),
            request.getRequestURI()
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseErrorEntity<String>> handleGenericException(
            Exception ex,
            HttpServletRequest request
    ) {
        return buildErrorResponse(
            HttpStatus.UNAUTHORIZED,
            HttpStatus.UNAUTHORIZED.value(),
            HttpStatus.UNAUTHORIZED.getReasonPhrase(),
            ex.getMessage(),
            request.getRequestURI()
        );
    }

    private ResponseEntity<ResponseErrorEntity<String>> buildErrorResponse(
        HttpStatus httpStatus,
        int status,
        String error,
        String message,
        String path
    ) {
        var response = new ResponseErrorEntity<String>();
        response.setStatus(status);
        response.setError(error);
        response.setMessage(message);
        response.setPath(path);
        return ResponseEntity.status(httpStatus).body(response);
    }
}
