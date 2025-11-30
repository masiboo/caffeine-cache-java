package nl.ing.api.contacting.conf.resource;

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
import nl.ing.api.contacting.conf.resource.dto.SettingsMetadataJavaDTOs;
import nl.ing.api.contacting.conf.resource.jaxrs.support.BaseResourceJava;
import nl.ing.api.contacting.conf.service.SettingsMetadataServiceJava;
import nl.ing.api.contacting.trust.rest.feature.permissions.Permissions;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.CompletableFuture;

import static nl.ing.api.contacting.conf.resource.jaxrs.support.AsyncUtils.okAsync;

@Path("/contacting-conf/settings-metadata")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Slf4j
public class SettingsMetadataResourceJava extends BaseResourceJava {

    private SettingsMetadataServiceJava settingsMetadataServiceJava;

    @Autowired
    public SettingsMetadataResourceJava(SettingsMetadataServiceJava settingsMetadataServiceJava) {
        this.settingsMetadataServiceJava = settingsMetadataServiceJava;
    }

    @GET
    @ApiOperation(
            value = "Get setting metadata",
            notes = "[Business Function: ACCOUNT_SETTING_MANAGEMENT] Endpoint for getting all settings metadata of platform")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Get setting metadata", response = SettingsMetadataJavaDTOs.class)
    })
    @Permissions({ContactingBusinessFunctions.TWILIO_LOGIN})
    public CompletableFuture<Response> getAllSettings() {
        return okAsync(() -> settingsMetadataServiceJava.getSettingsMetadata());
    }
}
