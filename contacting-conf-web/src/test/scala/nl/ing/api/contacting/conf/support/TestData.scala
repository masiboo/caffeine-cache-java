package nl.ing.api.contacting.conf.support

import com.ing.api.contacting.dto.resource.{AttributeDto, ConnectionType}
import com.ing.api.contacting.dto.resource.ConnectionType.{Fallback, Primary, WorkFromHome}
import com.ing.api.contacting.dto.resource.Layer.{Backend, Frontend}
import com.ing.api.contacting.dto.resource.account.AccountDto
import eu.timepit.refined.auto._
import nl.ing.api.contacting.conf.business.callflow.CallFlowTriggerAndProcessor.CallFlowRequestData
import nl.ing.api.contacting.conf.domain._
import nl.ing.api.contacting.conf.repository.model.SurveySetting
import nl.ing.api.contacting.conf.resource.dto.SurveySettingDTO
import nl.ing.api.contacting.repository.model.OrganisationLevelEnumeration.{CIRCLE, CLT, SUPER_CIRCLE}
import nl.ing.api.contacting.repository.model.{OrganisationLevelEnumeration, OrganisationModel, TaskQueueModel}

import java.time.ZonedDateTime
/**
 * @author Ayush Mittal
 */
trait TestData extends AccountSupport {

  val attributeDto = AttributeDto(Some(1), "entity", "entityValue", "label", "labelValue", Some("labelContent"))

  val superCircle1OrganisationModel = OrganisationModel(Some(1l),"sc1",account.id, None, OrganisationLevelEnumeration.SUPER_CIRCLE)
  val superCircle2OrganisationModel = OrganisationModel(Some(2l),"sc2",account.id, None, OrganisationLevelEnumeration.SUPER_CIRCLE)

  val circle1OrganisationModel = OrganisationModel(Some(3l),"c1",account.id, superCircle1OrganisationModel.id, OrganisationLevelEnumeration.CIRCLE)

  val team1OrganisationModel = OrganisationModel(Some(4l),"t1",account.id, circle1OrganisationModel.id, OrganisationLevelEnumeration.CLT)

  val taskQueueModel = TaskQueueModel(Some(1l),"1==1","tq",Some("sid"),None, true, workspaceId = 1l, "someone", ZonedDateTime.now())
  val taskQueueModel1 = TaskQueueModel(Some(2l),"2==2","tq2",Some("sid2"),None, true, workspaceId = 1l, "someone", ZonedDateTime.now())

  val surveySettingModel = SurveySetting(Some(1l), 1l, "setting1", "call", "Inbound", "voiceId", Some("cfName"), Some(1),
    Some(1l),Some(80.0F),Some(5l), true)

  val surveySettingVO = SurveySettingVO(Some(1l), 1l, "setting1", "call", "Inbound", "voiceId", Some("cfName"), Some(1),
    Some(1l),Some(80.0F),Some(5l), true)

  val surveySettingVOHtmlEscaped = SurveySettingVO(Some(1l), 1l, "setting1", "call &lt;script&gt;evil_hacker_script()&lt;/script&gt;;", "Inbound &lt;script&gt;evil_hacker_script()&lt;/script&gt;;", "voiceId &lt;script&gt;evil_hacker_script()&lt;/script&gt;;", Some("cfName &lt;script&gt;evil_hacker_script()&lt;/script&gt;;"), Some(1),
    Some(1l),Some(80.0F),Some(5l), true)

  val surveySettingDTO = SurveySettingDTO(Some(1l), "setting1", "call", "Inbound", "voiceId", Some("cfName"), Some(1),
    Some(1l),Some(80.0F),Some(5l), true)

  val surveySettingDtoWithXSSInjection = SurveySettingDTO(Some(1l), "setting1", "call <script>evil_hacker_script()</script>;", "Inbound <script>evil_hacker_script()</script>;", "voiceId <script>evil_hacker_script()</script>;", Some("cfName <script>evil_hacker_script()</script>;"), Some(1),
    Some(1l),Some(80.0F),Some(5l), true)

  val superCircle1: OrganisationModel =
    OrganisationModel(id = Some(1), name = "superCircle1", accountId = 1, parentId = None, organisationLevel = SUPER_CIRCLE)

  val circle1: OrganisationModel = OrganisationModel(id = Some(2),
                                                     name = "circle1",
                                                     accountId = 1,
                                                     parentId = superCircle1.id,
                                                     organisationLevel = CIRCLE)

  val superCircle2: OrganisationModel =
    OrganisationModel(id = Some(5), name = "superCircle2", accountId = 1, parentId = None, organisationLevel = SUPER_CIRCLE)

  val circle2: OrganisationModel = OrganisationModel(id = Some(6),
                                                     name = "circle2",
                                                     accountId = 1,
                                                     parentId = (superCircle2.id),
                                                     organisationLevel = CIRCLE)
  val circle3: OrganisationModel = OrganisationModel(id = Some(7),
                                                     name = "circle3",
                                                     accountId = 1,
                                                     parentId = (superCircle2.id),
                                                     organisationLevel = CIRCLE)

  val team1: OrganisationModel =
    OrganisationModel(id = Some(3), name = "team1", accountId = 1, parentId = (circle1.id), organisationLevel = CLT)

  val team1_2: OrganisationModel =
    OrganisationModel(id = Some(10), name = "team1_2", accountId = 1, parentId = (circle1.id), organisationLevel = CLT)

  val team2: OrganisationModel =
    OrganisationModel(id = Some(4), name = "team2", accountId = 1, parentId = (circle2.id), organisationLevel = CLT)

  val team3: OrganisationModel =
    OrganisationModel(id = Some(11), name = "team3", accountId = 1, parentId = (circle1.id), organisationLevel = CLT)

  val team4: OrganisationModel =
    OrganisationModel(id = Some(12), name = "team4", accountId = 1, parentId = (circle2.id), organisationLevel = CLT)

  val team5: OrganisationModel =
    OrganisationModel(id = Some(14), name = "team5", accountId = 1, parentId = (circle3.id), organisationLevel = CLT)

  val requestData : CallFlowRequestData = CallFlowRequestData(accountFriendlyName = "NL-tst", callFlowName = "zapping", cjid = "1234-cjid", surveyNameInTheVoice = "voiceSurvey", surveySettingFriendlyName = "survey-setting-name", telephoneNumber = "0620787")
}
