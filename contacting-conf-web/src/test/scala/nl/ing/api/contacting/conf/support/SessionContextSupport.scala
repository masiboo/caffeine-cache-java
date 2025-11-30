package nl.ing.api.contacting.conf.support

import com.ing.api.contacting.dto.context.ContactingContext
import nl.ing.api.contacting.conf.business.context.ContactingContextProvider
import nl.ing.api.contacting.conf.modules.CoreModule
import nl.ing.api.contacting.test.jersey.JerseySupport
import nl.ing.api.contacting.test.jersey.ObjectMapperProvider
import nl.ing.api.contacting.trust.rest.SessionContextProvider
import nl.ing.api.contacting.trust.rest.context.SessionContext
import org.glassfish.jersey.server.ResourceConfig
import org.scalatest.BeforeAndAfter

/**
 * @author G.J. Compagner
 */
trait SessionContextSupport[T, DTO] extends JerseySupport[T, DTO] with SystemModuleMockingSupport with BeforeAndAfter with AccountSupport {

  private var currentSessionContext: SessionContext = _

  override def init = {
    initAutoMock
    setDependency(classOf[CoreModule], configModule)
  }
  override def sessionContext: SessionContext = currentSessionContext

  def setContext(sessionContext: SessionContext): Unit = {
    this.currentSessionContext = sessionContext
  }

  private val reportMailings =
    Map("MONTHLY_CHAT" -> "to@ing.nl", "MONTHLY_CHAT" -> "to-also@ing.nl")

  private val settings =
    Map("ACW" -> "ON", "permisions" -> "false", "multitasking" -> "false")

  override def context: ContactingContext =
    ContactingContextProvider.getContactingContext(account, Some(sessionContext))

  override protected def registerProviders(resourceConfig: ResourceConfig): Unit = {
    setPort()
    super.registerProviders(resourceConfig)
    resourceConfig.packages(true, "nl.ing.api.contacting.jaxrs")
    resourceConfig.register(classOf[SessionContextProvider])
    resourceConfig.register(classOf[ObjectMapperProvider])
    ()
  }

  /*override def init(): Unit = {
    super.init()
    setDependency(classOf[CoreModule], configModule)
  }*/

  def clearContext(): Unit = {}
}
