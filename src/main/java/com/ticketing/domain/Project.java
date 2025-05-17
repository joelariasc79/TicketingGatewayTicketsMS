package com.ticketing.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonManagedReference;


@Entity
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long projectId;

    private String projectName;

    @OneToMany(mappedBy = "project")
    @JsonManagedReference("projectUsers") // Choose a name and use it consistently
    private List<User> users; // Or whatever you've named the user list

    public Project() {
    }

    public Project(Long projectId, String projectName, List<User> users) {
        this.projectId = projectId;
        this.projectName = projectName;
        this.users = users;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }

    public enum ProjectNameEnum {
        CREDIT_CARDS,
        DEBIT_CARDS,
        LOANS
    }
}