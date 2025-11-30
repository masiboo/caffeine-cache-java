package nl.ing.api.contacting.conf.mapper

import com.ing.api.contacting.dto.resource.blacklist.BlacklistFunctionality
import com.ing.api.contacting.dto.resource.blacklist.BlacklistItemDto
import com.ing.api.contacting.dto.resource.blacklist.BlacklistType
import nl.ing.api.contacting.conf.BaseSpec
import nl.ing.api.contacting.conf.mapper.BlacklistItemMapper.BlacklistItemDtoExtension

class BlacklistItemMapperSpec extends BaseSpec {

  it should "html escape the XSS injected data" in {
    val blacklistItemDtoWithXSSInjection = BlacklistItemDto(Some(42),
      BlacklistFunctionality.CALL_ME_NOW,
      BlacklistType.PHONE_NUMBER,
      "+01-123456 <script>evil_hacker_script()</script>;",
      "2011-04-06T11:11:11",
      None)

    val vo = new BlacklistItemDtoExtension(blacklistItemDtoWithXSSInjection).toVo
    vo.id shouldBe Some(42)
    vo.value shouldBe "+01-123456 &lt;script&gt;evil_hacker_script()&lt;/script&gt;;"
  }

}
