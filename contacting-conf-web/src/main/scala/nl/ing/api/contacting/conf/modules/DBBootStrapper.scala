package nl.ing.api.contacting.conf.modules

import com.ing.apisdk.merak.autoconfigure.core.ApplicationEventOrder
import com.typesafe.scalalogging.LazyLogging
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry
import nl.ing.api.contacting.prometheus.cassandra.CassandraSampleBuilder
import nl.ing.api.contacting.prometheus.dropwizard.DropwizardExports
import org.springframework.context.ApplicationListener
import org.springframework.context.event.ContextRefreshedEvent

import javax.annotation.{PostConstruct, PreDestroy}
import scala.concurrent.Future

class DBBootStrapper(prometheusMeterRegistry: PrometheusMeterRegistry, coreModule: CoreModule) extends ApplicationListener[ContextRefreshedEvent] with org.springframework.core.Ordered with LazyLogging {

  override def onApplicationEvent(e: ContextRefreshedEvent): Unit = {
    Future{
      logger.info("started bootstrapping cassandra")
      coreModule.initCassandra()
      logger.info("bootstrapping cassandra finished")
    }(ExecutionContextConfig.ioExecutionContext)
  }

  override def getOrder: Int = ApplicationEventOrder.MERAK_LAST_LISTENER

  @PreDestroy
  def closeProducer(): Unit = {
    coreModule.hazelcastInstance.shutdown()
  }

  @PostConstruct
  def registerCassandraMetrics(): Unit = {
    prometheusMeterRegistry.getPrometheusRegistry.register(DropwizardExports(coreModule.metricRegistry,
      new CassandraSampleBuilder(List.empty, coreModule.config.getString("datastax-java-driver.basic.session-name"))))
  }

}
