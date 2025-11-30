package nl.ing.api.contacting.conf.util

import com.ing.api.contacting.dto.resource.account.AccountDto
import nl.ing.twilio.domain.{TwilioAccount, TwilioCredentials}

object AccountDtoConverters {

  implicit class AccountDtoToTwilioAccountConverter(accountDto: AccountDto) {
    def asTwilioAccount: TwilioAccount = {
      TwilioAccount(
        id = accountDto.id,
        sid = accountDto.sid,
        friendlyName = accountDto.friendlyName,
        workspaceSid = accountDto.workspaceSid,
        credentialsPerRegion = accountDto.credentialsPerRegion.map(cr => {
          cr._1 -> TwilioCredentials(
            token = cr._2.token,
            privateKey = cr._2.privateKey,
            publicKeySid = cr._2.publicKeySid,
            signingKeySid = cr._2.signingKeySid,
            signingKeySecret = cr._2.signingKeySecret,
            secondaryAuthToken = cr._2.secondaryToken.orNull)
        }))
    }
  }
}

