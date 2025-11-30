package nl.ing.api.contacting.conf.resource.jaxrs.support;

import com.ing.api.contacting.dto.java.context.ContactingContext;
import com.ing.api.contacting.dto.resource.account.AccountDto;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import nl.ing.api.contacting.conf.business.context.ContactingContextProviderJava;
import nl.ing.api.contacting.conf.exception.Errors;
import nl.ing.api.contacting.trust.rest.context.SessionContext;
import nl.ing.api.contacting.trust.rest.context.SessionContextAttributesHelper;
import nl.ing.api.contacting.trust.rest.request.RequestReaders;

import java.util.Optional;

import static nl.ing.api.contacting.conf.exception.Errors.notFound;

public abstract class BaseResourceJava {
    @Context
    protected ContainerRequestContext containerRequest;

    protected Optional<SessionContext> getSessionContext() {
        return SessionContextAttributesHelper.getSessionContext(containerRequest, RequestReaders.containerRequestReader());
    }
    protected AccountDto accountFromRequestContext(SessionContext context) {
        return Optional.ofNullable(context)
                .flatMap(SessionContext::getSubAccount)
                .orElseThrow(() ->
                        notFound(
                                String.format("Request has no subAccount hence Forbidden for context: %s",
                                        getSessionContext().map(SessionContext::describe).orElse("")))
                );
    }

    protected AccountDto account() {
        Optional<SessionContext> context = getSessionContext();
        return context
                .flatMap(SessionContext::getSubAccount)
                .orElseThrow(() ->
                        notFound(
                                String.format("Request has no subAccount hence Forbidden for context: %s",
                                        getSessionContext().map(SessionContext::describe).orElse("")))
                );
    }

    protected ContactingContext getContactingContext() {
        Optional<SessionContext> sessionContext = getSessionContext();
        if (!sessionContext.isPresent()) {
            throw Errors.serverError("Request has no SessionContext");
        }
        return ContactingContextProviderJava.getContactingContext(
                account(), sessionContext);
    }
}
