package nl.ing.api.contacting.conf.domain.enums

/**
 * @author Ayush M
 */
sealed trait UserRole {
  def isAgent: Boolean = false
  def forEmployee: Boolean = true
  val role: String
}

case object ACCOUNT_ADMIN extends UserRole {
  override val role: String = "ACCOUNT_ADMIN"
}

case object ADMIN extends UserRole {
  override val role: String = "ADMIN"
}

case object AGENT extends UserRole {
  override val role: String = "AGENT"
  override def isAgent: Boolean = true
}

case object ANNOUNCEMENT_MANAGER extends UserRole {
  override val role: String = "ANNOUNCEMENT_MANAGER"
}

case object CAMPAIGN_MANAGER extends UserRole {
  override val role: String = "CAMPAIGN_MANAGER"
}

case object CJE extends UserRole {
  override val role: String = "CJE"
}

case object CUSTOMER_AUTHENTICATED extends UserRole {
  override val role: String = "CUSTOMER_AUTHENTICATED"
  override def forEmployee: Boolean = false
}

case object CUSTOMER_UNAUTHENTICATED extends UserRole {
  override val role: String = "CUSTOMER_UNAUTHENTICATED"
  override def forEmployee: Boolean = false
}

case object COACH extends UserRole {
  override val role: String = "COACH"
}

case object COMPLIANCE_OFFICER extends UserRole {
  override val role: String = "COMPLIANCE_OFFICER"
}

case object DATA_ANALYST extends UserRole {
  override val role: String = "DATA_ANALYST"
}

case object CONFIG_MANAGER extends UserRole {
  override val role: String = "CONFIG_MANAGER"
}

case object CONTACTING extends UserRole {
  override val role: String = "CONTACTING"
  override def forEmployee: Boolean = false
}

case object FOREIGN_API extends UserRole {
  override val role: String = "FOREIGN_API"
  override def forEmployee: Boolean = false
}

case object MONITORING extends UserRole {
  override val role: String = "MONITORING"
}

case object OPS_ADMIN extends UserRole {
  override val role: String = "OPS_ADMIN"
}

case object PRIVACY_OFFICER extends UserRole {
  override val role: String = "PRIVACY_OFFICER"
}

case object QUALITY_ASSURANCE extends UserRole {
  override val role: String = "QUALITY_ASSURANCE"
}

case object SUPERVISOR extends UserRole {
  override val role: String = "SUPERVISOR"
}

case object STAFF_MANAGER extends UserRole {
  override val role: String = "STAFF_MANAGER"
}

case object TELEOPTI_SUPER_ADMINISTRATOR extends UserRole {
  override val role: String = "TELEOPTI_SUPER_ADMINISTRATOR"
}

case object TELEOPTI_SCHEDULER extends UserRole {
  override val role: String = "TELEOPTI_SCHEDULER"
}

case object TELEOPTI_MEMBER extends UserRole {
  override val role: String = "TELEOPTI_MEMBER"
}

case object TELEOPTI_FORECASTER extends UserRole {
  override val role: String = "TELEOPTI_FORECASTER"
}

case object TELEOPTI_LEAD extends UserRole {
  override val role: String = "TELEOPTI_LEAD"
}

case object TEMPLATE_MANAGER extends UserRole {
  override val role: String = "TEMPLATE_MANAGER"
}

