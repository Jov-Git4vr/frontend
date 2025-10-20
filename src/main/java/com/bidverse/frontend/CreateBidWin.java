package com.bidverse.frontend;

import javax.swing.*;
import java.awt.*;
// removed unused java.io.File import

import static javax.swing.JOptionPane.showMessageDialog;

public class CreateBidWin extends JDialog {
    // Define the primary accent color for the application
    private static final Color PRIMARY_ACCENT = new Color(30, 144, 255); // Deep Sky Blue

    // Text fields for input
    private final JTextField titleField = new JTextField(25);
    private final JTextField categoryField = new JTextField(25);
    private final JTextArea descriptionArea = new JTextArea(3, 25);
    private final JTextField startTimeField = new JTextField(15); 
    private final JTextField endTimeField = new JTextField(15);
    private final JTextField basePriceField = new JTextField(10);
    private final JTextField imagePathField = new JTextField(20);
    private final JFrame mainFrame;

    public CreateBidWin(JFrame parent) {
        // Setup Dialog properties
        super(parent, "Create New Auction", true); // Modal Dialog
        this.mainFrame = parent;
        
        // Set a light background for a clean look
        getContentPane().setBackground(new Color(248, 248, 248)); 
        setLayout(new BorderLayout(15, 15)); 
        setSize(500, 550);
        setLocationRelativeTo(parent);
        
        // Use a wrapper panel for the form area
        JPanel formWrapperPanel = new JPanel(new GridBagLayout());
        formWrapperPanel.setBackground(Color.WHITE); // White background for the form area
        formWrapperPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 0, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 5, 10, 5); 
        gbc.anchor = GridBagConstraints.WEST;

        // --- Title ---
        JLabel formTitle = new JLabel("Create New Auction Item");
        formTitle.setFont(new Font("Arial", Font.BOLD, 20));
        formTitle.setForeground(PRIMARY_ACCENT.darker()); // Darker shade of accent for title
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        formWrapperPanel.add(formTitle, gbc);
        gbc.gridwidth = 1; 
        gbc.anchor = GridBagConstraints.WEST;

        // Helper for adding components (no change to logic, just structure)
        var helper = new Object() {
            java.util.concurrent.atomic.AtomicInteger row = new java.util.concurrent.atomic.AtomicInteger(1);
            void addField(String labelText, JComponent component, int span) {
                // Label
                gbc.gridx = 0;
                gbc.gridy = row.get();
                formWrapperPanel.add(new JLabel(labelText), gbc);
                
                // Input Component
                gbc.gridx = 1;
                gbc.gridy = row.get();
                gbc.weightx = 1.0;
                gbc.fill = GridBagConstraints.HORIZONTAL;
                gbc.gridwidth = span;
                formWrapperPanel.add(component, gbc);
                
                gbc.weightx = 0;
                gbc.fill = GridBagConstraints.NONE;
                gbc.gridwidth = 1;
                row.incrementAndGet();
            }
        };

        // --- Form Fields (no color change needed for fields themselves) ---
        helper.addField("Title:", titleField, 1);
        helper.addField("Category:", categoryField, 1);
        
        JScrollPane descriptionScrollPane = new JScrollPane(descriptionArea);
        descriptionScrollPane.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY)); // Add a subtle border
        helper.addField("Description:", descriptionScrollPane, 1);
        
        // Date/Time fields in one row
        JPanel timePanel = new JPanel(new GridLayout(1, 4, 10, 0));
        timePanel.setBackground(Color.WHITE);
        timePanel.add(new JLabel("Start Time (HH:mm:ss):"));
        timePanel.add(startTimeField);
        timePanel.add(new JLabel("End Time (HH:mm:ss):"));
        timePanel.add(endTimeField);
        
    gbc.gridx = 0;
    gbc.gridy = helper.row.get();
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        formWrapperPanel.add(timePanel, gbc);
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
    helper.row.incrementAndGet();

        helper.addField("Base Price:", basePriceField, 1);

        // Image Field with Browse Button
        JPanel imagePanel = new JPanel(new BorderLayout(5, 0));
        imagePanel.add(imagePathField, BorderLayout.CENTER);
        
        JButton browseButton = new JButton("Browse");
        browseButton.setBackground(new Color(220, 220, 220)); // Light Gray background for secondary button
        browseButton.addActionListener(e -> browseImageFile());
        imagePanel.add(browseButton, BorderLayout.EAST);
        imagePanel.setBackground(Color.WHITE);
        helper.addField("Image:", imagePanel, 1);

        add(formWrapperPanel, BorderLayout.CENTER);

        // --- Button Panel (Bottom) ---
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 50, 15));
        buttonPanel.setBackground(getContentPane().getBackground());

        // Primary Button: Deep Sky Blue
        JButton createButton = new JButton("Create Bid");
        createButton.setFont(new Font("Arial", Font.BOLD, 14));
        createButton.setBackground(PRIMARY_ACCENT); 
        createButton.setForeground(Color.WHITE);
        createButton.setOpaque(true);
        createButton.setBorderPainted(false);
        createButton.addActionListener(e -> createAuction());

        // Secondary Button: Default or Neutral Gray
        JButton cancelButton = new JButton("Cancel");
        cancelButton.setBackground(new Color(200, 200, 200)); // Lighter Gray
        cancelButton.setOpaque(true);
        cancelButton.setBorderPainted(false);
        cancelButton.addActionListener(e -> dispose());
        
        buttonPanel.add(createButton);
        buttonPanel.add(cancelButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void browseImageFile() {
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            imagePathField.setText(fileChooser.getSelectedFile().getAbsolutePath());
        }
    }

    private void createAuction() {
        // ... (rest of the createAuction logic remains the same)
        try {
            String title = titleField.getText();
            String category = categoryField.getText();
            String description = descriptionArea.getText();
            Double basePrice = Double.parseDouble(basePriceField.getText());
            String startTime = startTimeField.getText();
            String endTime = endTimeField.getText();
            String imagePath = imagePathField.getText();

            // Minimal field validation
            if (title.isEmpty() || category.isEmpty() || description.isEmpty() || 
                startTime.isEmpty() || endTime.isEmpty()) {
                showMessageDialog(this, "Please fill in all text fields.", "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            BackendClient.AuctionItemDto newAuctionDto = new BackendClient.AuctionItemDto(
                    null, title, basePrice, category, description, 
                    "OPEN", Main.sell_id, null, startTime, endTime);

            // 2. Create Auction
            BackendClient.createAuction(newAuctionDto).ifPresentOrElse(savedAuction -> {
                Long newAuctionId = savedAuction.auctionId();
                if (newAuctionId != null) {
                    // 3. Upload Image
                    if (imagePath != null && !imagePath.trim().isEmpty()) {
                        if (BackendClient.uploadImage(imagePath, newAuctionId)) {
                            showMessageDialog(mainFrame, "Auction Created and Image Uploaded Successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                        } else {
                            showMessageDialog(mainFrame, "Auction Created, but Image Upload Failed. Check console for details.", "Warning", JOptionPane.WARNING_MESSAGE);
                        }
                    } else {
                        showMessageDialog(mainFrame, "Auction Created Successfully (No Image Provided).", "Success", JOptionPane.INFORMATION_MESSAGE);
                    }
                    dispose();
                } else {
                    showMessageDialog(mainFrame, "Failed to create auction: Server returned null ID.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }, () -> showMessageDialog(mainFrame, "Failed to create auction. Check server status/logs.", "Error", JOptionPane.ERROR_MESSAGE));
        } catch (NumberFormatException ex) {
            showMessageDialog(this, "Invalid Base Price format. Please use numbers.", "Input Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            showMessageDialog(this, "An unexpected error occurred: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }
}