package com.bidverse.frontend;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
// removed unused TableCellEditor import
import javax.swing.table.TableCellRenderer;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.util.List;
import java.util.stream.Collectors;

import static javax.swing.JOptionPane.showMessageDialog;

public class SellerCurrentBids extends JPanel {

    // --- Custom Color Palette ---
    private static final Color PRIMARY_COLOR = new Color(25, 118, 210);      // Deep Blue (#1976D2)
    private static final Color ACCENT_COLOR = new Color(66, 165, 245);       // Light Blue (#42A5F5) - for selection/highlight
    private static final Color BACKGROUND_COLOR = new Color(245, 245, 245); // Light Gray (#F5F5F5)
    private static final Color TEXT_COLOR = new Color(51, 51, 51);           // Dark Gray (#333333)
    private static final Color DELETE_COLOR = new Color(210, 25, 25);        // Red for Delete
    private static final Color END_BID_COLOR = new Color(255, 152, 0);       // Orange for End Bid

    private final JFrame mainFrame;
    private JTable currentBidsTable;
    private DefaultTableModel tableModel;

    public SellerCurrentBids(JFrame frame) {
        this.mainFrame = frame;

        // Apply background color
        setBackground(BACKGROUND_COLOR);
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Back button (Styled)
        JButton backButton = new JButton("<- Back to Home");
        styleActionButton(backButton, PRIMARY_COLOR, Color.WHITE);
        backButton.addActionListener(e -> Main.switchToPanel(mainFrame, new SellerHome(mainFrame)));
        
        // Wrap back button for better layout control
        JPanel northPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        northPanel.setBackground(BACKGROUND_COLOR);
        northPanel.add(backButton);
        add(northPanel, BorderLayout.NORTH);

        // Table Setup
        String[] columnNames = {"S.No.", "Auction ID", "Item Name", "Highest Bid", "Winner", "Actions"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                // Only the "Actions" column is clickable/editable
                return column == 5; 
            }
        };
        currentBidsTable = new JTable(tableModel);
        currentBidsTable.setRowHeight(30);

        // Table Styling
        currentBidsTable.setBackground(Color.WHITE);
        currentBidsTable.setForeground(TEXT_COLOR);
        currentBidsTable.setGridColor(BACKGROUND_COLOR.darker());
        currentBidsTable.setSelectionBackground(ACCENT_COLOR);
        currentBidsTable.setSelectionForeground(Color.WHITE);
        currentBidsTable.setFont(new Font("SansSerif", Font.PLAIN, 12));

        // Table Header Styling
        currentBidsTable.getTableHeader().setBackground(PRIMARY_COLOR);
        currentBidsTable.getTableHeader().setForeground(Color.WHITE);
        currentBidsTable.getTableHeader().setFont(currentBidsTable.getTableHeader().getFont().deriveFont(Font.BOLD, 14f));
        currentBidsTable.getTableHeader().setResizingAllowed(false);
        currentBidsTable.getTableHeader().setReorderingAllowed(false);

        // Custom Renderer/Editor for the Actions column
        currentBidsTable.getColumn("Actions").setCellRenderer(new ButtonRenderer());
        // Pass a dummy JTextField to the DefaultCellEditor super constructor
        currentBidsTable.getColumn("Actions").setCellEditor(new ButtonEditor(new JTextField(), this::handleAction));

        // Add table to scroll pane
        JScrollPane scrollPane = new JScrollPane(currentBidsTable);
        scrollPane.getViewport().setBackground(Color.WHITE);
        add(scrollPane, BorderLayout.CENTER);

        loadCurrentBids();
    }

    private void styleActionButton(JButton button, Color background, Color foreground) {
        button.setBackground(background);
        button.setForeground(foreground);
        button.setFocusPainted(false);
        button.setFont(button.getFont().deriveFont(Font.BOLD));
    }

    private void loadCurrentBids() {
        tableModel.setRowCount(0); // Clear existing data
        
        // This part assumes BackendClient, Main, SellerHome exist and are working as intended
        List<BackendClient.AuctionItemDto> allAuctions = BackendClient.getAuctionsBySellerEmail(Main.email);
        
        // Filter by status = 'OPEN'
        List<BackendClient.AuctionItemDto> openAuctions = allAuctions.stream()
            .filter(a -> "OPEN".equalsIgnoreCase(a.status()))
            .collect(Collectors.toList());

        for (int i = 0; i < openAuctions.size(); i++) {
            BackendClient.AuctionItemDto auction = openAuctions.get(i);
            Long auctionId = auction.auctionId();

            // Fetch Highest Bid and Winner for this row (use AtomicReference to allow mutation inside lambdas)
            java.util.concurrent.atomic.AtomicReference<String> highestBidRef = new java.util.concurrent.atomic.AtomicReference<>("N/A");
            java.util.concurrent.atomic.AtomicReference<String> winnerNameRef = new java.util.concurrent.atomic.AtomicReference<>("N/A");

            BackendClient.getHighestBid(auctionId).ifPresent(bid -> {
                highestBidRef.set(String.format("$%.2f", bid.bidAmount()));

                // Flow: auction_id -> getHighestBid() -> bid_id -> getBidderbyId() -> bidder_name
                if (bid.bidderId() != null) {
                    BackendClient.getBidderById(bid.bidderId()).ifPresent(bidder -> {
                        winnerNameRef.set(bidder.bidderName());
                    });
                }
            });

            // Columns: S.No., Auction ID, Item Name, Highest Bid, Winner, Actions
            tableModel.addRow(new Object[]{
                i + 1, 
                auctionId, 
                auction.title(),
                highestBidRef.get(), 
                winnerNameRef.get(), 
                "View Details / End Bid" // This is the button text for the Actions column
            });
        }

        if (openAuctions.isEmpty()) {
            tableModel.addRow(new Object[]{"-", "-", "No Current Auctions Found.", "-", "-", "-"});
        }
    }

    private void handleAction(int row) {
        if (row < 0 || row >= tableModel.getRowCount()) return;
        Long auctionId = (Long) tableModel.getValueAt(row, 1);
        String actionType = (String) tableModel.getValueAt(row, 5);
        
        if ("View Details / End Bid".equals(actionType)) {
            // Open view/update/delete window
            openAuctionDetailsWindow(auctionId);
        }
    }
    
    // Helper method to display full auction details and actions
    private void openAuctionDetailsWindow(Long auctionId) {
        JDialog detailDialog = new JDialog(mainFrame, "Auction Details (ID: " + auctionId + ")", true);
        detailDialog.setLayout(new BorderLayout(10, 10));
        detailDialog.setSize(550, 500);
        detailDialog.setLocationRelativeTo(mainFrame);
        
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(BACKGROUND_COLOR); // Styled background
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8); // Increased insets for better spacing
        gbc.anchor = GridBagConstraints.WEST;

        // Load Auction Data
        BackendClient.getAuctionById(auctionId).ifPresentOrElse(auction -> {
            // Fields for display/update
            JTextField titleField = new JTextField(auction.title(), 20);
            JTextField categoryField = new JTextField(auction.category(), 20);
            JTextArea descriptionArea = new JTextArea(auction.description(), 3, 20);
            JTextField startTimeField = new JTextField(auction.startTime(), 15);
            JTextField endTimeField = new JTextField(auction.endTime(), 15);
            JTextField basePriceField = new JTextField(String.valueOf(auction.basePrice()), 10);

            // Add fields to panel
            java.util.concurrent.atomic.AtomicInteger row = new java.util.concurrent.atomic.AtomicInteger(0);
            var helper = new Object() {
                void addField(String labelText, JComponent component, int height) {
                    // Style label
                    JLabel label = new JLabel(labelText);
                    label.setForeground(TEXT_COLOR);
                    label.setFont(label.getFont().deriveFont(Font.BOLD));

                    gbc.gridx = 0; gbc.gridy = row.get(); panel.add(label, gbc);
                    gbc.gridx = 1; 
                    gbc.fill = GridBagConstraints.HORIZONTAL;
                    
                    if (component instanceof JScrollPane) {
                        component.setPreferredSize(new Dimension(250, height)); // Slightly wider input
                        panel.add(component, gbc);
                        // Style JTextArea inside JScrollPane
                        JTextComponent innerComp = (JTextComponent)((JScrollPane) component).getViewport().getView();
                        if (innerComp != null) {
                            innerComp.setBackground(Color.WHITE);
                            innerComp.setForeground(TEXT_COLOR);
                        }
                    } else if (component instanceof JTextComponent) {
                        ((JTextComponent) component).setBackground(Color.WHITE);
                        ((JTextComponent) component).setForeground(TEXT_COLOR);
                        panel.add(component, gbc);
                    } else {
                        // For JLabel (Image Paths)
                        component.setForeground(TEXT_COLOR);
                        panel.add(component, gbc);
                    }
                    
                    gbc.fill = GridBagConstraints.NONE;
                    row.incrementAndGet();
                }
            };
            
            helper.addField("Item Name:", titleField, 25);
            helper.addField("Category:", categoryField, 25);
            helper.addField("Description:", new JScrollPane(descriptionArea), 70);
            helper.addField("Start time:", startTimeField, 25);
            helper.addField("End time:", endTimeField, 25);
            helper.addField("Base price:", basePriceField, 25);

            // Image Display (Simple for now: show file paths)
            List<BackendClient.ImagesDto> images = BackendClient.getImagesByAuction(auctionId);
            String imagePaths = images.stream().map(BackendClient.ImagesDto::filePath).collect(Collectors.joining(", "));
            helper.addField("Image Paths:", new JLabel(imagePaths), 25);

            detailDialog.add(new JScrollPane(panel), BorderLayout.CENTER);

            // --- Action Buttons ---
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
            buttonPanel.setBackground(BACKGROUND_COLOR);
            
            JButton updateButton = new JButton("Update Auction");
            JButton deleteButton = new JButton("Delete Auction");
            JButton endBidButton = new JButton("End Bid");

            // Style buttons
            styleActionButton(updateButton, PRIMARY_COLOR, Color.WHITE);
            styleActionButton(deleteButton, DELETE_COLOR, Color.WHITE);
            styleActionButton(endBidButton, END_BID_COLOR, Color.WHITE);

            updateButton.addActionListener(e -> {
                // Logic for Update Auction
                try {
                    BackendClient.AuctionItemDto updatedDto = new BackendClient.AuctionItemDto(
                        auctionId, titleField.getText(), Double.parseDouble(basePriceField.getText()), 
                        categoryField.getText(), descriptionArea.getText(), auction.status(), 
                        Main.sell_id, auction.winnerId(), startTimeField.getText(), endTimeField.getText());
                    
                    BackendClient.updateAuction(auctionId, updatedDto).ifPresentOrElse(
                        res -> {
                            showMessageDialog(detailDialog, "Auction updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                            loadCurrentBids(); // Refresh the table after update
                        },
                        () -> showMessageDialog(detailDialog, "Failed to update auction.", "Error", JOptionPane.ERROR_MESSAGE)
                    );
                } catch (NumberFormatException ex) {
                    showMessageDialog(detailDialog, "Invalid Base Price.", "Input Error", JOptionPane.ERROR_MESSAGE);
                }
            });

            deleteButton.addActionListener(e -> {
                // Logic for Delete Auction
                int confirm = JOptionPane.showConfirmDialog(detailDialog, "Delete this auction? This action is irreversible.", "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                if (confirm == JOptionPane.YES_OPTION) {
                    if (BackendClient.deleteAuction(auctionId)) {
                        showMessageDialog(mainFrame, "Auction deleted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                        detailDialog.dispose();
                        loadCurrentBids(); // Refresh the table
                    } else {
                        showMessageDialog(detailDialog, "Failed to delete auction.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            });

            endBidButton.addActionListener(e -> {
                // Logic for Close Auction
                int confirm = JOptionPane.showConfirmDialog(detailDialog, "End this auction and determine winner?", "Confirm Close", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    BackendClient.closeAuction(auctionId).ifPresentOrElse(
                        res -> {
                            showMessageDialog(mainFrame, "Auction ID " + auctionId + " closed successfully! The winner has been notified.", "Success", JOptionPane.INFORMATION_MESSAGE);
                            detailDialog.dispose();
                            loadCurrentBids(); // Refresh the table
                        },
                        () -> showMessageDialog(detailDialog, "Failed to close auction. Ensure there is at least one bid.", "Error", JOptionPane.ERROR_MESSAGE)
                    );
                }
            });
            
            buttonPanel.add(updateButton);
            buttonPanel.add(deleteButton);
            buttonPanel.add(endBidButton);
            detailDialog.add(buttonPanel, BorderLayout.SOUTH);

        }, () -> showMessageDialog(mainFrame, "Failed to load auction details.", "Error", JOptionPane.ERROR_MESSAGE));

        detailDialog.setVisible(true);
    }

    // Custom Button Renderer for the JTable (Uses predefined colors)
    class ButtonRenderer extends JButton implements TableCellRenderer {
        public ButtonRenderer() {
            setOpaque(true);
            // Apply color styling
            setBackground(PRIMARY_COLOR);
            setForeground(Color.WHITE);
            setFocusPainted(false);
            setFont(getFont().deriveFont(Font.BOLD, 11f));
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            setText((value == null) ? "" : value.toString());
            
            if (isSelected) {
                // Slightly darker highlight on selection
                setBackground(ACCENT_COLOR.darker()); 
            } else {
                setBackground(PRIMARY_COLOR);
            }
            
            return this;
        }
    }

    // Custom Button Editor for the JTable (Uses predefined colors)
    class ButtonEditor extends DefaultCellEditor {
        private final JButton button;
        private String label;
        private boolean isPushed;
        private final java.util.function.Consumer<Integer> actionHandler;

        public ButtonEditor(JTextField textField, java.util.function.Consumer<Integer> actionHandler) {
            super(textField);
            this.actionHandler = actionHandler;
            button = new JButton();
            button.setOpaque(true);
            // Apply color styling
            button.setBackground(PRIMARY_COLOR);
            button.setForeground(Color.WHITE);
            button.setFocusPainted(false);
            button.setFont(button.getFont().deriveFont(Font.BOLD, 11f));
            
            // This is crucial: stop editing when the button is clicked to trigger getCellEditorValue
            button.addActionListener(e -> fireEditingStopped()); 
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                boolean isSelected, int row, int column) {
            label = (value == null) ? "" : value.toString();
            button.setText(label);
            isPushed = true;
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            if (isPushed) {
                // Perform the action here
                actionHandler.accept(currentBidsTable.getSelectedRow());
            }
            isPushed = false;
            // Return the label text to be stored in the cell model
            return label; 
        }
    }
}