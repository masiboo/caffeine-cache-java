package nl.ing.api.contacting.conf.resource.serializer

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.ing.api.contacting.dto.audit.AuditType

class AuditTypeDeserializer extends JsonDeserializer[AuditType] {
  override def deserialize(jsonParser: JsonParser, deserializationContext: DeserializationContext): AuditType = {
    AuditType.withName(jsonParser.getValueAsString)
  }
}
