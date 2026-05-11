package com.projetbdd;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserService {
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public boolean authenticate(String login, String password) {
        try (Connection conn = Database.getConnection()) {
            String sql = "SELECT id_utilisateur, password_hash, role, id_client, id_livreur FROM utilisateur WHERE login = ? AND actif = TRUE";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, login);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        String storedHash = rs.getString("password_hash");
                        if (passwordEncoder.matches(password, storedHash)) {
                            Session session = Session.getInstance();
                            session.login(
                                rs.getLong("id_utilisateur"),
                                login,
                                rs.getString("role"),
                                rs.getObject("id_client") != null ? rs.getLong("id_client") : null,
                                rs.getObject("id_livreur") != null ? rs.getLong("id_livreur") : null
                            );
                            return true;
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean createUser(String login, String password, String role, Long idClient, Long idLivreur) {
        try (Connection conn = Database.getConnection()) {
            String sql = "INSERT INTO utilisateur (login, password_hash, role, id_client, id_livreur) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, login);
                stmt.setString(2, passwordEncoder.encode(password));
                stmt.setString(3, role);
                if (idClient != null) {
                    stmt.setLong(4, idClient);
                } else {
                    stmt.setNull(4, java.sql.Types.BIGINT);
                }
                if (idLivreur != null) {
                    stmt.setLong(5, idLivreur);
                } else {
                    stmt.setNull(5, java.sql.Types.BIGINT);
                }
                return stmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean userExists(String login) {
        try (Connection conn = Database.getConnection()) {
            String sql = "SELECT 1 FROM utilisateur WHERE login = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, login);
                try (ResultSet rs = stmt.executeQuery()) {
                    return rs.next();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void changePassword(long idUtilisateur, String newPassword) {
        try (Connection conn = Database.getConnection()) {
            String sql = "UPDATE utilisateur SET password_hash = ? WHERE id_utilisateur = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, passwordEncoder.encode(newPassword));
                stmt.setLong(2, idUtilisateur);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
