package com.ticketing.domain;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "User")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long userId;

    // @NotEmpty
    private String userName;

    // @NotEmpty
    private String userPassword;

    private String email;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id", nullable = true)
    @JsonBackReference("managedUsers") // Use the same unique name
    private User manager; // Self-reference for manager

    @OneToMany(mappedBy = "manager")
    @JsonManagedReference("managedUsers") // Give it a unique name
    private List<User> managedUsers;

    @OneToMany(mappedBy = "createdBy")
    @JsonManagedReference("createdTickets")
    private List<Ticket> createdTickets;

    @OneToMany(mappedBy = "assignee")
    @JsonManagedReference("assignedTickets")
    private List<Ticket> assignedTickets;


    @ManyToOne
    @JoinColumn(name = "department_id")
    @JsonBackReference("departmentUsers") // Add the value here!
    private Department department;


    @ManyToOne
    @JoinColumn(name = "project_id")
    @JsonBackReference("projectUsers") // Ensure this matches the @JsonManagedReference in Project
    private Project project;


    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    @JsonManagedReference("userRoles") // Give this a unique name
    private Set<Role> roles = new HashSet<>();

    /*
     Explanation of the Annotations:

     @JsonManagedReference: This annotation is placed on the side of the relationship that you want to be serialized normally (the "parent" side).
     In this example, we assume you want the roles within the User to be fully serialized when a User object is being converted to JSON.
     @JsonBackReference: This annotation is placed on the "child" side of the relationship (the side that should not be fully serialized to break the cycle).
     In the Role entity, the user set will not be fully serialized; Jackson will handle the reference based on the @JsonManagedReference side.
     */

    public User() {
    }

    public User(Long userId, String userName, String userPassword, String email, Department department, Project project, User manager, Set<Role> roles) {
        this.userId = userId;
        this.userName = userName;
        this.userPassword = userPassword;
        this.email = email;
        this.department = department;
        this.project = project;
        this.manager = manager;
        this.roles = roles;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserPassword() {
        return userPassword;
    }

    public void setUserPassword(String userPassword) {
        this.userPassword = userPassword;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public User getManager() {
        return manager;
    }

    public void setManager(User manager) {
        this.manager = manager;
    }

    public List<User> getManagedUsers() {
        return managedUsers;
    }

    public void setManagedUsers(List<User> managedUsers) {
        this.managedUsers = managedUsers;
    }

    public List<Ticket> getCreatedTickets() {
        return createdTickets;
    }

    public void setCreatedTickets(List<Ticket> createdTickets) {
        this.createdTickets = createdTickets;
    }

    public List<Ticket> getAssignedTickets() {
        return assignedTickets;
    }

    public void setAssignedTickets(List<Ticket> assignedTickets) {
        this.assignedTickets = assignedTickets;
    }

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }
}