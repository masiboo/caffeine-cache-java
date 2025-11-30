package nl.ing.api.contacting.conf.domain

/**
 *
 * @param settings
 * @param formatsAdded
 * @param formatsRemoved
 */
case class SurveyUpdateVO(settings: SurveySettingVO, formatsAdded: Seq[SurveyPhoneNumberFormatVO], formatsRemoved: Seq[SurveyPhoneNumberFormatVO])
