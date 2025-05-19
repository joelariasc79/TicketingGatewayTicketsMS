package com.ticketing.dto;

import java.util.Set;

public class UserDTO {

    private Long id;
    private String userName;
    private String email;
    private String password;
    private Long manager;
    private Long department;
    private Long project;
    private Set<Long> roles; // Assuming you're passing Role IDs

    // Default Constructor
    public UserDTO() {
    }    
    
    public UserDTO(Long id, String userName) {
		super();
		this.id = id;
		this.userName = userName;
	}

    public UserDTO(Long id, String userName, String email, String password, Long manager, Long department,
			Long project, Set<Long> roles) {
		super();
		this.id = id;
		this.userName = userName;
		this.email = email;
		this.password = password;
		this.manager = manager;
		this.department = department;
		this.project = project;
		this.roles = roles;
	}
    
 // Getters and Setters

	public Long getId() {
        return id;
    }
	
	public Long getUserId() {
        return id;
    }
	

    public void setId(Long id) {
        this.id = id;
    }
    

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
    
    public Long getManager() {
		return manager;
	}

	public void setManager(Long manager) {
		this.manager = manager;
	}

	public Long getDepartment() {
		return department;
	}

	public void setDepartment(Long department) {
		this.department = department;
	}

	public Long getProject() {
		return project;
	}

	public void setProject(Long project) {
		this.project = project;
	}
	
	public Set<Long> getRoles() {
        return roles;
    }

    public void setRoles(Set<Long> roles) {
        this.roles = roles;
    }

    @Override
    public String toString() {
        return "NewRoleDTO{" +
                "id=" + id +
                ", userName='" + userName + '\'' +
                ", email='" + email + '\'' +
                ", password='********'" + // Masking password for toString
                ", roles=" + roles +
                '}';
    }
}