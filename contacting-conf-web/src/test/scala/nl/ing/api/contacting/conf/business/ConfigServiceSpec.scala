package nl.ing.api.contacting.conf.business

import nl.ing.api.contacting.conf.BaseSpec
import nl.ing.api.contacting.caching.hazelcast.config.HazelcastAPICache.clearCache
import nl.ing.api.contacting.conf.domain.ContactingConfigVO
import nl.ing.api.contacting.conf.modules.CoreModule
import nl.ing.api.contacting.conf.repository.cassandra.{ConfigRepository, FutureConfigRepository}
import org.mockito.Mockito
import org.mockito.Mockito._

import java.time.{LocalDateTime, ZoneId}
import scala.concurrent.Future

class ConfigServiceSpec extends BaseSpec {
  val configRepository = mock[FutureConfigRepository]
  val systemModuleMock = mock[CoreModule]
  private val testObject = new ConfigService(configRepository, systemModuleMock)

  "configs" should "be found" in {
    val configs = Seq(ContactingConfigVO("BUSINESS_FUNCTIONS_HIDDEN", "system tooling"))


    when(configRepository.findAll()).thenReturn(Future.successful(configs))
    whenReady(testObject.fetchConfigs()) {
      result => result shouldBe configs
    }
  }

  it should "be found by key" in {
    val configs = Seq(ContactingConfigVO("BUSINESS_FUNCTIONS_HIDDEN", "system tooling"), ContactingConfigVO("ROLES", "ADMIN"))
    when(configRepository.findAll()).thenReturn(Future.successful(configs))
    whenReady(testObject.findByKey("BUSINESS_FUNCTIONS_HIDDEN")) {
      result => result shouldBe Set("system tooling")
    }
  }

  it should "return false if cache key is not found" in {
    when(configRepository.findByKey(ConfigService.ACCOUNT_EHCACHE_ENABLED)).thenReturn(Future.successful(None))
    whenReady(testObject.isCacheEnabled) {
      result => result shouldBe false
    }
  }


  it should "return false if cache key's value is false" in {
    val config: Option[ContactingConfigVO] = Some(ContactingConfigVO("ACCOUNT_EHCACHE_ENABLED", "false"))
    when(configRepository.findByKey(ConfigService.ACCOUNT_EHCACHE_ENABLED)).thenReturn(Future.successful(config))
    whenReady(testObject.isCacheEnabled) {
      result => result shouldBe false
    }
  }

  it should "return from cache if the cassandra is not available" in {
    val configs = Seq(ContactingConfigVO("BUSINESS_FUNCTIONS_HIDDEN", "system tooling"), ContactingConfigVO("ROLES", "ADMIN"))
    when(configRepository.findByKeys(Seq(ConfigService.MAINTENANCE_MODE_ORACLE_START_DATE, ConfigService.MAINTENANCE_MODE_ORACLE_END_DATE)))
      .thenReturn(Future.successful(configs))
    when(systemModuleMock.now).thenReturn(LocalDateTime.now(ZoneId.of("Europe/Amsterdam")).minusHours(3))
    whenReady(testObject.isOracleUp) {
      result =>
        result shouldBe true
    }
    Mockito.reset()
    when(configRepository.findByKeys(Seq(ConfigService.MAINTENANCE_MODE_ORACLE_START_DATE, ConfigService.MAINTENANCE_MODE_ORACLE_END_DATE)))
      .thenReturn(Future.failed(new Exception("Cassandra failed due to some testing reason")))
    whenReady(testObject.isOracleUp) {
      result =>
        result shouldBe true
    }

  }

  it should "return future failure if cassandra is down and cache is empty" in {
    clearCache("oracle-up")
    Mockito.reset()
    when(configRepository.findByKeys(Seq(ConfigService.MAINTENANCE_MODE_ORACLE_START_DATE, ConfigService.MAINTENANCE_MODE_ORACLE_END_DATE)))
      .thenReturn(Future.failed(new Exception("Cassandra failed due to some testing reason")))
    whenReady(testObject.isOracleUp.failed) {
      result =>
        result shouldBe a [Exception]
    }
  }

}
