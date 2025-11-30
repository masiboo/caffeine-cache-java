package nl.ing.api.contacting.conf

import org.springframework.beans.factory.annotation.{Autowired, Qualifier}
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.scheduling.quartz.SchedulerFactoryBean
import org.springframework.scheduling.quartz.SpringBeanJobFactory
import org.quartz.spi.JobFactory
import org.springframework.beans.factory.config.PropertiesFactoryBean
import org.springframework.core.io.ClassPathResource

import java.util.Properties
import javax.sql.DataSource

/**
 * @author Ayush M
 */
class QuartzConfiguration {

  @Autowired
  @Qualifier("userDataSource")
  private var dataSource: DataSource = _

  @Autowired
  var ctx: ApplicationContext = _

  @Bean
  def jobFactory: JobFactory = {
    val jobFactory = new SpringBeanJobFactory()
    jobFactory.setApplicationContext(ctx)
    jobFactory
  }

  @Bean(name = Array("jdbcScheduler"))
  def schedulerFactoryBeanJDBC: SchedulerFactoryBean = {
    val schedulerFactory = new SchedulerFactoryBean
    schedulerFactory.setWaitForJobsToCompleteOnShutdown(true)
    schedulerFactory.setAutoStartup(true)
    schedulerFactory.setQuartzProperties(quartzProperties("quartz-jdbc.properties"))
    schedulerFactory.setSchedulerName("SchedulerJDBC")
    schedulerFactory.setDataSource(dataSource)
    schedulerFactory.setJobFactory(jobFactory)
    schedulerFactory
  }

  def quartzProperties(fileName: String): Properties = {
    val propertiesFactoryBean = new PropertiesFactoryBean
    propertiesFactoryBean.setLocation(new ClassPathResource(fileName))
    propertiesFactoryBean.afterPropertiesSet()
    propertiesFactoryBean.getObject
  }
}
