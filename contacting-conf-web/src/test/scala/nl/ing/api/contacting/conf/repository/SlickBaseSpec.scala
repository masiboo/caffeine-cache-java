package nl.ing.api.contacting.conf.repository

import cats.effect.IO
import com.ing.api.contacting.dto.context.{ContactingContext, SlickAuditContext}
import com.typesafe.config.ConfigFactory
import nl.ing.api.contacting.business.CacheConfigProvider
import nl.ing.api.contacting.conf.repository.cslick.CoCoDBComponent
import com.ing.api.contacting.dto.resource.account.AccountDto
import nl.ing.api.contacting.domain.slick.AccountVO
import nl.ing.api.contacting.repository.cslick.actions.AccountAction
import nl.ing.api.contacting.repository.cslick.actions.core.audit.AuditEntityTable
import nl.ing.api.contacting.tracing.Trace
import org.mockito.Mockito
import org.scalatest.BeforeAndAfterAll
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.flatspec.AnyFlatSpecLike
import org.scalatest.matchers.should.Matchers
import org.scalatest.time.{Millis, Seconds, Span}
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
/**
  * @author Ayush Mittal
  */
trait SlickBaseSpec extends AnyFlatSpecLike with Matchers with BeforeAndAfterAll with ScalaFutures with AuditEntityTable {

  implicit val defaultPatience: PatienceConfig =
    PatienceConfig(timeout = Span(5, Seconds), interval = Span(500, Millis))

  private val config = ConfigFactory.load("application-unit.conf")
  implicit val trace: Trace[IO] = Mockito.mock(classOf[Trace[IO]])

  def createAccountDto(id: Long): AccountDto = AccountDto(
    id = id,
    sid = "123",
    friendlyName = "account-unit",
    workspaceSid = "test workspace sid",
    timezone = "Europe/Amsterdam",
    workspaceId = 1,
    credentialsPerRegion = Map.empty,
    productServicesPerRegion = Map.empty
  )

  def createAccountVO(id: Long) = {
    val dto = createAccountDto(id)
    AccountVO(
    Option(dto.id) ,Option(dto.sid),dto.friendlyName,true,1,1,"Europe/Amsterdam","NL", "1079")
  }

  def createContext(accountId: Long) =
    ContactingContext(accountId, SlickAuditContext("modifiedBy", None, Some(accountId), None))

  val databaseConfig = DatabaseConfig.forConfig[JdbcProfile]("h2mem", config)

  implicit val h2DBComponent: CoCoDBComponent = new CoCoDBComponent {
    override val driver: JdbcProfile = databaseConfig.profile
    import driver.api._
    override val db: Database = databaseConfig.db
  }

  def createAccount(accountAction: AccountAction, accountId: Long = 1L) = {
    h2DBComponent.db.run(accountAction.save(createAccountVO(accountId)))
  }

  override val jdbcProfile = h2DBComponent.driver

  override def beforeAll(): Unit = {
    whenReady(databaseConfig.db.run(createAuditSchema))(_ => ())
  }

  override def afterAll(): Unit = {
    whenReady(databaseConfig.db.run(deleteAuditSchema))(_ => ())
  }

  implicit val testCacheConfigProvider = new CacheConfigProvider[Future] {

    var readEnabled: Boolean = false

    var writeEnabled: Boolean = true

    override def isReadEnabled: Future[Boolean] = Future.successful(readEnabled)

    override def isWriteEnabled: Future[Boolean] = Future.successful(writeEnabled)
  }
}
