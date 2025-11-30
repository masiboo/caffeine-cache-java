package nl.ing.api.contacting.conf.support

import nl.ing.api.contacting.conf.modules.CoreModule
import org.mockito.Mockito

/**
  * @author Ayush Mittal
  */
trait CoreModuleMockingSupport {

  implicit lazy val coreModule: CoreModule =  Mockito.mock(classOf[CoreModule], Mockito.RETURNS_DEEP_STUBS)
}
