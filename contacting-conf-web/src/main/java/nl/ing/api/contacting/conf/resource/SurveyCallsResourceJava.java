package nl.ing.api.contacting.conf.resource;

import io.swagger.annotations.*;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import nl.ing.api.contacting.conf.domain.ContactingBusinessFunctions;
import nl.ing.api.contacting.conf.domain.model.surveysetting.dto.AllSurveyOverviewDTO;
import nl.ing.api.contacting.conf.domain.model.surveysetting.dto.OfferedSurveyCallsDTO;
import nl.ing.api.contacting.conf.exception.Errors;
import nl.ing.api.contacting.conf.resource.jaxrs.support.BaseResourceJava;
import nl.ing.api.contacting.conf.service.SurveyService;
import nl.ing.api.contacting.trust.rest.feature.permissions.Permissions;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.CompletableFuture;

import static nl.ing.api.contacting.conf.resource.jaxrs.support.AsyncUtils.okAsync;

@Path("/contacting-conf/survey-calls")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Api(value = "offered survey calls api")
@Slf4j
public class SurveyCallsResourceJava extends BaseResourceJava {

    private final SurveyService surveyService;

    @Autowired
    public SurveyCallsResourceJava(SurveyService surveyService) {
        this.surveyService = surveyService;
    }

    @GET
    @ApiOperation(
            value = "Get all offered survey calls",
            notes = "[Business Function: SURVEY_MANAGEMENT] Endpoint for offered survey calls for a phone number")
    @ApiResponses(
            value = {
                    @ApiResponse(code = 200, message = "fetched offered survey calls", response = AllSurveyOverviewDTO.class)
            })
    @Permissions({ContactingBusinessFunctions.SURVEY_MANAGEMENT})
    public CompletableFuture<Response> getOfferedSurveyCalls(
            @QueryParam("phoneNum") @ApiParam(value = "phone number", required = true) String phoneNum) {

        // Validate phoneNum and handle missing parameter
        if (StringUtils.isBlank(phoneNum)) {
            throw Errors.badRequest("Phone number in the query parameter is required");
        }

        // Use okAsync for consistent response handling
        return okAsync(() -> {
            var result = surveyService.getOfferedSurveyCalls(account().friendlyName(), phoneNum);
            return new OfferedSurveyCallsDTO(result);
        });
    }
}

