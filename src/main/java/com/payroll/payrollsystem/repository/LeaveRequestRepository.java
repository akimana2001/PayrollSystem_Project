package com.payroll.payrollsystem.repository;

import com.payroll.payrollsystem.model.LeaveRequest;
import com.payroll.payrollsystem.model.enums.LeaveStatus;
import com.payroll.payrollsystem.model.enums.LeaveType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface LeaveRequestRepository
        extends JpaRepository<LeaveRequest, Long> {
    List<LeaveRequest> findByEmployeeId(Long employeeId);

    List<LeaveRequest> findByStatus(LeaveStatus status);

    List<LeaveRequest> findByEmployeeIdAndLeaveTypeAndStatusAndStartDateBetween(
            Long employeeId,
            LeaveType leaveType,
            LeaveStatus status,
            LocalDate start,
            LocalDate end
    );
}