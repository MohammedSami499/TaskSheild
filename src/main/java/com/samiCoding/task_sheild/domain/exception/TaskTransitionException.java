package com.samiCoding.task_sheild.domain.exception;


import com.samiCoding.task_sheild.domain.model.TaskStatus;

/**
 * Thrown when task status transition is invalid
 */
public class TaskTransitionException extends DomainException {

    public TaskTransitionException(TaskStatus from, TaskStatus to) {
        super(String.format("Cannot transition task from %s to %s", from, to));
    }
}
