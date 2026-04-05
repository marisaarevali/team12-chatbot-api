package com.example.bossbot.util;

import com.example.bossbot.role.RoleName;
import com.example.bossbot.user.User;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

/**
 * Utility class for accessing security context and authenticated user information.
 * References:
 * Slightly adapted version of TalTech Börsibaar application for secure user context handling.
 * See: https://github.com/taltech-vanemarendajaks/vanemarendaja-borsibaar
 */
public class SecurityUtils {

    /**
     * Gets the currently authenticated user from the SecurityContext.
     *
     * @return The authenticated User
     * @throws ResponseStatusException if user is not authenticated or not a User
     * instance
     */
    public static User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authenticated");
        }

        Object principal = authentication.getPrincipal();
        if (!(principal instanceof User)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid authentication");
        }

        User user = (User) principal;

        return user;
    }

    /**
     * Checks if the currently authenticated user has the ADMIN role.
     *
     * @param user The user to check
     * @throws ResponseStatusException if user is not an admin
     */
    public static void requireAdminRole(User user) {
        if (user.getRole() == null || !RoleName.ADMIN.equals(user.getRole().getRoleName())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Admin role required");
        }
    }

    /**
     * Checks if the currently authenticated user has the ADMIN role.
     * Gets the user from the SecurityContext.
     *
     * @throws ResponseStatusException if user is not authenticated or not an admin
     */
    public static void requireAdminRole() {
        User user = getCurrentUser();
        requireAdminRole(user);
    }
}
