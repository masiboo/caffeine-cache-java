package nl.ing.api.contacting.conf.exception

import com.twilio.exception.ApiException
import nl.ing.api.contacting.util.exception.ContactingBusinessError

/**
 * @author Ayush Mittal
 *
 * Represents a business error in contacting api
 */

trait ContactingBusinessErrorWithMsg extends ContactingBusinessError {
  val msg: String
}

case object ResourceAlreadyExists extends ContactingBusinessError

case object ResourceDoesNotExists extends ContactingBusinessError

case class ValueMissing(msg: String) extends ContactingBusinessError

case class TaskServiceLevelNotFoundException(message: String) extends ContactingBusinessError

case class WorkflowNotFoundException(message: String) extends ContactingBusinessError

case class Forbidden(msg: String) extends ContactingBusinessError

case class IllegalArgument(msg: String) extends ContactingBusinessErrorWithMsg

case class UnsupportedOperation(msg: String, original: Option[Throwable] = None) extends ContactingBusinessError

object MissingIdentityException extends ContactingBusinessError

case class TaskChannelNotFound(msg: String) extends ContactingBusinessError

case class TwilioCoreFactoryUpdateFailed(msg: String) extends ContactingBusinessError

case class TwilioApiException(apiException: ApiException) extends ContactingBusinessError

case object TwilioCoreFactoryUpdateFailed {
  def error(msg: String): TwilioCoreFactoryUpdateFailed = new TwilioCoreFactoryUpdateFailed(s"Failed to update connection settings in twilio core factory - $msg")
}

case class KafkaMessagePublishFailed(msg: String) extends ContactingBusinessError

case object KafkaMessagePublishFailed {
  def error(msg: String): KafkaMessagePublishFailed = new KafkaMessagePublishFailed(s"Failed to publish kafka message - $msg")
}

case class TwilioSyncFailed(msg: String) extends ContactingBusinessError

case object TwilioSyncFailed {
  def error(msg: String, cause: Option[String]): TwilioSyncFailed = new TwilioSyncFailed(s"Failed to update twilio sync - $msg" + cause.map(c => s", cause: $c").getOrElse(""))
}

case class WebhookDetailsError(msg: String = "Only one webhook can be active") extends ContactingBusinessError

case class RefinedTypeError(msg: String) extends ContactingBusinessErrorWithMsg {
  def error: String = s"Error with refined type: $msg"
}

case class InvalidPhoneNumberFormat(msg: String) extends ContactingBusinessErrorWithMsg
