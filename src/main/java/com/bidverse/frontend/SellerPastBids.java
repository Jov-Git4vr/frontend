package com.bidverse.frontend;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.util.List;
import java.util.stream.Collectors;

import static javax.swing.JOptionPane.showMessageDialog;

public class SellerPastBids extends JPanel {
    private final JFrame mainFrame;
    private JTable pastBidsTable;
    private DefaultTableModel tableModel;

    public SellerPastBids(JFrame frame) {
        this.mainFrame = frame;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Back button
        JButton backButton = new JButton("<- Back to Home");
        backButton.addActionListener(e -> Main.switchToPanel(mainFrame, new SellerHome(mainFrame)));
        add(backButton, BorderLayout.NORTH);

        // Table Setup
        String[] columnNames = {"S.No.", "Auction ID", "Item Name", "Final Amount", "Winner", "Details"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                // Only the "Details" column is clickable/editable
                return column == 5; 
            }
        };
        pastBidsTable = new JTable(tableModel);
        pastBidsTable.setRowHeight(30);

        // Custom Renderer/Editor for the Details column
        pastBidsTable.getColumn("Details").setCellRenderer(new ButtonRenderer());
        // Use an anonymous function to handle the button click action
        pastBidsTable.getColumn("Details").setCellEditor(new ButtonEditor(new JTextField(), this::viewWinnerDetails));

        add(new JScrollPane(pastBidsTable), BorderLayout.CENTER);

        loadPastBids();
    }

    private void loadPastBids() {
        tableModel.setRowCount(0); // Clear existing data
        
        List<BackendClient.AuctionItemDto> allAuctions = BackendClient.getAuctionsBySellerEmail(Main.email);
        
        // Filter by status = 'CLOSE'
        List<BackendClient.AuctionItemDto> closedAuctions = allAuctions.stream()
            .filter(a -> "CLOSE".equalsIgnoreCase(a.status()))
            .collect(Collectors.toList());

        for (int i = 0; i < closedAuctions.size(); i++) {
            BackendClient.AuctionItemDto auction = closedAuctions.get(i);
            Long auctionId = auction.auctionId();

            // Fetch Final Amount and Winner Name (use AtomicReference for mutation in lambdas)
            java.util.concurrent.atomic.AtomicReference<String> finalAmountRef = new java.util.concurrent.atomic.AtomicReference<>("N/A");
            java.util.concurrent.atomic.AtomicReference<String> winnerNameRef = new java.util.concurrent.atomic.AtomicReference<>("N/A");

            BackendClient.getHighestBid(auctionId).ifPresent(bid -> {
                finalAmountRef.set(String.format("$%.2f", bid.bidAmount()));

                // Flow: auction_id -> getHighestBid() -> bid_id -> getBidderbyId() -> bidder_name
                if (bid.bidderId() != null) {
                    BackendClient.getBidderById(bid.bidderId()).ifPresent(bidder -> {
                        winnerNameRef.set(bidder.bidderName());
                    });
                }
            });

            // Columns: S.No., Auction ID, Item Name, Final Amount, Winner, Details
            tableModel.addRow(new Object[]{
                i + 1, 
                auctionId, 
                auction.title(),
                finalAmountRef.get(), 
                winnerNameRef.get(), 
                "View Details"
            });
        }

        if (closedAuctions.isEmpty()) {
            tableModel.addRow(new Object[]{"-", "-", "No Past Auctions Found.", "-", "-", "-"});
        }
    }

    private void viewWinnerDetails(int row) {
        if (row < 0 || row >= tableModel.getRowCount()) return;
        Long auctionId = (Long) tableModel.getValueAt(row, 1);
        
        // Final Amount for the winner is the Highest Bid amount
        BackendClient.getHighestBid(auctionId).ifPresentOrElse(bid -> {
            if (bid.bidderId() == null) {
                showMessageDialog(mainFrame, "No winner found for this auction (no bids placed).", "No Winner", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            
            // Flow: auction_id -> getHighestBid() -> bid_id -> getBidderbyId()
            BackendClient.getBidderById(bid.bidderId()).ifPresentOrElse(bidder -> {
                
                String details = String.format(
                    "Winner Name: %s\nEmail: %s\nPhone No: %s\nAddress: %s\n\nFinal Bid Amount: $%.2f",
                    bidder.bidderName(),
                    bidder.bidderEmail(),
                    bidder.phno(),
                    bidder.address(),
                    bid.bidAmount()
                );

                JTextArea textArea = new JTextArea(details);
                textArea.setEditable(false);
                JScrollPane scrollPane = new JScrollPane(textArea);
                scrollPane.setPreferredSize(new Dimension(300, 200));

                showMessageDialog(mainFrame, scrollPane, "Winner Details - " + tableModel.getValueAt(row, 2), JOptionPane.INFORMATION_MESSAGE);

            }, () -> showMessageDialog(mainFrame, "Failed to load winner profile.", "Error", JOptionPane.ERROR_MESSAGE));

        }, () -> showMessageDialog(mainFrame, "Could not find final bid details.", "Error", JOptionPane.ERROR_MESSAGE));
    }

    // Custom Button Renderer for the JTable (Copied from SellerCurrentBids)
    class ButtonRenderer extends JButton implements TableCellRenderer {
        public ButtonRenderer() { setOpaque(true); }
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            setText((value == null) ? "" : value.toString());
            return this;
        }
    }

    // Custom Button Editor for the JTable (Copied from SellerCurrentBids)
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
                actionHandler.accept(pastBidsTable.getSelectedRow());
            }
            isPushed = false;
            return label;
        }
    }
}