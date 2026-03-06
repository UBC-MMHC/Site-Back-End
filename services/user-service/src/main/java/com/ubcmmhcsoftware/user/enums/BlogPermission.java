package com.ubcmmhcsoftware.user.enums;

import com.ubcmmhcsoftware.user.entity.RoleEnum;

import java.util.Collection;
import java.util.Set;

/**
 * Blog-level permissions and the roles that grant each one.
 */
public enum BlogPermission {

    CREATE(Set.of(RoleEnum.ROLE_BLOG_EDITOR, RoleEnum.ROLE_BLOG_MANAGER,
            RoleEnum.ROLE_ADMIN, RoleEnum.ROLE_SUPERADMIN)),

    EDIT(Set.of(RoleEnum.ROLE_BLOG_EDITOR, RoleEnum.ROLE_BLOG_MANAGER,
            RoleEnum.ROLE_ADMIN, RoleEnum.ROLE_SUPERADMIN)),

    DELETE(Set.of(RoleEnum.ROLE_BLOG_MANAGER,
            RoleEnum.ROLE_ADMIN, RoleEnum.ROLE_SUPERADMIN));

    private final Set<RoleEnum> grantedBy;

    BlogPermission(Set<RoleEnum> grantedBy) {
        this.grantedBy = grantedBy;
    }

    public boolean isGrantedByAny(Collection<String> roleNames) {
        return roleNames.stream()
                .anyMatch(name -> grantedBy.stream()
                        .anyMatch(role -> role.name().equals(name)));
    }
}
