package com.example.bank;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class TransferService {

    public static void transfer(Connection originDb,
                                Connection destinationDb,
                                int originAccountId,
                                int destinationAccountId,
                                double amount) throws SQLException {

        if (amount <= 0) {
            throw new IllegalArgumentException("El monto debe ser mayor que cero.");
        }

        try {
            // Iniciamos transacción manual en ambas conexiones.
            originDb.setAutoCommit(false);
            destinationDb.setAutoCommit(false);

            // 1) Debitar origen (validando fondos suficientes).
            double originBalance = getBalance(originDb, originAccountId);
            if (originBalance < amount) {
                throw new IllegalStateException("Saldo insuficiente en la cuenta de origen.");
            }
            updateBalance(originDb, originAccountId, originBalance - amount);

            // 2) Acreditar destino.
            double destinationBalance = getBalance(destinationDb, destinationAccountId);
            updateBalance(destinationDb, destinationAccountId, destinationBalance + amount);

            // 3) Confirmar en ambas BD. Si falla antes de esto, se revierte todo.
            originDb.commit();
            destinationDb.commit();

        } catch (Exception e) {
            // Si ocurre cualquier error en cualquier paso, revertimos ambas conexiones.
            rollbackQuietly(originDb);
            rollbackQuietly(destinationDb);
            throw new SQLException("Transferencia revertida: " + e.getMessage(), e);
        } finally {
            // Dejamos las conexiones limpias para futuros usos.
            originDb.setAutoCommit(true);
            destinationDb.setAutoCommit(true);
        }
    }

    private static double getBalance(Connection connection, int accountId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT balance FROM accounts WHERE id = ?")) {
            statement.setInt(1, accountId);
            try (ResultSet rs = statement.executeQuery()) {
                if (!rs.next()) {
                    throw new IllegalStateException("No existe la cuenta " + accountId + " en esa base de datos.");
                }
                return rs.getDouble("balance");
            }
        }
    }

    private static void updateBalance(Connection connection, int accountId, double newBalance) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "UPDATE accounts SET balance = ? WHERE id = ?")) {
            statement.setDouble(1, newBalance);
            statement.setInt(2, accountId);
            int updated = statement.executeUpdate();
            if (updated != 1) {
                throw new IllegalStateException("No se pudo actualizar la cuenta " + accountId + ".");
            }
        }
    }

    private static void rollbackQuietly(Connection connection) {
        try {
            connection.rollback();
        } catch (SQLException ignored) {
        }
    }
}
