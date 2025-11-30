package nl.ing.api.contacting.conf.exception;

import com.ing.apisdk.toolkit.esperanto.core.HttpEsperantoError;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class Errors {

    private Errors() {
    } // prevent instantiation

    public static ApplicationEsperantoException raise(ContactingErrors error, Object... args) {
        // Uses default message template
        HttpEsperantoError httpError = error.toHttpError(args);
        logError(httpError);
        return new ApplicationEsperantoException(httpError);
    }

    public static ApplicationEsperantoException raiseWithMsg(ContactingErrors error, String customMessage) {
        // Uses custom message
        HttpEsperantoError httpError = error.toHttpErrorWithMessage(customMessage);
        logError(httpError);
        return new ApplicationEsperantoException(httpError);
    }

    // Convenience wrappers for common cases
    public static ApplicationEsperantoException notFound(String msg) {
        return raiseWithMsg(ContactingErrors.RESOURCE_NOT_FOUND, msg);
    }

    public static ApplicationEsperantoException unexpected(String msg) {
        return raiseWithMsg(ContactingErrors.SERVER_ERROR, msg);
    }

    public static ApplicationEsperantoException alreadyExists(String msg) {
        return raiseWithMsg(ContactingErrors.RESOURCE_ALREADY_EXISTS, msg);
    }

    public static ApplicationEsperantoException badRequest(String msg) {
        return raiseWithMsg(ContactingErrors.INVALID_ARGUMENT, msg);
    }

    public static ApplicationEsperantoException valueMissing(String msg) {
        return raiseWithMsg(ContactingErrors.VALUE_MISSING, msg);
    }

    public static ApplicationEsperantoException forbidden(String msg) {
        return raiseWithMsg(ContactingErrors.FORBIDDEN, msg);
    }

    public static ApplicationEsperantoException serverError(String msg) {
        return raiseWithMsg(ContactingErrors.SERVER_ERROR, msg);
    }

    /**
     * Logs errors automatically.
     */

    private static void logError(HttpEsperantoError httpError) {

        String code = "UNKNOWN";
        String message = "(unknown error)";
        try {
            var esperantoError = httpError.esperantoError();
            if (esperantoError != null && esperantoError.error() != null) {
                code = esperantoError.error().code();
                message = esperantoError.error().message();
            }
        } catch (Exception e) {
            log.warn("Could not extract details from HttpEsperantoError", e);
        }
        log.error("Application error [{}]: {} (HTTP {})", code, message, httpError.httpStatusCode());

    }

    public static ApplicationEsperantoException conflict(String msg) {
        return raiseWithMsg(ContactingErrors.CONFLICT, msg);
    }
}

