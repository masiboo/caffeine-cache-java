package nl.ing.api.contacting.conf.exception;

/**
 * Exception class
 */
public class ConfServiceRuntimeException extends RuntimeException {

    public ConfServiceRuntimeException(String message) {
        super(message);
    }

    public ConfServiceRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }
}
