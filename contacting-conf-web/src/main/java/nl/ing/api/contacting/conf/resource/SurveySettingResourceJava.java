package nl.ing.api.contacting.conf.resource;

import io.swagger.annotations.*;
import jakarta.validation.constraints.NotNull;
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
import nl.ing.api.contacting.conf.domain.model.surveysetting.SurveyUpdateVO;
import nl.ing.api.contacting.conf.domain.model.surveysetting.dto.AllSurveyOverviewDTO;
import nl.ing.api.contacting.conf.domain.model.surveysetting.dto.SurveyAssociationUpdateDTO;
import nl.ing.api.contacting.conf.domain.model.surveysetting.dto.SurveyOverviewDTO;
import nl.ing.api.contacting.conf.domain.model.surveysetting.dto.SurveySettingDTO;
import nl.ing.api.contacting.conf.domain.model.surveysetting.dto.SurveyUpdateDTO;
import nl.ing.api.contacting.conf.exception.Errors;
import nl.ing.api.contacting.conf.mapper.SurveyMapperJava;
import nl.ing.api.contacting.conf.resource.jaxrs.support.BaseResourceJava;
import nl.ing.api.contacting.conf.service.SurveyService;
import nl.ing.api.contacting.trust.rest.feature.permissions.Permissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;

import java.net.URI;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static nl.ing.api.contacting.conf.resource.jaxrs.support.AsyncUtils.handleAsyncExecution;
import static nl.ing.api.contacting.conf.resource.jaxrs.support.AsyncUtils.noContentAsync;
import static nl.ing.api.contacting.conf.resource.jaxrs.support.AsyncUtils.okAsync;
import static nl.ing.api.contacting.conf.resource.jaxrs.support.AsyncUtils.okFuture;
import static nl.ing.api.contacting.conf.util.ValidationUtils.validatedLong;

@Path("/contacting-conf/survey-setting")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Api(value = "survey settings api")
@Slf4j
public class SurveySettingResourceJava extends BaseResourceJava {
    private static final String SURVEY_ID_NAME = "Survey Setting";
    private final SurveyService surveyService;

    @Autowired
    public SurveySettingResourceJava(SurveyService surveyService) {
        this.surveyService = surveyService;
    }

    @GET
    @ApiOperation(
            value = "Get all survey settings for account",
            notes = "[Business Function: SURVEY_MANAGEMENT] Endpoint for survey settings for an account")
    @ApiResponses({
            @ApiResponse(code = 200, message = "fetched survey settings", response = AllSurveyOverviewDTO.class),
            @ApiResponse(code = 404, message = "Survey-setting not found")
    })
    @Permissions({ContactingBusinessFunctions.SURVEY_MANAGEMENT})
    public CompletableFuture<Response> getSurveySettingsForAccount() {
        return okAsync(() -> {
                    List<SurveyOverviewDTO> result = surveyService.getAllSurveySettings(getContactingContext());
                    return new AllSurveyOverviewDTO(result);
                }
        );
    }

    @GET
    @Path("{id}")
    @ApiOperation(
            value = "Get survey settings by id",
            notes = "[Business Function: SURVEY_MANAGEMENT] Endpoint for survey settings for a given survey")
    @ApiResponses({
            @ApiResponse(code = 200, message = "fetched survey settings", response = SurveySettingDTO.class),
            @ApiResponse(code = 404, message = "survey setting does not exist"),
            @ApiResponse(code = 400, message = "validation error")
    })
    @Permissions({ContactingBusinessFunctions.SURVEY_MANAGEMENT})
    public CompletableFuture<Response> getSurveySettingById(
            @PathParam("id") @ApiParam(value = "survey setting Id", required = true) Long id) {

        Long validatedId = validatedLong(id, SURVEY_ID_NAME);

        return okFuture(
                surveyService.getSurveyDetailsVO(validatedId, getContactingContext())
                        .thenApply(optionalVO ->
                                optionalVO.map(SurveyMapperJava::surveyDetailsVOToOverviewDto)
                                        .orElseThrow(() -> Errors.valueMissing("Survey Setting with id: " + validatedId + " not found")))
        );
    }

    @POST
    @ApiOperation(
            value = "Create survey setting",
            notes = "[Business Function: SURVEY_MANAGEMENT] Endpoint for creating survey settings")
    @ApiResponses({
            @ApiResponse(code = 201, message = "created survey settings", response = String.class),
            @ApiResponse(code = 400, message = "validation error")
    })
    @Permissions({ContactingBusinessFunctions.SURVEY_MANAGEMENT})
    public CompletableFuture<Response> createSurveySetting(
            @RequestBody @ApiParam(value = "survey setting dto", required = true) SurveySettingDTO dto) {

        SurveySettingDTO.validateCallflowForChannel(dto);

        return handleAsyncExecution(
                () -> surveyService.createSurveySetting(SurveyMapperJava.surveySettingDTOToVo(dto, getContactingContext().accountId())),
                vo -> Response.created(
                                uriBuilderForResource(SurveySettingResourceJava.class, String.valueOf(vo.id().orElse(-1L)))
                        )
                        .type(MediaType.APPLICATION_JSON)
                        .build()
        );
    }

    @PUT
    @Path("{id}")
    @ApiOperation(
            value = "Update survey setting",
            notes = "[Business Function: SURVEY_MANAGEMENT] Endpoint for updating survey settings")
    @ApiResponses({
            @ApiResponse(code = 200, message = "updated survey settings", response = SurveyUpdateDTO.class),
            @ApiResponse(code = 400, message = "validation error"),
            @ApiResponse(code = 404, message = "Survey-setting not found")
    })
    @Permissions({ContactingBusinessFunctions.SURVEY_MANAGEMENT})
    public CompletableFuture<Response> updateSurveySetting(
            @PathParam("id") @ApiParam(value = "survey setting Id", required = true) Long id,
            @RequestBody @ApiParam(value = "survey setting dto", required = true) SurveyUpdateDTO dto) {

        SurveyUpdateDTO.validatePhFormat(dto);
        SurveySettingDTO.validateName(dto.settings());
        Long validatedId = validatedLong(id, SURVEY_ID_NAME);

        SurveyUpdateDTO updatedDTO = dto.withSettings(dto.settings().withId(validatedId));

        SurveyUpdateVO mappedVO = SurveyMapperJava.surveyUpdateDTOToVo(updatedDTO, getContactingContext().accountId(), validatedId);
        return okAsync(() ->
                surveyService.updateSurveySetting(mappedVO, getContactingContext())
        );
    }

    @PUT
    @Path("{id}/organizations")
    @ApiOperation(
            value = "Update survey setting orgs",
            notes = "[Business Function: SURVEY_MANAGEMENT] Endpoint for updating survey settings orgs")
    @ApiResponses({
            @ApiResponse(code = 202, message = "updated survey settings orgs"),
            @ApiResponse(code = 400, message = "validation error"),
            @ApiResponse(code = 404, message = "Survey-setting not found")
    })
    @Permissions({ContactingBusinessFunctions.SURVEY_MANAGEMENT})
    public CompletableFuture<Response> updateSurveySettingOrgs(
            @PathParam("id") @ApiParam(value = "survey setting Id", required = true) Long id,
            @RequestBody @ApiParam(value = "survey setting association update dto", required = true) SurveyAssociationUpdateDTO dto) {

        Long validatedId = validatedLong(id, SURVEY_ID_NAME);
        return noContentAsync(() ->
                surveyService.addRemoveOrgs(validatedId, dto)
        );
    }

    @PUT
    @Path("{id}/taskqueues")
    @ApiOperation(
            value = "Update survey setting taskqueues",
            notes = "[Business Function: SURVEY_MANAGEMENT] Endpoint for updating survey settings taskqueues")
    @ApiResponses({
            @ApiResponse(code = 202, message = "updated survey settings taskqueues"),
            @ApiResponse(code = 400, message = "validation error"),
            @ApiResponse(code = 404, message = "Survey-setting not found")
    })
    @Permissions({ContactingBusinessFunctions.SURVEY_MANAGEMENT})
    public CompletableFuture<Response> updateSurveySettingTaskQueues(
            @PathParam("id") @ApiParam(value = "survey setting Id", required = true) Long id,
            @RequestBody @ApiParam(value = "survey setting association update dto", required = true) @NotNull SurveyAssociationUpdateDTO dto) {

        Long validatedId = validatedLong(id, SURVEY_ID_NAME);
        return noContentAsync(() ->
                surveyService.addRemoveTaskQueues(validatedId, dto)
        );
    }

    @DELETE
    @Path("{id}")
    @ApiOperation(
            value = "Delete survey setting",
            notes = "[Business Function: SURVEY_MANAGEMENT] Endpoint for deleting survey settings")
    @ApiResponses({
            @ApiResponse(code = 204, message = "survey is deleted"),
            @ApiResponse(code = 404, message = "Survey-setting not found")
    })
    @Permissions({ContactingBusinessFunctions.SURVEY_MANAGEMENT})
    public CompletableFuture<Response> deleteSurveySetting(
            @PathParam("id") @ApiParam(value = "survey setting Id", required = true) Long id) {

        Long validatedId = validatedLong(id, SURVEY_ID_NAME);
        return noContentAsync(() ->
                surveyService.deleteSurveySetting(validatedId, getContactingContext())
        );
    }

    // Helper for URI building
    private static URI uriBuilderForResource(Class<?> resourceClass, String id) {
        return UriBuilder.fromResource(resourceClass)
                .path(id)
                .build();
    }
}

