package nl.ing.api.contacting.conf.mapper

import nl.ing.api.contacting.conf.BaseSpec
import nl.ing.api.contacting.conf.resource.dto.SurveySettingDTO
import nl.ing.api.contacting.conf.support.TestData
import nl.ing.api.contacting.conf.util.RefinedUtils._

class SurveyMapperSpec extends BaseSpec with TestData {

  it should "convert vo to dto" in {
      SurveyMapper.surveySettingVOToSettingDto(surveySettingVO) shouldBe surveySettingDTO
  }

  it should "convert dto to vo" in {
    SurveyMapper.surveySettingDTOToVo(surveySettingDTO, 1L) shouldBe Right(surveySettingVO)
  }

  it should "html escape the XSS injected data in the dto when converting to vo" in {
    SurveyMapper.surveySettingDTOToVo(surveySettingDtoWithXSSInjection, 1L) shouldBe Right(surveySettingVOHtmlEscaped)
  }

  it should "convert model to vo" in {
    SurveyMapper.surveySettingModelToVo(surveySettingModel) shouldBe Right(surveySettingVO)
  }

  it should "process underscores" in {
    SurveyMapper.surveySettingModelToVo(surveySettingModel.copy(name = "abc_")) shouldBe Right(surveySettingVO.copy(name="abc_".toFriendlyName.toOption.get))
  }

  it should "convert vo to model" in {
    SurveyMapper.surveySettingVoToModel(surveySettingVO) shouldBe surveySettingModel
  }
}
