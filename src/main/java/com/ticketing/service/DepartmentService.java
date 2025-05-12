package com.ticketing.service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ticketing.domain.Department;
import com.ticketing.repository.DepartmentRepository;

@Service
public class DepartmentService {

	@Autowired
	DepartmentRepository departmentRepository;
	
	public Set<Department> getDefaultDepartment() {
		Department department = departmentRepository.findById(2L).orElse(null); // Assuming Role ID 2 is the default
        Set<Department> setDepartment = new HashSet<>();
        if (department != null) {
        	setDepartment.add(department);
        }
        return setDepartment;
    }
	
    public List<Department> findAll() {
        return departmentRepository.findAll();
    }
    
    public Department save(Department role) {
        return departmentRepository.save(role);
    }
    
    public Department findByName(String name) {
    	Optional<Department> departmentOptional = departmentRepository.findByDepartmentName(name);
    	return departmentOptional.orElse(null);
    }
    

    public Optional<Department> findById(Long id) {
        return departmentRepository.findById(id);
    }
    
    public List<Department> getAllDepartments() {
        return departmentRepository.findAll();
    }
    	
}
