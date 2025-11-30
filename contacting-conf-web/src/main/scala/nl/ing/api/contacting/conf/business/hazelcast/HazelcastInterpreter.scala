package nl.ing.api.contacting.conf.business.hazelcast

import cats.data.EitherT
import cats.effect.{Async}
import nl.ing.api.contacting.caching.hazelcast.cache.DMultiMapCache
import nl.ing.api.contacting.conf.modules.ExecutionContextConfig

import scala.concurrent.ExecutionContext
import cats.syntax.all._
import com.ing.api.contacting.dto.resource.hazelcast.{HazelcastInstanceDto, HazelcastMapEntryDto}
import nl.ing.api.contacting.util.ResultF
class HazelcastInterpreter [F[_]: Async](dCache: DMultiMapCache) extends HazelcastAlgebra[F] {
  implicit private val ec: ExecutionContext = ExecutionContextConfig.executionContext

  import nl.ing.api.contacting.util.syntaxf.FutureSyntax.FutureAsync

   def getAllMapEntries(mapName: String): ResultF[F, Option[Map[String, List[HazelcastMapEntryDto]]]] = EitherT {
     dCache.findAll[String, HazelcastMapEntryDto](mapName).value.asDelayedF.map {
       case Right(m) => if (m.isEmpty) Right(None) else Right(Some(m))
     }
   }

  override def removeKey[K](mapName: String, key: K): ResultF[F, Option[List[HazelcastMapEntryDto]]] = EitherT {
    dCache.evictEntry[K, HazelcastMapEntryDto](mapName, key).value.asDelayedF.map {
      case Right(m) => if (m.isEmpty) Right(None) else Right(Some(m))
    }
  }

  override def getAllInstances(): ResultF[F, Set[HazelcastInstanceDto]] = EitherT {
    dCache.getInstances.value.asDelayedF
  }

  override def removeMap[K](mapName: String): ResultF[F, Option[Map[String, List[HazelcastMapEntryDto]]]] = EitherT {
    dCache.evictMap[String, HazelcastMapEntryDto](mapName).value.asDelayedF.map {
      case Right(m) => if (m.isEmpty) Right(None) else Right(Some(m))
    }
  }
}
