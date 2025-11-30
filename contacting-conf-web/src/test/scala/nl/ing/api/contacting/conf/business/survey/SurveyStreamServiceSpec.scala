package nl.ing.api.contacting.conf.business.survey

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import nl.ing.api.contacting.conf.BaseSpec
import nl.ing.api.contacting.conf.business.BlacklistService
import nl.ing.api.contacting.conf.repository.SurveyRepository
import nl.ing.api.contacting.conf.repository.cassandra.SurveyCallRecordsRepository
import nl.ing.api.contacting.conf.repository.cslick.data.BlacklistItemData
import nl.ing.api.contacting.conf.repository.model.{SurveyPhoneNumberFormat, SurveySetting, SurveySettingOptions}
import nl.ing.api.contacting.conf.surveytrigger.{ContactingSurveyTriggerEvent, SelfServiceSurveyTriggerEvent}
import nl.ing.api.contacting.domain.slick.AccountVO
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar

import java.time.LocalDateTime

class SurveyStreamServiceSpec extends BaseSpec {

  val selfServiceSurveyEvent = SelfServiceSurveyTriggerEvent("cjid", "+31121212121", 160, "accountSid", "eventId", "callSid", 123L, "testSurveyName")
  val outboundCallSurveyEvent = ContactingSurveyTriggerEvent("cjid", "outbound", "TQSid1234", "WKSID1234", "+31121212121", false, 160, "taskSid", "accountSid", "eventId", "callSid", "taskEnded", 123L)
  val surveyRepository = MockitoSugar.mock[SurveyRepository[IO]]
  val surveyCallRecordsRepository = MockitoSugar.mock[SurveyCallRecordsRepository[IO]]
  val randomizer = MockitoSugar.mock[Randomizer]
  val blacklistService = MockitoSugar.mock[BlacklistService[IO]]
  val surveyStreamService = new SurveyStreamService[IO](surveyCallRecordsRepository, surveyRepository, randomizer, blacklistService)

  it should "process the outbound call survey event and it should return a setting for event" in {
    val surveySetting = SurveySetting(Some(1L), 1L, "surveyForOutbound", "call", "outbound", "voiceId", Some("callflow"), None, None, Some(100.0F), Some(10L), false)
    val accountVO = AccountVO(Some(1), Some("123"), "account-unit", true, 1L, 1L, "Europe/Amsterdam", "NL", "1079")
    val phNumberFormatForSurvey = List(SurveyPhoneNumberFormat(Some(1L), -1L, "+31*", false))
    val surveySettingsOption = SurveySettingOptions(Some(surveySetting), Some(accountVO), phNumberFormatForSurvey)
    when(blacklistService.getAllBlacklistItems(selfServiceSurveyEvent.accountSid))
      .thenReturn(IO.pure(List(BlacklistItemData(Some(1L), "functionality", "entityType", "", LocalDateTime.now, None, 1L))))
    when(surveyRepository.getIfSurveyEligibleForOutbound(outboundCallSurveyEvent))
      .thenReturn(IO.pure(surveySettingsOption))
    val result = surveyStreamService.processIfSurveyEligible(outboundCallSurveyEvent)
    val survey = result.unsafeRunSync().get
    assert(survey.setting.name === "surveyForOutbound")
  }

  it should "process the self service survey event and it should return a setting for event" in {
    val surveySetting = SurveySetting(Some(1L), 1L, "testSurveyName", "call", "outbound", "voiceId", Some("callflow"), None, None, Some(100.0F), Some(10L), false)
    val accountVO = AccountVO(Some(1), Some("123"), "account-unit", true, 1L, 1L, "Europe/Amsterdam", "NL", "1079")
    val phNumberFormatForSurvey = List(SurveyPhoneNumberFormat(Some(1L), -1L, "+31*", false))
    val surveySettingsOption = SurveySettingOptions(Some(surveySetting), Some(accountVO), phNumberFormatForSurvey)
    when(blacklistService.getAllBlacklistItems(selfServiceSurveyEvent.accountSid))
      .thenReturn(IO.pure(List(BlacklistItemData(Some(1L), "functionality", "entityType", "", LocalDateTime.now, None, 1L))))
    when(surveyRepository.getSurveySettingsByName(selfServiceSurveyEvent.surveyName, selfServiceSurveyEvent.accountSid))
      .thenReturn(IO.pure(surveySettingsOption))
    val result = surveyStreamService.processIfSurveyEligible(selfServiceSurveyEvent)
    val survey = result.unsafeRunSync().get
    assert(survey.setting.name === "testSurveyName")
  }

  it should "process the self service survey event and it should not return a setting as the phone number is not valid" in {
    val surveySetting = SurveySetting(Some(1L), 1L, "testSurveyName", "call", "outbound", "voiceId", Some("callflow"), None, None, Some(100.0F), Some(10L), false)
    val accountVO = AccountVO(Some(1), Some("123"), "account-unit", true, 1L, 1L, "Europe/Amsterdam", "NL", "1079")
    val phNumberFormatForSurvey = List(SurveyPhoneNumberFormat(Some(1L), -1L, "+61*", false))
    val surveySettingsOption = SurveySettingOptions(Some(surveySetting), Some(accountVO), phNumberFormatForSurvey)
    when(blacklistService.getAllBlacklistItems(selfServiceSurveyEvent.accountSid))
      .thenReturn(IO.pure(List(BlacklistItemData(Some(1L), "functionality", "entityType", "", LocalDateTime.now, None, 1L))))
    when(surveyRepository.getSurveySettingsByName(selfServiceSurveyEvent.surveyName, selfServiceSurveyEvent.accountSid))
      .thenReturn(IO.pure(surveySettingsOption))
    val result = surveyStreamService.processIfSurveyEligible(selfServiceSurveyEvent)
    val survey = result.unsafeRunSync()
    assert(survey.isEmpty === true)
  }
}
