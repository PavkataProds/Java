package bg.sofia.uni.fmi.mjt.cryptowallet.exception;

public class FailedRequestException extends Exception {
    public FailedRequestException(String message) {
        super(message);
    }

    public FailedRequestException(String message, Throwable cause) {
        super(message, cause);
    }
}
