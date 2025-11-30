package nl.ing.api.contacting.conf.business.hazelcast

import com.ing.api.contacting.dto.resource.hazelcast.{HazelcastInstanceDto, HazelcastMapEntryDto}
import nl.ing.api.contacting.util.ResultF

import scala.concurrent.Future


trait HazelcastAlgebra[F[_]]  {
  def getAllMapEntries(mapName: String): ResultF[F, Option[Map[String, List[HazelcastMapEntryDto]]]]
  def removeKey[K](mapName: String, key: K): ResultF[F, Option[List[HazelcastMapEntryDto]]]

  def removeMap[K](mapName: String): ResultF[F, Option[Map[String, List[HazelcastMapEntryDto]]]]

  def getAllInstances(): ResultF[F, Set[HazelcastInstanceDto]]
}
