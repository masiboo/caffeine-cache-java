package nl.ing.api.contacting.conf.repository.support;

import com.ing.api.contacting.dto.java.context.ContactingContext;
import nl.ing.api.contacting.java.repository.jpa.audit.AuditEntityActions;
import org.springframework.data.jpa.repository.JpaRepository;

public abstract class AuditAwareRepository<T, ID> {

    protected final JpaRepository<T, ID> jpaRepository;
    protected final AuditEntityActions<T, ID> auditActions;

    protected AuditAwareRepository(JpaRepository<T, ID> jpaRepository,
                                   AuditEntityActions<T, ID> auditActions) {
        this.jpaRepository = jpaRepository;
        this.auditActions = auditActions;
    }


    public T saveAndAudit(T entity, ContactingContext context) {
        T saved = jpaRepository.save(entity);
        auditActions.auditInsert(saved, context);
        return saved;
    }


    public T updateAndAudit(T entity, ContactingContext context) {
        T updated = jpaRepository.save(entity);
        auditActions.auditUpdate(updated, context);
        return updated;
    }


    public void deleteByIdAndAudit(ID id, ContactingContext context) {
        jpaRepository.deleteById(id);
        auditActions.auditDelete(id, 1,  context);
    }

}
