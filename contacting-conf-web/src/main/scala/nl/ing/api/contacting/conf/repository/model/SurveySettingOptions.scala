package nl.ing.api.contacting.conf.repository.model

import nl.ing.api.contacting.domain.slick.AccountVO

case class SurveySettingOptions(
                               setting: Option[SurveySetting],
                               account: Option[AccountVO],
                               phFormats: List[SurveyPhoneNumberFormat]
                               )
