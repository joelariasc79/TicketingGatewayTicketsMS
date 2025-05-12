
package com.ticketing.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ticketing.domain.Department;
import com.ticketing.domain.Project;
import com.ticketing.domain.User;
import com.ticketing.repository.UserRepository;
import com.ticketing.repository.DepartmentRepository;
import com.ticketing.repository.ProjectRepository;


@Service
public class UserServiceImpl implements UserService {

	@Autowired
    private UserRepository userRepository;
	
	@Autowired
    private DepartmentRepository departmentRepository;
	
	@Autowired
    private ProjectRepository projectRepository;

    @Override
    public User save(User user) {
        return userRepository.save(user);
    }
    
//    @Override
//	public User save(User u) {
//		HashSet<Role> roleSet = new HashSet<>();
//		BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
//		String hashedPassword = passwordEncoder.encode(u.getUserPassword());
//		u.setUserPassword(hashedPassword);
//		Role userRole = roleRepository.findById(1).orElse(null);
//		roleSet.add(userRole);
//		u.setRoles(roleSet);
//		User user = userRepository.save(u);
//		return user;
//	}

    @Override
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    @Override
    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Override
    public void delete(User user) {
        userRepository.delete(user);
    }

    @Override
    public void deleteById(Long id) {
        userRepository.deleteById(id);
    }

    @Override
    public User findByUserName(String userName) {
        return userRepository.findByUserName(userName);
    }
    
 // Method to get all departments
    public List<Department> getAllDepartments() {
        return departmentRepository.findAll();
    }

    // Method to get all projects
    public List<Project> getAllProjects() {
        return projectRepository.findAll();
    }

    // Method to find users by department and/or project
    public List<User> findUsersByDepartmentAndProject(Long departmentId, Long projectId) {
        if (departmentId != null && projectId != null) {
            return userRepository.findByDepartment_DepartmentIdAndProject_ProjectId(departmentId, projectId);
        } else if (departmentId != null) {
            return userRepository.findByDepartment_DepartmentId(departmentId);
        } else if (projectId != null) {
            return userRepository.findByProject_ProjectId(projectId);
        } else {
            return userRepository.findAll(); // Or return an empty list, depending on your requirement
        }
    }


}
