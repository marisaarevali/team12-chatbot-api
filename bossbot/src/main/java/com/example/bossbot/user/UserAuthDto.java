package com.example.bossbot.user;

import com.example.bossbot.role.RoleName;

public record UserAuthDto(
        String email,
        String name,
        RoleName roleName,
        String token
) {}
