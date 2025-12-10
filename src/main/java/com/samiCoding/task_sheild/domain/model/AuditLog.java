package com.samiCoding.task_sheild.domain.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Audit log entity for tracking system activities
 */
@Entity
@Table(name = "audit_logs", indexes = {
        @Index(name = "idx_audit_user", columnList = "user_id"),
        @Index(name = "idx_audit_action", columnList = "action"),
        @Index(name = "idx_audit_created", columnList = "created_at")
}, schema = "tasksheild")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false, length = 100)
    private String action;

    @Column(name = "resource_type", length = 50)
    private String resourceType;

    @Column(name = "resource_id")
    private UUID resourceId;

    @Column(name = "ip_address", length = 45) // IPv6 can be 45 chars
    private String ipAddress;

    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;

    @Column(name = "details", columnDefinition = "TEXT")
    private String details;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Lifecycle callback - runs before entity is persisted
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    /**
     * Business method: Get human-readable audit description
     */
    public String getAuditDescription() {
        StringBuilder sb = new StringBuilder();

        if (user != null) {
            sb.append("User: ").append(user.getEmail()).append(" ");
        } else {
            sb.append("System ");
        }

        sb.append("performed action: ").append(action);

        if (resourceType != null) {
            sb.append(" on ").append(resourceType);
            if (resourceId != null) {
                sb.append(" (ID: ").append(resourceId).append(")");
            }
        }

        sb.append(" at ").append(createdAt);

        if (ipAddress != null) {
            sb.append(" from IP: ").append(ipAddress);
        }

        return sb.toString();
    }

    @Override
    public String toString() {
        return getAuditDescription();
    }
}
