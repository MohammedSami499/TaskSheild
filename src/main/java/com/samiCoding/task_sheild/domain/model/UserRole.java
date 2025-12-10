package com.samiCoding.task_sheild.domain.model;

public enum UserRole {

    USER,
    MANAGER,
    ADMIN,
    AUDITOR;

    public boolean hasPermission(UserRole role) {
        return this.ordinal() >= role.ordinal();
    }

}
