package com.example.bossbot.user;

import com.example.bossbot.role.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
        Optional<User> findByEmail(String email);

        /**
         * Find user by email with role eagerly loaded.
         * Will be used by OAuth2 resource server's JwtAuthenticationConverter to avoid LazyInitializationException.
         */
        @Query("SELECT u FROM User u LEFT JOIN FETCH u.role WHERE u.email = :email")
        Optional<User> findByEmailWithRole(@Param("email") String email);

        List<User> findByRoleId(Long roleId);
}
