package account.controller.exception;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    private final int status;
    private final String message;
    private List<ValidationError> errors;

    public ErrorResponse(int status, String message){
        this.status = status;
        this.message = message;
    }

    private static class ValidationError {
        private final String field;
        private final String message;

        private ValidationError(String field, String message) {
            this.field = field;
            this.message = message;
        }
    }

    public void addValidationError(String field, String message) {
        if (Objects.isNull(errors)) {
            errors = new ArrayList<>();
        }
        errors.add(new ValidationError(field, message));
    }
}
