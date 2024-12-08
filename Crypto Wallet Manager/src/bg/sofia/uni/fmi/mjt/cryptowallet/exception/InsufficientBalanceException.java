package bg.sofia.uni.fmi.mjt.cryptowallet.exception;

public class InsufficientBalanceException extends RuntimeException {
    public InsufficientBalanceException() {
        super();
    }

    public InsufficientBalanceException(String message) {
        super(message);
    }
}
