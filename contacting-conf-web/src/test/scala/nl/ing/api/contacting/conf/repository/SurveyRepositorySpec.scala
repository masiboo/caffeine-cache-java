package nl.ing.api.contacting.conf.repository

import cats.effect.IO
import nl.ing.api.contacting.conf.repository.cslick.actions.{SurveyOrgAction, SurveyPhNumberFormatAction, SurveySettingAction, SurveySettingQueries, SurveyTaskQAction}
import nl.ing.api.contacting.conf.repository.model.SurveyOrgMapping
import nl.ing.api.contacting.conf.repository.model.SurveyPhoneNumberFormat
import nl.ing.api.contacting.conf.repository.model.SurveySetting
import nl.ing.api.contacting.conf.repository.model.SurveyTaskQMapping
import nl.ing.api.contacting.conf.support.TestData
import nl.ing.api.contacting.repository.cslick.actions.AccountAction
import nl.ing.api.contacting.repository.cslick.actions.OrganisationAction
import nl.ing.api.contacting.repository.cslick.actions.TaskQueueAction
import nl.ing.api.contacting.repository.cslick.actions.WorkerAction
import nl.ing.api.contacting.repository.cslick.actions.WorkerOrganisationAction
import org.scalatestplus.junit.JUnitRunner
import nl.ing.api.contacting.conf.modules.ExecutionContextConfig.ioRunTime
import nl.ing.api.contacting.domain.slick.AccountVO
import org.h2.jdbc.JdbcSQLException

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration.DurationInt

@org.junit.runner.RunWith(value = classOf[JUnitRunner])
class SurveyRepositorySpec extends SlickBaseSpec with TestData {

  val surveySettingAction = new SurveySettingAction(h2DBComponent)
  val surveyPhoneNumberFormatAction = new SurveyPhNumberFormatAction(h2DBComponent, surveySettingAction)
  val taskQueueAction = new TaskQueueAction(h2DBComponent)
  val surveyTaskQAction = new SurveyTaskQAction(h2DBComponent, surveySettingAction, taskQueueAction)
  val organisationAction = new OrganisationAction(h2DBComponent)
  val surveyOrgAction = new SurveyOrgAction(h2DBComponent, surveySettingAction, organisationAction)
  val surveySettingQueries = new SurveySettingQueries(h2DBComponent, surveyTaskQAction, surveyPhoneNumberFormatAction, surveyOrgAction, workerOrgAction, accountAction)
  private lazy val workerAction = new WorkerAction(h2DBComponent)
  private lazy val workerOrgAction = new WorkerOrganisationAction(h2DBComponent, organisationAction, workerAction)
  lazy val accountAction = new AccountAction(h2DBComponent)


  val surveySettingRepo = new FutureSurveySettingRepository(surveySettingQueries)
  val ioSurveySettingRepo = new AsyncSurveySettingRepository[IO](surveySettingRepo)

  val surveyOrgRepo = new FutureSurveyOrgRepository(surveyOrgAction)
  val ioSurveyOrgRepo = new AsyncSurveyOrgRepository[IO](surveyOrgRepo)

  val surveyTaskQRepo = new FutureSurveyTaskQRepository(surveyTaskQAction)
  val ioSurveyTaskQRepo = new AsyncSurveyTaskQRepository[IO](surveyTaskQRepo)

  val surveyPhNumberFormatRepo = new FutSurveyPhNumberFormatRepo(surveyPhoneNumberFormatAction)
  val ioSurveyPhNumberFormatRepo = new AsyncSurveyPhNumberFormatRepo[IO](surveyPhNumberFormatRepo)

  val testObject = new SurveyRepository[IO](ioSurveySettingRepo, ioSurveyPhNumberFormatRepo, ioSurveyTaskQRepo, ioSurveyOrgRepo)

  import h2DBComponent.db


  override def beforeAll(): Unit = {
    val allInserts =
      for {
        _ <- db.run(accountAction.createSchema)
        _ <- db.run(organisationAction.createSchema)
        _ <- db.run(taskQueueAction.createSchema)
        _ <- db.run(surveySettingAction.createSchema)
        _ <- db.run(surveyPhoneNumberFormatAction.createSchema)
        _ <- db.run(surveyTaskQAction.createSchema)
        res <- db.run(surveyOrgAction.createSchema)
        _ <- db.run(accountAction.insert(AccountVO(Some(1), Some("123"), "account-unit", true, 1l, 1l, "Europe/Amsterdam", "NL", "1079")))
        _ <- db.run(organisationAction.insert(superCircle1OrganisationModel))
        _ <- db.run(organisationAction.insert(superCircle2OrganisationModel))
        _ <- db.run(taskQueueAction.insert(taskQueueModel))
        _ <- db.run(taskQueueAction.insert(taskQueueModel1))
      } yield res

    whenReady(allInserts) {
      _ =>
    }
  }

  override def afterAll(): Unit = {
    Await.result(Future.sequence {
      List(
        db.run(accountAction.deleteSchema),
        db.run(surveyPhoneNumberFormatAction.deleteSchema),
        db.run(surveyTaskQAction.deleteSchema),
        db.run(surveyOrgAction.deleteSchema),
        db.run(surveySettingAction.deleteSchema),
        db.run(organisationAction.deleteSchema),
        db.run(taskQueueAction.deleteSchema)
        )
    }, 10.seconds)
  }

  "crud operations and search in survey settings" should "succeed" in {
    val surveySettingModel = SurveySetting(None, account.id, "sample survey setting 1","call", "inbound", "voiceId", None, None, None, None, None, false)
    val phoneFormats = SurveyPhoneNumberFormat(None, -1l, "+31*", false)
    val connectedOrgs = List(SurveyOrgMapping(None, -1l, superCircle1OrganisationModel.id.get), SurveyOrgMapping(None, -1l, superCircle2OrganisationModel.id.get))
    val connectedTQs = SurveyTaskQMapping(None, -1l, taskQueueModel.id.get)
    val operations = for {
      model <- db.run(taskQueueAction.insert(taskQueueModel))
      _ <- db.run(taskQueueAction.insert(taskQueueModel1))
      insertSetting <- db.run(surveySettingAction.save(surveySettingModel))
      _ <- db.run(surveyPhoneNumberFormatAction.save(phoneFormats.copy(surveyId = insertSetting.id.get)))
      _ <- db.run(surveyOrgAction.save(connectedOrgs.map(_.copy(surveyId = insertSetting.id.get))))
      _ <- db.run(surveyTaskQAction.save(connectedTQs.copy(surveyId = insertSetting.id.get, taskqueueId = model)))
      searchSetting <- testObject.getSurveySettings(insertSetting.id.get, context).unsafeToFuture()
      searchDetails <- testObject.getSurveyDetails(insertSetting.id.get, context).unsafeToFuture()
      insertSetting2 <- db.run(surveySettingAction.save(surveySettingModel.copy(name = "survey setting 2")))
      all <- testObject.getSurveySettings(context).unsafeToFuture()
    } yield (model, searchSetting, searchDetails,insertSetting2,all)

    whenReady(operations){
      case (model,searchSetting,searchDetails,insertSetting2,all) =>
        searchSetting.isDefined shouldBe true
        searchSetting.get.id.isDefined shouldBe true
        searchSetting.get.copy(id = None) shouldBe surveySettingModel

        searchDetails.isDefined shouldBe true
        searchDetails.get.setting.copy(id = None) shouldBe surveySettingModel
        searchDetails.get.orgMapping.size shouldBe 2
        searchDetails.get.orgMapping.exists(_._1._1._2.id == superCircle1OrganisationModel.id) shouldBe true
        searchDetails.get.orgMapping.exists(_._1._1._2.id == superCircle2OrganisationModel.id) shouldBe true
        searchDetails.get.taskQMapping.exists(_._1.taskqueueId == model) shouldBe true

        searchDetails.get.taskQMapping.exists(_._2 == taskQueueModel.friendlyName) shouldBe true

        insertSetting2.name shouldBe "survey setting 2"
        all.size shouldBe 2
    }
  }

  "update on survey setting" should "work" in {
    val surveySettingModel = SurveySetting(None, account.id, "sample survey setting 2",
      "call","inbound","voiceId",None,None,None,None,None,false)
    val format1 = SurveyPhoneNumberFormat(Some(1l), -1l, "+31", false)
    val format2 = SurveyPhoneNumberFormat(Some(2l), -1l, "+32", true)
    val format3 = SurveyPhoneNumberFormat(Some(3l), -1l, "+33", false)
    val operations = for {
      insert <- testObject.surveySettingRepo.save(surveySettingModel, context)
      _ <- testObject.updateSurveySettings(surveySettingModel.copy(id = insert.id, name = "changed name"), Seq(format1.copy(surveyId = insert.id.get)), Nil, context).value
      findAfter1stUpdate <- testObject.getSurveyDetails(insert.id.get, context)
      _ <-testObject.updateSurveySettings(surveySettingModel.copy(id = insert.id, voiceSurveyId = "changed voiceid"), Seq(format2.copy(surveyId = insert.id.get), format3.copy(surveyId = insert.id.get)), Seq(format1.copy(id = findAfter1stUpdate.get.phNumFormat.head.id, surveyId = insert.id.get)), context).value
      findAfter2ndUpdate <- testObject.getSurveyDetails(insert.id.get, context)
    } yield (findAfter1stUpdate, findAfter2ndUpdate)

    whenReady(operations.unsafeToFuture()) {
      case (findAfter1stUpdate, findAfter2ndUpdate) =>
        findAfter1stUpdate.isDefined shouldBe true
        val settingVO = findAfter1stUpdate.get.setting
        settingVO.name shouldBe "changed name"
        val phoneNumberFormats = findAfter1stUpdate.get.phNumFormat
        phoneNumberFormats.size shouldBe 1
        phoneNumberFormats.exists(_.format == "+31") shouldBe true
        phoneNumberFormats.exists(_.direction == false) shouldBe true

        findAfter2ndUpdate.isDefined shouldBe true
        val setting1VO = findAfter2ndUpdate.get.setting
        setting1VO.name shouldBe "sample survey setting 2"
        setting1VO.voiceSurveyId shouldBe "changed voiceid"
        val phoneNumberFormats1 = findAfter2ndUpdate.get.phNumFormat
        phoneNumberFormats1.size shouldBe 2
        val excluded = phoneNumberFormats1.find(_.format == "+32")
        excluded.isDefined shouldBe true
        excluded.get.direction shouldBe true

        val included = phoneNumberFormats1.find(_.format == "+33")
        included.isDefined shouldBe true
        included.get.direction shouldBe false
    }
  }

  "connected tq" should "be added and deleted" in {
    val surveySettingModel = SurveySetting(None, account.id, "sample survey setting 3", "call", "inbound", "voiceId", None, None, None, None, None, false)
    val orgList = List(superCircle1OrganisationModel.id.get, superCircle2OrganisationModel.id.get)
    val operations = for {
      model1 <- IO.fromFuture(IO(db.run(taskQueueAction.insert(taskQueueModel))))
      model2 <- IO.fromFuture(IO(db.run(taskQueueAction.insert(taskQueueModel1))))
      tqList = List(model1, model2)
      insertSetting <- testObject.surveySettingRepo.save(surveySettingModel, context)
      id = insertSetting.id.get
      _ <- testObject.addRemoveTaskqueues(id, tqList, Nil)
      _ <- testObject.addRemoveOrgs(id, orgList, Nil)
      search1 <- testObject.getSurveyDetails(id, context)
      _ <- testObject.addRemoveTaskqueues(id, Nil, tqList)
      _ <- testObject.addRemoveOrgs(id, Nil, orgList)
      search2 <- testObject.getSurveyDetails(id, context)
    } yield (search1, search2)

    whenReady(operations.unsafeToFuture()) {
      case (afterAdd, afterDelete) =>
        afterAdd.isDefined shouldBe true
        afterAdd.get.taskQMapping.size shouldBe 2
        afterAdd.get.orgMapping.size shouldBe 2
        afterDelete.isDefined shouldBe true
        afterDelete.get.taskQMapping.size shouldBe 0
        afterDelete.get.orgMapping.size shouldBe 0
    }
  }

  "survey and all related foreign keys" should "be deleted" in {
    val surveySettingModel = SurveySetting(None, account.id, "sample survey setting 4",
      "call", "inbound", "voiceId", None, None, None, None, None, false)
    val tqList = List(taskQueueModel.id.get, taskQueueModel1.id.get)
    val orgList = List(superCircle1OrganisationModel.id.get, superCircle2OrganisationModel.id.get)
    val operations = for {
      insertSetting <- testObject.surveySettingRepo.save(surveySettingModel, context)
      id = insertSetting.id.get
      _ <- testObject.addRemoveTaskqueues(id, tqList, Nil)
      _ <- testObject.addRemoveOrgs(id, orgList, Nil)
      search1 <- testObject.getSurveyDetails(id, context)
      _ <- testObject.removeSurveySetting(id, context)
      search2 <- testObject.getSurveyDetails(id, context)
      searchOrgs <- testObject.surveyOrgRepository.getAllBySurveyId(id)
      searchTqs <- testObject.surveyTaskQRepository.getAllBySurveyId(id)
    } yield (search1, search2, searchOrgs, searchTqs)

    whenReady(operations.unsafeToFuture()) {
      case (afterAdd, afterDelete, searchOrgs, searchTqs) =>
        afterAdd.isDefined shouldBe true
        afterAdd.get.taskQMapping.size shouldBe 2
        afterAdd.get.orgMapping.size shouldBe 2

        afterDelete.isDefined shouldBe false

        searchOrgs shouldBe Nil
        searchTqs shouldBe Nil
    }
  }

  "duplicate survey name" should "not be allowed" in {
    val surveySettingModel = SurveySetting(None, 1, "sample survey setting 4",
      "call", "outbound", "voiceId", None, None, None, None, None, false)
    val insertSetting =  for {
      _ <- testObject.surveySettingRepo.save(surveySettingModel, context)
      _ <- testObject.surveySettingRepo.save(surveySettingModel, context)
    } yield ()

    whenReady(insertSetting.unsafeToFuture().failed) {
      result =>
        result shouldBe a [JdbcSQLException]
    }
  }

  "find by survey name" should "return survey setting based on the name" in {
    val surveySettingModel = SurveySetting(None, 1, "sss", "call", "outbound", "voiceId", None, None, None, None, None, false)
    val format1 = SurveyPhoneNumberFormat(Some(1L), -1L, "+31", false)

    val operations = for {
      insert <- testObject.surveySettingRepo.save(surveySettingModel, context)
      _ <- testObject.updateSurveySettings(surveySettingModel.copy(id = insert.id, name = "sss1"), Seq(format1.copy(surveyId = insert.id.get)), Nil, context).value
      search <- testObject.getSurveySettingsByName("sss1", "123")
    } yield search

    whenReady(operations.unsafeToFuture())(
      searchResult =>
        searchResult.setting.isDefined shouldBe true)
  }

  "find by survey name" should "return empty for wrong survey name" in {
    val surveySettingModel = SurveySetting(None, 1, "sss2", "call", "outbound", "voiceId", None, None, None, None, None, false)
    val operations = for {
      _ <- testObject.surveySettingRepo.save(surveySettingModel, context)
      search <- testObject.getSurveySettingsByName("sss3", "123")
    } yield search

    whenReady(operations.unsafeToFuture()) {
      searchResult =>
        searchResult.setting.isEmpty shouldBe true
    }
  }
}
