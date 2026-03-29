package com.example.bossbot.user;

import com.example.bossbot.role.Role;
import com.example.bossbot.role.RoleName;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {

    @Mapping(target = "roleName", expression = "java(roleToName(user.getRole()))")
    UserAuthDto toDto(User user, String token);

    // TODO:: to be implemented or deleted if not needed:
    @Mapping(target = "roleName", source = "role", qualifiedByName = "roleToName")
    @Mapping(target = "token", ignore = true)
    UserAuthDto toDto(User user);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "role", source = "roleName")
    User toEntity(UserAuthDto dto);

    @Named("roleToName")
    default RoleName roleToName(Role role) {
        return role == null ? null : role.getRoleName();
    }

    default Role map(RoleName roleName) {
        if (roleName == null)
            return null;
        Role r = new Role();
        r.setRoleName(roleName);
        return r;
    }
}

