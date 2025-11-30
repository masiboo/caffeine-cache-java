package nl.ing.api.contacting.conf.repository.cassandra.permissions

import cats.effect.Async
import com.datastax.oss.driver.api.core.ConsistencyLevel
import com.typesafe.scalalogging.LazyLogging
import nl.ing.api.contacting.conf.domain.Types.ChannelCapacity
import nl.ing.api.contacting.conf.domain.{EmployeeAccountsVO, OrganisationalRestriction}
import nl.ing.api.contacting.conf.modules.ExecutionContextConfig.ioExecutionContext
import nl.ing.api.contacting.conf.repository.cassandra.quill.QuillQueryExecutor
import nl.ing.api.contacting.tracing.Trace

import scala.concurrent.{ExecutionContext, Future}
import scala.language.higherKinds

trait EmployeesByAccountRepository[F[_]] {

  def findAllByAccount(accountFriendlyName: String): F[Seq[EmployeeAccountsVO]]

  def findByEmployeeId(employeeId: String, accountFriendlyName: String): F[Option[EmployeeAccountsVO]]

  def findByEmployeeIdWithConsistency(employeeId: String, accountFriendlyName: String)(
      consistencyLevel: ConsistencyLevel = ConsistencyLevel.ONE): F[Option[EmployeeAccountsVO]]

  def findByEmployeeId(employeeId: String): F[Seq[EmployeeAccountsVO]]

  def deleteByEmployeeId(employeeId: String, accountFriendlyName: String): F[Unit]

  /**
   * update/create the employee by first fetching the current state and then updating all the values, with the exception of the preferred account
   * It will create the employee when it doesn't exists yet
   *
   * @param employee the employee to be updated
   * @return F[Unit]
   */
  def upsert(employee: EmployeeAccountsVO): F[Unit]

  /**
   * save the employee by first fetching it to determine the preferred account
   *
   * @param employee the employee to save
   * @return
   */
  def saveWithoutOverridingPreferredAccount(employee: EmployeeAccountsVO): F[Unit]

  /**
   * creates a new employeeByAccount mapping with the team information
   *
   * @param employeeId          the corp.key
   * @param accountFriendlyName the twilio account
   * @param roles               the ldap roles
   * @param preferred           should this be the preferred account
   * @return
   */
  def createAccountByEmployee(employeeId: String,
                              accountFriendlyName: String,
                              roles: Set[String],
                              preferred: Boolean,
                              clt: Option[String] = None,
                              circle: Option[String] = None,
                              superCircle: Option[String] = None,
                              organisationalRestrictions: Option[Seq[OrganisationalRestriction]] = None,
                              allowedChannels: ChannelCapacity = Map(),
                              workerSid: Option[String] = None): F[Any]
}

/**
 * Cassandra repository class for storing employees by twilio account and user roles
 *
 * @param quillWrapper the support for persistence layer
 */
class FutureEmployeesByAccountRepository(implicit val quillWrapper: QuillQueryExecutor)
    extends LazyLogging with EmployeesByAccountRepository[Future] {

  implicit val ec: ExecutionContext = ioExecutionContext
  def findAllByAccount(accountFriendlyName: String): Future[Seq[EmployeeAccountsVO]] = {
    quillWrapper( Some(ConsistencyLevel.ONE)) {
      context =>
        import context._
        run(employeesByAccountSchema)
    }.map(emp => emp.filter(_.accountFriendlyName == accountFriendlyName))
  }

  def findByEmployeeId(employeeId: String, accountFriendlyName: String): Future[Option[EmployeeAccountsVO]] =
    findByEmployeeIdWithConsistency(employeeId, accountFriendlyName)().flatMap {
      case None =>
        logger.warn(
          s"Finding $employeeId in Cassandra with Local_Quorum, because reading with consistency ONE did not fetch anything")
        findByEmployeeIdWithConsistency(employeeId, accountFriendlyName)(
          ConsistencyLevel.LOCAL_QUORUM)
      case result => Future.successful(result)
    }

  def findByEmployeeIdWithConsistency(employeeId: String, accountFriendlyName: String)(
      consistencyLevel: ConsistencyLevel = ConsistencyLevel.ONE): Future[Option[EmployeeAccountsVO]] =
    quillWrapper(Some(consistencyLevel))(context => {
      import context._
      run(quote(employeesByAccountSchema.filter(a =>
        a.employeeId == lift(employeeId.toUpperCase()) && a.accountFriendlyName == lift(accountFriendlyName))))
    }).map(_.headOption)

  def findByEmployeeId(employeeId: String): Future[Seq[EmployeeAccountsVO]] = {
    quillWrapper(Some(ConsistencyLevel.ONE))(context => {
      import context._
      run(quote(employeesByAccountSchema.filter(a => a.employeeId == lift(employeeId.toUpperCase()))))
    })
  }

  def deleteByEmployeeId(employeeId: String, accountFriendlyName: String): Future[Unit] = {
    quillWrapper()(context => {
      import context._
      run(quote {
        employeesByAccountSchema
          .filter(e =>
            e.employeeId == lift(employeeId.toUpperCase()) && e.accountFriendlyName == lift(accountFriendlyName))
          .delete
      })
    })
  }

  /**
   * update/create the employee by first fetching the current state and then updating all the values, with the exception of the preferred account
   * It will create the employee when it doesn't exists yet
   *
   * @param employee the employee to be updated
   * @return Future[Option[EmployeeAccountVO] the updated employee object
   */
  def upsert(employee: EmployeeAccountsVO): Future[Unit] = {
    quillWrapper()(context => {
      import context._
      run(quote {
        employeesByAccountSchema.insert(lift(employee))
      })
    })
  }

  /**
   * save the employee by first fetching it to determine the preferred account
   *
   * @param employee the employee to save
   * @return
   */
  def saveWithoutOverridingPreferredAccount(employee: EmployeeAccountsVO): Future[Unit] = {
    this
      .findByEmployeeId(employee.employeeId)
      .map {
        case Nil =>
          upsert(employee.copy(preferredAccount = true))
        case current =>
          val preferred = current.find(_.accountFriendlyName == employee.accountFriendlyName) match {
            case None    => false
            case Some(x) => x.preferredAccount
          }
          upsert(employee.copy(preferredAccount = preferred))
      }
      .map(employees => ()) // don't return anything
  }

  /**
   * creates a new employeeByAccount mapping with the team information
   *
   * @param employeeId          the corp.key
   * @param accountFriendlyName the twilio account
   * @param roles               the ldap roles
   * @param preferred           should this be the preferred account
   * @return
   */
  def createAccountByEmployee(employeeId: String,
                              accountFriendlyName: String,
                              roles: Set[String],
                              preferred: Boolean,
                              clt: Option[String] = None,
                              circle: Option[String] = None,
                              superCircle: Option[String] = None,
                              organisationalRestrictions: Option[Seq[OrganisationalRestriction]] = None,
                              allowedChannels: ChannelCapacity = Map(),
                              workerSid: Option[String] = None): Future[Any] = {
    upsert(
      EmployeeAccountsVO(employeeId.toUpperCase(),
                         accountFriendlyName,
                         preferred,
                         roles,
                         clt,
                         circle,
                         superCircle,
                         organisationalRestrictions,
                         allowedChannels,
                         workerSid))
  }
}

class AsyncEmployeesByAccountRepository[F[_]: Async: Trace](futRepo: FutureEmployeesByAccountRepository)
    extends EmployeesByAccountRepository[F] {

  import nl.ing.api.contacting.conf.modules.ExecutionContextConfig.executionContext
  import nl.ing.api.contacting.util.syntaxf.FutureSyntax.FutureAsync

  override def findAllByAccount(accountFriendlyName: String): F[Seq[EmployeeAccountsVO]] =
    futRepo.findAllByAccount(accountFriendlyName).asDelayedF

  override def findByEmployeeId(employeeId: String, accountFriendlyName: String): F[Option[EmployeeAccountsVO]] =
    Trace[F].span("EmployeesByAccountRepository : findByEmployeeId") {
      futRepo.findByEmployeeId(employeeId, accountFriendlyName).asDelayedF
    }

  override def findByEmployeeIdWithConsistency(employeeId: String, accountFriendlyName: String)(
      consistencyLevel: ConsistencyLevel = ConsistencyLevel.ONE): F[Option[EmployeeAccountsVO]] =
    futRepo.findByEmployeeIdWithConsistency(employeeId, accountFriendlyName)(consistencyLevel).asDelayedF

  override def findByEmployeeId(employeeId: String): F[Seq[EmployeeAccountsVO]] =
    futRepo.findByEmployeeId(employeeId).asDelayedF

  override def deleteByEmployeeId(employeeId: String, accountFriendlyName: String): F[Unit] =
    futRepo.deleteByEmployeeId(employeeId, accountFriendlyName).asDelayedF

  /**
   * update/create the employee by first fetching the current state and then updating all the values, with the exception of the preferred account
   * It will create the employee when it doesn't exists yet
   *
   * @param employee the employee to be updated
   * @return F[Unit]
   */
  override def upsert(employee: EmployeeAccountsVO): F[Unit] =
    futRepo.upsert(employee).asDelayedF

  /**
   * save the employee by first fetching it to determine the preferred account
   *
   * @param employee the employee to save
   * @return
   */
  override def saveWithoutOverridingPreferredAccount(employee: EmployeeAccountsVO): F[Unit] =
    futRepo.saveWithoutOverridingPreferredAccount(employee).asDelayedF

  /**
   * creates a new employeeByAccount mapping with the team information
   *
   * @param employeeId          the corp.key
   * @param accountFriendlyName the twilio account
   * @param roles               the ldap roles
   * @param preferred           should this be the preferred account
   * @return
   */
  override def createAccountByEmployee(employeeId: String,
                                       accountFriendlyName: String,
                                       roles: Set[String],
                                       preferred: Boolean,
                                       clt: Option[String],
                                       circle: Option[String],
                                       superCircle: Option[String],
                                       organisationalRestrictions: Option[Seq[OrganisationalRestriction]],
                                       allowedChannels: ChannelCapacity,
                                       workerSid: Option[String]): F[Any] =
    futRepo
      .createAccountByEmployee(employeeId,
                               accountFriendlyName,
                               roles,
                               preferred,
                               clt,
                               circle,
                               superCircle,
                               organisationalRestrictions,
                               allowedChannels,
                               workerSid)
      .asDelayedF
}
