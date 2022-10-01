package account.controller.exception;

public class InvalidElementException extends RuntimeException {
    public InvalidElementException(String message) {
        super(message);
    }
}
