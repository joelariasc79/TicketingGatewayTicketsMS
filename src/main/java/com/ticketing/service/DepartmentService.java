package com.ticketing.service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict; // Future: for delete departments
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.ticketing.domain.Department;
import com.ticketing.repository.DepartmentRepository;

@Service
public class DepartmentService {

	@Autowired
	DepartmentRepository departmentRepository;
	
	@Cacheable("departments") // Cache the default department
	public Set<Department> getDefaultDepartment() {
		System.out.println("Fetching default department from DB or Cache...");
		Department department = departmentRepository.findById(2L).orElse(null); // Assuming Role ID 2 is the default
        Set<Department> setDepartment = new HashSet<>();
        if (department != null) {
        	setDepartment.add(department);
        }
        return setDepartment;
    }
	
    @Cacheable("departments") // Cache all departments
    public List<Department> findAll() {
    	System.out.println("Fetching all departments from DB or Cache...");
        return departmentRepository.findAll();
    }
    
    @CachePut(value = "departments", key = "#department.id") // Update cache after saving
    public Department save(Department department) { // Changed 'role' to 'department' for clarity
    	System.out.println("Saving/Updating department in DB and Cache: " + department.getDepartmentName());
        return departmentRepository.save(department);
    }
    
    @Cacheable(value = "departments", key = "#name") // Cache by department name
    public Department findByName(String name) {
    	System.out.println("Fetching department by name from DB or Cache: " + name);
    	Optional<Department> departmentOptional = departmentRepository.findByDepartmentName(name);
    	return departmentOptional.orElse(null);
    }
    
    @Cacheable("departments") // Cache by department ID
    public Optional<Department> findById(Long id) {
    	System.out.println("Fetching department by ID from DB or Cache: " + id);
        return departmentRepository.findById(id);
    }
    
    @Cacheable("departments") // Cache all departments (duplicate of findAll, consider unifying)
    public List<Department> getAllDepartments() {
    	System.out.println("Fetching all departments (via getAllDepartments) from DB or Cache...");
        return departmentRepository.findAll();
    }
}