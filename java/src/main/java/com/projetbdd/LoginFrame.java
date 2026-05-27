package com.projetbdd;

import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class LoginFrame extends JFrame {
    private final UserService userService = new UserService();
    private final JTextField loginField = new JTextField(20);
    private final JPasswordField passwordField = new JPasswordField(20);

    public LoginFrame() {
        super("RaPizz - Connexion");

        applyLookAndFeelDefaults();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(520, 690);
        setMinimumSize(new Dimension(480, 640));
        setLocationRelativeTo(null);
        setContentPane(createMainContent());
    }

    private JPanel createMainContent() {
        JPanel root = new JPanel(new GridBagLayout());
        root.setBackground(new Color(245, 247, 250));
        root.setBorder(new EmptyBorder(30, 30, 30, 30));

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(new EmptyBorder(36, 36, 36, 36));
        card.setPreferredSize(new Dimension(390, 560));
        card.setMaximumSize(new Dimension(390, 560));

        PizzaLogo logo = new PizzaLogo();
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel brand = new JLabel("RAPIZZ");
        brand.setFont(new Font("Segoe UI", Font.BOLD, 16));
        brand.setForeground(new Color(28, 100, 242));
        brand.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel title = new JLabel("Connexion");
        title.setFont(new Font("Segoe UI", Font.BOLD, 30));
        title.setForeground(new Color(24, 28, 33));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("Accède à ton espace client");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitle.setForeground(new Color(110, 118, 129));
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(logo);
        card.add(Box.createVerticalStrut(14));
        card.add(brand);
        card.add(Box.createVerticalStrut(14));
        card.add(title);
        card.add(Box.createVerticalStrut(8));
        card.add(subtitle);
        card.add(Box.createVerticalStrut(32));

        JPanel loginLabelPanel = createCenteredLabelPanel("Identifiant");
        card.add(loginLabelPanel);
        card.add(Box.createVerticalStrut(8));

        styleTextField(loginField, "Entre ton identifiant");
        card.add(loginField);
        card.add(Box.createVerticalStrut(18));

        JPanel passwordLabelPanel = createCenteredLabelPanel("Mot de passe");
        card.add(passwordLabelPanel);
        card.add(Box.createVerticalStrut(8));

        stylePasswordField(passwordField, "Entre ton mot de passe");
        card.add(passwordField);
        card.add(Box.createVerticalStrut(26));

        JButton loginButton = createPrimaryButton("Se connecter");
        loginButton.addActionListener(e -> handleLogin());
        loginButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(loginButton);

        card.add(Box.createVerticalStrut(12));

        JButton registerButton = createSecondaryButton("Créer un compte");
        registerButton.addActionListener(e -> handleRegister());
        registerButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(registerButton);

        card.add(Box.createVerticalGlue());
        card.add(Box.createVerticalStrut(18));

        JLabel footer = new JLabel("Application de gestion de pizzas prépayées");
        footer.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        footer.setForeground(new Color(140, 146, 155));
        footer.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(footer);

        root.add(card);
        return root;
    }

    private JPanel createCenteredLabelPanel(String text) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        panel.setOpaque(false);
        panel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));

        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));
        label.setForeground(new Color(45, 51, 59));
        label.setHorizontalAlignment(SwingConstants.CENTER);

        panel.add(label);
        return panel;
    }

    private void styleTextField(JTextField field, String toolTip) {
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        field.setPreferredSize(new Dimension(300, 44));
        field.setToolTipText(toolTip);
        field.setAlignmentX(Component.CENTER_ALIGNMENT);
    }

    private void stylePasswordField(JPasswordField field, String toolTip) {
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        field.setPreferredSize(new Dimension(300, 44));
        field.setToolTipText(toolTip);
        field.setAlignmentX(Component.CENTER_ALIGNMENT);
    }

    private JButton createPrimaryButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBackground(new Color(28, 100, 242));
        button.setForeground(Color.WHITE);
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        button.setPreferredSize(new Dimension(300, 44));
        return button;
    }

    private JButton createSecondaryButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBackground(new Color(240, 242, 245));
        button.setForeground(new Color(33, 37, 41));
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        button.setPreferredSize(new Dimension(300, 44));
        return button;
    }

    private void handleLogin() {
        String login = loginField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (login.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Veuillez remplir tous les champs.",
                    "Erreur",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (userService.authenticate(login, password)) {
            Session session = Session.getInstance();
            dispose();
            MainFrame mainFrame = new MainFrame();
            mainFrame.setVisible(true);
        } else {
            JOptionPane.showMessageDialog(
                    this,
                    "Identifiant ou mot de passe incorrect.",
                    "Erreur de connexion",
                    JOptionPane.ERROR_MESSAGE);
            passwordField.setText("");
        }
    }

    private void handleRegister() {
        String login = loginField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (login.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Veuillez remplir tous les champs.",
                    "Erreur",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (userService.userExists(login)) {
            JOptionPane.showMessageDialog(
                    this,
                    "Cet identifiant existe déjà.",
                    "Erreur",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (userService.createUser(login, password, "CLIENT", null, null)) {
            JOptionPane.showMessageDialog(
                    this,
                    "Inscription réussie ! Vous pouvez maintenant vous connecter.",
                    "Succès",
                    JOptionPane.INFORMATION_MESSAGE);
            loginField.setText("");
            passwordField.setText("");
        } else {
            JOptionPane.showMessageDialog(
                    this,
                    "Erreur lors de l'inscription.",
                    "Erreur",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void applyLookAndFeelDefaults() {
        try {
            FlatLightLaf.setup();

            UIManager.put("Button.arc", 18);
            UIManager.put("Component.arc", 18);
            UIManager.put("TextComponent.arc", 14);
            UIManager.put("ProgressBar.arc", 18);

            UIManager.put("TextField.margin", new Insets(10, 14, 10, 14));
            UIManager.put("PasswordField.margin", new Insets(10, 14, 10, 14));
            UIManager.put("Button.margin", new Insets(10, 14, 10, 14));

            UIManager.put("Component.focusWidth", 1);
            UIManager.put("Button.innerFocusWidth", 0);

            UIManager.put("TextField.background", Color.WHITE);
            UIManager.put("PasswordField.background", Color.WHITE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        AuthBootstrap.initialize();

        SwingUtilities.invokeLater(() -> {
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
        });
    }

    private static class PizzaLogo extends JPanel {
        public PizzaLogo() {
            setOpaque(false);
            setPreferredSize(new Dimension(78, 78));
            setMaximumSize(new Dimension(78, 78));
            setMinimumSize(new Dimension(78, 78));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int x = 9;
            int y = 9;
            int w = 60;
            int h = 60;

            g2.setColor(new Color(255, 243, 214));
            g2.fillOval(x, y, w, h);

            g2.setColor(new Color(236, 159, 90));
            g2.setStroke(new BasicStroke(6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.drawOval(x + 2, y + 2, w - 4, h - 4);

            g2.setColor(new Color(230, 74, 25));
            g2.fillOval(x + 14, y + 14, 10, 10);
            g2.fillOval(x + 34, y + 18, 10, 10);
            g2.fillOval(x + 24, y + 34, 10, 10);

            g2.setColor(new Color(67, 160, 71));
            g2.fill(new RoundRectangle2D.Double(x + 20, y + 16, 10, 4, 4, 4));
            g2.fill(new RoundRectangle2D.Double(x + 40, y + 30, 10, 4, 4, 4));
            g2.fill(new RoundRectangle2D.Double(x + 16, y + 42, 10, 4, 4, 4));

            g2.setColor(new Color(255, 255, 255, 180));
            g2.setStroke(new BasicStroke(2f));
            g2.drawArc(x + 12, y + 10, 18, 12, 20, 140);

            g2.dispose();
        }
    }
}