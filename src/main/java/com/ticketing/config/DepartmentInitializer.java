package com.ticketing.config;

import com.ticketing.domain.Department;
import com.ticketing.domain.Department.DepartmentNameEnum;
import com.ticketing.repository.DepartmentRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DepartmentInitializer implements CommandLineRunner {

    private final DepartmentRepository departmentRepository;

    public DepartmentInitializer(DepartmentRepository departmentRepository) {
        this.departmentRepository = departmentRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        for (DepartmentNameEnum departmentNameEnum : DepartmentNameEnum.values()) {
            String name = departmentNameEnum.name();
            if (departmentRepository.findByDepartmentName(name).isEmpty()) {
                Department department = new Department();
                department.setDepartmentName(name);
                departmentRepository.save(department);
                System.out.println("Added Department: " + name);
            } else {
                System.out.println("Department already exists: " + name);
            }
        }
    }
}