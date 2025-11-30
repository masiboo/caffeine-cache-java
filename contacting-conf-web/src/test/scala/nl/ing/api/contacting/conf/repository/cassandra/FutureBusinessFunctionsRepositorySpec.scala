package nl.ing.api.contacting.conf.repository.cassandra

import cats.data.EitherT
import com.datastax.oss.driver.api.core.{ConsistencyLevel, CqlSession}
import nl.ing.api.contacting.caching.hazelcast.cache.{DMultiMapCache, DMultiMapNoop}
import nl.ing.api.contacting.conf.domain.enums._
import nl.ing.api.contacting.conf.domain.{BusinessFunctionVO, NonEmployeeBusinessFunctionVO}
import nl.ing.api.contacting.conf.mapper.permissions.PermissionsMapper.ORG_ID_FOR_ACCOUNT
import nl.ing.api.contacting.conf.repository.cslick.CoCoDBComponent
import nl.ing.api.contacting.conf.support.{SystemModuleMockingSupport, TestModule}
import nl.ing.api.contacting.shared.client.ContactingAPIClient
import nl.ing.api.contacting.test.cassandra.ContactingCassandraSpec
import nl.ing.api.contacting.trust.rest.feature.permissions._
import nl.ing.api.contacting.util.exception.ContactingBusinessError
import org.mockito.Mockito
import org.scalatest.BeforeAndAfter
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.junit.JUnitRunner
import org.springframework.context.ApplicationContext

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, Future}

/**
 * Created by bo55nk on 6/9/17.
 */

@org.junit.runner.RunWith(value = classOf[JUnitRunner])
class FutureBusinessFunctionsRepositorySpec
    extends ContactingCassandraSpec[TestModule] with BeforeAndAfter with SystemModuleMockingSupport with ScalaFutures {


  override val timeout = 30000L
  override val recreateDatabase = false

  override lazy val keySpaceName: String = "contacting"

  val account = "nl-junit"

  val businessFunctions = List(
    BusinessFunctionVO(account, "listen-recordings", AGENT.role, TEAM, ORG_ID_FOR_ACCOUNT),
    BusinessFunctionVO(account, "listen-recordings", SUPERVISOR.role, CIRCLE, ORG_ID_FOR_ACCOUNT),
    BusinessFunctionVO(account, "listen-recordings", ADMIN.role, SUPER_CIRCLE, ORG_ID_FOR_ACCOUNT),
    BusinessFunctionVO(account, "chat", AGENT.role, SELF, ORG_ID_FOR_ACCOUNT),
    BusinessFunctionVO(account, "chat", CUSTOMER_AUTHENTICATED.role, SELF, ORG_ID_FOR_ACCOUNT),
    BusinessFunctionVO(account, "chat", CUSTOMER_UNAUTHENTICATED.role, SELF, ORG_ID_FOR_ACCOUNT),
    BusinessFunctionVO(account, "user-management", SUPERVISOR.role, TEAM, ORG_ID_FOR_ACCOUNT),
    BusinessFunctionVO(account, "user-management", ADMIN.role, CIRCLE, ORG_ID_FOR_ACCOUNT),
    BusinessFunctionVO(account, "user-management", ACCOUNT_ADMIN.role, ACCOUNT, ORG_ID_FOR_ACCOUNT),
    BusinessFunctionVO(account, "inbound-calls", AGENT.role, SELF, ORG_ID_FOR_ACCOUNT),
    BusinessFunctionVO(account, "statistics", CONTACTING.role, ACCOUNT, ORG_ID_FOR_ACCOUNT),
    BusinessFunctionVO(account, "statistics", AGENT.role, SELF, ORG_ID_FOR_ACCOUNT)
  )
  val permissions = NonEmployeeBusinessFunctionVO(businessFunctions)


  override val module: TestModule = new TestModule {
    override implicit def springContext: ApplicationContext = mock[ApplicationContext]
    override val contactingAPIClient: ContactingAPIClient =
      mock[ContactingAPIClient]
    override val dBComponent: CoCoDBComponent =
      mock[CoCoDBComponent]
    override lazy val distributedCache: DMultiMapCache = DMultiMapNoop
    override def session(consistencyLevel: ConsistencyLevel): CqlSession = cassandraUnit.session
  }

  val spyModule = Mockito.spy(module)

  "Permission Mappings" should " be found by account" in {
    val result = Await.result(spyModule.businessFunctionsRepository.findByAccount("NL-dev"), 10.seconds)
    result.size shouldBe 5
    result should contain(BusinessFunctionVO("NL-dev", "get_recordings", "AGENT", SELF, ORG_ID_FOR_ACCOUNT))
  }

  "Permission Mappings" should " not be found by unknown account" in {
    val result = Await.result(spyModule.businessFunctionsRepository.findByAccount("NL-unknows"), 10.seconds)
    result.size shouldBe 0
  }

  "Permission Mappings" should " be upserted" in {
    Await.result(spyModule.businessFunctionsRepository.upsertPermissions(account, businessFunctions).value, 10.seconds)
    val verifyMe = Await.result(spyModule.businessFunctionsRepository.findByAccount(account), 10.seconds)
    verifyMe.size shouldBe 12
  }

  "Empty list " should " not be give an exception" in {
    Await.result(module.businessFunctionsRepository.upsertPermissions(account, Seq()).value, 10.seconds)
  }

  "Permission Mappings" should " be deleted" in {
    BusinessFunctionVO(account,
                       "inbound-calls",
                       CUSTOMER_AUTHENTICATED.role,
                       SELF,
                       ORG_ID_FOR_ACCOUNT)
    BusinessFunctionVO(account, "statistics", CUSTOMER_AUTHENTICATED.role, SELF, ORG_ID_FOR_ACCOUNT)
    BusinessFunctionVO(account, "chat", CUSTOMER_AUTHENTICATED.role, SELF, ORG_ID_FOR_ACCOUNT)
    val deleteThis = BusinessFunctionVO(account,
                                        "inbound-calls",
                                        CUSTOMER_AUTHENTICATED.role,
                                        SELF,
                                        ORG_ID_FOR_ACCOUNT)
    val futureEither: EitherT[Future, ContactingBusinessError, Seq[BusinessFunctionVO]] = spyModule.businessFunctionsRepository
      .upsertPermissions(account, List(deleteThis))
      .flatMap(_ => spyModule.businessFunctionsRepository.deletePermissions(account, List(deleteThis)))
      .flatMap {
        _ =>
          // CL Local Quorum
          EitherT.right(spyModule.businessFunctionsRepository.findByAccount(account))
      }

    val result = Await.result(futureEither.value, 10.seconds)
    result match {
      case Left(_) => fail("test failed")
      case Right(vos) => vos.exists(x => x.businessFunction == deleteThis.businessFunction && x.role == deleteThis.role) shouldBe false
    }
  }

  "Permission Mappings" should " not be deleted" in {
    val permissionsToNotDelete = List(
      BusinessFunctionVO(account,
                         "inbound-calls",
                         CUSTOMER_AUTHENTICATED.role,
                         SELF,
                         ORG_ID_FOR_ACCOUNT),
      BusinessFunctionVO(account,
                         "statistics",
                         CUSTOMER_AUTHENTICATED.role,
                         SELF,
                         ORG_ID_FOR_ACCOUNT),
      BusinessFunctionVO(account, "chat", CUSTOMER_AUTHENTICATED.role, SELF, ORG_ID_FOR_ACCOUNT)
    )
    val permissionsToAttemptToDelete = List(
      permissionsToNotDelete.head.copy(accountFriendlyName = "random string"),
      permissionsToNotDelete(1).copy(businessFunction = "random bf"),
      permissionsToNotDelete(2).copy(role = "random role")
      )
    val futureEither: EitherT[Future, ContactingBusinessError, Seq[BusinessFunctionVO]] =
      (module.businessFunctionsRepository
        .upsertPermissions(account, permissionsToNotDelete)
        .flatMap(_ => module.businessFunctionsRepository.deletePermissions(account, permissionsToAttemptToDelete))
        .flatMap(_ => EitherT.right(module.businessFunctionsRepository.findByAccount(account))))

    futureEither.value.map(v => {
      v match {
        case Right(s) => println(s)
        case _ => println(s"error $v")
      }
      v
    })

    val result = Await.result(futureEither.value, 10.seconds)
    result match {
      case Left(_) => fail("test failed")
      case Right(vos) => {
        vos should contain(permissionsToNotDelete.head)
        vos should contain(permissionsToNotDelete(1))
        vos should contain(permissionsToNotDelete(2))
      }
    }

  }

  override def clearTables(): Unit = {}

  before {
    Mockito.clearInvocations(spyModule)
  }

  val data = Some("cassandra/permissions.cql")
}

