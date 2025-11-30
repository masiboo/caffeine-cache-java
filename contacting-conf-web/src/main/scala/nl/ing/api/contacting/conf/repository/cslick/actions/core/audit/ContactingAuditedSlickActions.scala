package nl.ing.api.contacting.conf.repository.cslick.actions.core.audit

import com.ing.api.contacting.dto.context.ContactingContext
import nl.ing.api.contacting.repository.cslick.actions.core.ContactingSlickActions

import scala.concurrent.ExecutionContext

/**
 * @author Ayush Mittal
 */
trait ContactingAuditedSlickActions[Entity <: AnyRef, Id]
    extends SlickAuditActions[Entity, Id] with ContactingSlickActions[Entity, Id] {

  import jdbcProfile.api._

  /**
   * Saves the passed entity with audit
   * Creates new entity
   * @param entity entity to be saved
   * @param contactingContext the contacting context
   * @param exc the execution context
   * @return DBIO action on saved/updated entity
   */
  def auditSave(entity: Entity, contactingContext: ContactingContext)(implicit exc: ExecutionContext): DBIO[Entity] = {
    auditInsert(save(entity, contactingContext.accountId), contactingContext)
  }

  /**
   * Saves the passed entities with audit
   * Creates new if entities id does not exist
   * Else updates existing entity
   * @param entities entity to be saved
   * @param contactingContext the contacting context
   * @param exc the execution context
   * @return
   */
  def auditSave(entities: Seq[Entity], contactingContext: ContactingContext)(
      implicit exc: ExecutionContext): DBIO[Seq[Entity]] = {
    val action = entities.map {
      entity =>
        auditSave(entity, contactingContext)
    }
    DBIO.sequence(action).transactionally
  }

  /**
   * Saves the passed entity with audit
   * Creates entity
   * @param entity entity to be saved
   * @param contactingContext the contacting context
   * @param exc the execution context
   * @return DBIO action on saved/updated entity
   */
  def auditSaveOrUpdate(entity: Entity, contactingContext: ContactingContext)(
      implicit exc: ExecutionContext): DBIO[Entity] = {
    auditInsert(saveOrUpdate(entity, contactingContext.accountId), contactingContext)
  }

  /**
   * Saves the passed entities with audit
   * Creates new if entities id does not exist
   * Else updates existing entity
   * @param entities entity to be saved
   * @param contactingContext the contacting context
   * @param exc the execution context
   * @return
   */
  def auditSaveOrUpdate(entities: Seq[Entity], contactingContext: ContactingContext)(
      implicit exc: ExecutionContext): DBIO[Seq[Entity]] = {
    val action = entities.map {
      entity =>
        auditSaveOrUpdate(entity, contactingContext)
    }
    DBIO.sequence(action).transactionally
  }

  /**
   * updates an entity with audit
   * @param entity entity to be updated
   * @param contactingContext the contacting context
   * @param exc the execution context
   * @return
   */
  def auditUpdate(entity: Entity, contactingContext: ContactingContext)(implicit exc: ExecutionContext): DBIO[Entity] = {
    auditUpdate(update(entity, contactingContext.accountId), contactingContext)
  }

  /**
   * updates an entities with audit
   * @param entities entity to be updated
   * @param contactingContext the contacting context
   * @param exc the execution context
   * @return
   */
  def auditUpdate(entities: Seq[Entity], contactingContext: ContactingContext)(
      implicit exc: ExecutionContext): DBIO[Seq[Entity]] = {
    val action = entities.map {
      entity =>
        auditUpdate(entity, contactingContext)
    }
    DBIO.sequence(action).transactionally
  }

  /**
   * delete entity and audit the delete action
   * @param id entity id to be delete
   * @param contactingContext the contacting context
   * @param exc the execution context
   * @return
   */
  def auditDeleteById(id: Id, contactingContext: ContactingContext)(implicit exc: ExecutionContext): DBIO[Int] = {
    auditDelete(id, deleteById(id, contactingContext.accountId), contactingContext)
  }

  /**
   * delete entity and audit the delete action
   * @param entity entity to be deleted
   * @param contactingContext the contacting context
   * @param exc the execution context
   * @return
   */
  def auditDelete(entity: Entity, contactingContext: ContactingContext)(implicit exc: ExecutionContext): DBIO[Int] = {
    tryExtractId(entity).flatMap {
      id =>
        auditDeleteById(id, contactingContext)
    }
  }
}
