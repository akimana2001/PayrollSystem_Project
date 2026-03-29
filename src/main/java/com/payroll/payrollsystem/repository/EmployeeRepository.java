package com.payroll.payrollsystem.repository;

import com.payroll.payrollsystem.model.Employee;
import com.payroll.payrollsystem.model.enums.EmploymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    Optional<Employee> findByEmail(String email);
    Optional<Employee> findByEmployeeNumber(String employeeNumber);

    boolean existsByEmail(String email);
    boolean existsByNationalId(String nationalId);

    List<Employee> findByStatus(EmploymentStatus status);

    @Query("""
            SELECT e FROM Employee e
            WHERE (:search IS NULL OR
                   LOWER(e.firstName) LIKE LOWER(CONCAT('%',:search,'%')) OR
                   LOWER(e.lastName)  LIKE LOWER(CONCAT('%',:search,'%')) OR
                   LOWER(e.email)     LIKE LOWER(CONCAT('%',:search,'%')) OR
                   e.employeeNumber   LIKE CONCAT('%',:search,'%'))
            AND (:departmentId IS NULL OR e.department.id = :departmentId)
            AND (:status IS NULL OR e.status = :status)
            ORDER BY e.lastName ASC
            """)
    Page<Employee> searchEmployees(
            @Param("search") String search,
            @Param("departmentId") Long departmentId,
            @Param("status") EmploymentStatus status,
            Pageable pageable
    );

    @Query("SELECT e.employeeNumber FROM Employee e ORDER BY e.id DESC LIMIT 1")
    Optional<String> findLastEmployeeNumber();

    @Query("""
            SELECT e FROM Employee e
            JOIN FETCH e.department
            WHERE e.status = 'ACTIVE'
            """)
    List<Employee> findAllActiveWithDepartment();

    long countByDepartmentIdAndStatus(Long id, EmploymentStatus employmentStatus);
}
