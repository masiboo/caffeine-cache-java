package nl.ing.api.contacting.conf.hazelcast

import cats.data.EitherT
import cats.effect.IO
import com.ing.api.contacting.dto.resource.hazelcast.{HazelcastInstanceDto, HazelcastMapEntryDto}
import nl.ing.api.contacting.caching.hazelcast.cache.DMultiMapCache
import nl.ing.api.contacting.conf.BaseSpec
import nl.ing.api.contacting.conf.business.hazelcast.HazelcastInterpreter
import nl.ing.api.contacting.util.exception.ContactingBusinessError
import org.mockito.Mockito.when
import nl.ing.api.contacting.conf.modules.ExecutionContextConfig.ioRunTime
import scala.concurrent.Future
import scala.util.Random
class HazelcastInterpreterSpec extends BaseSpec {
  private val dCache = mock[DMultiMapCache]

  it should "fetch all map entries" in {
    val mapName = Random.nextString(5)
    val map: Map[String, List[HazelcastMapEntryDto]] = Map(
      "1" -> List(HazelcastMapEntryDto(Random.nextString(3), Random.nextString(5))),
      "2" -> List(HazelcastMapEntryDto(Random.nextString(3), Random.nextString(5))),
      "3" -> List(HazelcastMapEntryDto(Random.nextString(3), Random.nextString(5)))
    )
    when(dCache.findAll[String, HazelcastMapEntryDto](mapName)).thenReturn(EitherT[Future, ContactingBusinessError, Map[String, List[HazelcastMapEntryDto]]](Future.successful(Right(map))))

    val testObject = new HazelcastInterpreter[IO](dCache)

    val mapEntries = testObject.getAllMapEntries(mapName).value.unsafeRunSync()
    mapEntries shouldBe Right(Some(map))
  }

  it should "return None for unknown mapName" in {
    val mapName = Random.nextString(5)

    when(dCache.findAll[String, HazelcastMapEntryDto](mapName)).thenReturn(EitherT[Future, ContactingBusinessError, Map[String, List[HazelcastMapEntryDto]]](Future.successful(Right(Map()))))

    val testObject = new HazelcastInterpreter[IO](dCache)

    val mapEntries = testObject.getAllMapEntries(mapName).value.unsafeRunSync()
    mapEntries shouldBe Right(None)
  }

  it should "fetch all map key entries and evict the cache" in {
    val mapName = Random.nextString(5)
    val key = Random.nextString(3)
    val list: List[HazelcastMapEntryDto] = List(HazelcastMapEntryDto(Random.nextString(3), Random.nextString(5)), HazelcastMapEntryDto(Random.nextString(3), Random.nextString(5)), HazelcastMapEntryDto(Random.nextString(3), Random.nextString(5)))
    when(dCache.evictEntry[String, HazelcastMapEntryDto](mapName, key)).thenReturn(EitherT[Future, ContactingBusinessError, List[HazelcastMapEntryDto]](Future.successful(Right(list))))

    val testObject = new HazelcastInterpreter[IO](dCache)

    val mapEntries = testObject.removeKey(mapName, key).value.unsafeRunSync()
    mapEntries shouldBe Right(Some(list))
  }

  it should "return None for unknown map-key and evict is neglected" in {
    val mapName = Random.nextString(5)
    val key = Random.nextString(3)
    when(dCache.evictEntry[String, HazelcastMapEntryDto](mapName, key)).thenReturn(EitherT[Future, ContactingBusinessError, List[HazelcastMapEntryDto]](Future.successful(Right(List()))))

    val testObject = new HazelcastInterpreter[IO](dCache)

    val mapEntries = testObject.removeKey(mapName, key).value.unsafeRunSync()
    mapEntries shouldBe Right(None)
  }

  it should "fetch all cluster instances" in {

    val instancesSet: Set[HazelcastInstanceDto] = Set(HazelcastInstanceDto(Random.nextString(3)), HazelcastInstanceDto(Random.nextString(3)), HazelcastInstanceDto(Random.nextString(3)))
    when(dCache.getInstances).thenReturn(EitherT[Future, ContactingBusinessError, Set[HazelcastInstanceDto]](Future.successful(Right(instancesSet))))

    val testObject = new HazelcastInterpreter[IO](dCache)

    val instances = testObject.getAllInstances().value.unsafeRunSync()
    instances shouldBe Right(instancesSet)
  }
  it should "evict the whole map" in {
    val mapName = Random.nextString(5)
    val map: Map[String, List[HazelcastMapEntryDto]] = Map(
      "1" -> List(HazelcastMapEntryDto(Random.nextString(3), Random.nextString(5))),
      "2" -> List(HazelcastMapEntryDto(Random.nextString(3), Random.nextString(5))),
      "3" -> List(HazelcastMapEntryDto(Random.nextString(3), Random.nextString(5)))
    )
    when(dCache.evictMap[String, HazelcastMapEntryDto](mapName)).thenReturn(EitherT[Future, ContactingBusinessError, Map[String, List[HazelcastMapEntryDto]]](Future.successful(Right(map))))

    val testObject = new HazelcastInterpreter[IO](dCache)

    val mapEntries = testObject.removeMap(mapName).value.unsafeRunSync()
    mapEntries shouldBe Right(Some(map))
  }

  it should "return None for the eviction of unknown mapName" in {
    val mapName = Random.nextString(5)

    when(dCache.evictMap[String, HazelcastMapEntryDto](mapName)).thenReturn(EitherT[Future, ContactingBusinessError, Map[String, List[HazelcastMapEntryDto]]](Future.successful(Right(Map()))))

    val testObject = new HazelcastInterpreter[IO](dCache)

    val mapEntries = testObject.removeMap(mapName).value.unsafeRunSync()
    mapEntries shouldBe Right(None)
  }

}
