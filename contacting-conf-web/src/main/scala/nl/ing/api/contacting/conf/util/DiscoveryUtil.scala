package nl.ing.api.contacting.conf.util

import com.typesafe.scalalogging.LazyLogging
import nl.ing.api.contacting.util.Json
import org.apache.http.client.methods.HttpGet
import org.apache.http.conn.ssl.NoopHostnameVerifier
import org.apache.http.conn.ssl.TrustAllStrategy
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClients
import org.apache.http.ssl.SSLContextBuilder
import org.apache.http.util.EntityUtils

import scala.util.Try

case class RootInterface(
                          services: Seq[Services],
                          lastModified: Int
                        )

case class Services(
                     name: String,
                     versions: Seq[String],
                     versionInstances: Seq[VersionInstances]
                   )

case class VersionInstances(
                             version: String,
                             instances: Seq[Instances]
                           )

case class Instances(
                      host: String,
                      port: Int,
                      datacenter: String,
                      confidence: Int,
                      heartbeatInterval: Double,
                      operationalMode: String
                    )

object DiscoveryUtil extends LazyLogging {
  private def createHttpClient(): CloseableHttpClient = HttpClients
    .custom()
    .setSSLContext(new SSLContextBuilder().loadTrustMaterial(null, TrustAllStrategy.INSTANCE).build())
    .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)
    .build();

  def hosts(serviceDiscoveryURL: Array[String]): Set[String] = {
    serviceDiscoveryURL.flatMap {
      url =>
        logger.info(s"Attempt to query discovery with $url")
        val instances: Option[RootInterface] = Try(Json.as[RootInterface](EntityUtils.toString(createHttpClient().execute(new HttpGet(s"https://$url/services?serviceNames=ContactingConfigurationAPI")).getEntity))).toOption
        instances match {
          case Some(res) =>
            val liveInstances: Seq[String] = res.services.flatMap(_.versionInstances.flatMap(_.instances.filter(_.operationalMode == "live").map(_.host + ":5701")))
            logger.info(s"Fetched live instances of ContactingConfigurationAPI: $liveInstances")
            liveInstances
          case None =>
            logger.info(s"Failed to query discovery with $url. ")
            Seq()
        }
    }
  }.toSet

}
