package nl.ing.api.contacting.conf.modules

import com.ing.apisdk.toolkit.connectivity.kafka.avro.serde.{DecryptingKafkaPayloadAvroDeserializer, EncryptingKafkaPayloadAvroSerializer, EncryptionAwareSerDeConfig}
import com.ing.apisdk.toolkit.connectivity.kafka.kafkaserde.util.KafkaSerdeConfig
import com.typesafe.scalalogging.LazyLogging
import io.confluent.kafka.serializers.{AbstractKafkaSchemaSerDeConfig, KafkaAvroDeserializer}
import nl.ing.api.contacting.conf.SSLConfig
import nl.ing.api.contacting.conf.surveytrigger.{SelfServiceSurveyTriggerEventConsumer, ContactingSurveyTriggerEventConsumer}
import org.apache.avro.generic.GenericRecord
import org.apache.kafka.clients.consumer.{ConsumerConfig, KafkaConsumer}
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerConfig}
import org.apache.kafka.common.serialization.{StringDeserializer, StringSerializer}
import org.springframework.core.io.ClassPathResource

import java.util
import java.util.Properties

trait KafkaModule {
  def startContactingSurveyEventConsumer(): Unit
  def stopContactingSurveyEventConsumer(): Unit

  def startSelfServiceSurveyEventConsumer(): Unit
  def stopSelfServiceSurveyEventConsumer(): Unit

  val entityEventProducer: KafkaProducer[String, GenericRecord]
}

trait DefaultKafkaModule extends KafkaModule with LazyLogging {
  this: CoreModule =>
  var contactingSurveyTriggerEventConsumer: KafkaConsumer[String, GenericRecord] = _
  var selfServiceSurveyTriggerEventConsumer: KafkaConsumer[String, GenericRecord] = _

  lazy val entityEventProducer: KafkaProducer[String, GenericRecord] = {
    val props: Properties = new Properties()
    props.put(ProducerConfig.CLIENT_ID_CONFIG, entityEventProducerConfig.producerClientId)
    props.put(ProducerConfig.ACKS_CONFIG, entityEventProducerConfig.ackConfig)
    props.put(ProducerConfig.RETRIES_CONFIG, entityEventProducerConfig.retriesConfig)
    props.put(ProducerConfig.RETRY_BACKOFF_MS_CONFIG, entityEventProducerConfig.retryBackOff)
    props.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, entityEventProducerConfig.maxInFlight)
    props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, classOf[StringSerializer].getName)
    props.put(KafkaSerdeConfig.MAP_SCHEMA_NAME_CONFIG, entityEventProducerConfig.topic)
    props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, classOf[EncryptingKafkaPayloadAvroSerializer].getName)
    props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaConfig.bootstrapServers)
    props.put(EncryptionAwareSerDeConfig.SHARED_SECRET_CONFIG, entityEventProducerConfig.sharedSecret)
    props.put("schema.registry.url", kafkaConfig.schemaRegistryUrl)
    props.put(AbstractKafkaSchemaSerDeConfig.AUTO_REGISTER_SCHEMAS, kafkaConfig.autoRegisterSchemas.getOrElse("false"))
    props.put(AbstractKafkaSchemaSerDeConfig.USE_LATEST_VERSION, "true")
    setSslProps(props, entityEventProducerConfig.securityProtocol, kafkaConfig.sslConfig)
    new KafkaProducer[String, GenericRecord](props)
  }

  override def startContactingSurveyEventConsumer(): Unit = {
    contactingSurveyTriggerEventConsumer = createConsumer(surveyTriggerConsumerConfig)
    contactingSurveyTriggerEventConsumer.subscribe(util.Arrays.asList(surveyTriggerConsumerConfig.topic))
    new ContactingSurveyTriggerEventConsumer(contactingSurveyTriggerEventConsumer, surveyTriggerTopicProcessor).start()
  }

  override def startSelfServiceSurveyEventConsumer(): Unit = {
    selfServiceSurveyTriggerEventConsumer = createConsumer(selfServiceSurveyTriggerConsumerConfig)
    selfServiceSurveyTriggerEventConsumer.subscribe(util.Arrays.asList(selfServiceSurveyTriggerConsumerConfig.topic))
    new SelfServiceSurveyTriggerEventConsumer(selfServiceSurveyTriggerEventConsumer, surveyTriggerTopicProcessor).start()
  }

  override def stopContactingSurveyEventConsumer(): Unit = {
    logger.info("Closing kafka consumer")
    contactingSurveyTriggerEventConsumer.unsubscribe()
    contactingSurveyTriggerEventConsumer.close()
  }

  override def stopSelfServiceSurveyEventConsumer(): Unit = {
    logger.info("Closing slef service survey kafka consumer")
    selfServiceSurveyTriggerEventConsumer.unsubscribe()
    selfServiceSurveyTriggerEventConsumer.close()
  }

  private def createConsumer(consumerConfig: nl.ing.api.contacting.conf.ConsumerConfig) = {
    val props = new Properties()
    props.put(ConsumerConfig.GROUP_ID_CONFIG, consumerConfig.groupId)
    props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaConfig.bootstrapServers)
    props.put("schema.registry.url", kafkaConfig.schemaRegistryUrl)
    props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, Boolean.box(false))
    props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, classOf[StringDeserializer].getName)
    consumerConfig.useEncryption match {
      case Some(false) => props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, classOf[KafkaAvroDeserializer].getName)
      case _ => props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, classOf[DecryptingKafkaPayloadAvroDeserializer].getName)
    }
    props.put(EncryptionAwareSerDeConfig.SHARED_SECRET_CONFIG, consumerConfig.sharedSecret)
    props.put(AbstractKafkaSchemaSerDeConfig.USE_LATEST_VERSION, "true")
    props.put(AbstractKafkaSchemaSerDeConfig.AUTO_REGISTER_SCHEMAS, kafkaConfig.autoRegisterSchemas.getOrElse("false"))
    setSslProps(props, consumerConfig.securityProtocol, kafkaConfig.sslConfig)
    setAvroSslProps(props, kafkaConfig.sslConfig)
    new KafkaConsumer[String, GenericRecord](props)
  }

  private def setSslProps(props: Properties, securityProtocol: String, sslConfig: Option[SSLConfig]) = {
    props.put("security.protocol", securityProtocol)
    sslConfig.foreach { value =>
      props.put("ssl.truststore.location", new ClassPathResource(value.truststoreLocation).getFile.getAbsolutePath)
      props.put("ssl.truststore.password", value.truststorePassword)
      props.put("ssl.keystore.location", new ClassPathResource(value.keystoreLocation).getFile.getAbsolutePath)
      props.put("ssl.keystore.password", value.keystorePassword)
      props.put("ssl.key.password", value.keyPassword)
    }
  }

  private def setAvroSslProps(props: Properties, sslConfig: Option[SSLConfig]) = {
    sslConfig.foreach { value =>
      props.put("schema.registry.ssl.truststore.location", new ClassPathResource(value.truststoreLocation).getFile.getAbsolutePath)
      props.put("schema.registry.ssl.truststore.password", value.truststorePassword)
      props.put("schema.registry.ssl.keystore.location", new ClassPathResource(value.keystoreLocation).getFile.getAbsolutePath)
      props.put("schema.registry.ssl.keystore.password", value.keystorePassword)
      props.put("schema.registry.ssl.key.password", value.keyPassword)
    }
  }

}
