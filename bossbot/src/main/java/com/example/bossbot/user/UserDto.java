package com.example.bossbot.user;

import com.example.bossbot.role.RoleName;

public record UserDto(
        Long id,
        String name,
        RoleName roleName
) {
    public UserDto (User user){
        this(
                user.getId(),
                user.getName(),
                user.getRole() != null ? user.getRole().getRoleName() : RoleName.USER
        );
    }
}
