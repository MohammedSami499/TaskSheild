package com.samiCoding.task_sheild.domain.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Task entity representing a task in the system
 */
@Entity
@Table(name = "tasks", indexes = {
        @Index(name = "idx_task_status", columnList = "status"),
        @Index(name = "idx_task_due_date", columnList = "due_date"),
        @Index(name = "idx_task_created_by", columnList = "created_by"),
        @Index(name = "idx_task_assigned_to", columnList = "assigned_to")
} , schema = "tasksheild")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotBlank
    @NotNull
    @NotEmpty
    @Column(nullable = false, length = 255)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private TaskStatus status = TaskStatus.TODO;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    @Builder.Default
    private TaskPriority priority = TaskPriority.MEDIUM;

    @Column(name = "due_date")
    private LocalDateTime dueDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to")
    private User assignedTo;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    /**
     * Lifecycle callback - runs before entity is persisted
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        validateTask();
        validate();
    }

    /**
     * Lifecycle callback - runs before entity is updated
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        validateTask();
        validate();

        // If status changed to DONE, set completed date
        if (status == TaskStatus.DONE && completedAt == null) {
            completedAt = LocalDateTime.now();
        }
    }

    /**
     * Business rule: Validate task data
     */
    private void validateTask() {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Task title cannot be null or empty");
        }
        if (createdBy == null) {
            throw new IllegalArgumentException("Task must have a creator");
        }
    }

    /**
     * Business method: Change task status with validation
     * @param newStatus The new status to set
     * @throws IllegalStateException if transition is not allowed
     */
    public void changeStatus(TaskStatus newStatus) {
        if (!status.canTransitionTo(newStatus)) {
            throw new IllegalStateException(
                    String.format("Cannot change task status from %s to %s",
                            status.getDisplayName(), newStatus.getDisplayName())
            );
        }
        this.status = newStatus;
    }

    /**
     * Business method: Check if task is overdue
     */
    public boolean isOverdue() {
        if (dueDate == null || status == TaskStatus.DONE || status == TaskStatus.CANCELLED) {
            return false;
        }
        return LocalDateTime.now().isAfter(dueDate);
    }

    /**
     * Business method: Assign task to a user
     * @param user The user to assign the task to
     */
    public void assignTo(User user) {
        if (user == null) {
            throw new IllegalArgumentException("Cannot assign task to null user");
        }
        this.assignedTo = user;
    }

    /**
     * Business method: Unassign task
     */
    public void unassign() {
        this.assignedTo = null;
    }


    public void validate() {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Task title cannot be null or empty");
        }
        if (createdBy == null) {
            throw new IllegalArgumentException("Task must have a creator");
        }

        if (completedAt == null) {
            throw new IllegalArgumentException("Task must have a completed date");
        }
    }

    /**
     * Business method: Check if task can be edited by user
     */
    public boolean canBeEditedBy(User user) {
        if (user == null) return false;

        // Creator can always edit
        if (createdBy.getId().equals(user.getId())) {
            return true;
        }

        // Assigned user can edit if task is assigned to them
        if (assignedTo != null && assignedTo.getId().equals(user.getId())) {
            return true;
        }

        // Admins and managers can edit any task
        return user.getRole().hasPermission(UserRole.MANAGER);
    }

    @Override
    public String toString() {
        return "Task{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", status=" + status +
                ", priority=" + priority +
                ", createdBy=" + (createdBy != null ? createdBy.getEmail() : "null") +
                ", assignedTo=" + (assignedTo != null ? assignedTo.getEmail() : "none") +
                '}';
    }
}
