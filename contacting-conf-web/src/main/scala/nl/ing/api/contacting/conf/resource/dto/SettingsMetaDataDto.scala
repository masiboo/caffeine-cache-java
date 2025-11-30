package nl.ing.api.contacting.conf.resource.dto

import com.fasterxml.jackson.module.scala.JsonScalaEnumeration
import nl.ing.api.contacting.conf.repository.model.InputType.InputType
import nl.ing.api.contacting.conf.repository.model.InputTypeEnum

case class SettingsMetaDataDto(name: String,
                               @JsonScalaEnumeration(classOf[InputTypeEnum]) inputType: InputType,
                               regex: Option[String],
                               options: Seq[SettingsOptionsDto],
                               capability: Seq[String],
                               consumers: Seq[String])

case class SettingsOptionsDto(value: String,
                              displayName: String)

case class SettingsMetaDataDtos(data: Seq[SettingsMetaDataDto])