package com.projetbdd;

import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.FlatDarculaLaf;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class LoginFrame extends JFrame {
    private static boolean isDarkTheme = false;
    private final UserService userService = new UserService();
    private final ModernTextField loginField = new ModernTextField("Entre ton identifiant");
    private final ModernPasswordField passwordField = new ModernPasswordField("Entre ton mot de passe");
    private JPanel mainPanel;

    public LoginFrame() {
        super("RaPizz - Connexion");

        applyLookAndFeelDefaults();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(700, 850);
        setMinimumSize(new Dimension(600, 750));
        setLocationRelativeTo(null);
        setResizable(true);
        mainPanel = createMainContent();
        setContentPane(mainPanel);
    }

    private JPanel createMainContent() {
        JPanel mainWrapper = new JPanel(new BorderLayout());
        mainWrapper.setBackground(isDarkTheme ? new Color(30, 30, 35) : new Color(245, 247, 250));

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(isDarkTheme ? new Color(45, 45, 50) : Color.WHITE);
        card.setBorder(new EmptyBorder(20, 36, 36, 36));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        JPanel headerCard = new JPanel(new BorderLayout());
        headerCard.setOpaque(false);
        headerCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        headerCard.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel buttonPanel = new JPanel(new BorderLayout());
        buttonPanel.setOpaque(false);
        buttonPanel.setPreferredSize(new Dimension(40, 40));

        JLabel themeLabel = new JLabel("🌙");
        if (isDarkTheme) {
            themeLabel.setText("☀️");
        }
        themeLabel.setFont(new Font(null, Font.PLAIN, 20));
        themeLabel.setHorizontalAlignment(SwingConstants.CENTER);
        themeLabel.setVerticalAlignment(SwingConstants.CENTER);

        JButton themeButton = new JButton();
        themeButton.setLayout(new BorderLayout());
        themeButton.add(themeLabel, BorderLayout.CENTER);
        themeButton.setBackground(isDarkTheme ? new Color(50, 50, 55) : new Color(220, 224, 230));
        themeButton.setForeground(isDarkTheme ? Color.WHITE : new Color(24, 28, 33));
        themeButton.setFocusPainted(false);
        themeButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        themeButton.setPreferredSize(new Dimension(40, 40));
        themeButton.setMargin(new Insets(0, 0, 0, 0));
        themeButton.setOpaque(true);
        themeButton.setBorderPainted(false);
        themeButton.setContentAreaFilled(true);
        themeButton.setFocusable(false);

        themeButton.addActionListener(e -> {
            isDarkTheme = !isDarkTheme;
            toggleTheme();
        });

        headerCard.add(themeButton, BorderLayout.EAST);
        card.add(headerCard);

        PizzaLogo logo = new PizzaLogo();
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel brand = new JLabel("RAPIZZ");
        brand.setFont(new Font("Segoe UI", Font.BOLD, 16));
        brand.setForeground(new Color(28, 100, 242));
        brand.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel title = new JLabel("Connexion");
        title.setFont(new Font("Segoe UI", Font.BOLD, 30));
        title.setForeground(isDarkTheme ? Color.WHITE : new Color(24, 28, 33));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("Accède à ton espace client");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitle.setForeground(isDarkTheme ? new Color(180, 180, 190) : new Color(110, 118, 129));
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

        loginField.setAlignmentX(Component.CENTER_ALIGNMENT);
        loginField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        loginField.setPreferredSize(new Dimension(360, 60));
        card.add(loginField);
        card.add(Box.createVerticalStrut(18));

        JPanel passwordLabelPanel = createCenteredLabelPanel("Mot de passe");
        card.add(passwordLabelPanel);
        card.add(Box.createVerticalStrut(8));

        passwordField.setAlignmentX(Component.CENTER_ALIGNMENT);
        passwordField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        passwordField.setPreferredSize(new Dimension(360, 60));
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
        footer.setForeground(isDarkTheme ? new Color(120, 120, 130) : new Color(140, 146, 155));
        footer.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(footer);

        mainWrapper.add(card, BorderLayout.CENTER);
        return mainWrapper;
    }

    private JPanel createCenteredLabelPanel(String text) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        panel.setOpaque(false);
        panel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));

        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));
        label.setForeground(isDarkTheme ? new Color(200, 200, 210) : new Color(45, 51, 59));
        label.setHorizontalAlignment(SwingConstants.CENTER);

        panel.add(label);
        return panel;
    }

    private JButton createPrimaryButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBackground(new Color(28, 100, 242));
        button.setForeground(Color.WHITE);
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        button.setPreferredSize(new Dimension(330, 44));
        return button;
    }

    private JButton createSecondaryButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBackground(isDarkTheme ? new Color(60, 60, 65) : new Color(240, 242, 245));
        button.setForeground(isDarkTheme ? Color.WHITE : new Color(33, 37, 41));
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        button.setPreferredSize(new Dimension(330, 44));
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
            MainFrame.setDarkTheme(isDarkTheme);
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
            passwordField.hidePassword();
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
            // Auto-login après création
            if (userService.authenticate(login, password)) {
                MainFrame.setDarkTheme(isDarkTheme);
                dispose();
                MainFrame mainFrame = new MainFrame();
                mainFrame.setVisible(true);
            } else {
                JOptionPane.showMessageDialog(
                        this,
                        "Inscription réussie mais erreur lors de la connexion. Veuillez vous connecter manuellement.",
                        "Attention",
                        JOptionPane.WARNING_MESSAGE);
                loginField.setText("");
                passwordField.setText("");
                passwordField.hidePassword();
            }
        } else {
            JOptionPane.showMessageDialog(
                    this,
                    "Erreur lors de l'inscription.",
                    "Erreur",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void toggleTheme() {
        try {
            if (isDarkTheme) {
                FlatDarculaLaf.setup();
            } else {
                FlatLightLaf.setup();
            }

            UIManager.put("Button.arc", 18);
            UIManager.put("Component.arc", 18);
            UIManager.put("TextComponent.arc", 18);
            UIManager.put("ProgressBar.arc", 18);
            UIManager.put("Component.focusWidth", 1);
            UIManager.put("Button.innerFocusWidth", 0);
            
            // Force text color for visibility in both themes
            UIManager.put("TextComponent.foreground", isDarkTheme ? Color.WHITE : new Color(24, 28, 33));

            GradientHeader.setDarkTheme(isDarkTheme);
            SwingUtilities.updateComponentTreeUI(this);
            
            // Update text field colors directly (safe check)
            if (loginField != null && loginField.field != null) {
                Color textColor = isDarkTheme ? Color.WHITE : new Color(24, 28, 33);
                loginField.field.setForeground(textColor);
                loginField.field.setCaretColor(textColor);
            }
            if (passwordField != null && passwordField.field != null) {
                Color textColor = isDarkTheme ? Color.WHITE : new Color(24, 28, 33);
                passwordField.field.setForeground(textColor);
                passwordField.field.setCaretColor(textColor);
            }
            
            mainPanel = createMainContent();
            setContentPane(mainPanel);
            revalidate();
            repaint();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void applyLookAndFeelDefaults() {
        try {
            FlatLightLaf.setup();

            UIManager.put("Button.arc", 18);
            UIManager.put("Component.arc", 18);
            UIManager.put("TextComponent.arc", 18);
            UIManager.put("ProgressBar.arc", 18);

            UIManager.put("TextField.margin", new Insets(10, 14, 10, 14));
            UIManager.put("PasswordField.margin", new Insets(10, 14, 10, 14));
            UIManager.put("Button.margin", new Insets(10, 14, 10, 14));

            UIManager.put("Component.focusWidth", 1);
            UIManager.put("Button.innerFocusWidth", 0);

            UIManager.put("TextField.background", Color.WHITE);
            UIManager.put("PasswordField.background", Color.WHITE);
            UIManager.put("TextComponent.foreground", new Color(24, 28, 33));
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

            if (isDarkTheme) {
                g2.setColor(new Color(70, 50, 30));
                g2.fillOval(x, y, w, h);

                g2.setColor(new Color(180, 120, 80));
                g2.setStroke(new BasicStroke(6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawOval(x + 2, y + 2, w - 4, h - 4);

                g2.setColor(new Color(220, 100, 50));
                g2.fillOval(x + 14, y + 14, 10, 10);
                g2.fillOval(x + 34, y + 18, 10, 10);
                g2.fillOval(x + 24, y + 34, 10, 10);

                g2.setColor(new Color(100, 180, 100));
                g2.fill(new RoundRectangle2D.Double(x + 20, y + 16, 10, 4, 4, 4));
                g2.fill(new RoundRectangle2D.Double(x + 40, y + 30, 10, 4, 4, 4));
                g2.fill(new RoundRectangle2D.Double(x + 16, y + 42, 10, 4, 4, 4));

                g2.setColor(new Color(150, 150, 150, 150));
                g2.setStroke(new BasicStroke(2f));
                g2.drawArc(x + 12, y + 10, 18, 12, 20, 140);
            } else {
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
            }

            g2.dispose();
        }
    }

    private static class ModernTextField extends JPanel {
        protected final JTextField field;
        private final String placeholder;
        private boolean focused = false;

        public ModernTextField(String placeholder) {
            this.placeholder = placeholder;
            this.field = new JTextField();

            setOpaque(false);
            setLayout(new BorderLayout());
            setBorder(new EmptyBorder(0, 0, 0, 0));

            field.setOpaque(false);
            field.setBorder(new EmptyBorder(0, 16, 0, 16));
            field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            field.setForeground(isDarkTheme ? Color.WHITE : new Color(24, 28, 33));
            field.setCaretColor(isDarkTheme ? Color.WHITE : new Color(24, 28, 33));

            field.addFocusListener(new java.awt.event.FocusAdapter() {
                @Override
                public void focusGained(java.awt.event.FocusEvent e) {
                    focused = true;
                    repaint();
                }

                @Override
                public void focusLost(java.awt.event.FocusEvent e) {
                    focused = false;
                    repaint();
                }
            });

            field.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) {
                    repaint();
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    repaint();
                }

                @Override
                public void changedUpdate(DocumentEvent e) {
                    repaint();
                }
            });

            add(field, BorderLayout.CENTER);
        }

        public String getText() {
            return field.getText();
        }

        public void setText(String text) {
            field.setText(text);
            repaint();
        }

        @Override
        public void setMaximumSize(Dimension maximumSize) {
            super.setMaximumSize(maximumSize);
        }

        @Override
        public void setPreferredSize(Dimension preferredSize) {
            super.setPreferredSize(preferredSize);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();
            int inset = 1;

            g2.setColor(isDarkTheme ? new Color(50, 50, 55) : new Color(248, 250, 252));
            g2.fillRoundRect(inset, inset, w - inset * 2 - 1, h - inset * 2 - 1, 22, 22);

            g2.setColor(focused ? new Color(28, 100, 242) : (isDarkTheme ? new Color(70, 70, 75) : new Color(220, 224, 230)));
            g2.setStroke(new BasicStroke(focused ? 2f : 1f));
            g2.drawRoundRect(inset, inset, w - inset * 2 - 1, h - inset * 2 - 1, 22, 22);

            if (field.getText().isEmpty() && !field.hasFocus()) {
                g2.setColor(isDarkTheme ? new Color(120, 120, 130) : new Color(145, 152, 161));
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                FontMetrics fm = g2.getFontMetrics();
                int textY = (h - fm.getHeight()) / 2 + fm.getAscent();
                g2.drawString(placeholder, 16, textY);
            }

            g2.dispose();
            super.paintComponent(g);
        }
    }

    private static class ModernPasswordField extends JPanel {
        private final JPasswordField field;
        private final JButton toggleButton;
        private final String placeholder;
        private boolean focused = false;
        private char defaultEchoChar;

        public ModernPasswordField(String placeholder) {
            this.placeholder = placeholder;
            this.field = new JPasswordField();
            this.toggleButton = new JButton("Afficher");

            setOpaque(false);
            setLayout(new BorderLayout());
            setBorder(new EmptyBorder(0, 0, 0, 0));

            field.setOpaque(false);
            field.setBorder(new EmptyBorder(0, 16, 0, 6));
            field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            field.setForeground(isDarkTheme ? Color.WHITE : new Color(24, 28, 33));
            field.setCaretColor(isDarkTheme ? Color.WHITE : new Color(24, 28, 33));
            defaultEchoChar = field.getEchoChar();

            toggleButton.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            toggleButton.setForeground(new Color(28, 100, 242));
            toggleButton.setBorderPainted(false);
            toggleButton.setContentAreaFilled(false);
            toggleButton.setFocusPainted(false);
            toggleButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
            toggleButton.setMargin(new Insets(0, 8, 0, 12));

            toggleButton.addActionListener(e -> {
                if (field.getEchoChar() == (char) 0) {
                    hidePassword();
                } else {
                    field.setEchoChar((char) 0);
                    toggleButton.setText("Masquer");
                }
            });

            field.addFocusListener(new java.awt.event.FocusAdapter() {
                @Override
                public void focusGained(java.awt.event.FocusEvent e) {
                    focused = true;
                    repaint();
                }

                @Override
                public void focusLost(java.awt.event.FocusEvent e) {
                    focused = false;
                    repaint();
                }
            });

            field.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) {
                    repaint();
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    repaint();
                }

                @Override
                public void changedUpdate(DocumentEvent e) {
                    repaint();
                }
            });

            add(field, BorderLayout.CENTER);
            add(toggleButton, BorderLayout.EAST);
        }

        public char[] getPassword() {
            return field.getPassword();
        }

        public void setText(String text) {
            field.setText(text);
            repaint();
        }

        public void hidePassword() {
            field.setEchoChar(defaultEchoChar);
            toggleButton.setText("Afficher");
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();
            int inset = 1;

            g2.setColor(isDarkTheme ? new Color(50, 50, 55) : new Color(248, 250, 252));
            g2.fillRoundRect(inset, inset, w - inset * 2 - 1, h - inset * 2 - 1, 22, 22);

            g2.setColor(focused ? new Color(28, 100, 242)
                    : (isDarkTheme ? new Color(70, 70, 75) : new Color(220, 224, 230)));
            g2.setStroke(new BasicStroke(focused ? 2f : 1f));
            g2.drawRoundRect(inset, inset, w - inset * 2 - 1, h - inset * 2 - 1, 22, 22);

            if (field.getPassword().length == 0 && !field.hasFocus()) {
                g2.setColor(isDarkTheme ? new Color(120, 120, 130) : new Color(145, 152, 161));
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                FontMetrics fm = g2.getFontMetrics();
                int textY = (h - fm.getHeight()) / 2 + fm.getAscent();
                g2.drawString(placeholder, 16, textY);
            }

            g2.dispose();
            super.paintComponent(g);
        }
    }
}