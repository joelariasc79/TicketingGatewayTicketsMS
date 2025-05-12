package com.ticketing.config;

import com.ticketing.domain.Project;
import com.ticketing.domain.Project.ProjectNameEnum;
import com.ticketing.repository.ProjectRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class ProjectInitializer implements CommandLineRunner {

    private final ProjectRepository projectRepository;

    public ProjectInitializer(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        for (ProjectNameEnum projectNameEnum : ProjectNameEnum.values()) {
            String name = projectNameEnum.name().replace("_", " "); // Replace underscores with spaces for project names
            if (projectRepository.findByProjectName(name).isEmpty()) {
                Project project = new Project();
                project.setProjectName(name);
                projectRepository.save(project);
                System.out.println("Added Project: " + name);
            } else {
                System.out.println("Project already exists: " + name);
            }
        }
    }
}