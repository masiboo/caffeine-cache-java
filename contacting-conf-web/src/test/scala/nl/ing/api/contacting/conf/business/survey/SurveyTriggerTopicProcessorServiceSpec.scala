package nl.ing.api.contacting.conf.business.survey

import cats.effect.IO
import com.ing.api.contacting.dto.resource.blacklist.{BlacklistFunctionality, BlacklistType}
import com.twitter.finagle.http.Response
import nl.ing.api.contacting.conf.BaseSpec
import nl.ing.api.contacting.conf.business.BlacklistService
import nl.ing.api.contacting.conf.domain.SurveyCallRecordVO
import nl.ing.api.contacting.conf.modules.ExecutionContextConfig.ioRunTime
import nl.ing.api.contacting.conf.repository.SurveyRepository
import nl.ing.api.contacting.conf.repository.cassandra.SurveyCallRecordsRepository
import nl.ing.api.contacting.conf.repository.cslick.data.BlacklistItemData
import nl.ing.api.contacting.conf.repository.model.SurveyPhoneNumberFormat
import nl.ing.api.contacting.conf.repository.model.SurveySetting
import nl.ing.api.contacting.conf.repository.model.SurveySettingOptions
import nl.ing.api.contacting.conf.surveytrigger.ContactingSurveyTriggerEvent
import nl.ing.api.contacting.conf.tracing.TestTrace.TestTrace
import nl.ing.api.contacting.domain.slick.AccountVO
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.scalatestplus.mockito.MockitoSugar

import java.time.{Instant, LocalDateTime}
import scala.collection.mutable.ListBuffer
import scala.language.postfixOps
import scala.concurrent.duration.DurationInt

class SurveyTriggerTopicProcessorServiceSpec extends BaseSpec  {

  val surveyPhFmt1 =  SurveyPhoneNumberFormat(None, 1, "+31*", false)
  val surveyPhFmt2 =  SurveyPhoneNumberFormat(None, 1, "+91*", false)
  val surveyPhFmt3 =  SurveyPhoneNumberFormat(None, 1, "+31", false)
  val surveyPhFmt4 =  SurveyPhoneNumberFormat(None, 1, "+91", false)
  val surveyPhFmt5 =  SurveyPhoneNumberFormat(None, 1, "+919910630", false)

  val accountVo = AccountVO(
    Some(1),
    Some("123"),
    "account-unit",
    true,
    1l,
    1l,
    "Europe/Amsterdam",
    "NL", "1079")

  var traceList = ListBuffer.empty[String]
  def addToList(str: String): ListBuffer[String] = {
    traceList += str
  }
  implicit val testTrace: TestTrace[ListBuffer[String]] = new TestTrace(addToList)


  it should "validated phone number for allowed types" in {
    val mockSurveyCallRepo = MockitoSugar.mock[SurveyCallRecordsRepository[IO]]
    val mockRepo = MockitoSugar.mock[SurveyRepository[IO]]
    val blacklistServiceMock = MockitoSugar.mock[BlacklistService[IO]]
    val randomizer = MockitoSugar.mock[Randomizer]
    val testObject = new SurveyStreamService[IO](mockSurveyCallRepo,mockRepo, randomizer, blacklistServiceMock)

    val isAllowed = testObject.isPhoneNumberEligible("+31", List(surveyPhFmt3, surveyPhFmt4), "cjid", "callSid")
    val isAllowed2 = testObject.isPhoneNumberEligible("+3134", List(surveyPhFmt3, surveyPhFmt4), "cjid", "callSid")
    val isAllowed3 = testObject.isPhoneNumberEligible("+919910630",List(surveyPhFmt5), "cjid", "callSid")
    val isAllowed4= testObject.isPhoneNumberEligible("+911234567890",List(surveyPhFmt1, surveyPhFmt2), "cjid", "callSid")
    isAllowed shouldEqual true
    isAllowed2 shouldEqual false
    isAllowed3 shouldEqual true
    isAllowed4 shouldEqual true
  }

  it should "validated phone number for not allowed types" in {
    val mockSurveyCallRepo = MockitoSugar.mock[SurveyCallRecordsRepository[IO]]
    val mockRepo = MockitoSugar.mock[SurveyRepository[IO]]
    val randomizer = MockitoSugar.mock[Randomizer]
    val blacklistServiceMock = MockitoSugar.mock[BlacklistService[IO]]
    val testObject = new SurveyStreamService[IO](mockSurveyCallRepo, mockRepo, randomizer, blacklistServiceMock)

    val isAllowed = testObject.isPhoneNumberEligible("+21 (123) 456-7890",List(surveyPhFmt1, surveyPhFmt2), "cjid", "callSid")
    val isAllowed2 = testObject.isPhoneNumberEligible("+1 (1234) 456-7890",List(surveyPhFmt1, surveyPhFmt2), "cjid", "callSid")
    isAllowed shouldEqual false
    isAllowed2 shouldEqual false
  }

  it should "Process Survey if all condition met" in {
    val mockSurveyCallRepo = MockitoSugar.mock[SurveyCallRecordsRepository[IO]]
    val mockRepo = MockitoSugar.mock[SurveyRepository[IO]]
    val randomizer = MockitoSugar.mock[Randomizer]
    val mockBlacklistService = MockitoSugar.mock[BlacklistService[IO]]
    val testObject = new SurveyStreamService[IO](mockSurveyCallRepo, mockRepo, randomizer, mockBlacklistService)

    val survey = SurveySetting(Some(11L),11,"test_survey","call","Inbound","123",Some("survey_call"),Some(3),
      Some(3),Some(100),Some(0),true)

    val callDtl1 = ContactingSurveyTriggerEvent("CJID","Inbound","taskQueueSid","workerSid","+311234567890",true,100,"taskSid",
      "100","workspaceSid","callSid","taskReason",1635522723986L)
    val phoneNumberFormat = SurveyPhoneNumberFormat(None, survey.id.getOrElse(0), "+31*", false)
    val blacklistItemData = BlacklistItemData(None,BlacklistFunctionality.ALL.toString,BlacklistType.PHONE_NUMBER.toString,"+316888888",LocalDateTime.now,None,1l)

    when(mockRepo.getIfSurveyEligibleForInbound(any())).thenReturn(IO.pure(SurveySettingOptions(Some(survey), Some(accountVo), List(phoneNumberFormat))))
    when(mockSurveyCallRepo.findCallRecordsForCustomerAndSurvey(any(), any(), any())).thenReturn(IO(List(SurveyCallRecordVO("account-unit",
      "+311234567890", "test_survey", Instant.ofEpochMilli(System.currentTimeMillis() - (4 * 24 * 60 * 60 * 1000))))))
    when(randomizer.random(any())).thenReturn(90)
    when(mockBlacklistService.getAllBlacklistItems(any[String]())).thenReturn(IO.pure(Seq(blacklistItemData)))

    val res = testObject.processIfSurveyEligible(callDtl1).unsafeRunSync()
    res.isDefined shouldEqual true
  }

  it should "return no offerable survey if customer phone number is in blocklist" in {
    val mockSurveyCallRepo = MockitoSugar.mock[SurveyCallRecordsRepository[IO]]
    val mockRepo = MockitoSugar.mock[SurveyRepository[IO]]
    val randomizer = MockitoSugar.mock[Randomizer]
    val mockBlacklistService = MockitoSugar.mock[BlacklistService[IO]]
    val testObject = new SurveyStreamService[IO](mockSurveyCallRepo, mockRepo, randomizer, mockBlacklistService)

    val callDtl1 = ContactingSurveyTriggerEvent("CJID","Inbound","taskQueueSid","workerSid","+311234567890",true,100,"taskSid",
      "100","workspaceSid","callSid","taskReason",1635522723986L)
    val blacklistItemData = BlacklistItemData(None,BlacklistFunctionality.ALL.toString,BlacklistType.PHONE_NUMBER.toString,"+311234567890",LocalDateTime.now,None,1l)

    when(mockBlacklistService.getAllBlacklistItems(any[String]())).thenReturn(IO.pure(Seq(blacklistItemData)))

    val res = testObject.processIfSurveyEligible(callDtl1).unsafeRunSync()
    res.isDefined shouldEqual false
  }

  it should "Process Survey if all condition met for outbound" in {
    val mockSurveyCallRepo = MockitoSugar.mock[SurveyCallRecordsRepository[IO]]
    val mockRepo = MockitoSugar.mock[SurveyRepository[IO]]
    val randomizer = MockitoSugar.mock[Randomizer]
    val mockBlacklistService = MockitoSugar.mock[BlacklistService[IO]]
    val testObject = new SurveyStreamService[IO](mockSurveyCallRepo, mockRepo, randomizer, mockBlacklistService)

    val survey = SurveySetting(Some(11L),11,"test_survey","call","Outbound","123",Some("survey_call"),Some(3),
      Some(3),Some(100),Some(0),true)
    val callDtl1 = ContactingSurveyTriggerEvent("CJID","Outbound","taskQueueSid","workerSid","+311234567890",false,100,"taskSid",
      "100","workspaceSid","callSid","taskReason",1635522723986L)
    val phoneNumberFormat = SurveyPhoneNumberFormat(None, survey.id.getOrElse(0), "+31*", false)
    val blacklistItemData = BlacklistItemData(None,BlacklistFunctionality.ALL.toString,BlacklistType.PHONE_NUMBER.toString,"+316888888",LocalDateTime.now,None,1l)

    when(mockBlacklistService.getAllBlacklistItems(any[String]())).thenReturn(IO.pure(Seq(blacklistItemData)))
    when(mockRepo.getIfSurveyEligibleForOutbound(any())).thenReturn(IO.pure(SurveySettingOptions(Some(survey), Some(accountVo), List(phoneNumberFormat))))
    when(mockSurveyCallRepo.findCallRecordsForCustomerAndSurvey(any(), any(), any())).thenReturn(IO(List(SurveyCallRecordVO("account-unit",
      "+311234567890", "test_survey", Instant.ofEpochMilli(System.currentTimeMillis() - (4 * 24 * 60 * 60 * 1000))))))
    when(randomizer.random(any())).thenReturn(90)

    val res = testObject.processIfSurveyEligible(callDtl1).unsafeRunSync()
    res.isDefined shouldEqual true
  }

  it should "Process Survey if all condition met it returns" in {
    val mockSurveyCallRepo = MockitoSugar.mock[SurveyCallRecordsRepository[IO]]
    val mockRepo = MockitoSugar.mock[SurveyRepository[IO]]
    val mockBlacklistService = MockitoSugar.mock[BlacklistService[IO]]
    val randomizer = MockitoSugar.mock[Randomizer]
    val testObject = new SurveyStreamService[IO](mockSurveyCallRepo, mockRepo, randomizer, mockBlacklistService)

    val survey = SurveySetting(Some(11L),11,"test_survey","call","Inbound","123",Some("survey_call"),Some(3),
      Some(3),Some(100),Some(2),true)
    val callDtl1 = ContactingSurveyTriggerEvent("CJID","Inbound","taskQueueSid","workerSid","+311234567890",true,100,"taskSid",
      "100","workspaceSid","callSid","taskReason",1635522723986L)
    val phoneNumberFormat = SurveyPhoneNumberFormat(None, survey.id.getOrElse(0), "+31*", false)
    val blacklistItemData = BlacklistItemData(None,BlacklistFunctionality.ALL.toString,BlacklistType.PHONE_NUMBER.toString,"+316888888",LocalDateTime.now,None,1l)

    when(mockBlacklistService.getAllBlacklistItems(any[String]())).thenReturn(IO.pure(Seq(blacklistItemData)))
    when(mockRepo.getIfSurveyEligibleForInbound(any())).thenReturn(IO.pure(SurveySettingOptions(Some(survey), Some(accountVo), List(phoneNumberFormat))))
    when(mockSurveyCallRepo.findCallRecordsForCustomerAndSurvey(any(), any(), any())).thenReturn(IO(List(SurveyCallRecordVO("account-unit",
      "+311234567890", "test_survey", Instant.ofEpochMilli(System.currentTimeMillis() - (4 * 24 * 60 * 60 * 1000))))))
    when(randomizer.random(any())).thenReturn(90)

    (testObject.processIfSurveyEligible(callDtl1).map(
      res =>
          res.isDefined shouldEqual true
    ) *> IO.sleep(4 seconds)).unsafeRunSync()

    testObject.processIfSurveyEligible(callDtl1).map(
      res =>
        res.isDefined shouldEqual true
    ).unsafeRunSync()
  }

  it should "Process Survey and don't check min freq if not defined" in {
    val mockSurveyCallRepo = MockitoSugar.mock[SurveyCallRecordsRepository[IO]]
    val mockRepo = MockitoSugar.mock[SurveyRepository[IO]]
    val randomizer = MockitoSugar.mock[Randomizer]
    val mockBlacklistService = MockitoSugar.mock[BlacklistService[IO]]
    val testObject = new SurveyStreamService[IO](mockSurveyCallRepo, mockRepo, randomizer, mockBlacklistService)

    val survey = SurveySetting(Some(11L),11,"test_survey","call","Outbound","123",Some("survey_call"),None,
      Some(3),Some(100),Some(0),true)
    val callDtl1 = ContactingSurveyTriggerEvent("CJID","Outbound","taskQueueSid","workerSid","+311234567890",false,100,"taskSid",
      "100","workspaceSid","callSid","taskReason",1635522723986L)
    val phoneNumberFormat = SurveyPhoneNumberFormat(None, survey.id.getOrElse(0), "+31*", false)
    val blacklistItemData = BlacklistItemData(None,BlacklistFunctionality.ALL.toString,BlacklistType.PHONE_NUMBER.toString,"+316888888",LocalDateTime.now,None,1l)

    when(mockBlacklistService.getAllBlacklistItems(any[String]())).thenReturn(IO.pure(Seq(blacklistItemData)))
    when(mockRepo.getIfSurveyEligibleForOutbound(any())).thenReturn(IO.pure(SurveySettingOptions(Some(survey), Some(accountVo), List(phoneNumberFormat))))
    when(randomizer.random(any())).thenReturn(90)

    val res = testObject.processIfSurveyEligible(callDtl1).unsafeRunSync()
    res.isDefined shouldEqual true
    verify(mockSurveyCallRepo, times(0)).findCallRecords(any(), any())
  }

  it should "process survey if min freq is defined and the last survey was older than min freq" in {
    val mockSurveyCallRepo = MockitoSugar.mock[SurveyCallRecordsRepository[IO]]
    val mockRepo = MockitoSugar.mock[SurveyRepository[IO]]
    val randomizer = MockitoSugar.mock[Randomizer]
    val mockBlacklistService = MockitoSugar.mock[BlacklistService[IO]]
    val testObject = new SurveyStreamService[IO](mockSurveyCallRepo, mockRepo, randomizer, mockBlacklistService)
    val MIN_FREQUENCY_2_DAYS_AGO = Option(2)
    val FOUR_DAYS_AGO = 4 * 24 * 60 * 60 * 1000
    val TIME_OF_CALL_29_10_2021 = 1635522723986L

    val survey = SurveySetting(Some(11L), 11, "test_survey", "call", "Outbound", "123", Some("survey_call"), MIN_FREQUENCY_2_DAYS_AGO,
      Some(3), Some(100), Some(0), true)
    val callDtl1 = ContactingSurveyTriggerEvent("CJID", "Outbound", "taskQueueSid", "workerSid", "+311234567890", false, 100, "taskSid",
      "100", "workspaceSid", "callSid", "taskReason", TIME_OF_CALL_29_10_2021)
    val phoneNumberFormat = SurveyPhoneNumberFormat(None, survey.id.getOrElse(0), "+31*", false)
    val blacklistItemData = BlacklistItemData(None, BlacklistFunctionality.ALL.toString, BlacklistType.PHONE_NUMBER.toString, "+316888888", LocalDateTime.now, None, 1l)

    when(mockBlacklistService.getAllBlacklistItems(any[String]())).thenReturn(IO.pure(Seq(blacklistItemData)))
    when(mockRepo.getIfSurveyEligibleForOutbound(any())).thenReturn(IO.pure(SurveySettingOptions(Some(survey), Some(accountVo), List(phoneNumberFormat))))
    when(randomizer.random(any())).thenReturn(90)
    when(mockSurveyCallRepo.findCallRecordsForCustomerAndSurvey(any(), any(), any())).thenReturn(IO(List(SurveyCallRecordVO("account-unit",
      "+311234567890", "test_survey", Instant.ofEpochMilli(System.currentTimeMillis() - (FOUR_DAYS_AGO))))))

    val res = testObject.processIfSurveyEligible(callDtl1).unsafeRunSync()
    res.isDefined shouldEqual true
  }

  it should "NOT process survey if min freq is defined and the last survey was more recent than min freq" in {
    val mockSurveyCallRepo = MockitoSugar.mock[SurveyCallRecordsRepository[IO]]
    val mockRepo = MockitoSugar.mock[SurveyRepository[IO]]
    val randomizer = MockitoSugar.mock[Randomizer]
    val mockBlacklistService = MockitoSugar.mock[BlacklistService[IO]]
    val testObject = new SurveyStreamService[IO](mockSurveyCallRepo, mockRepo, randomizer, mockBlacklistService)
    val MIN_FREQUENCY_7_DAYS_AGO = Option(7)
    val FOUR_DAYS_AGO = 4 * 24 * 60 * 60 * 1000
    val TIME_OF_CALL_29_10_2021 = 1635522723986L

    val survey = SurveySetting(Some(11L), 11, "test_survey", "call", "Outbound", "123", Some("survey_call"), MIN_FREQUENCY_7_DAYS_AGO,
      Some(3), Some(100), Some(0), true)
    val callDtl1 = ContactingSurveyTriggerEvent("CJID", "Outbound", "taskQueueSid", "workerSid", "+311234567890", false, 100, "taskSid",
      "100", "workspaceSid", "callSid", "taskReason", TIME_OF_CALL_29_10_2021)
    val phoneNumberFormat = SurveyPhoneNumberFormat(None, survey.id.getOrElse(0), "+31*", false)
    val blacklistItemData = BlacklistItemData(None, BlacklistFunctionality.ALL.toString, BlacklistType.PHONE_NUMBER.toString, "+316888888", LocalDateTime.now, None, 1l)

    when(mockBlacklistService.getAllBlacklistItems(any[String]())).thenReturn(IO.pure(Seq(blacklistItemData)))
    when(mockRepo.getIfSurveyEligibleForOutbound(any())).thenReturn(IO.pure(SurveySettingOptions(Some(survey), Some(accountVo), List(phoneNumberFormat))))
    when(randomizer.random(any())).thenReturn(90)
    when(mockSurveyCallRepo.findCallRecordsForCustomerAndSurvey(any(), any(), any())).thenReturn(IO(List(SurveyCallRecordVO("account-unit",
      "+311234567890", "test_survey", Instant.ofEpochMilli(System.currentTimeMillis() - (FOUR_DAYS_AGO))))))

    val res = testObject.processIfSurveyEligible(callDtl1).unsafeRunSync()
    res.isDefined shouldEqual false
  }

  it should "process survey if min freq is defined and there are no previous surveys for this customer" in {
    val mockSurveyCallRepo = MockitoSugar.mock[SurveyCallRecordsRepository[IO]]
    val mockRepo = MockitoSugar.mock[SurveyRepository[IO]]
    val randomizer = MockitoSugar.mock[Randomizer]
    val mockBlacklistService = MockitoSugar.mock[BlacklistService[IO]]
    val testObject = new SurveyStreamService[IO](mockSurveyCallRepo, mockRepo, randomizer, mockBlacklistService)
    val MIN_FREQUENCY_2_DAYS_AGO = Option(2)
    val TIME_OF_CALL_29_10_2021 = 1635522723986L

    val survey = SurveySetting(Some(11L), 11, "test_survey", "call", "Outbound", "123", Some("survey_call"), MIN_FREQUENCY_2_DAYS_AGO,
      Some(3), Some(100), Some(0), true)
    val callDtl1 = ContactingSurveyTriggerEvent("CJID", "Outbound", "taskQueueSid", "workerSid", "+311234567890", false, 100, "taskSid",
      "100", "workspaceSid", "callSid", "taskReason", TIME_OF_CALL_29_10_2021)
    val phoneNumberFormat = SurveyPhoneNumberFormat(None, survey.id.getOrElse(0), "+31*", false)
    val blacklistItemData = BlacklistItemData(None, BlacklistFunctionality.ALL.toString, BlacklistType.PHONE_NUMBER.toString, "+316888888", LocalDateTime.now, None, 1l)

    when(mockBlacklistService.getAllBlacklistItems(any[String]())).thenReturn(IO.pure(Seq(blacklistItemData)))
    when(mockRepo.getIfSurveyEligibleForOutbound(any())).thenReturn(IO.pure(SurveySettingOptions(Some(survey), Some(accountVo), List(phoneNumberFormat))))
    when(randomizer.random(any())).thenReturn(90)
    when(mockSurveyCallRepo.findCallRecordsForCustomerAndSurvey(any(), any(), any())).thenReturn(IO(List()))

    val res = testObject.processIfSurveyEligible(callDtl1).unsafeRunSync()
    res.isDefined shouldEqual true
  }

  it should "return left if callflow is not defined" in {
    val mockSurveyCallRepo = MockitoSugar.mock[SurveyCallRecordsRepository[IO]]
    val mockRepo = MockitoSugar.mock[SurveyRepository[IO]]
    val response = MockitoSugar.mock[Response]
    val randomizer = MockitoSugar.mock[Randomizer]
    val mockBlacklistService = MockitoSugar.mock[BlacklistService[IO]]
    val testObject = new SurveyStreamService[IO](mockSurveyCallRepo, mockRepo, randomizer, mockBlacklistService)

    val survey = SurveySetting(Some(11L),11,"test_survey","call","inbound","123",None,Some(3),
      Some(3),Some(100),Some(60 * 1000),true)
    val callDtl1 = ContactingSurveyTriggerEvent("CJID","Inbound","taskQueueSid","workerSid","+311234567890",true,100,"taskSid",
      "100","workspaceSid","callSid","taskReason",1635522723986L)
    val blacklistItemData = BlacklistItemData(None,BlacklistFunctionality.ALL.toString,BlacklistType.PHONE_NUMBER.toString,"+316888888",LocalDateTime.now,None,1l)

    when(mockBlacklistService.getAllBlacklistItems(any[String]())).thenReturn(IO.pure(Seq(blacklistItemData)))
    when(mockRepo.getIfSurveyEligibleForInbound(any())).thenReturn(IO.pure(SurveySettingOptions(Some(survey), Some(accountVo), List())))

    val res = testObject.processIfSurveyEligible(callDtl1).unsafeRunSync()

    res.isEmpty shouldEqual true
  }

  it should "return left if percentage is below the provided one" in {
    val mockSurveyCallRepo = MockitoSugar.mock[SurveyCallRecordsRepository[IO]]
    val mockRepo = MockitoSugar.mock[SurveyRepository[IO]]
    val response = MockitoSugar.mock[Response]
    val randomizer = MockitoSugar.mock[Randomizer]
    val mockBlacklistService = MockitoSugar.mock[BlacklistService[IO]]
    val testObject = new SurveyStreamService[IO](mockSurveyCallRepo, mockRepo, randomizer, mockBlacklistService)

    val survey = SurveySetting(Some(11L),11,"test_survey","call","inbound","123",Some("survey_call"),Some(3),
      Some(3),Some(80),Some(60 * 1000),true)

    val callDtl1 = ContactingSurveyTriggerEvent("CJID","Inbound","taskQueueSid","workerSid","+311234567890",true,100,"taskSid",
      "100","workspaceSid","callSid","taskReason",1635522723986L)
    val blacklistItemData = BlacklistItemData(None,BlacklistFunctionality.ALL.toString,BlacklistType.PHONE_NUMBER.toString,"+316888888",LocalDateTime.now,None,1l)

    when(mockBlacklistService.getAllBlacklistItems(any[String]())).thenReturn(IO.pure(Seq(blacklistItemData)))
    when(mockRepo.getIfSurveyEligibleForInbound(any())).thenReturn(IO.pure(SurveySettingOptions(Some(survey), Some(accountVo), List())))
    when(randomizer.random(any())).thenReturn(7)

    val res = testObject.processIfSurveyEligible(callDtl1).unsafeRunSync()

    res.isEmpty shouldEqual true
  }

  it should "return successful IO after inserting offered survey call" in {
    val mockSurveyCallRepo = MockitoSugar.mock[SurveyCallRecordsRepository[IO]]
    val mockRepo = MockitoSugar.mock[SurveyRepository[IO]]
    val randomizer = MockitoSugar.mock[Randomizer]
    val mockBlacklistService = MockitoSugar.mock[BlacklistService[IO]]
    val testObject = new SurveyStreamService[IO](mockSurveyCallRepo, mockRepo, randomizer, mockBlacklistService)
    val surveyOfferedCall = SurveyCallRecordVO("NL-unit", "+311234567890", "test_survey", Instant.now())
    val blacklistItemData = BlacklistItemData(None,BlacklistFunctionality.ALL.toString,BlacklistType.PHONE_NUMBER.toString,"+316888888",LocalDateTime.now,None,1l)

    when(mockBlacklistService.getAllBlacklistItems(any[String]())).thenReturn(IO.pure(Seq(blacklistItemData)))
    when(mockSurveyCallRepo.upsert(surveyOfferedCall)).thenReturn(IO.unit)
    testObject.addOfferedSurveyCallRecord(surveyOfferedCall).unsafeRunSync()

    verify(mockSurveyCallRepo, times(1)).upsert(any())
  }
}
