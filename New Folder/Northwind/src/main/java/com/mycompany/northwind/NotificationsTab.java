package com.mycompany.northwind;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.*;
import java.util.Date;

public class NotificationsTab extends JPanel {
    private final DefaultTableModel activeModel;
    private final DefaultTableModel inactiveModel;
    private final JTable activeTable;
    private final JTable inactiveTable;
    private final Connection conn;
    private JTextField searchField;
    private String ClieantId;
    public NotificationsTab(Connection conn) {
        this.conn = conn;
        setLayout(new BorderLayout());
        
        JTabbedPane clientTabs = new JTabbedPane();
        
        JPanel activePanel = new JPanel(new BorderLayout());
        String[] activeColumns = {"ID", "Company", "Contact", "Phone", "Email", "Last Order Date"};
        activeModel = new DefaultTableModel(activeColumns, 0) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return columnIndex == 5 ? Date.class : String.class;
            }
        };
        activeTable = new JTable(activeModel);
        activeTable.setAutoCreateRowSorter(true);
        activePanel.add(new JScrollPane(activeTable), BorderLayout.CENTER);
        
        JPanel inactivePanel = new JPanel(new BorderLayout());
        String[] inactiveColumns = {"ID", "Company", "Contact", "Phone", "Email", "Last Order Date"};
        inactiveModel = new DefaultTableModel(inactiveColumns, 0) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return columnIndex == 5 ? Date.class : String.class;
            }
        };
        inactiveTable = new JTable(inactiveModel);
        inactiveTable.setAutoCreateRowSorter(true);
        inactivePanel.add(new JScrollPane(inactiveTable), BorderLayout.CENTER);
        
        JPanel topPanel = new JPanel(new BorderLayout());
        
        JPanel buttonPanel = new JPanel();
        JButton addButton = new JButton("Add Client");
        JButton updateButton = new JButton("Update Client");
        JButton deleteButton = new JButton("Delete Client");
        JButton notifyButton = new JButton("Send Notification");
        
        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(notifyButton);
        
        JPanel searchPanel = new JPanel();
        searchField = new JTextField(20);
        JButton searchButton = new JButton("Search");
        
        searchPanel.add(new JLabel("Search:"));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        
        topPanel.add(buttonPanel, BorderLayout.WEST);
        topPanel.add(searchPanel, BorderLayout.EAST);
        
        clientTabs.addTab("Active Clients", activePanel);
        clientTabs.addTab("Inactive Clients", inactivePanel);
        
        add(topPanel, BorderLayout.NORTH);
        add(clientTabs, BorderLayout.CENTER);
        
        loadActiveClients();
        loadInactiveClients();
        
        addButton.addActionListener(e -> showClientDialog(null));
        updateButton.addActionListener(e -> {
            activeTable.requestFocus();
            int selectedRow = activeTable.getSelectedRow();
            if (selectedRow >= 0) {
                String clientId = (String) activeModel.getValueAt(selectedRow, 0);
                showClientDialog(clientId);
            } else {
                JOptionPane.showMessageDialog(this, 
                    "Please select a client to update",
                    "Selection Required",
                    JOptionPane.WARNING_MESSAGE);
            }
        });
        deleteButton.addActionListener(e -> {
            activeTable.requestFocus();
            int selectedRow = activeTable.getSelectedRow();
            if (selectedRow >= 0) {
                String clientId = (String) activeModel.getValueAt(selectedRow, 0);
                deleteClient(clientId);
            } else {
                JOptionPane.showMessageDialog(this,
                    "Please select a client to delete",
                    "Selection Required",
                    JOptionPane.WARNING_MESSAGE);
            }
        });
        notifyButton.addActionListener(e -> showNotificationDialog());
        searchButton.addActionListener(e -> searchClients());
    }
    
    private void loadActiveClients() {
        activeModel.setRowCount(0);
        
        String query = "SELECT c.id, c.company, CONCAT(c.first_name, ' ', c.last_name) as contact, " +
                      "c.business_phone, c.email_address, " +
                      "MAX(o.order_date) as last_order_date " +
                      "FROM customers c " +
                      "LEFT JOIN orders o ON c.id = o.customer_id " +
                      "WHERE o.order_date IS NOT NULL " +
                      "AND o.order_date >= DATE_SUB(CURDATE(), INTERVAL 1 YEAR) " +
                      "GROUP BY c.id, c.company, contact, c.business_phone, c.email_address";
        
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                activeModel.addRow(new Object[]{
                    rs.getString("id"),
                    rs.getString("company"),
                    rs.getString("contact"),
                    rs.getString("business_phone"),
                    rs.getString("email_address"),
                    rs.getDate("last_order_date")
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                "Error loading active clients: " + e.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void loadInactiveClients() {
        inactiveModel.setRowCount(0);
        
        String query = "SELECT c.id, c.company, CONCAT(c.first_name, ' ', c.last_name) as contact, " +
                      "c.business_phone, c.email_address, " +
                      "MAX(o.order_date) as last_order_date " +
                      "FROM customers c " +
                      "LEFT JOIN orders o ON c.id = o.customer_id " +
                      "WHERE o.order_date IS NULL " +
                      "OR o.order_date < DATE_SUB(CURDATE(), INTERVAL 1 YEAR) " +
                      "GROUP BY c.id, c.company, contact, c.business_phone, c.email_address";
        
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                inactiveModel.addRow(new Object[]{
                    rs.getString("id"),
                    rs.getString("company"),
                    rs.getString("contact"),
                    rs.getString("business_phone"),
                    rs.getString("email_address"),
                    rs.getDate("last_order_date")
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                "Error loading inactive clients: " + e.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void searchClients() {
        String searchTerm = searchField.getText().trim();
        if (searchTerm.isEmpty()) {
            loadActiveClients();
            loadInactiveClients();
            return;
        }
        
        activeModel.setRowCount(0);
        inactiveModel.setRowCount(0);
        
        String query = "SELECT c.id, c.company, CONCAT(c.first_name, ' ', c.last_name) as contact, " +
                      "c.business_phone, c.email_address, " +
                      "MAX(o.order_date) as last_order_date, " +
                      "CASE WHEN MAX(o.order_date) >= DATE_SUB(CURDATE(), INTERVAL 1 YEAR) " +
                      "THEN 'active' ELSE 'inactive' END as status " +
                      "FROM customers c " +
                      "LEFT JOIN orders o ON c.id = o.customer_id " +
                      "WHERE c.company LIKE ? OR c.first_name LIKE ? OR c.last_name LIKE ? " +
                      "OR c.email_address LIKE ? " +
                      "GROUP BY c.id, c.company, contact, c.business_phone, c.email_address";
        
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            String likeTerm = "%" + searchTerm + "%";
            stmt.setString(1, likeTerm);
            stmt.setString(2, likeTerm);
            stmt.setString(3, likeTerm);
            stmt.setString(4, likeTerm);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Object[] row = {
                        rs.getString("id"),
                        rs.getString("company"),
                        rs.getString("contact"),
                        rs.getString("business_phone"),
                        rs.getString("email_address"),
                        rs.getDate("last_order_date")
                    };
                    
                    if ("active".equals(rs.getString("status"))) {
                        activeModel.addRow(row);
                    } else {
                        inactiveModel.addRow(row);
                    }
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                "Error searching clients: " + e.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void showClientDialog(String clientId) {
        JDialog dialog = new JDialog();
        dialog.setTitle(clientId == null ? "Add New Client" : "Update Client");
        dialog.setSize(400, 400);
        dialog.setModal(true);
        dialog.setLayout(new GridLayout(0, 2, 5, 5));
        
        JTextField companyField = new JTextField();
        JTextField firstNameField = new JTextField();
        JTextField lastNameField = new JTextField();
        JTextField phoneField = new JTextField();
        JTextField emailField = new JTextField();
        JButton saveButton = new JButton("Save");
        
        if (clientId != null) {
            String query = "SELECT company, first_name, last_name, business_phone, email_address " +
                          "FROM customers WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setInt(1, Integer.parseInt(clientId));
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        companyField.setText(rs.getString("company"));
                        firstNameField.setText(rs.getString("first_name"));
                        lastNameField.setText(rs.getString("last_name"));
                        phoneField.setText(rs.getString("business_phone"));
                        emailField.setText(rs.getString("email_address"));
                    }
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(dialog,
                    "Error loading client data: " + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
        
        dialog.add(new JLabel("Company:"));
        dialog.add(companyField);
        dialog.add(new JLabel("First Name:"));
        dialog.add(firstNameField);
        dialog.add(new JLabel("Last Name:"));
        dialog.add(lastNameField);
        dialog.add(new JLabel("Phone:"));
        dialog.add(phoneField);
        dialog.add(new JLabel("Email:"));
        dialog.add(emailField);
        dialog.add(new JLabel(""));
        dialog.add(saveButton);
        
        saveButton.addActionListener(e -> {
            try {
                if (clientId == null) {
                    String query = "INSERT INTO customers (company, first_name, last_name, " +
                                  "business_phone, email_address) VALUES (?, ?, ?, ?, ?)";
                    try (PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
                        stmt.setString(1, companyField.getText().trim());
                        stmt.setString(2, firstNameField.getText().trim());
                        stmt.setString(3, lastNameField.getText().trim());
                        stmt.setString(4, phoneField.getText().trim());
                        stmt.setString(5, emailField.getText().trim());
                        stmt.executeUpdate();
                    }
                    JOptionPane.showMessageDialog(dialog, "Client added successfully!");
                } else {
                    String query = "UPDATE customers SET company = ?, first_name = ?, last_name = ?, " +
                                  "business_phone = ?, email_address = ? WHERE id = ?";
                    try (PreparedStatement stmt = conn.prepareStatement(query)) {
                        stmt.setString(1, companyField.getText().trim());
                        stmt.setString(2, firstNameField.getText().trim());
                        stmt.setString(3, lastNameField.getText().trim());
                        stmt.setString(4, phoneField.getText().trim());
                        stmt.setString(5, emailField.getText().trim());
                        stmt.setInt(6, Integer.parseInt(clientId));
                        stmt.executeUpdate();
                    }
                    JOptionPane.showMessageDialog(dialog, "Client updated successfully!");
                }
                
                dialog.dispose();
                loadActiveClients();
                loadInactiveClients();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(dialog,
                    "Error saving client: " + ex.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        });
        
        dialog.setVisible(true);
    }
    
    private void showNotificationDialog() {
        JDialog dialog = new JDialog();
        dialog.setTitle("Send Notification");
        dialog.setSize(500, 400);
        dialog.setModal(true);
        dialog.setLayout(new BorderLayout());
        
        JComboBox<String> notificationType = new JComboBox<>(new String[]{"Special Offer", "New Product"});
        JTextArea messageArea = new JTextArea(5, 30);
        JCheckBox activeClientsCheck = new JCheckBox("Active Clients", true);
        JCheckBox inactiveClientsCheck = new JCheckBox("Inactive Clients", false);
        JButton sendButton = new JButton("Send Notification");
        
        JPanel detailsPanel = new JPanel(new GridLayout(0, 2, 5, 5));
        detailsPanel.add(new JLabel("Notification Type:"));
        detailsPanel.add(notificationType);
        detailsPanel.add(new JLabel("Message:"));
        detailsPanel.add(new JScrollPane(messageArea));
        detailsPanel.add(new JLabel("Recipients:"));
        
        JPanel recipientsPanel = new JPanel(new GridLayout(2, 1));
        recipientsPanel.add(activeClientsCheck);
        recipientsPanel.add(inactiveClientsCheck);
        detailsPanel.add(recipientsPanel);
        
        dialog.add(detailsPanel, BorderLayout.CENTER);
        dialog.add(sendButton, BorderLayout.SOUTH);
        
        sendButton.addActionListener(e -> {
            String type = (String) notificationType.getSelectedItem();
            String message = messageArea.getText().trim();
            boolean sendToActive = activeClientsCheck.isSelected();
            boolean sendToInactive = inactiveClientsCheck.isSelected();
            
            if (message.isEmpty()) {
                JOptionPane.showMessageDialog(dialog,
                    "Please enter a message",
                    "Input Required",
                    JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            if (!sendToActive && !sendToInactive) {
                JOptionPane.showMessageDialog(dialog,
                    "Please select at least one recipient group",
                    "Selection Required",
                    JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            StringBuilder recipients = new StringBuilder();
            if (sendToActive) recipients.append("Active Clients");
            if (sendToInactive) {
                if (sendToActive) recipients.append(" and ");
                recipients.append("Inactive Clients");
            }
            
            JOptionPane.showMessageDialog(dialog,
                "Notification sent successfully to " + recipients + "!",
                "Success",
                JOptionPane.INFORMATION_MESSAGE);
            
            dialog.dispose();
        });
        
        dialog.setVisible(true);
    }
    
    private void deleteClient(String clientId) {
        int confirm = JOptionPane.showConfirmDialog(this, 
            "Are you sure you want to delete this client? Client: ", 
            "Confirm Delete", 
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);

        System.out.print("User clicked :"+ confirm);
        
        if (confirm == JOptionPane.YES_OPTION) {
            System.out.println("Pressed yes so delete");
            try {
                int id = Integer.parseInt(clientId);

                String query = "DELETE FROM customers WHERE id = ?";
                try (PreparedStatement stmt = conn.prepareStatement(query)) {
                    stmt.setInt(1, id);
                    int affectedRows = stmt.executeUpdate();

                    if (affectedRows > 0) {
                        JOptionPane.showMessageDialog(null, 
                            "Client deleted successfully!", 
                            "Success", 
                            JOptionPane.INFORMATION_MESSAGE);

                        // Reload client lists (assuming methods exist)
                        loadActiveClients();
                        loadInactiveClients();
                    } else {
                        JOptionPane.showMessageDialog(null, 
                            "Client not found!", 
                            "Error", 
                            JOptionPane.ERROR_MESSAGE);
                    }
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(null, 
                    "Invalid Client ID format!", 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
                System.out.println("Error parsing Client ID: " + e.getMessage());
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(null, 
                    "Error deleting client: " + e.getMessage(), 
                    "Database Error", 
                    JOptionPane.ERROR_MESSAGE);
                System.out.println("SQL Error: " + e.getMessage());
            }
        }else {
            System.out.println("User clicked No or closed the dialog.");
        }
    }
}