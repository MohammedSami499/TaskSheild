package com.samiCoding.task_sheild.domain.model;

/**
 * Task status workflow
 * TODO → IN_PROGRESS → REVIEW → DONE
 * Can go back to TODO from any state
 */

public enum TaskStatus {

    TODO ("To Do"),
    IN_PROGRESS ("In Progress"),
    REVIEW("Under Review"),
    DONE("Completed"),
    CANCELLED("Cancelled");

    private final String displayName;
    TaskStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Check if the transition from current status to new status is Valid
     *
     */
    public boolean canTransitionTo(TaskStatus newStatus) {
        if (this == DONE && newStatus == CANCELLED) {
            return true;  // Can't change a completed task (except to cancel)
        }

        if (this == CANCELLED) {
            return false; // Can't change Cancelled Task
        }

        if (this == DONE && newStatus != CANCELLED) {
            return false;
        }
        return true;
    }

}
