package nl.ing.api.contacting.conf.business

import com.typesafe.scalalogging.LazyLogging
import nl.ing.api.contacting.business.CacheConfigProvider
import nl.ing.api.contacting.conf.modules.ExecutionContextConfig.executionContext
import scala.concurrent.Future

class DefaultCacheConfigProvider(configService: ConfigService) extends CacheConfigProvider[Future] with LazyLogging {

  override def isReadEnabled: Future[Boolean] = {
    configService.isOracleDown.recover{
      case throwable: Throwable =>
        logger.warn(s"unable to fetch oracle maintenance flag from oracle, defaulting to false", throwable)
        false
    }
  }

  override def isWriteEnabled: Future[Boolean] = Future.successful(true)
}