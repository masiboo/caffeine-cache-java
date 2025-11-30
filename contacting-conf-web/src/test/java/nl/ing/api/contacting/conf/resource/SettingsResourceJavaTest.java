package nl.ing.api.contacting.conf.resource;

import com.ing.api.contacting.dto.resource.account.AccountDto;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Response;
import nl.ing.api.contacting.conf.service.SettingsServiceJava;
import nl.ing.api.contacting.trust.rest.context.EmployeeContext;
import nl.ing.api.contacting.trust.rest.context.SessionContext;
import nl.ing.api.contacting.trust.rest.context.SessionContextAttributesHelper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import scala.jdk.javaapi.OptionConverters;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SettingsResourceJavaTest {

    @Mock
    private SettingsServiceJava settingsServiceJava;

    @Mock
    private EmployeeContext employeeContext;

    @Mock
    private SessionContext sessionContext;

    @Mock
    private AccountDto accountDto;

    @Mock
    private ContainerRequestContext containerRequestContext;

    @InjectMocks
    private SettingsResourceJava settingsResourceJava;

    private MockedStatic<SessionContextAttributesHelper> mockedHelper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockedHelper = mockStatic(SessionContextAttributesHelper.class);
    }

    @AfterEach
    void tearDown() {
        mockedHelper.close();
    }

    @Test
    @DisplayName("getMySettings returns 400 BadRequest when employee has no team")
    void returnsBadRequestWhenNoTeam() throws Exception {
        when(employeeContext.team()).thenReturn(OptionConverters.toScala(Optional.empty()));
        when(employeeContext.employeeId()).thenReturn("emp123");
        Response response = settingsResourceJava.getMySettings(employeeContext).get();

        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        assertEquals("Employee has no primary team", response.getEntity());
    }

}

