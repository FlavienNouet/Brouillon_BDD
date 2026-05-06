package com.projetbdd;

import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PizzaService {

    public void rechargerCompte(long idClient, BigDecimal montant) throws SQLException {
        final String sql = "CALL fn_recharger_compte(?, ?)";
        try (Connection cn = Database.getConnection();
             CallableStatement cs = cn.prepareCall(sql)) {
            cs.setLong(1, idClient);
            cs.setBigDecimal(2, montant);
            cs.execute();
        }
    }

    public long passerCommande(long idClient, long idLivreur, String lignesJson, int minutesLivraison) throws SQLException {
        final String sql = "CALL fn_passer_commande(?, ?, ?, ?, ?)";
        try (Connection cn = Database.getConnection();
             CallableStatement cs = cn.prepareCall(sql)) {
            cs.setLong(1, idClient);
            cs.setLong(2, idLivreur);
            cs.setString(3, lignesJson);
            cs.setInt(4, minutesLivraison);
            cs.registerOutParameter(5, java.sql.Types.BIGINT);
            cs.execute();

            Long idCommande = cs.getLong(5);
            if (idCommande != null && idCommande > 0) {
                return idCommande;
            }

            throw new SQLException("Commande refusee: solde insuffisant.");
        }
    }

    public BigDecimal lireSolde(long idClient) throws SQLException {
        final String sql = "SELECT solde FROM client WHERE id_client = ?";
        try (Connection cn = Database.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setLong(1, idClient);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getBigDecimal(1);
                }
            }
        }
        throw new SQLException("Client introuvable: " + idClient);
    }
}
