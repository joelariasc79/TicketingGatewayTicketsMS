package com.ticketing.config;

import com.ticketing.domain.Role;
import com.ticketing.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.Transactional;

@Configuration
public class RoleInitializer {

    @Autowired
    private RoleRepository roleRepository;

    @Bean
    public ApplicationRunner initializeRoles() {
        return args -> {
            addRoleIfNotExists(Role.RoleName.USER);
            addRoleIfNotExists(Role.RoleName.MANAGER);
            addRoleIfNotExists(Role.RoleName.ADMIN);
        };
    }

    @Transactional
    public void addRoleIfNotExists(Role.RoleName roleName) {
        if (roleRepository.findByRoleName(roleName).isEmpty()) {
            Role role = new Role();
            role.setRoleName(roleName);
            roleRepository.save(role);
            System.out.println("Added role: " + roleName);
        }
    }
}