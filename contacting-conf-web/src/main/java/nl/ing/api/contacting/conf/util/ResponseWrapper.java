
import com.ing.apisdk.toolkit.esperanto.core.HttpEsperantoError;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import org.springframework.http.HttpStatus;

import java.util.Map;
import java.util.Optional;

/**
 * Utility class for creating standardized HTTP responses.
 */
public final class ResponseWrapper {

    public static final String APPLICATION_NAME = "contacting-configuration-api";
    /**
     * Creates a Not Found (404) response with the provided message.
     *
     * @param msg The error message
     * @return A Response object with status 404 Not Found
     */
    public static Response notFound(String msg) {
        return Response.status(Response.Status.NOT_FOUND).entity(msg).build();
    }

    public static Response businessError(ContactingErrors error, String msg) {
        return Response.status(error.getStatus()).entity(msg).build();
    }

    public static Response esperantoError(ApplicationEsperantoException exception) {
        return Response.status(exception.getHttpStatus()).entity(exception.getHttpError()).build();
    }

    public static Response toData(Object payload) {
        return Response.ok(Map.of("data", payload)).build();
    }

    /**
     * Creates an OK response with the provided value if present, or a Not Found response if empty.
     *
     * @param value    The optional value to include in the response
     * @param notFound The Not Found response to return if value is empty
     * @param <A>      The type of the optional value
     * @return A Response object with either the value or Not Found status
     */
    public static <A> Response optJsonResponse(Optional<A> value, Response notFound) {
        return value.map(ResponseWrapper::okJsonResponse).orElse(notFound);
    }

    /**
     * Creates an empty OK (200) response.
     *
     * @return A Response object with status 200 OK and no content
     */
    public static Response emptyOkResponse() {
        return Response.ok().build();
    }

    /**
     * Creates a No Content (204) response.
     *
     * @return A Response object with status 204 No Content
     */
    public static Response noContentResponse() {
        return Response.noContent().build();
    }

    /**
     * Creates a Multi-Status (207) response with the provided entity.
     *
     * @param value The entity to include in the response body
     * @param <A>   The type of the entity
     * @return A Response object with status 207 Multi-Status
     */
    public static <A> Response multiStatusContent(A value) {
        return Response.status(HttpStatus.MULTI_STATUS.value()).entity(value).build();
    }

    /**
     * Creates a response based on the number of rows deleted.
     *
     * @param rowsDeleted The number of rows deleted
     * @return A Response object with either No Content or Not Found status
     */
    public static Response deleteResponse(int rowsDeleted) {
        return rowsDeleted == 0
                ? notFound("resource not found")
                : noContentResponse();
    }

    /**
     * Creates an OK (200) response with the provided entity as JSON.
     *
     * @param value The entity to include in the response body
     * @param <A>   The type of the entity
     * @return A Response object with status 200 OK and JSON content
     */
    public static <A> Response okJsonResponse(A value) {
        return Response.ok(value).type(MediaType.APPLICATION_JSON).build();
    }

    /**
     * Creates a Created (201) response with the provided class and path.
     *
     * @param classOf The class to use for URI building
     * @param path    The path to include in the URI
     * @return A Response object with status 201 Created
     */
    public static Response createdResponse(Class<?> classOf, String path) {
        return Response.created(UriBuilder.fromResource(classOf).path(path).build())
                .type(MediaType.APPLICATION_JSON)
                .build();
    }

    /**
     * Creates a Created (201) response with the provided class and path.
     *
     * @param value The class to use for URI building
     * @return A Response object with status 201 Created
     */
    public static <A> Response created(A value) {
        return  Response.status(Response.Status.CREATED)
                .entity(value)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }

    public static Response createdResponseWithLocationHeader(Class<?> resourceClass, String path) {
        var location = UriBuilder.fromResource(resourceClass).path(path).build();
        return Response.created(location)
                .entity(path)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }

    /**
     * Creates an OK (200) response with the provided entity.
     *
     * @param value The entity to include in the response body
     * @param <A>   The type of the entity
     * @return A Response object with status 200 OK
     */
    public static <A> Response okResponse(A value) {
        return Response.ok(value).build();
    }

    /**
     * Creates a Server Error (500) response with an optional message.
     *
     * @param message An optional error message
     * @return A Response object with status 500 Internal Server Error
     */
    public static Response serverError(Optional<String> message) {
        HttpEsperantoError error = ContactingErrors.SERVER_ERROR.toHttpErrorWithMessage(message.orElse("Internal server error"));
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(error)
                .build();
    }

    /**
     * Creates a Forbidden (403) response with the provided entity.
     *
     * @param value The entity to include in the response body
     * @param <A>   The type of the entity
     * @return A Response object with status 403 Forbidden
     */
    public static <A> Response forbidden(A value) {
        return Response.status(403).type(MediaType.TEXT_PLAIN).entity(value).build();
    }


    /**
     * Creates a Conflict (409) response with the provided entity.
     *
     * @param value The entity to include in the response body
     * @param <A>   The type of the entity
     * @return A Response object with status 409 Conflict
     */
    public static <A> Response conflict(A value) {
        return Response.status(409).entity(value).build();
    }

    /**
     * Creates a Bad Request (400) response with the provided entity.
     *
     * @param value The entity to include in the response body
     * @param <A>   The type of the entity
     * @return A Response object with status 400 Bad Request
     */
    public static <A> Response badRequest(A value) {
        return Response.status(400).type(MediaType.TEXT_PLAIN).entity(value).build();
    }

    private ResponseWrapper() {
        // Utility class - prevent instantiation
    }
}
