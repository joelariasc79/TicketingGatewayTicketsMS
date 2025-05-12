package com.ticketing.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.ticketing.domain.Role;
import com.ticketing.service.RoleService;

@Controller
@RequestMapping("/api/admin") // Consider a more appropriate base path if needed
public class RoleController {

    @Autowired
    private RoleService roleService;

    @GetMapping("/roles/all")
    @ResponseBody
    public ResponseEntity<List<Role>> getAllRoles() {
        return ResponseEntity.ok(roleService.findAll());
    }
}