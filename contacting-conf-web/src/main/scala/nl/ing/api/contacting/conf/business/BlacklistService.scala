package nl.ing.api.contacting.conf.business

import cats.effect.{Async, IO}
import com.ing.api.contacting.dto.context.ContactingContext
import com.ing.api.contacting.dto.resource.account.AccountDto
import com.ing.api.contacting.dto.resource.blacklist.BlacklistFunctionality
import com.ing.api.contacting.dto.resource.blacklist.BlacklistFunctionality.BlacklistFunctionality
import nl.ing.api.contacting.conf.repository.BlacklistRepository
import nl.ing.api.contacting.conf.repository.cslick.data.BlacklistItemData
import nl.ing.api.contacting.conf.util.ImplicitLazyLogging
import nl.ing.api.contacting.tracing.Trace

import java.time.LocalDateTime

/**
  * BlacklistService
  *
  * Provides some functionality to handle the basic CRUD operations on the blacklist.
  */
class BlacklistService[F[_]: Async : Trace](blacklistRepository: BlacklistRepository[F])
    extends ImplicitLazyLogging {

  def getAllBlacklistItemsByFunctionality(
      blacklistFunctionality: BlacklistFunctionality,
      context: ContactingContext): F[Seq[BlacklistItemData]] =
    blacklistFunctionality match {
      case BlacklistFunctionality.ALL => this.getAllBlacklistItems(context)
      case _ =>
        blacklistRepository
          .getAllBlacklistItemsByFunctionality(context.accountId,
                                               LocalDateTime.now,
                                               blacklistFunctionality)
    }

  def getAllBlacklistItems(
      context: ContactingContext): F[Seq[BlacklistItemData]] = {
    blacklistRepository
      .getAllBlacklistItems(context.accountId, LocalDateTime.now)
  }

  def getAllBlacklistItems(accountSid: String): F[Seq[BlacklistItemData]] = {
    blacklistRepository
      .getAllSurveyBlacklistItems(accountSid, LocalDateTime.now)
  }

  def cleanUpBlacklist(implicit account: AccountDto): F[Int] = {
    blacklistRepository.cleanUpBlacklist(account.id)
  }

  def createBlackListItem(entity: BlacklistItemData,
                          context: ContactingContext): F[BlacklistItemData] = {
    blacklistRepository
      .save(entity, context)
  }

  def updateBlackListItem(entity: BlacklistItemData,
                          context: ContactingContext): F[BlacklistItemData] = {
    blacklistRepository
      .update(entity, context)
  }

  def deleteBlackListItem(id: Long, context: ContactingContext): F[Int] = {
    blacklistRepository
      .deleteById(id, context)
  }
}
