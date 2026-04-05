package com.example.bossbot.security;

import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;

/**
 * Utility class for validating OAuth2 authentication tokens and extracting user information.
 * Provides static methods to verify authentication state and retrieve required user attributes
 * such as email from OAuth2User principals. Throws exceptions if validation fails.
 */
public class OAuth2AuthValidator {
    private OAuth2AuthValidator() {}

    public static OAuth2User requireAuthenticatedUser(OAuth2AuthenticationToken auth){
        if (auth == null || !auth.isAuthenticated()) {
            throw new AuthenticationCredentialsNotFoundException("User not authenticated");
        }

        if (!(auth.getPrincipal() instanceof OAuth2User user)) {
            throw new IllegalStateException("Unexpected principal type");
        }

        return user;
    }

    public static String requireEmail(OAuth2AuthenticationToken auth) {
        OAuth2User user = requireAuthenticatedUser(auth);
        String email = user.getAttribute("email");
        if (email == null) {
            throw new AuthenticationCredentialsNotFoundException("User email not found");
        }
        return email;
    }
}
