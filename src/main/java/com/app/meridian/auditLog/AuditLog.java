package com.app.meridian.auditLog;

import com.app.meridian.common.BaseEntity;
import com.app.meridian.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(name = "audit_logs")
public class AuditLog extends BaseEntity {

    @NotNull
    @Column(name = "action", nullable = false)
    private String action;

    @NotNull
    @Column(name = "entity_type", nullable = false)
    private String entityType;

    @Column(name = "entity_id", nullable = true)
    private Long entityId;

    @Column(name = "summary", nullable = true, columnDefinition = "TEXT")
    private String summary;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "actor_id", nullable = true, foreignKey = @ForeignKey(name = "fk_audit_logs_actor"))
    @OnDelete(action = OnDeleteAction.SET_NULL)
    private User actor;

}
