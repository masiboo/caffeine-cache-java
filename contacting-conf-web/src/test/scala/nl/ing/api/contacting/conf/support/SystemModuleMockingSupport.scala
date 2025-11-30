package nl.ing.api.contacting.conf.support

import nl.ing.api.contacting.conf.modules.{CassandraModule, CoreModule}
import org.mockito.Mockito
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.Millis
import org.scalatest.time.Seconds
import org.scalatest.time.Span

trait TestModule extends CoreModule with CassandraModule with JunitKafkaModule

trait SystemModuleMockingSupport extends ScalaFutures {

  implicit lazy val configModule: CoreModule = Mockito.mock(classOf[CoreModule], Mockito.RETURNS_DEEP_STUBS)
  implicit val defaultPatience: PatienceConfig = PatienceConfig(timeout = Span(50, Seconds), interval = Span(500, Millis))

}
