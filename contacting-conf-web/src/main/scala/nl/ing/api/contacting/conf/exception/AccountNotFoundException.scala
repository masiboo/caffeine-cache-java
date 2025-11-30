package nl.ing.api.contacting.conf.exception

/**
  * Created by bo55nk on 5/31/17.
  */
case class AccountNotFoundException(accountSid: String) extends RuntimeException(s"AccountSid not found:$accountSid")
