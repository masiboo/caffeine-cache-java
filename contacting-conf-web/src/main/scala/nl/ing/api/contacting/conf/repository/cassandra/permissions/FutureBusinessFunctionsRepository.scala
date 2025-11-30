package nl.ing.api.contacting.conf.repository.cassandra.permissions

import cats.data.EitherT
import cats.effect.Async
import com.datastax.oss.driver.api.core.ConsistencyLevel
import io.getquill.MappedEncoding
import nl.ing.api.contacting.caching.hazelcast.cache.DMultiMapCache
import nl.ing.api.contacting.conf.domain.BusinessFunctionVO
import nl.ing.api.contacting.conf.modules.ExecutionContextConfig
import nl.ing.api.contacting.conf.repository.cassandra.quill.QuillQueryExecutor
import nl.ing.api.contacting.tracing.Trace
import nl.ing.api.contacting.trust.rest.feature.permissions.OrganisationalRestrictionLevel
import nl.ing.api.contacting.util.ResultF

import scala.concurrent.{ExecutionContext, Future}
import scala.language.higherKinds

trait BusinessFunctionsRepository[F[_]] {

  /**
   * search all business functions for the account
   *
   * @param accountFriendlyName the twilio account name, like nl-prd
   * @return Seq of BusinessFunction objects
   */
  def findByAccount(accountFriendlyName: String): F[Seq[BusinessFunctionVO]]

  def upsertPermissions(accountFriendlyName: String, permissions: Traversable[BusinessFunctionVO]): ResultF[F, Unit]

  def deletePermissions(accountFriendlyName: String, permissions: Traversable[BusinessFunctionVO]): ResultF[F, Unit]
}

/**
 * Cassandra repository class for storing permissions based on business functions and user roles
 *
 * @param quillWrapper : Quill query executor
 */
class FutureBusinessFunctionsRepository(dCache: DMultiMapCache)(implicit val quillWrapper: QuillQueryExecutor)
  extends BusinessFunctionsRepository[Future] {
  implicit private val ec: ExecutionContext = ExecutionContextConfig.executionContext

  private val businessFunctionMapName: String = "BF-BY-ACCOUNT" // {NL-tst -> [,,,], BE-tst -> [,,],..}

  // Quill can't decode enums, so provide a Mapping for it
  implicit private val encodeOrganisationalLevel: MappedEncoding[OrganisationalRestrictionLevel, String] =
    MappedEncoding[OrganisationalRestrictionLevel, String](_.value)
  implicit private val decodeOganisationLevel: MappedEncoding[String, OrganisationalRestrictionLevel] =
    MappedEncoding[String, OrganisationalRestrictionLevel](OrganisationalRestrictionLevel.fromValue)
  /**
   * search all business functions for the account
   *
   * @param accountFriendlyName the twilio account name, like nl-prd
   * @return Seq of BusinessFunction objects
   */
  def findByAccount(accountFriendlyName: String): Future[Seq[BusinessFunctionVO]] = {

    dCache.getAll(businessFunctionMapName, accountFriendlyName) {
      key =>
        quillWrapper(Some(ConsistencyLevel.ONE))(context => {
          import context._
          run(quote(businessFunctionsSchema.filter(a => a.accountFriendlyName == lift(key))))
        })
    }
  }

  def upsertPermissions(accountFriendlyName: String, permissions: Traversable[BusinessFunctionVO]): ResultF[Future, Unit] = {
    dCache.upsertAll(businessFunctionMapName, accountFriendlyName, permissions.toList) {
      quillWrapper()(context => {
        import context._
        run(quote {
          liftQuery(permissions.toList).foreach(permission => businessFunctionsSchema.insert(permission))
        })
      }).map(_ => ())
    }
  }

  def deletePermissions(accountFriendlyName: String, permissions: Traversable[BusinessFunctionVO]): ResultF[Future, Unit] = {
    dCache.deleteAll(businessFunctionMapName, accountFriendlyName) {
      businessFunctionVO: BusinessFunctionVO =>
        permissions.exists(p => p.accountFriendlyName ==
          businessFunctionVO.accountFriendlyName &&
          p.businessFunction == businessFunctionVO.businessFunction &&
          p.organisationId == businessFunctionVO.organisationId && p.role == businessFunctionVO.role)
    } {
      quillWrapper()(context => {
        import context._
        run(quote {
          liftQuery(permissions.toList).foreach(
            permission =>
              businessFunctionsSchema
                .withFilter(
                  p =>
                    p.accountFriendlyName ==
                      permission.accountFriendlyName &&
                      p.businessFunction == permission.businessFunction &&
                      p.organisationId == permission.organisationId && p.role == permission.role)
                .delete)
        })
      })
    }.map(_ => ())
  }
}

class AsyncBusinessFunctionsRepository[F[_] : Async : Trace](futRepo: FutureBusinessFunctionsRepository)
  extends BusinessFunctionsRepository[F] {

  import ExecutionContextConfig.executionContext
  import nl.ing.api.contacting.util.syntaxf.FutureSyntax.FutureAsync

  /**
   * search all business functions for the account
   *
   * @param accountFriendlyName the twilio account name, like nl-prd
   * @return Seq of BusinessFunction objects
   */
  override def findByAccount(accountFriendlyName: String): F[Seq[BusinessFunctionVO]] =
    Trace[F].span("BusinessFunctionsRepository: findByAccount") {
      futRepo.findByAccount(accountFriendlyName).asDelayedF
    }

  override def upsertPermissions(accountFriendlyName: String, permissions: Traversable[BusinessFunctionVO]): ResultF[F, Unit] =
    EitherT(futRepo.upsertPermissions(accountFriendlyName, permissions).value.asDelayedF)

  override def deletePermissions(accountFriendlyName: String, permissions: Traversable[BusinessFunctionVO]): ResultF[F, Unit] =
    EitherT(futRepo.deletePermissions(accountFriendlyName, permissions).value.asDelayedF)
}
