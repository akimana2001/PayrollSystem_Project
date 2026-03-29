package com.payroll.payrollsystem.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class TaxCalculationService {

    @Value("${app.payroll.rssb.employee-rate}")
    private BigDecimal rssbEmployeeRate;

    @Value("${app.payroll.rssb.employer-rate}")
    private BigDecimal rssbEmployerRate;

    public BigDecimal calculateRssbEmployee(BigDecimal grossSalary) {
        return grossSalary
                .multiply(rssbEmployeeRate)
                .setScale(2, RoundingMode.HALF_UP);
    }
    public BigDecimal calculateRssbEmployer(BigDecimal grossSalary) {
        return grossSalary
                .multiply(rssbEmployerRate)
                .setScale(2, RoundingMode.HALF_UP);
    }
    public BigDecimal calculatePAYE(BigDecimal grossSalary) {

        BigDecimal tax = BigDecimal.ZERO;
        double salary = grossSalary.doubleValue();

        if (salary <= 30000) {
            tax = BigDecimal.ZERO;

        } else if (salary <= 100000) {
            tax = BigDecimal.valueOf(
                    (salary - 30000) * 0.20);

        } else if (salary <= 200000) {
            tax = BigDecimal.valueOf(
                    (70000 * 0.20) +
                            (salary - 100000) * 0.30);

        } else {
            tax = BigDecimal.valueOf(
                    (70000 * 0.20) +
                            (100000 * 0.30) +
                            (salary - 200000) * 0.40);
        }

        return tax.setScale(2, RoundingMode.HALF_UP);
    }
    public BigDecimal calculateNetSalary(BigDecimal grossSalary) {
        BigDecimal payeTax = calculatePAYE(grossSalary);
        BigDecimal rssbEmployee = calculateRssbEmployee(grossSalary);
        return grossSalary
                .subtract(payeTax)
                .subtract(rssbEmployee)
                .setScale(2, RoundingMode.HALF_UP);
    }
}
