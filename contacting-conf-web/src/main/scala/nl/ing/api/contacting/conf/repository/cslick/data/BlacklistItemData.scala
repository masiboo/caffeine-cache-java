package nl.ing.api.contacting.conf.repository.cslick.data

import com.ing.api.contacting.dto.resource.blacklist.{BlacklistFunctionality, BlacklistType}
import nl.ing.api.contacting.conf.domain.BlacklistItemVO
import nl.ing.api.contacting.domain.ContactingDomainVO

import java.time.LocalDateTime

object BlacklistItemConverter {

  implicit class BlacklistItemVOExtension(blacklistItem: BlacklistItemVO) {
    def toData(accountId: Long): BlacklistItemData = {
      BlacklistItemData(
        blacklistItem.id,
        blacklistItem.functionality.toString,
        blacklistItem.entityType.toString,
        blacklistItem.value,
        blacklistItem.startDate,
        blacklistItem.endDate,
        accountId
      )
    }
  }

  implicit class BlacklistItemDataExtension(blacklistItemData: BlacklistItemData) {
    def toVo: BlacklistItemVO = {
      BlacklistItemVO(
        id = blacklistItemData.id,
        functionality = BlacklistFunctionality.withName(blacklistItemData.functionality),
        entityType = BlacklistType.withName(blacklistItemData.entityType),
        value = blacklistItemData.value,
        startDate = blacklistItemData.startDate,
        endDate = blacklistItemData.endDate
      )
    }
  }

}

case class BlacklistItemData(id: Option[Long],
                             functionality: String,
                             entityType: String,
                             value: String,
                             startDate: LocalDateTime,
                             endDate: Option[LocalDateTime],
                             accountId: Long) extends ContactingDomainVO[Long]
