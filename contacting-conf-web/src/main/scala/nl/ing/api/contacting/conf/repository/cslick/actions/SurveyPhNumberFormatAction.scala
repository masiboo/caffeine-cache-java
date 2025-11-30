package nl.ing.api.contacting.conf.repository.cslick.actions

import nl.ing.api.contacting.conf.modules.ExecutionContextConfig
import nl.ing.api.contacting.conf.repository.cslick.CoCoDBComponent
import nl.ing.api.contacting.conf.repository.model.SurveyPhoneNumberFormat
import nl.ing.api.contacting.repository.cslick.Lens
import nl.ing.api.contacting.repository.cslick.actions.core.SlickActions
import slick.ast.BaseTypedType


/**
 * @author Ayush Mittal
 */
class SurveyPhNumberFormatAction(val dBComponent: CoCoDBComponent, val surveySettingAction: SurveySettingAction) extends SlickActions[SurveyPhoneNumberFormat,Long] {

  override val jdbcProfile = dBComponent.driver

  import jdbcProfile.api._

  class SurveyPhNumberFormatTable(tag: Tag) extends Table[SurveyPhoneNumberFormat](tag, "SURVEY_PH_NUM_FORMAT") {

    def id = column[Long]("ID", O.PrimaryKey, O.AutoInc)

    def surveyId = column[Long]("SURVEY_ID")

    def format = column[String]("FORMAT")

    def direction = column[Boolean]("DIRECTION")

    def surveyForeignKey =
      foreignKey(
        "FK_SURVEY_FORMAT_SURVEY_ID",
        surveyId,
        surveySettingAction.tableQuery)(_.id, onDelete = ForeignKeyAction.Cascade)

    override def * = (id.?, surveyId, format,  direction).mapTo[SurveyPhoneNumberFormat]
  }

  override protected implicit val btt: BaseTypedType[Long] = jdbcProfile.api.longColumnType

  override type EntityTable = SurveyPhNumberFormatTable

  override def tableQuery: jdbcProfile.api.TableQuery[SurveyPhNumberFormatTable] = TableQuery[SurveyPhNumberFormatTable]

  override def primaryColumn(table: SurveyPhNumberFormatTable): jdbcProfile.api.Rep[Long] = table.id

  override def idLens: Lens[SurveyPhoneNumberFormat, Option[Long]] = Lens.lens[SurveyPhoneNumberFormat, Option[Long]] { _.id } {
    (vo, id1) =>
      vo.copy(id = id1)
  }

  private def findBySurveyIdCompiled(surveyId: Rep[Long]) = Compiled {
    tableQuery.filter(_.surveyId === surveyId)
  }

  def findBySurveyId(surveyId: Long) = {
    findBySurveyIdCompiled(surveyId).result
  }

  def addDeleteFormats(formatsAdded: Seq[SurveyPhoneNumberFormat], formatsRemoved: Seq[SurveyPhoneNumberFormat]) = {
    implicit val ec = ExecutionContextConfig.ioExecutionContext
    DBIO.sequence(Seq(save(formatsAdded), delete(formatsRemoved))).transactionally
  }
}
