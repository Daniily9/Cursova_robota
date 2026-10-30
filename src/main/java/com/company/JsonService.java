package com.company;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.*;
import java.util.*;

public class JsonService {

    private final ObjectMapper mapper = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT);

    public void exportToJson(File file)
            throws SQLException, IOException {

        List<Map<String, Object>> departments =
                new ArrayList<>();

        List<Map<String, Object>> employees =
                new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection()) {

            String departmentSql =
                    "SELECT id, name FROM departments ORDER BY id";

            try (PreparedStatement stmt =
                         conn.prepareStatement(departmentSql);
                 ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {
                    Map<String, Object> department =
                            new LinkedHashMap<>();

                    department.put("id", rs.getInt("id"));
                    department.put("name", rs.getString("name"));

                    departments.add(department);
                }
            }

            String employeeSql = """
                    SELECT id, first_name, last_name,
                           position, salary, department_id
                    FROM employees
                    ORDER BY id
                    """;

            try (PreparedStatement stmt =
                         conn.prepareStatement(employeeSql);
                 ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {
                    Map<String, Object> employee =
                            new LinkedHashMap<>();

                    employee.put("id", rs.getInt("id"));
                    employee.put(
                            "firstName",
                            rs.getString("first_name")
                    );
                    employee.put(
                            "lastName",
                            rs.getString("last_name")
                    );
                    employee.put(
                            "position",
                            rs.getString("position")
                    );
                    employee.put(
                            "salary",
                            rs.getBigDecimal("salary")
                    );
                    employee.put(
                            "departmentId",
                            rs.getInt("department_id")
                    );

                    employees.add(employee);
                }
            }
        }

        Map<String, Object> backup = new LinkedHashMap<>();
        backup.put("departments", departments);
        backup.put("employees", employees);

        mapper.writeValue(file, backup);
    }

    public void importFromJson(File file)
            throws SQLException, IOException {

        JsonNode root = mapper.readTree(file);

        if (root == null || !root.isObject()
                || !root.path("departments").isArray()
                || !root.path("employees").isArray()) {
            throw new IOException(
                    "JSON повинен містити масиви departments і employees."
            );
        }

        JsonNode departments = root.get("departments");
        JsonNode employees = root.get("employees");

        Set<Integer> departmentIds = new HashSet<>();
        Set<Integer> employeeIds = new HashSet<>();
        Set<String> departmentNames = new HashSet<>();

        for (JsonNode department : departments) {
            int id = requiredPositiveId(department, "id");
            String name = requiredText(department, "name", 100);

            if (!departmentIds.add(id)) {
                throw new IOException(
                        "Повторний ID підрозділу: " + id
                );
            }

            if (!departmentNames.add(name)) {
                throw new IOException(
                        "Повторна назва підрозділу: " + name
                );
            }
        }

        for (JsonNode employee : employees) {
            int id = requiredPositiveId(employee, "id");
            int departmentId = requiredPositiveId(
                    employee, "departmentId"
            );

            requiredText(employee, "firstName", 100);
            requiredText(employee, "lastName", 100);
            requiredText(employee, "position", 100);

            JsonNode salaryNode = employee.path("salary");

            if (!salaryNode.isNumber()) {
                throw new IOException(
                        "Некоректна зарплата працівника ID=" + id
                );
            }

            BigDecimal salary = salaryNode.decimalValue();

            if (salary.signum() < 0
                    || salary.scale() > 2
                    || salary.precision() - salary.scale() > 10) {
                throw new IOException(
                        "Зарплата повинна бути від 0 до 9999999999.99 "
                        + "і мати не більше 2 знаків після коми."
                );
            }

            if (!employeeIds.add(id)) {
                throw new IOException(
                        "Повторний ID працівника: " + id
                );
            }

            if (!departmentIds.contains(departmentId)) {
                throw new IOException(
                        "Працівник " + id
                        + " посилається на відсутній підрозділ "
                        + departmentId
                );
            }
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);

            try {
                try (PreparedStatement stmt = conn.prepareStatement(
                        "DELETE FROM employees")) {
                    stmt.executeUpdate();
                }

                try (PreparedStatement stmt = conn.prepareStatement(
                        "DELETE FROM departments")) {
                    stmt.executeUpdate();
                }

                String insertDepartment = """
                        INSERT INTO departments (id, name)
                        VALUES (?, ?)
                        """;

                try (PreparedStatement stmt =
                             conn.prepareStatement(insertDepartment)) {

                    for (JsonNode department : departments) {
                        stmt.setInt(1, department.get("id").intValue());
                        stmt.setString(
                                2,
                                department.get("name")
                                        .textValue().trim()
                        );
                        stmt.addBatch();
                    }

                    stmt.executeBatch();
                }

                String insertEmployee = """
                        INSERT INTO employees
                        (id, first_name, last_name,
                         position, salary, department_id)
                        VALUES (?, ?, ?, ?, ?, ?)
                        """;

                try (PreparedStatement stmt =
                             conn.prepareStatement(insertEmployee)) {

                    for (JsonNode employee : employees) {
                        stmt.setInt(1, employee.get("id").intValue());
                        stmt.setString(
                                2,
                                employee.get("firstName")
                                        .textValue().trim()
                        );
                        stmt.setString(
                                3,
                                employee.get("lastName")
                                        .textValue().trim()
                        );
                        stmt.setString(
                                4,
                                employee.get("position")
                                        .textValue().trim()
                        );
                        stmt.setBigDecimal(
                                5,
                                employee.get("salary").decimalValue()
                        );
                        stmt.setInt(
                                6,
                                employee.get("departmentId").intValue()
                        );
                        stmt.addBatch();
                    }

                    stmt.executeBatch();
                }

                resetSequence(conn, "departments");
                resetSequence(conn, "employees");

                conn.commit();

            } catch (SQLException | RuntimeException ex) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackError) {
                    ex.addSuppressed(rollbackError);
                }
                throw ex;
            }
        }
    }

    private int requiredPositiveId(JsonNode node, String field)
            throws IOException {

        JsonNode value = node.path(field);

        if (!node.isObject() || !value.isIntegralNumber()
                || !value.canConvertToInt()
                || value.intValue() <= 0) {
            throw new IOException(
                    "Поле " + field
                    + " повинно бути додатним цілим ID."
            );
        }

        return value.intValue();
    }

    private String requiredText(
            JsonNode node, String field, int maxLength)
            throws IOException {

        JsonNode value = node.path(field);

        if (!value.isTextual()) {
            throw new IOException(
                    "Відсутнє текстове поле: " + field
            );
        }

        String text = value.textValue().trim();

        if (text.isEmpty() || text.length() > maxLength) {
            throw new IOException(
                    "Поле " + field
                    + " повинно містити від 1 до "
                    + maxLength + " символів."
            );
        }

        return text;
    }

    private void resetSequence(Connection conn, String table)
            throws SQLException {

        if (!table.equals("departments")
                && !table.equals("employees")) {
            throw new IllegalArgumentException(
                    "Невідома таблиця."
            );
        }

        String sql = """
                SELECT setval(
                    pg_get_serial_sequence(?, 'id'),
                    COALESCE((SELECT MAX(id) FROM %s), 1),
                    EXISTS(SELECT 1 FROM %s)
                )
                """.formatted(table, table);

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, table);
            stmt.executeQuery().close();
        }
    }
}
