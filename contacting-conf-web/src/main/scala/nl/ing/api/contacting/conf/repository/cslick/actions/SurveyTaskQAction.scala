package nl.ing.api.contacting.conf.repository.cslick.actions

import nl.ing.api.contacting.conf.modules.ExecutionContextConfig.executionContext
import nl.ing.api.contacting.conf.repository.cslick.CoCoDBComponent
import nl.ing.api.contacting.conf.repository.model.SurveyTaskQMapping
import nl.ing.api.contacting.repository.cslick.Lens
import nl.ing.api.contacting.repository.cslick.actions.TaskQueueAction
import nl.ing.api.contacting.repository.cslick.actions.core.SlickActions
import slick.ast.BaseTypedType

/**
 * @author Ayush Mittal
 */
class SurveyTaskQAction(val dBComponent: CoCoDBComponent, val surveySettingAction: SurveySettingAction, val taskQAction: TaskQueueAction) extends SlickActions[SurveyTaskQMapping,Long]{

  override val jdbcProfile = dBComponent.driver

  import jdbcProfile.api._

  class SurveyTaskQTable(tag: Tag) extends Table[SurveyTaskQMapping](tag, "SURVEY_TASKQ_MAPPING") {

    def id = column[Long]("ID", O.PrimaryKey, O.AutoInc)

    def surveyId = column[Long]("SURVEY_ID")

    def taskQId = column[Long]("TASKQUEUE_ID")

    def idx =
      index("SurveyTaskQ_VALUE", (surveyId, taskQId), unique = true)

    def surveyForeignKey =
      foreignKey(
        "FK_SURVEY_TASKQ_SURVEY_ID",
        surveyId,
        surveySettingAction.tableQuery)(_.id, onDelete = ForeignKeyAction.Cascade)

    def taskQForeignKey =
      foreignKey(
        "FK_SURVEY_TASKQ_TASKQ_ID",
        taskQId,
        taskQAction.tableQuery)(_.id, onDelete = ForeignKeyAction.NoAction)

    override def * = (id.?, surveyId, taskQId).mapTo[SurveyTaskQMapping]
  }

  override protected implicit val btt: BaseTypedType[Long] = jdbcProfile.api.longColumnType

  override type EntityTable = SurveyTaskQTable

  override def tableQuery: jdbcProfile.api.TableQuery[SurveyTaskQTable] = TableQuery[SurveyTaskQTable]

  override def primaryColumn(table: SurveyTaskQTable): jdbcProfile.api.Rep[Long] = table.id

  override def idLens: Lens[SurveyTaskQMapping, Option[Long]] = Lens.lens[SurveyTaskQMapping, Option[Long]] { _.id } {
    (vo, id1) =>
      vo.copy(id = id1)
  }

  def findBySurveyId(surveyId: Long) = {
    (for {
      taskQMapping <- tableQuery
      taskQueue <- taskQMapping.taskQForeignKey
    } yield (taskQMapping,taskQueue)).filter{
      case (surveyTaskQ,_) => surveyTaskQ.surveyId === surveyId
    }.map{
      case (surveyTaskQ,taskQ) => (surveyTaskQ,taskQ.friendlyName)
    }.result
  }

  def addRemoveTaskQueues(surveyId: Long, taskQueuesToAdd: List[Long], taskQueuesToRemove: List[Long]) = {
    DBIO.sequence(List(addTaskQueues(surveyId,taskQueuesToAdd),removeTaskQueues(surveyId, taskQueuesToRemove))).transactionally
  }

  private def removeTaskQueues(surveyId: Long, taskQueuesToRemove: List[Long]) = {
    tableQuery.filter(r => r.surveyId === surveyId && r.taskQId.inSet(taskQueuesToRemove)).delete
  }

  private def addTaskQueues(surveyId: Long, taskQueuesToAdd: List[Long]) = {
    save(taskQueuesToAdd.map(taskQId => SurveyTaskQMapping(None, surveyId, taskQId)))
  }

}
