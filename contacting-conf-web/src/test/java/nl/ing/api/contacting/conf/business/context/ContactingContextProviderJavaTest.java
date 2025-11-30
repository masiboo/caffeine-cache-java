package nl.ing.api.contacting.conf.business.context;

import com.ing.api.contacting.dto.java.context.AuditContext;
import com.ing.api.contacting.dto.java.context.ContactingContext;
import com.ing.api.contacting.dto.resource.account.AccountDto;
import nl.ing.api.contacting.trust.rest.context.SessionContext;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ContactingContextProviderJavaTest {

    @Test
    void testGetContactingContext() {
        AccountDto accountDto = mock(AccountDto.class);
        when(accountDto.id()).thenReturn(42L);

        SessionContext sessionContext = mock(SessionContext.class);
        Optional<SessionContext> sessionOpt = Optional.of(sessionContext);

        ContactingContext context = ContactingContextProviderJava.getContactingContext(accountDto, sessionOpt);

        assertNotNull(context);
        assertEquals(42L, context.accountId());
        assertNotNull(context.auditContext());
    }

    @Test
    void testGetSystemContactingContext() {
        Long accountId = 99L;
        ContactingContext context = ContactingContextProviderJava.getSystemContactingContext(accountId);

        assertNotNull(context);
        assertEquals(accountId, context.accountId());
        AuditContext auditContext = context.auditContext();
        assertNotNull(auditContext);
        assertEquals("System", auditContext.modifiedBy());
        assertEquals(Optional.of(accountId), auditContext.accountId());
    }
}