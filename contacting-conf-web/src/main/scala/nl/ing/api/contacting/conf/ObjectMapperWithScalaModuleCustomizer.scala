package nl.ing.api.contacting.conf

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.datatype.joda.JodaModule
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.ing.apisdk.merak.autoconfigure.jackson.MerakJackson2ObjectMapperBuilderCustomizer
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder

/**
  * @author Ayush Mittal
  * This class is to override the default serialization that Spring provides
  */
class ObjectMapperWithScalaModuleCustomizer extends MerakJackson2ObjectMapperBuilderCustomizer {
  override def customize(builder: Jackson2ObjectMapperBuilder): Unit = {
    builder
      .modulesToInstall(classOf[DefaultScalaModule], classOf[JodaModule])
      .serializationInclusion(JsonInclude.Include.NON_ABSENT)
      .featuresToDisable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
      .build()
  }
}

