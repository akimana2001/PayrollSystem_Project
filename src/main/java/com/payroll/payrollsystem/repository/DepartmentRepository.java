package com.payroll.payrollsystem.repository;

import com.payroll.payrollsystem.model.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {

    Optional<Department> findByName(String name);
    boolean existsByName(String name);
    boolean existsByCode(String code);

    List<Department> findByActiveTrue();
}