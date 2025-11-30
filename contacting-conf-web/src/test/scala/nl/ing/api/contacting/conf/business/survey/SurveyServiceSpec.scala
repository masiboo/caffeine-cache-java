package nl.ing.api.contacting.conf.business.survey

import cats.data.EitherT
import cats.effect.IO
import cats.implicits._
import com.ing.api.contacting.dto.context.ContactingContext
import eu.timepit.refined.api.Refined
import eu.timepit.refined.auto._
import nl.ing.api.contacting.conf.BaseSpec
import nl.ing.api.contacting.conf.domain.SurveyPhoneNumberFormatVO
import nl.ing.api.contacting.conf.domain.SurveySettingVO
import nl.ing.api.contacting.conf.domain.SurveyUpdateVO
import nl.ing.api.contacting.conf.repository.SurveyRepository
import nl.ing.api.contacting.conf.repository.cassandra.{AsyncSurveyCallRecordsRepository, FutureSurveyCallRecordsRepository, SurveyCallRecordsRepository}
import nl.ing.api.contacting.tracing.Trace
import nl.ing.api.contacting.util.ResultF
import nl.ing.api.contacting.util.exception.ContactingBusinessError
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito
import org.mockito.Mockito._
import org.scalatestplus.mockito.MockitoSugar
import nl.ing.api.contacting.conf.modules.ExecutionContextConfig.ioRunTime
class SurveyServiceSpec extends BaseSpec  {
  val surveyRepository = MockitoSugar.mock[SurveyRepository[IO]]
  val asyncSurveyCallRecordsRepository = MockitoSugar.mock[SurveyCallRecordsRepository[IO]]
  val context = ContactingContext(11, mock[com.ing.api.contacting.dto.context.SlickAuditContext])
  val surveyService = new SurveyService[IO](surveyRepository, asyncSurveyCallRecordsRepository)

  it should "pass validation for valid phone format types" in {
    val addFormat1 = SurveyPhoneNumberFormatVO(Some(11L), 11L, "+9*", "allowed")
    val addFormat2 = SurveyPhoneNumberFormatVO(Some(11L), 11L, "+919910*", "allowed")
    val removeFormat1 = SurveyPhoneNumberFormatVO(Some(11L), 11L, "+31", "allowed")
    val removeFormat2 = SurveyPhoneNumberFormatVO(Some(11L), 11L, "+316878*", "allowed")
    val surveySettingVo = SurveySettingVO(Some(11L), 11L, Refined.unsafeApply("survey setting"),
      "call","in","voiceSurveyId",None,None,None,None,None,false)
    val surveyUpdateVo = SurveyUpdateVO(surveySettingVo, Seq(addFormat1, addFormat2), Seq(removeFormat1, removeFormat2))
    val updateRes: ResultF[IO, Unit]= EitherT(IO.pure(().asRight[ContactingBusinessError]))
    when(surveyRepository.updateSurveySettings(any(), any(), any(), any())).thenReturn(updateRes)

    val result = surveyService.updateSurveySettings(surveyUpdateVo, context).isRight.unsafeRunSync()
    result shouldEqual true
  }

}
