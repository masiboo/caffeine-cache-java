package nl.ing.api.contacting.conf.repository.cslick.actions.core.audit

import com.ing.api.contacting.dto.audit.AuditEntity
import com.ing.api.contacting.dto.audit.AuditInfo
import com.ing.api.contacting.dto.audit._
import com.ing.api.contacting.dto.context.ContactingContext
import nl.ing.api.contacting.repository.cslick.actions.core.audit.AuditEntityTable
import nl.ing.api.contacting.util.Json

import java.sql.Timestamp
import java.time.Instant
import java.util.Date
import scala.concurrent.ExecutionContext

/**
 * @author Ayush Mittal
 */
trait SlickAuditActions[Entity <: AnyRef, ID] extends AuditEntityTable {
  import jdbcProfile.api._

  private type AuditedRow = (AuditInfo, AuditEntity)
  private type AuditedTable = (AuditInfoTable, AuditEntityTable)

  val _tag: String

  def _id(entity: Entity): Option[ID]

  protected def auditInsert(insertingEntity: DBIO[Entity], context: ContactingContext)(
      implicit ex: ExecutionContext): DBIO[Entity] = {
    (for {
      insertedEntity <- insertingEntity
      _ <- audit(ADD, context, Option(insertedEntity))
    } yield insertedEntity).transactionally
  }

  protected def auditUpdate(updatingEntity: DBIO[Entity], context: ContactingContext)(
      implicit ex: ExecutionContext): DBIO[Entity] = {

    (for {
      updatedEntity <- updatingEntity
      _ <- audit(UPDATE, context, Option(updatedEntity))
    } yield updatedEntity).transactionally
  }

  protected def auditDelete(id: ID, deleteAction: DBIO[Int], context: ContactingContext)(
      implicit ex: ExecutionContext): DBIO[Int] = {

    (for {
      result <- deleteAction
      _ <- audit(DELETE, context, None, Option(id))
    } yield result).transactionally
  }

  protected def audit(auditType: AuditType,
                      context: ContactingContext,
                      entity: Option[Entity] = None,
                      entityId: Option[ID] = None)(implicit ex: ExecutionContext): DBIO[AuditedRow] = {
    for {
      auditInfo <- createAuditInfo(context)
      auditEntity <- createAuditEntity(entity, entityId, auditInfo.id.get, auditType, context)
    } yield (auditInfo, auditEntity)
  }

  def getAllAuditedVersions(accountId: Option[Long], numRows: Int = 21, context: ContactingContext)(
      implicit ex: ExecutionContext,
      m1: Manifest[ID],
      m2: Manifest[Entity]): DBIO[Seq[AuditedEntity[Entity, ID]]] = {
    result((for {
      auditedEntity <- AuditEntityTable.filter(_.accountId === accountId).filter(_.entityType === _tag)
      auditedInfo <- AuditInfoTable if auditedInfo.id === auditedEntity.revId
    } yield (auditedInfo, auditedEntity)).sortBy(_._1.id.desc).take(numRows))
  }

  def getAuditedVersions(entityId: ID, numRows: Int = 21)(implicit ex: ExecutionContext,
                                                          m1: Manifest[ID],
                                                          m2: Manifest[Entity]): DBIO[Seq[AuditedEntity[Entity, ID]]] = {
    result((for {
      auditedEntity <- AuditEntityTable.filter(_.entityType === _tag).filter(_.entityId === entityId.toString)
      auditedInfo <- AuditInfoTable if auditedInfo.id === auditedEntity.revId
    } yield (auditedInfo, auditedEntity)).sortBy(_._1.id.desc).take(numRows))
  }

  protected def createAuditInfo(context: ContactingContext)(implicit ex: ExecutionContext): DBIO[AuditInfo] = {
    context.auditContext.auditInfo match {
      case None =>
        insertReturningAuditInfoId +=
          AuditInfo(None,
                    context.auditContext.modifiedBy,
                    context.auditContext.modifiedTime.getOrElse(Timestamp.from(Instant.now())))
      case Some(info) => DBIO.successful(info)
    }
  }

  private def createAuditEntity(entity: Option[Entity],
                                entityId: Option[ID],
                                auditId: Long,
                                auditType: AuditType,
                                context: ContactingContext)(implicit ex: ExecutionContext): DBIO[AuditEntity] = {
    insertReturningAuditEntityId +=
      AuditEntity(None,
                  auditId,
                  context.auditContext.accountId,
                  auditType,
                  _tag,
                  entity.flatMap(_id).getOrElse(entityId.get).toString,
                  entity.map(e => Json.toJsonString(e)))
  }

  private lazy val insertReturningAuditInfoId: jdbcProfile.IntoInsertActionComposer[AuditInfo, AuditInfo] =
    AuditInfoTable.returning(AuditInfoTable.map(_.id)).into((table, id) => table.copy(id = Option(id)))

  private lazy val insertReturningAuditEntityId: jdbcProfile.IntoInsertActionComposer[AuditEntity, AuditEntity] =
    AuditEntityTable.returning(AuditEntityTable.map(_.id)).into((table, id) => table.copy(id = Option(id)))

  protected def getAuditType(entity: Option[Entity]): AuditType =
    entity
      .map(_id(_) match {
        case None => ADD
        case _    => UPDATE
      })
      .getOrElse(DELETE)

  private def result(query: Query[AuditedTable, AuditedRow, Seq])(
      implicit ex: ExecutionContext,
      m1: Manifest[ID],
      m2: Manifest[Entity]): DBIO[Seq[AuditedEntity[Entity, ID]]] =
    query.result.map(_.map(toAuditedEntity))

  private def toAuditedEntity(implicit m1: Manifest[ID], m2: Manifest[Entity]): AuditedRow => AuditedEntity[Entity, ID] = {
    case (info, audit) =>
      AuditedEntity[Entity, ID](
        audit.id,
        audit.revId,
        audit.accountId,
        audit.auditType,
        Json.as[ID](audit.entityId),
        audit.entityJson.map(str => Json.as[Entity](str)),
        audit.entityJsonData,
        info.modifiedBy,
        new Date(info.modifiedTime.getTime)
      )
  }
}
