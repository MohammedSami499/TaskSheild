package com.samiCoding.task_sheild.domain.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("User Entity Tests")
class UserTest {

    private User validUser;

    @BeforeEach
    void setUp() {
        validUser = User.builder()
                .email("john.doe@example.com")
                .password("SecurePass123!")
                .firstName("John")
                .lastName("Doe")
                .build();
    }

    @Nested
    @DisplayName("Creation Tests")
    class CreationTests {

        @Test
        @DisplayName("Should create user with valid data")
        void shouldCreateUserWithValidData() {
            assertNotNull(validUser);
            assertEquals("john.doe@example.com", validUser.getEmail());
            assertEquals("John", validUser.getFirstName());
            assertEquals("Doe", validUser.getLastName());
            assertEquals(UserRole.USER, validUser.getRole());
            assertTrue(validUser.isEnabled());
            assertNull(validUser.getCreatedAt());
        }

        @Test
        @DisplayName("Should have default role as USER")
        void shouldHaveDefaultRoleAsUser() {
            User user = User.builder()
                    .email("test@example.com")
                    .password("password")
                    .firstName("Test")
                    .lastName("User")
                    .build();

            assertEquals(UserRole.USER, user.getRole());
        }

        @Test
        @DisplayName("Should set timestamps on creation")
        void shouldSetTimestampsOnCreation() {
            LocalDateTime before = LocalDateTime.now().minusSeconds(1);
            User user = User.builder()
                    .email("timestamp@test.com")
                    .password("pass")
                    .firstName("Time")
                    .lastName("Stamp")
                    .build();
            LocalDateTime after = LocalDateTime.now().plusSeconds(1);

            user.setCreatedAt(LocalDateTime.now());
            assertNotNull(user.getCreatedAt());
            assertTrue(user.getCreatedAt().isAfter(before) || user.getCreatedAt().equals(before));
            assertTrue(user.getCreatedAt().isBefore(after) || user.getCreatedAt().equals(after));
        }
    }

    @Nested
    @DisplayName("Email Validation Tests")
    class EmailValidationTests {

        @ParameterizedTest
        @ValueSource(strings = {
                "invalid-email",
                "@example.com",
                "test@",
                "test@.com",
                "test@com.",
                ""
        })
        @DisplayName("Should throw exception for invalid email")
        void shouldThrowExceptionForInvalidEmail(String invalidEmail) {
                User user = User.builder()
                        .email(invalidEmail)
                        .password("password")
                        .firstName("Test")
                        .lastName("User")
                        .build();
                assertThrows(IllegalStateException.class, user::validate);
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "test@example.com",
                "user.name@domain.co.uk",
                "user+tag@example.org",
                "user_name@sub.domain.com"
        })
        @DisplayName("Should accept valid email formats")
        void shouldAcceptValidEmailFormats(String validEmail) {
            User user = User.builder()
                    .email(validEmail)
                    .password("password")
                    .firstName("Test")
                    .lastName("User")
                    .build();

            assertEquals(validEmail, user.getEmail());
        }

        @Test
        @DisplayName("Should throw exception for null email")
        void shouldThrowExceptionForNullEmail() {
                User user = User.builder()
                        .email(null)
                        .password("password")
                        .firstName("Test")
                        .lastName("User")
                        .build();
                assertThrows(IllegalStateException.class, user::validate);
        }
    }

    @Nested
    @DisplayName("Business Method Tests")
    class BusinessMethodTests {

        @Test
        @DisplayName("Should return full name correctly")
        void shouldReturnFullNameCorrectly() {
            User user = User.builder()
                    .email("full@name.com")
                    .password("pass")
                    .firstName("John")
                    .lastName("Smith")
                    .build();

            assertEquals("John Smith", user.getFullName());
        }

        @Test
        @DisplayName("Should increment failed login attempts")
        void shouldIncrementFailedLoginAttempts() {
            User user = User.builder()
                    .email("login@test.com")
                    .password("pass")
                    .firstName("Login")
                    .lastName("Test")
                    .build();

            assertEquals(0, user.getFailedLoginAttempts());
            assertTrue(user.isAccountNonLocked());

            // Increment 4 times
            for (int i = 0; i < 4; i++) {
                user.incrementFailedLoginAttempts();
            }

            assertEquals(4, user.getFailedLoginAttempts());
            assertTrue(user.isAccountNonLocked());

            // 5th attempt should lock account
            user.incrementFailedLoginAttempts();
            assertEquals(5, user.getFailedLoginAttempts());
            assertFalse(user.isAccountNonLocked());
            assertNotNull(user.getLockedUntil());
        }

        @Test
        @DisplayName("Should reset failed login attempts")
        void shouldResetFailedLoginAttempts() {
            User user = User.builder()
                    .email("reset@test.com")
                    .password("pass")
                    .firstName("Reset")
                    .lastName("Test")
                    .build();

            user.incrementFailedLoginAttempts();
            user.incrementFailedLoginAttempts();
            assertEquals(2, user.getFailedLoginAttempts());

            user.resetFailedLoginAttempts();
            assertEquals(0, user.getFailedLoginAttempts());
            assertTrue(user.isAccountNonLocked());
            assertNull(user.getLockedUntil());
        }
    }

    @Nested
    @DisplayName("Spring Security Integration Tests")
    class SpringSecurityTests {

        @Test
        @DisplayName("Should return correct authorities")
        void shouldReturnCorrectAuthorities() {
            User user = User.builder()
                    .email("auth@test.com")
                    .password("pass")
                    .firstName("Auth")
                    .lastName("Test")
                    .role(UserRole.ADMIN)
                    .build();

            var authorities = user.getAuthorities();
            assertNotNull(authorities);
            assertTrue(authorities.stream()
                    .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN")));
        }

        @Test
        @DisplayName("Should return email as username")
        void shouldReturnEmailAsUsername() {
            assertEquals("john.doe@example.com", validUser.getUsername());
        }

        @Test
        @DisplayName("Should respect account lock status")
        void shouldRespectAccountLockStatus() {
            User user = User.builder()
                    .email("lock@test.com")
                    .password("pass")
                    .firstName("Lock")
                    .lastName("Test")
                    .build();

            assertTrue(user.isAccountNonLocked());

            user.setLockedUntil(LocalDateTime.now().plusMinutes(30));
            assertFalse(user.isAccountNonLocked());

            user.setLockedUntil(LocalDateTime.now().minusMinutes(1));
            assertTrue(user.isAccountNonLocked());
        }
    }
}
