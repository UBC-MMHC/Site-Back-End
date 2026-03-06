package com.ubcmmhcsoftware.auth.enums;

public enum RoleEnum {
    ROLE_USER(1),
    ROLE_BLOG_EDITOR(2),
    ROLE_BLOG_MANAGER(3),
    ROLE_ADMIN(4),
    ROLE_SUPERADMIN(5);

    private final int level;

    RoleEnum(int level) {
        this.level = level;
    }

    public int getLevel() {
        return level;
    }
}
