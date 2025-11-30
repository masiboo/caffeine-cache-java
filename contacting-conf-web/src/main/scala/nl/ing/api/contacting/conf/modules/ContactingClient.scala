package nl.ing.api.contacting.conf.modules

import com.twitter.finagle.http.Response
import nl.ing.api.contacting.cc2.CC2GlobalExecutionContext
import nl.ing.api.contacting.shared.client.ContactingAPIClient
import nl.ing.api.contacting.shared.client.ContactingClientCacheConfig
import nl.ing.api.contacting.shared.client.DefaultContactingAPIClient
import nl.ing.api.contacting.shared.client.StubContactingAPIClient
import nl.ing.api.contacting.shared.domain.TaskChannel.TaskChannel

import scala.concurrent.Future

/**
 * @author Ayush Mittal
 */
trait ContactingClient {
  val contactingAPIClient: ContactingAPIClient
}

trait DefaultContactingClient extends ContactingClient {

  this: CoreModule =>

  lazy val contactingAPIClient: ContactingAPIClient = {
    DefaultContactingAPIClient.createClient(manifest,
      routingResilientHttpClientWithRefererFilter,
      ContactingClientCacheConfig.createFromConfig(cacheConfig), CC2GlobalExecutionContext.Implicits.global)
  }
}

trait LocalContactingClient extends ContactingClient {
  this: CoreModule =>

  private lazy val defaultClient: DefaultContactingAPIClient =
    DefaultContactingAPIClient.createClient(manifest,
      routingResilientHttpClientWithRefererFilter,
      ContactingClientCacheConfig.createFromConfig(cacheConfig), CC2GlobalExecutionContext.Implicits.global)

  lazy val contactingAPIClient: ContactingAPIClient = new StubContactingAPIClient() {
    override def createTaskV2(taskChannel: TaskChannel, routingAttributes: Map[String, Any],
                              nonRoutingAttributes: Map[String, Any], customerData : scala.Option[scala.List[com.ing.api.contacting.dto.resource.tasks.CustomerTaskAttrs]],taskTTL: Option[Int],
                              country: Option[String], employeeId: Option[String]): Future[Response] = {
      defaultClient.createTaskV2(taskChannel, routingAttributes, nonRoutingAttributes, None, taskTTL, country, employeeId)
    }
  }
}
