
package com.company;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    private static final String URL =
            "jdbc:postgresql://localhost:5432/company_db";

    public static Connection getConnection()
            throws SQLException {

        String username = System.getenv("COMPANY_DB_USER");
        String password = System.getenv("COMPANY_DB_PASSWORD");

        if (username == null || username.isBlank()
                || password == null) {
            throw new SQLException(
                "Не задано COMPANY_DB_USER або COMPANY_DB_PASSWORD"
            );
        }

        return DriverManager.getConnection(
                URL, username, password
        );
    }
}
