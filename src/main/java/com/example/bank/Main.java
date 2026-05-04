package com.example.bank;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class Main {
    // Cada conexión apunta a una base distinta para simular origen y destino en servidores separados.
    private static final String ORIGIN_DB_URL = "jdbc:sqlite:origin.db";
    private static final String DESTINATION_DB_URL = "jdbc:sqlite:destination.db";

    public static void main(String[] args) {
        // try-with-resources garantiza cierre automático de conexiones y Scanner.
        try (Connection originConn = DriverManager.getConnection(ORIGIN_DB_URL);
             Connection destinationConn = DriverManager.getConnection(DESTINATION_DB_URL);
             Scanner scanner = new Scanner(System.in)) {

            // Preparamos estructura de tablas y datos iniciales (solo si aún no existen).
            DatabaseInitializer.initialize(originConn);
            DatabaseInitializer.initialize(destinationConn);

            DatabaseInitializer.seedAccount(originConn, 1, "Cuenta Origen", 1000.0);
            DatabaseInitializer.seedAccount(destinationConn, 2, "Cuenta Destino", 500.0);

            System.out.println("=== Transferencia bancaria entre 2 bases de datos ===");
            showBalances(originConn, destinationConn);

            System.out.print("Monto a transferir: ");
            double amount = readAmount(scanner);

            TransferService.transfer(originConn, destinationConn, 1, 2, amount);
            System.out.println("Transferencia completada correctamente.");

            showBalances(originConn, destinationConn);

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    private static double readAmount(Scanner scanner) {
        String input = scanner.nextLine();
        try {
            return Double.parseDouble(input);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("El monto debe ser numérico. Valor recibido: " + input, e);
        }
    }

    private static void showBalances(Connection originConn, Connection destinationConn) throws SQLException {
        System.out.println("Saldo origen (cuenta 1): " + getBalance(originConn, 1));
        System.out.println("Saldo destino (cuenta 2): " + getBalance(destinationConn, 2));
    }

    private static double getBalance(Connection conn, int id) throws SQLException {
        try (PreparedStatement statement = conn.prepareStatement("SELECT balance FROM accounts WHERE id = ?")) {
            statement.setInt(1, id);
            try (ResultSet rs = statement.executeQuery()) {
                if (!rs.next()) {
                    throw new IllegalStateException("Cuenta no encontrada: " + id);
                }
                return rs.getDouble("balance");
            }
        }
    }
}
