package nl.ing.api.contacting.conf.repository

import cats.effect.Async
import nl.ing.api.contacting.conf.repository.SurveyTaskQRepository.SurveyTaskQDetails
import nl.ing.api.contacting.conf.repository.cslick.actions.SurveyTaskQAction
import nl.ing.api.contacting.conf.repository.model.SurveyTaskQMapping
import nl.ing.api.contacting.tracing.Trace

import scala.concurrent.Future

/**
 * @author Ayush Mittal
 */

object SurveyTaskQRepository {
  type SurveyTaskQDetails = (SurveyTaskQMapping, String)
}
trait SurveyTaskQRepository[F[_]] {
  def getAllBySurveyId(surveyId: Long): F[Seq[SurveyTaskQDetails]]

  def addRemoveTaskQueues(surveyId: Long, taskQueuesToAdd: List[Long], taskQueuesToRemove: List[Long]): F[List[Any]]
}


class FutureSurveyTaskQRepository(surveyTaskQAction: SurveyTaskQAction) extends SurveyTaskQRepository[Future]{

  import surveyTaskQAction.dBComponent.db

  def getAllBySurveyId(surveyId: Long): Future[Seq[SurveyTaskQDetails]] = {
    db.run(surveyTaskQAction.findBySurveyId(surveyId))
  }

  override def addRemoveTaskQueues(surveyId: Long, taskQueuesToAdd: List[Long], taskQueuesToRemove: List[Long]): Future[List[Any]] =
    db.run(surveyTaskQAction.addRemoveTaskQueues(surveyId, taskQueuesToAdd, taskQueuesToRemove))
}

class AsyncSurveyTaskQRepository[F[_]: Async : Trace](futRepo: FutureSurveyTaskQRepository) extends SurveyTaskQRepository[F]{

  import nl.ing.api.contacting.conf.modules.ExecutionContextConfig.executionContext
  import nl.ing.api.contacting.util.syntaxf.FutureSyntax.FutureAsync

  override def getAllBySurveyId(surveyId: Long): F[Seq[SurveyTaskQDetails]] = futRepo.getAllBySurveyId(surveyId).asDelayedF

  override def addRemoveTaskQueues(surveyId: Long, taskQueuesToAdd: List[Long], taskQueuesToRemove: List[Long]): F[List[Any]] =
    futRepo.addRemoveTaskQueues(surveyId, taskQueuesToAdd, taskQueuesToRemove).asDelayedF
}