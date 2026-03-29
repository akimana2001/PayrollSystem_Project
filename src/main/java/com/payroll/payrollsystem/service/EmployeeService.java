package com.payroll.payrollsystem.service;

import com.payroll.payrollsystem.dto.request.EmployeeRequest;
import com.payroll.payrollsystem.dto.request.TerminationRequest;
import com.payroll.payrollsystem.dto.response.EmployeeResponse;
import com.payroll.payrollsystem.exception.DuplicateResourceException;
import com.payroll.payrollsystem.exception.ResourceNotFoundException;
import com.payroll.payrollsystem.model.Department;
import com.payroll.payrollsystem.model.Employee;
import com.payroll.payrollsystem.model.enums.EmploymentStatus;
import com.payroll.payrollsystem.repository.DepartmentRepository;
import com.payroll.payrollsystem.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final AuditLogService auditLogService;


    @PreAuthorize("hasRole('ROLE_HR_MANAGER')")
    public EmployeeResponse create(EmployeeRequest request,
                                   String createdBy) {

        if (employeeRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException(
                    "Email already registered: " + request.getEmail());
        }

        if (employeeRepository.existsByNationalId(
                request.getNationalId())) {
            throw new DuplicateResourceException(
                    "National ID already registered");
        }

        Department department = departmentRepository
                .findById(request.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Department not found: "
                                + request.getDepartmentId()));

        Employee employee = Employee.builder()
                .employeeNumber(generateEmployeeNumber())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .nationalId(request.getNationalId())
                .dateOfBirth(request.getDateOfBirth())
                .gender(request.getGender())
                .address(request.getAddress())
                .jobTitle(request.getJobTitle())
                .department(department)
                .dateOfJoining(request.getDateOfJoining())
                .status(EmploymentStatus.ACTIVE)
                .basicSalary(request.getBasicSalary())
                .housingAllowance(request.getHousingAllowance() != null
                        ? request.getHousingAllowance()
                        : java.math.BigDecimal.ZERO)
                .transportAllowance(request.getTransportAllowance() != null
                        ? request.getTransportAllowance()
                        : java.math.BigDecimal.ZERO)
                .medicalAllowance(request.getMedicalAllowance() != null
                        ? request.getMedicalAllowance()
                        : java.math.BigDecimal.ZERO)
                .otherAllowance(request.getOtherAllowance() != null
                        ? request.getOtherAllowance()
                        : java.math.BigDecimal.ZERO)
                .salaryType(request.getSalaryType() != null
                        ? request.getSalaryType()
                        : com.payroll.payrollsystem.model.enums
                        .SalaryType.GROSS_BASED)
                .bankName(request.getBankName())
                .bankAccountNumber(request.getBankAccountNumber())
                .bankBranch(request.getBankBranch())
                .createdBy(createdBy)
                .build();

        Employee saved = employeeRepository.save(employee);

        auditLogService.log("CREATE", "Employee",
                saved.getId(),
                "Created employee: " + saved.getEmployeeNumber(),
                createdBy);

        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('ROLE_HR_MANAGER'," +
            "'ROLE_FINANCE_MANAGER'," +
            "'ROLE_ACCOUNTANT'," +
            "'ROLE_GENERAL_MANAGER')")
    public EmployeeResponse getById(Long id) {
        return mapToResponse(findById(id));
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('ROLE_HR_MANAGER'," +
            "'ROLE_FINANCE_MANAGER'," +
            "'ROLE_ACCOUNTANT'," +
            "'ROLE_GENERAL_MANAGER')")
    public Page<EmployeeResponse> search(String search,
                                         Long departmentId,
                                         EmploymentStatus status,
                                         Pageable pageable) {
        return employeeRepository
                .searchEmployees(search, departmentId,
                        status, pageable)
                .map(this::mapToResponse);
    }

    @PreAuthorize("hasRole('ROLE_HR_MANAGER')")
    public EmployeeResponse update(Long id,
                                   EmployeeRequest request,
                                   String updatedBy) {

        Employee employee = findById(id);

        if (!employee.getEmail().equals(request.getEmail())
                && employeeRepository.existsByEmail(
                request.getEmail())) {
            throw new DuplicateResourceException(
                    "Email already in use: " + request.getEmail());
        }

        Department department = departmentRepository
                .findById(request.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Department not found: "
                                + request.getDepartmentId()));

        employee.setFirstName(request.getFirstName());
        employee.setLastName(request.getLastName());
        employee.setEmail(request.getEmail());
        employee.setPhoneNumber(request.getPhoneNumber());
        employee.setJobTitle(request.getJobTitle());
        employee.setAddress(request.getAddress());
        employee.setDepartment(department);
        employee.setBasicSalary(request.getBasicSalary());
        employee.setHousingAllowance(request.getHousingAllowance());
        employee.setTransportAllowance(
                request.getTransportAllowance());
        employee.setMedicalAllowance(request.getMedicalAllowance());
        employee.setOtherAllowance(request.getOtherAllowance());
        employee.setBankName(request.getBankName());
        employee.setBankAccountNumber(
                request.getBankAccountNumber());
        employee.setBankBranch(request.getBankBranch());

        Employee updated = employeeRepository.save(employee);

        auditLogService.log("UPDATE", "Employee",
                updated.getId(),
                "Updated employee: " + updated.getEmployeeNumber(),
                updatedBy);

        return mapToResponse(updated);
    }

    // ── TERMINATE ─────────────────────────────────────────────
    // soft delete only — never hard delete employees

    @PreAuthorize("hasRole('ROLE_HR_MANAGER')")
    public void terminate(Long id,
                          TerminationRequest request,
                          String terminatedBy) {

        Employee employee = findById(id);

        if (employee.getStatus() == EmploymentStatus.TERMINATED) {
            throw new IllegalStateException(
                    "Employee already terminated: "
                            + employee.getEmployeeNumber());
        }

        employee.setStatus(EmploymentStatus.TERMINATED);
        employee.setDateOfTermination(
                request.getTerminationDate());
        employeeRepository.save(employee);

        auditLogService.log("TERMINATE", "Employee",
                id, "Terminated: " + employee.getEmployeeNumber()
                        + " on " + request.getTerminationDate(),
                terminatedBy);
    }

    private Employee findById(Long id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Employee not found with id: " + id));
    }
    private String generateEmployeeNumber() {
        String year = String.valueOf(LocalDate.now().getYear());
        Optional<String> last =
                employeeRepository.findLastEmployeeNumber();
        int next = 1;
        if (last.isPresent()) {
            try {
                String[] parts = last.get().split("-");
                next = Integer.parseInt(parts[2]) + 1;
            } catch (Exception e) {
                log.warn("Could not parse last employee number");
            }
        }
        return String.format("EMP-%s-%03d", year, next);
    }
    private EmployeeResponse mapToResponse(Employee e) {
        return EmployeeResponse.builder()
                .id(e.getId())
                .employeeNumber(e.getEmployeeNumber())
                .firstName(e.getFirstName())
                .lastName(e.getLastName())
                .fullName(e.getFullName())
                .email(e.getEmail())
                .phoneNumber(e.getPhoneNumber())
                .jobTitle(e.getJobTitle())
                .departmentId(e.getDepartment().getId())
                .departmentName(e.getDepartment().getName())
                .dateOfJoining(e.getDateOfJoining())
                .dateOfTermination(e.getDateOfTermination())
                .status(e.getStatus())
                .basicSalary(e.getBasicSalary())
                .housingAllowance(e.getHousingAllowance())
                .transportAllowance(e.getTransportAllowance())
                .medicalAllowance(e.getMedicalAllowance())
                .otherAllowance(e.getOtherAllowance())
                .grossSalary(e.getGrossSalary())
                .salaryType(e.getSalaryType())
                .bankName(e.getBankName())
                .bankAccountNumber(maskAccount(
                        e.getBankAccountNumber()))
                .createdAt(e.getCreatedAt())
                .createdBy(e.getCreatedBy())
                .build();
    }

    private String maskAccount(String account) {
        if (account == null || account.length() < 4) {
            return "****";
        }
        return "******" + account.substring(account.length() - 4);
    }
}
