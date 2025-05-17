package com.ticketing.domain;

import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;


@Entity
public class Role {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long roleId;

    @Enumerated(EnumType.STRING)
    private RoleName roleName;

    @ManyToMany(mappedBy="roles")
    @JsonBackReference // This side will be omitted during full serialization
    Set<User> user = new HashSet<>();

    public Role(Long roleId, RoleName roleName, Set<User> user) {
        super();
        this.roleId = roleId;
        this.roleName = roleName;
        this.user = user;
    }

    public enum RoleName {
        USER,
        MANAGER,
        ADMIN
    }

    public Long getRoleId() {
        return roleId;
    }

    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }

    public RoleName getRoleName() {
        return roleName;
    }

    public void setRoleName(RoleName roleName) {
        this.roleName = roleName;
    }

    public Set<User> getUser() {
        return user;
    }

    public void setUser(Set<User> user) {
        this.user = user;
    }

    public Role() {
        super();
    }

    public boolean isEmpty() {

        return false;
    }

}