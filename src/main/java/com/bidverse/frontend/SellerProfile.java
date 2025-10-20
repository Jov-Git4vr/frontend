package com.bidverse.frontend;

import javax.swing.*;
import java.awt.*;

import static javax.swing.JOptionPane.showMessageDialog;

public class SellerProfile extends JPanel {
    // --- COLOR CONSTANTS ---
    private static final Color PRIMARY_ACCENT = new Color(30, 144, 255); // Deep Sky Blue
    private static final Color DANGER_COLOR = new Color(220, 53, 69);   // Bootstrap Red for delete actions
    private static final Color LIGHT_BG = new Color(248, 248, 248);     // Light Gray Background
    private static final Color LINK_COLOR = new Color(0, 102, 204);     // Standard Blue for link/back button

    private final JFrame mainFrame;
    private BackendClient.SellerDto currentSeller;

    private final JLabel nameValue = new JLabel();
    private final JLabel emailValue = new JLabel(Main.email); // Set email once as it's immutable
    private final JLabel phoneValue = new JLabel();
    private final JLabel paymentValue = new JLabel();

    public SellerProfile(JFrame frame) {
        this.mainFrame = frame;
        setLayout(new BorderLayout(15, 15)); // Increased padding for cleaner look
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        setBackground(LIGHT_BG); // Set main panel background

        // Back button to Home (Styled as a link)
        JButton backButton = new JButton("<- Back to Home");
        backButton.setFont(new Font("Arial", Font.PLAIN, 12));
        backButton.setForeground(LINK_COLOR);
        backButton.setContentAreaFilled(false); // Transparent background
        backButton.setBorderPainted(false);
        backButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backButton.addActionListener(e -> Main.switchToPanel(mainFrame, new SellerHome(mainFrame)));
        
        // Wrap back button in a panel to align left
        JPanel northPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        northPanel.setBackground(LIGHT_BG);
        northPanel.add(backButton);
        add(northPanel, BorderLayout.NORTH);

        // --- Profile Display Panel ---
        JPanel profilePanel = new JPanel(new GridBagLayout());
        profilePanel.setBackground(Color.WHITE); // White background for the content area
        profilePanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1),
            BorderFactory.createEmptyBorder(30, 30, 30, 30)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 15, 10, 15);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Title
        JLabel title = new JLabel("Seller Profile");
        title.setFont(new Font("Arial", Font.BOLD, 28));
        title.setForeground(PRIMARY_ACCENT.darker()); // Darker accent for emphasis
        gbc.gridwidth = 2;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER;
        profilePanel.add(title, gbc);
        
        // Add a separator line
        gbc.gridy = 1;
        profilePanel.add(new JSeparator(SwingConstants.HORIZONTAL), gbc);
        
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridwidth = 1;
        
        // Helper to add labels and values with consistent styling
        var helper = new Object() {
            java.util.concurrent.atomic.AtomicInteger row = new java.util.concurrent.atomic.AtomicInteger(2);
            void addField(String labelText, JLabel valueLabel) {
                // Label (Bold)
                JLabel label = new JLabel(labelText);
                label.setFont(new Font("Arial", Font.BOLD, 14));
                gbc.gridy = row.get(); gbc.gridx = 0; profilePanel.add(label, gbc);
                
                // Value
                valueLabel.setFont(new Font("Arial", Font.PLAIN, 14));
                gbc.gridx = 1; profilePanel.add(valueLabel, gbc);
                row.incrementAndGet();
            }
        };

        // Profile Fields
        helper.addField("Name:", nameValue);
        helper.addField("Email:", emailValue);
        helper.addField("Phone No.:", phoneValue);
        helper.addField("Payment Details:", paymentValue);

        // Center the profile panel within the frame
        JPanel centerWrapper = new JPanel(new GridBagLayout());
        centerWrapper.setBackground(LIGHT_BG);
        centerWrapper.add(profilePanel);
        add(centerWrapper, BorderLayout.CENTER);

        // --- Button Panel ---
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 50, 20));
        buttonPanel.setBackground(LIGHT_BG);
        
        // 1. Update Button (Primary Accent)
        JButton updateButton = new JButton("Update Profile");
        updateButton.setFont(new Font("Arial", Font.BOLD, 14));
        updateButton.setBackground(PRIMARY_ACCENT);
        updateButton.setForeground(Color.WHITE);
        updateButton.setOpaque(true);
        updateButton.setBorderPainted(false);
        updateButton.addActionListener(e -> openUpdateWindow());

        // 2. Delete Button (Danger Color)
        JButton deleteButton = new JButton("Delete User");
        deleteButton.setFont(new Font("Arial", Font.BOLD, 14));
        deleteButton.setBackground(DANGER_COLOR);
        deleteButton.setForeground(Color.WHITE);
        deleteButton.setOpaque(true);
        deleteButton.setBorderPainted(false);
        deleteButton.addActionListener(e -> deleteUser());

        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);
        add(buttonPanel, BorderLayout.SOUTH);

        loadProfileData();
    }

    private void loadProfileData() {
        BackendClient.getSellerByEmail(Main.email).ifPresentOrElse(seller -> {
            this.currentSeller = seller;
            nameValue.setText(seller.sellerName());
            phoneValue.setText(String.valueOf(seller.phno()));
            paymentValue.setText(seller.paymentDetails());
            Main.sellerName = seller.sellerName();
            mainFrame.setTitle("BidVerse - Seller Panel (" + Main.sellerName + ")");
        }, () -> {
            showMessageDialog(mainFrame, "Failed to load profile. Connection error.", "Error", JOptionPane.ERROR_MESSAGE);
            nameValue.setText("Error Loading Data");
        });
    }

    private void openUpdateWindow() {
        if (currentSeller == null) return;

        JDialog updateDialog = new JDialog(mainFrame, "Update Profile", true);
        updateDialog.setLayout(new GridBagLayout());
        updateDialog.setSize(400, 300); // Slightly larger
        updateDialog.setLocationRelativeTo(mainFrame);
        updateDialog.getContentPane().setBackground(Color.WHITE); // White background for dialog
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField nameField = new JTextField(currentSeller.sellerName(), 20);
        JTextField phoneField = new JTextField(String.valueOf(currentSeller.phno()), 20);
        JTextArea paymentArea = new JTextArea(currentSeller.paymentDetails(), 4, 20);
        paymentArea.setLineWrap(true);
        JScrollPane paymentScrollPane = new JScrollPane(paymentArea);

        // Name
        gbc.gridx = 0; gbc.gridy = 0; updateDialog.add(new JLabel("Name:"), gbc);
        gbc.gridx = 1; updateDialog.add(nameField, gbc);

        // Phone
        gbc.gridx = 0; gbc.gridy = 1; updateDialog.add(new JLabel("Phone No.:"), gbc);
        gbc.gridx = 1; updateDialog.add(phoneField, gbc);

        // Payment Details
        gbc.gridx = 0; gbc.gridy = 2; updateDialog.add(new JLabel("Payment Details:"), gbc);
        gbc.gridx = 1; gbc.weighty = 1.0; gbc.fill = GridBagConstraints.BOTH; 
        updateDialog.add(paymentScrollPane, gbc);

        // OK Button (Primary Accent)
        JButton okButton = new JButton("OK");
        okButton.setFont(new Font("Arial", Font.BOLD, 14));
        okButton.setBackground(PRIMARY_ACCENT);
        okButton.setForeground(Color.WHITE);
        okButton.setOpaque(true);
        okButton.setBorderPainted(false);
        
        okButton.addActionListener(e -> {
            try {
                // ... (Update logic remains the same)
                BackendClient.SellerDto updatedDto = new BackendClient.SellerDto(
                        currentSeller.sellerId(), 
                        nameField.getText(), 
                        currentSeller.sellerEmail(),
                        Long.parseLong(phoneField.getText()), 
                        paymentArea.getText());

                BackendClient.updateSeller(Main.email, updatedDto).ifPresentOrElse(
                    savedSeller -> {
                        showMessageDialog(mainFrame, "Profile Updated Successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                        loadProfileData();
                        updateDialog.dispose();
                    }, 
                    () -> showMessageDialog(mainFrame, "Update failed. Server error.", "Error", JOptionPane.ERROR_MESSAGE)
                );
            } catch (NumberFormatException ex) {
                showMessageDialog(updateDialog, "Invalid Phone Number format.", "Input Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        gbc.gridx = 1; gbc.gridy = 3; gbc.weighty = 0; gbc.fill = GridBagConstraints.NONE; gbc.anchor = GridBagConstraints.EAST; 
        updateDialog.add(okButton, gbc);

        updateDialog.setVisible(true);
    }

    private void deleteUser() {
        int confirm = JOptionPane.showConfirmDialog(mainFrame, 
            "Are you sure you want to delete your account? This cannot be undone.", 
            "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            if (BackendClient.deleteSeller(Main.email)) {
                showMessageDialog(mainFrame, "Account deleted. Logging out.", "Success", JOptionPane.INFORMATION_MESSAGE);
                System.exit(0);
            } else {
                showMessageDialog(mainFrame, "Failed to delete account. Server error.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}