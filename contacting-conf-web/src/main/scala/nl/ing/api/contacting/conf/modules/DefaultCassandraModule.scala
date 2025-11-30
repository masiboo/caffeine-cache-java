package nl.ing.api.contacting.conf.modules

import com.datastax.oss.driver.api.core.ConsistencyLevel
import com.datastax.oss.driver.api.core.CqlSession
import com.datastax.oss.driver.api.core.config.DefaultDriverOption
import com.datastax.oss.driver.api.core.config.DriverConfigLoader
import com.ing.apisdk.toolkit.core.util.TlsUtil
import com.ing.apisdk.toolkit.keystores.KeyStores
import com.typesafe.scalalogging.LazyLogging
import nl.ing.api.contacting.util.StringDecrypt

import javax.net.ssl.SSLContext
import scala.util.Try

trait CassandraModule {
  def session(consistencyLevel: ConsistencyLevel = ConsistencyLevel.LOCAL_QUORUM): CqlSession
}

trait DefaultCassandraModule extends CassandraModule with LazyLogging {
  this:CoreModule =>

  override def session(consistencyLevel: ConsistencyLevel = ConsistencyLevel.LOCAL_QUORUM): CqlSession = {
    val username = config.getString("cassandra-config.username")
    val password = StringDecrypt.decrypt(config.getString("cassandra-config.password"))

    // Since it's not possible to use "profiles" on query level (via quill) we build our own config per CL
    val loader: DriverConfigLoader =
      DriverConfigLoader.programmaticBuilder()
        .withString(DefaultDriverOption.REQUEST_CONSISTENCY, consistencyLevel.name())
        .build()

    val builder = CqlSession.builder()
      .withAuthCredentials(username, password)
      .withMetricRegistry(metricRegistry)
      .withConfigLoader(loader)

    if(Try(config.getBoolean("cassandra-config.enable-ssl")).getOrElse(true)){
      val trust = TlsUtil.createTrustManagerFactory(KeyStores.loadKeyStore("classpath:cassandra-trust.jks","JKS", env.getProperty("service.manifest.trust-store.password")))
      val sslContext = SSLContext.getInstance("TLS")
      sslContext.init(null, trust.getTrustManagers, null)
      builder.withSslContext(sslContext).build() // get sslcontext provided by merak
    } else {
      builder.build()
    }
  }
}
