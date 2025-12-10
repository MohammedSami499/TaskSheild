package com.samiCoding.task_sheild.domain.repository;

import com.samiCoding.task_sheild.domain.model.AuditLog;
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

/**
 * Repository for AuditLog entities with custom queries for audit trail analysis
 *
 * Key Features:
 * 1. Advanced filtering capabilities
 * 2. Performance-optimized queries
 * 3. Audit-specific business queries
 * 4. Pagination support for large datasets
 */
@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {

    // ==================== Basic CRUD operations (inherited from JpaRepository) ====================
    // findAll(), findById(), save(), deleteById(), etc. are automatically available

    // ==================== Find by User ====================

    /**
     * Find all audit logs for a specific user
     */
    List<AuditLog> findByUser(User user);

    /**
     * Find all audit logs for a specific user with pagination
     */
    Page<AuditLog> findByUser(User user, Pageable pageable);

    /**
     * Find all audit logs for a specific user ID
     */
    List<AuditLog> findByUserId(UUID userId);

    // ==================== Find by Action ====================

    /**
     * Find all audit logs for a specific action
     */
    List<AuditLog> findByAction(String action);

    /**
     * Find all audit logs for multiple actions
     */
    List<AuditLog> findByActionIn(List<String> actions);

    /**
     * Find all audit logs for action starting with prefix (e.g., "USER_")
     */
    List<AuditLog> findByActionStartingWith(String actionPrefix);

    // ==================== Find by Resource ====================

    /**
     * Find all audit logs for a specific resource type
     */
    List<AuditLog> findByResourceType(String resourceType);

    /**
     * Find all audit logs for a specific resource ID
     */
    List<AuditLog> findByResourceId(UUID resourceId);

    /**
     * Find audit logs for a specific resource type and ID
     */
    List<AuditLog> findByResourceTypeAndResourceId(String resourceType, UUID resourceId);

    /**
     * Find the latest audit log for a specific resource
     */
    @Query("SELECT a FROM AuditLog a WHERE a.resourceType = :resourceType AND a.resourceId = :resourceId ORDER BY a.createdAt DESC")
    List<AuditLog> findLatestByResource(
            @Param("resourceType") String resourceType,
            @Param("resourceId") UUID resourceId,
            Pageable pageable
    );

    // ==================== Time-based Queries ====================

    /**
     * Find audit logs created after a specific date
     */
    List<AuditLog> findByCreatedAtAfter(LocalDateTime date);

    /**
     * Find audit logs created before a specific date
     */
    List<AuditLog> findByCreatedAtBefore(LocalDateTime date);

    /**
     * Find audit logs created between two dates
     */
    List<AuditLog> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Find today's audit logs
     */
//    @Query("SELECT a FROM AuditLog a WHERE DATE(a.createdAt) = CURRENT_DATE")
//    List<AuditLog> findTodayLogs();

    // ==================== Advanced Filtering Queries ====================

    /**
     * Find audit logs with multiple criteria (dynamic filtering)
     */
    @Query("SELECT a FROM AuditLog a WHERE " +
            "(:userId IS NULL OR a.user.id = :userId) AND " +
            "(:action IS NULL OR a.action = :action) AND " +
            "(:resourceType IS NULL OR a.resourceType = :resourceType) AND " +
            "(:startDate IS NULL OR a.createdAt >= :startDate) AND " +
            "(:endDate IS NULL OR a.createdAt <= :endDate)")
    Page<AuditLog> findWithFilters(
            @Param("userId") UUID userId,
            @Param("action") String action,
            @Param("resourceType") String resourceType,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable
    );

    /**
     * Find audit logs for security-related actions
     */
    @Query("SELECT a FROM AuditLog a WHERE a.action IN ('USER_LOGIN', 'USER_LOGIN_FAILED', 'USER_LOGOUT', 'PASSWORD_CHANGED', 'ROLE_CHANGED', 'USER_LOCKED', 'USER_UNLOCKED')")
    List<AuditLog> findSecurityLogs();

    /**
     * Find failed login attempts for a user
     */
    @Query("SELECT a FROM AuditLog a WHERE a.user.id = :userId AND a.action = 'USER_LOGIN_FAILED' AND a.createdAt >= :sinceDate ORDER BY a.createdAt DESC")
    List<AuditLog> findFailedLoginAttempts(
            @Param("userId") UUID userId,
            @Param("sinceDate") LocalDateTime sinceDate
    );

    // ==================== Statistical Queries ====================

    /**
     * Count audit logs by action type
     */
    @Query("SELECT a.action, COUNT(a) FROM AuditLog a GROUP BY a.action ORDER BY COUNT(a) DESC")
    List<Object[]> countByAction();

    /**
     * Count audit logs by user
     */
    @Query("SELECT a.user.id, a.user.email, COUNT(a) FROM AuditLog a WHERE a.user IS NOT NULL GROUP BY a.user.id, a.user.email ORDER BY COUNT(a) DESC")
    List<Object[]> countByUser();

    /**
     * Get daily audit log statistics
     */
    @Query("SELECT DATE(a.createdAt), COUNT(a) FROM AuditLog a WHERE a.createdAt >= :startDate GROUP BY DATE(a.createdAt) ORDER BY DATE(a.createdAt) DESC")
    List<Object[]> getDailyStats(@Param("startDate") LocalDateTime startDate);

    /**
     * Find the most active users (based on audit logs)
     */
    @Query("SELECT a.user.email, COUNT(a) as activityCount FROM AuditLog a WHERE a.user IS NOT NULL AND a.createdAt >= :startDate GROUP BY a.user.email ORDER BY activityCount DESC")
    Page<Object[]> findMostActiveUsers(
            @Param("startDate") LocalDateTime startDate,
            Pageable pageable
    );

    // ==================== Cleanup Queries ====================

    /**
     * Delete audit logs older than specified date
     * Useful for implementing audit log retention policies
     */
    void deleteByCreatedAtBefore(LocalDateTime date);

    /**
     * Count audit logs older than specified date
     * Useful for monitoring log size
     */
    long countByCreatedAtBefore(LocalDateTime date);

    // ==================== IP Address Analysis ====================

    /**
     * Find all unique IP addresses in audit logs
     */
    @Query("SELECT DISTINCT a.ipAddress FROM AuditLog a WHERE a.ipAddress IS NOT NULL")
    List<String> findDistinctIpAddresses();

    /**
     * Find audit logs from a specific IP address
     */
    List<AuditLog> findByIpAddress(String ipAddress);

    /**
     * Find suspicious activity from IP addresses with multiple failed logins
     */
    @Query("SELECT a.ipAddress, COUNT(a) as failedCount FROM AuditLog a " +
            "WHERE a.action = 'USER_LOGIN_FAILED' AND a.createdAt >= :sinceDate " +
            "GROUP BY a.ipAddress HAVING COUNT(a) > :threshold " +
            "ORDER BY failedCount DESC")
    List<Object[]> findSuspiciousIpAddresses(
            @Param("sinceDate") LocalDateTime sinceDate,
            @Param("threshold") long threshold
    );

    // ==================== Performance Optimized Queries ====================

    /**
     * Get audit log summary without loading full entity (DTO projection)
     */
    @Query("SELECT a.id, a.action, a.resourceType, a.createdAt, u.email as userEmail FROM AuditLog a " +
            "LEFT JOIN a.user u " +
            "WHERE a.createdAt >= :startDate " +
            "ORDER BY a.createdAt DESC")
    List<Object[]> findAuditSummary(@Param("startDate") LocalDateTime startDate);

    /**
     * Check if specific action was performed on a resource
     * Returns boolean instead of loading full entities
     */
    @Query("SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END FROM AuditLog a " +
            "WHERE a.resourceType = :resourceType AND a.resourceId = :resourceId AND a.action = :action")
    boolean existsByResourceAndAction(
            @Param("resourceType") String resourceType,
            @Param("resourceId") UUID resourceId,
            @Param("action") String action
    );

    // ==================== Business-Specific Queries ====================

    /**
     * Find user login history
     */
    @Query("SELECT a FROM AuditLog a WHERE " +
            "a.user.id = :userId AND " +
            "(a.action = 'USER_LOGIN' OR a.action = 'USER_LOGOUT') " +
            "ORDER BY a.createdAt DESC")
    List<AuditLog> findUserLoginHistory(@Param("userId") UUID userId);

    /**
     * Find all modifications to a specific resource
     */
    @Query("SELECT a FROM AuditLog a WHERE " +
            "a.resourceType = :resourceType AND a.resourceId = :resourceId AND " +
            "(a.action LIKE '%_CREATED' OR a.action LIKE '%_UPDATED' OR a.action LIKE '%_DELETED') " +
            "ORDER BY a.createdAt DESC")
    List<AuditLog> findResourceModificationHistory(
            @Param("resourceType") String resourceType,
            @Param("resourceId") UUID resourceId
    );

    /**
     * Find audit trail for compliance reporting
     */
    @Query("SELECT a FROM AuditLog a WHERE " +
            "a.action IN ('USER_CREATED', 'USER_UPDATED', 'USER_DELETED', " +
            "'TASK_CREATED', 'TASK_UPDATED', 'TASK_DELETED', " +
            "'ROLE_CHANGED', 'PERMISSION_CHANGED') AND " +
            "a.createdAt BETWEEN :startDate AND :endDate " +
            "ORDER BY a.createdAt DESC")
    Page<AuditLog> findComplianceAuditTrail(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable
    );
}