package nl.ing.api.contacting.conf.business.context;

import com.ing.api.contacting.dto.java.context.AuditContext;
import com.ing.api.contacting.dto.java.context.ContactingContext;
import com.ing.api.contacting.dto.resource.account.AccountDto;
import nl.ing.api.contacting.trust.rest.context.EmployeeContext;
import nl.ing.api.contacting.trust.rest.context.SessionContext;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Utility class for creating ContactingContext objects.
 */
public final class ContactingContextProviderJava {

    private ContactingContextProviderJava() {
        // Private constructor to prevent instantiation
    }

    /**
     * Creates a ContactingContext from an AccountDto and SessionContext
     */
    public static ContactingContext getContactingContext(
            AccountDto accountDto,
            Optional<SessionContext> sessionContext) {
        return new ContactingContext(accountDto.id(), getAuditContext(sessionContext));
    }

    /**
     * Creates a system ContactingContext for a given account ID
     */
    public static ContactingContext getSystemContactingContext(Long accountId) {
        return new ContactingContext(
                accountId,
                new AuditContext("System", null, Optional.ofNullable(accountId), null)
        );
    }

    /**
     * Creates an AuditContext from a SessionContext
     */
    private static AuditContext getAuditContext(Optional<SessionContext> sessionContext) {
        return new AuditContext(
                getEmployeeId(sessionContext),
                Optional.of(LocalDateTime.now()),
                getOptionalAccount(sessionContext)
                        .map(AccountDto::id),
                Optional.empty()
        );
    }

    /**
     * Extracts the optional AccountDto from a SessionContext
     */
    private static Optional<AccountDto> getOptionalAccount(Optional<SessionContext> sessionContext) {
        return sessionContext.flatMap(SessionContext::getSubAccount);
    }

    /**
     * Extracts the employee ID from a SessionContext or returns "System" as default
     */
    private static String getEmployeeId(Optional<SessionContext> sessionContext) {
        return sessionContext
                .map(ctx ->(ctx.trustContext() instanceof EmployeeContext eC) ? eC.getEmployeeId() : "System")
                .orElseGet(() -> "System");
    }
}
