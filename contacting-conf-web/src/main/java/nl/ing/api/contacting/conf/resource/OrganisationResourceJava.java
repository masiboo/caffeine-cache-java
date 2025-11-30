package nl.ing.api.contacting.conf.resource;

import com.ing.api.contacting.dto.java.resource.organisation.OrganisationDto;
import com.ing.api.contacting.dto.java.resource.organisation.OrganisationSaveDto;
import com.ing.api.contacting.dto.java.resource.organisation.OrganisationsDto;
import io.swagger.annotations.*;
import jakarta.validation.Valid;
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
import lombok.extern.slf4j.Slf4j;
import nl.ing.api.contacting.conf.exception.Errors;
import nl.ing.api.contacting.conf.mapper.OrganisationMapperJava;
import nl.ing.api.contacting.conf.resource.jaxrs.support.BaseResourceJava;
import nl.ing.api.contacting.conf.service.OrganisationServiceJava;
import nl.ing.api.contacting.conf.util.ResponseWrapper;
import nl.ing.api.contacting.trust.rest.feature.permissions.Permissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.concurrent.CompletableFuture;

import static nl.ing.api.contacting.conf.domain.ContactingBusinessFunctions.ANNOUNCEMENT_MANAGEMENT;
import static nl.ing.api.contacting.conf.domain.ContactingBusinessFunctions.ORGANIZATION_ADMINISTRATION;
import static nl.ing.api.contacting.conf.domain.ContactingBusinessFunctions.REAL_TIME_QUEUE_DASHBOARDS;
import static nl.ing.api.contacting.conf.domain.ContactingBusinessFunctions.REAL_TIME_TEAM_DASHBOARDS;
import static nl.ing.api.contacting.conf.domain.ContactingBusinessFunctions.SURVEY_MANAGEMENT;
import static nl.ing.api.contacting.conf.domain.ContactingBusinessFunctions.SYSTEM_TOOLING;
import static nl.ing.api.contacting.conf.domain.ContactingBusinessFunctions.TWILIO_LOGIN;
import static nl.ing.api.contacting.conf.domain.ContactingBusinessFunctions.USER_ADMINISTRATION;
import static nl.ing.api.contacting.conf.mapper.OrganisationMapperJava.toEntity;
import static nl.ing.api.contacting.conf.resource.jaxrs.support.AsyncUtils.noContentAsync;
import static nl.ing.api.contacting.conf.resource.jaxrs.support.AsyncUtils.okAsync;

@Path("/contacting-conf/organisations/")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Api("contacting organisations api")
@Permissions({ORGANIZATION_ADMINISTRATION})
@Slf4j
public class OrganisationResourceJava extends BaseResourceJava {
    private final OrganisationServiceJava organisationService;

    @Autowired
    public OrganisationResourceJava(OrganisationServiceJava organisationService) {
        this.organisationService = organisationService;
    }

    @POST
    @ApiOperation(
            value = "create an organisation",
            notes = "[Business Function: ORGANIZATION_ADMINISTRATION] Endpoint for creating an organisation")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "organisation created")
    })

    public Response createOrganisation(
            @Valid @ApiParam(value = "organisations dto", required = true) @RequestBody OrganisationSaveDto organisationDtoJava) {


        Long id = organisationService.create(
                toEntity(organisationDtoJava, getContactingContext().accountId()),
                getContactingContext()
        );
        log.debug("Created organisation with ID: {}", id);
        return ResponseWrapper.createdResponse(OrganisationResourceJava.class, String.valueOf(id));

    }

    @PUT
    @ApiOperation(
            value = "update an organisation",
            notes = "[Business Function: ORGANIZATION_ADMINISTRATION] Endpoint for updating an organisation")
    @ApiResponses(value = {
            @ApiResponse(code = 204, message = "organisation updated"),
            @ApiResponse(code = 404, message = "organisation not found")
    })
    @Path("{id}")

    public CompletableFuture<Response> updateOrganisation(
            @PathParam("id") @ApiParam(value = "organisations id", required = true) Long organisationId,
            @ApiParam(value = "organisations dto", required = true) @RequestBody OrganisationSaveDto organisationDto) {

        return noContentAsync(() -> {
            organisationService.update(
                    toEntity(organisationDto.withId(organisationId), getContactingContext().accountId()),
                    getContactingContext()
            );
        });
    }


    @GET
    @ApiOperation(
            value = "retrieve an organisation by id",
            notes = "[Business Function: ORGANIZATION_ADMINISTRATION,USER_ADMINISTRATION,SYSTEM_TOOLING] Endpoint for retrieving an organisation by id")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "organisation retrieved", response = OrganisationDto.class),
            @ApiResponse(code = 404, message = "organisation not found")
    })
    @Path("{id}")
    @Permissions({
            ORGANIZATION_ADMINISTRATION,
            USER_ADMINISTRATION,
            SYSTEM_TOOLING
    })

    public CompletableFuture<Response> retrieveOrganisation(
            @PathParam("id") @ApiParam(value = "organisation id", required = true) Long id) {
        log.debug("JAVA [GET] /contacting-conf/organisations/{}", id);

        return okAsync(() ->
                organisationService.getById(id, getContactingContext())
                        .map(OrganisationMapperJava::toDto)
                        .orElseThrow(() -> Errors.notFound(String.format("Org %d not found", id)))
        );
    }


    @GET
    @ApiOperation(
            value = "Get all organisations for the account",
            notes = "[Business Function: ORGANIZATION_ADMINISTRATION,USER_ADMINISTRATION,SYSTEM_TOOLING,REAL_TIME_TEAM_DASHBOARDS," +
                    "REAL_TIME_AGENT_DASHBOARDS,REAL_TIME_QUEUE_DASHBOARDS,ANNOUNCEMENT_MANAGEMENT]" +
                    "Endpoint for retrieving all organisations for an account")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "organisations retrieved", response = OrganisationsDto.class)
    })
    @Permissions({
            ORGANIZATION_ADMINISTRATION,
            USER_ADMINISTRATION,
            REAL_TIME_TEAM_DASHBOARDS,
            REAL_TIME_QUEUE_DASHBOARDS,
            ANNOUNCEMENT_MANAGEMENT,
            SYSTEM_TOOLING,
            TWILIO_LOGIN
    })
    public CompletableFuture<Response> getAllOrganisations() {
        log.debug("JAVA [GET] /contacting-conf/organisations");
        return okAsync(() ->
                new OrganisationsDto(
                        organisationService.getOrganisationTree(getContactingContext())
                                .stream()
                                .map(OrganisationMapperJava::toDto)
                                .toList()
                )
        );

    }

    @DELETE
    @ApiOperation(
            value = "delete an organisation by id",
            notes = "[Business Function: ORGANIZATION_ADMINISTRATION] Endpoint for deleting an organisation by id")
    @ApiResponses(value = {
            @ApiResponse(code = 204, message = "organisation deleted"),
            @ApiResponse(code = 404, message = "organisation not found")
    })
    @Path("{id}")
    public CompletableFuture<Response> deleteOrganisation(
            @PathParam("id") @ApiParam(value = "organisation id", required = true) Long id) {
        log.debug("JAVA [DELETE] /contacting-conf/organisations/{}", id);

        return noContentAsync(() -> {
            int deletedRows = organisationService.delete(id, getContactingContext());
            if (deletedRows == 0) {
                throw Errors.notFound(String.format("Organisation with id %d not found", id));
            }
        });
    }

    @GET
    @Path("me")
    @ApiOperation(
            value = "Get organisations for employee",
            notes = "[Business Function: SURVEY_MANAGEMENT] Endpoint for getting organisations for worker")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "organisation retrieved", response = OrganisationsDto.class)
    })
    @Permissions({SURVEY_MANAGEMENT})
    public CompletableFuture<Response> myOrganisations() {
        log.debug("JAVA [GET] /contacting-conf/organisations/me");
        return okAsync(() ->
                new OrganisationsDto(
                        organisationService.getAllowedOrganisations(getSessionContext(), getContactingContext())
                                .stream()
                                .map(OrganisationMapperJava::toDto)
                                .toList()
                )
        );
    }

    @GET
    @Path("me/user-administration")
    @ApiOperation(
            value = "Get organisations for employee",
            notes = "{Business Function: USER_ADMINISTRATION] Endpoint for getting organisations for worker")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "organisation retrieved", response = OrganisationsDto.class)
    })
    @Permissions({USER_ADMINISTRATION})
    public CompletableFuture<Response> myOrganisationsUserAdministration() {
        log.debug("JAVA [GET] /contacting-conf/organisations/me/user-administration");
        return okAsync(() ->
                new OrganisationsDto(
                        organisationService.getAllowedOrganisations(getSessionContext(), getContactingContext())
                                .stream()
                                .map(OrganisationMapperJava::toDto)
                                .toList()
                )
        );
    }

}

