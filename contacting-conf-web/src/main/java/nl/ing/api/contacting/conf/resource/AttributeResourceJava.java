package nl.ing.api.contacting.conf.resource;

import com.ing.api.contacting.dto.java.resource.attribute.AttributeDto;
import com.ing.api.contacting.dto.java.resource.attribute.AttributeDtos;
import com.ing.api.contacting.dto.java.resource.attribute.GroupedAttributeDtos;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.DefaultValue;
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
import nl.ing.api.contacting.conf.resource.jaxrs.support.BaseResourceJava;
import nl.ing.api.contacting.conf.service.AttributeService;
import nl.ing.api.contacting.trust.rest.feature.permissions.Permissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import static nl.ing.api.contacting.conf.resource.jaxrs.support.AsyncUtils.createdResponseWithLocationHeader;
import static nl.ing.api.contacting.conf.resource.jaxrs.support.AsyncUtils.emptyOkResponse;
import static nl.ing.api.contacting.conf.resource.jaxrs.support.AsyncUtils.noContentAsync;
import static nl.ing.api.contacting.conf.resource.jaxrs.support.AsyncUtils.okAsync;

@Slf4j
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Path("/contacting-conf/attributes")
public class AttributeResourceJava extends BaseResourceJava {

    private AttributeService attributeService;

    @Autowired
    public AttributeResourceJava(AttributeService attributeService) {
        this.attributeService = attributeService;
    }

    @GET
    @ApiOperation(
            value = "Get all attributes",
            notes = "[Business Function: ATTRIBUTE_MANAGEMENT] Endpoint for getting all attributes for account")
    @ApiResponses({
            @ApiResponse(code = 200, message = "", response = AttributeDto.class, responseContainer = "List"),
    })
    @Permissions({ContactingBusinessFunctions.CONTACT_HANDLING,
            ContactingBusinessFunctions.ATTRIBUTE_MANAGEMENT})
    public CompletableFuture<Response> getAllAttributes( @QueryParam("bypassCache") @DefaultValue("true") boolean bypassCache) {
        return okAsync(() ->  new AttributeDtos(attributeService.getAll(getContactingContext().withCache(bypassCache))));
    }

    @GET
    @ApiOperation(
            value = "Get grouped attributes",
            notes = "[Business Function: ATTRIBUTE_MANAGEMENT] Endpoint for getting all attributes for account")
    @ApiResponses({
            @ApiResponse(code = 200, message = "", response = GroupedAttributeDtos.class),
    })
    @Produces({("application/vnd.ing.contacting.grouped.attributes+json")})
    @Permissions({ContactingBusinessFunctions.CONTACT_HANDLING,
            ContactingBusinessFunctions.ATTRIBUTE_MANAGEMENT})
    public CompletableFuture<Response> getGroupedAttributes( @QueryParam("bypassCache") @DefaultValue("true") boolean bypassCache) {
        return okAsync(() ->  new AttributeDtos(attributeService.getAll(getContactingContext().withCache(bypassCache))).toGroupedDtos());
    }


    @GET
    @Path("/{id}")
    @ApiOperation(
            value = "Get attribute by id",
            notes = "[Business Function: ATTRIBUTE_MANAGEMENT] Endpoint for getting specific attribute for account")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Get all", response = AttributeDto.class),
            @ApiResponse(code = 404, message = "attribute does not exist")
    })
    @Permissions({
            ContactingBusinessFunctions.CONTACT_HANDLING,
            ContactingBusinessFunctions.ATTRIBUTE_MANAGEMENT
    })
    public CompletableFuture<Response> getAttribute(@PathParam("id") @ApiParam(value = "id", required = true) long id,
                                                    @QueryParam("bypassCache") @DefaultValue("true") boolean bypassCache) {
        log.debug("Fetching attribute for id {}", id);

        return okAsync(() -> attributeService.findById(id, getContactingContext().withCache(bypassCache)));
    }

    @POST
    @ApiOperation(
            value = "Create attribute",
            notes = "[Business Function: ATTRIBUTE_MANAGEMENT] Endpoint for creating an attribute")
    @ApiResponses({
            @ApiResponse(code = 201, message = "Create")
    })
    @Permissions({ContactingBusinessFunctions.ATTRIBUTE_MANAGEMENT})
    public CompletableFuture<Response> createAttribute(
            @ApiParam(value = "Attribute dto", required = true) AttributeDto attributeDto) {
        log.debug("Received attribute creation request for account {}, attribute - {}", getContactingContext().accountId(), attributeDto);

        return createdResponseWithLocationHeader(
                () -> null,
                AttributeResourceJava.class,
                attributeService.save(attributeDto, getContactingContext()).toString());
    }

    @PUT
    @Path("/{id}")
    @ApiOperation(
            value = "Update attribute by id",
            notes = "[Business Function: ATTRIBUTE_MANAGEMENT] Endpoint for updating a attribute"
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "successfully updated attribute"),
            @ApiResponse(code = 404, message = "attribute does not exist")
    })
    @Permissions({ContactingBusinessFunctions.ATTRIBUTE_MANAGEMENT})
    public CompletableFuture<Response> updateAttribute(@PathParam("id") @ApiParam(value = "id", required = true) long id,
                                                       @RequestBody AttributeDto attributeDto) {
        log.debug("Received attribute update request for account {}, attribute - {}", getContactingContext().accountId(), attributeDto);

        return okAsync(() -> attributeService.update(id, attributeDto, getContactingContext()));
    }

    @PUT
    @ApiOperation(
            value = "Update attributes",
            notes = "[Business Function: ATTRIBUTE_MANAGEMENT] Endpoint for updating attributes"
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "successfully updated attribute"),
            @ApiResponse(code = 500, message = "if one of the attribute does not exist")
    })
    @Permissions({ContactingBusinessFunctions.ATTRIBUTE_MANAGEMENT})
    public CompletableFuture<Response> updateAttributes(@RequestBody List<AttributeDto> attributeDtos) {

        log.debug("Received attributes update request for account {}, attribute id's - {}",
                getContactingContext().accountId(),
                attributeDtos.stream()
                        .map(dto -> dto.id().map(String::valueOf).orElse(""))
                        .toList());

        attributeService.updateAttributes(attributeDtos, getContactingContext());
        return emptyOkResponse();
    }


    @DELETE
    @Path("{id}")
    @ApiOperation(
            value = "Delete attribute by id",
            notes = "[Business Function: ATTRIBUTE_MANAGEMENT] Endpoint for deleting attributes"
    )
    @ApiResponses({
            @ApiResponse(code = 204, message = "successfully deleted attribute"),
            @ApiResponse(code = 404, message = "attribute not found")
    })
    @Permissions({ContactingBusinessFunctions.ATTRIBUTE_MANAGEMENT})
    public CompletionStage<Response> deleteAttribute(
            @PathParam("id") @ApiParam(value = "id", required = true) long attributeId) {

        log.debug("Received attribute delete request for account {}, attribute id - {}", getContactingContext().accountId(), attributeId);

        return noContentAsync(() -> attributeService.deleteById(attributeId, getContactingContext()));
    }
}
