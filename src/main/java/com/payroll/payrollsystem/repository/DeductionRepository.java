package com.payroll.payrollsystem.repository;

import com.payroll.payrollsystem.model.Deduction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeductionRepository extends JpaRepository<Deduction, Long> {

    List<Deduction> findByEmployeeIdAndRecurringTrue(Long employeeId);

    List<Deduction> findByPayrollRecordId(Long payrollRecordId);
}
