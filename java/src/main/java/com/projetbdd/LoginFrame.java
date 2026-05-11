package com.projetbdd;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

public class LoginFrame extends JFrame {
    private final UserService userService = new UserService();
    private final JTextField loginField = new JTextField(20);
    private final JPasswordField passwordField = new JPasswordField(20);

    public LoginFrame() {
        super("Pizza Prepayee - Connexion");
        setSize(400, 350);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        setLayout(new BorderLayout());

        applyLookAndFeelDefaults();

        GradientHeader header = new GradientHeader();
        header.setPreferredSize(new Dimension(0, 80));
        header.setLayout(new BorderLayout());
        header.setBorder(new EmptyBorder(16, 20, 16, 20));

        JLabel title = new JLabel("Connexion");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        header.add(title, BorderLayout.CENTER);
        add(header, BorderLayout.NORTH);

        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBorder(new EmptyBorder(30, 40, 30, 40));
        formPanel.setBackground(Color.WHITE);

        JLabel loginLabel = new JLabel("Identifiant:");
        loginLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        formPanel.add(loginLabel);
        formPanel.add(Box.createVerticalStrut(6));
        loginField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        loginField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        formPanel.add(loginField);
        formPanel.add(Box.createVerticalStrut(16));

        JLabel passwordLabel = new JLabel("Mot de passe:");
        passwordLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        formPanel.add(passwordLabel);
        formPanel.add(Box.createVerticalStrut(6));
        passwordField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        passwordField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        formPanel.add(passwordField);
        formPanel.add(Box.createVerticalStrut(24));

        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));

        JButton loginButton = new JButton("Connexion");
        loginButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        loginButton.setFocusPainted(false);
        loginButton.setBackground(new Color(33, 150, 243));
        loginButton.setForeground(Color.WHITE);
        loginButton.setPreferredSize(new Dimension(120, 36));
        loginButton.addActionListener(e -> handleLogin());
        buttonPanel.add(loginButton);

        buttonPanel.add(Box.createHorizontalStrut(12));

        JButton registerButton = new JButton("S'inscrire");
        registerButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        registerButton.setFocusPainted(false);
        registerButton.setBackground(new Color(76, 175, 80));
        registerButton.setForeground(Color.WHITE);
        registerButton.setPreferredSize(new Dimension(120, 36));
        registerButton.addActionListener(e -> handleRegister());
        buttonPanel.add(registerButton);

        formPanel.add(buttonPanel);
        add(formPanel, BorderLayout.CENTER);
    }

    private void handleLogin() {
        String login = loginField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (login.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Veuillez remplir tous les champs.", "Erreur", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (userService.authenticate(login, password)) {
            Session session = Session.getInstance();
            dispose();
            MainFrame mainFrame = new MainFrame();
            mainFrame.setVisible(true);
        } else {
            JOptionPane.showMessageDialog(this, "Identifiant ou mot de passe incorrect.", "Erreur de connexion", JOptionPane.ERROR_MESSAGE);
            passwordField.setText("");
        }
    }

    private void handleRegister() {
        String login = loginField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (login.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Veuillez remplir tous les champs.", "Erreur", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (userService.userExists(login)) {
            JOptionPane.showMessageDialog(this, "Cet identifiant existe déjà.", "Erreur", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (userService.createUser(login, password, "CLIENT", null, null)) {
            JOptionPane.showMessageDialog(this, "Inscription réussie ! Vous pouvez maintenant vous connecter.", "Succès", JOptionPane.INFORMATION_MESSAGE);
            loginField.setText("");
            passwordField.setText("");
        } else {
            JOptionPane.showMessageDialog(this, "Erreur lors de l'inscription.", "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void applyLookAndFeelDefaults() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        AuthBootstrap.initialize();
        LoginFrame loginFrame = new LoginFrame();
        loginFrame.setVisible(true);
    }
}
