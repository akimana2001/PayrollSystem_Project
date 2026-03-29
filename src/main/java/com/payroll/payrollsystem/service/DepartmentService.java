package com.payroll.payrollsystem.service;

import com.payroll.payrollsystem.dto.request.DepartmentRequest;
import com.payroll.payrollsystem.dto.response.DepartmentResponse;
import com.payroll.payrollsystem.exception.DuplicateResourceException;
import com.payroll.payrollsystem.exception.ResourceNotFoundException;
import com.payroll.payrollsystem.model.Department;
import com.payroll.payrollsystem.repository.DepartmentRepository;
import com.payroll.payrollsystem.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final EmployeeRepository employeeRepository;
    private final AuditLogService auditLogService;

    @PreAuthorize("hasRole('ROLE_HR_MANAGER')")
    public DepartmentResponse create(DepartmentRequest request,
                                     String createdBy) {

        if (departmentRepository.existsByName(request.getName())) {
            throw new DuplicateResourceException(
                    "Department already exists: " + request.getName());
        }

        if (departmentRepository.existsByCode(request.getCode())) {
            throw new DuplicateResourceException(
                    "Department code already exists: " + request.getCode());
        }

        Department department = Department.builder()
                .name(request.getName())
                .code(request.getCode())
                .description(request.getDescription())
                .active(true)
                .build();

        Department saved = departmentRepository.save(department);

        auditLogService.log("CREATE", "Department",
                saved.getId(),
                "Created department: " + saved.getName(),
                createdBy);

        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<DepartmentResponse> getAll() {
        return departmentRepository.findByActiveTrue()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public DepartmentResponse getById(Long id) {
        Department department = departmentRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Department not found with id: " + id));
        return mapToResponse(department);
    }

    @PreAuthorize("hasRole('ROLE_HR_MANAGER')")
    public DepartmentResponse update(Long id,
                                     DepartmentRequest request,
                                     String updatedBy) {

        Department department = departmentRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Department not found with id: " + id));

        department.setName(request.getName());
        department.setCode(request.getCode());
        department.setDescription(request.getDescription());

        Department updated = departmentRepository.save(department);

        auditLogService.log("UPDATE", "Department",
                updated.getId(),
                "Updated department: " + updated.getName(),
                updatedBy);

        return mapToResponse(updated);
    }

    @PreAuthorize("hasRole('ROLE_HR_MANAGER')")
    public void deactivate(Long id, String deactivatedBy) {

        Department department = departmentRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Department not found with id: " + id));

        department.setActive(false);
        departmentRepository.save(department);

        auditLogService.log("DEACTIVATE", "Department",
                id, "Deactivated department: " + department.getName(),
                deactivatedBy);
    }

    private DepartmentResponse mapToResponse(Department department) {
        long employeeCount = employeeRepository
                .countByDepartmentIdAndStatus(
                        department.getId(),
                        com.payroll.payrollsystem.model.enums
                                .EmploymentStatus.ACTIVE);

        return DepartmentResponse.builder()
                .id(department.getId())
                .name(department.getName())
                .code(department.getCode())
                .description(department.getDescription())
                .active(department.isActive())
                .employeeCount(employeeCount)
                .createdAt(department.getCreatedAt())
                .build();
    }
}
