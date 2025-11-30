package nl.ing.api.contacting.conf.domain

import io.getquill.Udt
import nl.ing.api.contacting.conf.domain.Types.ChannelCapacity


object Types {
  type ChannelCapacity = Map[String, Int]
}
/**
  *
  * @param employeeId
  * @param accountFriendlyName
  * @param preferredAccount
  * @param roles
  * @param businessUnit is a super circle
  * @param department   is a circle
  * @param team         is a clt
  */
case class EmployeeAccountsVO(employeeId: String, accountFriendlyName: String, preferredAccount: Boolean = true, roles: Option[String],
                              businessUnit: Option[String], department: Option[String], team: Option[String],
                              organisationalRestrictions: Option[Set[OrganisationalRestriction]] = None,
                              allowedChannels: ChannelCapacity = Map[String, Int](),
                              workerSid: Option[String] = None) {

  /**
    * Return the roles as a Set of Strings
    *
    * @return
    */
  def rolesAsSet(): Set[String] = roles match {
    case Some(x) => x.split(",").toSet[String]
    case None => Set[String]()
  }
}

object EmployeeAccountsVO {

  def apply(employeeId: String, accountFriendlyName: String, preferredAccount: Boolean, roles: Set[String], businessUnit: Option[String],
            department: Option[String], team: Option[String], organisationalRestrictions: Option[Seq[OrganisationalRestriction]],
            allowedChannels: Map[String, Int], workerSid: Option[String]) : EmployeeAccountsVO =
    EmployeeAccountsVO(employeeId, accountFriendlyName, preferredAccount, Some(roles.mkString(",")), businessUnit, department, team, organisationalRestrictions.map(_.toSet), allowedChannels, workerSid)
}

/**
  * Case class for storing and sending the organisational restrictions
  *
  * @param cltId           The team id the worker belongs to
  * @param cltName         The team the worker belongs to
  * @param circleId        The circle id the worker belongs to
  * @param circleName      The circle the worker belongs to
  * @param superCircleId   The SuperCircle id to worker belongs to
  * @param superCircleName The SuperCircle to worker belongs to
  * @param preferred       Whether this is the preferred/primary team
  */
case class OrganisationalRestriction(cltId: Int, cltName: String, circleId: Int, circleName: String, superCircleId: Int, superCircleName: String, preferred: Boolean) extends Udt
