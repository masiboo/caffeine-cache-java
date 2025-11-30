package nl.ing.api.contacting.conf.resource;

import com.ing.api.contacting.dto.java.context.AuditContext;
import com.ing.api.contacting.dto.java.resource.organisation.BusinessFunctionsDto;
import com.ing.api.contacting.dto.resource.account.AccountDto;
import com.ing.api.contacting.dto.resource.permissions.PermissionsDto;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import nl.ing.api.contacting.conf.domain.ContactingBusinessFunctions;
import nl.ing.api.contacting.conf.domain.model.permission.BusinessFunctionVO;
import nl.ing.api.contacting.conf.domain.model.permission.BusinessFunctionsDtoWrapper;
import nl.ing.api.contacting.conf.exception.Errors;
import nl.ing.api.contacting.conf.mapper.BusinessFunctionMapper;
import nl.ing.api.contacting.conf.resource.jaxrs.support.BaseResourceJava;
import nl.ing.api.contacting.conf.service.PermissionService;
import nl.ing.api.contacting.trust.rest.context.APISystemContext;
import nl.ing.api.contacting.trust.rest.context.AuthorizationContext;
import nl.ing.api.contacting.trust.rest.context.EmployeeContext;
import nl.ing.api.contacting.trust.rest.context.SessionContext;
import nl.ing.api.contacting.trust.rest.feature.permissions.Permissions;
import nl.ing.api.contacting.trust.rest.param.SessionContextParam;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static nl.ing.api.contacting.conf.resource.jaxrs.support.AsyncUtils.*;
import static nl.ing.api.contacting.conf.service.PermissionService.systemToolingPermission;

@Path("/contacting-conf/permissions")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Slf4j
public class PermissionResourceJava extends BaseResourceJava {
    private final PermissionService permissionService;

    @Autowired
    public PermissionResourceJava(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    @GET
    @Path("/me")
    @Produces("application/vnd.ing.contacting.permissions-v2+json")
    @ApiOperation(value = "Get permissions v2",
            notes = "API for getting permissions for a context")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "permissions returned", response = PermissionsDto.class),
            @ApiResponse(code = 403, message = "No authorizationContext present")
    })
    public CompletableFuture<Response> getPermissionsV2(@SessionContextParam SessionContext sessionCtx) {
        AccountDto accountDto = accountFromRequestContext(sessionCtx);
        if (sessionCtx.trustContext() instanceof AuthorizationContext authContext) {
            return okAsync(() -> permissionService.fetchPermissions(authContext, getContactingContext(), accountDto.friendlyName()));
        } else if (sessionCtx.trustContext() instanceof APISystemContext apiContext) {
            log.debug("Returning {} for {}", systemToolingPermission, apiContext.apiName());
            return okAsync(() -> systemToolingPermission);
        }
        return okAsync(() -> Errors.forbidden("No authorizationContext present"));
    }

    @GET
    @Path("{accountFriendlyName}/{employeeId}")
    @Permissions(ContactingBusinessFunctions.SYSTEM_TOOLING)
    @ApiOperation(value = "Get permissions for employee",
            notes = "[Business Function: SYSTEM_TOOLING] API for getting permissions for an employee")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "permissions returned", response = PermissionsDto.class),
            @ApiResponse(code = 404, message = "employee not found")
    })
    public CompletableFuture<Response> getPermissions(
            @PathParam("employeeId") @ApiParam(value = "employee id", required = true) String employeeId,
            @PathParam("accountFriendlyName") @ApiParam(value = "account friendly name", required = true) String accountFriendlyName) {

        Map<String, Object> employeeBusinessFunction = permissionService.getPermissionsForEmployeeAndAccountFriendlyName(getContactingContext(), employeeId, accountFriendlyName);
        return okAsync(() -> employeeBusinessFunction);
    }

    @PUT
    @Permissions(ContactingBusinessFunctions.PERMISSIONS_ADMINISTRATION)
    @ApiOperation(value = "Update permissions for account",
            notes = "[Business Function: PERMISSIONS_ADMINISTRATION] API for updating permissions for a account")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "permissions updated")
    })
    public CompletableFuture<Response> updatePermissions(
            @ApiParam(value = "business function dto", required = true) List<BusinessFunctionsDto> permissions,
            @SessionContextParam SessionContext sessionContext,
            @QueryParam("bypassCache") @DefaultValue("false") boolean bypassCache) {
        AuditContext auditContext;
        if (sessionContext.trustContext() instanceof EmployeeContext employeeContext) {
            auditContext = new AuditContext(employeeContext.getEmployeeId(), accountFromRequestContext(sessionContext).id());
        } else {
            auditContext = new AuditContext("System");
        }

        AccountDto accountDto = accountFromRequestContext(sessionContext);

        List<BusinessFunctionVO> businessFunctionVOS = permissions.stream()
                .flatMap(permission ->
                        BusinessFunctionMapper.toBusinessFunctionVOList(accountDto.friendlyName(), permission).stream())
                .toList();

        return noContentAsync( () -> permissionService.syncBusinessFunctions(
                        getContactingContext().withCache(bypassCache),
                        businessFunctionVOS,
                        accountDto,
                        auditContext ));
    }

    @GET
    @ApiOperation(value = "Get permissions for account",
            notes = "[Business Function: PERMISSIONS_ADMINISTRATION] API for getting permissions for a account")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "permissions returned"),
            @ApiResponse(code = 404, message = "no permissions found")
    })
    @Permissions(ContactingBusinessFunctions.PERMISSIONS_ADMINISTRATION)

    public CompletableFuture<Response> getAllBusinessFunctions(
            @SessionContextParam SessionContext sessionCtx) {
        AccountDto accountDto = accountFromRequestContext(sessionCtx);
        List<BusinessFunctionVO> businessFunctionVOS = permissionService.getEditableBusinessFunctions(getContactingContext(), accountDto.friendlyName());
        if (businessFunctionVOS == null || businessFunctionVOS.isEmpty()) {
            throw Errors.notFound("no permissions found");
        }
        List<BusinessFunctionsDto> businessFunctionDtos = BusinessFunctionMapper.toDtoList(businessFunctionVOS);
        return okAsync(() -> new BusinessFunctionsDtoWrapper(businessFunctionDtos));
    }

}
