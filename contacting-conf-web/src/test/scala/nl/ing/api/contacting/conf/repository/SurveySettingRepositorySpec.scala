package nl.ing.api.contacting.conf.repository

import cats.effect.IO
import nl.ing.api.contacting.conf.modules.ExecutionContextConfig.ioRunTime
import nl.ing.api.contacting.conf.repository.cslick.actions._
import nl.ing.api.contacting.conf.repository.model._
import nl.ing.api.contacting.conf.surveytrigger.{ContactingSurveyTriggerEvent, SelfServiceSurveyTriggerEvent}
import nl.ing.api.contacting.domain.slick.AccountVO
import nl.ing.api.contacting.repository.cslick.actions._
import nl.ing.api.contacting.repository.model._
import org.mockito.Mockito.spy
import org.scalatest.BeforeAndAfterEach
import org.scalatest.concurrent.ScalaFutures

import java.time.{LocalDateTime, ZonedDateTime}
import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationInt


class SurveySettingRepositorySpec extends SlickBaseSpec with ScalaFutures with BeforeAndAfterEach {

  val surveySettingAction = new SurveySettingAction(h2DBComponent)
  val taskQAction = new TaskQueueAction(h2DBComponent)
  val surveyTaskQAction = new SurveyTaskQAction(h2DBComponent, surveySettingAction, taskQAction)
  val surveyPhoneNumberAction = new SurveyPhNumberFormatAction(h2DBComponent, surveySettingAction)
  val orgAction: OrganisationAction = new OrganisationAction(h2DBComponent)
  val surveyOrgAction: SurveyOrgAction = new SurveyOrgAction(h2DBComponent, surveySettingAction, orgAction)
  val workerAction: WorkerAction = new WorkerAction(h2DBComponent)
  val accountAction = new AccountAction(h2DBComponent)
  val workerOrganisationAction: WorkerOrganisationAction = new WorkerOrganisationAction(h2DBComponent, orgAction, workerAction)
  val surveySettingQueries = new SurveySettingQueries(h2DBComponent, surveyTaskQAction, surveyPhoneNumberAction, surveyOrgAction, workerOrganisationAction, accountAction)
  val testObject = new FutureSurveySettingRepository(surveySettingQueries)
  val accountVo = AccountVO(Some(1), Some("123"), "account-unit", true, 1l, 1l, "Europe/Amsterdam", "NL", "1079")

  override def beforeAll(): Unit = {
    super.beforeAll()
    val db = surveySettingAction.dBComponent.db
    val allDB = for {
      _ <- testObject.createSchema
      _ <- db.run(taskQAction.createSchema)
      _ <- db.run(surveyTaskQAction.createSchema)
      _ <- db.run(surveyPhoneNumberAction.createSchema)
      _ <- db.run(orgAction.createSchema)
      _ <- db.run(surveyOrgAction.createSchema)
      _ <- db.run(workerAction.createSchema)
      _ <- db.run(workerOrganisationAction.createSchema)
      res <- db.run(accountAction.createSchema)
    } yield (res)
    Await.result(allDB, 10.seconds)
  }

  override def afterAll(): Unit = {
    super.afterAll()
    val db = surveySettingAction.dBComponent.db
    val allDB = for {
      _ <- db.run(taskQAction.deleteSchema)
      _ <- db.run(surveyTaskQAction.deleteSchema)
      _ <- db.run(surveyPhoneNumberAction.deleteSchema)

      _ <- db.run(orgAction.deleteSchema)
      _ <- db.run(surveyOrgAction.deleteSchema)
      _ <- db.run(workerAction.deleteSchema)
      _ <- db.run(workerOrganisationAction.deleteSchema)
      _ <- db.run(accountAction.deleteSchema)
      res <- testObject.deleteSchema
    } yield (res)
    Await.result(allDB, 10.seconds)
  }

  import taskQAction.jdbcProfile.api._

  def insert[A](query: DBIO[A]) = {
    whenReady(h2DBComponent.db.run(query)) {
      identity
    }
  }

  it should "retrieve survey setting if condition met" in {
    val surveyVO = SurveySetting(Some(11L), 1, "test_survey1", "call", "inbound", "123", Some("survey_call"), Some(3),
      Some(3), Some(80), Some(60 * 1000), true)

    val taskVO = TaskQueueModel(Some(11L), "targetWorkers", "friendlyName", Some("taskSid"), None, true, 1, "someone", ZonedDateTime.now())
    val survey: SurveySetting = insert(surveySettingAction.save(surveyVO))
    val taskQ: TaskQueueModel = insert(taskQAction.save(taskVO))
    val account = insert(accountAction.save(accountVo))
    val surveyTaskQ = SurveyTaskQMapping(None, survey.id.getOrElse(0), taskQ.id.getOrElse(0))
    insert(surveyTaskQAction.save(surveyTaskQ))
    val phoneNumberFormat = SurveyPhoneNumberFormat(None, survey.id.getOrElse(0), "*<>*/sd*", true)
    insert(surveyPhoneNumberAction.save(phoneNumberFormat))
    val callDtl = ContactingSurveyTriggerEvent("cjid", "inbound", "taskSid", "workerSid", "customerPhoneNumber", true, 2 * 60 * 1000,
      "taskSid", "11", "workspaceSid", "callSid", "taskReason", 1635742083856L)

    val surveySetting = whenReady(testObject.getIfSurveyEligibleForInbound(callDtl))(identity)

    surveySetting.setting.isEmpty shouldBe false
    surveySetting.account.isEmpty shouldBe false
    surveySetting.phFormats.isEmpty shouldBe false
  }

  it should "retrieve survey setting if condition met for both direction" in {
    val surveyVO = SurveySetting(Some(11L), 1, "test_survey2", "call", "INBOUND-OUTBOUND", "123", Some("survey_call"), Some(3),
      Some(3), Some(80), Some(60 * 1000), true)
    val taskVO = TaskQueueModel(Some(11L), "targetWorkers", "friendlyName", Some("taskSid"), None, true, 1, "someone", ZonedDateTime.now())
    val survey: SurveySetting = insert(surveySettingAction.save(surveyVO))
    val taskQ: TaskQueueModel = insert(taskQAction.save(taskVO))
    val account = insert(accountAction.save(accountVo))
    val surveyTaskQ = SurveyTaskQMapping(None, survey.id.getOrElse(0), taskQ.id.getOrElse(0))
    insert(surveyTaskQAction.save(surveyTaskQ))
    val phoneNumberFormat = SurveyPhoneNumberFormat(None, survey.id.getOrElse(0), "*<>*/sd*", true)
    insert(surveyPhoneNumberAction.save(phoneNumberFormat))
    val callDtl = ContactingSurveyTriggerEvent("cjid", "inbound", "taskSid", "workerSid", "customerPhoneNumber", true, 2 * 60 * 1000,
      "taskSid", "11", "workspaceSid", "callSid", "taskReason", 1635742083856L)

    val surveySetting = whenReady(testObject.getIfSurveyEligibleForInbound(callDtl))(identity)
    surveySetting.setting.isEmpty shouldBe false
    surveySetting.account.isEmpty shouldBe false
    surveySetting.phFormats.isEmpty shouldBe false
  }

  it should "not retrieve survey setting if condition doesn't met" in {
    val surveyVO = SurveySetting(Some(11L), 1, "test_survey3", "call", "inbound", "123", Some("survey_call"), Some(3),
      Some(3), Some(80), Some(60 * 1000), true)
    val taskVO = TaskQueueModel(Some(10L), "targetWorkers", "friendlyName", Some("taskSid"), None, true, 1, "someone", ZonedDateTime.now())
    val survey: SurveySetting = insert(surveySettingAction.save(surveyVO))
    val taskQ: TaskQueueModel = insert(taskQAction.save(taskVO))
    val account = insert(accountAction.save(accountVo))
    val surveyTaskQ = SurveyTaskQMapping(None, survey.id.getOrElse(0), taskQ.id.getOrElse(0))
    insert(surveyTaskQAction.save(surveyTaskQ))
    val phoneNumberFormat = SurveyPhoneNumberFormat(None, survey.id.getOrElse(0), "*<>*/sd*", true)
    insert(surveyPhoneNumberAction.save(phoneNumberFormat))
    val callDtl = ContactingSurveyTriggerEvent("cjid", "inbound", "taskQueueSid", "workerSid", "customerPhoneNumber", true, 30 * 1000,
      "taskSid", "11", "workspaceSid", "callSid", "taskReason", 1635742083856L)

    val surveySetting = whenReady(testObject.getIfSurveyEligibleForInbound(callDtl))(identity)

    surveySetting.setting.isEmpty shouldBe true
  }

  it should "retrieve survey setting for outobound" in {
    val surveyRepoSpy = spy[FutureSurveySettingRepository](new FutureSurveySettingRepository(surveySettingQueries))
    val testObjectAsync = new AsyncSurveySettingRepository[IO](surveyRepoSpy)
    val surveyVO = SurveySetting(Some(11L), 1, "test_survey", "call", "outbound", "123", Some("survey_call"), Some(3),
      Some(3), Some(80), Some(60 * 1000), true)
    val organisationModel = OrganisationModel(Some(11L), "name", 1L, None, OrganisationLevelEnumeration.CLT)
    val surveyOrgMapping = SurveyOrgMapping(None, 11, 11)
    val workerModel = WorkerModel(11, Some("workerSid"), "friendlyName", None, true, 11, "11", None, None, None, None, "source", LocalDateTime.now(), None, None, None, "displayName", None, false, false, true)
    val workerOrgModel = WorkerOrganisationModel(None, 11L, 11, 1)
    val survey: SurveySetting = insert(surveySettingAction.save(surveyVO))
    val worker = insert(workerAction.save(workerModel))
    val organization = insert(orgAction.save(organisationModel))
    val account = insert(accountAction.save(accountVo))
    val workerOrgNew = workerOrgModel.copy(workerId = worker.id, organisationId = organization.id.get)
    val workerOrganisation = insert(workerOrganisationAction.save(workerOrgNew))
    val surveyWorkerNew = surveyOrgMapping.copy(surveyId = survey.id.getOrElse(0), orgId = organization.id.get)
    val surveyWorker = insert(surveyOrgAction.save(surveyWorkerNew))
    val phoneNumberFormat = SurveyPhoneNumberFormat(None, survey.id.getOrElse(0), "*<>*/sd*", true)
    insert(surveyPhoneNumberAction.save(phoneNumberFormat))

    val callDtl = ContactingSurveyTriggerEvent("cjid", "outbound", "taskQueueSid", "workerSid", "customerPhoneNumber", true, 30 * 1000,
      "taskSid", "11", "workspaceSid", "callSid", "taskReason", 1635742083856L)

    val result = testObjectAsync.getIfSurveyEligibleForOutbound(callDtl).unsafeRunSync()
    result.setting.isEmpty shouldBe false
    result.account.isEmpty shouldBe false
    result.phFormats.isEmpty shouldBe false
  }

  it should "retrieve survey setting for outobound with nested org" in {
    val surveyRepoSpy = spy[FutureSurveySettingRepository](new FutureSurveySettingRepository(surveySettingQueries))
    val testObjectAsync = new AsyncSurveySettingRepository[IO](surveyRepoSpy)

    val organisationModelSuperCircle = OrganisationModel(None, "SuperCircleOrg", 1L, None, OrganisationLevelEnumeration.SUPER_CIRCLE)
    val organizationSuperCircle = insert(orgAction.save(organisationModelSuperCircle))

    val organisationModelCircle = OrganisationModel(None, "CircleOrg", 1L, Some(organisationModelSuperCircle.id.getOrElse(1L)), OrganisationLevelEnumeration.CIRCLE)
    val organizationCircle = insert(orgAction.save(organisationModelCircle))

    val organisationModel = OrganisationModel(None, "TeamOrg", 1L, Some(organisationModelCircle.id.getOrElse(2L)), OrganisationLevelEnumeration.CLT)
    val organization = insert(orgAction.save(organisationModel))

    val surveyOrgMapping = SurveyOrgMapping(None, 11, 11)


    val workerModel = WorkerModel(11, Some("workerSid"), "friendlyName", None, true, 11, "11", None, None, None, None, "source", LocalDateTime.now(), None, None, None, "displayName", None, false, false, true)
    val worker = insert(workerAction.save(workerModel))

    val workerOrgModel = WorkerOrganisationModel(None, 11L, organizationCircle.id.getOrElse(3L), 1)
    val workerOrgNew = workerOrgModel.copy(workerId = worker.id, organisationId = organization.id.get)
    val workerOrganisation = insert(workerOrganisationAction.save(workerOrgNew))
    val account = insert(accountAction.save(accountVo))

    val surveyVO = SurveySetting(Some(11L), 1, "test_survey", "call", "outbound", "123", Some("survey_call"), Some(3),
      Some(3), Some(80), Some(60 * 1000), true)
    val survey: SurveySetting = insert(surveySettingAction.save(surveyVO.copy(accountId = account.id.get)))

    val surveyWorkerNew = surveyOrgMapping.copy(surveyId = survey.id.getOrElse(0), orgId = organizationSuperCircle.id.get)
    val surveyWorker = insert(surveyOrgAction.save(surveyWorkerNew))
    val phoneNumberFormat = SurveyPhoneNumberFormat(None, survey.id.get, "*<>*/sd*", true)
    insert(surveyPhoneNumberAction.save(phoneNumberFormat))

    val callDtl = ContactingSurveyTriggerEvent("cjid", "outbound", "taskQueueSid", "workerSid", "customerPhoneNumber", true, 30 * 1000,
      "taskSid", "11", "workspaceSid", "callSid", "taskReason", 1635742083856L)

    val result: SurveySettingOptions = testObjectAsync.getIfSurveyEligibleForOutbound(callDtl).unsafeRunSync()
    result.setting.isEmpty shouldBe false
    result.account.isEmpty shouldBe false
    result.phFormats.isEmpty shouldBe false
  }

  it should "retrieve survey setting for outobound if setting is in both" in {
    val surveyRepoSpy = spy[FutureSurveySettingRepository](new FutureSurveySettingRepository(surveySettingQueries))
    val testObjectAsync = new AsyncSurveySettingRepository[IO](surveyRepoSpy)
    val surveyVO = SurveySetting(Some(11L), 1, "test_survey4", "call", "INBOUND-OUTBOUND", "123", Some("survey_call"), Some(3),
      Some(3), Some(80), Some(60 * 1000), true)
    val organisationModel = OrganisationModel(Some(11L), "name", 1L, None, OrganisationLevelEnumeration.CLT)
    val organization = insert(orgAction.save(organisationModel))
    val surveyOrgMapping = SurveyOrgMapping(None, 11, 11)
    val workerModel = WorkerModel(11, Some("workerSid"), "friendlyName", None, true, 11, "11", None, None, None, None, "source", LocalDateTime.now(), None, None, None, "displayName", None, false, false, true)
    val workerOrgModel = WorkerOrganisationModel(None, 11L, 11, 1)
    val survey: SurveySetting = insert(surveySettingAction.save(surveyVO))
    val worker = insert(workerAction.save(workerModel))
    val account = insert(accountAction.save(accountVo))
    val workerOrgNew = workerOrgModel.copy(workerId = worker.id, organisationId = organization.id.get)
    val workerOrganisation = insert(workerOrganisationAction.save(workerOrgNew))
    val surveyWorkerNew = surveyOrgMapping.copy(surveyId = survey.id.getOrElse(0), orgId = organization.id.get)
    val surveyWorker = insert(surveyOrgAction.save(surveyWorkerNew))
    val phoneNumberFormat = SurveyPhoneNumberFormat(None, survey.id.getOrElse(0), "*<>*/sd*", true)
    insert(surveyPhoneNumberAction.save(phoneNumberFormat))

    val callDtl = ContactingSurveyTriggerEvent("cjid", "outbound", "taskQueueSid", "workerSid", "customerPhoneNumber", true, 30 * 1000,
      "taskSid", "11", "workspaceSid", "callSid", "taskReason", 1635742083856L)

    val result = testObjectAsync.getIfSurveyEligibleForOutbound(callDtl).unsafeRunSync()
    result.setting.isEmpty shouldBe false
    result.account.isEmpty shouldBe false
    result.phFormats.isEmpty shouldBe false
  }

  it should "not retrieve if channel is not call" in {
    val surveyVO = SurveySetting(Some(12L), 1, "test_survey5", "chat", "INBOUND-OUTBOUND", "123", Some("survey_call"), Some(3),
      Some(3), Some(80), Some(1000), false)
    val taskVO = TaskQueueModel(Some(11L), "targetWorkers", "friendlyName", Some("taskSid2"), None, true, 1, "someone", ZonedDateTime.now())
    val survey: SurveySetting = insert(surveySettingAction.save(surveyVO))
    val taskQ: TaskQueueModel = insert(taskQAction.save(taskVO))
    val account = insert(accountAction.save(accountVo))
    val surveyTaskQ = SurveyTaskQMapping(None, survey.id.getOrElse(0), taskQ.id.getOrElse(0))
    insert(surveyTaskQAction.save(surveyTaskQ))
    val phoneNumberFormat = SurveyPhoneNumberFormat(None, survey.id.getOrElse(0), "*<>*/sd*", true)
    insert(surveyPhoneNumberAction.save(phoneNumberFormat))
    val callDtl = ContactingSurveyTriggerEvent("cjid", "inbound", "taskSid2", "workerSid", "customerPhoneNumber", false, 10 * 1000,
      "taskSid", "11", "workspaceSid", "callSid", "taskReason", 1635742083856L)

    val surveySetting = whenReady(testObject.getIfSurveyEligibleForInbound(callDtl))(identity)
    surveySetting.setting.isEmpty shouldBe true
  }

  it should "return the self service survey setting" in {
    val surveyVO = SurveySetting(Some(11L), 1, "selfServiceSurvey", "call", "outbound", "123", Some("survey_call"), Some(3),
      Some(3), Some(80), Some(60 * 1000), true)

    val survey: SurveySetting = insert(surveySettingAction.save(surveyVO))
    insert(accountAction.save(accountVo))
    val phoneNumberFormat = SurveyPhoneNumberFormat(None, survey.id.getOrElse(0), "*<>*/sd*", true)
    insert(surveyPhoneNumberAction.save(phoneNumberFormat))

    val selfServiceSurveyEvent = SelfServiceSurveyTriggerEvent("cjid", "customerPhoneNumber", 2 * 60 * 1000, "123", "eventId", "callSid", 1635742083856L, "selfServiceSurvey")
    val surveySetting = whenReady(testObject.findByName(selfServiceSurveyEvent.surveyName, selfServiceSurveyEvent.accountSid))(identity)

    surveySetting.setting.isEmpty shouldBe false
    surveySetting.account.isEmpty shouldBe false
    surveySetting.phFormats.isEmpty shouldBe false
  }

}
