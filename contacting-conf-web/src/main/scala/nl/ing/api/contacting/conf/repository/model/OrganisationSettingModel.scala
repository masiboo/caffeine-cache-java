package nl.ing.api.contacting.conf.repository.model

case class OrganisationSettingModel(id: Option[Long],
                                    key: String,
                                    value: String,
                                    accountId: Long,
                                    orgId: Long,
                                    enabled: Boolean,
                                    capability: Option[String])
