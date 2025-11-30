package nl.ing.api.contacting.conf.repository.model

import com.fasterxml.jackson.core.`type`.TypeReference
import nl.ing.api.contacting.conf.repository.model.InputType.InputType

/**
 * @author Dinesh Chandra
 */
class InputTypeEnum extends TypeReference[InputType.type]

object InputType extends Enumeration {
  type InputType = Value
  val RADIO: InputType = Value("RADIO")
  val DROPDOWN: InputType = Value("DROPDOWN")
  val TEXTBOX: InputType = Value("TEXTBOX")

  def isValidatable(value: Value): Boolean = value match {
    case RADIO | DROPDOWN => true
    case _ => false
  }
}


case class SettingsMetaDataModel(id: Option[Long],
                                 name: String,
                                 inputType: InputType,
                                 regex: Option[String],
                                 capability: Option[String],
                                 consumers: Option[String])

case class SettingsOptionsModel(id: Option[Long],
                                settingsMetaId: Long,
                                value: String,
                                displayName: String)
