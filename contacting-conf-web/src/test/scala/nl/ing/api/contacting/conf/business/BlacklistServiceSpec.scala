package nl.ing.api.contacting.conf.business

import cats.effect.IO
import com.ing.api.contacting.dto.context.{ContactingContext, SlickAuditContext}
import com.ing.api.contacting.dto.resource.blacklist.{BlacklistFunctionality, BlacklistType}
import nl.ing.api.contacting.conf.BaseSpec
import nl.ing.api.contacting.conf.domain.BlacklistItemVO
import nl.ing.api.contacting.conf.repository.BlacklistRepository
import nl.ing.api.contacting.conf.repository.cslick.data.BlacklistItemData
import nl.ing.api.contacting.conf.support.TestData
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito
import org.mockito.Mockito._
import org.scalatest.EitherValues
import nl.ing.api.contacting.conf.modules.ExecutionContextConfig.ioRunTime
import java.time.{ZoneOffset, ZonedDateTime}

class BlacklistServiceSpec extends BaseSpec with EitherValues with TestData {
  val now = ZonedDateTime.now(ZoneOffset.UTC).toLocalDateTime
  def testContext =
    ContactingContext(account.id, SlickAuditContext("modifiedBy", None, Some(account.id), None))

  val blacklistItems = Seq(
    BlacklistItemData(Option(11),
                      BlacklistFunctionality.ALL.toString,
                      BlacklistType.IP.toString,
                      "127.0.0.1",
                      now,
                      None,
                      account.id),
    BlacklistItemData(Option(12),
                      BlacklistFunctionality.CALL_ME_NOW.toString,
                      BlacklistType.PHONE_NUMBER.toString,
                      "+31600000000",
                      now,
                      None,
                      account.id),
    BlacklistItemData(Option(13),
                      BlacklistFunctionality.ALL.toString,
                      BlacklistType.IP.toString,
                      "127.0.0.2",
                      now,
                      None,
                      account.id)
  )

  val blacklistItem = BlacklistItemVO(Option(11),
                                      BlacklistFunctionality.ALL,
                                      BlacklistType.IP,
                                      "127.0.0.1",
                                      now,
                                      None)

  implicit val blacklistRepository: BlacklistRepository[IO] = Mockito.mock(classOf[BlacklistRepository[IO]])
  val testObject = new BlacklistService(blacklistRepository)

  it should "be able to query by functionality with ALL" in {
    when(blacklistRepository.getAllBlacklistItems(any[Long](), any()))
      .thenReturn(IO.pure(blacklistItems))

    val result = testObject
      .getAllBlacklistItemsByFunctionality(BlacklistFunctionality.ALL, testContext)
      .attempt
      .unsafeRunSync
    result.isRight shouldBe true
    result.right.value.size shouldBe 3
  }

  it should "be able to query by functionality with CallMeNow" in {
    when(
      blacklistRepository.getAllBlacklistItemsByFunctionality(
        any(),
        any(),
        same(BlacklistFunctionality.CALL_ME_NOW)))
      .thenReturn(IO.pure(Seq(blacklistItems(1))))

    val result = testObject
      .getAllBlacklistItemsByFunctionality(BlacklistFunctionality.CALL_ME_NOW,
        testContext)
      .attempt
      .unsafeRunSync
    result.isRight shouldBe true
    result.right.value shouldBe Seq(blacklistItems(1))
  }

  it should "be able to return all blacklist items" in {
    when(blacklistRepository.getAllBlacklistItems(any[Long](), any()))
      .thenReturn(IO.pure(blacklistItems))

    val result = testObject.getAllBlacklistItems(testContext).attempt.unsafeRunSync
    result.isRight shouldBe true
    result.right.value.size shouldBe 3
  }

  it should "be able to return all blacklist items by sid" in {
    when(blacklistRepository.getAllSurveyBlacklistItems(any[String](), any()))
      .thenReturn(IO.pure(blacklistItems))

    val result = testObject.getAllBlacklistItems("123").attempt.unsafeRunSync
    result.isRight shouldBe true
    result.right.value.size shouldBe 3
  }

  it should "clean up blacklist" in {
    when(blacklistRepository.cleanUpBlacklist(1))
      .thenReturn(IO.pure(1))

    val result = testObject.cleanUpBlacklist(account).attempt.unsafeRunSync
    result.isRight shouldBe true
    result.right.value shouldBe 1
  }
}
