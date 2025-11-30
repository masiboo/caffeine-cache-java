package nl.ing.api.contacting.conf.resource.jaxrs.support;

import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import nl.ing.api.contacting.conf.exception.ApplicationEsperantoException;
import nl.ing.api.contacting.conf.util.ResponseWrapper;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;
import java.util.function.Supplier;

@Slf4j
public class AsyncUtils {

    private AsyncUtils() {
        // private constructor to prevent instantiation
    }

    private static final long DEFAULT_TIMEOUT_SECONDS = 10;

    //Using scala configured executor. This executor service is suitable for DB operations
    private static final Executor dbExecutor = nl.ing.api.contacting.conf.modules.ExecutionContextConfig.listeningExecutorService();

    public static <T> CompletableFuture<T> supplyAsync(Supplier<T> supplier) {
        return CompletableFuture.supplyAsync(supplier, dbExecutor);
    }

    /**
     * Wraps a synchronous/blocking supplier into a CompletableFuture and handles it via AsyncResponseHandler.
     *
     * @param supplier       The blocking method to execute
     * @param successHandler Converts the result into a Response
     */
    public static <T> CompletableFuture<Response> handleAsyncExecution(
            Supplier<T> supplier,
            Function<T, Response> successHandler) {

        CompletableFuture<T> future = CompletableFuture.supplyAsync(supplier, dbExecutor);
        return handleFuture(future, successHandler);
    }

    public static <T> CompletableFuture<Response> handleSyncExecution(
            Supplier<T> supplier,
            Function<T, Response> successHandler) {

        CompletableFuture<T> future = CompletableFuture.completedFuture(supplier.get());
        return handleFuture(future, successHandler);
    }

    public static <T> CompletableFuture<Response> okAsync(Supplier<T> supplier) {
        return handleAsyncExecution(supplier, ResponseWrapper::okJsonResponse);
    }

    public static CompletableFuture<Response> emptyOkResponse() {
        return CompletableFuture.completedFuture(ResponseWrapper.emptyOkResponse());
    }

    public static <T> CompletableFuture<Response> createdResponseWithLocationHeader(Supplier<T> supplier, Class<?> resourceClass, String locationUri) {
        return handleAsyncExecution(supplier, result -> ResponseWrapper.createdResponseWithLocationHeader(resourceClass, locationUri));
    }

    public static <T> CompletableFuture<Response> created(Supplier<T> supplier) {
        return handleAsyncExecution(supplier, ResponseWrapper::created);
    }

    public static CompletableFuture<Response> notFound(String msg) {
        return CompletableFuture.completedFuture(ResponseWrapper.notFound(msg));
    }

    public static <T> CompletableFuture<Response> okDataAsync(Supplier<T> supplier) {
        return handleAsyncExecution(supplier, result -> ResponseWrapper.toData(result));
    }

    public static <T> CompletableFuture<Response> okFuture(CompletableFuture<T> future) {
        return handleFuture(future, ResponseWrapper::okJsonResponse);
    }

    public static <T> CompletableFuture<Response> noContent(Supplier<CompletableFuture<Void>> supplier) {
        return handleAsyncExecution(supplier, result -> ResponseWrapper.noContentResponse());
    }

    public static CompletableFuture<Response> noContentAsync(Runnable runnable) {
        return handleAsyncExecution(() -> {
            runnable.run();
            return null;
        }, result -> ResponseWrapper.noContentResponse());
    }

    /**
     * Handles the result of a CompletableFuture and maps it to an appropriate HTTP response,
     * with standardized error handling.
     *
     * @param future         The CompletableFuture to handle
     * @param successHandler Function to convert successful result to Response
     * @param <T>            The type of result returned by the future
     * @return A CompletableFuture that will complete with an appropriate HTTP response
     */
    public static <T> CompletableFuture<Response> handleFuture(
            CompletableFuture<T> future,
            Function<T, Response> successHandler) {

        return future
                .orTimeout(DEFAULT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .thenApply(successHandler)
                .exceptionally(throwable -> {
                    Throwable cause = throwable.getCause() != null ? throwable.getCause() : throwable;
                    if (cause instanceof TimeoutException) {
                        log.warn("Request timed out for async processing", cause);
                        return ResponseWrapper.serverError(Optional.of("Request timed out. Please try again later."));
                    } else if (cause instanceof ApplicationEsperantoException applicationEsperantoException) {
                        log.warn("Application exception in completion {}", applicationEsperantoException.getMessage());
                        return ResponseWrapper.esperantoError(applicationEsperantoException);
                    } else {
                        log.warn("Unexpected error processing request", throwable);
                        return ResponseWrapper.serverError(Optional.of("An unexpected error occurred. Please try again later."));
                    }
                });
    }


}
