package com.ticketing.service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict; // Future: for delete roles
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.ticketing.domain.Role;
import com.ticketing.repository.RoleRepository;

@Service
public class RoleService {

	@Autowired
	RoleRepository roleRepository;
	
	@Cacheable("roles") // Cache the default role
	public Set<Role> getDefaultRole() {
		System.out.println("Fetching default role from DB or Cache...");
        Role role = roleRepository.findById(2L).orElse(null); // Assuming Role ID 2 is the default
        Set<Role> setRole = new HashSet<>();
        if (role != null) {
            setRole.add(role);
        }
        return setRole;
    }
	
    @Cacheable("roles") // Cache all roles
    public List<Role> findAll() {
    	System.out.println("Fetching all roles from DB or Cache...");
        return roleRepository.findAll();
    }
    
    @CachePut(value = "roles", key = "#role.id") // Update cache after saving/updating a role
    public Role save(Role role) {
    	System.out.println("Saving/Updating role in DB and Cache: " + role.getRoleName());
        return roleRepository.save(role);
    }
    
    @Cacheable(value = "roles", key = "#name") // Cache by role name
    public Role findByName(String name) {
    	System.out.println("Fetching role by name from DB or Cache: " + name);
    	Optional<Role> roleOptional = roleRepository.findByRoleName(name);
    	return roleOptional.orElse(null);
    }
    
    @Cacheable("roles") // Cache by role ID
    public Optional<Role> findById(Long id) {
    	System.out.println("Fetching role by ID from DB or Cache: " + id);
        return roleRepository.findById(id);
    }
    
    // If you had a delete method, you would use @CacheEvict like this:
    /*
    @CacheEvict(value = "roles", key = "#id")
    public void deleteById(Long id) {
        System.out.println("Deleting role from DB and Cache: " + id);
        roleRepository.deleteById(id);
    }

    @CacheEvict(value = "roles", key = "#role.id")
    public void delete(Role role) {
        System.out.println("Deleting role from DB and Cache: " + role.getId());
        roleRepository.delete(role);
    }
    */
}