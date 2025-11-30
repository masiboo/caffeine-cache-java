package nl.ing.api.contacting.conf.repository.cassandra

import com.datastax.oss.driver.api.core.{ConsistencyLevel, CqlSession}
import nl.ing.api.contacting.conf.domain.SurveyCallRecordVO
import nl.ing.api.contacting.conf.repository.cslick.CoCoDBComponent
import nl.ing.api.contacting.conf.support.{SystemModuleMockingSupport, TestModule}
import nl.ing.api.contacting.shared.client.ContactingAPIClient
import nl.ing.api.contacting.test.cassandra.ContactingCassandraSpec
import org.scalatest.BeforeAndAfter
import org.scalatestplus.junit.JUnitRunner
import org.springframework.context.ApplicationContext

import java.util.Date

@org.junit.runner.RunWith(value = classOf[JUnitRunner])
class SurveyCallRecordsRepositorySpec extends ContactingCassandraSpec[TestModule] with SystemModuleMockingSupport with BeforeAndAfter {

  override val timeout = 500000L

  override lazy val keySpaceName: String = "contacting"

  override val module = new TestModule {
    implicit override val springContext: ApplicationContext = mock[ApplicationContext]
    override val contactingAPIClient: ContactingAPIClient = mock[ContactingAPIClient]
    override val dBComponent: CoCoDBComponent = mock[CoCoDBComponent]

    override def session(consistencyLevel: ConsistencyLevel): CqlSession = cassandraUnit.session
  }

  override val recreateDatabase = true

  val data = Some("cassandra/survey_call_records.cql")


  "Survey Calls Records" should " be inserted" in {
      whenReady(module.futSurveyCallRecordsRepo.upsert(SurveyCallRecordVO("NL", "+31687855550", "testsurvey1", new Date().toInstant))) {
        _ =>
          whenReady(module.futSurveyCallRecordsRepo.findCallRecords("NL", "+31687855550")) {
            result =>
              result.head.survey_name shouldBe "testsurvey1"
              result.head.account_friendly_name shouldBe "NL"
          }
      }
    }

  "All Survey Calls Records" should " be found" in {
    whenReady(module.futSurveyCallRecordsRepo.findCallRecords("NL", "+31687855555")) {
      result =>
        result.size shouldBe 2
        result.head.survey_name shouldBe "nlsurvey2"
        result.head.account_friendly_name shouldBe "NL"
    }
  }

}
