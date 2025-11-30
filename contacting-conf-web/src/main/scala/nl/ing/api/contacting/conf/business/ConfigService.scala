package nl.ing.api.contacting.conf.business

import com.typesafe.scalalogging.LazyLogging
import nl.ing.api.contacting.conf.business.ConfigService.dateFormat
import nl.ing.api.contacting.caching.hazelcast.config.HazelcastAPICache.fromCacheableFunction
import nl.ing.api.contacting.conf.domain.ContactingConfigVO
import nl.ing.api.contacting.conf.modules.CoreModule
import nl.ing.api.contacting.conf.modules.ExecutionContextConfig.executionContext
import nl.ing.api.contacting.conf.repository.cassandra.ConfigRepository
import org.apache.commons.lang3.StringUtils

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import scala.concurrent.Future


object ConfigService {
  val ACCOUNT_EHCACHE_ENABLED = "ACCOUNT_EHCACHE_ENABLED"
  val MAINTENANCE_MODE_ORACLE_START_DATE = "MAINTENANCE_MODE_ORACLE_START_DATE"
  val MAINTENANCE_MODE_ORACLE_END_DATE = "MAINTENANCE_MODE_ORACLE_END_DATE"

  val dateFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")
}

/**
 * Created by le26rc on 11-12-2017.
 * Updated by M. Hoekstra
 *
 * Service for managing system config based on key/value(s). For example we store the list of business functions and the list of readonly business functions like this.
 */
class ConfigService(configRepository: ConfigRepository[Future], systemModule: CoreModule) extends LazyLogging {
  /**
   * gets all the config items
   *
   * @return
   */
  def fetchConfigs(): Future[Seq[ContactingConfigVO]] = configRepository.findAll()

  /**
   * find the config items by the given key
   *
   * @param key the name of the config item
   * @return a Set of values based on the given item
   */
  def findByKey(key: String): Future[Set[String]] = {
    configRepository.findAll()
      .map(_.filter(_.key == key)
             .flatMap(_.valuesAsSet()))
      .map(_.toSet)
  }

  def isCacheEnabled: Future[Boolean] =
    configRepository.findByKey(ConfigService.ACCOUNT_EHCACHE_ENABLED) map {
      optConfig =>
        optConfig.exists(_.booleanValueWithFalseDefault)
    }


  def isOracleUp(): Future[Boolean] = {
    fromCacheableFunction("oracle-up", StringUtils.EMPTY) { _ =>
      configRepository.findByKeys(Seq(ConfigService.MAINTENANCE_MODE_ORACLE_START_DATE, ConfigService.MAINTENANCE_MODE_ORACLE_END_DATE)) map {
        optConfigs =>
          checkOracleStatus(optConfigs, systemModule.now)
      }
    }
  }

  private def checkOracleStatus(optConfigs: Seq[ContactingConfigVO], now: LocalDateTime): Boolean = {
    val start = optConfigs.find(_.key.equals(ConfigService.MAINTENANCE_MODE_ORACLE_START_DATE))
      .map(s => LocalDateTime.parse(s.values, dateFormat)).getOrElse(LocalDateTime.MAX)
    val end = optConfigs.find(_.key.equals(ConfigService.MAINTENANCE_MODE_ORACLE_END_DATE))
      .map(s => LocalDateTime.parse(s.values, dateFormat)).getOrElse(LocalDateTime.MIN)
    now.isBefore(start) || now.isAfter(end)
  }

  def isOracleDown: Future[Boolean] = isOracleUp().map(!_)

  /**
   * update the full contacting config table
   * @param config the new config
   * @return
   */
  def update(config: Seq[ContactingConfigVO]) : Future[Unit] = {
    configRepository.update(config)
  }
}
