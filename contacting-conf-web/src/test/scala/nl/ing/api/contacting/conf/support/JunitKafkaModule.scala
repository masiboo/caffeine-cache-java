package nl.ing.api.contacting.conf.support

import nl.ing.api.contacting.conf.modules.{CoreModule, KafkaModule}
import org.apache.avro.generic.GenericRecord
import org.apache.kafka.clients.producer.KafkaProducer


trait JunitKafkaModule extends KafkaModule {
  this: CoreModule =>
  override def stopContactingSurveyEventConsumer(): Unit = {}

  override def stopSelfServiceSurveyEventConsumer(): Unit = {}

  override def startContactingSurveyEventConsumer(): Unit = {}

  override def startSelfServiceSurveyEventConsumer(): Unit = {}

  override val entityEventProducer: KafkaProducer[String, GenericRecord] = null
}
