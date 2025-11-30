package nl.ing.api.contacting.conf.domain

import scala.util.Try

case class ContactingConfigVO(key: String, values: String) {
  /**
    * will split the values by ",", so it will give a set of values
    *
    * @return
    */
  def valuesAsSet(): Set[String] = {
    values.split(",").toSet
  }

  private def toBoolean(default: Boolean): Boolean=
    Try(values.toBoolean).getOrElse(default)

  def booleanValueWithFalseDefault: Boolean =
    toBoolean(false)

  def booleanValueWithTrueDefault: Boolean =
    toBoolean(true)

}
