package nl.ing.api.contacting.conf.util

import cats.data
import cats.data.NonEmptyList
import cats.data.NonEmptySet
import cats.effect.IO
import cats.effect.Sync
import cats.implicits._
import cats.kernel.Order
import com.twilio.exception.ApiException
import com.typesafe.scalalogging.LazyLogging
import jakarta.ws.rs.core.Response
import nl.ing.api.contacting.conf.exception.AccountNotFoundException
import nl.ing.api.contacting.conf.exception.Forbidden
import nl.ing.api.contacting.conf.exception.IllegalArgument
import nl.ing.api.contacting.conf.exception.InvalidPhoneNumberFormat
import nl.ing.api.contacting.conf.exception.KafkaMessagePublishFailed
import nl.ing.api.contacting.conf.exception.MissingIdentityException
import nl.ing.api.contacting.conf.exception.RefinedTypeError
import nl.ing.api.contacting.conf.exception.ResourceAlreadyExists
import nl.ing.api.contacting.conf.exception.ResourceDoesNotExists
import nl.ing.api.contacting.conf.exception.TaskChannelNotFound
import nl.ing.api.contacting.conf.exception.TaskServiceLevelNotFoundException
import nl.ing.api.contacting.conf.exception.TwilioApiException
import nl.ing.api.contacting.conf.exception.TwilioCoreFactoryUpdateFailed
import nl.ing.api.contacting.conf.exception.TwilioSyncFailed
import nl.ing.api.contacting.conf.exception.UnsupportedOperation
import nl.ing.api.contacting.conf.exception.ValueMissing
import nl.ing.api.contacting.conf.exception.WebhookDetailsError
import nl.ing.api.contacting.conf.exception.WorkflowNotFoundException
import nl.ing.api.contacting.conf.util.Responses.badRequest
import nl.ing.api.contacting.conf.util.Responses.conflict
import nl.ing.api.contacting.conf.util.Responses.forbidden
import nl.ing.api.contacting.conf.util.Responses.notFound
import nl.ing.api.contacting.conf.util.Responses.serverError
import nl.ing.api.contacting.util.ErrorMappingFunction
import nl.ing.api.contacting.util.Result
import nl.ing.api.contacting.util.ResultF
import nl.ing.api.contacting.util.ResultT
import nl.ing.api.contacting.util.exception.ContactingBusinessError
import nl.ing.api.contacting.util.exception.RuntimeError
import nl.ing.api.contacting.util.exception.TimeoutException
import nl.ing.api.contacting.util.syntax.ResultTSyntax.defaultDuration

import java.sql.SQLException
import java.sql.SQLIntegrityConstraintViolationException
import scala.concurrent.duration.Duration
import scala.concurrent.duration.FiniteDuration
import scala.language.higherKinds

/**
 * @author Ayush Mittal
 */
object ContactingCatsUtils extends LazyLogging {

  type OccupiedList[A] = data.NonEmptyList[A]

  type OccupiedSet[A] = data.NonEmptySet[A]

  /**
   * Get an occupied or a non empty List from string value which contains data seperated by a delimiter
   * @param value : the input value
   * @param delimiter : the delimiter
   * @param ifEmpty : The contacingbusiness error if input string is empty
   * @return
   */
  def nelFromValue(value: String,
                   delimiter: String = ",",
                   ifEmpty: ContactingBusinessError = IllegalArgument("empty string is not allowed"))
  : Result[OccupiedList[String]] = {
    value.split(delimiter).toList match {
      case List("") | Nil => Left(ifEmpty)
      case head :: tail   => Right(NonEmptyList.of(head, tail: _*))
    }
  }

  /**
   * Get  an occupied list from a list
   * @param head : the head of list
   * @param tail : the tail
   * @tparam A
   * @return : An OccupiedList
   */
  def nelFromList[A](head: A, tail: List[A]): OccupiedList[A] = {
    NonEmptyList.of(head, tail: _*)
  }

  /**
   * Get  an occupied list from a list
   * @param list : the list
   * @tparam A
   * @return : An OccupiedList
   */
  def nelFromList[A](
                      list: List[A],
                      ifEmpty: ContactingBusinessError = IllegalArgument("empty string is not allowed")): Result[OccupiedList[A]] = {
    list match {
      case head :: tail => Right(nelFromList(head, tail))
      case Nil          => Left(ifEmpty)
    }
  }

  /**
   * Get  an occupied set from another Set
   * @param head : the head of Set
   * @param tail : the tail of set
   * @tparam A
   * @return : An OccupiedSet
   */
  def nesFromSet[A](head: A, tail: Set[A])(implicit order: Order[A]): OccupiedSet[A] = {
    NonEmptySet.of(head, tail.toList: _*)
  }

  /**
   * Get an occupied or a non empty Set from string value which contains data seperated by a delimiter
   * @param value : the input value
   * @param delimiter : the delimiter
   * @param ifEmpty : The contacingbusiness error if input string is empty
   * @return
   */
  def nesFromValue(
                    value: String,
                    delimiter: String = ",",
                    ifEmpty: ContactingBusinessError = IllegalArgument("empty string is not allowed")): Result[OccupiedSet[String]] = {
    value.split(delimiter).toList match {
      case List("") | Nil => Left(ifEmpty)
      case head :: tail   => Right(cats.data.NonEmptySet.of(head, tail: _*))
    }
  }

  /**
   * Get an occupied or a non empty Set from a scala set,
   * @param set : the input value
   * @param ifEmpty : The contacingbusiness error if input set is empty
   * @return
   */
  def nesFromSet[A](set: Set[A], ifEmpty: ContactingBusinessError = IllegalArgument("empty set is not allowed"))(
    implicit order: Order[A]): Result[OccupiedSet[A]] = {
    if (set.isEmpty) Left(ifEmpty)
    else Right(NonEmptySet.of[A](set.head, set.tail.toList: _*))
  }

  def nesFromSingleValue[A](a: A)(implicit order: Order[A]): OccupiedSet[A] = {
    NonEmptySet.of[A](a)
  }

  def contactingErrorMappingFunction[A]: ErrorMappingFunction[A] = {
    case t: AccountNotFoundException => Left(Forbidden(t.getMessage))
    case t: ApiException => Left(TwilioApiException(t))
    case t: RuntimeException if(t.getCause.isInstanceOf[ApiException]) =>
      Left(TwilioApiException(t.getCause.asInstanceOf[ApiException]))
    case s: SQLIntegrityConstraintViolationException =>
      Left(UnsupportedOperation(s"sql integrity violation - ${s.getMessage}"))
    case s: SQLException =>
      Left(UnsupportedOperation(s"sql exception - ${s.getMessage}"))
  }

  def toResponse[A](context: Result[A])(f: A => Response): Response =
    context match {
      case Right(a)                                   => f(a)
      case Left(RefinedTypeError(msg))                => badRequest(msg)
      case Left(ResourceDoesNotExists)                => notFound("resource not found")
      case Left(ValueMissing(msg))                    => notFound(msg)
      case Left(Forbidden(msg))                       => forbidden(msg)
      case Left(ResourceAlreadyExists)                => conflict("Resource already exists.")
      case Left(IllegalArgument(msg))                 => badRequest(msg)
      case Left(KafkaMessagePublishFailed(msg))       =>  badRequest(msg)
      case Left(TwilioCoreFactoryUpdateFailed(msg))   =>  badRequest(msg)
      case Left(TwilioSyncFailed(msg))                =>  badRequest(msg)
      case Left(WebhookDetailsError(msg))             =>  badRequest(msg)
      case Left(TaskServiceLevelNotFoundException(_)) => badRequest("No matching task service level found")
      case Left(WorkflowNotFoundException(_))         => badRequest("Could not find workflow")
      case Left(UnsupportedOperation(msg, _))         => badRequest(msg)
      case Left(MissingIdentityException)             => forbidden("Missing employee or customer context")
      case Left(TaskChannelNotFound(msg)) =>
        badRequest(msg) //task channel are always created by scripts and the failure is always for a worker update; henve bad request
      case Left(InvalidPhoneNumberFormat(msg)) => badRequest(msg)
      case Left(TimeoutException(timeoutSeconds)) =>
        logger.error(s"timeout exception occurred of $timeoutSeconds seconds")
        serverError(Some(s"Request timed out after ${timeoutSeconds.toSeconds} seconds"))
      case Left(RuntimeError(ex)) =>
        logger.error(
          s"exception occurred while execution: ${Option(ex.getCause).map(_.getMessage).getOrElse(ex.getMessage)}",
          ex)
        serverError(Option(ex.getCause).map(_.getMessage).orElse(Option(ex.getMessage)))
      case Left(ex) =>
        logger.error(s"exception occurred while execution: ", ex)
        serverError(Option(ex.toString))
    }

  implicit class ResultTResponseOps[A](resultT: ResultT[A]) {
    import nl.ing.api.contacting.util.syntax.ResultTSyntax.ResultTOps

    @deprecated(message =  "Use toIOResponse, this method  is blocking", since =  "1.0.24")
    def response(f: A => Response)(implicit maxAwaitDuration: Duration = defaultDuration): Response =
      ContactingCatsUtils.toResponse(resultT.unsafeResult(maxAwaitDuration))(f)

    def toIOResponse(f: A => Response)(implicit maxAwaitDuration: FiniteDuration = defaultDuration): IO[Response] = {
      resultT.value.timeout(maxAwaitDuration).map{
        result : Result[A] =>
          toResponse(result)(f)
      }
    }
  }

  implicit class IOResponseOps[A](io: IO[A]) {

    import nl.ing.api.contacting.util.syntax.IOSyntax.IOOps

    @deprecated(message =  "Use toIOResponse, this method  is blocking", since =  "1.0.24")
    def response(f: A => Response, partialFunction: ErrorMappingFunction[A] = contactingErrorMappingFunction)(
      implicit maxAwaitDuration: Duration = defaultDuration): Response = {
      io.adaptErrorT(partialFunction).response(f)(maxAwaitDuration)
    }

    def toResultT(partialFunction: ErrorMappingFunction[A] = contactingErrorMappingFunction): ResultT[A] =
      io.adaptErrorT(partialFunction)

    def toIOResponse(f: A => Response, partialFunction: ErrorMappingFunction[A] = contactingErrorMappingFunction)(implicit maxAwaitDuration: FiniteDuration = defaultDuration): IO[Response] = {
      io.timeout(maxAwaitDuration).adaptErrorT(partialFunction).value.map{
        result: Result[A] =>
          toResponse(result)(f)
      }
    }
  }

  implicit class IOResultResponse[A](io: IO[Result[A]]) {

    import nl.ing.api.contacting.util.syntax.IOSyntax.IOResultOps

    @deprecated(message =  "Use toIOResponse, this method  is blocking", since =  "1.0.24")
    def response(f: A => Response)(implicit maxAwaitDuration: Duration = defaultDuration): Response =
      io.toResultT.response(f)(maxAwaitDuration)

    def toIOResponse(f: A => Response)(implicit maxAwaitDuration: FiniteDuration = defaultDuration): IO[Response] = {
      io.timeout(maxAwaitDuration).map{
        result: Result[A] =>
          toResponse(result)(f)
      }
    }
  }

  implicit class FResultF[F[_]: Sync, A](f: F[A]) {
    import nl.ing.api.contacting.util.syntaxf.FSyntax.FOps
    def handleTwilioErrors: ResultF[F,A] = {
      f.adaptErrorT(contactingErrorMappingFunction)
    }
  }
}
