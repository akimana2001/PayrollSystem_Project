package com.payroll.payrollsystem.service;

import com.payroll.payrollsystem.dto.request.LeaveRequestDto;
import com.payroll.payrollsystem.dto.response.LeaveResponse;
import com.payroll.payrollsystem.exception.ResourceNotFoundException;
import com.payroll.payrollsystem.model.Employee;
import com.payroll.payrollsystem.model.LeaveRequest;
import com.payroll.payrollsystem.model.enums.LeaveStatus;
import com.payroll.payrollsystem.repository.EmployeeRepository;
import com.payroll.payrollsystem.repository.LeaveRequestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class LeaveService {
    private final LeaveRequestRepository leaveRequestRepository;
    private final EmployeeRepository employeeRepository;
    private final AuditLogService auditLogService;

    public LeaveResponse submit(LeaveRequestDto request,
                                String submittedBy) {

        Employee employee = employeeRepository
                .findById(request.getEmployeeId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Employee not found: "
                                + request.getEmployeeId()));


        int totalDays = request.getEndDate()
                .compareTo(request.getStartDate()) + 1;

        LeaveRequest leaveRequest = LeaveRequest.builder()
                .employee(employee)
                .leaveType(request.getLeaveType())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .totalDays(totalDays)
                .reason(request.getReason())
                .status(LeaveStatus.PENDING)
                .build();

        LeaveRequest saved =
                leaveRequestRepository.save(leaveRequest);

        auditLogService.log(
                "LEAVE_SUBMITTED",
                "LeaveRequest",
                saved.getId(),
                "Leave request submitted by "
                        + employee.getFullName(),
                submittedBy);

        return mapToResponse(saved);
    }

    @PreAuthorize("hasRole('ROLE_HR_MANAGER')")
    public LeaveResponse approve(Long id,
                                 String comment,
                                 String approvedBy) {

        LeaveRequest leave = findById(id);

        leave.setStatus(LeaveStatus.APPROVED);
        leave.setReviewedBy(approvedBy);
        leave.setReviewedAt(LocalDateTime.now());
        leave.setReviewComment(comment);

        LeaveRequest saved =
                leaveRequestRepository.save(leave);

        auditLogService.log(
                "LEAVE_APPROVED",
                "LeaveRequest",
                id,
                "Leave approved by " + approvedBy,
                approvedBy);

        return mapToResponse(saved);
    }

    @PreAuthorize("hasRole('ROLE_HR_MANAGER')")
    public LeaveResponse reject(Long id,
                                String comment,
                                String rejectedBy) {

        LeaveRequest leave = findById(id);

        leave.setStatus(LeaveStatus.REJECTED);
        leave.setReviewedBy(rejectedBy);
        leave.setReviewedAt(LocalDateTime.now());
        leave.setReviewComment(comment);

        LeaveRequest saved =
                leaveRequestRepository.save(leave);

        auditLogService.log(
                "LEAVE_REJECTED",
                "LeaveRequest",
                id,
                "Leave rejected by " + rejectedBy,
                rejectedBy);

        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<LeaveResponse> getByEmployee(Long employeeId) {
        return leaveRequestRepository
                .findByEmployeeId(employeeId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ROLE_HR_MANAGER')")
    public List<LeaveResponse> getPending() {
        return leaveRequestRepository
                .findByStatus(LeaveStatus.PENDING)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private LeaveRequest findById(Long id) {
        return leaveRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Leave request not found: " + id));
    }

    private LeaveResponse mapToResponse(LeaveRequest l) {
        return LeaveResponse.builder()
                .id(l.getId())
                .employeeId(l.getEmployee().getId())
                .employeeName(l.getEmployee().getFullName())
                .departmentName(l.getEmployee()
                        .getDepartment().getName())
                .leaveType(l.getLeaveType())
                .startDate(l.getStartDate())
                .endDate(l.getEndDate())
                .totalDays(l.getTotalDays())
                .reason(l.getReason())
                .status(l.getStatus())
                .reviewedBy(l.getReviewedBy())
                .reviewedAt(l.getReviewedAt())
                .reviewComment(l.getReviewComment())
                .createdAt(l.getCreatedAt())
                .build();
    }
}
