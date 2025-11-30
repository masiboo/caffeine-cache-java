package nl.ing.api.contacting.conf.domain

case class SurveyDetailsVO(surveySettingVO: SurveySettingVO, phNumFormats: Seq[SurveyPhoneNumberFormatVO], taskQMappingVO: Seq[SurveyTaskQMappingVO],
                           orgMappingVO: Seq[SurveyOrgMappingVO])
