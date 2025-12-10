package com.samiCoding.task_sheild.domain.repository;


import com.samiCoding.task_sheild.domain.model.Task;
import com.samiCoding.task_sheild.domain.model.TaskStatus;
import com.samiCoding.task_sheild.domain.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface TaskRepository extends JpaRepository<Task, UUID> {

    Page<Task> findByCreatedBy(User createdBy, Pageable pageable);

    Page<Task> findByAssignedTo(User assignedTo, Pageable pageable);

    List<Task> findByStatusAndDueDateBefore(TaskStatus status, LocalDateTime dueDate);

    @Query("SELECT t FROM Task t WHERE " +
            "(:assignedToId IS NULL OR t.assignedTo.id = :assignedToId) AND " +
            "(:status IS NULL OR t.status = :status) AND " +
            "(:createdById IS NULL OR t.createdBy.id = :createdById)")
    Page<Task> findWithFilters(
            @Param("assignedToId") UUID assignedToId,
            @Param("status") TaskStatus status,
            @Param("createdById") UUID createdById,
            Pageable pageable
    );

    @Query("SELECT COUNT(t) FROM Task t WHERE t.assignedTo = :user AND t.status != 'DONE'")
    long countActiveTasksByUser(@Param("user") User user);
}
