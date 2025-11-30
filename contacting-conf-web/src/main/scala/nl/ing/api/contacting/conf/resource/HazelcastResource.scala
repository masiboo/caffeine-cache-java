package nl.ing.api.contacting.conf.resource

import nl.ing.api.contacting.conf.util.ContactingCatsUtils.ResultTResponseOps
import com.ing.api.contacting.dto.resource.hazelcast.{HazelcastCollectionDto, HazelcastInstanceSet, HazelcastMapEntriesDto, HazelcastMapEntryDto}
import io.swagger.annotations._
import jakarta.ws.rs.container.{AsyncResponse, Suspended}
import jakarta.ws.rs.core.{MediaType, Response}
import jakarta.ws.rs.{DELETE, GET, Path, PathParam, Produces}
import nl.ing.api.contacting.conf.domain.enums.ContactingBusinessFunctionsScala
import nl.ing.api.contacting.conf.util.Responses.{notFound, okJsonResponse}
import nl.ing.api.contacting.trust.rest.feature.permissions.Permissions

import scala.concurrent.duration.{DurationInt, FiniteDuration}
import scala.concurrent.{Await, Future}

/**
 * Resource for getting hazelcast info
 */
@Path("/contacting-conf/hz")
@Produces(Array(MediaType.APPLICATION_JSON))
@Api("hazelcast api")
class HazelcastResource extends ConfigBaseResource {

  private lazy val timeout: FiniteDuration = 5.seconds

  implicit def toResponse(response: Future[Response]): Response = Await.result(response, timeout)

  @DELETE
  @Path("/map/{mapname}/{keyname}")
  @Permissions(Array(ContactingBusinessFunctionsScala.SYSTEM_TOOLING))
  @ApiOperation(value = "delete map entry",
    notes = "[Business Function: SYSTEM_TOOLING] API for for deleting hazelcast multimap key. It returns the evicted cache collection")
  @ApiResponses(
    value = Array(
      new ApiResponse(code = 200, message = "evicted cache collection returned", response = classOf[HazelcastCollectionDto]),
      new ApiResponse(code = 404, message = "map-key entry not found"),
    ),
  )
  def evictMapKey(@Suspended asyncResponse: AsyncResponse, @PathParam("mapname") @ApiParam(value = "map name", required = true) mapName: String,
                     @PathParam("keyname") @ApiParam(value = "map key name", required = true) keyName: String): Unit = {

    withAsyncIOResponse(asyncResponse) {
      coreModule.hazelcastAlgebraF.removeKey(mapName, keyName).toIOResponse {
        case None =>
          notFound(s"map: ${mapName} with key: ${keyName} not found");
        case Some(hazelcastEntries) =>
          okJsonResponse(HazelcastCollectionDto(hazelcastEntries))
      }
    }
  }

  @DELETE
  @Path("/map/{mapname}")
  @Permissions(Array(ContactingBusinessFunctionsScala.SYSTEM_TOOLING))
  @ApiOperation(value = "delete map entry",
    notes = "[Business Function: SYSTEM_TOOLING] API for for deleting hazelcast multimap key. It returns the evicted cache collection")
  @ApiResponses(
    value = Array(
      new ApiResponse(code = 200, message = "evicted cache collection returned", response = classOf[HazelcastCollectionDto]),
      new ApiResponse(code = 404, message = "map-key entry not found"),
    ),
  )
  def evictMap(@Suspended asyncResponse: AsyncResponse, @PathParam("mapname") @ApiParam(value = "map name", required = true) mapName: String): Unit = {

    withAsyncIOResponse(asyncResponse) {
      coreModule.hazelcastAlgebraF.removeMap(mapName).toIOResponse {
        case None =>
          notFound(s"map: ${mapName}  not found");
        case Some(hazelcastEntries) =>
          okJsonResponse(HazelcastMapEntriesDto(hazelcastEntries))
      }
    }
  }

  @GET
  @Path("/map/{mapname}")
  @Permissions(Array(ContactingBusinessFunctionsScala.SYSTEM_TOOLING))
  @ApiOperation(value = "get all map entries",
    notes = "[Business Function: SYSTEM_TOOLING] API for for deleting hazelcast multimap key. It returns the evicted cache collection")
  @ApiResponses(
    value = Array(
      new ApiResponse(code = 200, message = "map cache entries returned", response = classOf[HazelcastMapEntriesDto]),
      new ApiResponse(code = 404, message = "map not found"),
    ),
  )
  def getAllEntries(@Suspended asyncResponse: AsyncResponse, @PathParam("mapname") @ApiParam(value = "map name", required = true) mapName: String): Unit = {

    withAsyncIOResponse(asyncResponse) {
      coreModule.hazelcastAlgebraF.getAllMapEntries(mapName).toIOResponse {
        case None =>
          notFound(s"map: ${mapName} not found");
        case Some(hazelcastEntries) =>
          okJsonResponse(HazelcastMapEntriesDto(hazelcastEntries))
      }
    }
  }

  @GET
  @Path("/instances")
  @Permissions(Array(ContactingBusinessFunctionsScala.SYSTEM_TOOLING))
  @ApiOperation(value = "get all hazelcast instances",
    notes = "[Business Function: SYSTEM_TOOLING] API for for getting all hazelcast cluster's instances")
  @ApiResponses(
    value = Array(
      new ApiResponse(code = 200, message = "hazelcast instances collection returned", response = classOf[HazelcastInstanceSet])
    )
  )
  def getAllInstances(@Suspended asyncResponse: AsyncResponse): Unit = {
    withAsyncIOResponse(asyncResponse) {
      coreModule.hazelcastAlgebraF.getAllInstances().toIOResponse (hazelcastInstances => okJsonResponse(HazelcastInstanceSet(hazelcastInstances)))
    }
  }

}
