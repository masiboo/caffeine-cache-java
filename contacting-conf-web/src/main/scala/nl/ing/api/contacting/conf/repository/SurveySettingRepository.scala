package nl.ing.api.contacting.conf.repository

import cats.effect.Async
import com.typesafe.scalalogging.LazyLogging
import nl.ing.api.contacting.conf.modules.ExecutionContextConfig._
import nl.ing.api.contacting.conf.repository.cslick.actions.{SurveyOrgAction, SurveyPhNumberFormatAction, SurveySettingAction, SurveySettingQueries, SurveyTaskQAction}
import nl.ing.api.contacting.conf.repository.model.{SurveyOrgMapping, SurveyPhoneNumberFormat, SurveySetting, SurveySettingOptions}
import nl.ing.api.contacting.conf.surveytrigger.ContactingSurveyTriggerEvent
import nl.ing.api.contacting.domain.slick.AccountVO
import nl.ing.api.contacting.repository.AccountBasedRepo
import nl.ing.api.contacting.repository.cslick.AccountBasedSlickRepo
import nl.ing.api.contacting.repository.cslick.AccountBasedSlickRepoF
import nl.ing.api.contacting.repository.cslick.actions.AccountAction
import nl.ing.api.contacting.repository.cslick.actions.WorkerOrganisationAction
import nl.ing.api.contacting.repository.model.{OrganisationLevelEnumeration, OrganisationModel, WorkerModel, WorkerOrganisationModel}
import nl.ing.api.contacting.tracing.Trace

import scala.concurrent.Future
import scala.language.higherKinds


/**
 * @author Ayush Mittal
 */

trait SurveySettingRepository[F[_]] extends AccountBasedRepo[SurveySetting, Long, F] {
  def getIfSurveyEligibleForInbound(callDetail: ContactingSurveyTriggerEvent): F[SurveySettingOptions]

  def getIfSurveyEligibleForOutbound(callDetail: ContactingSurveyTriggerEvent): F[SurveySettingOptions]

  def findByName(name: String, accountSid: String): F[SurveySettingOptions]
}

class FutureSurveySettingRepository(surveySettingQueries: SurveySettingQueries) extends AccountBasedSlickRepo[SurveySetting,
  Long, SurveySettingAction](surveySettingQueries.surveySettingsAction.dBComponent) with SurveySettingRepository[Future] {
  override val slickActions: SurveySettingAction = surveySettingQueries.surveySettingsAction

  import slickActions.jdbcProfile.api._

  override def getIfSurveyEligibleForInbound(callDetail: ContactingSurveyTriggerEvent): Future[SurveySettingOptions] = {
    dBComponent.db.run(surveySettingQueries.inboundSurveyQuery(callDetail).result).map(_.foldLeft((None: Option[SurveySetting], None: Option[AccountVO], List[SurveyPhoneNumberFormat]())){case ((_, _, resSP), (ss, acc, sp)) => (Some(ss), Some(acc), resSP :+ sp)})
      .map(SurveySettingOptions.apply _ tupled _)
  }

  override def getIfSurveyEligibleForOutbound(callDetail: ContactingSurveyTriggerEvent): Future[SurveySettingOptions] = {
    dBComponent.db.run(surveySettingQueries.outboundSurveyQuery(callDetail).result).map(_.flatten.foldLeft((None: Option[SurveySetting], None: Option[AccountVO], List[SurveyPhoneNumberFormat]())){case ((_, _, resSP), (ss, acc, sp)) => (Some(ss), Some(acc), resSP :+ sp)})
      .map(SurveySettingOptions.apply _ tupled _)
  }

  override def findByName(name: String, accountSid: String): Future[SurveySettingOptions] = {
    val result = dBComponent.db.run(surveySettingQueries.getSurverySettingsByNameQuery(name, accountSid).result)
    result
      .map(_.foldLeft((None: Option[SurveySetting], None: Option[AccountVO], List[SurveyPhoneNumberFormat]())) { case ((_, _, resSP), (ss, acc, sp)) => (Some(ss), Some(acc), resSP :+ sp) })
      .map(SurveySettingOptions.apply _ tupled _)
  }
}

class AsyncSurveySettingRepository[F[_] : Async : Trace](futRepo: FutureSurveySettingRepository) extends AccountBasedSlickRepoF[F, SurveySetting,
  Long, SurveySettingAction](futRepo) with SurveySettingRepository[F] with LazyLogging {

  import nl.ing.api.contacting.util.syntaxf.FutureSyntax.FutureAsync


  override def getIfSurveyEligibleForInbound(callDetail: ContactingSurveyTriggerEvent): F[SurveySettingOptions] = {
    futRepo.getIfSurveyEligibleForInbound(callDetail).asDelayedF
  }

  override def getIfSurveyEligibleForOutbound(callDetail: ContactingSurveyTriggerEvent): F[SurveySettingOptions] = {
    futRepo.getIfSurveyEligibleForOutbound(callDetail).asDelayedF
  }

  override def findByName(name: String, accountSid: String): F[SurveySettingOptions] = {
    futRepo.findByName(name, accountSid).asDelayedF
  }

}
