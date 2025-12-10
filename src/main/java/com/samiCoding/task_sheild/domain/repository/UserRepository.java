package com.samiCoding.task_sheild.domain.repository;

import com.samiCoding.task_sheild.domain.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    @Query("SELECT u FROM User u WHERE u.email = :email AND u.enabled = true")
    Optional<User> findActiveByEmail(@Param("email") String email);

    @Query("SELECT COUNT(u) > 0 FROM User u WHERE u.email = :email AND u.id <> :id")
    boolean existsByEmailAndIdNot(@Param("email") String email, @Param("id") UUID id);
}
