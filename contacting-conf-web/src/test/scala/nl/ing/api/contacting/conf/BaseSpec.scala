package nl.ing.api.contacting.conf

import cats.effect.IO
import com.ing.api.contacting.dto.context.{ContactingContext, SlickAuditContext}
import com.ing.api.contacting.dto.resource.account.AccountDto
import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import nl.ing.api.contacting.conf.support.CoreModuleMockingSupport
import nl.ing.api.contacting.tracing.Trace
import org.mockito.Mockito
import org.scalatest.BeforeAndAfter
import org.scalatest.BeforeAndAfterEach
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.flatspec.AnyFlatSpecLike
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.junit.JUnitRunner
import org.scalatestplus.mockito.MockitoSugar

import scala.collection.mutable
import scala.reflect.Manifest

/**
  * @author Ayush Mittal
  */
@org.junit.runner.RunWith(value = classOf[JUnitRunner])
abstract class BaseSpec
    extends AnyFlatSpecLike with MockitoSugar with Matchers with CoreModuleMockingSupport with BeforeAndAfter with BeforeAndAfterEach with ScalaFutures {
  private var mocks: mutable.Set[Any] = mutable.Set.empty

  /**
   * init method before each test
   */
  def init(): Unit = {}

  /**
   * create a mock and store it in a Set so we can reuse the mock
   *
   * @param m the runtime info of class T
   * @tparam T the type of mock
   * @return
   */
  def mock[T](implicit m: Manifest[T]): T = {
    mocks.find(theClazz => theClazz.getClass == m.runtimeClass).map(mock => mock.asInstanceOf[T]).getOrElse {
      val mock = Mockito.mock(m.runtimeClass)
      mocks += mock
      mock.asInstanceOf[T]
    }
  }

  before {
    Mockito.reset(coreModule)
    init()
  }

  val config: Config = ConfigFactory.load("application-unit.conf")

  implicit val trace: Trace[IO] = Mockito.mock(classOf[Trace[IO]])

}
