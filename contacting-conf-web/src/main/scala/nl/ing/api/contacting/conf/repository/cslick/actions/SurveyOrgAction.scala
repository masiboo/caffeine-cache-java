package nl.ing.api.contacting.conf.repository.cslick.actions

import nl.ing.api.contacting.conf.modules.ExecutionContextConfig.executionContext
import nl.ing.api.contacting.conf.repository.cslick.CoCoDBComponent
import nl.ing.api.contacting.conf.repository.model.SurveyOrgMapping
import nl.ing.api.contacting.repository.cslick.Lens
import nl.ing.api.contacting.repository.cslick.actions.OrganisationAction
import nl.ing.api.contacting.repository.cslick.actions.core.SlickActions
import slick.ast.BaseTypedType

/**
 * @author Ayush Mittal
 */
class SurveyOrgAction(val dBComponent: CoCoDBComponent, val surveySettingAction: SurveySettingAction, val orgAction: OrganisationAction) extends SlickActions[SurveyOrgMapping,Long]{

  override val jdbcProfile = dBComponent.driver

  import jdbcProfile.api._

  class SurveyOrgTable(tag: Tag) extends Table[SurveyOrgMapping](tag, "SURVEY_ORG_MAPPING") {

    def id = column[Long]("ID", O.PrimaryKey, O.AutoInc)

    def surveyId = column[Long]("SURVEY_ID")

    def orgId = column[Long]("ORG_ID")

    def idx =
      index("SurveyOrg_VALUE", (surveyId, orgId), unique = true)

    def surveyForeignKey =
      foreignKey(
        "FK_SURVEY_ORG_SURVEY_ID",
        surveyId,
        surveySettingAction.tableQuery)(_.id, onDelete = ForeignKeyAction.Cascade)

    def orgForeignKey =
      foreignKey(
        "FK_SURVEY_ORG_ORG_ID",
        orgId,
        orgAction.tableQuery)(_.id, onDelete = ForeignKeyAction.NoAction)

    override def * = (id.?, surveyId, orgId).mapTo[SurveyOrgMapping]
  }

  override protected implicit val btt: BaseTypedType[Long] = jdbcProfile.api.longColumnType

  override type EntityTable = SurveyOrgTable

  override def tableQuery: jdbcProfile.api.TableQuery[SurveyOrgTable] = TableQuery[SurveyOrgTable]

  override def primaryColumn(table: SurveyOrgTable): jdbcProfile.api.Rep[Long] = table.id

  override def idLens: Lens[SurveyOrgMapping, Option[Long]] = Lens.lens[SurveyOrgMapping, Option[Long]] { _.id } {
    (vo, id1) =>
      vo.copy(id = id1)
  }

  private def findBySurveyIdCompiled(surveyId: Rep[Long]) = Compiled {
    tableQuery.filter(_.surveyId === surveyId)
  }

  def findBySurveyId(surveyId: Long) = {
    (
      for {
        orgMapping <- tableQuery
        org <- orgMapping.orgForeignKey
      } yield (orgMapping,org)
    ).filter{
      row => row._1.surveyId === surveyId
    }.joinLeft(orgAction.tableQuery).on(_._2.parentId === _.id).joinLeft(orgAction.tableQuery).on(_._2.flatMap(_.parentId) === _.id).result

  }


  def addRemoveOrgs(surveyId: Long, orgsToAdd: List[Long], orgsToRemove: List[Long]) = {
    DBIO.sequence(List(addOrgs(surveyId,orgsToAdd),removeOrgs(surveyId, orgsToRemove))).transactionally
  }

  private def removeOrgs(surveyId: Long, orgsToRemove: List[Long]) = {
    tableQuery.filter(_.surveyId === surveyId).filter(_.orgId.inSet(orgsToRemove)).delete
  }

  private def addOrgs(surveyId: Long, orgsToAdd: List[Long]) = {
    save(orgsToAdd.map(orgId => SurveyOrgMapping(None, surveyId, orgId)))
  }

}
