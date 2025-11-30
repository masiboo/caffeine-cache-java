package nl.ing.api.contacting.conf.resource;

import com.ing.api.contacting.dto.java.resource.blacklist.BlacklistItemDto;
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
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import lombok.extern.slf4j.Slf4j;
import nl.ing.api.contacting.conf.domain.ContactingBusinessFunctions;
import nl.ing.api.contacting.conf.mapper.BlacklistItemMapperJava;
import nl.ing.api.contacting.conf.resource.jaxrs.support.BaseResourceJava;
import nl.ing.api.contacting.conf.service.BlacklistService;
import nl.ing.api.contacting.trust.rest.feature.permissions.Permissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static nl.ing.api.contacting.conf.resource.jaxrs.support.AsyncUtils.*;

@Path("/contacting-conf/blacklisted-items/")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Slf4j
public class BlacklistResourceJava extends BaseResourceJava {
    private final BlacklistService blacklistService;

    @Autowired
    public BlacklistResourceJava(BlacklistService blacklistService) {
        this.blacklistService = blacklistService;
    }

    @GET
    @Path("{id}")
    @ApiOperation(
            value = "Get Blacklist items by functionality (The value of id must be functionality)",
            notes = "[Business Functions: BLACKLIST_MANAGEMENT, SYSTEM_TOOLING] Endpoint that retrieves all blacklist items by functionality")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "", response = BlacklistItemDto.class),
            @ApiResponse(code = 404, message = "not found")
    })
    @Permissions({
            ContactingBusinessFunctions.BLACKLIST_MANAGEMENT,
            ContactingBusinessFunctions.SYSTEM_TOOLING
    })
    public CompletableFuture<Response> getByFunctionality(@PathParam("id") @ApiParam(value = "functionality of blacklist item", required = true) String functionality) {
        return okDataAsync(() ->
                blacklistService.getAllByFunctionality(functionality, getContactingContext())
        );
    }

    @GET
    @ApiOperation(value = "Get all Blacklist items",
            notes = "[Business Function: BLACKLIST_MANAGEMENT] Get all blacklist items for an account id. ")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "", response = BlacklistItemDto.class, responseContainer = "List")
    })
    @Permissions({ContactingBusinessFunctions.BLACKLIST_MANAGEMENT})
    public CompletableFuture<Response> getAllBlackListItems() {

        return okDataAsync(() ->
                blacklistService.getAllBlacklistItems(getContactingContext())
        );
    }

    @POST
    @ApiOperation(value = "Create Blacklist item", notes = "[Business Function: BLACKLIST_MANAGEMENT] Create a blacklist item. ")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "", response = String.class)
    })
    @Permissions({ContactingBusinessFunctions.BLACKLIST_MANAGEMENT})
    public CompletableFuture<Response> createBlackListItem(@RequestBody BlacklistItemDto blacklistItemDto) {

        return handleAsyncExecution(
                () -> {
                    var itemData = BlacklistItemMapperJava.fromDto(blacklistItemDto.withId(Optional.empty()));
                    return blacklistService.createBlackListItem(itemData, getContactingContext());
                },
                result -> Response.created(
                                UriBuilder.fromResource(BlacklistResourceJava.class)
                                        .path(result.getId().toString())
                                        .build())
                        .type(MediaType.APPLICATION_JSON)
                        .build()
        );
    }

    @PUT
    @Path("{id}")
    @ApiOperation(value = "Update BlackList item by id", notes = "[Business Function: BLACKLIST_MANAGEMENT] Update a blacklist item. ")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "successfully updated blacklist item", response = BlacklistItemDto.class),
            @ApiResponse(code = 404, message = "Blacklist item not found")
    })
    @Permissions({ContactingBusinessFunctions.BLACKLIST_MANAGEMENT})
    public CompletableFuture<Response> updateBlackListItem(@PathParam("id") @ApiParam(value = "BlackList Item id", required = true) Long id, @RequestBody BlacklistItemDto blacklistItemDto) {
        return okAsync(() -> {
            var vo = BlacklistItemMapperJava.fromDto(blacklistItemDto.withId(Optional.of(id)));
            var result = blacklistService.updateBlackListItem(vo, getContactingContext());
            return BlacklistItemMapperJava.toDto(result);
        });
    }

    @DELETE
    @Path("{id}")
    @ApiOperation(value = "Delete BlackList item by id", notes = "[Business Function: BLACKLIST_MANAGEMENT] Delete a blacklist item. ")
    @ApiResponses(value = {
            @ApiResponse(code = 204, message = "Successfully deleted blacklist item"),
            @ApiResponse(code = 404, message = "Blacklist item not found")
    })
    @Permissions({ContactingBusinessFunctions.BLACKLIST_MANAGEMENT})
    public CompletableFuture<Response> deleteBlackListItem(@PathParam("id") @ApiParam(name = "id", value = "id", required = true) Long id) {
        return noContentAsync(() -> blacklistService.deleteBlackListItem(id));
    }
}
