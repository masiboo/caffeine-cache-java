package nl.ing.api.contacting.conf.repository.cassandra

import cats.effect.Async
import com.datastax.oss.driver.api.core.ConsistencyLevel
import nl.ing.api.contacting.conf.domain.SurveyCallRecordVO
import nl.ing.api.contacting.conf.modules.ExecutionContextConfig.callflowSurveyExecutionContext
import nl.ing.api.contacting.conf.repository.cassandra.quill.ContactingQuillSession
import nl.ing.api.contacting.conf.repository.cassandra.quill.QuillQueryExecutor
import nl.ing.api.contacting.tracing.Trace
import nl.ing.api.contacting.util.syntaxf.FutureSyntax.FutureAsync

import scala.concurrent.ExecutionContext
import scala.concurrent.Future

object SurveyCallRecordsRepository {
  val SURVEY_CALL_RECORDS = "SURVEY_CALL_RECORDS"
}

trait SurveyCallRecordsRepository[F[_]] {
  def findCallRecords(accountFriendlyName: String, phoneNum: String): F[List[SurveyCallRecordVO]]

  def findCallRecordsForCustomerAndSurvey(accountFriendlyName: String, phoneNum: String, surveySettingName: String): F[List[SurveyCallRecordVO]]

  def upsert(surveyCall: SurveyCallRecordVO): F[Unit]
}

class FutureSurveyCallRecordsRepository(implicit val quillWrapper: QuillQueryExecutor) extends SurveyCallRecordsRepository[Future] {

  val cl = ConsistencyLevel.LOCAL_QUORUM

  implicit val executionContext: ExecutionContext = callflowSurveyExecutionContext

  def findCallRecords(accountFriendlyName: String, phoneNum: String): Future[List[SurveyCallRecordVO]] = {
    quillWrapper(Some(cl))((context: ContactingQuillSession) => {
      import context._
      run(quote(surveyCallRecordsSchema).filter(c => c.account_friendly_name == lift(accountFriendlyName)&&
        c.phone_num == lift(phoneNum)))
    }).map(_.toList)
  }

  def findCallRecordsForCustomerAndSurvey(accountFriendlyName: String, phoneNum: String, surveySettingName: String): Future[List[SurveyCallRecordVO]] = {
    findCallRecords(accountFriendlyName, phoneNum).map{
      records =>
        records.filter(_.survey_name == surveySettingName)
    }
  }

  def upsert(surveyCall: SurveyCallRecordVO): Future[Unit] = {
    quillWrapper(Some(cl))(context => {
      import context._
      run(quote {
        surveyCallRecordsSchema.insert(lift(surveyCall))
      })
    })
  }

}

class AsyncSurveyCallRecordsRepository[F[_]: Async : Trace](futRepo: FutureSurveyCallRecordsRepository)(implicit val quillWrapper: QuillQueryExecutor) extends SurveyCallRecordsRepository[F] {

  import nl.ing.api.contacting.conf.modules.ExecutionContextConfig.executionContext
  override def findCallRecords(accountFriendlyName: String, phoneNum: String): F[List[SurveyCallRecordVO]] =
    futRepo.findCallRecords(accountFriendlyName, phoneNum).asDelayedF

  override def findCallRecordsForCustomerAndSurvey(accountFriendlyName: String, phoneNum: String, surveySettingName: String): F[List[SurveyCallRecordVO]] =
    futRepo.findCallRecordsForCustomerAndSurvey(accountFriendlyName, phoneNum, surveySettingName).asDelayedF

  override def upsert(surveyCall: SurveyCallRecordVO): F[Unit] =
    futRepo.upsert(surveyCall).asDelayedF
}
