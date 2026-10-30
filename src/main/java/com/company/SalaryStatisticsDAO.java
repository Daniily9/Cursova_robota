package com.company;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SalaryStatisticsDAO {

    public BigDecimal getTotalSalaryFund()
            throws SQLException {

        String sql = """
            SELECT COALESCE(SUM(salary), 0) AS total
            FROM employees
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                return rs.getBigDecimal("total");
            }
        }

        return BigDecimal.ZERO;
    }

    public List<SalaryStatistics> getDepartmentStatistics()
            throws SQLException {

        List<SalaryStatistics> statistics = new ArrayList<>();

        String sql = """
            SELECT d.id,
                   d.name,
                   COUNT(e.id) AS employee_count,
                   COALESCE(SUM(e.salary), 0) AS salary_fund
            FROM departments d
            LEFT JOIN employees e
                ON d.id = e.department_id
            GROUP BY d.id, d.name
            ORDER BY salary_fund DESC, d.name
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                statistics.add(new SalaryStatistics(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getInt("employee_count"),
                        rs.getBigDecimal("salary_fund")
                ));
            }
        }

        return statistics;
    }
}
