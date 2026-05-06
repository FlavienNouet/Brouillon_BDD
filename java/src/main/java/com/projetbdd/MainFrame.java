package com.projetbdd;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.math.BigDecimal;

public class MainFrame extends JFrame {
    private final PizzaService service = new PizzaService();

    private final JTextField clientIdField = new JTextField("1");
    private final JTextField livreurIdField = new JTextField("1");
    private final JTextField montantRechargeField = new JTextField("20.00");
    private final JTextField minutesLivraisonField = new JTextField("25");
    private final JTextArea lignesCommandeArea = new JTextArea(
            "[{\"pizza_id\":1,\"taille\":\"humaine\",\"quantite\":2}]"
    );

    public MainFrame() {
        super("Projet BDD - Pizza Prepaye");
        setSize(760, 450);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JPanel top = new JPanel(new GridLayout(5, 2, 8, 8));
        top.add(new JLabel("ID Client"));
        top.add(clientIdField);
        top.add(new JLabel("ID Livreur"));
        top.add(livreurIdField);
        top.add(new JLabel("Montant recharge"));
        top.add(montantRechargeField);
        top.add(new JLabel("Minutes livraison"));
        top.add(minutesLivraisonField);
        top.add(new JLabel("Lignes JSON"));
        top.add(new JLabel("(zone en dessous)"));

        add(top, BorderLayout.NORTH);
        add(new JScrollPane(lignesCommandeArea), BorderLayout.CENTER);

        JPanel bottom = new JPanel(new GridLayout(1, 3, 8, 8));
        JButton rechargeBtn = new JButton("Recharger");
        JButton commandeBtn = new JButton("Passer commande");
        JButton soldeBtn = new JButton("Voir solde");

        rechargeBtn.addActionListener(e -> rechargerCompte());
        commandeBtn.addActionListener(e -> passerCommande());
        soldeBtn.addActionListener(e -> voirSolde());

        bottom.add(rechargeBtn);
        bottom.add(commandeBtn);
        bottom.add(soldeBtn);
        add(bottom, BorderLayout.SOUTH);
    }

    private void rechargerCompte() {
        try {
            long idClient = Long.parseLong(clientIdField.getText().trim());
            BigDecimal montant = new BigDecimal(montantRechargeField.getText().trim());
            service.rechargerCompte(idClient, montant);
            JOptionPane.showMessageDialog(this, "Recharge effectuee.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void passerCommande() {
        try {
            long idClient = Long.parseLong(clientIdField.getText().trim());
            long idLivreur = Long.parseLong(livreurIdField.getText().trim());
            int minutes = Integer.parseInt(minutesLivraisonField.getText().trim());
            String json = lignesCommandeArea.getText().trim();

            long idCommande = service.passerCommande(idClient, idLivreur, json, minutes);
            JOptionPane.showMessageDialog(this, "Commande creee: " + idCommande);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void voirSolde() {
        try {
            long idClient = Long.parseLong(clientIdField.getText().trim());
            BigDecimal solde = service.lireSolde(idClient);
            JOptionPane.showMessageDialog(this, "Solde client " + idClient + " = " + solde);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void open() {
        SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true));
    }
}
