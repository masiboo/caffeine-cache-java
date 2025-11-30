package nl.ing.api.contacting.conf.resource;

import com.ing.api.contacting.dto.java.resource.PlatformAccountSettings;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import nl.ing.api.contacting.conf.domain.ContactingBusinessFunctions;
import nl.ing.api.contacting.conf.resource.jaxrs.support.BaseResourceJava;
import nl.ing.api.contacting.conf.service.AccountSettingsServiceJava;
import nl.ing.api.contacting.trust.rest.feature.permissions.Permissions;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.CompletableFuture;

import static nl.ing.api.contacting.conf.resource.jaxrs.support.AsyncUtils.okAsync;

@Path("/contacting-conf/accounts")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Slf4j
public class AccountsResourceJava extends BaseResourceJava {

    private AccountSettingsServiceJava accountSettingsService;

    @Autowired
    public AccountsResourceJava(AccountSettingsServiceJava accountSettingsService) {
        this.accountSettingsService = accountSettingsService;
    }

    @GET
    @Path("{id}/platform-account-settings")
    @ApiOperation(
            value = "Get all platform",
            notes = "[Business Function: SYSTEM_TOOLING] Endpoint for getting all platform account settings")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Get All platform", response = PlatformAccountSettings.class)
    })
    @Permissions(ContactingBusinessFunctions.SYSTEM_TOOLING)
    public CompletableFuture<Response> getAllPlatform(@PathParam("id") Long id, @QueryParam("bypassCache") @DefaultValue("false") boolean bypassCache) {

        return okAsync(
                () ->
                        accountSettingsService.getPlatformAccountSettings(getContactingContext().withAccountId(id).withCache(bypassCache))
        );
    }
}
