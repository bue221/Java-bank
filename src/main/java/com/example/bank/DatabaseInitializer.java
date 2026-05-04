package com.example.bank;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseInitializer {

    public static void initialize(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("""
                CREATE TABLE IF NOT EXISTS accounts (
                    id INTEGER PRIMARY KEY,
                    holder TEXT NOT NULL,
                    balance REAL NOT NULL CHECK(balance >= 0)
                )
            """);
        }
    }

    public static void seedAccount(Connection connection, int id, String holder, double initialBalance) throws SQLException {
        try (PreparedStatement check = connection.prepareStatement("SELECT COUNT(*) FROM accounts WHERE id = ?")) {
            check.setInt(1, id);
            boolean exists = check.executeQuery().getInt(1) > 0;
            if (!exists) {
                try (PreparedStatement insert = connection.prepareStatement(
                        "INSERT INTO accounts(id, holder, balance) VALUES(?, ?, ?)")) {
                    insert.setInt(1, id);
                    insert.setString(2, holder);
                    insert.setDouble(3, initialBalance);
                    insert.executeUpdate();
                }
            }
        }
    }
}
