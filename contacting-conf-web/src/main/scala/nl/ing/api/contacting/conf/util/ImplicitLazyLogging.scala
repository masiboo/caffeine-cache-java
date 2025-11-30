package nl.ing.api.contacting.conf.util

import com.typesafe.scalalogging.LazyLogging
import com.typesafe.scalalogging.Logger

trait ImplicitLazyLogging extends LazyLogging {

  implicit val lazyLogging: Logger = logger
}
