package nl.ing.api.contacting.conf.business.twilio

import com.typesafe.scalalogging.StrictLogging
import nl.ing.api.contacting.conf.exception.AccountNotFoundException
import nl.ing.api.contacting.conf.util.AccountDtoConverters.AccountDtoToTwilioAccountConverter
import nl.ing.api.contacting.shared.client.ContactingAPIClient
import com.ing.api.contacting.dto.resource.account.AccountDto
import nl.ing.twilio.business.account.TwilioAccountResolver
import nl.ing.twilio.domain.TwilioAccount

import scala.concurrent.Await
import scala.concurrent.duration._

/**
  * @author G.J. Compagner
  */
class DefaultTwilioAccountResolver(implicit contactingAPIClient: ContactingAPIClient) extends TwilioAccountResolver with StrictLogging {

  /**
    * This function should not be used anymore, use contacting-trust helper instead
    *
    * @return
    */
  override def getCurrentAccount: TwilioAccount = {
    throw new IllegalStateException("getCurrentAccount is nog longer available")
  }

  override def getAccount(accountSid: String): TwilioAccount = {
    getAccountDto(accountSid).getOrElse(
      getAccountDto(accountSid).getOrElse(throw AccountNotFoundException(accountSid))
    ).asTwilioAccount
  }

  def getAccountDto(accountId: String): Option[AccountDto] = {
    try {
      Await.result(contactingAPIClient.getAccounts(), 30.seconds).find(_.sid == accountId)
    } catch {
      case t : Throwable =>
        logger.error(s"Error getting accountDto by accountId: $accountId", t)
        throw t
    }
  }
}
