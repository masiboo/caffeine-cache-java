package nl.ing.api.contacting.conf.business.permissions

import cats.effect.Async
import cats.implicits._
import com.typesafe.scalalogging.LazyLogging
import io.opentelemetry.api.trace.Span
import nl.ing.api.contacting.caching.util.Flags
import nl.ing.api.contacting.caching.hazelcast.config.HazelcastAPICache.fromCacheableFunction
import nl.ing.api.contacting.conf.domain.{BusinessFunctionVO, EmployeeAccountsVO}
import nl.ing.api.contacting.conf.modules.ExecutionContextConfig
import nl.ing.api.contacting.conf.repository.cassandra.permissions.{BusinessFunctionsRepository, EmployeesByAccountRepository}
import nl.ing.api.contacting.tracing.Trace

import scala.concurrent.Future
import scala.language.higherKinds

/**
 * @author Ayush Mittal
 */
trait BusinessFunctionDataSource[F[_]] {

  def findByAccount(accountFriendlyName: String): F[Seq[BusinessFunctionVO]]

  def findByEmployeeId(employeeId: String, accountFriendlyName: String): F[Option[EmployeeAccountsVO]]

}

case class AsyncBusinessFunctionDS[F[_]: Async: Trace](
    businessFunctionsRepository: BusinessFunctionsRepository[Future],
    employeesByAccountRepository: EmployeesByAccountRepository[Future])
    extends BusinessFunctionDataSource[F] with LazyLogging {

  import ExecutionContextConfig.executionContext
  import nl.ing.api.contacting.util.syntaxf.FutureSyntax.FutureAsync

  override def findByAccount(accountFriendlyName: String): F[Seq[BusinessFunctionVO]] =
    Trace[F].span("Business-Functions for account") {
      for {
        res <-
          businessFunctionsRepository.findByAccount(accountFriendlyName).asDelayedF
        _ <- Trace[F].put(("account-name", accountFriendlyName),
          ("business-functions", res.map(bf => (bf.businessFunction, bf.role, bf.restriction)).toString))
      } yield res
    }


  override def findByEmployeeId(employeeId: String, accountFriendlyName: String): F[Option[EmployeeAccountsVO]] =
    Trace[F].span("Fetch employee from cassandra") {
      for {
        res <- fromCacheableFunction("my-permissions", (employeeId, accountFriendlyName))(input =>
          employeesByAccountRepository.findByEmployeeId(input._1, input._2))(ExecutionContextConfig.ioExecutionContext, Flags.defaultFlags).asDelayedF
        _ <- Trace[F].put(
          ("employee-id", res.map(_.employeeId).toString),
          ("roles", res.map(_.roles).toString),
          ("org-restrictions", res.map(_.organisationalRestrictions).toString)
        )
      } yield res
    }
}
