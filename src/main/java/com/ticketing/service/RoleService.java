package com.ticketing.service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ticketing.domain.Role;
import com.ticketing.repository.RoleRepository;

@Service
public class RoleService {

	@Autowired
	RoleRepository roleRepository;
	
	public Set<Role> getDefaultRole() {
        Role role = roleRepository.findById(2L).orElse(null); // Assuming Role ID 2 is the default
        Set<Role> setRole = new HashSet<>();
        if (role != null) {
            setRole.add(role);
        }
        return setRole;
    }
	
    public List<Role> findAll() {
        return roleRepository.findAll();
    }
    
    public Role save(Role role) {
        return roleRepository.save(role);
    }
    
    public Role findByName(String name) {
    	Optional<Role> roleOptional = roleRepository.findByRoleName(name);
    	return roleOptional.orElse(null);
    }
    

//    public Role findById(Long id) {
//        return roleRepository.findById(id).orElse(null);
//    }
//    
    
    public Optional<Role> findById(Long id) {
        return roleRepository.findById(id);
    }
    	
}
