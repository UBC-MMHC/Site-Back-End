package com.ubcmmhcsoftware.ubcmmhc_web.Service;

import com.ubcmmhcsoftware.ubcmmhc_web.DTO.BlogPermissionsDTO;
import com.ubcmmhcsoftware.ubcmmhc_web.Enum.BlogPermission;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service responsible for blog-related business logic.
 */
@Service
@RequiredArgsConstructor
public class BlogService {

    /**
     * Resolves the blog permissions for the given authenticated user.
     *
     * @param userDetails the currently authenticated principal
     * @return a DTO describing what blog actions the user may perform
     */
    public BlogPermissionsDTO getUserPermissions(UserDetails userDetails) {
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        return BlogPermissionsDTO.builder()
                .user(userDetails.getUsername())
                .roles(roles)
                .canCreate(BlogPermission.CREATE.isGrantedByAny(roles))
                .canEdit(BlogPermission.EDIT.isGrantedByAny(roles))
                .canDelete(BlogPermission.DELETE.isGrantedByAny(roles))
                .build();
    }
}
