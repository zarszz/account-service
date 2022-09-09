package account.controller.exception;

import account.controller.exception.ErrorResponse;
import org.apache.tomcat.util.ExceptionUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.LockedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolationException;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.HashMap;

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
        var response = new HashMap<String, Object>();

        response.put("timestamp", "data");
        response.put("status", HttpStatus.BAD_REQUEST.value());
        response.put("error", "Bad Request");
        response.put("message", errors);
        response.put("path", request.getRequestURI());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(value = {LockedException.class})
    public ResponseEntity<Object> handleConstrainLockedException(
            LockedException ex,
            HttpServletRequest request
    ) {
        var response = new HashMap<String, Object>();

        response.put("timestamp", "data");
        response.put("status", HttpStatus.UNAUTHORIZED.value());
        response.put("error", HttpStatus.UNAUTHORIZED.getReasonPhrase());
        response.put("message", ex.getMessage());
        response.put("path", request.getRequestURI());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

//    @ExceptionHandler(value = {LockedException.class})
//    public ResponseEntity<Object> handleUndeclaredThrowableException(
//            UndeclaredThrowableException ex,
//            HttpServletRequest request
//    ) {
//        var response = new HashMap<String, Object>();
//
//        response.put("timestamp", "data");
//        response.put("status", HttpStatus.UNAUTHORIZED.value());
//        response.put("error", HttpStatus.UNAUTHORIZED.getReasonPhrase());
//        response.put("message", ex.getMessage());
//        response.put("path", request.getRequestURI());
//        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
//    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleGenericException(
            Exception ex,
            HttpServletRequest request
    ) {
        var response = new HashMap<String, Object>();

        response.put("timestamp", "data");
        response.put("status", HttpStatus.UNAUTHORIZED.value());
        response.put("error", HttpStatus.UNAUTHORIZED.getReasonPhrase());
        response.put("message", ex.getMessage());
        response.put("path", request.getRequestURI());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }
}
