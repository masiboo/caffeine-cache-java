package nl.ing.api.contacting.conf.resource

import cats.effect.IO
import com.ing.api.contacting.dto.audit.AuditContext
import com.ing.api.contacting.dto.resource.account.AccountDto
import com.ing.api.contacting.dto.resource.permissions.PermissionsDto
import com.typesafe.scalalogging.LazyLogging
import io.swagger.annotations._
import jakarta.ws.rs.container.{AsyncResponse, Suspended}
import jakarta.ws.rs.core.Response.Status
import jakarta.ws.rs.core.{MediaType, Response}
import jakarta.ws.rs._
import nl.ing.api.contacting.conf.business.permissions.PermissionAlgebra.systemToolingPermission
import nl.ing.api.contacting.conf.domain.BusinessFunctionVO
import nl.ing.api.contacting.conf.domain.enums.ContactingBusinessFunctionsScala
import nl.ing.api.contacting.conf.mapper.permissions.{BusinessFunctionMapper, PermissionsMapper}
import nl.ing.api.contacting.conf.modules.ExecutionContextConfig.executionContext
import nl.ing.api.contacting.conf.resource.dto.{BusinessFunctionsDto, BusinessFunctionsDtoWrapper}
import nl.ing.api.contacting.conf.resource.jaxrs.support.AuditContextSupport
import nl.ing.api.contacting.conf.util.ContactingCatsUtils.IOResponseOps
import nl.ing.api.contacting.trust.rest.context.{APISystemContext, AuthorizationContext, SessionContext}
import nl.ing.api.contacting.trust.rest.feature.permissions.Permissions
import nl.ing.api.contacting.trust.rest.param.SessionContextParam
import nl.ing.api.contacting.util.syntaxf.FutureSyntax.FutureAsync

import scala.concurrent.duration.{DurationInt, FiniteDuration}
import scala.concurrent.{Await, Future}
import scala.util.Try

/**
 * Resource for getting permissions based on business functions and user roles
 */
/*
@Path("/contacting-conf/permissions")
@Produces(Array(MediaType.APPLICATION_JSON))
@Api("contacting permissions api")
class PermissionsResource extends ConfigBaseResource with AuditContextSupport with LazyLogging {

  private lazy val timeout: FiniteDuration = 5.seconds

  implicit def toResponse(response: Future[Response]): Response = Await.result(response, timeout)

  @GET
  @Path("/me")
  @Produces(Array("application/vnd.ing.contacting.permissions-v2+json"))
  @ApiOperation(value = "Get permissions v2",
    notes = "API for getting permissions for a context")
  @ApiResponses(
    value = Array(
      new ApiResponse(code = 200, message = "permissions returned", response = classOf[PermissionsDto]),
      new ApiResponse(code = 403, message = "No authorizationContext present"),
    ),
  )
  def getPermissions_v2(@SessionContextParam sessionCtx: SessionContext,@Suspended asyncResponse: AsyncResponse): Unit = {
    val resourceSpan = span
    val accountDto = getAccount(sessionCtx)
    withAsyncIOResponse(asyncResponse){
      sessionCtx.trustContext match {
        case authorizationContext: AuthorizationContext =>
          coreModule.permissionReader.fetchPermissions(authorizationContext, accountDto.friendlyName).run(resourceSpan).toIOResponse {
            permissions =>
              Response.ok().entity(PermissionsMapper.toDto(permissions)).build()
          }
        case apiSystemContext: APISystemContext => //context for system requests for API. E.g Fetching all accounts, connections..
          logger.debug(s"Returning $systemToolingPermission for ${apiSystemContext.apiName}")
          IO.pure(Response.ok().entity(systemToolingPermission).build())
        case _ =>
          IO.pure(Response.status(Status.FORBIDDEN).entity("No authorizationContext present").build())
      }
    }
  }

  @GET
  @Path("{accountFriendlyName}/{employeeId}")
  @Permissions(Array(ContactingBusinessFunctionsScala.SYSTEM_TOOLING))
  @ApiOperation(value = "Get permissions for employee",
    notes = "[Business Function: SYSTEM_TOOLING] API for getting permissions for an employee")
  @ApiResponses(
    value = Array(
      new ApiResponse(code = 200, message = "permissions returned", response = classOf[PermissionsDto]),
      new ApiResponse(code = 404, message = "employee not found"),
    ),
  )
  def getPermissions(@Suspended asyncResponse: AsyncResponse, @PathParam("employeeId") @ApiParam(value = "employee id", required = true) employeeId: String,
                     @PathParam("accountFriendlyName") @ApiParam(value = "account friendly name", required = true) accountFriendlyName: String): Unit = {
    val resourceSpan = span
    withAsyncIOResponse(asyncResponse){
      coreModule.permissionAlgebraF.getPermissionsForEmployee(employeeId, accountFriendlyName).run(resourceSpan).toIOResponse {
        permissions =>
          Response.ok().entity(PermissionsMapper.toDto(permissions)).build()
      }
    }
  }

  @PUT
  @Permissions(Array(ContactingBusinessFunctionsScala.PERMISSIONS_ADMINISTRATION))
  @ApiOperation(value = "Update permissions for account",
    notes = "[Business Function: PERMISSIONS_ADMINISTRATION] API for updating permissions for a account")
  @ApiResponses(
    value = Array(
      new ApiResponse(code = 201, message = "permissions updated")
    ),
  )
  def updatePermissions(@Suspended asyncResponse: AsyncResponse, @ApiParam(value = "business function dto", required = true) permissions: List[BusinessFunctionsDto],
                        @SessionContextParam sessionContext: SessionContext): Unit = {
    val accountDto: AccountDto = getAccount(sessionContext)
    val auditCtx: AuditContext =
      auditContext(sessionContext)
    withAsyncIOResponse(asyncResponse) {
      val accountName: String = accountDto.friendlyName
      val businessFunctions: Seq[BusinessFunctionVO] = permissions.flatMap(p => {
        BusinessFunctionMapper.toVO(accountName, p)
      })
      coreModule.permissionService
        .syncBusinessFunctions(businessFunctions, accountName)(auditCtx)
        .map(_ => Response.noContent().build()).asDelayedF[IO]
    }
  }

  @GET
  @Permissions(Array(ContactingBusinessFunctionsScala.PERMISSIONS_ADMINISTRATION))
  @ApiOperation(value = "Get permissions for account",
    notes = "[Business Function: PERMISSIONS_ADMINISTRATION] API for getting permissions for a account")
  @ApiResponses(
    value = Array(
      new ApiResponse(code = 200, message = "permissions returned", response = classOf[Seq[BusinessFunctionsDto]]),
      new ApiResponse(code = 404, message = "no permissions found")
    ),
  )
  def getAllBusinessFunctions(@SessionContextParam sessionContext: SessionContext): Response = {
    val accountDTO = getAccount(sessionContext)
    coreModule.permissionService.getEditableBusinessFunctions(accountDTO.friendlyName).map {
      case Nil =>
        Response.status(Response.Status.NOT_FOUND).entity("no permissions found").build
      case xs  =>
        Response.ok().entity(BusinessFunctionsDtoWrapper(BusinessFunctionMapper.toDto(xs))).build
    }
  }

}
*/