package com.projetbdd;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JTable;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.RowFilter;
import javax.swing.SwingWorker;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class MainFrame extends JFrame {
    private final PizzaService service = new PizzaService();
    private final UserService userService = new UserService();
    private final Session session = Session.getInstance();

    private final JComboBox<PizzaService.ClientOption> clientCombo = new JComboBox<>();
    private final JComboBox<PizzaService.LivreurOption> livreurCombo = new JComboBox<>();
    private final JComboBox<PizzaService.PizzaOption> pizzaCombo = new JComboBox<>();
    private final JComboBox<PizzaService.TailleOption> tailleCombo = new JComboBox<>();
    private final JSpinner quantiteSpinner = new JSpinner();

    private final JLabel pizzaDetailsLabel = new JLabel("Selectionne une pizza pour voir les ingredients.");
    private final JLabel panierTotalLabel = new JLabel("Total estime panier: 0.00 EUR");
    private final JLabel fidelityLabel = new JLabel("Chargement...");

    private final DefaultListModel<CartLine> cartModel = new DefaultListModel<>();
    private final JList<CartLine> cartList = new JList<>(cartModel);

    private final JTextArea outputArea = new JTextArea();
    private final JTextArea dashboardArea = new JTextArea();
    private final JLabel vehiclesNeverUsedValueLabel = new JLabel("0");
    private final JLabel averageOrdersValueLabel = new JLabel("0.00");
    private final JLabel aboveAverageValueLabel = new JLabel("0");
    private final JLabel totalClientsValueLabel = new JLabel("0");
    private final DefaultTableModel adminUsersTableModel = new DefaultTableModel(new Object[]{"ID", "Login", "Role", "Client", "Livreur", "Actif", "Création"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final TableRowSorter<DefaultTableModel> adminUsersSorter = new TableRowSorter<>(adminUsersTableModel);
    private final JLabel adminSelectedUserLabel = new JLabel("Aucun utilisateur sélectionné");
    private final JLabel adminSelectedRoleLabel = new JLabel("-");
    private final JLabel adminSelectedStatusLabel = new JLabel("-");
    private final JLabel adminSelectedLinkedLabel = new JLabel("-");
    private final JTextField adminSearchField = new JTextField();
    private final DefaultTableModel vehiclesTableModel = new DefaultTableModel(new Object[]{"ID", "Type", "Immatriculation", "Actif"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final DefaultTableModel ordersTableModel = new DefaultTableModel(new Object[]{"ID Client", "Client", "Nombre de commandes"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final DefaultTableModel aboveAverageTableModel = new DefaultTableModel(new Object[]{"ID Client", "Client", "Commandes"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    private final JLabel bestClientLabel = new JLabel("Aucun");
    private final JLabel worstDelivererLabel = new JLabel("Aucun");
    private final JLabel mostOrderedPizzaLabel = new JLabel("Aucune");
    private final JLabel leastOrderedPizzaLabel = new JLabel("Aucune");
    private final JLabel favoriteIngredientLabel = new JLabel("Aucun");

    private final JTextField montantRechargeField = new JTextField("20.00");
    private final JTextField minutesLivraisonField = new JTextField("25");

    public MainFrame() {
        super("Projet BDD - Pizza Prepayee");
        setSize(1024, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(920, 640));
        setLayout(new BorderLayout(0, 12));

        applyLookAndFeelDefaults();

        GradientHeader header = new GradientHeader();
        header.setPreferredSize(new Dimension(0, 96));
        header.setLayout(new BorderLayout());
        header.setBorder(new EmptyBorder(16, 20, 16, 20));

        JPanel titlePanel = new JPanel();
        titlePanel.setOpaque(false);
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Gestion Commandes Pizza");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));

        String subtitle = "Plateforme prepayee - " + session.getLogin() + " [" + session.getRole() + "]";
        JLabel subtitleLabel = new JLabel(subtitle);
        subtitleLabel.setForeground(new Color(227, 238, 255));
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        titlePanel.add(title);
        titlePanel.add(Box.createVerticalStrut(4));
        titlePanel.add(subtitleLabel);

        JButton logoutBtn = new JButton("Déconnexion");
        logoutBtn.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        logoutBtn.setBackground(new Color(200, 50, 50));
        logoutBtn.setForeground(Color.WHITE);
        logoutBtn.setFocusPainted(false);
        logoutBtn.addActionListener(e -> handleLogout());

        JPanel headerCenter = new JPanel();
        headerCenter.setOpaque(false);
        headerCenter.add(titlePanel);

        header.add(titlePanel, BorderLayout.CENTER);
        header.add(logoutBtn, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tabs.setBackground(new Color(243, 246, 251));

        // Ajouter les tabs selon le rôle
        if (session.isClient()) {
            tabs.addTab("Commande", buildClientOrderTab());
            tabs.addTab("Mon compte", buildClientAccountTab());
        } else if (session.isLivreur()) {
            tabs.addTab("Commandes à livrer", buildDeliveryTab());
            tabs.addTab("Fiche de livraison", buildDeliverySlipTab());
        } else if (session.isAdmin()) {
            tabs.addTab("Commande", buildOrderTab());
            tabs.addTab("Mon compte", buildAccountTab());
            tabs.addTab("Tableau de bord", buildDashboardTab());
            tabs.addTab("Gestion utilisateurs", buildAdminUserTab());
        }

        add(tabs, BorderLayout.CENTER);

        refreshData();
    }

    private void handleLogout() {
        session.logout();
        dispose();
        LoginFrame loginFrame = new LoginFrame();
        loginFrame.setVisible(true);
    }

    private JPanel buildClientOrderTab() {
        JPanel tab = new JPanel(new BorderLayout(12, 12));
        tab.setBorder(new EmptyBorder(14, 14, 14, 14));
        tab.setBackground(new Color(243, 246, 251));

        JPanel pizzaCard = buildCard("Choix pizza");
        JPanel pizzaGrid = new JPanel(new GridLayout(3, 2, 10, 10));
        pizzaGrid.setOpaque(false);

        setupCombo(pizzaCombo);
        setupCombo(tailleCombo);
        quantiteSpinner.setModel(new SpinnerNumberModel(1, 1, 50, 1));

        pizzaGrid.add(buildLabel("Pizza"));
        pizzaGrid.add(pizzaCombo);
        pizzaGrid.add(buildLabel("Taille"));
        pizzaGrid.add(tailleCombo);
        pizzaGrid.add(buildLabel("Quantite"));
        pizzaGrid.add(quantiteSpinner);

        pizzaDetailsLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        pizzaDetailsLabel.setForeground(new Color(49, 66, 100));
        pizzaDetailsLabel.setBorder(new EmptyBorder(8, 0, 8, 0));

        JButton addLineBtn = new JButton("Ajouter au panier");
        stylePrimaryButton(addLineBtn, new Color(23, 122, 89));
        addLineBtn.addActionListener(e -> addLineToCart());

        JPanel pizzaBottom = new JPanel(new BorderLayout(8, 8));
        pizzaBottom.setOpaque(false);
        pizzaBottom.add(pizzaDetailsLabel, BorderLayout.CENTER);
        pizzaBottom.add(addLineBtn, BorderLayout.EAST);

        pizzaCard.add(pizzaGrid, BorderLayout.CENTER);
        pizzaCard.add(pizzaBottom, BorderLayout.SOUTH);

        JPanel cartCard = buildCard("Panier commande");
        cartList.setFont(new Font("Consolas", Font.PLAIN, 12));
        cartList.setVisibleRowCount(6);
        JScrollPane cartScroll = new JScrollPane(cartList);
        cartScroll.setBorder(BorderFactory.createLineBorder(new Color(208, 216, 229)));

        JButton removeBtn = new JButton("Retirer");
        JButton clearBtn = new JButton("Vider");
        JButton submitBtn = new JButton("Valider commande");
        stylePrimaryButton(removeBtn, new Color(191, 76, 76));
        stylePrimaryButton(clearBtn, new Color(116, 126, 147));
        stylePrimaryButton(submitBtn, new Color(0, 159, 117));
        removeBtn.addActionListener(e -> removeSelectedLine());
        clearBtn.addActionListener(e -> clearCart());
        submitBtn.addActionListener(e -> passerCommandeClient());

        JPanel cartButtons = new JPanel(new GridLayout(1, 3, 10, 10));
        cartButtons.setOpaque(false);
        cartButtons.add(removeBtn);
        cartButtons.add(clearBtn);
        cartButtons.add(submitBtn);

        panierTotalLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        panierTotalLabel.setForeground(new Color(19, 72, 171));

        JPanel cartBottom = new JPanel();
        cartBottom.setOpaque(false);
        cartBottom.setLayout(new BoxLayout(cartBottom, BoxLayout.Y_AXIS));
        cartBottom.add(Box.createVerticalStrut(6));
        cartBottom.add(panierTotalLabel);
        cartBottom.add(Box.createVerticalStrut(8));
        cartBottom.add(cartButtons);

        cartCard.add(cartScroll, BorderLayout.CENTER);
        cartCard.add(cartBottom, BorderLayout.SOUTH);

        JPanel configCard = buildCard("Parametres livraison");
        JPanel configGrid = new JPanel(new GridLayout(1, 2, 10, 10));
        configGrid.setOpaque(false);

        setupField(minutesLivraisonField);
        configGrid.add(buildLabel("Minutes livraison"));
        configGrid.add(minutesLivraisonField);
        configCard.add(configGrid, BorderLayout.CENTER);

        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.add(pizzaCard);
        left.add(Box.createVerticalStrut(10));
        left.add(configCard);

        JPanel right = new JPanel();
        right.setOpaque(false);
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
        
        JPanel fidelityCard = buildCard("Programme de fidelite");
        fidelityLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        fidelityLabel.setForeground(new Color(236, 143, 25));
        fidelityCard.add(fidelityLabel, BorderLayout.CENTER);
        right.add(fidelityCard);
        right.add(Box.createVerticalStrut(10));
        
        right.add(cartCard);
        right.add(Box.createVerticalStrut(10));

        JPanel output = buildCard("Resultat");
        outputArea.setEditable(false);
        outputArea.setLineWrap(true);
        outputArea.setWrapStyleWord(true);
        outputArea.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        outputArea.setBackground(new Color(249, 251, 254));
        outputArea.setText("Pret. Selectionne des pizzas pour creer une commande.");
        JScrollPane outputScroll = new JScrollPane(outputArea);
        outputScroll.setBorder(BorderFactory.createLineBorder(new Color(208, 216, 229)));
        output.add(outputScroll, BorderLayout.CENTER);
        right.add(output);

        JPanel center = new JPanel(new GridLayout(1, 2, 12, 0));
        center.setOpaque(false);
        center.add(left);
        center.add(right);

        tab.add(center, BorderLayout.CENTER);

        pizzaCombo.addActionListener(e -> refreshPizzaDetails());
        tailleCombo.addActionListener(e -> refreshPizzaDetails());

        return tab;
    }

    private JPanel buildClientAccountTab() {
        JPanel tab = new JPanel(new BorderLayout(12, 12));
        tab.setBorder(new EmptyBorder(14, 14, 14, 14));
        tab.setBackground(new Color(243, 246, 251));

        JPanel balanceCard = buildCard("Consultation solde");
        JLabel balanceValueLabel = new JLabel("Chargement...");
        balanceValueLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        balanceValueLabel.setForeground(new Color(0, 159, 117));

        JButton checkBalanceBtn = new JButton("Rafraichir");
        stylePrimaryButton(checkBalanceBtn, new Color(236, 143, 25));
        checkBalanceBtn.addActionListener(e -> {
            try {
                BigDecimal solde = service.lireSolde(session.getIdClient());
                balanceValueLabel.setText("Votre solde: " + solde + " EUR");
                showOutput("Solde consulte: " + solde + " EUR");
            } catch (Exception ex) {
                balanceValueLabel.setText("Erreur: " + ex.getMessage());
                showOutput("Erreur lecture solde: " + ex.getMessage());
            }
        });

        JPanel balanceTop = new JPanel(new BorderLayout());
        balanceTop.setOpaque(false);
        balanceTop.add(balanceValueLabel, BorderLayout.CENTER);
        balanceTop.add(checkBalanceBtn, BorderLayout.EAST);

        balanceCard.add(balanceTop, BorderLayout.CENTER);

        JPanel rechargeCard = buildCard("Recharge compte");
        JPanel rechargeGrid = new JPanel(new GridLayout(2, 2, 10, 10));
        rechargeGrid.setOpaque(false);

        setupField(montantRechargeField);

        JButton rechargeBtn = new JButton("Recharger");
        stylePrimaryButton(rechargeBtn, new Color(36, 112, 255));
        rechargeBtn.addActionListener(e -> {
            try {
                BigDecimal montant = new BigDecimal(montantRechargeField.getText().trim());
                service.rechargerCompte(session.getIdClient(), montant);
                showOutput("Recharge de " + montant + " EUR effectuee.");
                montantRechargeField.setText("20.00");
                checkBalanceBtn.doClick();
                JOptionPane.showMessageDialog(this, "Recharge effectuee.", "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                showOutput("Erreur recharge: " + ex.getMessage());
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        });

        rechargeGrid.add(buildLabel("Montant (EUR)"));
        rechargeGrid.add(montantRechargeField);
        JPanel emptyPlaceholder = new JPanel();
        emptyPlaceholder.setOpaque(false);
        rechargeGrid.add(emptyPlaceholder);
        rechargeGrid.add(rechargeBtn);
        rechargeCard.add(rechargeGrid, BorderLayout.CENTER);

        JPanel center = new JPanel();
        center.setOpaque(false);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.add(balanceCard);
        center.add(Box.createVerticalStrut(12));
        center.add(rechargeCard);
        center.add(Box.createVerticalGlue());

        tab.add(center, BorderLayout.CENTER);

        // Charger le solde initial
        try {
            BigDecimal solde = service.lireSolde(session.getIdClient());
            balanceValueLabel.setText("Votre solde: " + solde + " EUR");
        } catch (Exception ex) {
            balanceValueLabel.setText("Erreur lors du chargement du solde");
        }

        return tab;
    }

    private JPanel buildDeliveryTab() {
        JPanel tab = new JPanel(new BorderLayout(12, 12));
        tab.setBorder(new EmptyBorder(14, 14, 14, 14));
        tab.setBackground(new Color(243, 246, 251));

        JPanel ordersCard = buildCard("Commandes en attente de livraison");
        JTextArea ordersArea = new JTextArea();
        ordersArea.setEditable(false);
        ordersArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
        ordersArea.setBackground(new Color(249, 251, 254));
        ordersArea.setText("Chargement...");

        JScrollPane ordersScroll = new JScrollPane(ordersArea);
        ordersScroll.setBorder(BorderFactory.createLineBorder(new Color(208, 216, 229)));
        ordersCard.add(ordersScroll, BorderLayout.CENTER);

        JButton refreshBtn = new JButton("Rafraichir");
        stylePrimaryButton(refreshBtn, new Color(98, 84, 177));
        refreshBtn.addActionListener(e -> {
            new Thread(() -> {
                try {
                    StringBuilder sb = new StringBuilder();
                    try (Connection cn = Database.getConnection();
                         Statement st = cn.createStatement()) {
                        try (ResultSet rs = st.executeQuery(
                                "SELECT c.id_commande, c.date_commande, cl.nom, c.date_livraison_prevue, c.statut " +
                                "FROM commande c JOIN client cl ON c.id_client = cl.id_client " +
                                "WHERE c.id_livreur = " + session.getIdLivreur() + " AND c.statut IN ('cree', 'preparee')" +
                                " ORDER BY c.date_livraison_prevue")) {
                            if (!rs.isBeforeFirst()) {
                                sb.append("Aucune commande en attente de livraison.");
                            } else {
                                while (rs.next()) {
                                    sb.append("Commande #").append(rs.getLong("id_commande"))
                                            .append(" | Client: ").append(rs.getString("nom"))
                                            .append(" | Prevue: ").append(rs.getTimestamp("date_livraison_prevue"))
                                            .append(" | Statut: ").append(rs.getString("statut"))
                                            .append("\n");
                                }
                            }
                        }
                    }
                    final String result = sb.toString();
                    SwingUtilities.invokeLater(() -> ordersArea.setText(result));
                } catch (Exception ex) {
                    SwingUtilities.invokeLater(() -> ordersArea.setText("Erreur: " + ex.getMessage()));
                }
            }).start();
        });

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(refreshBtn, BorderLayout.EAST);

        tab.add(top, BorderLayout.NORTH);
        tab.add(ordersCard, BorderLayout.CENTER);

        refreshBtn.doClick();
        return tab;
    }

    private JPanel buildDeliverySlipTab() {
        JPanel tab = new JPanel(new BorderLayout(12, 12));
        tab.setBorder(new EmptyBorder(14, 14, 14, 14));
        tab.setBackground(new Color(243, 246, 251));

        JButton refreshBtn = new JButton("Rafraichir");
        stylePrimaryButton(refreshBtn, new Color(98, 84, 177));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel titleLabel = new JLabel("Fiches de livraison");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(new Color(28, 46, 92));
        header.add(titleLabel, BorderLayout.WEST);
        header.add(refreshBtn, BorderLayout.EAST);

        DefaultTableModel slipsTableModel = new DefaultTableModel(
                new Object[]{"Commande", "Client", "Pizza", "Quantité", "Gratuite", "Date Commande", "Date Prévue", "Date Réelle", "Retard (min)", "Véhicule"},
                0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable slipsTable = buildReportTable(slipsTableModel);
        JScrollPane slipsScroll = new JScrollPane(slipsTable);
        slipsScroll.setBorder(BorderFactory.createLineBorder(new Color(208, 216, 229)));

        JPanel detailsCard = buildCard("Résumé de livraison");
        JTextArea detailsArea = new JTextArea();
        detailsArea.setEditable(false);
        detailsArea.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        detailsArea.setLineWrap(true);
        detailsArea.setWrapStyleWord(true);
        detailsArea.setBackground(new Color(249, 251, 254));
        JScrollPane detailsScroll = new JScrollPane(detailsArea);
        detailsScroll.setBorder(BorderFactory.createLineBorder(new Color(208, 216, 229)));
        detailsCard.add(detailsScroll, BorderLayout.CENTER);

        refreshBtn.addActionListener(e -> {
            new Thread(() -> {
                try {
                    List<PizzaService.DeliverySlip> slips = service.getDeliverySlips(session.getIdLivreur());
                    SwingUtilities.invokeLater(() -> {
                        slipsTableModel.setRowCount(0);
                        for (PizzaService.DeliverySlip slip : slips) {
                            String retardDisplay = slip.minutesRetard > 0 ? slip.minutesRetard + " ⚠️" : "-";
                            slipsTableModel.addRow(new Object[]{
                                    slip.idCommande,
                                    slip.nomClient,
                                    slip.nomPizza,
                                    slip.quantite,
                                    slip.estGratuite ? "Oui" : "Non",
                                    slip.dateCommande,
                                    slip.datePreveue,
                                    slip.dateReelle != null ? slip.dateReelle : "En cours",
                                    retardDisplay,
                                    slip.typeVehicule + " (" + slip.immatriculation + ")"
                            });
                        }
                        
                        StringBuilder detailsText = new StringBuilder();
                        detailsText.append("FICHE DE LIVRAISON\n");
                        detailsText.append("=".repeat(70)).append("\n\n");
                        detailsText.append("Livreur: ").append(session.getLogin()).append("\n");
                        detailsText.append("Total commandes: ").append(slips.size()).append("\n\n");
                        
                        long totalRetards = slips.stream().filter(s -> s.minutesRetard > 0).count();
                        detailsText.append("Commandes avec retard: ").append(totalRetards).append(" / ").append(slips.size()).append("\n");
                        
                        long totalGratuites = slips.stream().filter(s -> s.estGratuite).count();
                        detailsText.append("Pizzas gratuites distribuées: ").append(totalGratuites).append("\n\n");
                        
                        detailsText.append("Détails des commandes:\n");
                        detailsText.append("-".repeat(70)).append("\n");
                        for (PizzaService.DeliverySlip slip : slips) {
                            detailsText.append("\n📋 Commande #").append(slip.idCommande).append("\n");
                            detailsText.append("  Client: ").append(slip.nomClient).append("\n");
                            detailsText.append("  Pizza: ").append(slip.nomPizza).append(" x").append(slip.quantite)
                                    .append(" (").append(slip.prixBase).append(" EUR)").append("\n");
                            if (slip.estGratuite) {
                                detailsText.append("  🎉 GRATUITE (fidélité ou retard)\n");
                            }
                            detailsText.append("  Commande: ").append(slip.dateCommande).append("\n");
                            detailsText.append("  Prévue: ").append(slip.datePreveue).append("\n");
                            if (slip.dateReelle != null) {
                                detailsText.append("  Livrée: ").append(slip.dateReelle).append("\n");
                                if (slip.minutesRetard > 0) {
                                    detailsText.append("  ⚠️ RETARD: ").append(slip.minutesRetard).append(" minutes\n");
                                }
                            }
                            detailsText.append("  Véhicule: ").append(slip.typeVehicule).append(" (").append(slip.immatriculation).append(")\n");
                        }
                        
                        detailsArea.setText(detailsText.toString());
                    });
                } catch (Exception ex) {
                    SwingUtilities.invokeLater(() -> detailsArea.setText("Erreur: " + ex.getMessage()));
                }
            }).start();
        });

        JPanel tableCard = buildCard("Détail des livraisons");
        tableCard.add(slipsScroll, BorderLayout.CENTER);

        JPanel center = new JPanel(new GridLayout(2, 1, 12, 12));
        center.setOpaque(false);
        center.add(tableCard);
        center.add(detailsCard);

        tab.add(header, BorderLayout.NORTH);
        tab.add(center, BorderLayout.CENTER);

        refreshBtn.doClick();
        return tab;
    }

    private JPanel buildAdminUserTab() {
        JPanel tab = new JPanel(new BorderLayout(12, 12));
        tab.setBorder(new EmptyBorder(14, 14, 14, 14));
        tab.setBackground(new Color(243, 246, 251));

        JPanel headerCard = buildCard("Gestion des comptes");
        JPanel headerRow = new JPanel(new BorderLayout(10, 10));
        headerRow.setOpaque(false);

        JPanel headerText = new JPanel();
        headerText.setOpaque(false);
        headerText.setLayout(new BoxLayout(headerText, BoxLayout.Y_AXIS));
        JLabel title = new JLabel("Comptes utilisateurs");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(new Color(28, 46, 92));
        JLabel subtitle = new JLabel("Recherche, consultation et suppression de comptes");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitle.setForeground(new Color(90, 99, 120));
        headerText.add(title);
        headerText.add(Box.createVerticalStrut(3));
        headerText.add(subtitle);

        JButton refreshBtn = new JButton("Rafraîchir");
        stylePrimaryButton(refreshBtn, new Color(98, 84, 177));

        headerRow.add(headerText, BorderLayout.WEST);
        headerRow.add(refreshBtn, BorderLayout.EAST);
        headerCard.add(headerRow, BorderLayout.CENTER);

        JPanel searchCard = buildCard("Recherche rapide");
        JPanel searchRow = new JPanel(new BorderLayout(10, 10));
        searchRow.setOpaque(false);
        searchRow.add(buildLabel("Filtrer par login ou rôle"), BorderLayout.NORTH);
        setupField(adminSearchField);
        searchRow.add(adminSearchField, BorderLayout.CENTER);
        searchCard.add(searchRow, BorderLayout.CENTER);

        JTable usersTable = new JTable(adminUsersTableModel);
        usersTable.setRowSorter(adminUsersSorter);
        usersTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        usersTable.setRowHeight(26);
        usersTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        usersTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        usersTable.setFillsViewportHeight(true);
        usersTable.setShowGrid(false);
        usersTable.setBackground(new Color(249, 251, 254));
        usersTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public java.awt.Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                java.awt.Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(245, 248, 252));
                }
                return c;
            }
        });

        JPanel tableCard = buildCard("Liste des comptes");
        JScrollPane tableScroll = new JScrollPane(usersTable);
        tableScroll.setBorder(BorderFactory.createLineBorder(new Color(208, 216, 229)));
        tableCard.add(tableScroll, BorderLayout.CENTER);

        JPanel detailCard = buildCard("Détails du compte");
        JPanel detailPanel = new JPanel();
        detailPanel.setOpaque(false);
        detailPanel.setLayout(new BoxLayout(detailPanel, BoxLayout.Y_AXIS));

        adminSelectedUserLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        adminSelectedUserLabel.setForeground(new Color(29, 44, 78));
        adminSelectedRoleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        adminSelectedStatusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        adminSelectedLinkedLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        detailPanel.add(adminSelectedUserLabel);
        detailPanel.add(Box.createVerticalStrut(12));
        detailPanel.add(buildLabel("Rôle"));
        detailPanel.add(adminSelectedRoleLabel);
        detailPanel.add(Box.createVerticalStrut(8));
        detailPanel.add(buildLabel("Statut"));
        detailPanel.add(adminSelectedStatusLabel);
        detailPanel.add(Box.createVerticalStrut(8));
        detailPanel.add(buildLabel("Liens"));
        detailPanel.add(adminSelectedLinkedLabel);
        detailPanel.add(Box.createVerticalStrut(16));

        JButton deleteBtn = new JButton("Supprimer le compte");
        stylePrimaryButton(deleteBtn, new Color(191, 76, 76));
        deleteBtn.setEnabled(false);
        detailPanel.add(deleteBtn);

        JLabel warning = new JLabel("La suppression retire définitivement le compte sélectionné.");
        warning.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        warning.setForeground(new Color(140, 72, 72));
        warning.setBorder(new EmptyBorder(12, 0, 0, 0));
        detailPanel.add(warning);
        detailCard.add(detailPanel, BorderLayout.CENTER);

        JPanel center = new JPanel(new GridLayout(1, 2, 12, 12));
        center.setOpaque(false);
        JPanel leftColumn = new JPanel(new BorderLayout(0, 12));
        leftColumn.setOpaque(false);
        leftColumn.add(searchCard, BorderLayout.NORTH);
        leftColumn.add(tableCard, BorderLayout.CENTER);
        center.add(leftColumn);
        center.add(detailCard);

        tab.add(headerCard, BorderLayout.NORTH);
        tab.add(center, BorderLayout.CENTER);

        Runnable refreshAction = () -> loadAdminUsers(usersTable, deleteBtn);
        refreshBtn.addActionListener(e -> refreshAction.run());
        adminSearchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                applyAdminUserFilter();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                applyAdminUserFilter();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                applyAdminUserFilter();
            }
        });

        usersTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateAdminUserDetails(usersTable, deleteBtn);
            }
        });

        deleteBtn.addActionListener(e -> deleteSelectedAdminUser(usersTable, deleteBtn));

        tab.addHierarchyListener(e -> {
            if (tab.isShowing() && adminUsersTableModel.getRowCount() == 0) {
                refreshAction.run();
            }
        });

        return tab;
    }

    private void applyAdminUserFilter() {
        String text = adminSearchField.getText().trim();
        if (text.isEmpty()) {
            adminUsersSorter.setRowFilter(null);
            return;
        }
        String regex = "(?i)" + Pattern.quote(text);
        adminUsersSorter.setRowFilter(RowFilter.regexFilter(regex));
    }

    private void loadAdminUsers(JTable usersTable, JButton deleteBtn) {
        deleteBtn.setEnabled(false);
        adminSelectedUserLabel.setText("Aucun utilisateur sélectionné");
        adminSelectedRoleLabel.setText("-");
        adminSelectedStatusLabel.setText("-");
        adminSelectedLinkedLabel.setText("-");

        new SwingWorker<List<UserService.UserAccountInfo>, Void>() {
            @Override
            protected List<UserService.UserAccountInfo> doInBackground() throws Exception {
                return userService.listUsers();
            }

            @Override
            protected void done() {
                try {
                    List<UserService.UserAccountInfo> users = get();
                    adminUsersTableModel.setRowCount(0);
                    for (UserService.UserAccountInfo user : users) {
                        adminUsersTableModel.addRow(new Object[]{
                                user.idUtilisateur,
                                user.login,
                                user.role,
                                user.idClient != null ? user.idClient : "-",
                                user.idLivreur != null ? user.idLivreur : "-",
                                user.actif ? "Oui" : "Non",
                                user.dateCreation
                        });
                    }
                    applyAdminUserFilter();
                    if (usersTable.getRowCount() > 0) {
                        usersTable.setRowSelectionInterval(0, 0);
                    }
                    showOutput("Liste des utilisateurs chargée: " + users.size());
                } catch (Exception ex) {
                    showOutput("Erreur chargement utilisateurs: " + ex.getMessage());
                    JOptionPane.showMessageDialog(MainFrame.this, ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void updateAdminUserDetails(JTable usersTable, JButton deleteBtn) {
        int viewRow = usersTable.getSelectedRow();
        if (viewRow < 0) {
            deleteBtn.setEnabled(false);
            adminSelectedUserLabel.setText("Aucun utilisateur sélectionné");
            adminSelectedRoleLabel.setText("-");
            adminSelectedStatusLabel.setText("-");
            adminSelectedLinkedLabel.setText("-");
            return;
        }

        int modelRow = usersTable.convertRowIndexToModel(viewRow);
        Object idValue = adminUsersTableModel.getValueAt(modelRow, 0);
        Object loginValue = adminUsersTableModel.getValueAt(modelRow, 1);
        Object roleValue = adminUsersTableModel.getValueAt(modelRow, 2);
        Object clientValue = adminUsersTableModel.getValueAt(modelRow, 3);
        Object livreurValue = adminUsersTableModel.getValueAt(modelRow, 4);
        Object statusValue = adminUsersTableModel.getValueAt(modelRow, 5);
        Object creationValue = adminUsersTableModel.getValueAt(modelRow, 6);

        adminSelectedUserLabel.setText("#" + idValue + " - " + loginValue);
        adminSelectedRoleLabel.setText(String.valueOf(roleValue));
        adminSelectedStatusLabel.setText(String.valueOf(statusValue) + " | Création: " + creationValue);
        adminSelectedLinkedLabel.setText("Client: " + clientValue + " | Livreur: " + livreurValue);
        deleteBtn.setEnabled(!String.valueOf(idValue).equals(String.valueOf(session.getIdUtilisateur())));

        if (!deleteBtn.isEnabled()) {
            adminSelectedStatusLabel.setText(adminSelectedStatusLabel.getText() + " | suppression du compte courant interdite");
        }
    }

    private void deleteSelectedAdminUser(JTable usersTable, JButton deleteBtn) {
        int viewRow = usersTable.getSelectedRow();
        if (viewRow < 0) {
            JOptionPane.showMessageDialog(this, "Sélectionne d'abord un compte.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int modelRow = usersTable.convertRowIndexToModel(viewRow);
        long idUtilisateur = ((Number) adminUsersTableModel.getValueAt(modelRow, 0)).longValue();
        String login = String.valueOf(adminUsersTableModel.getValueAt(modelRow, 1));

        if (idUtilisateur == session.getIdUtilisateur()) {
            JOptionPane.showMessageDialog(this, "Tu ne peux pas supprimer ton propre compte.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Supprimer le compte '" + login + "' (#" + idUtilisateur + ") ?",
                "Confirmer la suppression",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        boolean deleted = userService.deleteUser(idUtilisateur);
        if (deleted) {
            showOutput("Compte supprimé: " + login + " (#" + idUtilisateur + ")");
            loadAdminUsers(usersTable, deleteBtn);
            JOptionPane.showMessageDialog(this, "Compte supprimé.", "Succès", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "Suppression impossible.", "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void passerCommandeClient() {
        try {
            if (cartModel.isEmpty()) {
                throw new IllegalStateException("Le panier est vide.");
            }

            JComboBox<PizzaService.LivreurOption> livreurComboLocal = new JComboBox<>();
            for (PizzaService.LivreurOption livreur : service.listLivreurs()) {
                livreurComboLocal.addItem(livreur);
            }

            if (livreurComboLocal.getItemCount() == 0) {
                throw new IllegalStateException("Aucun livreur disponible.");
            }

            PizzaService.LivreurOption livreur = (PizzaService.LivreurOption) livreurComboLocal.getSelectedItem();
            int minutes = Integer.parseInt(minutesLivraisonField.getText().trim());
            String json = buildOrderJson();

            long idCommande = service.passerCommande(session.getIdClient(), livreur.id, json, minutes);
            showOutput("Commande creee: " + idCommande + " | Livreur: " + livreur.nom);
            clearCart();
            refreshFidelityLabel();
            JOptionPane.showMessageDialog(this, "Commande creee: " + idCommande);
        } catch (Exception ex) {
            showOutput("Erreur commande: " + ex.getMessage());
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JPanel buildOrderTab() {
        JPanel tab = new JPanel(new BorderLayout(12, 12));
        tab.setBorder(new EmptyBorder(14, 14, 14, 14));
        tab.setBackground(new Color(243, 246, 251));

        JPanel pickerCard = buildCard("Choix client et livreur");
        JPanel pickerGrid = new JPanel(new GridLayout(2, 2, 10, 10));
        pickerGrid.setOpaque(false);

        setupCombo(clientCombo);
        setupCombo(livreurCombo);

        pickerGrid.add(buildLabel("Client"));
        pickerGrid.add(clientCombo);
        pickerGrid.add(buildLabel("Livreur + vehicule"));
        pickerGrid.add(livreurCombo);
        pickerCard.add(pickerGrid, BorderLayout.CENTER);

        JPanel pizzaCard = buildCard("Choix pizza");
        JPanel pizzaGrid = new JPanel(new GridLayout(3, 2, 10, 10));
        pizzaGrid.setOpaque(false);

        setupCombo(pizzaCombo);
        setupCombo(tailleCombo);
        quantiteSpinner.setModel(new SpinnerNumberModel(1, 1, 50, 1));

        pizzaGrid.add(buildLabel("Pizza"));
        pizzaGrid.add(pizzaCombo);
        pizzaGrid.add(buildLabel("Taille"));
        pizzaGrid.add(tailleCombo);
        pizzaGrid.add(buildLabel("Quantite"));
        pizzaGrid.add(quantiteSpinner);

        pizzaDetailsLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        pizzaDetailsLabel.setForeground(new Color(49, 66, 100));
        pizzaDetailsLabel.setBorder(new EmptyBorder(8, 0, 8, 0));

        JButton addLineBtn = new JButton("Ajouter au panier");
        stylePrimaryButton(addLineBtn, new Color(23, 122, 89));
        addLineBtn.addActionListener(e -> addLineToCart());

        JPanel pizzaBottom = new JPanel(new BorderLayout(8, 8));
        pizzaBottom.setOpaque(false);
        pizzaBottom.add(pizzaDetailsLabel, BorderLayout.CENTER);
        pizzaBottom.add(addLineBtn, BorderLayout.EAST);

        pizzaCard.add(pizzaGrid, BorderLayout.CENTER);
        pizzaCard.add(pizzaBottom, BorderLayout.SOUTH);

        JPanel cartCard = buildCard("Panier commande");
        cartList.setFont(new Font("Consolas", Font.PLAIN, 12));
        cartList.setVisibleRowCount(6);
        JScrollPane cartScroll = new JScrollPane(cartList);
        cartScroll.setBorder(BorderFactory.createLineBorder(new Color(208, 216, 229)));

        JButton removeBtn = new JButton("Retirer");
        JButton clearBtn = new JButton("Vider");
        JButton submitBtn = new JButton("Valider commande");
        stylePrimaryButton(removeBtn, new Color(191, 76, 76));
        stylePrimaryButton(clearBtn, new Color(116, 126, 147));
        stylePrimaryButton(submitBtn, new Color(0, 159, 117));
        removeBtn.addActionListener(e -> removeSelectedLine());
        clearBtn.addActionListener(e -> clearCart());
        submitBtn.addActionListener(e -> passerCommande());

        JPanel cartButtons = new JPanel(new GridLayout(1, 3, 10, 10));
        cartButtons.setOpaque(false);
        cartButtons.add(removeBtn);
        cartButtons.add(clearBtn);
        cartButtons.add(submitBtn);

        panierTotalLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        panierTotalLabel.setForeground(new Color(19, 72, 171));

        JPanel cartBottom = new JPanel();
        cartBottom.setOpaque(false);
        cartBottom.setLayout(new BoxLayout(cartBottom, BoxLayout.Y_AXIS));
        cartBottom.add(Box.createVerticalStrut(6));
        cartBottom.add(panierTotalLabel);
        cartBottom.add(Box.createVerticalStrut(8));
        cartBottom.add(cartButtons);

        cartCard.add(cartScroll, BorderLayout.CENTER);
        cartCard.add(cartBottom, BorderLayout.SOUTH);

        JPanel configCard = buildCard("Parametres livraison");
        JPanel configGrid = new JPanel(new GridLayout(1, 2, 10, 10));
        configGrid.setOpaque(false);

        setupField(minutesLivraisonField);
        configGrid.add(buildLabel("Minutes livraison"));
        configGrid.add(minutesLivraisonField);
        configCard.add(configGrid, BorderLayout.CENTER);

        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.add(pickerCard);
        left.add(Box.createVerticalStrut(10));
        left.add(pizzaCard);
        left.add(Box.createVerticalStrut(10));
        left.add(configCard);

        JPanel right = new JPanel();
        right.setOpaque(false);
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
        right.add(cartCard);
        right.add(Box.createVerticalStrut(10));

        JPanel output = buildCard("Resultat");
        outputArea.setEditable(false);
        outputArea.setLineWrap(true);
        outputArea.setWrapStyleWord(true);
        outputArea.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        outputArea.setBackground(new Color(249, 251, 254));
        outputArea.setText("Pret. Selectionne client, livreur et pizzas pour creer une commande.");
        JScrollPane outputScroll = new JScrollPane(outputArea);
        outputScroll.setBorder(BorderFactory.createLineBorder(new Color(208, 216, 229)));
        output.add(outputScroll, BorderLayout.CENTER);
        right.add(output);

        JPanel center = new JPanel(new GridLayout(1, 2, 12, 0));
        center.setOpaque(false);
        center.add(left);
        center.add(right);

        tab.add(center, BorderLayout.CENTER);

        pizzaCombo.addActionListener(e -> refreshPizzaDetails());
        tailleCombo.addActionListener(e -> refreshPizzaDetails());

        return tab;
    }

    private JPanel buildAccountTab() {
        JPanel tab = new JPanel(new BorderLayout(12, 12));
        tab.setBorder(new EmptyBorder(14, 14, 14, 14));
        tab.setBackground(new Color(243, 246, 251));

        JPanel balanceCard = buildCard("Consultation solde");
        JPanel balanceGrid = new JPanel(new GridLayout(1, 1, 10, 10));
        balanceGrid.setOpaque(false);

        JComboBox<PizzaService.ClientOption> balanceClientCombo = new JComboBox<>();
        setupCombo(balanceClientCombo);

        JButton checkBalanceBtn = new JButton("Voir solde");
        stylePrimaryButton(checkBalanceBtn, new Color(236, 143, 25));
        checkBalanceBtn.addActionListener(e -> {
            try {
                PizzaService.ClientOption c = (PizzaService.ClientOption) balanceClientCombo.getSelectedItem();
                if (c == null) throw new IllegalStateException("Aucun client.");
                BigDecimal solde = service.lireSolde(c.id);
                showOutput("Solde client " + c.nom + " (#" + c.id + ") = " + solde + " EUR");
                JOptionPane.showMessageDialog(this, "Solde: " + solde + " EUR", "Solde", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                showOutput("Erreur: " + ex.getMessage());
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        });

        balanceGrid.add(balanceClientCombo);
        balanceGrid.add(checkBalanceBtn);
        balanceCard.add(balanceGrid, BorderLayout.CENTER);

        JPanel rechargeCard = buildCard("Recharge compte");
        JPanel rechargeGrid = new JPanel(new GridLayout(3, 2, 10, 10));
        rechargeGrid.setOpaque(false);

        JComboBox<PizzaService.ClientOption> rechargeClientCombo = new JComboBox<>();
        setupCombo(rechargeClientCombo);
        setupField(montantRechargeField);

        JButton rechargeBtn = new JButton("Recharger");
        stylePrimaryButton(rechargeBtn, new Color(36, 112, 255));
        rechargeBtn.addActionListener(e -> {
            try {
                PizzaService.ClientOption c = (PizzaService.ClientOption) rechargeClientCombo.getSelectedItem();
                if (c == null) throw new IllegalStateException("Aucun client.");
                BigDecimal montant = new BigDecimal(montantRechargeField.getText().trim());
                service.rechargerCompte(c.id, montant);
                showOutput("Recharge de " + montant + " EUR effectuee pour " + c.nom);
                refreshAccountTab(balanceClientCombo, rechargeClientCombo);
                JOptionPane.showMessageDialog(this, "Recharge effectuee.", "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                showOutput("Erreur recharge: " + ex.getMessage());
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        });

        rechargeGrid.add(buildLabel("Client"));
        rechargeGrid.add(rechargeClientCombo);
        rechargeGrid.add(buildLabel("Montant (EUR)"));
        rechargeGrid.add(montantRechargeField);
        JPanel emptyPlaceholder = new JPanel();
        emptyPlaceholder.setOpaque(false);
        rechargeGrid.add(emptyPlaceholder);
        rechargeGrid.add(rechargeBtn);
        rechargeCard.add(rechargeGrid, BorderLayout.CENTER);

        JPanel center = new JPanel();
        center.setOpaque(false);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.add(balanceCard);
        center.add(Box.createVerticalStrut(12));
        center.add(rechargeCard);
        center.add(Box.createVerticalGlue());

        tab.add(center, BorderLayout.CENTER);

        // Populate combos on visibility
        tab.addHierarchyListener(e -> {
            if (tab.isShowing() && balanceClientCombo.getItemCount() == 0) {
                try {
                    balanceClientCombo.removeAllItems();
                    rechargeClientCombo.removeAllItems();
                    for (PizzaService.ClientOption c : service.listClients()) {
                        balanceClientCombo.addItem(c);
                        rechargeClientCombo.addItem(c);
                    }
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        });

        return tab;
    }

    private void refreshAccountTab(JComboBox<PizzaService.ClientOption> balanceCombo, JComboBox<PizzaService.ClientOption> rechargeCombo) {
        try {
            balanceCombo.removeAllItems();
            rechargeCombo.removeAllItems();
            for (PizzaService.ClientOption c : service.listClients()) {
                balanceCombo.addItem(c);
                rechargeCombo.addItem(c);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private JPanel buildDashboardTab() {
        JPanel tab = new JPanel(new BorderLayout(12, 12));
        tab.setBorder(new EmptyBorder(14, 14, 14, 14));
        tab.setBackground(new Color(243, 246, 251));

        JButton refreshDashBtn = new JButton("Rafraichir");
        stylePrimaryButton(refreshDashBtn, new Color(98, 84, 177));
        refreshDashBtn.addActionListener(e -> loadDashboardStats());

        JPanel dashTop = new JPanel(new BorderLayout());
        dashTop.setOpaque(false);
        JLabel title = new JLabel("Dashboard administrateur");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(new Color(28, 46, 92));
        dashTop.add(title, BorderLayout.WEST);
        dashTop.add(refreshDashBtn, BorderLayout.EAST);

        JPanel metricsRow = new JPanel(new GridLayout(1, 4, 10, 10));
        metricsRow.setOpaque(false);
        metricsRow.add(buildMetricCard("Véhicules jamais servis", vehiclesNeverUsedValueLabel, new Color(191, 76, 76)));
        metricsRow.add(buildMetricCard("Moyenne commandes/client", averageOrdersValueLabel, new Color(36, 112, 255)));
        metricsRow.add(buildMetricCard("Clients au-dessus moyenne", aboveAverageValueLabel, new Color(0, 159, 117)));
        metricsRow.add(buildMetricCard("Clients commandant", totalClientsValueLabel, new Color(98, 84, 177)));

        JPanel statsRow = new JPanel(new GridLayout(1, 5, 10, 10));
        statsRow.setOpaque(false);
        statsRow.add(buildMetricCard("👥 Meilleur client", bestClientLabel, new Color(255, 193, 7)));
        statsRow.add(buildMetricCard("🚚 Plus mauvais livreur", worstDelivererLabel, new Color(244, 67, 54)));
        statsRow.add(buildMetricCard("🍕 Pizza + demandée", mostOrderedPizzaLabel, new Color(76, 175, 80)));
        statsRow.add(buildMetricCard("🍕 Pizza - demandée", leastOrderedPizzaLabel, new Color(158, 158, 158)));
        statsRow.add(buildMetricCard("🌶️ Ingrédient favori", favoriteIngredientLabel, new Color(233, 30, 99)));

        JTable vehiclesTable = buildReportTable(vehiclesTableModel);
        JTable ordersTable = buildReportTable(ordersTableModel);
        JTable aboveAverageTable = buildReportTable(aboveAverageTableModel);

        JPanel vehiclesCard = buildCard("Véhicules n'ayant jamais servi");
        vehiclesCard.add(new JScrollPane(vehiclesTable), BorderLayout.CENTER);

        JPanel ordersCard = buildCard("Nombre de commandes par client");
        ordersCard.add(new JScrollPane(ordersTable), BorderLayout.CENTER);

        JPanel aboveAverageCard = buildCard("Clients ayant commandé plus que la moyenne");
        aboveAverageCard.add(new JScrollPane(aboveAverageTable), BorderLayout.CENTER);

        JPanel cards = new JPanel(new GridLayout(3, 1, 12, 12));
        cards.setOpaque(false);
        cards.add(vehiclesCard);
        cards.add(ordersCard);
        cards.add(aboveAverageCard);

        JPanel center = new JPanel(new BorderLayout(12, 12));
        center.setOpaque(false);
        
        JPanel allMetrics = new JPanel();
        allMetrics.setOpaque(false);
        allMetrics.setLayout(new BoxLayout(allMetrics, BoxLayout.Y_AXIS));
        allMetrics.add(metricsRow);
        allMetrics.add(Box.createVerticalStrut(8));
        allMetrics.add(statsRow);
        
        center.add(allMetrics, BorderLayout.NORTH);
        center.add(cards, BorderLayout.CENTER);

        tab.add(dashTop, BorderLayout.NORTH);
        tab.add(center, BorderLayout.CENTER);

        tab.addHierarchyListener(e -> {
            if (tab.isShowing() && "0".equals(vehiclesNeverUsedValueLabel.getText())) {
                loadDashboardStats();
            }
        });

        return tab;
    }

    private void loadDashboardStats() {
        refreshDashboardAsync();
    }

    private void refreshDashboardAsync() {
        setDashboardLoadingState(true);
        new SwingWorker<PizzaService.AdminDashboardData, Void>() {
            @Override
            protected PizzaService.AdminDashboardData doInBackground() throws Exception {
                return service.loadAdminDashboardData();
            }

            @Override
            protected void done() {
                try {
                    updateDashboard(get());
                } catch (Exception ex) {
                    setDashboardError("Erreur chargement dashboard: " + ex.getMessage());
                }
            }
        }.execute();
    }

    private void updateDashboard(PizzaService.AdminDashboardData data) {
        vehiclesTableModel.setRowCount(0);
        ordersTableModel.setRowCount(0);
        aboveAverageTableModel.setRowCount(0);

        for (PizzaService.VehicleUnusedOption vehicle : data.vehiclesNeverUsed) {
            vehiclesTableModel.addRow(new Object[]{
                    vehicle.id,
                    vehicle.typeVehicule,
                    vehicle.immatriculation,
                    vehicle.actif ? "Oui" : "Non"
            });
        }

        for (PizzaService.ClientOrderCount stat : data.ordersPerClient) {
            ordersTableModel.addRow(new Object[]{stat.idClient, stat.nom, stat.nbCommandes});
        }

        for (PizzaService.ClientOrderCount stat : data.clientsAboveAverage) {
            aboveAverageTableModel.addRow(new Object[]{stat.idClient, stat.nom, stat.nbCommandes});
        }

        vehiclesNeverUsedValueLabel.setText(String.valueOf(data.vehiclesNeverUsed.size()));
        averageOrdersValueLabel.setText(data.averageOrdersPerClient.toPlainString());
        aboveAverageValueLabel.setText(String.valueOf(data.clientsAboveAverage.size()));
        totalClientsValueLabel.setText(String.valueOf(data.ordersPerClient.size()));
        
        if (data.bestClient != null) {
            bestClientLabel.setText(data.bestClient.nom + " (" + data.bestClient.nbCommandes + " cmd)");
        } else {
            bestClientLabel.setText("Aucun");
        }
        
        if (data.worstDeliverer != null) {
            worstDelivererLabel.setText(data.worstDeliverer.nomLivreur + " (" + data.worstDeliverer.nbRetards + " retards - " 
                    + data.worstDeliverer.typeVehicule + ")");
        } else {
            worstDelivererLabel.setText("Aucun");
        }
        
        if (data.mostOrderedPizza != null) {
            mostOrderedPizzaLabel.setText(data.mostOrderedPizza.nomPizza + " (" + data.mostOrderedPizza.nbCommandes + " cmd)");
        } else {
            mostOrderedPizzaLabel.setText("Aucune");
        }
        
        if (data.leastOrderedPizza != null) {
            leastOrderedPizzaLabel.setText(data.leastOrderedPizza.nomPizza + " (" + data.leastOrderedPizza.nbCommandes + " cmd)");
        } else {
            leastOrderedPizzaLabel.setText("Aucune");
        }
        
        if (data.favoriteIngredient != null) {
            favoriteIngredientLabel.setText(data.favoriteIngredient.nomIngredient + " (" + data.favoriteIngredient.nbOccurrences + " fois)");
        } else {
            favoriteIngredientLabel.setText("Aucun");
        }
        
        showOutput("Dashboard admin rafraichi.");
    }

    private void setDashboardLoadingState(boolean loading) {
        if (loading) {
            vehiclesNeverUsedValueLabel.setText("...");
            averageOrdersValueLabel.setText("...");
            aboveAverageValueLabel.setText("...");
            totalClientsValueLabel.setText("...");
            bestClientLabel.setText("...");
            worstDelivererLabel.setText("...");
            mostOrderedPizzaLabel.setText("...");
            leastOrderedPizzaLabel.setText("...");
            favoriteIngredientLabel.setText("...");
        }
    }

    private void setDashboardError(String message) {
        vehiclesNeverUsedValueLabel.setText("Erreur");
        averageOrdersValueLabel.setText("Erreur");
        aboveAverageValueLabel.setText("Erreur");
        totalClientsValueLabel.setText("Erreur");
        bestClientLabel.setText("Erreur");
        worstDelivererLabel.setText("Erreur");
        mostOrderedPizzaLabel.setText("Erreur");
        leastOrderedPizzaLabel.setText("Erreur");
        favoriteIngredientLabel.setText("Erreur");
        JOptionPane.showMessageDialog(this, message, "Erreur dashboard", JOptionPane.ERROR_MESSAGE);
        showOutput(message);
    }

    private JTable buildReportTable(DefaultTableModel model) {
        JTable table = new JTable(model);
        table.setRowHeight(24);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        table.setShowGrid(false);
        table.setFillsViewportHeight(true);
        table.setBackground(new Color(249, 251, 254));
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public java.awt.Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                java.awt.Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(245, 248, 252));
                }
                return c;
            }
        });
        return table;
    }

    private JPanel buildMetricCard(String title, JLabel valueLabel, Color accent) {
        JPanel card = new JPanel(new BorderLayout(6, 6));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(218, 226, 239)),
                new EmptyBorder(12, 12, 12, 12)
        ));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        titleLabel.setForeground(new Color(90, 99, 120));

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        valueLabel.setForeground(accent);

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        return card;
    }

    private void applyLookAndFeelDefaults() {
        UIManager.put("Button.focus", new Color(0, 0, 0, 0));
        UIManager.put("TextField.font", new Font("Segoe UI", Font.PLAIN, 13));
        UIManager.put("Label.font", new Font("Segoe UI", Font.PLAIN, 13));
    }

    private void setupCombo(JComboBox<?> combo) {
        combo.setBackground(new Color(252, 253, 255));
        combo.setBorder(BorderFactory.createLineBorder(new Color(203, 212, 226)));
        combo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
    }

    private JPanel buildCard(String title) {
        JPanel card = new JPanel(new BorderLayout(8, 8));
        card.setOpaque(true);
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(218, 226, 239)),
                new EmptyBorder(12, 12, 12, 12)
        ));

        JLabel cardTitle = new JLabel(title);
        cardTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        cardTitle.setForeground(new Color(29, 44, 78));
        cardTitle.setBorder(new EmptyBorder(0, 0, 6, 0));
        card.add(cardTitle, BorderLayout.NORTH);
        return card;
    }

    private JLabel buildLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(new Color(49, 66, 100));
        label.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        return label;
    }

    private void setupField(JTextField field) {
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(203, 212, 226)),
                new EmptyBorder(6, 8, 6, 8)
        ));
        field.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        field.setBackground(new Color(252, 253, 255));
    }

    private void stylePrimaryButton(JButton button, Color bg) {
        button.setBackground(bg);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setBorder(new EmptyBorder(10, 14, 10, 14));
        button.setOpaque(true);
    }

    private void showOutput(String text) {
        outputArea.setText(text);
    }

    private void refreshData() {
        try {
            clientCombo.removeAllItems();
            livreurCombo.removeAllItems();
            pizzaCombo.removeAllItems();
            tailleCombo.removeAllItems();

            for (PizzaService.ClientOption client : service.listClients()) {
                clientCombo.addItem(client);
            }
            for (PizzaService.LivreurOption livreur : service.listLivreurs()) {
                livreurCombo.addItem(livreur);
            }
            for (PizzaService.PizzaOption pizza : service.listPizzas()) {
                pizzaCombo.addItem(pizza);
            }
            for (PizzaService.TailleOption taille : service.listTailles()) {
                tailleCombo.addItem(taille);
            }

            refreshPizzaDetails();
            refreshFidelityLabel();
            showOutput("Donnees rechargees. Tu peux maintenant choisir client, livreur, pizza et taille.");
        } catch (SQLException ex) {
            showOutput("Erreur chargement donnees: " + ex.getMessage());
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void refreshFidelityLabel() {
        if (!session.isClient()) {
            return;
        }
        try {
            PizzaService.FidelityInfo fidelity = service.getFidelityInfo(session.getIdClient());
            fidelityLabel.setText(
                String.format("Pizzas achetees: %d | Encore %d avant une gratuite! 🎉 (prochain bonus à %d)",
                    fidelity.totalPizzas, fidelity.pizzasUntilFree, fidelity.nextFreeNumber)
            );
        } catch (SQLException ex) {
            fidelityLabel.setText("Erreur chargement fidelite: " + ex.getMessage());
        }
    }

    private void refreshPizzaDetails() {
        PizzaService.PizzaOption pizza = (PizzaService.PizzaOption) pizzaCombo.getSelectedItem();
        PizzaService.TailleOption taille = (PizzaService.TailleOption) tailleCombo.getSelectedItem();

        if (pizza == null || taille == null) {
            pizzaDetailsLabel.setText("Selectionne une pizza pour voir les ingredients.");
            return;
        }

        BigDecimal prixTaille = pizza.prixBase.multiply(taille.coefficient).setScale(2, RoundingMode.HALF_UP);
        pizzaDetailsLabel.setText("Ingredients: " + pizza.ingredients + " | Prix base: " + pizza.prixBase
                + " EUR | Prix " + taille.code + ": " + prixTaille + " EUR");
    }

    private void addLineToCart() {
        PizzaService.PizzaOption pizza = (PizzaService.PizzaOption) pizzaCombo.getSelectedItem();
        PizzaService.TailleOption taille = (PizzaService.TailleOption) tailleCombo.getSelectedItem();
        Number qtyValue = (Number) quantiteSpinner.getValue();
        int quantite = qtyValue.intValue();

        if (pizza == null || taille == null) {
            JOptionPane.showMessageDialog(this, "Choisis une pizza et une taille.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        if (quantite <= 0) {
            JOptionPane.showMessageDialog(this, "La quantite doit etre > 0.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        cartModel.addElement(new CartLine(pizza, taille, quantite));
        cartList.setSelectedIndex(cartModel.size() - 1);
        refreshCartTotal();
        showOutput("Ajoute au panier: " + pizza.nom + " | taille=" + taille.code + " | qte=" + quantite);
    }

    private void removeSelectedLine() {
        int index = cartList.getSelectedIndex();
        if (index >= 0) {
            CartLine removed = cartModel.get(index);
            cartModel.remove(index);
            refreshCartTotal();
            showOutput("Ligne retiree du panier: " + removed);
        }
    }

    private void clearCart() {
        cartModel.clear();
        refreshCartTotal();
        showOutput("Panier vide.");
    }

    private void refreshCartTotal() {
        BigDecimal total = BigDecimal.ZERO;
        for (int i = 0; i < cartModel.size(); i++) {
            total = total.add(cartModel.get(i).estimatedTotal());
        }
        total = total.setScale(2, RoundingMode.HALF_UP);
        panierTotalLabel.setText("Total estime panier: " + total + " EUR");
    }

    private String buildOrderJson() {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < cartModel.size(); i++) {
            CartLine line = cartModel.get(i);
            if (i > 0) {
                sb.append(',');
            }
            sb.append("{\"pizza_id\":")
                    .append(line.pizza.id)
                    .append(",\"taille\":\"")
                    .append(line.taille.code)
                    .append("\",\"quantite\":")
                    .append(line.quantite)
                    .append('}');
        }
        sb.append(']');
        return sb.toString();
    }

    private void rechargerCompte() {
        try {
            PizzaService.ClientOption client = (PizzaService.ClientOption) clientCombo.getSelectedItem();
            if (client == null) {
                throw new IllegalStateException("Aucun client disponible.");
            }

            long idClient = client.id;
            BigDecimal montant = new BigDecimal(montantRechargeField.getText().trim());
            service.rechargerCompte(idClient, montant);
            showOutput("Recharge effectuee pour le client " + idClient + " : +" + montant);
            refreshData();
            JOptionPane.showMessageDialog(this, "Recharge effectuee.");
        } catch (Exception ex) {
            showOutput("Erreur recharge: " + ex.getMessage());
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void passerCommande() {
        try {
            if (cartModel.isEmpty()) {
                throw new IllegalStateException("Le panier est vide.");
            }

            PizzaService.ClientOption client = (PizzaService.ClientOption) clientCombo.getSelectedItem();
            PizzaService.LivreurOption livreur = (PizzaService.LivreurOption) livreurCombo.getSelectedItem();
            if (client == null || livreur == null) {
                throw new IllegalStateException("Client ou livreur manquant.");
            }

            long idClient = client.id;
            long idLivreur = livreur.id;
            int minutes = Integer.parseInt(minutesLivraisonField.getText().trim());
            String json = buildOrderJson();

            long idCommande = service.passerCommande(idClient, idLivreur, json, minutes);
            showOutput("Commande creee: " + idCommande + " | Client: " + client.nom + " | Livreur: " + livreur.nom
                    + " | Vehicule: " + livreur.typeVehicule + " | JSON: " + json);
            clearCart();
            refreshData();
            JOptionPane.showMessageDialog(this, "Commande creee: " + idCommande);
        } catch (Exception ex) {
            showOutput("Erreur commande: " + ex.getMessage());
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void voirSolde() {
        try {
            PizzaService.ClientOption client = (PizzaService.ClientOption) clientCombo.getSelectedItem();
            if (client == null) {
                throw new IllegalStateException("Aucun client disponible.");
            }
            long idClient = client.id;
            BigDecimal solde = service.lireSolde(idClient);
            showOutput("Solde client " + client.nom + " (#" + idClient + ") = " + solde);
            JOptionPane.showMessageDialog(this, "Solde client " + idClient + " = " + solde);
        } catch (Exception ex) {
            showOutput("Erreur lecture solde: " + ex.getMessage());
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void open() {
        SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true));
    }

    private static final class CartLine {
        private final PizzaService.PizzaOption pizza;
        private final PizzaService.TailleOption taille;
        private final int quantite;

        private CartLine(PizzaService.PizzaOption pizza, PizzaService.TailleOption taille, int quantite) {
            this.pizza = pizza;
            this.taille = taille;
            this.quantite = quantite;
        }

        private BigDecimal estimatedTotal() {
            return pizza.prixBase
                    .multiply(taille.coefficient)
                    .multiply(BigDecimal.valueOf(quantite))
                    .setScale(2, RoundingMode.HALF_UP);
        }

        @Override
        public String toString() {
            return pizza.nom + " | taille=" + taille.code + " | qte=" + quantite + " | estime=" + estimatedTotal() + " EUR";
        }
    }
}
