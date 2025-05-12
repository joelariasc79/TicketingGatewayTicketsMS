package com.ticketing.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.ticketing.domain.Project;
import com.ticketing.service.ProjectService;

@Controller
@RequestMapping("/api/admin/projects") // Consider a more appropriate base path if needed
public class ProjectController {

    @Autowired
    private ProjectService projectService;

    @GetMapping("/all")
    @ResponseBody
    public ResponseEntity<List<Project>> getAllProjects() {
        return ResponseEntity.ok(projectService.findAll());
    }
}