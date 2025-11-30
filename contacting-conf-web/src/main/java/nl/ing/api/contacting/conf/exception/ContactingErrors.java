package nl.ing.api.contacting.conf.exception;

import com.ing.apisdk.toolkit.esperanto.core.ErrorType;
import com.ing.apisdk.toolkit.esperanto.core.EsperantoError;
import com.ing.apisdk.toolkit.esperanto.core.HttpEsperantoError;
import jakarta.ws.rs.core.Response;
import lombok.Getter;

@Getter
public enum ContactingErrors {

    /**
     * API Generic errors
     */
    RESOURCE_NOT_FOUND("1001", Response.Status.NOT_FOUND, "Resource not found %s"),
    SERVER_ERROR("1002", Response.Status.INTERNAL_SERVER_ERROR, "Error processing request"),
    RESOURCE_ALREADY_EXISTS("1003", Response.Status.CONFLICT, "Resource already exists %s"),
    VALUE_MISSING("1004", Response.Status.NOT_FOUND, "Value %s is missing"),
    INVALID_ARGUMENT("1005", Response.Status.BAD_REQUEST, "Invalid argument: %s"),
    FORBIDDEN("1006", Response.Status.FORBIDDEN, "Operation forbidden for %s"),
    CONFLICT("1007", Response.Status.CONFLICT, "Conflict with current state of the resource"),

    /**
     * Opeartion errors
     */
    UNSUPPORTED_OP("2001", Response.Status.FORBIDDEN, "Operation not supported"),
    MISSING_IDENTITY("2002", Response.Status.FORBIDDEN, "Missing employee or customer context"),
    KAFKA_MSG_PUB_FAIL("2003", Response.Status.BAD_REQUEST, "Failed to publish kafka message - %s"),
    WEB_HOOK_ERR("2004", Response.Status.BAD_REQUEST, "Only one webhook can be active"),
    INVALID_PHONE_NUM("2005", Response.Status.BAD_REQUEST, "Failed to update twilio sync - %s"),

    /*
        /**Twilio Errors
        */
    TASK_SERVICE_LEVEL_MISSING("3001", Response.Status.BAD_REQUEST, "No matching task service level found"),
    WORK_FLOW_NOT_FOUND("3002", Response.Status.BAD_REQUEST, "Could not find workflow"),
    TASK_CHANNEL_NOT_FOUND("3003", Response.Status.BAD_REQUEST, ""),
    TWILIO_SYNC_FAIL("3004", Response.Status.BAD_REQUEST, "Failed to update twilio sync - %s"),
    TWILIO_CORE_FACTORY_UPDATE_FAIL("3005", Response.Status.BAD_REQUEST, "Failed to update connection settings in twilio core factory - %s");

    private final String code;
    private final Response.Status status;
    private final String message;

    ContactingErrors(String code, Response.Status status, String message) {
        this.code = code;
        this.status = status;
        this.message = message;
    }


    /**
     * Automatically determine ErrorType based on HTTP status family.
     */
    private ErrorType resolveErrorType() {
        Response.Status.Family family = status.getFamily();
        return switch (family) {
            case CLIENT_ERROR -> ErrorType.FUNCTIONAL;
            default -> ErrorType.TECHNICAL;
        };
    }

    /**
     * Builds HttpEsperantoError using the default message template.
     * Example: INVALID_ARGUMENT.toHttpError("email format")
     */
    public HttpEsperantoError toHttpError(Object... args) {
        String formattedMessage = String.format(message, args);
        return buildHttpError(formattedMessage);
    }

    /**
     * Builds HttpEsperantoError using a custom message (ignores default template).
     * Example: INVALID_ARGUMENT.toHttpErrorWithMessage("Custom validation failed for username")
     */
    public HttpEsperantoError toHttpErrorWithMessage(String customMessage) {
        return buildHttpError(customMessage);
    }

    private HttpEsperantoError buildHttpError(String message) {
        EsperantoError esperantoError = new EsperantoError(code, message);
        return new HttpEsperantoError(esperantoError, resolveErrorType(), status.getStatusCode());
    }

    public String format(Object... args) {
        return String.format(message, args);
    }

}


