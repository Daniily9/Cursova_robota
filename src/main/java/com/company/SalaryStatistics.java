package com.company;

import java.math.BigDecimal;

public class SalaryStatistics {

    private final int departmentId;
    private final String departmentName;
    private final int employeeCount;
    private final BigDecimal salaryFund;

    public SalaryStatistics(
            int departmentId,
            String departmentName,
            int employeeCount,
            BigDecimal salaryFund) {

        this.departmentId = departmentId;
        this.departmentName = departmentName;
        this.employeeCount = employeeCount;
        this.salaryFund = salaryFund;
    }

    public int getDepartmentId() {
        return departmentId;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public int getEmployeeCount() {
        return employeeCount;
    }

    public BigDecimal getSalaryFund() {
        return salaryFund;
    }
}
