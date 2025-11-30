package nl.ing.api.contacting.conf.resource

import io.swagger.annotations.Api
import io.swagger.jaxrs.Reader
import io.swagger.jaxrs.config.ReaderListener
import io.swagger.models.Swagger
import io.swagger.models.parameters.HeaderParameter
import nl.ing.api.contacting.conf.resource.SwaggerListener._

import scala.collection.JavaConverters._

object SwaggerListener {
  private val TWILIO_ACCOUNT_TOKEN_HEADER_PARAM =
    new HeaderParameter().name("X-ING-Twilio-Session-Token")
      .required(false)
      .`type`("string")
      .description("Twilio Account token JWT. Read here https://confluence.ing.net/display/CC2/Account+tokens")

  private val ING_TWILIO_CJID_HEADER_PARAM =
    new HeaderParameter().name("X-ING-TWILIO-CJID")
      .required(false)
      .`type`("string")
      .description("Optional cjid for e2e tracing")
}

@Api
class SwaggerListener extends ReaderListener {
  override def afterScan(reader: Reader, swagger: Swagger): Unit = {
    val updated = swagger.getPaths.asScala.map { paths =>
      paths._2.getOperations.asScala.map {
        operation =>
          operation.addParameter(TWILIO_ACCOUNT_TOKEN_HEADER_PARAM)
          operation.addParameter(ING_TWILIO_CJID_HEADER_PARAM)
          operation
      }
      paths
    }
    swagger.setPaths(updated.asJava)
  }

  //not used
  override def beforeScan(reader: Reader, swagger: Swagger): Unit = ()
}