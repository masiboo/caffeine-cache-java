package nl.ing.api.contacting.conf.permissions

import cats.data.EitherT
import com.ing.api.contacting.dto.resource.account.AccountDto
import com.ing.apisdk.toolkit.logging.audit.slf4j.Slf4jAuditLogger
import nl.ing.api.contacting.conf.BaseSpec
import nl.ing.api.contacting.conf.business.ConfigService
import nl.ing.api.contacting.conf.business.permissions.PermissionService
import nl.ing.api.contacting.conf.domain.{BusinessFunctionVO, ContactingConfigVO, EmployeeAccountsVO, OrganisationalRestriction}
import nl.ing.api.contacting.conf.domain.enums._
import nl.ing.api.contacting.conf.mapper.permissions.PermissionsMapper.ORG_ID_FOR_ACCOUNT
import nl.ing.api.contacting.conf.repository.cassandra.permissions.BusinessFunctionsRepository
import nl.ing.api.contacting.trust.rest.context.{ContactingApiContext, CustomerContext, EmployeeContext, UnauthenticatedContext}
import nl.ing.api.contacting.trust.rest.feature.permissions._
import nl.ing.api.contacting.util.exception.ContactingBusinessError
import org.mockito.Mockito._
import org.mockito.{ArgumentCaptor, Mockito}
import cats.implicits._
import java.util.Optional
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by M07G789 on 17-2-2017.
  *
  *
  */
class PermissionServiceSpec extends BaseSpec {
  val account: AccountDto
  = AccountDto(
    id = 1L,
    sid = "sid",
    friendlyName = "nl-junit",
    workspaceSid = "123",
    timezone = "Europe/Amsterdam",
    workspaceId = 1,
    legalEntity = "legalEntity",
    credentialsPerRegion = Map.empty,
    productServicesPerRegion = Map.empty
  )

  val configService: ConfigService =  Mockito.mock(classOf[ConfigService])
  val businessFunctionsRepository: BusinessFunctionsRepository[Future] = Mockito.mock(classOf[BusinessFunctionsRepository[Future]])
  val auditLogger: Slf4jAuditLogger = Mockito.mock(classOf[Slf4jAuditLogger])
  val testObject = new PermissionService(configService, businessFunctionsRepository, auditLogger)
  
  private val BUSINESS_FUNC: String = "get_recordings"

  val pms = Seq(
    BusinessFunctionVO("1", BUSINESS_FUNC, AGENT.role, SELF, ORG_ID_FOR_ACCOUNT),
    BusinessFunctionVO("1", BUSINESS_FUNC, ADMIN.role, TEAM, ORG_ID_FOR_ACCOUNT),
    BusinessFunctionVO("1", BUSINESS_FUNC, SUPERVISOR.role, CIRCLE, ORG_ID_FOR_ACCOUNT),
    BusinessFunctionVO("1", BUSINESS_FUNC, ACCOUNT_ADMIN.role, ACCOUNT, ORG_ID_FOR_ACCOUNT),
    BusinessFunctionVO("1", BUSINESS_FUNC, MONITORING.role, SUPER_CIRCLE, ORG_ID_FOR_ACCOUNT)
  )

  private val employeeOption = mock[Optional[EmployeeContext]]
  private val employee = mock[EmployeeContext]

  private val customerOption = mock[Optional[CustomerContext]]
  private val customer = mock[CustomerContext]

  private val apiOption = mock[Optional[ContactingApiContext]]
  private val api = mock[ContactingApiContext]
  private val unAuthenticated = mock[UnauthenticatedContext]

  val userWithAdmin = EmployeeAccountsVO(
    "ADMIN1",
    "NL",
    preferredAccount = true,
    Set(ADMIN.role),
    Some("bu-1"),
    Some("dep-1"),
    Some("team-1"),
    Some(Seq(OrganisationalRestriction(1, "team-1", 2, "dep-1", 3, "bu-1", true))),
    Map.empty[String, Int],
    None
  )

  val userWithSuperVisorAndAdmin = EmployeeAccountsVO(
    "ACCOUNT_ADMIN1",
    "NL",
    preferredAccount = true,
    Set(SUPERVISOR.role, ACCOUNT_ADMIN.role),
    Some("bu-1"),
    Some("dep-1"),
    Some("team-1"),
    Some(Seq(OrganisationalRestriction(1, "team-1", 2, "dep-1", 3, "bu-1", true))),
    Map.empty[String, Int],
    None
  )

  val readOnlyBusinessFunctions =
    Set("system tooling", "permissions administration")

  val permissions = List(
    BusinessFunctionVO(account.friendlyName, "listen-recordings", AGENT.role, TEAM, -1),
    BusinessFunctionVO(account.friendlyName, "listen-recordings", SUPERVISOR.role, CIRCLE, -1),
    BusinessFunctionVO(account.friendlyName, "listen-recordings", ADMIN.role, SUPER_CIRCLE, -1),
    BusinessFunctionVO(account.friendlyName, "chat", AGENT.role, SELF, -1),
    BusinessFunctionVO(account.friendlyName, "chat", CUSTOMER_AUTHENTICATED.role, SELF, -1),
    BusinessFunctionVO(account.friendlyName, "chat", CUSTOMER_UNAUTHENTICATED.role, SELF, -1),
    BusinessFunctionVO(account.friendlyName, "user-management", SUPERVISOR.role, TEAM, -1),
    BusinessFunctionVO(account.friendlyName, "user-management", ADMIN.role, CIRCLE, -1),
    BusinessFunctionVO(account.friendlyName, "user-management", ACCOUNT_ADMIN.role, ACCOUNT, -1),
    BusinessFunctionVO(account.friendlyName, "inbound-calls", AGENT.role, SELF, -1),
    BusinessFunctionVO(account.friendlyName, "statistics", CONTACTING.role, ACCOUNT, -1),
    BusinessFunctionVO(account.friendlyName, "statistics", AGENT.role, SELF, -1)
  )

  val configs = Seq(
    ContactingConfigVO("BUSINESS_FUNCTIONS", "listen-recordings,chat,user-management,inbound-calls,statistics"),
    ContactingConfigVO("BUSINESS_FUNCTIONS_AT_TEAM_LEVEL", "user-management"),
    ContactingConfigVO("ROLES",
      "AGENT,SUPERVISOR,CUSTOMER_AUTHENTICATED,CUSTOMER_UNAUTHENTICATED,ADMIN,ACCOUNT_ADMIN,CONTACTING")
  )

  override def init() = {
    Mockito.reset(configService,
      businessFunctionsRepository,
      auditLogger,
      employeeOption,
      apiOption,
      customerOption)
    when(configService.findByKey(PermissionService.READONLY_BUSINESS_FUNCTIONS))
      .thenReturn(Future.successful(readOnlyBusinessFunctions))
    when(configService.fetchConfigs()).thenReturn(Future.successful(configs))

    ()
  }

  it should "give all editable business functions" in {
    val allPermissions = permissions ++ Seq(
      BusinessFunctionVO(account.friendlyName, "system tooling", CONTACTING.role, ACCOUNT, -1),
      BusinessFunctionVO(account.friendlyName, "permissions administration", AGENT.role, SELF, -1)
    )
    when(businessFunctionsRepository.findByAccount(account.friendlyName))
      .thenReturn(Future.successful(allPermissions))

    whenReady(testObject.getEditableBusinessFunctions(account.friendlyName)) {
      result =>
        result shouldBe permissions.sortBy(_.businessFunction)
    }
  }

  it should "delete permissions which are not in this update" in {
    val bfToBeDeleted = BusinessFunctionVO(account.friendlyName, "listen-recordings", AGENT.role, TEAM, -1)
    val bfCap: ArgumentCaptor[List[BusinessFunctionVO]] =
      ArgumentCaptor.forClass(classOf[List[BusinessFunctionVO]])

    val acNameCaptor: ArgumentCaptor[String] = ArgumentCaptor.forClass(classOf[String])

    when(businessFunctionsRepository.findByAccount("nl-junit")).thenReturn(Future.successful(permissions))
    when(businessFunctionsRepository.deletePermissions(acNameCaptor.capture(), bfCap.capture())).thenReturn(EitherT.right[ContactingBusinessError](Future.successful(())))
    when(businessFunctionsRepository.upsertPermissions(account.friendlyName, List())).thenReturn(EitherT.right[ContactingBusinessError](Future.successful(())))

    whenReady(testObject.syncBusinessFunctions(permissions.tail, account.friendlyName)(null)) {
      _ =>
        bfCap.getValue shouldBe List(bfToBeDeleted)
        acNameCaptor.getValue shouldBe account.friendlyName
    }
  }

  it should "upsert new permissions" in {
    val bfToBeAdded = BusinessFunctionVO(account.friendlyName, "statistics", ADMIN.role, SUPER_CIRCLE, -1)
    val acNameCaptor: ArgumentCaptor[String] = ArgumentCaptor.forClass(classOf[String])
    val deleteCaptor: ArgumentCaptor[List[BusinessFunctionVO]] =
      ArgumentCaptor.forClass(classOf[List[BusinessFunctionVO]])
    val addedCaptor: ArgumentCaptor[List[BusinessFunctionVO]] =
      ArgumentCaptor.forClass(classOf[List[BusinessFunctionVO]])
    when(configService.findByKey(PermissionService.READONLY_BUSINESS_FUNCTIONS))
      .thenReturn(Future.successful(readOnlyBusinessFunctions))
    when(businessFunctionsRepository.findByAccount("nl-junit")).thenReturn(Future.successful(permissions))
    when(businessFunctionsRepository.deletePermissions(acNameCaptor.capture(), deleteCaptor.capture()))
      .thenReturn(EitherT.right[ContactingBusinessError](Future.successful(())))
    when(businessFunctionsRepository.upsertPermissions(acNameCaptor.capture(),addedCaptor.capture()))
      .thenReturn(EitherT.right[ContactingBusinessError](Future.successful(())))

    whenReady(testObject.syncBusinessFunctions(permissions ++ List(bfToBeAdded), account.friendlyName)(null)) {
      result =>
        addedCaptor.getValue shouldBe List(bfToBeAdded)
        deleteCaptor.getValue shouldBe List()
    }
  }

  it should "merge permissions" in {
    val acNameCaptor: ArgumentCaptor[String] = ArgumentCaptor.forClass(classOf[String])
    val newPermissions = List(
      BusinessFunctionVO(account.friendlyName, "listen-recordings", SUPERVISOR.role, CIRCLE, -1),
      BusinessFunctionVO(account.friendlyName, "listen-recordings", ADMIN.role, SUPER_CIRCLE, -1),
      BusinessFunctionVO(account.friendlyName, "chat", ADMIN.role, SELF, -1),
      BusinessFunctionVO(account.friendlyName, "chat", CUSTOMER_AUTHENTICATED.role, SELF, -1),
      BusinessFunctionVO(account.friendlyName, "chat", CUSTOMER_UNAUTHENTICATED.role, SELF, -1),
      BusinessFunctionVO(account.friendlyName, "user-management", SUPERVISOR.role, TEAM, -1),
      BusinessFunctionVO(account.friendlyName, "user-management", ADMIN.role, CIRCLE, -1),
      BusinessFunctionVO(account.friendlyName, "user-management", ACCOUNT_ADMIN.role, ACCOUNT, -1),
      BusinessFunctionVO(account.friendlyName, "inbound-calls", AGENT.role, ACCOUNT, -1),
      BusinessFunctionVO(account.friendlyName, "statistics", CONTACTING.role, ACCOUNT, -1),
      BusinessFunctionVO(account.friendlyName, "statistics", AGENT.role, SELF, -1)
    )
    val bfToBeDeleted = Seq(
      BusinessFunctionVO(account.friendlyName, "listen-recordings", AGENT.role, TEAM, -1),
      BusinessFunctionVO(account.friendlyName, "chat", AGENT.role, SELF, -1),
      BusinessFunctionVO(account.friendlyName, "inbound-calls", AGENT.role, SELF, -1)
    )
    val toBeAdded = Seq(
      BusinessFunctionVO(account.friendlyName, "chat", ADMIN.role, SELF, -1),
      BusinessFunctionVO(account.friendlyName, "inbound-calls", AGENT.role, ACCOUNT, -1)
    )

    val deleteCaptor: ArgumentCaptor[List[BusinessFunctionVO]] =
      ArgumentCaptor.forClass(classOf[List[BusinessFunctionVO]])
    val addedCaptor: ArgumentCaptor[List[BusinessFunctionVO]] =
      ArgumentCaptor.forClass(classOf[List[BusinessFunctionVO]])
    when(configService.findByKey(PermissionService.READONLY_BUSINESS_FUNCTIONS))
      .thenReturn(Future.successful(readOnlyBusinessFunctions))
    when(businessFunctionsRepository.deletePermissions(acNameCaptor.capture(),deleteCaptor.capture()))
      .thenReturn(EitherT.right[ContactingBusinessError](Future.successful(())))
    when(businessFunctionsRepository.upsertPermissions(acNameCaptor.capture(),addedCaptor.capture()))
      .thenReturn(EitherT.right[ContactingBusinessError](Future.successful(()))
      )
    when(businessFunctionsRepository.findByAccount("nl-junit")).thenReturn(Future.successful(permissions))
    val ordederedBusinessFunctionRepository =
      Mockito.inOrder(businessFunctionsRepository)

    //following will make sure that firstMock was called before secondMock
    whenReady(testObject.syncBusinessFunctions(newPermissions, account.friendlyName)(null)) {
      result =>
        deleteCaptor.getValue should contain(bfToBeDeleted.head)
        deleteCaptor.getValue should contain(bfToBeDeleted(1))
        deleteCaptor.getValue should contain(bfToBeDeleted(2))

        addedCaptor.getValue should contain(toBeAdded.head)
        addedCaptor.getValue should contain(toBeAdded(1))
        ordederedBusinessFunctionRepository
          .verify(businessFunctionsRepository)
          .deletePermissions(acNameCaptor.capture(),deleteCaptor.capture())
        ordederedBusinessFunctionRepository
          .verify(businessFunctionsRepository)
          .upsertPermissions(acNameCaptor.capture(),addedCaptor.capture())
    }

  }

  it should "validate if team level permission can be added for allowed business functions" in {
    val bfToBeAdded = BusinessFunctionVO(account.friendlyName, "statistics", ADMIN.role, SUPER_CIRCLE, 14)
    whenReady(testObject.syncBusinessFunctions(permissions ++ List(bfToBeAdded), account.friendlyName)(null).failed) {
      result =>
        result.getMessage shouldBe "requirement failed: permission at team level are only allowed for List(user-management)"
    }
  }

  it should "throw an exception when a business function not yet assigned is being altered/added" in {
    val bfTitleThatsNotYetAddedSoNoChangingOrAdding =
      BusinessFunctionVO(account.friendlyName, "godmode", ADMIN.role, SUPER_CIRCLE, -1)
    when(businessFunctionsRepository.findByAccount("nl-junit")).thenReturn(Future.successful(permissions))
    whenReady(testObject.syncBusinessFunctions(permissions ++ List(bfTitleThatsNotYetAddedSoNoChangingOrAdding), account.friendlyName)(null).failed) {
      result =>
        result.getMessage shouldBe "requirement failed: updating permissions with an invalid business function"

    }
  }

  it should "throw an exception when a non existing user role is being used" in {
    val bfWithUserRoleThatIsNotAllowed =
      BusinessFunctionVO(account.friendlyName, "statistics", "rollercoaster", SUPER_CIRCLE, -1)
    when(businessFunctionsRepository.findByAccount("nl-junit")).thenReturn(Future.successful(permissions))
    whenReady(testObject.syncBusinessFunctions(permissions ++ List(bfWithUserRoleThatIsNotAllowed), account.friendlyName)(null).failed) {
      result =>
        result.getMessage shouldBe "requirement failed: updating permissions with an invalid role: rollercoaster"
    }
  }
}
