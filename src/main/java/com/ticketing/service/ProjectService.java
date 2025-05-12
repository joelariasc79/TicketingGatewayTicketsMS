package com.ticketing.service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ticketing.domain.Project;
import com.ticketing.repository.ProjectRepository;

@Service
public class ProjectService {

	@Autowired
	ProjectRepository projectRepository;
	
	public Set<Project> getDefaultProject() {
		Project project = projectRepository.findById(2L).orElse(null); // Assuming Role ID 2 is the default
        Set<Project> setProject = new HashSet<>();
        if (project != null) {
        	setProject.add(project);
        }
        return setProject;
    }
	
    public List<Project> findAll() {
        return projectRepository.findAll();
    }
    
    public Project save(Project role) {
        return projectRepository.save(role);
    }
    
    public Project findByName(String name) {
    	Optional<Project> projectOptional = projectRepository.findByProjectName(name);
    	return projectOptional.orElse(null);
    }
    
    public Optional<Project> findById(Long id) {
        return projectRepository.findById(id);
    }
    
    public List<Project> getAllProjects() {
        return projectRepository.findAll();
    }
    	
}
