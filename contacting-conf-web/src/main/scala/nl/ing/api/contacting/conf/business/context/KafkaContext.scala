package nl.ing.api.contacting.conf.business.context

import nl.ing.api.contacting.conf.activeconnections.ActiveConnections
import org.apache.kafka.clients.producer.KafkaProducer

case class KafkaContext(kafkaProducer: KafkaProducer[String, ActiveConnections], topic: String)
