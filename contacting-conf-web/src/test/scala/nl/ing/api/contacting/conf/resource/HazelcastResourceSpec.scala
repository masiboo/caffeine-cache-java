package nl.ing.api.contacting.conf.resource

import cats.data.EitherT
import cats.effect.IO
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.ing.api.contacting.dto.resource.hazelcast._
import jakarta.ws.rs.core.MediaType
import nl.ing.api.contacting.conf.business.hazelcast.HazelcastInterpreter
import nl.ing.api.contacting.conf.modules.CoreModule
import nl.ing.api.contacting.conf.support.SessionContextSupport
import nl.ing.api.contacting.test.jersey.JerseySupport.JsonSupport
import nl.ing.api.contacting.util.exception.{ContactingBusinessError, RuntimeError}
import org.mockito.Mockito.when

import scala.util.Random

class HazelcastResourceSpec extends SessionContextSupport[HazelcastResource, HazelcastMapEntryDto]{
  override implicit val path: String = "/contacting-conf/hz"
  implicit val jsonObjectMapper =
    new ObjectMapper().registerModule(DefaultScalaModule)
  private val hazelcastAlgebraF = mock[HazelcastInterpreter[IO]]

  override protected def beforeEach(): Unit = {
    when(get[CoreModule].hazelcastAlgebraF)
      .thenReturn(hazelcastAlgebraF)
  }
  it should "retrieve all entries" in {
    val mapName = Random.nextString(5)
    val map: Map[String, List[HazelcastMapEntryDto]] = Map(
      "1" -> List(HazelcastMapEntryDto(Random.nextString(3), Random.nextString(5))),
      "2" -> List(HazelcastMapEntryDto(Random.nextString(3), Random.nextString(5))),
      "3" -> List(HazelcastMapEntryDto(Random.nextString(3), Random.nextString(5)))
    )
    when(hazelcastAlgebraF.getAllMapEntries(mapName)).thenReturn(EitherT[IO, ContactingBusinessError, Option[Map[String, List[HazelcastMapEntryDto]]]](IO(Right(Some(map)))))

    val response = target(path + s"/map/${mapName}").request().accept(MediaType.APPLICATION_JSON).get

    response.getStatus shouldBe 200
    val responseMap = response.asDto(classOf[HazelcastMapEntriesDto])
    responseMap.data.keys.size shouldBe 3
    responseMap.data shouldBe map
  }

  it should "return 404 in case of unknown map" in {
    val mapName = Random.nextString(5)
    when(hazelcastAlgebraF.getAllMapEntries(mapName)).thenReturn(EitherT[IO, ContactingBusinessError, Option[Map[String, List[HazelcastMapEntryDto]]]](IO(Right(None))))

    val response = target(path + s"/map/${mapName}").request().accept(MediaType.APPLICATION_JSON).get

    response.getStatus shouldBe 404
  }

  it should "return 500 in case of errors 01" in {
    val mapName = Random.nextString(5)
    when(hazelcastAlgebraF.getAllMapEntries(mapName)).thenReturn(EitherT[IO, ContactingBusinessError, Option[Map[String, List[HazelcastMapEntryDto]]]](IO(Left(RuntimeError(throw new RuntimeException("Error"))))))

    val response = target(path + s"/map/${mapName}").request().accept(MediaType.APPLICATION_JSON).get

    response.getStatus shouldBe 500
  }

  it should "delete and retrieve all entries" in {
    val mapName = Random.nextString(5)
    val key = Random.nextString(3)
    val l: List[HazelcastMapEntryDto] = List(
      HazelcastMapEntryDto(Random.nextString(3), Random.nextString(5)),
      HazelcastMapEntryDto(Random.nextString(3), Random.nextString(5)),
      HazelcastMapEntryDto(Random.nextString(3), Random.nextString(5)))

    when(hazelcastAlgebraF.removeKey(mapName, key)).thenReturn(EitherT[IO, ContactingBusinessError, Option[List[HazelcastMapEntryDto]]](IO(Right(Some(l)))))

    val response = target(path + s"/map/${mapName}/${key}").request().accept(MediaType.APPLICATION_JSON).delete

    response.getStatus shouldBe 200
    val responseList = response.asDto(classOf[HazelcastCollectionDto])
    responseList.data.size shouldBe 3
    responseList.data shouldBe l
  }

  it should "return 404 in case of unknown map-key" in {
    val mapName = Random.nextString(5)
    val key = Random.nextString(3)
    when(hazelcastAlgebraF.removeKey(mapName, key)).thenReturn(EitherT[IO, ContactingBusinessError, Option[List[HazelcastMapEntryDto]]](IO(Right(None))))

    val response = target(path + s"/map/${mapName}/${key}").request().accept(MediaType.APPLICATION_JSON).delete
    response.getStatus shouldBe 404
  }

  it should "return 500 in case of errors 02" in {

    val mapName = Random.nextString(5)
    val key = Random.nextString(3)
    when(hazelcastAlgebraF.removeKey(mapName, key)).thenReturn(EitherT[IO, ContactingBusinessError, Option[List[HazelcastMapEntryDto]]](IO(Left(RuntimeError(throw new RuntimeException("Error"))))))

    val response = target(path + s"/map/${mapName}/${key}").request().accept(MediaType.APPLICATION_JSON).delete
    response.getStatus shouldBe 500

  }

  it should "fetch all custer instances" in {
    val instances: Set[HazelcastInstanceDto] = Set(
      HazelcastInstanceDto(Random.nextString(3)),
      HazelcastInstanceDto(Random.nextString(3)),
      HazelcastInstanceDto(Random.nextString(3)))

    when(hazelcastAlgebraF.getAllInstances()).thenReturn(EitherT[IO, ContactingBusinessError, Set[HazelcastInstanceDto]](IO(Right(instances))))

    val response = target(path + "/instances").request().accept(MediaType.APPLICATION_JSON).get
    response.getStatus shouldBe 200
   val responseSet = response.asDto(classOf[HazelcastInstanceSet])
    responseSet.data.size shouldBe 3
    responseSet.data shouldBe instances
  }

  it should "return 500 in case of errors 03" in {

    when(hazelcastAlgebraF.getAllInstances()).thenReturn(EitherT[IO, ContactingBusinessError, Set[HazelcastInstanceDto]](IO(Left(RuntimeError(throw new RuntimeException("Error"))))))

    val response = target(path + "/instances").request().accept(MediaType.APPLICATION_JSON).get
    response.getStatus shouldBe 500
  }

  it should "evict the whole map" in {
    val mapName = Random.nextString(5)
    val map: Map[String, List[HazelcastMapEntryDto]] = Map(
      "1" -> List(HazelcastMapEntryDto(Random.nextString(3), Random.nextString(5))),
      "2" -> List(HazelcastMapEntryDto(Random.nextString(3), Random.nextString(5))),
      "3" -> List(HazelcastMapEntryDto(Random.nextString(3), Random.nextString(5)))
    )
    when(hazelcastAlgebraF.removeMap(mapName)).thenReturn(EitherT[IO, ContactingBusinessError, Option[Map[String, List[HazelcastMapEntryDto]]]](IO(Right(Some(map)))))

    val response = target(path + s"/map/${mapName}").request().accept(MediaType.APPLICATION_JSON).delete

    response.getStatus shouldBe 200
    val responseMap = response.asDto(classOf[HazelcastMapEntriesDto])
    responseMap.data.keys.size shouldBe 3
    responseMap.data shouldBe map
  }

  it should "return 404 in case of the eviction of an unknown map" in {
    val mapName = Random.nextString(5)
    when(hazelcastAlgebraF.removeMap(mapName)).thenReturn(EitherT[IO, ContactingBusinessError, Option[Map[String, List[HazelcastMapEntryDto]]]](IO(Right(None))))

    val response = target(path + s"/map/${mapName}").request().accept(MediaType.APPLICATION_JSON).delete

    response.getStatus shouldBe 404
  }

  it should "return 500 in case of errors 04" in {
    val mapName = Random.nextString(5)
    when(hazelcastAlgebraF.removeMap(mapName)).thenReturn(EitherT[IO, ContactingBusinessError, Option[Map[String, List[HazelcastMapEntryDto]]]](IO(Left(RuntimeError(throw new RuntimeException("Error"))))))

    val response = target(path + s"/map/${mapName}").request().accept(MediaType.APPLICATION_JSON).delete

    response.getStatus shouldBe 500
  }

}
