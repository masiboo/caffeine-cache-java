package nl.ing.api.contacting.conf.mapper

import com.ing.api.contacting.dto.resource.blacklist.BlacklistItemDto
import nl.ing.api.contacting.conf.domain.BlacklistItemVO
import nl.ing.api.contacting.shared.util.TimeConversions
import org.springframework.web.util.HtmlUtils

object BlacklistItemMapper {
  implicit class BlacklistItemExtension(blacklistItemVO: BlacklistItemVO) {
    def toDto: BlacklistItemDto = {
      BlacklistItemDto(
        id = blacklistItemVO.id,
        functionality = blacklistItemVO.functionality,
        entityType = blacklistItemVO.entityType,
        value = blacklistItemVO.value,
        startDate = TimeConversions.localDateTimeToString(blacklistItemVO.startDate),
        endDate = blacklistItemVO.endDate.map(TimeConversions.localDateTimeToString)
      )
    }
  }

  implicit class BlacklistItemDtoExtension(blacklistItemDto: BlacklistItemDto) {
    def toVo: BlacklistItemVO = {
      BlacklistItemVO(
        id = blacklistItemDto.id,
        functionality = blacklistItemDto.functionality,
        entityType = blacklistItemDto.entityType,
        value = HtmlUtils.htmlEscape(blacklistItemDto.value),
        startDate = TimeConversions.stringToLocalDateTime(blacklistItemDto.startDate),
        endDate = blacklistItemDto.endDate.map(TimeConversions.stringToLocalDateTime)
      )
    }
  }
}
