package nl.ing.api.contacting.conf.repository

import cats.effect.Async
import nl.ing.api.contacting.conf.repository.SurveyOrgRepository.SurveyOrgDetails
import nl.ing.api.contacting.conf.repository.cslick.actions.SurveyOrgAction
import nl.ing.api.contacting.conf.repository.model.SurveyOrgMapping
import nl.ing.api.contacting.repository.model.OrganisationModel
import nl.ing.api.contacting.tracing.Trace

import scala.concurrent.Future

/**
 * @author Ayush Mittal
 */

trait SurveyOrgRepository[F[_]] {
  def getAllBySurveyId(surveyId: Long): F[Seq[SurveyOrgDetails]]

  def addRemoveOrgs(surveyId: Long, orgsToAdd: List[Long], orgsToRemove: List[Long]): F[List[Any]]
}

object SurveyOrgRepository {
  type SurveyOrgDetails = (((SurveyOrgMapping, OrganisationModel), Option[OrganisationModel]), Option[OrganisationModel])
}


class FutureSurveyOrgRepository(surveyOrgAction: SurveyOrgAction) extends SurveyOrgRepository[Future] {

  import surveyOrgAction.dBComponent.db

  override def getAllBySurveyId(surveyId: Long): Future[Seq[SurveyOrgDetails]] =
    db.run(surveyOrgAction.findBySurveyId(surveyId))


  def addRemoveOrgs(surveyId: Long, orgsToAdd: List[Long], orgsToRemove: List[Long]): Future[List[Any]] =
    db.run(surveyOrgAction.addRemoveOrgs(surveyId, orgsToAdd, orgsToRemove))

}

class AsyncSurveyOrgRepository[F[_]: Async : Trace](futRepo: FutureSurveyOrgRepository) extends SurveyOrgRepository[F]{

  import nl.ing.api.contacting.conf.modules.ExecutionContextConfig.executionContext
  import nl.ing.api.contacting.util.syntaxf.FutureSyntax.FutureAsync

  override def getAllBySurveyId(surveyId: Long): F[Seq[SurveyOrgDetails]] = futRepo.getAllBySurveyId(surveyId).asDelayedF

  override def addRemoveOrgs(surveyId: Long, orgsToAdd: List[Long], orgsToRemove: List[Long]): F[List[Any]] =
    futRepo.addRemoveOrgs(surveyId, orgsToAdd, orgsToRemove).asDelayedF
}
