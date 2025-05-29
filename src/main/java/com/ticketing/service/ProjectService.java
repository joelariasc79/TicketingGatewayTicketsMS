package com.ticketing.service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict; // Future: for delete projects
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.ticketing.domain.Project;
import com.ticketing.repository.ProjectRepository;

@Service
public class ProjectService {

	@Autowired
	ProjectRepository projectRepository;
	
	@Cacheable("projects") // Cache the default project
	public Set<Project> getDefaultProject() {
		System.out.println("Fetching default project from DB or Cache...");
		Project project = projectRepository.findById(2L).orElse(null); // Assuming Project ID 2 is the default
        Set<Project> setProject = new HashSet<>();
        if (project != null) {
        	setProject.add(project);
        }
        return setProject;
    }
	
    @Cacheable("projects") // Cache all projects
    public List<Project> findAll() {
    	System.out.println("Fetching all projects from DB or Cache...");
        return projectRepository.findAll();
    }
    
    @CachePut(value = "projects", key = "#project.id") // Update cache after saving/updating a project
    public Project save(Project project) { // Changed 'role' to 'project' for clarity and consistency
    	System.out.println("Saving/Updating project in DB and Cache: " + project.getProjectName());
        return projectRepository.save(project);
    }
    
    @Cacheable(value = "projects", key = "#name") // Cache by project name
    public Project findByName(String name) {
    	System.out.println("Fetching project by name from DB or Cache: " + name);
    	Optional<Project> projectOptional = projectRepository.findByProjectName(name);
    	return projectOptional.orElse(null);
    }
    
    @Cacheable("projects") // Cache by project ID
    public Optional<Project> findById(Long id) {
    	System.out.println("Fetching project by ID from DB or Cache: " + id);
        return projectRepository.findById(id);
    }
    
    @Cacheable("projects") // Cache all projects (duplicate of findAll, consider unifying)
    public List<Project> getAllProjects() {
    	System.out.println("Fetching all projects (via getAllProjects) from DB or Cache...");
        return projectRepository.findAll();
    }
    
    // If you had a delete method, you would use @CacheEvict like this:
    /*
    @CacheEvict(value = "projects", key = "#id")
    public void deleteById(Long id) {
        System.out.println("Deleting project from DB and Cache: " + id);
        projectRepository.deleteById(id);
    }

    @CacheEvict(value = "projects", key = "#project.id")
    public void delete(Project project) {
        System.out.println("Deleting project from DB and Cache: " + project.getId());
        projectRepository.delete(project);
    }
    */
}