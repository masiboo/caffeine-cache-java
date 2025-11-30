package nl.ing.api.contacting.conf.resource
/*
import cats.data.Kleisli
import cats.effect.IO
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper
import com.ing.api.contacting.dto.audit.AuditContext
import com.ing.api.contacting.dto.resource.organisation.FlatOrganisationUnitDto
import com.ing.api.contacting.dto.resource.permissions.{BusinessFunctionDto, PermissionsDto}
import jakarta.ws.rs.client.Entity
import jakarta.ws.rs.core.MediaType
import nl.ing.api.contacting.conf.business.permissions.{PermissionAlgebra, PermissionReaderService, PermissionService}
import nl.ing.api.contacting.conf.domain._
import nl.ing.api.contacting.conf.domain.enums._
import nl.ing.api.contacting.conf.mapper.permissions.PermissionsMapper.ORG_ID_FOR_ACCOUNT
import nl.ing.api.contacting.conf.modules.CoreModule
import nl.ing.api.contacting.conf.resource.dto.{BusinessFunctionAccess, BusinessFunctionsDto, BusinessFunctionsDtoWrapper}
import nl.ing.api.contacting.conf.support.{AccountSupport, SessionContextSupport}
import nl.ing.api.contacting.test.jersey.JerseySupport.JsonSupport
import nl.ing.api.contacting.tracing.Span
import nl.ing.api.contacting.tracing.Trace.IOKleisli
import nl.ing.api.contacting.trust.rest.context.{AuthorizationContext, EmployeeContext, SessionContext, UserContext}
import nl.ing.api.contacting.trust.rest.feature.permissions._
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito._

import scala.concurrent.Future

class PermissionResourceSpec extends SessionContextSupport[PermissionsResource, BusinessFunctionDto] with AccountSupport {
  val path = "/contacting-conf/permissions"
  implicit val jsonObjectMapper = new ObjectMapper().registerModule(DefaultScalaModule)

  before {
    setContext(
      SessionContext(
        EmployeeContext(sessionId = "123456",

                        employeeId = "123",
                        accountToken = Some("123"),
                        subAccount = Some(account))))
  }

  /*override def init(): Unit = {
    setDependency(classOf[PermissionsModule], systemModule)
  }*/

  val businessFunctionDtos =
    List(
      BusinessFunctionsDto("listen-recordings", Seq(BusinessFunctionAccess(AGENT.role, TEAM.level))),
      BusinessFunctionsDto("chat", Seq(BusinessFunctionAccess(AGENT.role, SELF.level))),
      BusinessFunctionsDto("user-management", Seq(BusinessFunctionAccess(ADMIN.role, CIRCLE.level)))
    )

  val businessFunctionVOs =
    List(
      BusinessFunctionVO("account-unit", "listen-recordings", AGENT.role, TEAM, ORG_ID_FOR_ACCOUNT),
      BusinessFunctionVO("account-unit", "chat", AGENT.role, SELF, ORG_ID_FOR_ACCOUNT),
      BusinessFunctionVO("account-unit", "user-management", ADMIN.role, CIRCLE, ORG_ID_FOR_ACCOUNT)
    )

  val BusinessFunctionsDtos =
    List(
      BusinessFunctionsDto("listen-recordings", Seq(BusinessFunctionAccess(AGENT.role, TEAM.level))),
      BusinessFunctionsDto("chat", Seq(BusinessFunctionAccess(AGENT.role, SELF.level))),
      BusinessFunctionsDto("user-management", Seq(BusinessFunctionAccess(ADMIN.role, CIRCLE.level)))
    )

  "Permissions for employee " should "be returned for v2" in {
    val permissionReaderService = mock[PermissionReaderService[IOKleisli]]
    when(get[CoreModule].permissionReader).thenReturn(permissionReaderService)

    val sessionContextCaptor: ArgumentCaptor[EmployeeContext] = ArgumentCaptor.forClass(classOf[EmployeeContext])
    val permissions = EmployeeBusinessFunctionVO(
      Some(
        PermissionOrganisationVO(1,
                                 "clt",
                                 2,
                                 "circle",
                                 3,
                                 "supercircle",
                                 Set(OrganisationalRestriction(1, "clt", 2, "circle", 3, "supercircle", true)))),
      businessFunctionVOs
    )

    when(permissionReaderService.fetchPermissions(sessionContextCaptor.capture(), same(account.friendlyName)))
      .thenReturn(Kleisli.liftF[IO, Span[IO], PermissionBusinessFunctionVO](IO.pure(permissions)))

    val response = target(s"$path/me").request().accept("application/vnd.ing.contacting.permissions-v2+json").get()
    val entity = response.asDto(classOf[PermissionsDto])
    response.getStatus shouldBe 200

    entity.organisation.get.isInstanceOf[FlatOrganisationUnitDto] shouldBe true
    entity.organisation.get.cltName shouldBe "clt"
    entity.organisation.get.circleName shouldBe "circle"
    entity.organisation.get.superCircleName shouldBe "supercircle"
    entity.organisationalRestrictions.mkString(",") shouldBe "FlatOrganisationUnitDto(1,clt,2,circle,3,supercircle)"
    entity.businessFunctions should contain(BusinessFunctionDto("listen-recordings", "AGENT", TEAM.level))
    entity.businessFunctions should contain(BusinessFunctionDto("chat", "AGENT", SELF.level))
    entity.businessFunctions should contain(BusinessFunctionDto("user-management", "ADMIN", CIRCLE.level))
    sessionContextCaptor.getValue.sessionId shouldBe "123456"
  }

  "Permissions for requested employee " should "be returned for v2" in {
    val permissionAlgebraF = mock[PermissionAlgebra[IOKleisli]]
    when(get[CoreModule].permissionAlgebraF).thenReturn(permissionAlgebraF)

    val employeeId: ArgumentCaptor[String] = ArgumentCaptor.forClass(classOf[String])
    val accountCaptor: ArgumentCaptor[String] = ArgumentCaptor.forClass(classOf[String])
    val permissions = EmployeeBusinessFunctionVO(
      Some(
        PermissionOrganisationVO(1,
          "clt",
          2,
          "circle",
          3,
          "supercircle",
          Set(OrganisationalRestriction(1, "clt", 2, "circle", 3, "supercircle", true)))),
      businessFunctionVOs
    )

    when(permissionAlgebraF.getPermissionsForEmployee(employeeId.capture(), accountCaptor.capture()))
      .thenReturn(Kleisli.liftF[IO, Span[IO], PermissionBusinessFunctionVO](IO.pure(permissions)))

    val response = target(s"$path/NL-unit/BO55NK").request().get()
    val entity = response.asDto(classOf[PermissionsDto])
    response.getStatus shouldBe 200

    entity.organisation.get.isInstanceOf[FlatOrganisationUnitDto] shouldBe true
    entity.organisation.get.cltName shouldBe "clt"
    entity.organisation.get.circleName shouldBe "circle"
    entity.organisation.get.superCircleName shouldBe "supercircle"
    entity.organisationalRestrictions.mkString(",") shouldBe "FlatOrganisationUnitDto(1,clt,2,circle,3,supercircle)"
    entity.businessFunctions should contain(BusinessFunctionDto("listen-recordings", "AGENT", TEAM.level))
    entity.businessFunctions should contain(BusinessFunctionDto("chat", "AGENT", SELF.level))
    entity.businessFunctions should contain(BusinessFunctionDto("user-management", "ADMIN", CIRCLE.level))
    employeeId.getValue shouldBe "BO55NK"
    accountCaptor.getValue shouldBe "NL-unit"
  }

  "Permissions for customer " should "be returned" in {
    val permissionReaderService = mock[PermissionReaderService[IOKleisli]]
    when(get[CoreModule].permissionReader).thenReturn(permissionReaderService)

    val sessionContextCaptor: ArgumentCaptor[AuthorizationContext] = ArgumentCaptor.forClass(classOf[AuthorizationContext])
    val permissions = NonEmployeeBusinessFunctionVO(businessFunctionVOs)
    when(permissionReaderService.fetchPermissions(sessionContextCaptor.capture(), same(account.friendlyName)))
      .thenReturn(Kleisli.liftF[IO, Span[IO], PermissionBusinessFunctionVO](IO.pure(permissions)))

    val response = target(s"$path/me").request().accept("application/vnd.ing.contacting.permissions-v2+json").get()
    val entity = response.asDto(classOf[PermissionsDto])
    response.getStatus shouldBe 200

    entity.organisation shouldBe None
    entity.businessFunctions should contain(BusinessFunctionDto("listen-recordings", "AGENT", TEAM.level))
    entity.businessFunctions should contain(BusinessFunctionDto("chat", "AGENT", SELF.level))
    entity.businessFunctions should contain(BusinessFunctionDto("user-management", "ADMIN", CIRCLE.level))
    sessionContextCaptor.getValue.asInstanceOf[UserContext].sessionId shouldBe "123456"
  }

  "Permissions" should "be synced" in {
    val permissionService = mock[PermissionService]

    when(get[CoreModule].permissionService).thenReturn(permissionService)

    val bfCaptor: ArgumentCaptor[List[BusinessFunctionVO]] = ArgumentCaptor.forClass(classOf[List[BusinessFunctionVO]])
    when(permissionService.syncBusinessFunctions(bfCaptor.capture(), same(account.friendlyName))(any()))
      .thenReturn(Future.successful(()))

    val response = target(s"$path").request().put(Entity.entity(BusinessFunctionsDtos, MediaType.APPLICATION_JSON))
    response.getStatus shouldBe 204
    bfCaptor.getValue shouldBe businessFunctionVOs
  }

  "invalid Permissions " should " not be be synced and throw 500" in {
    val permissionService = mock[PermissionService]
    val badBf = BusinessFunctionsDto("invalid", Seq(BusinessFunctionAccess("bad role", SUPER_CIRCLE.level)))
    val badBfVO = BusinessFunctionVO("account-unit", "invalid", "bad role", SUPER_CIRCLE, ORG_ID_FOR_ACCOUNT)
    when(get[CoreModule].permissionService).thenReturn(permissionService)

    val bfCaptor: ArgumentCaptor[Seq[BusinessFunctionVO]] = ArgumentCaptor.forClass(classOf[Seq[BusinessFunctionVO]])
    when(permissionService.syncBusinessFunctions(bfCaptor.capture(), any[String]())(any[AuditContext]()))
      .thenThrow(new IllegalArgumentException("invalid business function name: 'listen-recordings--asd!'"))

    val response =
      target(s"$path").request().put(Entity.entity(BusinessFunctionsDtos ++ List(badBf), MediaType.APPLICATION_JSON))
    response.getStatus shouldBe 500
    bfCaptor.getValue shouldBe businessFunctionVOs ++ List(badBfVO)
  }

  "All editable business functions" should " be retrieved" in {
    val permissionService = mock[PermissionService]

    when(get[CoreModule].permissionService).thenReturn(permissionService)
    when(permissionService.getEditableBusinessFunctions(same(account.friendlyName)))
      .thenReturn(Future.successful(businessFunctionVOs))

    val response = target(s"$path").request().get()
    response.getStatus shouldBe 200
    val dtos = JsonUtil.fromJson[BusinessFunctionsDtoWrapper](response.readEntity(classOf[String]))
    dtos.data.size shouldBe 3
  }

  object JsonUtil {
    val mapper = new ObjectMapper() with ScalaObjectMapper
    mapper.registerModule(DefaultScalaModule)

    def fromJson[T](json: String)(implicit m: Manifest[T]): T = {
      mapper.readValue[T](json)
    }
  }

  override def beforeEach(): Unit = {
    super.beforeEach()
  }

  override def afterEach(): Unit = {
    super.afterEach()
  }
}
*/