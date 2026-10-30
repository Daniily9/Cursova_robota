
package com.company;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EmployeeDAO {

    public List<Employee> getAllEmployees()
            throws SQLException {

        List<Employee> employees = new ArrayList<>();

        String sql = """
            SELECT id, first_name, last_name,
                   position, salary, department_id
            FROM employees
            ORDER BY id
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                employees.add(new Employee(
                    rs.getInt("id"),
                    rs.getString("first_name"),
                    rs.getString("last_name"),
                    rs.getString("position"),
                    rs.getBigDecimal("salary"),
                    rs.getInt("department_id")
                ));
            }
        }

        return employees;
    }

    public List<Employee> getEmployeesByDepartment(int departmentId)
            throws SQLException {

        List<Employee> employees = new ArrayList<>();

        String sql = """
            SELECT id, first_name, last_name,
                   position, salary, department_id
            FROM employees
            WHERE department_id = ?
            ORDER BY last_name, first_name
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, departmentId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    employees.add(new Employee(
                        rs.getInt("id"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("position"),
                        rs.getBigDecimal("salary"),
                        rs.getInt("department_id")
                    ));
                }
            }
        }

        return employees;
    }

    // Додавання нового працівника
    public void addEmployee(Employee employee)
            throws SQLException {

        String sql = """
            INSERT INTO employees
            (first_name, last_name, position, salary, department_id)
            VALUES (?, ?, ?, ?, ?)
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, employee.getFirstName());
            stmt.setString(2, employee.getLastName());
            stmt.setString(3, employee.getPosition());
            stmt.setBigDecimal(4, employee.getSalary());
            stmt.setInt(5, employee.getDepartmentId());

            stmt.executeUpdate();
        }
    }

    // Редагування працівника
    public void updateEmployee(Employee employee)
            throws SQLException {

        String sql = """
            UPDATE employees
            SET first_name = ?,
                last_name = ?,
                position = ?,
                salary = ?,
                department_id = ?
            WHERE id = ?
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, employee.getFirstName());
            stmt.setString(2, employee.getLastName());
            stmt.setString(3, employee.getPosition());
            stmt.setBigDecimal(4, employee.getSalary());
            stmt.setInt(5, employee.getDepartmentId());
            stmt.setInt(6, employee.getId());

            if (stmt.executeUpdate() == 0) {
                throw new SQLException(
                    "Працівника для оновлення не знайдено"
                );
            }
        }
    }

    // Видалення працівника
    public void deleteEmployee(int employeeId)
            throws SQLException {

        String sql = "DELETE FROM employees WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, employeeId);

            if (stmt.executeUpdate() == 0) {
                throw new SQLException(
                    "Працівника для видалення не знайдено"
                );
            }
        }
    }

    // Отримання працівника за ID
    public Employee getEmployeeById(int id)
            throws SQLException {

        String sql = """
            SELECT id, first_name, last_name,
                   position, salary, department_id
            FROM employees
            WHERE id = ?
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Employee(
                        rs.getInt("id"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("position"),
                        rs.getBigDecimal("salary"),
                        rs.getInt("department_id")
                    );
                }
            }
        }

        return null;
    }

    public int transferEmployees(
            int sourceDepartmentId,
            int targetDepartmentId) throws SQLException {

        if (sourceDepartmentId == targetDepartmentId) {
            throw new IllegalArgumentException(
                    "Підрозділи повинні відрізнятися."
            );
        }

        String sql = """
            UPDATE employees
            SET department_id = ?
            WHERE department_id = ?
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, targetDepartmentId);
            stmt.setInt(2, sourceDepartmentId);

            return stmt.executeUpdate();
        }
    }
}
