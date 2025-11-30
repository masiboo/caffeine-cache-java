package nl.ing.api.contacting.conf.resource;

import com.ing.api.contacting.dto.java.resource.employee.settings.EmployeeSettings;
import com.ing.api.contacting.dto.resource.organisation.FlatOrganisationUnitDto;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import nl.ing.api.contacting.conf.domain.ContactingBusinessFunctions;
import nl.ing.api.contacting.conf.resource.jaxrs.support.BaseResourceJava;
import nl.ing.api.contacting.conf.service.SettingsServiceJava;
import nl.ing.api.contacting.trust.rest.context.EmployeeContext;
import nl.ing.api.contacting.trust.rest.feature.permissions.Permissions;
import nl.ing.api.contacting.trust.rest.param.SessionContextParam;
import org.springframework.beans.factory.annotation.Autowired;
import scala.jdk.javaapi.OptionConverters;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static nl.ing.api.contacting.conf.resource.jaxrs.support.AsyncUtils.okAsync;
import static nl.ing.api.contacting.conf.util.Responses.badRequest;

@Path("/contacting-conf/settings")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Api("Employee Settings api")
@Slf4j
public class SettingsResourceJava extends BaseResourceJava {

    private final SettingsServiceJava settingsServiceJava;

    @Autowired
    public SettingsResourceJava(SettingsServiceJava settingsServiceJava) {
        this.settingsServiceJava = settingsServiceJava;
    }

    @GET
    @Path("me")
    @ApiOperation(
            value = "Get all settings for an employee",
            notes = "[Business Function: TWILIO_LOGIN] Endpoint for getting all organisation level settings for an employee")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "fetched all org settings for employee", response = EmployeeSettings.class),
            @ApiResponse(code = 400, message = "IF worker has no primary team")
    })
    @Permissions({ContactingBusinessFunctions.TWILIO_LOGIN})
    public CompletableFuture<Response> getMySettings(@SessionContextParam EmployeeContext employeeContext) {
        //TODO: use java for contacting trust
        Optional<FlatOrganisationUnitDto> primaryTeamOpt = OptionConverters.toJava(employeeContext.team());

        if (primaryTeamOpt.isEmpty()) {
            log.warn("Employee: {} has no primary team", employeeContext.employeeId());
            return CompletableFuture.completedFuture(badRequest("Employee has no primary team"));
        }

        String friendlyName = account().friendlyName();

        return okAsync(() -> settingsServiceJava.getAllSettingsForEmployee(
                getContactingContext(),
                friendlyName,
                primaryTeamOpt.get()
        ));
    }
}

