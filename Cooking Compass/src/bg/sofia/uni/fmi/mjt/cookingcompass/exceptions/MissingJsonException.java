package bg.sofia.uni.fmi.mjt.cookingcompass.exceptions;

public class MissingJsonException extends Exception {
    public MissingJsonException(String message) {
        super(message);
    }

    public MissingJsonException(String message, Throwable cause) {
        super(message, cause);
    }
}