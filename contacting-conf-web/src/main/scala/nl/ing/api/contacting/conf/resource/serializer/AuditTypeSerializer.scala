package nl.ing.api.contacting.conf.resource.serializer

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import com.ing.api.contacting.dto.audit.AuditType

class AuditTypeSerializer extends JsonSerializer[AuditType] {
  override def serialize(t: AuditType, jsonGenerator: JsonGenerator, serializerProvider: SerializerProvider): Unit =
    jsonGenerator.writeString(t.name)
}
