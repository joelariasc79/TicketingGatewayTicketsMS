
package com.ticketing.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate; 

import com.ticketing.domain.Department;
import com.ticketing.domain.Project;
import com.ticketing.domain.User;

import com.ticketing.dto.UserDto;

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
	
	private final RestTemplate restTemplate;

	
	@Value("${userservice.url}") // Configure this in application.properties/yml (e.g., http://localhost:8081)
    private String userServiceBaseUrl;
	

    public UserServiceImpl(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
    
    
    public Optional<UserDto> getUserById(Long userId) {
        String url = userServiceBaseUrl + "/api/users/" + userId;
        try {
            UserDto userDto = restTemplate.getForObject(url, UserDto.class);
            return Optional.ofNullable(userDto);
        } catch (Exception e) {
            // Log error, handle specific exceptions (e.g., HttpClientErrorException for 404)
            System.err.println("Error fetching user " + userId + ": " + e.getMessage());
            return Optional.empty();
        }
    }
	
    @Override
    public User save(User user) {
        return userRepository.save(user);
    }
    

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
