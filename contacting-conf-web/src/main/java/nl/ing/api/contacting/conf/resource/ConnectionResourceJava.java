package nl.ing.api.contacting.conf.resource;

import com.ing.api.contacting.dto.java.resource.connection.ActivateDto;
import com.ing.api.contacting.dto.java.resource.connection.ConnectionDetailsDtoJava;
import com.ing.api.contacting.dto.java.resource.connection.ConnectionModel;
import com.ing.api.contacting.dto.java.resource.connection.WebhookDto;
import com.ing.api.contacting.dto.resource.account.AccountDto;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import nl.ing.api.contacting.conf.domain.ContactingBusinessFunctions;
import nl.ing.api.contacting.conf.domain.model.connection.WebhookConnectionVO;
import nl.ing.api.contacting.conf.resource.dto.ConnectionModelV1;
import nl.ing.api.contacting.conf.resource.dto.TwilioRegionDTO;
import nl.ing.api.contacting.conf.resource.jaxrs.support.BaseResourceJava;
import nl.ing.api.contacting.conf.service.ConnectionDetailsServiceJava;
import nl.ing.api.contacting.trust.rest.context.EmployeeContext;
import nl.ing.api.contacting.trust.rest.feature.permissions.Permissions;
import nl.ing.api.contacting.trust.rest.param.SessionContextParam;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static nl.ing.api.contacting.conf.resource.jaxrs.support.AsyncUtils.noContent;
import static nl.ing.api.contacting.conf.resource.jaxrs.support.AsyncUtils.okAsync;

@Path("/contacting-conf/connections")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Slf4j
public class ConnectionResourceJava extends BaseResourceJava {

    private ConnectionDetailsServiceJava connectionDetailsService;

    @Autowired
    public ConnectionResourceJava(ConnectionDetailsServiceJava connectionDetailsService) {
        this.connectionDetailsService = connectionDetailsService;
    }

    @GET
    @Path("/regions")
    @ApiOperation(
            value = "Get all regions",
            notes = "[Business Function: SYSTEM_TOOLING] Endpoint for getting all regions for account")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Get All platform", response = TwilioRegionDTO.Regions.class)
    })
    @Permissions(ContactingBusinessFunctions.SYSTEM_TOOLING)
    public CompletableFuture<TwilioRegionDTO.Regions> getAllRegions() {
        return CompletableFuture.completedFuture(TwilioRegionDTO.allRegions());
    }


    /**
     * Retrieves all connections and webhooks.
     *
     * @return a CompletableFuture containing the HTTP response with all connections
     */
    @GET
    @ApiOperation(value = "Get all connections", notes = "Endpoint for getting all connections")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Get all", response = ConnectionModelV1.AllConnections.class),
            @ApiResponse(code = 404, message = "Connection not found")
    })
    @Permissions({ContactingBusinessFunctions.SYSTEM_TOOLING})
    public CompletableFuture<Response> getAllConnections() {
        return okAsync(() ->
                connectionDetailsService.getAllConnections()
        );
    }

    /**
     * Retrieves all connections and webhooks for a specific account asynchronously.
     *
     * @return a CompletableFuture containing the HTTP response with all connections for the account
     */
    @GET
    @Path("/account")
    @ApiOperation(value = "Get all for account", notes = "Endpoint for getting all connections for one account")
    @ApiResponses({
            @ApiResponse(code = 200, message = "", response = ConnectionModel.AllConnections.class),
            @ApiResponse(code = 404, message = "Connection not found")
    })
    @Permissions({ContactingBusinessFunctions.SYSTEM_TOOLING})
    public CompletableFuture<Response> getAllConnectionForAccount(@QueryParam("bypassCache") @DefaultValue("false") boolean bypassCache) {

        return okAsync(() ->
                connectionDetailsService.getAllConnectionsForAccount(getContactingContext().withCache(bypassCache))
        );

    }

    /**
     * Retrieves all backend connections for a specific account.
     *
     * @param accountId the account ID to filter backend connections
     * @return a CompletableFuture containing the HTTP response with all backend connections for the account
     */
    @GET
    @Path("/account/{accountId}/backend")
    @ApiOperation(value = "Get all backend", notes = "Endpoint for getting all backend connections for account")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Get all backend", response = ConnectionModel.BackendConnections.class),
            @ApiResponse(code = 404, message = "Connection not found")
    })
    @Permissions({ContactingBusinessFunctions.SYSTEM_TOOLING})
    public CompletableFuture<Response> getAllBackend(@PathParam("accountId") Long accountId) {
        return okAsync(() ->
                connectionDetailsService.getAllBackend(accountId)
        );
    }

    /**
     * Retrieves all backend connections for a specific account.
     *
     * @param accountId the account ID to filter backend connections
     * @return a CompletableFuture containing the HTTP response with all backend connections for the account
     */
    @GET
    @Path("/account/{accountId}/frontend")
    @ApiOperation(value = "Get all frontend", notes = "Endpoint for getting all frontend connections for account")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Get all frontend", response = ConnectionModel.FrontendConnections.class),
            @ApiResponse(code = 404, message = "Connection not found")
    })
    @Permissions({ContactingBusinessFunctions.SYSTEM_TOOLING})
    public CompletableFuture<Response> getAllFrontEnd(@PathParam("accountId") Long accountId) {
        final Long effectiveAccountId = (accountId != null) ? accountId : getContactingContext().accountId();
        return okAsync(() ->
                connectionDetailsService.getAllFrontEnd(effectiveAccountId)
        );
    }

    /**
     * Activates connections and webhooks for an account.
     * Receives ActivateDto, validates only one active webhook, updates connections, and activates webhooks.
     *
     * @return
     */
    @PUT
    @Path("/activate")
    @ApiOperation(
            value = "Activate connections",
            notes = "[Business Function: SYSTEM_TOOLING] Endpoint for activating connections"
    )
    @ApiResponses({
            @ApiResponse(code = 204, message = "Activate"),
            @ApiResponse(code = 400, message = "Only one active webhook allowed"),
            @ApiResponse(code = 500, message = "Activation failed")
    })
    @Permissions({ContactingBusinessFunctions.SYSTEM_TOOLING})
    public CompletableFuture<Response> activateConnections(ActivateDto activateDto) {
        return noContent(() -> CompletableFuture.runAsync(
                () -> connectionDetailsService.createActivateConnections(activateDto)
        ));
    }

    @GET
    @Path("/me")
    @Permissions({ContactingBusinessFunctions.TWILIO_LOGIN})
    @ApiOperation(
            value = "Get agent connections",
            notes = "[Business Function: SYSTEM_TOOLING] Endpoint for getting agent connection settings"
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Get agent connections", response = ConnectionModel.class),
            @ApiResponse(code = 404, message = "Connection not found")
    })
    public CompletableFuture<Response> getAgentConnections(
            @SessionContextParam EmployeeContext employeeContext,
            @HeaderParam("X-Forwarded-For") String ipAddresses) {

        //Logger statement is info as per existing scala implementation
        var accountName = employeeContext.getSubAccount()
                .map(AccountDto::friendlyName)
                .orElse("");
        log.info("Fetching all connections for employee {} for account: {}", employeeContext.employeeId(), accountName);

        return okAsync(() ->
                connectionDetailsService.getAgentConnectionSettings(getContactingContext(), ipAddresses));
    }

    @GET
    @Path("/v2")
    @ApiOperation(
            value = "Get all connections v2",
            notes = "[Business Function: SYSTEM_TOOLING] Endpoint for getting connections")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Get all", response = ConnectionModel.AllConnections.class),
            @ApiResponse(code = 404, message = "Connection not found")
    })
    @Permissions(ContactingBusinessFunctions.SYSTEM_TOOLING)
    public CompletableFuture<Response> getAllConnectionsV2() {
        return okAsync(() -> connectionDetailsService.getAllConnectionsV2());
    }

    @PUT
    @Path("/webhook")
    @ApiOperation(
            value = "Update webhook details",
            notes = "[Business Function: SYSTEM_TOOLING] Endpoint for updating webhook details"
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Update webhook details", response = WebhookConnectionVO.class),
            @ApiResponse(code = 404, message = "Connection not found")
    })
    @Permissions(ContactingBusinessFunctions.SYSTEM_TOOLING)
    public CompletableFuture<Response> updateWebhookDetails(@ApiParam(value = "dtos", required = true) List<WebhookDto> dtos) {
        return okAsync(() ->
                connectionDetailsService.updateWebhookDetails(dtos)
        );
    }

    @PUT
    @ApiOperation(
            value = "Update connection details",
            notes = "[Business Function: SYSTEM_TOOLING] Endpoint for updating connection details of a connection"
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Update connection details"),
            @ApiResponse(code = 404, message = "Connection not found")
    })
    @Permissions({ContactingBusinessFunctions.SYSTEM_TOOLING})
    @Path("{connectionId}/details/{detailId}")
    public CompletableFuture<Response> updateConnectionDetails(
            @PathParam("connectionId") @ApiParam(value = "Connection id", required = true) Long connectionId,
            @PathParam("detailId") @ApiParam(value = "Detail Id", required = true) Long detailId,
            ConnectionDetailsDtoJava connectionDetailsDto) {

        return okAsync( () ->
                connectionDetailsService.updateConnectionDetails(connectionDetailsDto, connectionId, detailId)
        );
    }

}
