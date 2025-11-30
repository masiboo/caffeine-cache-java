package nl.ing.api.contacting.conf.exception;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import lombok.extern.slf4j.Slf4j;
import nl.ing.api.contacting.conf.util.ResponseWrapper;

/**
 * Exception handler for JAX-RS
 * We already have a provider for Scala so making this specific to ApplicationEsperantoException
 */
@Provider
@Slf4j
public class GlobalExceptionHandler implements ExceptionMapper<ApplicationEsperantoException> {
    
    @Override
    public Response toResponse(ApplicationEsperantoException e) {
        return ResponseWrapper.esperantoError(e);
    }
}