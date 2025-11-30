package nl.ing.api.contacting.conf.support

import com.ing.api.contacting.dto.context.ContactingContext
import com.ing.api.contacting.dto.context.SlickAuditContext
import com.ing.api.contacting.dto.resource.account.AccountDto

/**
 * Created on 15/08/2020 at 23:42
 *
 * @author bo55nk
 */
trait AccountSupport {

  implicit def account: AccountDto = AccountDto(
    id = 1L,
    sid = "123",
    friendlyName = "account-unit",
    workspaceSid = "test workspace sid",
    timezone = "Europe/Amsterdam",
    workspaceId = 1,
    legalEntity = "legalEntity",
    credentialsPerRegion = Map.empty,
    productServicesPerRegion = Map.empty
  )

  def context =
    ContactingContext(account.id, SlickAuditContext("modifiedBy", None, Some(account.id), None))

}
