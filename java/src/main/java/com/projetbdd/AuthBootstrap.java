package com.projetbdd;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public final class AuthBootstrap {
    private static final BCryptPasswordEncoder PASSWORD_ENCODER = new BCryptPasswordEncoder(10);

    private AuthBootstrap() {
    }

    public static void initialize() {
        try (Connection connection = Database.getConnection()) {
            createUsersTableIfNeeded(connection);
            ensureAdminAccount(connection);
            ensureClientAccount(connection);
            ensureLivreurAccount(connection);
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }

    private static void createUsersTableIfNeeded(Connection connection) throws SQLException {
        String sql = """
                CREATE TABLE IF NOT EXISTS utilisateur (
                    id_utilisateur BIGINT AUTO_INCREMENT PRIMARY KEY,
                    login VARCHAR(50) NOT NULL UNIQUE,
                    password_hash VARCHAR(255) NOT NULL,
                    role VARCHAR(20) NOT NULL,
                    id_client BIGINT NULL,
                    id_livreur BIGINT NULL,
                    actif BOOLEAN NOT NULL DEFAULT TRUE,
                    date_creation TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (id_client) REFERENCES client(id_client) ON DELETE SET NULL,
                    FOREIGN KEY (id_livreur) REFERENCES livreur(id_livreur) ON DELETE SET NULL
                )
                """;
        try (Statement statement = connection.createStatement()) {
            statement.execute(sql);
        }
    }

    private static void ensureAdminAccount(Connection connection) throws SQLException {
        ensureAccount(connection, "admin", "admin123", "ADMIN", null, null);
    }

    private static void ensureClientAccount(Connection connection) throws SQLException {
        Long clientId = findOrCreateClient(connection, "Client_1", "client1@mail.com");
        ensureAccount(connection, "client1", "client123", "CLIENT", clientId, null);
    }

    private static void ensureLivreurAccount(Connection connection) throws SQLException {
        Long livreurId = findOrCreateLivreur(connection, "Leo");
        ensureAccount(connection, "livreur1", "livreur123", "LIVREUR", null, livreurId);
    }

    private static void ensureAccount(Connection connection, String login, String password, String role, Long clientId, Long livreurId) throws SQLException {
        String selectSql = "SELECT id_utilisateur FROM utilisateur WHERE login = ? LIMIT 1";
        try (PreparedStatement select = connection.prepareStatement(selectSql)) {
            select.setString(1, login);
            try (ResultSet rs = select.executeQuery()) {
                if (rs.next()) {
                    String updateSql = "UPDATE utilisateur SET password_hash = ?, role = ?, id_client = ?, id_livreur = ?, actif = TRUE WHERE login = ?";
                    try (PreparedStatement update = connection.prepareStatement(updateSql)) {
                        update.setString(1, PASSWORD_ENCODER.encode(password));
                        update.setString(2, role);
                        if (clientId != null) {
                            update.setLong(3, clientId);
                        } else {
                            update.setNull(3, java.sql.Types.BIGINT);
                        }
                        if (livreurId != null) {
                            update.setLong(4, livreurId);
                        } else {
                            update.setNull(4, java.sql.Types.BIGINT);
                        }
                        update.setString(5, login);
                        update.executeUpdate();
                    }
                    return;
                }
            }
        }

        String insertSql = "INSERT INTO utilisateur (login, password_hash, role, id_client, id_livreur, actif) VALUES (?, ?, ?, ?, ?, TRUE)";
        try (PreparedStatement insert = connection.prepareStatement(insertSql)) {
            insert.setString(1, login);
            insert.setString(2, PASSWORD_ENCODER.encode(password));
            insert.setString(3, role);
            if (clientId != null) {
                insert.setLong(4, clientId);
            } else {
                insert.setNull(4, java.sql.Types.BIGINT);
            }
            if (livreurId != null) {
                insert.setLong(5, livreurId);
            } else {
                insert.setNull(5, java.sql.Types.BIGINT);
            }
            insert.executeUpdate();
        }
    }

    private static Long findOrCreateClient(Connection connection, String nom, String email) throws SQLException {
        String findSql = "SELECT id_client FROM client WHERE email = ? LIMIT 1";
        try (PreparedStatement find = connection.prepareStatement(findSql)) {
            find.setString(1, email);
            try (ResultSet rs = find.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong("id_client");
                }
            }
        }

        String insertSql = "INSERT INTO client (nom, email, solde, date_abonnement) VALUES (?, ?, ?, CURDATE())";
        try (PreparedStatement insert = connection.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
            insert.setString(1, nom);
            insert.setString(2, email);
            insert.setBigDecimal(3, new java.math.BigDecimal("50.00"));
            insert.executeUpdate();
            try (ResultSet keys = insert.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getLong(1);
                }
            }
        }
        throw new SQLException("Impossible de creer le client de test");
    }

    private static Long findOrCreateLivreur(Connection connection, String nom) throws SQLException {
        String findSql = "SELECT id_livreur FROM livreur WHERE nom = ? LIMIT 1";
        try (PreparedStatement find = connection.prepareStatement(findSql)) {
            find.setString(1, nom);
            try (ResultSet rs = find.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong("id_livreur");
                }
            }
        }

        String insertSql = "INSERT INTO livreur (nom, id_vehicule) VALUES (?, NULL)";
        try (PreparedStatement insert = connection.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
            insert.setString(1, nom);
            insert.executeUpdate();
            try (ResultSet keys = insert.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getLong(1);
                }
            }
        }
        throw new SQLException("Impossible de creer le livreur de test");
    }
}
