package nl.ing.api.contacting.conf.domain.model.permission;

import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.UserDefinedType;

@UserDefinedType("organisational_restriction")
public record OrganisationalRestriction(
        @Column("clt_id") Integer cltId,
        @Column("clt_name") String cltName,
        @Column("circle_id") Integer circleId,
        @Column("circle_name") String circleName,
        @Column("super_circle_id") Integer superCircleId,
        @Column("super_circle_name") String superCircleName,
        @Column("preferred") boolean preferred
) {}