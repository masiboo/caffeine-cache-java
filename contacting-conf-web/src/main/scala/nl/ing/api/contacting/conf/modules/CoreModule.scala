package nl.ing.api.contacting.conf.modules

import com.ing.apisdk.merak.autoconfigure.core.{ApplicationProperties, ServiceProperties}
import com.ing.apisdk.toolkit.logging.audit.slf4j.Slf4jAuditLogger
import com.typesafe.config.{Config, ConfigFactory}
import nl.ing.api.contacting.conf.{ConsumerConfig, KakfaConfig, ProducerConfig}
import nl.ing.api.contacting.util.StringDecrypt
import org.apache.commons.validator.routines.{RegexValidator, UrlValidator}
import org.jasypt.encryption.StringEncryptor
import org.springframework.context.ApplicationContext
import org.springframework.core.env.Environment
import pureconfig.ConfigSource
import pureconfig.generic.auto._

import java.time.{LocalDateTime, ZoneId}

/**
  * @author Ayush Mittal
  */

trait CoreModule extends SlickModule with ContactingClient with ConfigModule with BusinessModule with KafkaModule with DefaultQuillModule with CassandraModule {
  implicit def springContext: ApplicationContext

  private lazy val applicationProps = springContext.getBean(classOf[ApplicationProperties])
  private lazy val applicationId: String = applicationProps.getIngIdentifier.orElse("-")
  private lazy val serviceProps: ServiceProperties = springContext.getBean(classOf[ServiceProperties])
  protected lazy val auditLogger: Slf4jAuditLogger = new Slf4jAuditLogger(new com.ing.apisdk.toolkit.logging.audit.api.AuditContext(applicationId, serviceProps.getInstance().getHost))
  lazy val env: Environment = springContext.getBean(classOf[Environment])

  lazy val kafkaDatalakeEnabled = env.getRequiredProperty("kafka.datalake.enabled", classOf[Boolean])
}


trait ConfigModule {
  implicit def springContext: ApplicationContext
  implicit lazy val stringEncryptor: StringEncryptor = springContext.getBean(classOf[StringEncryptor])
  lazy val config: Config = ConfigFactory.load("application.conf")
  lazy val cacheConfig: Config = ConfigFactory.load("cache.conf")
  private lazy val configSource: ConfigSource = ConfigSource.fromConfig(config)
  lazy val appConfig: Config = config.getConfig("confapi")
  lazy val urlValidator: UrlValidator = new UrlValidator(Array("wss", "https", "http"))
  lazy val urlValidatorForConnectionSetting: UrlValidator = new UrlValidator(Array("wss", "https", "http"), new RegexValidator(".*"), UrlValidator.ALLOW_LOCAL_URLS)
  private lazy val kafkaConfigEncrypted: KakfaConfig = configSource.at("kakfa-config").loadOrThrow[KakfaConfig]
  private lazy val kafkaSSL = kafkaConfigEncrypted.sslConfig match {
    case None => None
    case Some(ssl) => Some(ssl.copy(truststorePassword = StringDecrypt.decrypt(ssl.truststorePassword),
      keystorePassword = StringDecrypt.decrypt(ssl.keystorePassword),
      keyPassword = StringDecrypt.decrypt(ssl.keyPassword)
    ))
  }
  lazy val kafkaConfig: KakfaConfig = kafkaConfigEncrypted.copy(sslConfig = kafkaSSL)
  private lazy val entityEventProducerConfigEncrypted: ProducerConfig = configSource.at("entity-events.producer-config").loadOrThrow[ProducerConfig]
  lazy val entityEventProducerConfig: ProducerConfig = entityEventProducerConfigEncrypted.copy(sharedSecret = StringDecrypt.decrypt(entityEventProducerConfigEncrypted.sharedSecret))

  private lazy val surveyTriggerConsumerConfigEncrypted: ConsumerConfig = configSource.at("survey-trigger.consumer-config").loadOrThrow[ConsumerConfig]
  lazy val surveyTriggerConsumerConfig: ConsumerConfig = surveyTriggerConsumerConfigEncrypted.copy(sharedSecret = StringDecrypt.decrypt(surveyTriggerConsumerConfigEncrypted.sharedSecret))

  lazy val selfServiceSurveyTriggerConsumerConfigWithoutSecret: ConsumerConfig = configSource.at("self-service-survey-trigger.consumer-config").loadOrThrow[ConsumerConfig]
  lazy val selfServiceSurveyTriggerConsumerConfig: ConsumerConfig = selfServiceSurveyTriggerConsumerConfigWithoutSecret.copy(sharedSecret = StringDecrypt.decrypt(selfServiceSurveyTriggerConsumerConfigWithoutSecret.sharedSecret))
  def now: LocalDateTime = LocalDateTime.now(ZoneId.of("Europe/Amsterdam"))
}
