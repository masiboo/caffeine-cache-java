package nl.ing.api.contacting.conf.repository

import cats.effect.IO
import com.ing.api.contacting.dto.context.{ContactingContext, SlickAuditContext}
import com.ing.api.contacting.dto.resource.blacklist.{BlacklistFunctionality, BlacklistType}
import nl.ing.api.contacting.conf.repository.cslick.actions.BlacklistActions
import nl.ing.api.contacting.conf.repository.cslick.data.BlacklistItemData
import nl.ing.api.contacting.repository.cslick.actions.AccountAction
import org.scalatest.{BeforeAndAfterEach, EitherValues}
import org.scalatestplus.junit.JUnitRunner
import nl.ing.api.contacting.util.syntaxf.FutureSyntax.FutureAsync
import nl.ing.api.contacting.conf.modules.ExecutionContextConfig.executionContext
import nl.ing.api.contacting.conf.modules.ExecutionContextConfig.ioRunTime
import java.time.LocalDateTime

@org.junit.runner.RunWith(value = classOf[JUnitRunner])
class BlacklistRepositorySpec
    extends SlickBaseSpec
    with BeforeAndAfterEach
    with EitherValues {
  val accountAction = new AccountAction(h2DBComponent)
  val action = new BlacklistActions(h2DBComponent, accountAction)
  val futObject = new FutureBlacklistRepository(action)
  val testObject = new AsyncBlacklistRepository[IO](futObject)

  val context1 =
    ContactingContext(1l, SlickAuditContext("modifiedBy", None, Some(1l), None))

  override def beforeAll: Unit = {
    super.beforeAll()
    whenReady(h2DBComponent.db.run(accountAction.createSchema)){_ => ()}
    whenReady(h2DBComponent.db.run(action.createSchema)){_ => ()}
  }
  override def afterAll: Unit = {
    super.afterAll()
    whenReady(h2DBComponent.db.run(action.deleteSchema)){_ => ()}
    whenReady(h2DBComponent.db.run(accountAction.deleteSchema)){_ => ()}
  }

  it should "be able to insert a blacklist item" in {
    val blacklistItemData =
      BlacklistItemData(None,
                        BlacklistFunctionality.ALL.toString,
                        BlacklistType.IP.toString,
                        "127.0.0.1",
                        LocalDateTime.now,
                        None,
                        1l)

    val result = (for{
      _ <- createAccount(accountAction,1L).asDelayedF[IO]
      blcks<- testObject.save(blacklistItemData, context1).attempt
    } yield blcks).unsafeRunSync()

    result.isRight shouldBe true
    result.right.value.copy(id = None) shouldBe blacklistItemData
  }

  it should "be able to update a blacklist item" in {
    val blacklistItemData =
      BlacklistItemData(None,
                        BlacklistFunctionality.ALL.toString,
                        BlacklistType.PHONE_NUMBER.toString,
                        "+316888888",
                        LocalDateTime.now,
                        None,
                        1l)

    val res =
      for {
        save <- testObject.save(blacklistItemData, context1)
        update <- testObject.update(save.copy(value = "127.0.0.55"), context1)
      } yield update

    val result = res.attempt.unsafeRunSync()

    result.isRight shouldBe true
    result.right.value.value shouldBe "127.0.0.55"
  }

  it should "be able to get all blacklist items by account id" in {
    val result = testObject
      .getAllBlacklistItems(1l, LocalDateTime.now)
      .attempt
      .unsafeRunSync
    result.isRight shouldBe true
    result.right.value.size shouldBe 2
  }

  it should "be able to get all blacklist items by account sid" in {
    val result = (for{
      _ <- createAccount(accountAction,1L).asDelayedF[IO]
      blckItems <- testObject
        .getAllSurveyBlacklistItems("123", LocalDateTime.now)
        .attempt
    } yield blckItems).unsafeRunSync()
    result.isRight shouldBe true
    result.right.value.size shouldBe 1
  }

  it should "be able to get all blacklist items by account id and valid end time" in {
    val blacklistItemData =
      BlacklistItemData(None,
                        BlacklistFunctionality.ALL.toString,
                        BlacklistType.IP.toString,
                        "127.0.0.1",
                        LocalDateTime.now,
                        Option(LocalDateTime.now.plusMinutes(10)),
                        1l)

    val res = for {
      _ <- testObject.save(blacklistItemData, context1)
      getAll <- testObject.getAllBlacklistItems(1l, LocalDateTime.now)
    } yield getAll

    val result = res.attempt.unsafeRunSync
    result.isRight shouldBe true
    result.right.value.size shouldBe 3 //last one will be in the list
  }

  it should "not be able to get all items when the end time is not valid" in {
    val blacklistItemData =
      BlacklistItemData(None,
                        BlacklistFunctionality.ALL.toString,
                        BlacklistType.IP.toString,
                        "127.0.0.255",
                        LocalDateTime.now,
                        Some(LocalDateTime.now.minusMinutes(10)),
                        1l)

    val res = for {
      _ <- testObject.save(blacklistItemData, context1)
      getAll <- testObject.getAllBlacklistItems(1l, LocalDateTime.now)
    } yield getAll

    val result = res.attempt.unsafeRunSync
    result.isRight shouldBe true
    result.right.value.size shouldBe 3 //last one will not be in the list
  }

  it should "be able to get all items by functionality" in {
    val blacklistItemDataCallMeNow =
      BlacklistItemData(None,
                        BlacklistFunctionality.CALL_ME_NOW.toString,
                        BlacklistType.IP.toString,
                        "127.0.0.1",
                        LocalDateTime.now,
                        Some(LocalDateTime.now.plusMinutes(10)),
                        1l)

    val res = for {
      _ <- testObject.save(blacklistItemDataCallMeNow, context1)
      res1 <- testObject.getAllBlacklistItemsByFunctionality(
        1l,
        LocalDateTime.now,
        BlacklistFunctionality.ALL)
      res2 <- testObject.getAllBlacklistItemsByFunctionality(
        1l,
        LocalDateTime.now,
        BlacklistFunctionality.CALL_ME_NOW)
    } yield (res1, res2)

    val result = res.attempt.unsafeRunSync
    result.isRight shouldBe true
    result.right.value._1.size shouldBe 3
    result.right.value._2.size shouldBe 1
  }

  it should "clean up blacklist" in {
    val result =
      testObject.cleanUpBlacklist(1l).attempt.unsafeRunSync()

    result.isRight shouldBe true
    result.right.value shouldBe 1 //only one blacklist item was in the past
  }
}
