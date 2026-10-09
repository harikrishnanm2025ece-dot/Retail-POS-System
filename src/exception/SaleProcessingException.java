package exception;

public class SaleProcessingException extends RuntimeException {
    public SaleProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}
