package nl.ing.api.contacting.conf.repository

import cats.effect.Async
import com.ing.api.contacting.dto.resource.blacklist.BlacklistFunctionality.BlacklistFunctionality
import nl.ing.api.contacting.conf.modules.ExecutionContextConfig.executionContext
import nl.ing.api.contacting.conf.repository.cslick.actions.BlacklistActions
import nl.ing.api.contacting.conf.repository.cslick.data.BlacklistItemData
import nl.ing.api.contacting.repository.cslick.{AccountBasedSlickRepo, AccountBasedSlickRepoF}
import nl.ing.api.contacting.tracing.Trace
import nl.ing.api.contacting.util.syntaxf.FutureSyntax.FutureAsync

import java.time.LocalDateTime
import scala.concurrent.Future
import scala.language.higherKinds

trait BlacklistRepository[F[_]]{
  def getAllBlacklistItemsByFunctionality(accountId: Long,
                                          now: LocalDateTime,
                                          blacklistFunctionality: BlacklistFunctionality): F[Seq[BlacklistItemData]]

  def getAllBlacklistItems(accountId: Long, now: LocalDateTime): F[Seq[BlacklistItemData]]

  def getAllSurveyBlacklistItems(accountSid: String, now: LocalDateTime): F[Seq[BlacklistItemData]]

  def cleanUpBlacklist(accountId: Long): F[Int]

  def save(entity : BlacklistItemData, context : com.ing.api.contacting.dto.context.ContactingContext) : F[BlacklistItemData]

  def update(entity : BlacklistItemData, context : com.ing.api.contacting.dto.context.ContactingContext) : F[BlacklistItemData]

  def deleteById(id : Long, context : com.ing.api.contacting.dto.context.ContactingContext) : F[scala.Int]

}

class FutureBlacklistRepository(blackListAction: BlacklistActions)
  extends AccountBasedSlickRepo[BlacklistItemData, Long, BlacklistActions](blackListAction.dbComponent) with BlacklistRepository[Future]{
  import blackListAction.dbComponent
  override val slickActions: BlacklistActions = blackListAction

  override def getAllBlacklistItemsByFunctionality(accountId: Long,
                                          now: LocalDateTime,
                                          blacklistFunctionality: BlacklistFunctionality): Future[Seq[BlacklistItemData]] =
    dbComponent.db.run(blackListAction.allForAccountAndFunctionality(accountId, now, blacklistFunctionality.toString))

  override def getAllBlacklistItems(accountId: Long, now: LocalDateTime): Future[Seq[BlacklistItemData]] = {
    dbComponent.db.run(blackListAction.allForAccount(accountId, now))
  }

  override def getAllSurveyBlacklistItems(accountSid: String, now: LocalDateTime): Future[Seq[BlacklistItemData]] = {
    dbComponent.db.run(blackListAction.allSurveyBlacklistItems(accountSid, now)).map(_.map(_._2))(executionContext)
  }

  override def cleanUpBlacklist(accountId: Long): Future[Int] = {
    dbComponent.db.run(blackListAction.cleanUpBlacklist(accountId))
  }
}


class AsyncBlacklistRepository[F[_] : Async : Trace](futRepo: FutureBlacklistRepository)
  extends AccountBasedSlickRepoF[F, BlacklistItemData, Long, BlacklistActions](futRepo) with BlacklistRepository[F] {

  def getAllBlacklistItemsByFunctionality(accountId: Long,
                                          now: LocalDateTime,
                                          blacklistFunctionality: BlacklistFunctionality): F[Seq[BlacklistItemData]] = {
    futRepo.getAllBlacklistItemsByFunctionality(accountId,now, blacklistFunctionality).asDelayedF
  }

  def getAllBlacklistItems(accountId: Long, now: LocalDateTime): F[Seq[BlacklistItemData]] = {
    futRepo.getAllBlacklistItems(accountId, now).asDelayedF
  }

  def getAllSurveyBlacklistItems(accountSid: String, now: LocalDateTime): F[Seq[BlacklistItemData]] = {
    futRepo.getAllSurveyBlacklistItems(accountSid, now).asDelayedF
  }

  def cleanUpBlacklist(accountId: Long): F[Int] = {
    futRepo.cleanUpBlacklist(accountId).asDelayedF
  }
}
