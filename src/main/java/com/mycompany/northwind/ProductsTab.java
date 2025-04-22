package com.mycompany.northwind;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class ProductsTab extends JPanel {
    private final DefaultTableModel model;
    private final JTable table;
    private final Connection conn;
    private final Map<Integer, String> suppliers = new HashMap<>();
    private final Map<Integer, String> categories = new HashMap<>();
    
    public ProductsTab(Connection conn) {
        this.conn = conn;
        setLayout(new BorderLayout());
        
        // Create components
        String[] columns = {"ID", "Product Name", "Supplier", "Category", "Quantity Per Unit", "Unit Price"};
        model = new DefaultTableModel(columns, 0) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return columnIndex == 0 ? Integer.class : String.class;
            }
        };
        table = new JTable(model);
        table.setAutoCreateRowSorter(true);
        
        // Button to add new product
        JButton addButton = new JButton("Add New Product");
        addButton.addActionListener(e -> showAddProductDialog());
        
        // Add components to panel
        add(addButton, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        
        // Load initial data
        loadSuppliersAndCategories();
        loadProducts();
    }
    
    private void loadSuppliersAndCategories() {
        // Load suppliers
        String supplierQuery = "SELECT supplier_id, company_name FROM suppliers";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(supplierQuery)) {
            
            while (rs.next()) {
                suppliers.put(rs.getInt("supplier_id"), rs.getString("company_name"));
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading suppliers: " + e.getMessage());
        }
        
        // Load categories
        String categoryQuery = "SELECT category_id, category_name FROM categories";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(categoryQuery)) {
            
            while (rs.next()) {
                categories.put(rs.getInt("category_id"), rs.getString("category_name"));
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading categories: " + e.getMessage());
        }
    }
    
    private void loadProducts() {
        model.setRowCount(0); // Clear existing data
        
        String query = "SELECT p.product_id, p.product_name, s.company_name, c.category_name, " +
                      "p.quantity_per_unit, p.unit_price " +
                      "FROM products p " +
                      "JOIN suppliers s ON p.supplier_id = s.supplier_id " +
                      "JOIN categories c ON p.category_id = c.category_id";
        
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                Object[] row = {
                    rs.getInt("product_id"),
                    rs.getString("product_name"),
                    rs.getString("company_name"),
                    rs.getString("category_name"),
                    rs.getString("quantity_per_unit"),
                    rs.getBigDecimal("unit_price").toString()
                };
                model.addRow(row);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database error: " + e.getMessage());
        }
    }
    
    private void showAddProductDialog() {
        JDialog dialog = new JDialog();
        dialog.setTitle("Add New Product");
        dialog.setSize(400, 300);
        dialog.setModal(true);
        dialog.setLayout(new GridLayout(0, 2, 5, 5));
        
        // Create form components
        JTextField nameField = new JTextField();
        JComboBox<String> supplierCombo = new JComboBox<>(suppliers.values().toArray(new String[0]));
        JComboBox<String> categoryCombo = new JComboBox<>(categories.values().toArray(new String[0]));
        JTextField quantityField = new JTextField();
        JTextField priceField = new JTextField();
        JButton saveButton = new JButton("Save");
        
        // Add components to dialog
        dialog.add(new JLabel("Product Name:"));
        dialog.add(nameField);
        dialog.add(new JLabel("Supplier:"));
        dialog.add(supplierCombo);
        dialog.add(new JLabel("Category:"));
        dialog.add(categoryCombo);
        dialog.add(new JLabel("Quantity Per Unit:"));
        dialog.add(quantityField);
        dialog.add(new JLabel("Unit Price:"));
        dialog.add(priceField);
        dialog.add(new JLabel(""));
        dialog.add(saveButton);
        
        // Save button action
        saveButton.addActionListener(e -> {
            try {
                // Validate inputs
                if (nameField.getText().trim().isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "Product name is required");
                    return;
                }
                
                if (priceField.getText().trim().isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "Unit price is required");
                    return;
                }
                
                // Get selected supplier and category IDs
                int supplierId = suppliers.entrySet().stream()
                    .filter(entry -> entry.getValue().equals(supplierCombo.getSelectedItem()))
                    .map(Map.Entry::getKey)
                    .findFirst().orElse(0);
                
                int categoryId = categories.entrySet().stream()
                    .filter(entry -> entry.getValue().equals(categoryCombo.getSelectedItem()))
                    .map(Map.Entry::getKey)
                    .findFirst().orElse(0);
                
                // Insert new product
                String query = "INSERT INTO products (product_name, supplier_id, category_id, " +
                              "quantity_per_unit, unit_price) VALUES (?, ?, ?, ?, ?)";
                
                try (PreparedStatement stmt = conn.prepareStatement(query)) {
                    stmt.setString(1, nameField.getText().trim());
                    stmt.setInt(2, supplierId);
                    stmt.setInt(3, categoryId);
                    stmt.setString(4, quantityField.getText().trim());
                    stmt.setBigDecimal(5, new java.math.BigDecimal(priceField.getText().trim()));
                    
                    int rowsAffected = stmt.executeUpdate();
                    if (rowsAffected > 0) {
                        JOptionPane.showMessageDialog(dialog, "Product added successfully!");
                        dialog.dispose();
                        loadProducts(); // Refresh the table
                    }
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(dialog, "Error saving product: " + ex.getMessage());
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Please enter a valid numeric price");
            }
        });
        
        dialog.setVisible(true);
    }
}