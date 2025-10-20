package com.bidverse.frontend;

import javax.swing.*;
import java.awt.*;
// no changes: action imports removed

public class SellerHome extends JPanel {
    private final JFrame mainFrame;

    public SellerHome(JFrame frame) {
        this.mainFrame = frame;
        setLayout(new BorderLayout());

        // --- 1. Top Bar (Profile and Logout) ---
        JPanel topPanel = new JPanel(new BorderLayout());
        JLabel welcomeLabel = new JLabel("Welcome, " + Main.sellerName + "!");
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 16));
        topPanel.add(welcomeLabel, BorderLayout.WEST);

        JPanel profilePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton profileButton = new JButton("Profile 👤");
        JButton logoutButton = new JButton("Logout");

        profileButton.addActionListener(e -> {
            SellerProfile profile = new SellerProfile(mainFrame);
            Main.switchToPanel(mainFrame, profile);
        });

        logoutButton.addActionListener(e -> {
            JOptionPane.showMessageDialog(mainFrame, "Logged out successfully.", "Logout", JOptionPane.INFORMATION_MESSAGE);
            System.exit(0); 
        });

        profilePanel.add(profileButton);
        profilePanel.add(logoutButton);
        topPanel.add(profilePanel, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        // --- 2. Center Menu ---
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;

        JButton createBidButton = new JButton("Create New Auction");
        createBidButton.setPreferredSize(new Dimension(250, 60));
        createBidButton.addActionListener(e -> new CreateBidWin(mainFrame).setVisible(true));
        gbc.gridy = 0;
        centerPanel.add(createBidButton, gbc);

        JButton viewCurrentBidsButton = new JButton("View Current Auctions");
        viewCurrentBidsButton.setPreferredSize(new Dimension(250, 60));
        viewCurrentBidsButton.addActionListener(e -> {
            SellerCurrentBids currentBidsPanel = new SellerCurrentBids(mainFrame);
            Main.switchToPanel(mainFrame, currentBidsPanel);
        });
        gbc.gridy = 1;
        centerPanel.add(viewCurrentBidsButton, gbc);

        JButton viewPastBidsButton = new JButton("View Past Auctions");
        viewPastBidsButton.setPreferredSize(new Dimension(250, 60));
        viewPastBidsButton.addActionListener(e -> {
            SellerPastBids pastBidsPanel = new SellerPastBids(mainFrame);
            Main.switchToPanel(mainFrame, pastBidsPanel);
        });
        gbc.gridy = 2;
        centerPanel.add(viewPastBidsButton, gbc);

        add(centerPanel, BorderLayout.CENTER);
    }
}