package nl.ing.api.contacting.conf.modules

import javax.annotation.PostConstruct
import javax.annotation.PreDestroy

/**
  * This spring bean will bootstrap kafka. This needs to be postponed unitil both Spring and Scala DI are done.
  * Reason behind this is that the kafka consumer needs some Spring bean handlers to process the messages, we need to be
  * sure that this starts after the system is fully operational.
  *
  * @param kafkaModule
  */
class KafkaBootstrapper(kafkaModule: KafkaModule) {

  @PostConstruct
  def start() = {
    if (kafkaModule.asInstanceOf[CoreModule].kafkaDatalakeEnabled) {
      kafkaModule.startContactingSurveyEventConsumer()
      kafkaModule.startSelfServiceSurveyEventConsumer()
    }
  }

  @PreDestroy
  def closeProducer(): Unit = {
    if (kafkaModule.asInstanceOf[CoreModule].kafkaDatalakeEnabled){
      kafkaModule.stopContactingSurveyEventConsumer()
      kafkaModule.stopSelfServiceSurveyEventConsumer()
    }
  }

}
