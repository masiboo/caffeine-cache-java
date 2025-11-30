package nl.ing.api.contacting.conf.domain


import com.ing.api.contacting.dto.resource.blacklist.BlacklistFunctionality.BlacklistFunctionality
import com.ing.api.contacting.dto.resource.blacklist.BlacklistType.BlacklistType

import java.time.LocalDateTime

case class BlacklistItemVO(id: Option[Long],
                           functionality: BlacklistFunctionality,
                           entityType: BlacklistType,
                           value: String,
                           startDate: LocalDateTime,
                           endDate: Option[LocalDateTime])
