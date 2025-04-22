package com.mycompany.northwind;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class EmployeesTab extends JPanel {
    private DefaultTableModel model;
    private JTable table;
    private Connection conn;
    
    public EmployeesTab(Connection conn) {
        this.conn = conn;
        setLayout(new BorderLayout());
        
        // Create components
        String[] columns = {
            "First Name", "Last Name", "Address", "Address Line 2", "City",
            "Region", "Postal Code", "Phone", "Office", "Active"
        };
        model = new DefaultTableModel(columns, 0);
        table = new JTable(model);
        
        // Filter components
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel filterLabel = new JLabel("Filter:");
        JTextField filterField = new JTextField(20);
        JButton filterButton = new JButton("Apply Filter");
        
        filterPanel.add(filterLabel);
        filterPanel.add(filterField);
        filterPanel.add(filterButton);
        
        // Add components to panel
        add(filterPanel, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        
        // Load initial data
        loadEmployees("");
        
        // Add filter button action
        filterButton.addActionListener(e -> {
            loadEmployees(filterField.getText());
        });
        
        // Add filter on Enter key
        filterField.addActionListener(e -> {
            loadEmployees(filterField.getText());
        });
    }
    
    private void loadEmployees(String filter) {
        try {
            model.setRowCount(0); // Clear existing data
            
            String query = "SELECT first_name, last_name, address, address2, city, " +
                          "region, postal_code, phone, office, active FROM employees " +
                          "WHERE first_name LIKE ? OR last_name LIKE ? OR city LIKE ?";
            
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                String filterPattern = "%" + filter + "%";
                stmt.setString(1, filterPattern);
                stmt.setString(2, filterPattern);
                stmt.setString(3, filterPattern);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        Object[] row = {
                            rs.getString("first_name"),
                            rs.getString("last_name"),
                            rs.getString("address"),
                            rs.getString("address2"),
                            rs.getString("city"),
                            rs.getString("region"),
                            rs.getString("postal_code"),
                            rs.getString("phone"),
                            rs.getString("office"),
                            rs.getBoolean("active") ? "Yes" : "No"
                        };
                        model.addRow(row);
                    }
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database error: " + e.getMessage());
        }
    }
}