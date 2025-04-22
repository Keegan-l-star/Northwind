package com.mycompany.northwind;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.Date;

public class NotificationsTab extends JPanel {
    private final DefaultTableModel activeModel;
    private final DefaultTableModel inactiveModel;
    private final JTable activeTable;
    private final JTable inactiveTable;
    private final Connection conn;
    
    public NotificationsTab(Connection conn) {
        this.conn = conn;
        setLayout(new BorderLayout());
        
        // Create tabbed pane for active/inactive clients
        JTabbedPane clientTabs = new JTabbedPane();
        
        // Panel for active clients
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
        
        // Panel for inactive clients
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
        
        // Add client management buttons
        JPanel buttonPanel = new JPanel();
        JButton addButton = new JButton("Add Client");
        JButton updateButton = new JButton("Update Client");
        JButton deleteButton = new JButton("Delete Client");
        
        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);
        
        // Add components to main panel
        clientTabs.addTab("Active Clients", activePanel);
        clientTabs.addTab("Inactive Clients", inactivePanel);
        
        add(buttonPanel, BorderLayout.NORTH);
        add(clientTabs, BorderLayout.CENTER);
        
        // Load initial data
        loadActiveClients();
        loadInactiveClients();
        
        // Button actions
        addButton.addActionListener(e -> showClientDialog(null));
        updateButton.addActionListener(e -> {
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
    }
    
    private void loadActiveClients() {
        activeModel.setRowCount(0);
        
        String query = "SELECT customer_id, company_name, contact_name, phone, email, " +
                      "(SELECT MAX(order_date) FROM orders WHERE customer_id = c.customer_id) as last_order_date " +
                      "FROM customers c " +
                      "WHERE EXISTS (SELECT 1 FROM orders WHERE customer_id = c.customer_id " +
                      "AND order_date >= DATE_SUB(CURDATE(), INTERVAL 1 YEAR))";
        
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                activeModel.addRow(new Object[]{
                    rs.getString("customer_id"),
                    rs.getString("company_name"),
                    rs.getString("contact_name"),
                    rs.getString("phone"),
                    rs.getString("email"),
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
        
        String query = "SELECT customer_id, company_name, contact_name, phone, email, " +
                      "(SELECT MAX(order_date) FROM orders WHERE customer_id = c.customer_id) as last_order_date " +
                      "FROM customers c " +
                      "WHERE NOT EXISTS (SELECT 1 FROM orders WHERE customer_id = c.customer_id " +
                      "AND order_date >= DATE_SUB(CURDATE(), INTERVAL 1 YEAR)) OR " +
                      "NOT EXISTS (SELECT 1 FROM orders WHERE customer_id = c.customer_id)";
        
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                inactiveModel.addRow(new Object[]{
                    rs.getString("customer_id"),
                    rs.getString("company_name"),
                    rs.getString("contact_name"),
                    rs.getString("phone"),
                    rs.getString("email"),
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
    
    private void showClientDialog(String clientId) {
        JDialog dialog = new JDialog();
        dialog.setTitle(clientId == null ? "Add New Client" : "Update Client");
        dialog.setSize(400, 300);
        dialog.setModal(true);
        dialog.setLayout(new GridLayout(0, 2, 5, 5));
        
        // Create form components
        JTextField companyField = new JTextField();
        JTextField contactField = new JTextField();
        JTextField phoneField = new JTextField();
        JTextField emailField = new JTextField();
        JButton saveButton = new JButton("Save");
        
        // If updating, load existing data
        if (clientId != null) {
            String query = "SELECT company_name, contact_name, phone, email FROM customers WHERE customer_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, clientId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        companyField.setText(rs.getString("company_name"));
                        contactField.setText(rs.getString("contact_name"));
                        phoneField.setText(rs.getString("phone"));
                        emailField.setText(rs.getString("email"));
                    }
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(dialog,
                    "Error loading client data: " + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
        
        // Add components to dialog
        dialog.add(new JLabel("Company Name:"));
        dialog.add(companyField);
        dialog.add(new JLabel("Contact Name:"));
        dialog.add(contactField);
        dialog.add(new JLabel("Phone:"));
        dialog.add(phoneField);
        dialog.add(new JLabel("Email:"));
        dialog.add(emailField);
        dialog.add(new JLabel(""));
        dialog.add(saveButton);
        
        // Save button action
        saveButton.addActionListener(e -> {
            try {
                if (clientId == null) {
                    // Add new client
                    String query = "INSERT INTO customers (company_name, contact_name, phone, email) VALUES (?, ?, ?, ?)";
                    try (PreparedStatement stmt = conn.prepareStatement(query)) {
                        stmt.setString(1, companyField.getText().trim());
                        stmt.setString(2, contactField.getText().trim());
                        stmt.setString(3, phoneField.getText().trim());
                        stmt.setString(4, emailField.getText().trim());
                        stmt.executeUpdate();
                    }
                    JOptionPane.showMessageDialog(dialog, "Client added successfully!");
                } else {
                    // Update existing client
                    String query = "UPDATE customers SET company_name = ?, contact_name = ?, phone = ?, email = ? " +
                                  "WHERE customer_id = ?";
                    try (PreparedStatement stmt = conn.prepareStatement(query)) {
                        stmt.setString(1, companyField.getText().trim());
                        stmt.setString(2, contactField.getText().trim());
                        stmt.setString(3, phoneField.getText().trim());
                        stmt.setString(4, emailField.getText().trim());
                        stmt.setString(5, clientId);
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
    
    private void deleteClient(String clientId) {
        int confirm = JOptionPane.showConfirmDialog(this, 
            "Are you sure you want to delete this client?", 
            "Confirm Delete", 
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            String query = "DELETE FROM customers WHERE customer_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, clientId);
                int affectedRows = stmt.executeUpdate();
                
                if (affectedRows > 0) {
                    JOptionPane.showMessageDialog(this, 
                        "Client deleted successfully!",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);
                    loadActiveClients();
                    loadInactiveClients();
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this,
                    "Error deleting client: " + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}