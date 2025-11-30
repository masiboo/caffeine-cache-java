package nl.ing.api.contacting.conf

import ch.qos.logback.access.common.PatternLayout
import ch.qos.logback.access.tomcat.LogbackValve
import com.datastax.oss.driver.api.core.CqlSession
import com.hazelcast.config._
import com.hazelcast.core.{Hazelcast, HazelcastInstance}
import com.ing.api.contacting.configuration.CC2MetricsAutoConfiguration
import com.twilio.Twilio
import io.micrometer.core.instrument.Clock
import io.micrometer.core.instrument.observation.DefaultMeterObservationHandler
import io.micrometer.observation.ObservationRegistry
import io.micrometer.prometheusmetrics.{PrometheusConfig, PrometheusMeterRegistry}
import io.prometheus.metrics.model.registry.PrometheusRegistry
import liquibase.integration.spring.SpringLiquibase
import net.ttddyy.dsproxy.listener.QueryExecutionListener
import net.ttddyy.dsproxy.support.ProxyDataSourceBuilder
import net.ttddyy.observation.tracing.DataSourceObservationListener
import nl.ing.api.contacting.conf.business.twilio.DefaultTwilioAccountResolver
import nl.ing.api.contacting.conf.logging.TraceIdConverter
import nl.ing.api.contacting.conf.modules._
import nl.ing.api.contacting.conf.util.DiscoveryUtil
import nl.ing.api.contacting.conf.web.provider.ToBeCompliantWithRidiculousTPAStandardsInterceptor
import nl.ing.api.contacting.prometheus.PrometheusBootstrapper
import nl.ing.api.contacting.repository.ContactingDataSourceFactory
import nl.ing.api.contacting.trust.rest.SessionContextProvider
import nl.ing.api.contacting.trust.rest.feature.permissions.PermissionsDynamicFeature
import nl.ing.api.contacting.trust.rest.filter.CC2AutoConfiguration
import nl.ing.tomcat.utils.jdbc.DataSourceFactory
import org.glassfish.jersey.server.ResourceConfig
import org.jasypt.encryption.StringEncryptor
import org.springframework.beans.factory.annotation.{Autowired, Value}
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory
import org.springframework.boot.web.server.WebServerFactoryCustomizer
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.{Bean, Import, Profile, PropertySource, Configuration => SpringConfiguration}
import org.springframework.core.env.Environment

import java.util.concurrent.TimeUnit
import java.util.function.Consumer
import javax.annotation.PostConstruct
import javax.sql.DataSource
import scala.jdk.CollectionConverters._

/**
 * @author Ayush Mittal
 */

@SpringConfiguration
@Import(Array(classOf[QuartzConfiguration], classOf[CC2AutoConfiguration], classOf[CC2MetricsAutoConfiguration]))
@PropertySource(Array("classpath:contacting-service-client-defaults.properties"))
class CoreConfiguration {

  @Autowired
  var ctx: ApplicationContext = _

  @Autowired
  protected var env: Environment = _

  @Value("database.properties")
  @Autowired
  private var propertyFileName: String = _

  @Autowired
  var stringEncryptor: StringEncryptor = _

  @Bean
  @Profile(Array("tst", "prd", "acc"))
  protected def coreModule: CoreModule = {
    new CoreModule with DefaultContactingClient with DefaultSlickModule with DefaultQuillModule with DefaultCassandraModule with DefaultKafkaModule {
      override implicit def springContext: ApplicationContext = ctx
    }
  }

  @Bean
  @Profile(Array("dev", "itest"))
  protected def stubCoreModule: CoreModule = {
    new CoreModule with LocalContactingClient with DefaultSlickModule with DefaultQuillModule with DefaultCassandraModule with DefaultKafkaModule {
      override implicit def springContext: ApplicationContext = ctx
    }
  }

  @Bean
  protected def kafkaBootstrapper(systemModule: CoreModule) = {
    new KafkaBootstrapper(systemModule)
  }


  @Bean
  def observationRegistry(promRegistry: PrometheusRegistry) = {
    val observationRegistry = ObservationRegistry.create
    observationRegistry.observationConfig.observationHandler(new DefaultMeterObservationHandler(new PrometheusMeterRegistry(PrometheusConfig.DEFAULT, promRegistry, Clock.SYSTEM)))
    observationRegistry
  }

  @Bean(name = Array("userDataSource"))
  protected def userDataSource: DataSource = {
    // Create a DataSource proxy with the observation listener
    val dataSource: DataSource = new ContactingDataSourceFactory(propertyFileName, Some(stringEncryptor))
      .create("param/datasource/default",
        DataSourceFactory.IsolationLevel.READ_COMMITTED,
        DataSourceFactory.Commit.AUTO,
        DataSourceFactory.Access.READ_WRITE)
    val listener: DataSourceObservationListener = new DataSourceObservationListener(ctx.getBean(classOf[ObservationRegistry]));
    ProxyDataSourceBuilder.create(dataSource).logSlowQueryBySlf4j(200, TimeUnit.MILLISECONDS).listener(QueryExecutionListener.DEFAULT).listener(listener).methodListener(listener).build
  }


  @Bean
  def myObjectMapperCustomizer: Jackson2ObjectMapperBuilderCustomizer = {
    new ObjectMapperWithScalaModuleCustomizer()
  }

  @Bean
  protected def customJerseyConfig: Consumer[ResourceConfig] = resourceConfig => {
    resourceConfig.register(classOf[PermissionsDynamicFeature])
    resourceConfig.register(classOf[SessionContextProvider])
    resourceConfig.register(classOf[ToBeCompliantWithRidiculousTPAStandardsInterceptor])
  }

  @Bean
  @Profile(Array("itest"))
  def liquibase: SpringLiquibase = {
    val liquibase = new SpringLiquibase()
    liquibase.setDataSource(userDataSource)
    liquibase.setShouldRun(env.getProperty("spring.liquibase.enabled", classOf[Boolean], true))
    liquibase.setChangeLog(env.getProperty("spring.liquibase.change-log"))
    liquibase
  }

  @Bean
  def twilioAccountResolver(coreModule: CoreModule) = new DefaultTwilioAccountResolver()(coreModule.contactingAPIClient)

  @Bean
  def cqlSession(coreModule: CoreModule): CqlSession = coreModule.contactingCassandraContext.session

  @Bean
  def cassandraBoot(registry: PrometheusMeterRegistry, systemModule: CoreModule): DBBootStrapper = new DBBootStrapper(registry, systemModule)

  @Bean
  def prometheusBootstrapper(registry: PrometheusMeterRegistry) = new PrometheusBootstrapper(registry)

  @Bean
  @ConditionalOnProperty(name = Array("kafka.access.log.enabled"), havingValue = "true")
  def addLogbackValve(): WebServerFactoryCustomizer[TomcatServletWebServerFactory] = {
    val logbackValve = new LogbackValve()
    logbackValve.setFilename("logback-access.xml")
    logbackValve.setAsyncSupported(true)

    //Default converter for accessLog
    PatternLayout.ACCESS_DEFAULT_CONVERTER_SUPPLIER_MAP.put("traceId", () => new TraceIdConverter());

    (factory) => factory.addContextValves(logbackValve)
  }

  @Bean
  def hazelcastInstance(): HazelcastInstance = {
    val serviceDiscoveryURL = env.getRequiredProperty("servicediscovery.seed-nodes").split(",")
    val config = new Config()
    config.setInstanceName("coco-hazelcast5")
    config.setProperty("hazelcast.heartbeat.failuredetector.type", "phi-accrual")
    config.setProperty("hazelcast.heartbeat.interval.seconds", "1")
    config.setProperty("hazelcast.max.no.heartbeat.seconds", "10")
    val nc = new NetworkConfig()
    nc.setRestApiConfig(new RestApiConfig().setEnabled(true).enableAllGroups())
    nc.addOutboundPort(0)
    nc.setPort(5701)
    nc.setReuseAddress(true)
    nc.setPublicAddress(env.getRequiredProperty("service.instance.host"))
    val j = new JoinConfig
    val tcp = new TcpIpConfig
    if (!env.getActiveProfiles.contains("itest"))
      tcp.setMembers(DiscoveryUtil.hosts(serviceDiscoveryURL).toList.asJava)
    tcp.setEnabled(true)
    tcp.setConnectionTimeoutSeconds(15)
    j.setTcpIpConfig(tcp)
    nc.setJoin(j)
    config.setNetworkConfig(nc)
    Hazelcast.newHazelcastInstance(config)
  }


  @PostConstruct
  def setTwilioExecutor(): Unit = {
    Twilio setExecutorService ExecutionContextConfig.listeningExecutorService
  }
}
