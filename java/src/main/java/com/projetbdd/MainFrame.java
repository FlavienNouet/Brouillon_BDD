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
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
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

public class MainFrame extends JFrame {
    private final PizzaService service = new PizzaService();

    private final JComboBox<PizzaService.ClientOption> clientCombo = new JComboBox<>();
    private final JComboBox<PizzaService.LivreurOption> livreurCombo = new JComboBox<>();
    private final JComboBox<PizzaService.PizzaOption> pizzaCombo = new JComboBox<>();
    private final JComboBox<PizzaService.TailleOption> tailleCombo = new JComboBox<>();
    private final JSpinner quantiteSpinner = new JSpinner();

    private final JLabel pizzaDetailsLabel = new JLabel("Selectionne une pizza pour voir les ingredients.");
    private final JLabel panierTotalLabel = new JLabel("Total estime panier: 0.00 EUR");

    private final DefaultListModel<CartLine> cartModel = new DefaultListModel<>();
    private final JList<CartLine> cartList = new JList<>(cartModel);

    private final JTextArea outputArea = new JTextArea();
    private final JTextArea dashboardArea = new JTextArea();

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

        JLabel title = new JLabel("Gestion Commandes Pizza");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));

        JLabel subtitle = new JLabel("Plateforme prepayee - Recharge, commandes et dashboard");
        subtitle.setForeground(new Color(227, 238, 255));
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        JPanel titlePanel = new JPanel();
        titlePanel.setOpaque(false);
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.add(title);
        titlePanel.add(Box.createVerticalStrut(4));
        titlePanel.add(subtitle);
        header.add(titlePanel, BorderLayout.CENTER);
        add(header, BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tabs.setBackground(new Color(243, 246, 251));

        tabs.addTab("Commande", buildOrderTab());
        tabs.addTab("Mon compte", buildAccountTab());
        tabs.addTab("Tableau de bord", buildDashboardTab());

        add(tabs, BorderLayout.CENTER);

        refreshData();
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

        JPanel statsCard = buildCard("Statistiques");
        dashboardArea.setEditable(false);
        dashboardArea.setLineWrap(true);
        dashboardArea.setWrapStyleWord(true);
        dashboardArea.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        dashboardArea.setBackground(new Color(249, 251, 254));
        dashboardArea.setText("Chargement des statistiques...");

        JScrollPane statsScroll = new JScrollPane(dashboardArea);
        statsScroll.setBorder(BorderFactory.createLineBorder(new Color(208, 216, 229)));
        statsCard.add(statsScroll, BorderLayout.CENTER);

        JButton refreshDashBtn = new JButton("Rafraichir");
        stylePrimaryButton(refreshDashBtn, new Color(98, 84, 177));
        refreshDashBtn.addActionListener(e -> loadDashboardStats());

        JPanel dashTop = new JPanel(new BorderLayout());
        dashTop.setOpaque(false);
        dashTop.add(refreshDashBtn, BorderLayout.EAST);

        tab.add(dashTop, BorderLayout.NORTH);
        tab.add(statsCard, BorderLayout.CENTER);

        tab.addHierarchyListener(e -> {
            if (tab.isShowing() && dashboardArea.getText().equals("Chargement des statistiques...")) {
                loadDashboardStats();
            }
        });

        return tab;
    }

    private void loadDashboardStats() {
        new Thread(() -> {
            try {
                StringBuilder sb = new StringBuilder();
                try (Connection cn = Database.getConnection();
                     Statement st = cn.createStatement()) {

                    // Chiffre d'affaires
                    try (ResultSet rs = st.executeQuery(
                            "SELECT ROUND(COALESCE(SUM(cl.prix_facture), 0), 2) AS ca FROM commande_ligne cl")) {
                        if (rs.next()) {
                            sb.append("=== CHIFFRE D'AFFAIRES ===\n");
                            sb.append("CA total: ").append(rs.getBigDecimal("ca")).append(" EUR\n\n");
                        }
                    }

                    // Meilleur client
                    try (ResultSet rs = st.executeQuery(
                            "SELECT c.id_client, c.nom, ROUND(SUM(cl.prix_facture), 2) AS depense FROM client c " +
                            "JOIN commande co ON co.id_client = c.id_client " +
                            "JOIN commande_ligne cl ON cl.id_commande = co.id_commande " +
                            "GROUP BY c.id_client, c.nom ORDER BY depense DESC LIMIT 1")) {
                        if (rs.next()) {
                            sb.append("=== MEILLEUR CLIENT ===\n");
                            sb.append("Client: ").append(rs.getString("nom"))
                                    .append(" (#").append(rs.getLong("id_client"))
                                    .append(") | Depense: ").append(rs.getBigDecimal("depense")).append(" EUR\n\n");
                        }
                    }

                    // Pizza la plus demandee
                    try (ResultSet rs = st.executeQuery(
                            "SELECT p.nom, COUNT(*) AS nb FROM commande_ligne cl " +
                            "JOIN pizza p ON p.id_pizza = cl.id_pizza " +
                            "GROUP BY p.nom ORDER BY nb DESC LIMIT 1")) {
                        if (rs.next()) {
                            sb.append("=== PIZZA PLUS DEMANDEE ===\n");
                            sb.append("Pizza: ").append(rs.getString("nom"))
                                    .append(" | Commandes: ").append(rs.getInt("nb")).append("\n\n");
                        }
                    }

                    // Nombre total de commandes
                    try (ResultSet rs = st.executeQuery(
                            "SELECT COUNT(*) AS nb FROM commande")) {
                        if (rs.next()) {
                            sb.append("=== STATISTIQUES GLOBALES ===\n");
                            sb.append("Nombre de commandes: ").append(rs.getInt("nb")).append("\n");
                        }
                    }

                    // Nombre total de clients actifs
                    try (ResultSet rs = st.executeQuery(
                            "SELECT COUNT(DISTINCT id_client) AS nb FROM commande")) {
                        if (rs.next()) {
                            sb.append("Clients ayant commande: ").append(rs.getInt("nb")).append("\n");
                        }
                    }

                    // Montant total facture
                    try (ResultSet rs = st.executeQuery(
                            "SELECT COALESCE(SUM(montant), 0) AS total FROM compte_transaction WHERE type_transaction = 'debit_commande'")) {
                        if (rs.next()) {
                            sb.append("Total facture: ").append(rs.getBigDecimal("total")).append(" EUR\n");
                        }
                    }

                }
                SwingUtilities.invokeLater(() -> dashboardArea.setText(sb.toString()));
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> dashboardArea.setText("Erreur chargement dashboard: " + ex.getMessage()));
            }
        }).start();
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
            showOutput("Donnees rechargees. Tu peux maintenant choisir client, livreur, pizza et taille.");
        } catch (SQLException ex) {
            showOutput("Erreur chargement donnees: " + ex.getMessage());
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
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

    private static final class GradientHeader extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            GradientPaint gradient = new GradientPaint(
                    0, 0, new Color(19, 72, 171),
                    getWidth(), getHeight(), new Color(42, 130, 228)
            );
            g2.setPaint(gradient);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 0, 0);
            g2.dispose();
        }
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
