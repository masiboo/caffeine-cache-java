package nl.ing.api.contacting.conf.permissions

import cats.effect.IO
import nl.ing.api.contacting.conf.business.permissions.{BusinessFunctionDataSource, PermissionAlgebra}
import nl.ing.api.contacting.conf.domain.enums._
import nl.ing.api.contacting.conf.domain.{BusinessFunctionVO, EmployeeAccountsVO, OrganisationalRestriction}
import nl.ing.api.contacting.conf.mapper.permissions.PermissionsMapper.ORG_ID_FOR_ACCOUNT
import nl.ing.api.contacting.conf.support.PermissionSupport
import nl.ing.api.contacting.conf.tracing.TestTrace.TestTrace
import nl.ing.api.contacting.trust.rest.context.{ContactingApiContext, CustomerContext, EmployeeContext, UnauthenticatedContext}
import nl.ing.api.contacting.trust.rest.feature.permissions.{ACCOUNT, CIRCLE, SELF, SUPER_CIRCLE}
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import nl.ing.api.contacting.conf.modules.ExecutionContextConfig.ioRunTime
import scala.collection.mutable.ListBuffer

/**
 * @author Ayush Mittal
 */
class PermissionAlgebraSpec extends PermissionSupport with BeforeAndAfterEach {

  var traceList = ListBuffer.empty[String]

  def addToList(str: String): ListBuffer[String] = {
    traceList += str
  }

  implicit val testTrace = new TestTrace(addToList)

  override def beforeEach(): Unit = {
    traceList = ListBuffer.empty
  }

  var employeeAccountVo: Option[EmployeeAccountsVO] = Some(userWithAdmin)

  val testBusinessFunctionDataSource = new BusinessFunctionDataSource[IO] {
    override def findByAccount(accountFriendlyName: String): IO[Seq[BusinessFunctionVO]] = IO.pure(permissions)

    override def findByEmployeeId(employeeId: String, accountFriendlyName: String): IO[Option[EmployeeAccountsVO]] =
      IO.pure(employeeAccountVo)
  }

  private val employee = mock[EmployeeContext]
  private val customer = mock[CustomerContext]
  private val api = mock[ContactingApiContext]
  private val unAuthenticated = mock[UnauthenticatedContext]

  val testObject = new PermissionAlgebra[IO](testBusinessFunctionDataSource)

  it should "return permissions when employee requests it" in {
    when(employee.getEmployeeId).thenReturn(userWithAdmin.employeeId)

    val result =
      testObject.getPermissions(employee, "NL-dev").unsafeRunSync()

    result.organisation.get.cltName shouldBe "team-1"
    result.organisation.get.circleName shouldBe "dep-1"
    result.organisation.get.superCircleName shouldBe "bu-1"
    result.organisation.get.organisationalRestriction shouldBe Set(
      OrganisationalRestriction(1, "team-1", 2, "dep-1", 3, "bu-1", true))

    result.businessFunctions should contain(
      BusinessFunctionVO(account.friendlyName, "user-management", ADMIN.role, CIRCLE, ORG_ID_FOR_ACCOUNT))
    result.businessFunctions should contain(
      BusinessFunctionVO(account.friendlyName, "listen-recordings", ADMIN.role, SUPER_CIRCLE, ORG_ID_FOR_ACCOUNT))
    result.businessFunctions should not contain BusinessFunctionVO(account.friendlyName,
                                                                   "system-tooling",
                                                                   CONTACTING.role,
                                                                   ACCOUNT,
                                                                   ORG_ID_FOR_ACCOUNT)

  }

  it should "return permissions when ops admin requests for another employee" in {
    val result =
      testObject.getPermissionsForEmployee(userWithAdmin.employeeId, account.friendlyName).unsafeRunSync()
    result.organisation.get.cltName shouldBe "team-1"
    result.organisation.get.circleName shouldBe "dep-1"
    result.organisation.get.superCircleName shouldBe "bu-1"
    result.organisation.get.organisationalRestriction shouldBe Set(
      OrganisationalRestriction(1, "team-1", 2, "dep-1", 3, "bu-1", true))

    result.businessFunctions should contain(
      BusinessFunctionVO(account.friendlyName, "user-management", ADMIN.role, CIRCLE, ORG_ID_FOR_ACCOUNT))
    result.businessFunctions should contain(
      BusinessFunctionVO(account.friendlyName, "listen-recordings", ADMIN.role, SUPER_CIRCLE, ORG_ID_FOR_ACCOUNT))
    result.businessFunctions should not contain BusinessFunctionVO(account.friendlyName,
      "system-tooling",
      CONTACTING.role,
      ACCOUNT,
      ORG_ID_FOR_ACCOUNT)

  }

  it should "return permissions when employee without team requests it " in {
    when(employee.getEmployeeId).thenReturn(userWithAdmin.employeeId)

    employeeAccountVo = employeeAccountVo.map(_.copy(organisationalRestrictions = None))
    val result = testObject.getPermissions(employee, "NL-dev").unsafeRunSync()
    result.organisation shouldBe None
    result.businessFunctions should contain(
      BusinessFunctionVO(account.friendlyName, "user-management", ADMIN.role, CIRCLE, ORG_ID_FOR_ACCOUNT))
    result.businessFunctions should contain(
      BusinessFunctionVO(account.friendlyName, "listen-recordings", ADMIN.role, SUPER_CIRCLE, ORG_ID_FOR_ACCOUNT))
    result.businessFunctions should not contain BusinessFunctionVO(account.friendlyName,
                                                                   "system-tooling",
                                                                   CONTACTING.role,
                                                                   ACCOUNT,
                                                                   ORG_ID_FOR_ACCOUNT)
  }

  it should "return permissions when employee with no active team requests it " in {
    when(employee.getEmployeeId).thenReturn(userWithAdmin.employeeId)

    employeeAccountVo = employeeAccountVo.map(
      _.copy(
        organisationalRestrictions = Some(Set(OrganisationalRestriction(1, "team-1", 2, "dep-1", 3, "bu-1", false)))))

    val result = testObject.getPermissions(employee, "NL-dev").unsafeRunSync()
    result.organisation shouldBe None
    result.businessFunctions should contain(
      BusinessFunctionVO(account.friendlyName, "user-management", ADMIN.role, CIRCLE, ORG_ID_FOR_ACCOUNT))
    result.businessFunctions should contain(
      BusinessFunctionVO(account.friendlyName, "listen-recordings", ADMIN.role, SUPER_CIRCLE, ORG_ID_FOR_ACCOUNT))
    result.businessFunctions should not contain BusinessFunctionVO(account.friendlyName,
                                                                   "system-tooling",
                                                                   CONTACTING.role,
                                                                   ACCOUNT,
                                                                   ORG_ID_FOR_ACCOUNT)
  }

  it should "return max permissions when employee has multiple roles" in {
    when(employee.getEmployeeId).thenReturn(userWithSuperVisorAndAdmin.employeeId)

    employeeAccountVo = Some(userWithSuperVisorAndAdmin)

    val result = testObject.getPermissions(employee, "NL-dev").unsafeRunSync()
    result.businessFunctions should contain(
      BusinessFunctionVO(account.friendlyName, "user-management", ACCOUNT_ADMIN.role, ACCOUNT, ORG_ID_FOR_ACCOUNT))
    result.businessFunctions should contain(
      BusinessFunctionVO(account.friendlyName, "listen-recordings", SUPERVISOR.role, CIRCLE, ORG_ID_FOR_ACCOUNT))

  }

  it should "return zero permissions when employee has NO roles" in {
    when(employee.getEmployeeId).thenReturn(userWithSuperVisorAndAdmin.employeeId)

    employeeAccountVo = None
    val result = testObject.getPermissions(employee, "NL-dev").unsafeRunSync()
    result.businessFunctions.size shouldBe 0
    result.organisation shouldBe None
  }

  it should "return permissions when customer requests it" in {
    when(customer.getCustomerId).thenReturn("9797")

    val result = testObject.getPermissions(customer, "NL-dev").unsafeRunSync()
    result.organisation shouldBe None
    result.businessFunctions should contain(
      BusinessFunctionVO(account.friendlyName, "chat", CUSTOMER_AUTHENTICATED.role, SELF, ORG_ID_FOR_ACCOUNT))
  }

  it should "return permissions when contacting requests it" in {
    when(api.apiName).thenReturn("CONTACTING")

    val result = testObject.getPermissions(api, "NL-dev").unsafeRunSync()
    result.organisation shouldBe None
    result.businessFunctions should contain(
      BusinessFunctionVO(account.friendlyName, "statistics", CONTACTING.role, ACCOUNT, ORG_ID_FOR_ACCOUNT))
    result.businessFunctions should contain(
      BusinessFunctionVO("NL-dev",
                         ContactingBusinessFunctionsScala.SYSTEM_TOOLING,
                         CONTACTING.role,
                         ACCOUNT,
                         ORG_ID_FOR_ACCOUNT))
  }

  it should "return permissions when unauthenticated requests it" in {
    val result = testObject.getPermissions(unAuthenticated, "NL-dev").unsafeRunSync()
    result.organisation shouldBe None
    result.businessFunctions should contain(
      BusinessFunctionVO(account.friendlyName, "chat", CUSTOMER_UNAUTHENTICATED.role, SELF, ORG_ID_FOR_ACCOUNT))
  }
}
