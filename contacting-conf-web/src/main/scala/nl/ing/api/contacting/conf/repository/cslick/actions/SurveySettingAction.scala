package nl.ing.api.contacting.conf.repository.cslick.actions

import nl.ing.api.contacting.conf.repository.cslick.CoCoDBComponent
import nl.ing.api.contacting.conf.repository.model.{SurveyPhoneNumberFormat, SurveySetting, SurveySettingOptions}
import nl.ing.api.contacting.conf.surveytrigger.ContactingSurveyTriggerEvent
import nl.ing.api.contacting.domain.slick.AccountVO
import nl.ing.api.contacting.repository.cslick.Lens
import nl.ing.api.contacting.repository.cslick.actions.{AccountAction, WorkerOrganisationAction}
import nl.ing.api.contacting.repository.cslick.actions.core.ContactingSlickActions
import slick.ast.BaseTypedType

import scala.concurrent.Future

/**
 * @author Ayush Mittal
 */
class SurveySettingAction(val dBComponent: CoCoDBComponent) extends ContactingSlickActions[SurveySetting, Long] {

  override val jdbcProfile = dBComponent.driver

  import jdbcProfile.api._
  import nl.ing.api.contacting.conf.domain.SurveyConstants._

  class SurveySettingsTable(tag: Tag) extends Table[SurveySetting](tag, "SURVEY_SETTINGS") {

    def id = column[Long]("ID", O.PrimaryKey, O.AutoInc)

    def name = column[String]("NAME")

    def channel = column[String]("CHANNEL")

    def channelDirection = column[String]("CHANNEL_DIRECTION")

    def voiceSurveyId = column[String]("VOICE_SURVEY_ID")

    def callflowName = column[Option[String]]("CALLFLOW_NAME")

    def minFrequency = column[Option[Int]]("MIN_FREQUENCY")

    def delay = column[Option[Long]]("DELAY")

    def surveyOfferRatio = column[Option[Float]]("SURVEY_OFFER_RATIO")

    def minContactLength = column[Option[Long]]("MIN_CONTACT_LENGTH")

    def surveyForTransfers = column[Boolean]("SURVEY_FOR_TRANSFERS")

    def accountId = column[Long]("ACCOUNT_ID")

    def idx =
      index("SurveyNameAccountId_VALUE", (name, accountId), unique = true)

    override def * = (id.?, accountId, name, channel, channelDirection, voiceSurveyId, callflowName, minFrequency, delay,
      surveyOfferRatio, minContactLength, surveyForTransfers).mapTo[SurveySetting]
  }

  override type EntityTable = SurveySettingsTable

  override def tableQuery = TableQuery[SurveySettingsTable]

  override def primaryColumn(table: SurveySettingsTable) = table.id

  implicit override protected val btt: BaseTypedType[Long] =
    jdbcProfile.api.longColumnType

  override def idLens: Lens[SurveySetting, Option[Long]] =
    Lens.lens[SurveySetting, Option[Long]] { _.id } {
      (vo, id1) =>
        vo.copy(id = id1)
    }

  override def contextIdColumn(tableQuery: SurveySettingsTable): jdbcProfile.api.Rep[Long] =
    tableQuery.accountId
}

/**
 * SurveySettingQueries has been introduced to avoid circular dependency problem formed by the different actions required by the queries.
 * @param dBComponent
 * @param surveyTaskQAction
 * @param surveyPhNumberFormatAction
 * @param surveyOrgAction
 * @param workerOrganisationAction
 * @param accountAction
 */
class SurveySettingQueries(val dBComponent: CoCoDBComponent, val surveyTaskQAction: SurveyTaskQAction, val surveyPhNumberFormatAction: SurveyPhNumberFormatAction,
                           val surveyOrgAction: SurveyOrgAction, val workerOrganisationAction: WorkerOrganisationAction, val accountAction: AccountAction) {
  val surveySettingsAction = surveyTaskQAction.surveySettingAction
  import surveySettingsAction.jdbcProfile.api._
  import nl.ing.api.contacting.conf.domain.SurveyConstants._

  def inboundSurveyQuery(callDetail: ContactingSurveyTriggerEvent) = {
    surveyTaskQAction.taskQAction.tableQuery.join(surveyTaskQAction.tableQuery).on(_.id === _.taskQId)
      .join(surveyTaskQAction.surveySettingAction.tableQuery).on(_._2.surveyId === _.id)
      .filter {
        case ((taskQueueTable, surveyTaskQTable), surveySettingTable) =>
          taskQueueTable.sid === callDetail.taskQueueSid && (surveySettingTable.channelDirection.toUpperCase === CHANNEL_DIRECTION_INBOUND ||
            surveySettingTable.channelDirection.toUpperCase === CHANNEL_DIRECTION_INBOUND_OUTBOUND) && surveySettingTable.channel.toUpperCase === CHANNEL_CALL
      }.join(accountAction.tableQuery).on(_._2.accountId === _.id).join(surveyPhNumberFormatAction.tableQuery).on(_._1._2.id === _.surveyId)
      .map(allT => (allT._1._1._2, allT._1._2, allT._2))
  }

  private def orgsForWorkerQuery(workerSid: String) = {
    workerOrganisationAction.workerAction.tableQuery.filter(_.sid === workerSid)
      .join(workerOrganisationAction.tableQuery.filter(_.preferred === 1)).on(_.id === _.workerId).join(workerOrganisationAction.organisationAction.tableQuery).on{
      case ((workerTable, workerOrganisationTable), organisationTable) => workerOrganisationTable.organisationId === organisationTable.id
    }.joinLeft(workerOrganisationAction.organisationAction.tableQuery).on{
      case (((workerTable, workerOrganisationTable), wOrganisationTable), parentOrganisationTable) =>
        wOrganisationTable.parentId === parentOrganisationTable.id
    }.joinLeft(workerOrganisationAction.organisationAction.tableQuery).on{
      case ((((workerTable, workerOrganisationTable), wOrganisationTable), parentOrganisationTable), superParentOrgTable) =>
        parentOrganisationTable.flatMap(_.parentId) === superParentOrgTable.id
    }
  }

  val outboundCallSurveySettingQuery = surveySettingsAction.tableQuery.filter {
    case surveySettingTable =>
      (surveySettingTable.channelDirection.toUpperCase === CHANNEL_DIRECTION_OUTBOUND ||
        surveySettingTable.channelDirection.toUpperCase === CHANNEL_DIRECTION_INBOUND_OUTBOUND) && surveySettingTable.channel.toUpperCase === CHANNEL_CALL
  }

  def outboundSurveyQuery(callDetail: ContactingSurveyTriggerEvent) = {
    import workerOrganisationAction.organisationAction.jdbcProfile.api._

    val surveySettingsWithAccAndPhNumFmtQuery = outboundCallSurveySettingQuery.join(surveyOrgAction.tableQuery).on(_.id === _.surveyId).join(accountAction.tableQuery)
      .on(_._1.accountId === _.id).join(surveyPhNumberFormatAction.tableQuery).on(_._1._1.id === _.surveyId)

    surveySettingsWithAccAndPhNumFmtQuery.joinRight(orgsForWorkerQuery(callDetail.workerSid)).on {
      case ((((surveySettingsTable, surveyOrgTable), accountVo), phFormats), ((((workerTable, workerOrganisationTable), wOrganisationTable), parentWOrgTable), superParentWOrgTable)) =>
        surveyOrgTable.orgId === wOrganisationTable.id || surveyOrgTable.orgId === wOrganisationTable.parentId || surveyOrgTable.orgId === superParentWOrgTable.flatMap(_.id)
    }.map(settingsRep => settingsRep._1.map(settings => (settings._1._1._1, settings._1._2, settings._2)))
  }

  def getSurverySettingsByNameQuery(name: String, accountSid: String): Query[(surveySettingsAction.SurveySettingsTable, accountAction.AccountTable, surveyPhNumberFormatAction.SurveyPhNumberFormatTable), (SurveySetting, AccountVO, SurveyPhoneNumberFormat), Seq] = {
    surveySettingsAction.tableQuery.filter(_.name === name).join(accountAction.tableQuery.filter(_.sid === accountSid)).on {
      case (surveySettingsTable, accountTable) =>
        surveySettingsTable.name === name && accountTable.id === surveySettingsTable.accountId
    }.join(surveyPhNumberFormatAction.tableQuery).on(_._1.id === _.surveyId).map(res => (res._1._1, res._1._2, res._2))
  }
}
