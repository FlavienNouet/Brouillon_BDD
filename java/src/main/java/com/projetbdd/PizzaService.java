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
        public final ClientOrderCount bestClient;
        public final DelivererStats worstDeliverer;
        public final PizzaStats mostOrderedPizza;
        public final PizzaStats leastOrderedPizza;
        public final IngredientStat favoriteIngredient;

        public AdminDashboardData(List<VehicleUnusedOption> vehiclesNeverUsed,
                                  List<ClientOrderCount> ordersPerClient,
                                  BigDecimal averageOrdersPerClient,
                                  List<ClientOrderCount> clientsAboveAverage,
                                  ClientOrderCount bestClient,
                                  DelivererStats worstDeliverer,
                                  PizzaStats mostOrderedPizza,
                                  PizzaStats leastOrderedPizza,
                                  IngredientStat favoriteIngredient) {
            this.vehiclesNeverUsed = vehiclesNeverUsed;
            this.ordersPerClient = ordersPerClient;
            this.averageOrdersPerClient = averageOrdersPerClient;
            this.clientsAboveAverage = clientsAboveAverage;
            this.bestClient = bestClient;
            this.worstDeliverer = worstDeliverer;
            this.mostOrderedPizza = mostOrderedPizza;
            this.leastOrderedPizza = leastOrderedPizza;
            this.favoriteIngredient = favoriteIngredient;
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
                listClientsAboveAverage(),
                getBestClient(),
                getWorstDeliverer(),
                getMostOrderedPizza(),
                getLeastOrderedPizza(),
                getFavoriteIngredient()
        );
    }

    public static final class DelivererStats {
        public final long idLivreur;
        public final String nomLivreur;
        public final long nbRetards;
        public final String typeVehicule;
        public final String immatriculation;

        public DelivererStats(long idLivreur, String nomLivreur, long nbRetards, String typeVehicule, String immatriculation) {
            this.idLivreur = idLivreur;
            this.nomLivreur = nomLivreur;
            this.nbRetards = nbRetards;
            this.typeVehicule = typeVehicule;
            this.immatriculation = immatriculation;
        }
    }

    public static final class PizzaStats {
        public final long idPizza;
        public final String nomPizza;
        public final long nbCommandes;

        public PizzaStats(long idPizza, String nomPizza, long nbCommandes) {
            this.idPizza = idPizza;
            this.nomPizza = nomPizza;
            this.nbCommandes = nbCommandes;
        }
    }

    public static final class IngredientStat {
        public final long idIngredient;
        public final String nomIngredient;
        public final long nbOccurrences;

        public IngredientStat(long idIngredient, String nomIngredient, long nbOccurrences) {
            this.idIngredient = idIngredient;
            this.nomIngredient = nomIngredient;
            this.nbOccurrences = nbOccurrences;
        }
    }

    public ClientOrderCount getBestClient() throws SQLException {
        final String sql = """
                SELECT c.id_client, c.nom, COUNT(co.id_commande) AS nb_commandes
                FROM client c
                LEFT JOIN commande co ON co.id_client = c.id_client
                GROUP BY c.id_client, c.nom
                ORDER BY nb_commandes DESC
                LIMIT 1
                """;
        try (Connection cn = Database.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return new ClientOrderCount(
                        rs.getLong("id_client"),
                        rs.getString("nom"),
                        rs.getLong("nb_commandes")
                );
            }
        }
        return null;
    }

    public DelivererStats getWorstDeliverer() throws SQLException {
        final String sql = """
                SELECT l.id_livreur, l.nom, 
                       COUNT(CASE WHEN TIMESTAMPDIFF(MINUTE, c.date_commande, c.date_livraison_reelle) > 30 THEN 1 END) AS nb_retards,
                       COALESCE(v.type_vehicule, 'N/A') AS type_vehicule,
                       COALESCE(v.immatriculation, 'N/A') AS immatriculation
                FROM livreur l
                LEFT JOIN commande c ON c.id_livreur = l.id_livreur
                LEFT JOIN vehicule v ON v.id_vehicule = l.id_vehicule
                GROUP BY l.id_livreur, l.nom, v.type_vehicule, v.immatriculation
                HAVING COUNT(CASE WHEN TIMESTAMPDIFF(MINUTE, c.date_commande, c.date_livraison_reelle) > 30 THEN 1 END) > 0
                ORDER BY nb_retards DESC
                LIMIT 1
                """;
        try (Connection cn = Database.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return new DelivererStats(
                        rs.getLong("id_livreur"),
                        rs.getString("nom"),
                        rs.getLong("nb_retards"),
                        rs.getString("type_vehicule"),
                        rs.getString("immatriculation")
                );
            }
        }
        return null;
    }

    public PizzaStats getMostOrderedPizza() throws SQLException {
        final String sql = """
                SELECT p.id_pizza, p.nom, COUNT(cl.id_ligne) AS nb_commandes
                FROM pizza p
                LEFT JOIN commande_ligne cl ON cl.id_pizza = p.id_pizza
                GROUP BY p.id_pizza, p.nom
                ORDER BY nb_commandes DESC
                LIMIT 1
                """;
        try (Connection cn = Database.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return new PizzaStats(
                        rs.getLong("id_pizza"),
                        rs.getString("nom"),
                        rs.getLong("nb_commandes")
                );
            }
        }
        return null;
    }

    public PizzaStats getLeastOrderedPizza() throws SQLException {
        final String sql = """
                SELECT p.id_pizza, p.nom, COUNT(cl.id_ligne) AS nb_commandes
                FROM pizza p
                LEFT JOIN commande_ligne cl ON cl.id_pizza = p.id_pizza
                GROUP BY p.id_pizza, p.nom
                ORDER BY nb_commandes ASC
                LIMIT 1
                """;
        try (Connection cn = Database.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return new PizzaStats(
                        rs.getLong("id_pizza"),
                        rs.getString("nom"),
                        rs.getLong("nb_commandes")
                );
            }
        }
        return null;
    }

    public IngredientStat getFavoriteIngredient() throws SQLException {
        final String sql = """
                SELECT i.id_ingredient, i.nom, COUNT(cl.id_ligne) AS nb_occurrences
                FROM ingredient i
                LEFT JOIN pizza_ingredient pi ON pi.id_ingredient = i.id_ingredient
                LEFT JOIN commande_ligne cl ON cl.id_pizza = pi.id_pizza
                GROUP BY i.id_ingredient, i.nom
                ORDER BY nb_occurrences DESC
                LIMIT 1
                """;
        try (Connection cn = Database.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return new IngredientStat(
                        rs.getLong("id_ingredient"),
                        rs.getString("nom"),
                        rs.getLong("nb_occurrences")
                );
            }
        }
        return null;
    }

    public static final class FidelityInfo {
        public final long totalPizzas;
        public final long pizzasUntilFree;
        public final long nextFreeNumber;

        public FidelityInfo(long totalPizzas, long pizzasUntilFree, long nextFreeNumber) {
            this.totalPizzas = totalPizzas;
            this.pizzasUntilFree = pizzasUntilFree;
            this.nextFreeNumber = nextFreeNumber;
        }
    }

    public FidelityInfo getFidelityInfo(long idClient) throws SQLException {
        final String sql = """
                SELECT COALESCE(SUM(cl.quantite), 0) AS total
                FROM commande c
                JOIN commande_ligne cl ON cl.id_commande = c.id_commande
                WHERE c.id_client = ?
                """;
        try (Connection cn = Database.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setLong(1, idClient);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    long totalPizzas = rs.getLong("total");
                    long nextFreeNumber = ((totalPizzas / 10) + 1) * 10;
                    long pizzasUntilFree = nextFreeNumber - totalPizzas;
                    return new FidelityInfo(totalPizzas, pizzasUntilFree, nextFreeNumber);
                }
            }
        }
        return new FidelityInfo(0, 10, 10);
    }

    public static final class DeliverySlip {
        public final long idCommande;
        public final String nomClient;
        public final String nomLivreur;
        public final String typeVehicule;
        public final String immatriculation;
        public final String dateCommande;
        public final String datePreveue;
        public final String dateReelle;
        public final String nomPizza;
        public final BigDecimal prixBase;
        public final long quantite;
        public final boolean estGratuite;
        public final long minutesRetard;

        public DeliverySlip(long idCommande, String nomClient, String nomLivreur, String typeVehicule,
                           String immatriculation, String dateCommande, String datePreveue, String dateReelle,
                           String nomPizza, BigDecimal prixBase, long quantite, boolean estGratuite, long minutesRetard) {
            this.idCommande = idCommande;
            this.nomClient = nomClient;
            this.nomLivreur = nomLivreur;
            this.typeVehicule = typeVehicule;
            this.immatriculation = immatriculation;
            this.dateCommande = dateCommande;
            this.datePreveue = datePreveue;
            this.dateReelle = dateReelle;
            this.nomPizza = nomPizza;
            this.prixBase = prixBase;
            this.quantite = quantite;
            this.estGratuite = estGratuite;
            this.minutesRetard = minutesRetard;
        }
    }

    public List<DeliverySlip> getDeliverySlips(long idLivreur) throws SQLException {
        List<DeliverySlip> slips = new ArrayList<>();
        final String sql = """
                SELECT c.id_commande, cl.nom AS nomClient, l.nom AS nomLivreur,
                       COALESCE(v.type_vehicule, 'N/A') AS typeVehicule,
                       COALESCE(v.immatriculation, 'N/A') AS immatriculation,
                       DATE_FORMAT(c.date_commande, '%Y-%m-%d %H:%i') AS dateCommande,
                       DATE_FORMAT(c.date_livraison_prevue, '%Y-%m-%d %H:%i') AS datePreveue,
                       DATE_FORMAT(c.date_livraison_reelle, '%Y-%m-%d %H:%i') AS dateReelle,
                       p.nom AS nomPizza, p.prix_base, cld.quantite, cld.est_gratuite,
                       TIMESTAMPDIFF(MINUTE, c.date_livraison_prevue, c.date_livraison_reelle) AS minutesRetard
                FROM commande c
                JOIN client cl ON cl.id_client = c.id_client
                JOIN livreur l ON l.id_livreur = c.id_livreur
                LEFT JOIN vehicule v ON v.id_vehicule = l.id_vehicule
                JOIN commande_ligne cld ON cld.id_commande = c.id_commande
                JOIN pizza p ON p.id_pizza = cld.id_pizza
                WHERE c.id_livreur = ?
                ORDER BY c.date_commande DESC, c.id_commande, cld.id_ligne
                """;
        try (Connection cn = Database.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setLong(1, idLivreur);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    long minutesRetard = rs.getLong("minutesRetard");
                    slips.add(new DeliverySlip(
                            rs.getLong("id_commande"),
                            rs.getString("nomClient"),
                            rs.getString("nomLivreur"),
                            rs.getString("typeVehicule"),
                            rs.getString("immatriculation"),
                            rs.getString("dateCommande"),
                            rs.getString("datePreveue"),
                            rs.getString("dateReelle"),
                            rs.getString("nomPizza"),
                            rs.getBigDecimal("prix_base"),
                            rs.getLong("quantite"),
                            rs.getBoolean("est_gratuite"),
                            minutesRetard > 0 ? minutesRetard : 0
                    ));
                }
            }
        }
        return slips;
    }
}
