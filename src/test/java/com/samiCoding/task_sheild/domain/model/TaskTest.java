package com.samiCoding.task_sheild.domain.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Task Entity Tests")
class TaskTest {

    private User creator;
    private User assignee;
    private Task task;

    @BeforeEach
    void setUp() {
        creator = User.builder()
                .id(UUID.randomUUID())
                .email("creator@example.com")
                .password("pass")
                .firstName("Creator")
                .lastName("User")
                .build();

        assignee = User.builder()
                .id(UUID.randomUUID())
                .email("assignee@example.com")
                .password("pass")
                .firstName("Assignee")
                .lastName("User")
                .build();

        task = Task.builder()
                .title("Test Task")
                .description("Test Description")
                .createdBy(creator)
                .status(TaskStatus.TODO)
                .priority(TaskPriority.MEDIUM)
                .build();
    }

    @Nested
    @DisplayName("Creation Tests")
    class CreationTests {

        @Test
        @DisplayName("Should create task with valid data")
        void shouldCreateTaskWithValidData() {
            assertNotNull(task);
            assertEquals("Test Task", task.getTitle());
            assertEquals("Test Description", task.getDescription());
            assertEquals(TaskStatus.TODO, task.getStatus());
            assertEquals(TaskPriority.MEDIUM, task.getPriority());
            assertEquals(creator, task.getCreatedBy());
            assertNull(task.getAssignedTo());
            assertNull(task.getCreatedAt());
        }

        @Test
        @DisplayName("Should have default status as TODO")
        void shouldHaveDefaultStatusAsTodo() {
            Task newTask = Task.builder()
                    .title("New Task")
                    .createdBy(creator)
                    .build();

            assertEquals(TaskStatus.TODO, newTask.getStatus());
        }

        @Test
        @DisplayName("Should have default priority as MEDIUM")
        void shouldHaveDefaultPriorityAsMedium() {
            Task newTask = Task.builder()
                    .title("New Task")
                    .createdBy(creator)
                    .build();

            assertEquals(TaskPriority.MEDIUM, newTask.getPriority());
        }

        @Test
        @DisplayName("Should throw exception for null title")
        void shouldThrowExceptionForNullTitle() {
            Task task = Task.builder()
                    .title(null)
                    .createdBy(creator)
                    .build();

            assertThrows(IllegalArgumentException.class, task::validate);
        }

        @Test
        @DisplayName("Should throw exception for empty title")
        void shouldThrowExceptionForEmptyTitle() {
                Task task = Task.builder()
                        .title("")
                        .createdBy(creator)
                        .build();
                assertThrows(IllegalArgumentException.class , task::validate);
        }

        @Test
        @DisplayName("Should throw exception for null creator")
        void shouldThrowExceptionForNullCreator() {
                Task task = Task.builder()
                        .title("Task")
                        .createdBy(null)
                        .build();
                assertThrows(IllegalArgumentException.class , task::validate);
        }
    }

    @Nested
    @DisplayName("Status Transition Tests")
    class StatusTransitionTests {

        @Test
        @DisplayName("Should allow valid status transitions")
        void shouldAllowValidStatusTransitions() {
            // TODO → IN_PROGRESS
            task.changeStatus(TaskStatus.IN_PROGRESS);
            assertEquals(TaskStatus.IN_PROGRESS, task.getStatus());

            // IN_PROGRESS → REVIEW
            task.changeStatus(TaskStatus.REVIEW);
            assertEquals(TaskStatus.REVIEW, task.getStatus());

            // REVIEW → DONE
            task.changeStatus(TaskStatus.DONE);
            assertEquals(TaskStatus.DONE, task.getStatus());
            task.setCompletedAt(LocalDateTime.now());
            assertNotNull(task.getCompletedAt());

            // Can go back to TODO from IN_PROGRESS
            Task task2 = Task.builder()
                    .title("Task 2")
                    .createdBy(creator)
                    .status(TaskStatus.IN_PROGRESS)
                    .build();

            task2.changeStatus(TaskStatus.TODO);
            assertEquals(TaskStatus.TODO, task2.getStatus());
        }

        @Test
        @DisplayName("Should not allow transition from DONE")
        void shouldNotAllowTransitionFromDone() {
            Task doneTask = Task.builder()
                    .title("Done Task")
                    .createdBy(creator)
                    .status(TaskStatus.DONE)
                    .build();

            assertThrows(IllegalStateException.class, () -> {
                doneTask.changeStatus(TaskStatus.IN_PROGRESS);
            });

            // But can cancel a completed task
            doneTask.changeStatus(TaskStatus.CANCELLED);
            assertEquals(TaskStatus.CANCELLED, doneTask.getStatus());
        }

        @Test
        @DisplayName("Should not allow transition from CANCELLED")
        void shouldNotAllowTransitionFromCancelled() {
            Task cancelledTask = Task.builder()
                    .title("Cancelled Task")
                    .createdBy(creator)
                    .status(TaskStatus.CANCELLED)
                    .build();

            assertThrows(IllegalStateException.class, () -> {
                cancelledTask.changeStatus(TaskStatus.TODO);
            });
        }
    }

    @Nested
    @DisplayName("Assignment Tests")
    class AssignmentTests {

        @Test
        @DisplayName("Should assign task to user")
        void shouldAssignTaskToUser() {
            assertNull(task.getAssignedTo());

            task.assignTo(assignee);
            assertEquals(assignee, task.getAssignedTo());
        }

        @Test
        @DisplayName("Should throw exception when assigning to null user")
        void shouldThrowExceptionWhenAssigningToNullUser() {
            assertThrows(IllegalArgumentException.class, () -> {
                task.assignTo(null);
            });
        }

        @Test
        @DisplayName("Should unassign task")
        void shouldUnassignTask() {
            task.assignTo(assignee);
            assertNotNull(task.getAssignedTo());

            task.unassign();
            assertNull(task.getAssignedTo());
        }
    }

    @Nested
    @DisplayName("Overdue Tests")
    class OverdueTests {

        @Test
        @DisplayName("Should not be overdue when no due date")
        void shouldNotBeOverdueWhenNoDueDate() {
            assertFalse(task.isOverdue());
        }

        @Test
        @DisplayName("Should not be overdue when future due date")
        void shouldNotBeOverdueWhenFutureDueDate() {
            task.setDueDate(LocalDateTime.now().plusDays(1));
            assertFalse(task.isOverdue());
        }

        @Test
        @DisplayName("Should be overdue when past due date")
        void shouldBeOverdueWhenPastDueDate() {
            task.setDueDate(LocalDateTime.now().minusDays(1));
            assertTrue(task.isOverdue());
        }

        @Test
        @DisplayName("Should not be overdue when completed")
        void shouldNotBeOverdueWhenCompleted() {
            task.setDueDate(LocalDateTime.now().minusDays(1));
            task.setStatus(TaskStatus.DONE);
            assertFalse(task.isOverdue());
        }

        @Test
        @DisplayName("Should not be overdue when cancelled")
        void shouldNotBeOverdueWhenCancelled() {
            task.setDueDate(LocalDateTime.now().minusDays(1));
            task.setStatus(TaskStatus.CANCELLED);
            assertFalse(task.isOverdue());
        }
    }

    @Nested
    @DisplayName("Permission Tests")
    class PermissionTests {

        @Test
        @DisplayName("Creator can edit task")
        void creatorCanEditTask() {
            assertTrue(task.canBeEditedBy(creator));
        }

        @Test
        @DisplayName("Assigned user can edit task")
        void assignedUserCanEditTask() {
            task.assignTo(assignee);
            assertTrue(task.canBeEditedBy(assignee));
        }

        @Test
        @DisplayName("Manager can edit any task")
        void managerCanEditAnyTask() {
            User manager = User.builder()
                    .email("manager@example.com")
                    .password("pass")
                    .firstName("Manager")
                    .lastName("User")
                    .role(UserRole.MANAGER)
                    .build();

            assertTrue(task.canBeEditedBy(manager));
        }

        @Test
        @DisplayName("Admin can edit any task")
        void adminCanEditAnyTask() {
            User admin = User.builder()
                    .email("admin@example.com")
                    .password("pass")
                    .firstName("Admin")
                    .lastName("User")
                    .role(UserRole.ADMIN)
                    .build();

            assertTrue(task.canBeEditedBy(admin));
        }

        @Test
        @DisplayName("Other user cannot edit task")
        void otherUserCannotEditTask() {
            User otherUser = User.builder()
                    .email("other@example.com")
                    .password("pass")
                    .firstName("Other")
                    .lastName("User")
                    .build();

            assertFalse(task.canBeEditedBy(otherUser));
        }

        @Test
        @DisplayName("Null user cannot edit task")
        void nullUserCannotEditTask() {
            assertFalse(task.canBeEditedBy(null));
        }
    }
}
