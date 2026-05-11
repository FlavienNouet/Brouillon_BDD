package com.projetbdd;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class UserService {
    public static final class UserAccountInfo {
        public final long idUtilisateur;
        public final String login;
        public final String role;
        public final Long idClient;
        public final Long idLivreur;
        public final boolean actif;
        public final Timestamp dateCreation;

        public UserAccountInfo(long idUtilisateur, String login, String role, Long idClient, Long idLivreur, boolean actif, Timestamp dateCreation) {
            this.idUtilisateur = idUtilisateur;
            this.login = login;
            this.role = role;
            this.idClient = idClient;
            this.idLivreur = idLivreur;
            this.actif = actif;
            this.dateCreation = dateCreation;
        }
    }

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

    public List<UserAccountInfo> listUsers() throws SQLException {
        List<UserAccountInfo> users = new ArrayList<>();
        String sql = "SELECT id_utilisateur, login, role, id_client, id_livreur, actif, date_creation FROM utilisateur ORDER BY id_utilisateur";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                users.add(new UserAccountInfo(
                        rs.getLong("id_utilisateur"),
                        rs.getString("login"),
                        rs.getString("role"),
                        rs.getObject("id_client") != null ? rs.getLong("id_client") : null,
                        rs.getObject("id_livreur") != null ? rs.getLong("id_livreur") : null,
                        rs.getBoolean("actif"),
                        rs.getTimestamp("date_creation")
                ));
            }
        }
        return users;
    }

    public boolean deleteUser(long idUtilisateur) {
        try (Connection conn = Database.getConnection()) {
            // Start transaction
            conn.setAutoCommit(false);
            try {
                // First, fetch the utilisateur to get linked client/livreur IDs
                Long idClient = null;
                Long idLivreur = null;
                String fetchSql = "SELECT id_client, id_livreur FROM utilisateur WHERE id_utilisateur = ?";
                try (PreparedStatement fetchStmt = conn.prepareStatement(fetchSql)) {
                    fetchStmt.setLong(1, idUtilisateur);
                    try (ResultSet rs = fetchStmt.executeQuery()) {
                        if (rs.next()) {
                            if (rs.getObject("id_client") != null) {
                                idClient = rs.getLong("id_client");
                            }
                            if (rs.getObject("id_livreur") != null) {
                                idLivreur = rs.getLong("id_livreur");
                            }
                        }
                    }
                }

                // Delete linked client if exists
                if (idClient != null) {
                    String deleteClientSql = "DELETE FROM client WHERE id_client = ?";
                    try (PreparedStatement clientStmt = conn.prepareStatement(deleteClientSql)) {
                        clientStmt.setLong(1, idClient);
                        clientStmt.executeUpdate();
                    }
                }

                // Delete linked livreur if exists
                if (idLivreur != null) {
                    String deleteLivreurSql = "DELETE FROM livreur WHERE id_livreur = ?";
                    try (PreparedStatement livreurStmt = conn.prepareStatement(deleteLivreurSql)) {
                        livreurStmt.setLong(1, idLivreur);
                        livreurStmt.executeUpdate();
                    }
                }

                // Finally, delete the utilisateur
                String deleteUtilisateurSql = "DELETE FROM utilisateur WHERE id_utilisateur = ?";
                try (PreparedStatement stmt = conn.prepareStatement(deleteUtilisateurSql)) {
                    stmt.setLong(1, idUtilisateur);
                    int result = stmt.executeUpdate();
                    conn.commit();
                    return result > 0;
                }
            } catch (SQLException e) {
                conn.rollback();
                e.printStackTrace();
                return false;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
