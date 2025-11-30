package nl.ing.api.contacting.conf.business.client

import cats.data.Kleisli
import cats.effect.IO
import nl.ing.api.contacting.conf.BaseSpec
import nl.ing.api.contacting.conf.domain.{BusinessFunctionVO, EmployeeBusinessFunctionVO, NonEmployeeBusinessFunctionVO, OrganisationalRestriction, PermissionBusinessFunctionVO, PermissionOrganisationVO}
import nl.ing.api.contacting.conf.domain.enums._
import nl.ing.api.contacting.conf.mapper.permissions.PermissionsMapper
import nl.ing.api.contacting.conf.mapper.permissions.PermissionsMapper.ORG_ID_FOR_ACCOUNT
import nl.ing.api.contacting.conf.support.AccountSupport
import nl.ing.api.contacting.tracing.Span
import nl.ing.api.contacting.trust.rest.context.{AuthorizationContext, SessionContext}
import nl.ing.api.contacting.trust.rest.feature.permissions._
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.Millis
import org.scalatest.time.Seconds
import org.scalatest.time.{Span => SpanTest}

import java.util.Optional

/**
 * Created by D.Chandra on 9/4/17.
 */
class ContactingPermissionsProviderSpec extends BaseSpec with ScalaFutures with AccountSupport {

  val sessionContext = mock[SessionContext]
  val authContext = mock[AuthorizationContext]
  val testObject = new ContactingPermissionsProvider(coreModule)
  implicit val defaultPatience: PatienceConfig =
    PatienceConfig(timeout = SpanTest(5, Seconds), interval = SpanTest(500, Millis))

  val businessFunctionVOs =
    List(
      BusinessFunctionVO("nl-dev", "listen-recordings", AGENT.role, TEAM, ORG_ID_FOR_ACCOUNT),
      BusinessFunctionVO("nl-dev", "chat", AGENT.role, SELF, ORG_ID_FOR_ACCOUNT),
      BusinessFunctionVO("nl-dev", "user-management", ADMIN.role, CIRCLE, ORG_ID_FOR_ACCOUNT)
      )

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

  override def init(): Unit = {
    when(sessionContext.trustContext).thenReturn(authContext)
    when(authContext.subAccount).thenReturn(Option(account))
  }

  it should "get permissions from contacting api locally" in {
    val accountCapture: ArgumentCaptor[String] = ArgumentCaptor.forClass(classOf[String])
    when(coreModule.permissionReader.fetchPermissions(same(authContext), accountCapture.capture()))
      .thenReturn(Kleisli.liftF[IO, Span[IO], PermissionBusinessFunctionVO](IO.pure(permissions)))

    whenReady(testObject.fetchPermissions(sessionContext)) {
      result =>
        result shouldEqual Some(PermissionsMapper.toDto(permissions))
        accountCapture.getValue shouldEqual "account-unit"
    }
  }

  it should "get no permissions from contacting api locally" in {
    val accountCapture: ArgumentCaptor[String] = ArgumentCaptor.forClass(classOf[String])
    when(coreModule.permissionReader.fetchPermissions(same(authContext), accountCapture.capture())).thenReturn(
      Kleisli.liftF[IO, Span[IO], PermissionBusinessFunctionVO](IO.pure(NonEmployeeBusinessFunctionVO(Seq()))))

    whenReady(testObject.fetchPermissions(sessionContext)) {
      result =>
        result.isDefined shouldBe true
        result.get.businessFunctions shouldBe Seq()
        accountCapture.getValue shouldEqual "account-unit"
    }
  }
}
