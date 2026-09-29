package hotel.exception;

/**
 * Thrown when a command line cannot be parsed or its arguments are invalid.
 * The program logs and skips the offending line rather than crashing.
 */
public class InvalidCommandException extends RuntimeException {

    public InvalidCommandException(String message) {
        super(message);
    }

    public InvalidCommandException(String message, Throwable cause) {
        super(message, cause);
    }
}
