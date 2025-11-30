package nl.ing.api.contacting.conf.business.survey

import cats.Parallel
import cats.effect.Async
import cats.implicits._
import com.ing.api.contacting.dto.context.ContactingContext
import nl.ing.api.contacting.conf.domain.SurveyCallRecordVO
import nl.ing.api.contacting.conf.domain.SurveyDetailsVO
import nl.ing.api.contacting.conf.domain.SurveySettingVO
import nl.ing.api.contacting.conf.domain.SurveyUpdateVO
import nl.ing.api.contacting.conf.exception.RefinedTypeError
import nl.ing.api.contacting.conf.mapper.SurveyMapper._
import nl.ing.api.contacting.conf.modules.ExecutionContextConfig.executionContext
import nl.ing.api.contacting.conf.repository.SurveyRepository
import nl.ing.api.contacting.conf.repository.cassandra.SurveyCallRecordsRepository
import nl.ing.api.contacting.conf.repository.model.SurveySetting
import nl.ing.api.contacting.tracing.Trace
import nl.ing.api.contacting.util.ResultF
import nl.ing.api.contacting.util.syntaxf.FutureSyntax.FutureAsync

import scala.language.higherKinds


/**
 * @author Ayush Mittal
 */
class SurveyService[F[_]: Async: Trace](surveyRepository: SurveyRepository[F], surveyCallRecordsRepository: SurveyCallRecordsRepository[F]) {


  /**
   * Get survey settings and related info
   * @param surveyId : Survey Id
   * @param context: contacting context
   * @param parallel : implicit instance of cats parallel to fetch data from detail tables
   * @return SurveyDetails if found else None
   */
  def getSurveyDetails(surveyId: Long, context: ContactingContext)(implicit parallel: Parallel[F]): F[Option[Either[RefinedTypeError, SurveyDetailsVO]]] = {
    surveyRepository.getSurveyDetails(surveyId,context).map(_.map(surveyDetailsToVo))
  }

  /**
   *
   * @param context: contacting context
   * @return : SurveySettingsVO
   */
  def getAllSurveySettings(context: ContactingContext): F[Seq[Either[RefinedTypeError, SurveySettingVO]]] =
    surveyRepository.getSurveySettings(context).map(_.map(surveySettingModelToVo))

  /**
   *
   * @param surveySettingVO
   * @param context
   * @return
   */
  def createSurveySettings(surveySettingVO: SurveySettingVO, context: ContactingContext): F[SurveySetting] =
    surveyRepository.surveySettingRepo.save(surveySettingVoToModel(surveySettingVO), context)

  /**
   *
   * @param surveySettingVO
   * @param context
   * @return
   */
  def updateSurveySettings(surveySettingVO: SurveySettingVO, context: ContactingContext): F[SurveySetting] =
    surveyRepository.surveySettingRepo.update(surveySettingVoToModel(surveySettingVO), context)


  /**
   *
   * @param surveyUpdateVO
   * @return
   */
  def updateSurveySettings(surveyUpdateVO: SurveyUpdateVO, context: ContactingContext): ResultF[F, Unit] =
    surveyRepository.updateSurveySettings(
      surveySettingVoToModel(surveyUpdateVO.settings),
      surveyUpdateVO.formatsAdded.map(surveyPhNumFormatToVoModel),
      surveyUpdateVO.formatsRemoved.map(surveyPhNumFormatToVoModel),
      context)
  /**
   * Add, remove connected orgs
   */
  def addRemoveOrgs(surveyId: Long, orgsToAdd: List[Long], orgsToRemove: List[Long]): F[List[Any]] =
    surveyRepository.addRemoveOrgs(surveyId, orgsToAdd, orgsToRemove)

  /**
   * Add, remove connected taskqueues
   */
  def addRemoveTaskqueues(surveyId: Long, taskQueuesToAdd: List[Long], taskQueuesToRemove: List[Long]): F[List[Any]] =
    surveyRepository.addRemoveTaskqueues(surveyId, taskQueuesToAdd, taskQueuesToRemove)

  /**
   * Remove survey setting
   */
  def removeSurveySetting(surveyId: Long, context: ContactingContext): F[Int] =
    surveyRepository.removeSurveySetting(surveyId, context)


  /**
   * Get offered survey details
   */
  def getOfferedSurveyCalls(accountFriendlyName: String, phoneNum: String): F[List[SurveyCallRecordVO]] =
    surveyCallRecordsRepository.findCallRecords(accountFriendlyName, phoneNum)

}
