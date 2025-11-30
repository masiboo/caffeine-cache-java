package nl.ing.api.contacting.conf.resource.dto

import nl.ing.api.contacting.conf.BaseSpec
import nl.ing.api.contacting.conf.support.TestData

class SurveyUpdateDTOSpec extends BaseSpec with TestData {

  it should "fail validation for too long phone format types" in {
    val addFormat = SurveyPhoneNumberFormatUpdateDTO(None, "+316878888888888988888", 1)
    val surveyUpdateDTO = SurveyUpdateDTO(surveySettingDTO, Seq(addFormat), Seq())

    val result = SurveyUpdateDTO.validatePhFormat(surveyUpdateDTO)
    result.isRight shouldEqual false
  }

  it should "fail validation for invalid character at end phone format types" in {
    val removeUpdate = SurveyPhoneNumberFormatUpdateDTO(None, "+919910%", 1)
    val surveyUpdateDTO = SurveyUpdateDTO(surveySettingDTO, Seq(), Seq(removeUpdate))

    val result = SurveyUpdateDTO.validatePhFormat(surveyUpdateDTO)
    result.isRight shouldEqual false
  }

}
