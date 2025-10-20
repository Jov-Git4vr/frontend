
package com.bidverse.frontend;

import javax.swing.*;

import java.awt.*;

/**
 * MainFrame holds CardLayout and all pages.
 * Card names: "intro", "login", "profile", "bidder", "seller"
 */
public class MainFrame extends JFrame {
    private CardLayout cardLayout;
    private JPanel cards;

    public MainFrame() {
        setTitle("Bidverse");
        // FIX SIZE TYPO: setSize(800, 600)
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        cardLayout = new CardLayout();
        cards = new JPanel(cardLayout);

        // Pages are JPanels that take a reference to MainFrame for navigation
        // ADDED: The new BidverseIntroPanel
        BidverseIntroPanel intro = new BidverseIntroPanel(this);

        LoginPage login = new LoginPage(this);
        ProfileSelectionPage profile = new ProfileSelectionPage(this);
        BidderRegisterPage bidder = new BidderRegisterPage(this);
        SellerRegisterPage seller = new SellerRegisterPage(this);
        SellerHome sellerHome = new SellerHome(this);
        
        // ADD intro card first
        cards.add(intro, "intro");

        cards.add(login, "login");
        cards.add(profile, "profile");
        cards.add(bidder, "bidder");
        cards.add(seller, "seller");
        cards.add(sellerHome, "sellerhome");

        add(cards);

        // SHOW intro card first
        showCard("intro");

        setVisible(true);
    }

    public void showCard(String name) {
        cardLayout.show(cards, name);
    }

    /**
     * Called by pages when login/register successful to close the app.
     */
    public void closeAppWithSuccess(String message) {
        JOptionPane.showMessageDialog(this, message);
        dispose();
        System.exit(0);
    }
}