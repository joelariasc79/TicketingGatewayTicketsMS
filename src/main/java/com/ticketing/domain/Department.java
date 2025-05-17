package com.ticketing.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonManagedReference;


@Entity
public class Department {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long departmentId;

    private String departmentName;

    @OneToMany(mappedBy = "department")
    @JsonManagedReference("departmentUsers") // Use the same name!
    private List<User> users;

    public Department() {
    }

    public Department(Long departmentId, String departmentName, List<User> users) {
        this.departmentId = departmentId;
        this.departmentName = departmentName;
        this.users = users;
    }

    public Long getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Long departmentId) {
        this.departmentId = departmentId;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }

    public enum DepartmentNameEnum {
        IT,
        ADMINISTRATION,
        ENGINEERING,
        ACCOUNTING
    }
}