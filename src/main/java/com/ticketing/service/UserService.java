package com.ticketing.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.ticketing.domain.Department;
import com.ticketing.domain.Project;
import com.ticketing.domain.User;
import com.ticketing.dto.UserDto;



@Service
public interface UserService {

	User save(User user);
    Optional<User> findById(Long id);
    List<User> findAll();
    void delete(User user);
    void deleteById(Long id);
    User findByUserName(String userName);
    List<Department> getAllDepartments();
    List<Project> getAllProjects();
    List<User> findUsersByDepartmentAndProject(Long departmentId, Long projectId);
    Optional<UserDto> getUserById(Long userId);
}
