package com.bidverse.frontend;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * LoginPage is a JPanel used inside MainFrame.
 */
public class LoginPage extends JPanel {
    private MainFrame navigator;
    private RoundedTextField emailField;
    private RoundedPasswordField passwordField;

    // Title animation variables
    private JLabel title;
    private Timer titleTimer;
    private float brightness = 1.0f;
    private boolean increasing = false;
    private final Color TITLE_BASE_COLOR = new Color(75, 0, 130); // Indigo/Dark Purple
    private final Font PRETTY_TITLE_FONT = new Font("Verdana", Font.BOLD, 30); // Changed font/size

    public LoginPage(MainFrame navigator) {
        this.navigator = navigator;
        setLayout(null);
        GradientPanel bg = new GradientPanel();
        bg.setLayout(null);
        bg.setBounds(0, 0, 800, 600);

        // --- TITLE CHANGES (Pretty Font, Increased Size, Animation Setup) ---
        title = new JLabel("B I D V E R S E", SwingConstants.CENTER);
        title.setFont(PRETTY_TITLE_FONT);
        title.setForeground(TITLE_BASE_COLOR);
        title.setBounds(250, 75, 300, 60); // Increased height for larger font
        bg.add(title);

        setupTitleAnimation(); // Initialize and start the animation

        // Email
        JLabel emailLabel = new JLabel("Email:");
        emailLabel.setFont(Theme.LABEL_FONT);
        emailLabel.setBounds(220, 150, 80, 25);
        bg.add(emailLabel);

        emailField = new RoundedTextField();
        emailField.setBounds(220, 180, 360, 36);
        emailField.setBackground(Color.WHITE);
        emailField.setForeground(Color.BLACK);
        emailField.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        bg.add(emailField);

        // Password
        JLabel passLabel = new JLabel("Password:");
        passLabel.setFont(Theme.LABEL_FONT);
        passLabel.setBounds(220, 240, 80, 25);
        bg.add(passLabel);

        passwordField = new RoundedPasswordField();
        passwordField.setBounds(220, 270, 360, 36);
        passwordField.setBackground(Color.WHITE);
        passwordField.setForeground(Color.BLACK);
        passwordField.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        bg.add(passwordField);

        // Login button
        // --- CHANGE 4: Use RoundedButton ---
        RoundedButton loginBtn = new RoundedButton("Login", 44); // Height is 44
        loginBtn.setFont(Theme.BUTTON_FONT);
        loginBtn.setBackground(Theme.PURPLE);
        loginBtn.setForeground(Color.WHITE);
        loginBtn.setBounds(220, 350, 360, 44);
        loginBtn.addActionListener(e -> onLogin());
        bg.add(loginBtn);

        // Register button
        // --- CHANGE 5: Use RoundedButton ---
        RoundedButton registerBtn = new RoundedButton("Register", 44); // Height is 44
        registerBtn.setFont(Theme.BUTTON_FONT);
        registerBtn.setBackground(Theme.LIGHT_PURPLE);
        registerBtn.setForeground(Color.WHITE);
        registerBtn.setBounds(220, 410, 360, 44);
        registerBtn.addActionListener(e -> navigator.showCard("profile"));
        bg.add(registerBtn);

        add(bg);
    }

    // Method to setup and start the title animation
    private void setupTitleAnimation() {
        // Timer fires every 50ms
        titleTimer = new Timer(50, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Change brightness slowly
                if (increasing) {
                    brightness += 0.03f;
                    if (brightness >= 1.0f) {
                        brightness = 1.0f;
                        increasing = false;
                    }
                } else {
                    brightness -= 0.03f;
                    if (brightness <= 0.5f) { // Title pulses darker up to 50% brightness
                        brightness = 0.5f;
                        increasing = true;
                    }
                }

                // Apply the new color
                Color brightenedColor = new Color(
                        Math.min(255, (int) (TITLE_BASE_COLOR.getRed() * brightness)),
                        Math.min(255, (int) (TITLE_BASE_COLOR.getGreen() * brightness)),
                        Math.min(255, (int) (TITLE_BASE_COLOR.getBlue() * brightness)));
                title.setForeground(brightenedColor);
            }
        });
        titleTimer.start();
    }

    private void onLogin() {
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (email.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter email and password", "Validation",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        boolean ok = AuthService.login(email, password);

        if (ok) {
            Main.email = email;
            Main.idInitialize();
            String userRole = AuthService.getCurrentUserRole();

            emailField.setText("");
            passwordField.setText("");

            JOptionPane.showMessageDialog(this, "Login successful as " + userRole, "Success",
                    JOptionPane.INFORMATION_MESSAGE);

            if ("bidder".equalsIgnoreCase(userRole)) {
                navigator.showCard("bidder_dashboard");
            } else if ("seller".equalsIgnoreCase(userRole)) {
                navigator.showCard("sellerhome");
            } else {
                navigator.closeAppWithSuccess("Login successful, but role is undetermined.");
            }

        } else {
            JOptionPane.showMessageDialog(this, "Invalid credentials or user not found", "Login failed",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    // =================================================================
    // === NESTED CLASSES (REQUIRED TO AVOID NEW FILES) =================
    // =================================================================

    // New Class for Rounded Buttons
    private static class RoundedButton extends JButton {
        private final int arcSize;

        public RoundedButton(String text, int height) {
            super(text);
            this.arcSize = height; // Set arc size equal to button height for a pill shape
            setContentAreaFilled(false); // Make the button transparent so the custom paint shows
            setFocusPainted(false);
            setBorderPainted(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Paint background (color set by setBackground)
            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arcSize, arcSize);

            // If the button is pressed, darken the background slightly
            if (getModel().isArmed()) {
                g2.setColor(getBackground().darker());
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arcSize, arcSize);
            }

            super.paintComponent(g2); // Paint the text
            g2.dispose();
        }

        @Override
        protected void paintBorder(Graphics g) {
            // No border painting needed as borderPainted is false
        }
    }

    // Existing Rounded TextField Class
    private static class RoundedTextField extends JTextField {
        private Shape shape;
        private final int arcSize = 15;

        public RoundedTextField() {
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arcSize, arcSize);
            super.paintComponent(g2);
            g2.dispose();
        }

        @Override
        protected void paintBorder(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Color.LIGHT_GRAY); // Subtle border
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arcSize, arcSize);
            g2.dispose();
        }

        @Override
        public boolean contains(int x, int y) {
            if (shape == null || !shape.getBounds().equals(getBounds())) {
                shape = new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, arcSize, arcSize);
            }
            return shape.contains(x, y);
        }
    }

    // Existing Rounded PasswordField Class
    private static class RoundedPasswordField extends JPasswordField {
        private Shape shape;
        private final int arcSize = 15;

        public RoundedPasswordField() {
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arcSize, arcSize);
            super.paintComponent(g2);
            g2.dispose();
        }

        @Override
        protected void paintBorder(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Color.LIGHT_GRAY); // Subtle border
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arcSize, arcSize);
            g2.dispose();
        }

        @Override
        public boolean contains(int x, int y) {
            if (shape == null || !shape.getBounds().equals(getBounds())) {
                shape = new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, arcSize, arcSize);
            }
            return shape.contains(x, y);
        }
    }
}