package nl.ing.api.contacting.conf.resource.dto

import nl.ing.api.contacting.conf.BaseSpec
import nl.ing.api.contacting.conf.domain.SurveyConstants
import nl.ing.api.contacting.conf.support.TestData

class SurveySettingDTOSpec extends BaseSpec with TestData {
  import SurveyConstants._
  it should "pass if channel is call and testflow exists" in {
    val result = SurveySettingDTO.validateCallflowForChannel(surveySettingDTO.copy(channel = CHANNEL_CALL, callflowName = Some("test_callflow")))
    result.isRight shouldEqual true
  }

  it should "fail if channel is call but no callflow exists" in {
    val result = SurveySettingDTO.validateCallflowForChannel(surveySettingDTO.copy(channel = CHANNEL_CALL, callflowName = None))
    result.isLeft shouldEqual true
  }

  it should "pass if channel is not call no matter what callflow is" in {
    val result = SurveySettingDTO.validateCallflowForChannel(surveySettingDTO.copy(channel = "not_call", callflowName = None))
    val result2 = SurveySettingDTO.validateCallflowForChannel(surveySettingDTO.copy(channel = "not_call", callflowName = Some("test_callflow")))
    result.isRight shouldEqual true
    result2.isRight shouldEqual true
  }

  it should "pass if name length less than 256 characters" in {
    val result = SurveySettingDTO.validateName(surveySettingDTO.copy(name = "goodName"))
    result.isRight shouldEqual true
  }

  it should "fail if name length greater than 255 characters" in {
    val nameLongerThan255Chars = (1 to 256).map(_ => "X").mkString
    val result = SurveySettingDTO.validateName(surveySettingDTO.copy(name = nameLongerThan255Chars))
    result.isLeft shouldEqual true
  }

  it should "fail if name is null" in {
    val result = SurveySettingDTO.validateName(surveySettingDTO.copy(name = null))
    result.isLeft shouldEqual true
  }

}
