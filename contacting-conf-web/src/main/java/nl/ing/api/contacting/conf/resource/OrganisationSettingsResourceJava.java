package nl.ing.api.contacting.conf.resource;

import com.ing.api.contacting.dto.java.audit.AuditEntity;
import com.ing.api.contacting.dto.java.resource.organisation.settings.EmployeeOrganisationSettings;
import com.ing.api.contacting.dto.java.resource.organisation.settings.OrganisationSettingDto;
import com.ing.api.contacting.dto.java.resource.organisation.settings.OrganisationSettingDtos;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import nl.ing.api.contacting.conf.domain.ContactingBusinessFunctions;
import nl.ing.api.contacting.conf.domain.model.settingsmetadata.OrganisationSettingVO;
import nl.ing.api.contacting.conf.resource.jaxrs.support.BaseResourceJava;
import nl.ing.api.contacting.conf.service.OrganisationSettingsServiceJava;
import nl.ing.api.contacting.conf.util.ResponseWrapper;
import nl.ing.api.contacting.trust.rest.context.EmployeeContext;
import nl.ing.api.contacting.trust.rest.feature.permissions.Permissions;
import nl.ing.api.contacting.trust.rest.param.SessionContextParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static nl.ing.api.contacting.conf.resource.jaxrs.support.AsyncUtils.created;
import static nl.ing.api.contacting.conf.resource.jaxrs.support.AsyncUtils.okAsync;
import static nl.ing.api.contacting.conf.resource.jaxrs.support.AsyncUtils.okDataAsync;

@Path("/contacting-conf/organisation-settings")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Slf4j
public class OrganisationSettingsResourceJava extends BaseResourceJava {
    final  static  Integer DEFAULT_MAX_ROWS = 21;
    private OrganisationSettingsServiceJava organisationSettingService;

    @Autowired
    public OrganisationSettingsResourceJava(OrganisationSettingsServiceJava organisationSettingsService) {
        this.organisationSettingService = organisationSettingsService;
    }

    @GET
    @ApiOperation(value = "Get all organisation settings for an account",
            notes = "[Business Function: ACCOUNT_SETTING_MANAGEMENT] Endpoint for getting all organisation level settings")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "fetched all account settings for account", response = OrganisationSettingDtos.class)})
    @Permissions(ContactingBusinessFunctions.ACCOUNT_SETTING_MANAGEMENT)
    public CompletableFuture<Response> getAllOrgSettings(@QueryParam("capabilities")
                                                         @ApiParam(value = "capabilities", required = false) String capabilities) {
        return okDataAsync(() -> {
            if (capabilities == null || capabilities.isBlank()) {
                return organisationSettingService.getOrganisationSettingsWithOutCapabilities(getContactingContext());
            } else {
                return organisationSettingService.getOrganisationSettingsWithCapabilities(capabilities, getContactingContext());
            }
        });
    }

    @GET
    @Path("/me")
    @ApiOperation(value = "Get all organisation settings for an employee",
            notes = "[Business Function: TWILIO_LOGIN] Endpoint for getting all organisation level settings for an employee")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "fetched all org settings for employee", response = EmployeeOrganisationSettings.class)})
    @Permissions(ContactingBusinessFunctions.TWILIO_LOGIN)
    public CompletableFuture<Response> getMyOrganisationSettings(@SessionContextParam EmployeeContext employeeContext) {

        return okAsync(() -> organisationSettingService.getOrganisationSettingsForEmployee(
                getContactingContext(),
                employeeContext.restrictionPerOrganisation(),
                employeeContext.team()));
    }

    @POST
    @ApiOperation(value = "Create a organisation setting",
            notes = "[Business Function: ACCOUNT_SETTING_MANAGEMENT] Endpoint for saving organisation level setting")
    @ApiResponses(value = {@ApiResponse(code = 201, message = "saved org setting")})
    @Permissions(ContactingBusinessFunctions.ACCOUNT_SETTING_MANAGEMENT)
    public CompletableFuture<Response> saveOrganisationSetting(
            @RequestBody OrganisationSettingDto orgSetting) {

        return created(() -> organisationSettingService
                .createOrganisationSetting(orgSetting, getContactingContext()));

    }

    @PUT
    @Path("{id}")
    @ApiOperation(value = "Update a organisation setting",
            notes = "[Business Function: ACCOUNT_SETTINGS_MANAGEMENT] Endpoint for updating organisation level setting")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "", response = OrganisationSettingVO.class),
            @ApiResponse(code = 404, message = "organisation setting does not exist")})
    @Permissions(ContactingBusinessFunctions.ACCOUNT_SETTING_MANAGEMENT)
    public CompletableFuture<Response> updateOrganisationSetting(
            @PathParam("id") @ApiParam Long id, @RequestBody OrganisationSettingDto orgSetting) {

        return okAsync(() -> organisationSettingService
                .updateOrganisationSetting(orgSetting, id, getContactingContext()));

    }

    @DELETE
    @Path("{id}")
    @ApiOperation(value = "delete organisation setting",
            notes = "[Business Function: ACCOUNT_SETTING_MANAGEMENT] Endpoint for deleting organisation level settings")
    @ApiResponses(value = {
            @ApiResponse(code = 204, message = "deleted organisation setting"),
            @ApiResponse(code = 404, message = "organisation setting does not exist")})
    @Permissions(ContactingBusinessFunctions.ACCOUNT_SETTING_MANAGEMENT)
    public Response deleteOrganisationSetting(@PathParam("id") @ApiParam Long id) {

        organisationSettingService.deleteOrganisationSetting(id, getContactingContext());

        return ResponseWrapper.noContentResponse();
    }

    @GET
    @Path("revisions/{id}")
    @ApiOperation(
            value = "Get revisions of organisation settings",
            notes = "[Business Function:ACCOUNT_SETTING_MANAGEMENT] Endpoint for getting all organisation revisions")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "fetched all revisions", response = AuditEntity.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "organisation settings not found")})
    @Permissions(ContactingBusinessFunctions.ACCOUNT_SETTING_MANAGEMENT)
    public CompletableFuture<Response> auditedVersions(
            @PathParam("id") @ApiParam(value = "account settings id", required = true) Long id,
            @QueryParam("numRows") @ApiParam(value = "number of revisions to fetch, default 21", required = false) Integer numRows
    ) {
        int rows = Optional.ofNullable(numRows).orElse(DEFAULT_MAX_ROWS);
        return okDataAsync(() -> organisationSettingService.getAuditedVersions(id, rows, getContactingContext()));
    }
}
