package nl.ing.api.contacting.conf.domain

import nl.ing.api.contacting.conf.repository.model.SurveySetting
import nl.ing.api.contacting.conf.surveytrigger.SurveyEvent
import nl.ing.api.contacting.domain.slick.AccountVO

case class OfferableSurvey(callDetail: SurveyEvent,
                           account: AccountVO,
                           setting: SurveySetting)

