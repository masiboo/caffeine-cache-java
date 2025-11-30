package nl.ing.api.contacting.conf.repository

import nl.ing.api.contacting.conf.repository.SurveyOrgRepository.SurveyOrgDetails
import nl.ing.api.contacting.conf.repository.cslick.actions.SurveyOrgAction
import nl.ing.api.contacting.conf.repository.cslick.actions.SurveySettingAction
import nl.ing.api.contacting.conf.repository.model.SurveySetting
import nl.ing.api.contacting.conf.support.TestData
import nl.ing.api.contacting.repository.cslick.actions.OrganisationAction
import nl.ing.api.contacting.repository.model.OrganisationLevelEnumeration
import org.scalatestplus.junit.JUnitRunner

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration.DurationInt

@org.junit.runner.RunWith(value = classOf[JUnitRunner])
class SurveyOrgRepositorySpec extends SlickBaseSpec with TestData {

  val surveySettingAction = new SurveySettingAction(h2DBComponent)
  val organisationAction = new OrganisationAction(h2DBComponent)
  val surveyOrgAction = new SurveyOrgAction(h2DBComponent, surveySettingAction, organisationAction)

  val testObject = new FutureSurveyOrgRepository(surveyOrgAction)

  import h2DBComponent.db
  override def beforeAll(): Unit = {
    val program = for {
      _ <- db.run(organisationAction.createSchema)
      _ <- db.run(organisationAction.save(superCircle1OrganisationModel))
      _ <- db.run(organisationAction.save(superCircle2OrganisationModel))
      _ <- db.run(organisationAction.save(circle1OrganisationModel))
      _ <- db.run(organisationAction.save(team1OrganisationModel))
      _ <- db.run(surveySettingAction.createSchema)
      _ <- db.run(surveyOrgAction.createSchema)
    } yield()

    Await.result(program, 10.seconds)
  }

  override def afterAll(): Unit = {
    Await.result(Future.sequence{
      List(
        db.run(surveyOrgAction.deleteSchema),
        db.run(surveySettingAction.deleteSchema),
        db.run(organisationAction.deleteSchema),
      )
    }, 10.seconds)
  }

  it should "search by survey id" in {
    val surveySettingModel = SurveySetting(None, account.id, "sample survey setting 1",
      "call","inbound","voiceId",None,None,None,None,None,false)
    val program = for {
      setting <- db.run(surveySettingAction.save(surveySettingModel))
      _ <- testObject.addRemoveOrgs(setting.id.get,List(superCircle2OrganisationModel.id.get, team1OrganisationModel.id.get), Nil)
      search <- testObject.getAllBySurveyId(setting.id.get)
    } yield search

    whenReady(program){
      result :Seq[SurveyOrgDetails] =>
        result.size shouldBe 2
        val superCircle2Mapping = result.find(_._1._1._1.orgId == superCircle2OrganisationModel.id.get)
        superCircle2Mapping.isDefined shouldBe true
        superCircle2Mapping.get._2.isDefined shouldBe false
        superCircle2Mapping.get._1._1._2.organisationLevel shouldBe OrganisationLevelEnumeration.SUPER_CIRCLE
        superCircle2Mapping.get._1._1._2.name shouldBe superCircle2OrganisationModel.name

        val team1Mapping = result.find(_._1._1._1.orgId == team1OrganisationModel.id.get)
        team1Mapping.isDefined shouldBe true
        team1Mapping.get._2.isDefined shouldBe true
        team1Mapping.get._2.get.id shouldBe superCircle1OrganisationModel.id

        team1Mapping.get._1._2.isDefined shouldBe true
        team1Mapping.get._1._2.get.name shouldBe circle1OrganisationModel.name
        team1Mapping.get._1._1._2.organisationLevel shouldBe OrganisationLevelEnumeration.CLT
        team1Mapping.get._1._1._2.name shouldBe team1OrganisationModel.name
    }
  }
}
