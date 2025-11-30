package nl.ing.api.contacting.conf.domain

import nl.ing.api.contacting.conf.domain.types.ContactingTypes.DatabaseId
import nl.ing.api.contacting.conf.domain.types.ContactingTypes.SurveyPhNumDirection

case class SurveyPhoneNumberFormatVO(id: Option[DatabaseId], surveyId: DatabaseId, format: String, direction: SurveyPhNumDirection)
