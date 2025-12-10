package com.samiCoding.task_sheild.domain.model;

public enum TaskPriority {

    LOW(1),
    MEDIUM(2),
    High(3),
    URGENT(4);
    private final int level;
    TaskPriority(int level) {
        this.level = level;
    }

    public int getLevel() {return this.level;}

    public boolean isHigherThan(TaskPriority other) {
        return this.level > other.level;
    }
}
