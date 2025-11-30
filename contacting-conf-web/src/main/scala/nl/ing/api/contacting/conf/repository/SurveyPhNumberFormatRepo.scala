package nl.ing.api.contacting.conf.repository

import cats.effect.Async
import nl.ing.api.contacting.conf.repository.cslick.actions.SurveyPhNumberFormatAction
import nl.ing.api.contacting.conf.repository.model.SurveyPhoneNumberFormat
import nl.ing.api.contacting.tracing.Trace

import scala.concurrent.Future

/**
 * @author Ayush Mittal
 */
trait SurveyPhNumberFormatRepo[F[_]] {
  def getAllBySurveyId(surveyId: Long): F[Seq[SurveyPhoneNumberFormat]]
  def addDeleteFormats(formatsAdded: Seq[SurveyPhoneNumberFormat], formatsRemoved: Seq[SurveyPhoneNumberFormat]): F[Unit]
}

class FutSurveyPhNumberFormatRepo(surveyPhNumberFormatAction: SurveyPhNumberFormatAction) extends SurveyPhNumberFormatRepo[Future] {

  import surveyPhNumberFormatAction.dBComponent.db

  override def getAllBySurveyId(surveyId: Long): Future[Seq[SurveyPhoneNumberFormat]] = {
    db.run(surveyPhNumberFormatAction.findBySurveyId(surveyId))
  }

  def addDeleteFormats(formatsAdded: Seq[SurveyPhoneNumberFormat], formatsRemoved: Seq[SurveyPhoneNumberFormat]): Future[Unit] = {
    import nl.ing.api.contacting.conf.modules.ExecutionContextConfig.executionContext
    db.run(surveyPhNumberFormatAction.addDeleteFormats(formatsAdded,formatsRemoved)).map(_ => ())
  }
}

class AsyncSurveyPhNumberFormatRepo[F[_]: Async : Trace](futRepo: FutSurveyPhNumberFormatRepo) extends SurveyPhNumberFormatRepo[F]{

  import nl.ing.api.contacting.conf.modules.ExecutionContextConfig.executionContext
  import nl.ing.api.contacting.util.syntaxf.FutureSyntax.FutureAsync

  override def getAllBySurveyId(surveyId: Long): F[Seq[SurveyPhoneNumberFormat]] = futRepo.getAllBySurveyId(surveyId).asDelayedF

  def addDeleteFormats(formatsAdded: Seq[SurveyPhoneNumberFormat], formatsRemoved: Seq[SurveyPhoneNumberFormat]): F[Unit] =
    futRepo.addDeleteFormats(formatsAdded, formatsRemoved).asDelayedF
}