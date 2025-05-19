package com.ticketing.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ticketing.domain.User;


@Repository
public interface UserRepository extends JpaRepository<User, Long> {

	User findByUserName(String userName);
	
	// Find users by department ID
    List<User> findByDepartment_DepartmentId(Long departmentId);
    
    
 // Find users by project ID
    List<User> findByProject_ProjectId(Long projectId);
    
    
 // Find users by both department ID and project ID
    List<User> findByDepartment_DepartmentIdAndProject_ProjectId(Long departmentId, Long projectId);
    
    
    
//    Optional<User> findById(Long userId);

}
