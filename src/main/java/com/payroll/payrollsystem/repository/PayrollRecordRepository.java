package com.payroll.payrollsystem.repository;

import com.payroll.payrollsystem.model.PayrollRecord;
import com.payroll.payrollsystem.model.enums.PayrollStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface PayrollRecordRepository
        extends JpaRepository<PayrollRecord, Long> {

    Optional<PayrollRecord> findByEmployeeIdAndPayrollMonth(
            Long employeeId, String payrollMonth);

    boolean existsByEmployeeIdAndPayrollMonth(
            Long employeeId, String payrollMonth);


    List<PayrollRecord> findByPayrollMonth(String payrollMonth);

    List<PayrollRecord> findByPayrollMonthAndStatus(
            String payrollMonth, PayrollStatus status);


    Page<PayrollRecord> findByEmployeeIdOrderByPayrollMonthDesc(
            Long employeeId, Pageable pageable);


    @Query("""
            SELECT COALESCE(SUM(p.grossSalary), 0)
            FROM PayrollRecord p
            WHERE p.payrollMonth = :month
            AND p.status != 'CANCELLED'
            """)
    BigDecimal sumGrossSalaryByMonth(@Param("month") String month);

    @Query("""
            SELECT COALESCE(SUM(p.netSalary), 0)
            FROM PayrollRecord p
            WHERE p.payrollMonth = :month
            AND p.status != 'CANCELLED'
            """)
    BigDecimal sumNetSalaryByMonth(@Param("month") String month);

    @Query("""
            SELECT COALESCE(SUM(p.payeTax), 0)
            FROM PayrollRecord p
            WHERE p.payrollMonth = :month
            AND p.status != 'CANCELLED'
            """)
    BigDecimal sumPayeTaxByMonth(@Param("month") String month);

    @Query("""
            SELECT COALESCE(SUM(p.rssbEmployee + p.rssbEmployer), 0)
            FROM PayrollRecord p
            WHERE p.payrollMonth = :month
            AND p.status != 'CANCELLED'
            """)
    BigDecimal sumRssbByMonth(@Param("month") String month);

    @Query("""
            SELECT COUNT(p)
            FROM PayrollRecord p
            WHERE p.payrollMonth = :month
            AND p.status != 'CANCELLED'
            """)
    Long countByMonth(@Param("month") String month);

    @Query("""
            SELECT e.department.name,
                   SUM(p.grossSalary),
                   SUM(p.netSalary),
                   COUNT(p)
            FROM PayrollRecord p
            JOIN p.employee e
            WHERE p.payrollMonth = :month
            AND p.status != 'CANCELLED'
            GROUP BY e.department.name
            ORDER BY e.department.name
            """)
    List<Object[]> payrollCostByDepartment(@Param("month") String month);

    @Query("""
            SELECT p.payrollMonth,
                   SUM(p.grossSalary),
                   SUM(p.netSalary),
                   SUM(p.payeTax),
                   COUNT(p)
            FROM PayrollRecord p
            WHERE p.payrollMonth LIKE CONCAT(:year, '-%')
            AND p.status != 'CANCELLED'
            GROUP BY p.payrollMonth
            ORDER BY p.payrollMonth
            """)
    List<Object[]> yearlyPayrollSummary(@Param("year") String year);

    @Query("""
            SELECT DISTINCT p.payrollMonth
            FROM PayrollRecord p
            ORDER BY p.payrollMonth DESC
            """)
    List<String> findAllPayrollMonths();
}
