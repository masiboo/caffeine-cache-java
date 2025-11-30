package nl.ing.api.contacting.conf.resource

import cats.effect.IO
import com.ing.api.contacting.dto.resource.account.AccountDto
import io.opentelemetry.api.trace.Span
import jakarta.ws.rs.container.AsyncResponse
import jakarta.ws.rs.core.Response
import nl.ing.api.contacting.conf.exception.AccountNotFoundException
import nl.ing.api.contacting.conf.modules.{CoreModule, ExecutionContextConfig}
import nl.ing.api.contacting.conf.modules.ExecutionContextConfig.executionContext
import nl.ing.api.contacting.conf.resource.jaxrs.support.ContactingContextSupport
import nl.ing.api.contacting.tracing.ContactingSpan
import nl.ing.api.contacting.trust.rest.context.SessionContext
import org.springframework.beans.factory.annotation.Autowired
import nl.ing.api.contacting.util.OptionConversions._

import scala.concurrent.Future

/**
  * @author Ayush Mittal
  * A base resource for all web resources to fetch core system module
  */

trait ConfigBaseResource extends ContactingContextSupport {

  @Autowired
  var coreModule: CoreModule = _

  def span: ContactingSpan[IO] =
    ContactingSpan[IO](coreModule.tracer, Option(Span.current()))
  implicit def getAccount(sessionContext: SessionContext): AccountDto = {
    sessionContext.getSubAccount.toOption.getOrElse(throw AccountNotFoundException(s"Request has no subAccount hence Forbidden for context: $sessionContext}"))
  }
  /**
    * Utility function for supporting async responses to jax-rs resources
    *
    * @param asyncResponse JAX-RS async response container
    * @param fn            future containing the response that has to be passed back to the caller
    */
  def withAsyncFutureResponse(asyncResponse: AsyncResponse)(
      fn: => Future[Response]): Future[Boolean] =
    fn.map(asyncResponse.resume).recover({ case e => asyncResponse.resume(e) })

  /**
    * Utility function for supporting async responses to jax-rs resources
    *
    * @param asyncResponse JAX-RS async response container
    * @param response      the response that has to be passed back to the caller
    */
  def withAsyncResponse(asyncResponse: AsyncResponse)(
      response: => Response): Boolean =
    asyncResponse.resume(response)

  /**
   * Utility function for supporting async responses to jax-rs resources
   * @param asyncResponse : JAX-RS async response container
   * @param fn : the response that has to be passed back to the caller
   * @tparam F
   */
  def withAsyncIOResponse(asyncResponse: AsyncResponse)(fn: => IO[Response]): Unit =
    fn.unsafeRunAsync {
      case Left(throwable) => asyncResponse.resume(throwable)
      case Right(value) => asyncResponse.resume(value)
    }(ExecutionContextConfig.ioRunTime)
}
