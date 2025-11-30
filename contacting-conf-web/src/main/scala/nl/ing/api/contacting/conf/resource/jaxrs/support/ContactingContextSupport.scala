package nl.ing.api.contacting.conf.resource.jaxrs.support

import com.ing.api.contacting.dto.context.ContactingContext
import nl.ing.api.contacting.conf.business.context.ContactingContextProvider._

/**
  * @author Ayush Mittal
  * Provides ContactingContext
  */

trait ContactingContextSupport extends AccountSupport {

  def contactingContext: ContactingContext =
    getContactingContext(account, getSessionContext)
}
