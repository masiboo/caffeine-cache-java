package nl.ing.api.contacting.conf.domain

import nl.ing.api.contacting.conf.domain.types.ContactingTypes.DatabaseId

// tq names are managed by contacting api, so string type
case class SurveyTaskQMappingVO(id: Option[DatabaseId], surveyId: DatabaseId, taskQId: DatabaseId, tqName: String)
