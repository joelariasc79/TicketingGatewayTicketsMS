package com.ticketing.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.ticketing.domain.User;
import com.ticketing.service.UserService;

import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/api/admin") // Consider a more appropriate base path if needed
public class ManagerController {
	
	@Autowired
    private UserService userService;

	@GetMapping("/managers/all")
    @ResponseBody
    public ResponseEntity<List<User>> getAllManagers() {
        // Assuming you have a way to identify users who can be managers (e.g., by a specific role)
        List<User> managers = userService.findAll().stream()
                .filter(user -> user.getRoles().stream().anyMatch(role -> role.getRoleName().toString().equals("MANAGER")))
                .collect(Collectors.toList());
        return ResponseEntity.ok(managers);
    }

}
