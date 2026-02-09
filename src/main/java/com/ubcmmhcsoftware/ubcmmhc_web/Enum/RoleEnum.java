package com.ubcmmhcsoftware.ubcmmhc_web.Enum;

public enum RoleEnum {
    ROLE_USER(1),
    ROLE_ADMIN(2),
    ROLE_SUPERADMIN(3);

    private final int level;

    RoleEnum(int level) {
        this.level = level;
    }

    public int getLevel() {
        return level;
    }
}
