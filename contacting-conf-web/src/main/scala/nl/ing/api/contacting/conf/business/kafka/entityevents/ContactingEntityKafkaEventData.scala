package nl.ing.api.contacting.conf.business.kafka.entityevents

import com.ing.api.contacting.dto.context.ContactingContext
import nl.ing.api.contacting.conf.entityevents.ContactingEntityKafkaEvent
import nl.ing.api.contacting.conf.entityevents.DELETED
import nl.ing.api.contacting.conf.entityevents.ORGANISATION
import nl.ing.api.contacting.domain.slick.OrganisationVO

trait ContactingEntityKafkaEventData[T] {
  def eventDeleteData(t: T, contactingContext: ContactingContext): ContactingEntityKafkaEvent
}


object ContactingEntityKafkaEventData {
  implicit val organisationData: ContactingEntityKafkaEventData[OrganisationVO] = new ContactingEntityKafkaEventData[OrganisationVO] {
    override def eventDeleteData(t: OrganisationVO, contactingContext: ContactingContext): ContactingEntityKafkaEvent =
      ContactingEntityKafkaEvent(ORGANISATION,DELETED, t.id.getOrElse(-1L), t.name, contactingContext.accountId, Some(contactingContext.auditContext.modifiedBy))
  }
}

object ContactingEntityKafkaEventDataSyntax {
  implicit class ContactingEntityKafkaEventDataOps[A](value: A) {
    def toEventDeleteData(contactingContext: ContactingContext)(implicit contactingEntityKafkaEventData: ContactingEntityKafkaEventData[A]): ContactingEntityKafkaEvent = {
      contactingEntityKafkaEventData.eventDeleteData(value, contactingContext)
    }
  }
}
