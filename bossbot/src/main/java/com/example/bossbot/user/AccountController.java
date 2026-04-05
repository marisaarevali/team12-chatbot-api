package com.example.bossbot.user;


import com.example.bossbot.role.RoleName;
import com.example.bossbot.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for managing user account information.
 * Provides endpoints for retrieving details of the currently authenticated user.
 * SecurityUtils throws ResponseStatusException if occurs, ApiExceptionHandler catches it globally
 */
@RestController
@RequestMapping("/api/v1/account")
@RequiredArgsConstructor
public class AccountController {
    public record MeResponse(String email, String name, RoleName roleName, Long discordId) {
    }

    @GetMapping
    @Operation(summary = "Get current user account", description = "Returns the currently authenticated user")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved account info")
    @ApiResponse(responseCode = "401", description = "Not authenticated")
    public MeResponse me() {
        User user = SecurityUtils.getCurrentUser();
        return new MeResponse(
                user.getEmail(),
                user.getName(),
                user.getRole() != null ? user.getRole().getRoleName() : null,
                user.getDiscordId()
        );
    }
}
