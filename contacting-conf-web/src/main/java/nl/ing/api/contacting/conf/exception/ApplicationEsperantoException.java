package nl.ing.api.contacting.conf.exception;

import com.ing.apisdk.toolkit.esperanto.core.ErrorType;
import com.ing.apisdk.toolkit.esperanto.core.HttpEsperantoError;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ApplicationEsperantoException extends RuntimeException {

    private final HttpEsperantoError httpError;

    public ApplicationEsperantoException(HttpEsperantoError httpError) {
        super(extractMessage(httpError));
        this.httpError = httpError;
    }

    public HttpEsperantoError getHttpError() {
        return httpError;
    }

    public ErrorType getErrorType() {
        return httpError.errorType();
    }

    public int getHttpStatus() {
        return httpError.httpStatusCode();
    }

    public String getCode() {
        return httpError.esperantoError().error().code();
    }

    private static String extractMessage(HttpEsperantoError httpError) {
        try {
            var esperantoError = httpError.esperantoError();
            if (esperantoError != null && esperantoError.error() != null) {
                return esperantoError.error().message();
            }
        } catch (Exception ignored) {
            //Exception here shouldn't impact the api flow so just log and return a default message
            log.warn("Exception while parsing response error message {}", ignored);
        }
        return "(unknown error)";
    }
}

