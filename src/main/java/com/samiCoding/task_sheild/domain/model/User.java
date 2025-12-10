package com.samiCoding.task_sheild.domain.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

/**
 * User entity representing a system user
 * Implements Spring Security's UserDetails for authentication
 */
@Entity
@Table(name = "users",
        uniqueConstraints = @UniqueConstraint(columnNames = "email"),
        indexes = {
                @Index(name = "idx_user_email", columnList = "email"),
                @Index(name = "idx_user_role", columnList = "role")
        }, schema = "tasksheild")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 255)
    private String email;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private UserRole role = UserRole.USER;

    @Column(nullable = false)
    @Builder.Default
    private boolean enabled = true;

    @Column(name = "account_non_expired", nullable = false)
    @Builder.Default
    private boolean accountNonExpired = true;

    @Column(name = "account_non_locked", nullable = false)
    @Builder.Default
    private boolean accountNonLocked = true;

    @Column(name = "credentials_non_expired", nullable = false)
    @Builder.Default
    private boolean credentialsNonExpired = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @Column(name = "failed_login_attempts")
    @Builder.Default
    private int failedLoginAttempts = 0;

    @Column(name = "locked_until")
    private LocalDateTime lockedUntil;

    /**
     * Lifecycle callback - runs before entity is persisted
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        validateEmail();
        validate();
    }

    /**
     * Lifecycle callback - runs before entity is updated
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        validateEmail();
        validate();
    }

    public void validate(){
        if (createdAt == null){
            throw new IllegalStateException("Created at cannot be null");
        }

        if (updatedAt == null){
            throw new IllegalStateException("Updated at cannot be null");
        }

        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email cannot be null or empty");
        }

        // Simple email validation regex
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        if (!email.matches(emailRegex)) {
            throw new IllegalArgumentException("Invalid email format: " + email);
        }
    }

    /**
     * Business rule: Validate email format
     * @throws IllegalArgumentException if email is invalid
     */
    private void validateEmail() {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email cannot be null or empty");
        }

        // Simple email validation regex
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        if (!email.matches(emailRegex)) {
            throw new IllegalArgumentException("Invalid email format: " + email);
        }
    }

    /**
     * Business method: Get user's full name
     */
    public String getFullName() {
        return firstName + " " + lastName;
    }

    /**
     * Business method: Increment failed login attempts
     */
    public void incrementFailedLoginAttempts() {
        this.failedLoginAttempts++;
        if (this.failedLoginAttempts >= 5) {
            this.accountNonLocked = false;
            this.lockedUntil = LocalDateTime.now().plusMinutes(30);
        }
    }

    /**
     * Business method: Reset failed login attempts
     */
    public void resetFailedLoginAttempts() {
        this.failedLoginAttempts = 0;
        this.accountNonLocked = true;
        this.lockedUntil = null;
    }

    // Spring Security UserDetails implementation

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return accountNonExpired;
    }

    @Override
    public boolean isAccountNonLocked() {
        if (lockedUntil != null && LocalDateTime.now().isBefore(lockedUntil)) {
            return false;
        }
        return accountNonLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return credentialsNonExpired;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", fullName='" + getFullName() + '\'' +
                ", role=" + role +
                ", enabled=" + enabled +
                '}';
    }
}