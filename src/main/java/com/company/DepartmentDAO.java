
package com.company;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DepartmentDAO {

    public List<Department> getAllDepartments()
            throws SQLException {

        List<Department> departments = new ArrayList<>();

        String sql = "SELECT id, name FROM departments ORDER BY id";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                departments.add(new Department(
                    rs.getInt("id"),
                    rs.getString("name")
                ));
            }
        }

        return departments;
    }

    public void addDepartment(String name)
            throws SQLException {

        String sql = "INSERT INTO departments (name) VALUES (?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, name.trim());
            stmt.executeUpdate();
        }
    }

    public void updateDepartment(int id, String name)
            throws SQLException {

        String sql = "UPDATE departments SET name = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, name.trim());
            stmt.setInt(2, id);

            if (stmt.executeUpdate() == 0) {
                throw new SQLException("Підрозділ не знайдено.");
            }
        }
    }

    public void deleteDepartment(int id)
            throws SQLException {

        String sql = "DELETE FROM departments WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            if (stmt.executeUpdate() == 0) {
                throw new SQLException("Підрозділ не знайдено.");
            }
        }
    }
}
