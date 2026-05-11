package com.projetbdd;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PizzaService {

    public static final class VehicleUnusedOption {
        public final long id;
        public final String typeVehicule;
        public final String immatriculation;
        public final boolean actif;

        public VehicleUnusedOption(long id, String typeVehicule, String immatriculation, boolean actif) {
            this.id = id;
            this.typeVehicule = typeVehicule;
            this.immatriculation = immatriculation;
            this.actif = actif;
        }
    }

    public static final class ClientOrderCount {
        public final long idClient;
        public final String nom;
        public final long nbCommandes;

        public ClientOrderCount(long idClient, String nom, long nbCommandes) {
            this.idClient = idClient;
            this.nom = nom;
            this.nbCommandes = nbCommandes;
        }
    }

    public static final class AdminDashboardData {
        public final List<VehicleUnusedOption> vehiclesNeverUsed;
        public final List<ClientOrderCount> ordersPerClient;
        public final BigDecimal averageOrdersPerClient;
        public final List<ClientOrderCount> clientsAboveAverage;

        public AdminDashboardData(List<VehicleUnusedOption> vehiclesNeverUsed,
                                  List<ClientOrderCount> ordersPerClient,
                                  BigDecimal averageOrdersPerClient,
                                  List<ClientOrderCount> clientsAboveAverage) {
            this.vehiclesNeverUsed = vehiclesNeverUsed;
            this.ordersPerClient = ordersPerClient;
            this.averageOrdersPerClient = averageOrdersPerClient;
            this.clientsAboveAverage = clientsAboveAverage;
        }
    }

    public static final class ClientOption {
        public final long id;
        public final String nom;
        public final String email;
        public final BigDecimal solde;

        public ClientOption(long id, String nom, String email, BigDecimal solde) {
            this.id = id;
            this.nom = nom;
            this.email = email;
            this.solde = solde;
        }

        @Override
        public String toString() {
            return "#" + id + " - " + nom + " (solde: " + solde + ")";
        }
    }

    public static final class LivreurOption {
        public final long id;
        public final String nom;
        public final String typeVehicule;
        public final String immatriculation;

        public LivreurOption(long id, String nom, String typeVehicule, String immatriculation) {
            this.id = id;
            this.nom = nom;
            this.typeVehicule = typeVehicule;
            this.immatriculation = immatriculation;
        }

        @Override
        public String toString() {
            return "#" + id + " - " + nom + " | " + typeVehicule + " (" + immatriculation + ")";
        }
    }

    public static final class PizzaOption {
        public final long id;
        public final String nom;
        public final BigDecimal prixBase;
        public final String ingredients;

        public PizzaOption(long id, String nom, BigDecimal prixBase, String ingredients) {
            this.id = id;
            this.nom = nom;
            this.prixBase = prixBase;
            this.ingredients = ingredients;
        }

        @Override
        public String toString() {
            return nom + " - " + prixBase + " EUR";
        }
    }

    public static final class TailleOption {
        public final String code;
        public final BigDecimal coefficient;

        public TailleOption(String code, BigDecimal coefficient) {
            this.code = code;
            this.coefficient = coefficient;
        }

        @Override
        public String toString() {
            return code + " (coef " + coefficient + ")";
        }
    }

    public List<ClientOption> listClients() throws SQLException {
        List<ClientOption> clients = new ArrayList<>();
        final String sql = "SELECT id_client, nom, email, solde FROM client ORDER BY id_client";
        try (Connection cn = Database.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                clients.add(new ClientOption(
                        rs.getLong("id_client"),
                        rs.getString("nom"),
                        rs.getString("email"),
                        rs.getBigDecimal("solde")
                ));
            }
        }
        return clients;
    }

    public List<LivreurOption> listLivreurs() throws SQLException {
        List<LivreurOption> livreurs = new ArrayList<>();
        final String sql = """
                SELECT l.id_livreur,
                       l.nom,
                       COALESCE(v.type_vehicule, 'aucun') AS type_vehicule,
                       COALESCE(v.immatriculation, '-') AS immatriculation
                FROM livreur l
                LEFT JOIN vehicule v ON v.id_vehicule = l.id_vehicule
                ORDER BY l.id_livreur
                """;
        try (Connection cn = Database.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                livreurs.add(new LivreurOption(
                        rs.getLong("id_livreur"),
                        rs.getString("nom"),
                        rs.getString("type_vehicule"),
                        rs.getString("immatriculation")
                ));
            }
        }
        return livreurs;
    }

    public List<PizzaOption> listPizzas() throws SQLException {
        List<PizzaOption> pizzas = new ArrayList<>();
        final String sql = """
                SELECT p.id_pizza,
                       p.nom,
                       p.prix_base,
                       COALESCE(GROUP_CONCAT(i.nom ORDER BY i.nom SEPARATOR ', '), '') AS ingredients
                FROM pizza p
                LEFT JOIN pizza_ingredient pi ON pi.id_pizza = p.id_pizza
                LEFT JOIN ingredient i ON i.id_ingredient = pi.id_ingredient
                GROUP BY p.id_pizza, p.nom, p.prix_base
                ORDER BY p.id_pizza
                """;
        try (Connection cn = Database.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                pizzas.add(new PizzaOption(
                        rs.getLong("id_pizza"),
                        rs.getString("nom"),
                        rs.getBigDecimal("prix_base"),
                        rs.getString("ingredients")
                ));
            }
        }
        return pizzas;
    }

    public List<TailleOption> listTailles() throws SQLException {
        List<TailleOption> tailles = new ArrayList<>();
        final String sql = "SELECT code_taille, coefficient_prix FROM taille ORDER BY code_taille";
        try (Connection cn = Database.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                tailles.add(new TailleOption(
                        rs.getString("code_taille"),
                        rs.getBigDecimal("coefficient_prix")
                ));
            }
        }
        return tailles;
    }

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

            long idCommande = cs.getLong(5);
            boolean isNull = cs.wasNull();
            if (!isNull && idCommande > 0) {
                return idCommande;
            }

            String motifRefus = lireDernierRefusClient(idClient);
            if (motifRefus != null && !motifRefus.isBlank()) {
                throw new SQLException("Commande refusee: " + motifRefus);
            }

            throw new SQLException("Commande refusee: aucun id de commande retourne.");
        }
    }

    private String lireDernierRefusClient(long idClient) throws SQLException {
        final String sql = """
                SELECT CONCAT(motif, ' | requis=', montant_requis, ' | solde=', solde_disponible) AS detail
                FROM refus_commande
                WHERE id_client = ?
                ORDER BY date_refus DESC, id_refus DESC
                LIMIT 1
                """;
        try (Connection cn = Database.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setLong(1, idClient);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("detail");
                }
            }
        }
        return null;
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

    public List<VehicleUnusedOption> listVehiclesNeverUsed() throws SQLException {
        List<VehicleUnusedOption> vehicles = new ArrayList<>();
        final String sql = """
                SELECT v.id_vehicule,
                       v.type_vehicule,
                       COALESCE(v.immatriculation, '-') AS immatriculation,
                       v.actif
                FROM vehicule v
                LEFT JOIN livreur l ON l.id_vehicule = v.id_vehicule
                LEFT JOIN commande c ON c.id_livreur = l.id_livreur
                GROUP BY v.id_vehicule, v.type_vehicule, v.immatriculation, v.actif
                HAVING COUNT(c.id_commande) = 0
                ORDER BY v.id_vehicule
                """;
        try (Connection cn = Database.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                vehicles.add(new VehicleUnusedOption(
                        rs.getLong("id_vehicule"),
                        rs.getString("type_vehicule"),
                        rs.getString("immatriculation"),
                        rs.getBoolean("actif")
                ));
            }
        }
        return vehicles;
    }

    public List<ClientOrderCount> listOrdersPerClient() throws SQLException {
        List<ClientOrderCount> stats = new ArrayList<>();
        final String sql = """
                SELECT c.id_client, c.nom, COUNT(co.id_commande) AS nb_commandes
                FROM client c
                LEFT JOIN commande co ON co.id_client = c.id_client
                GROUP BY c.id_client, c.nom
                ORDER BY nb_commandes DESC, c.id_client
                """;
        try (Connection cn = Database.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                stats.add(new ClientOrderCount(
                        rs.getLong("id_client"),
                        rs.getString("nom"),
                        rs.getLong("nb_commandes")
                ));
            }
        }
        return stats;
    }

    public BigDecimal getAverageOrdersPerClient() throws SQLException {
        final String sql = """
                SELECT ROUND(COALESCE(AVG(nb), 0), 2) AS moyenne_commandes
                FROM (
                    SELECT COUNT(*) AS nb
                    FROM commande
                    GROUP BY id_client
                ) x
                """;
        try (Connection cn = Database.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                BigDecimal value = rs.getBigDecimal("moyenne_commandes");
                return value != null ? value : BigDecimal.ZERO;
            }
        }
        return BigDecimal.ZERO;
    }

    public List<ClientOrderCount> listClientsAboveAverage() throws SQLException {
        List<ClientOrderCount> clients = new ArrayList<>();
        final String sql = """
                WITH stats AS (
                    SELECT id_client, COUNT(*) AS nb
                    FROM commande
                    GROUP BY id_client
                ),
                moy AS (
                    SELECT AVG(nb) AS moyenne FROM stats
                )
                SELECT c.id_client, c.nom, s.nb
                FROM stats s
                JOIN moy m ON s.nb > m.moyenne
                JOIN client c ON c.id_client = s.id_client
                ORDER BY s.nb DESC, c.id_client
                """;
        try (Connection cn = Database.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                clients.add(new ClientOrderCount(
                        rs.getLong("id_client"),
                        rs.getString("nom"),
                        rs.getLong("nb")
                ));
            }
        }
        return clients;
    }

    public AdminDashboardData loadAdminDashboardData() throws SQLException {
        return new AdminDashboardData(
                listVehiclesNeverUsed(),
                listOrdersPerClient(),
                getAverageOrdersPerClient(),
                listClientsAboveAverage()
        );
    }
}
