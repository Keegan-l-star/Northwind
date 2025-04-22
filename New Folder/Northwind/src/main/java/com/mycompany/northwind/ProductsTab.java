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

    public ProductsTab(Connection conn) {
        this.conn = conn;
        setLayout(new BorderLayout());

        String[] columns = {"ID", "Product Code", "Product Name", "Supplier", "Category",
                           "Quantity Per Unit", "Standard Cost", "List Price"};
        model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(model);
        table.setAutoCreateRowSorter(true);

        JButton addButton = new JButton("Add New Product");
        addButton.addActionListener(e -> showAddProductDialog());

        add(addButton, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);

        loadSuppliers();
        loadProducts();
    }

    private void loadSuppliers() {
        String supplierQuery = "SELECT id, company FROM suppliers";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(supplierQuery)) {

            while (rs.next()) {
                suppliers.put(rs.getInt("id"), rs.getString("company"));
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading suppliers: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadProducts() {
        model.setRowCount(0);

        String query = "SELECT p.id, p.product_code, p.product_name, s.company AS supplier, p.category, " +
                      "p.quantity_per_unit, p.standard_cost, p.list_price " +
                      "FROM products p " +
                      "LEFT JOIN suppliers s ON p.supplier_ids = s.id " +
                      "ORDER BY p.id";

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                Object[] row = {
                    rs.getInt("id"),
                    rs.getString("product_code"),
                    rs.getString("product_name"),
                    rs.getString("supplier"),
                    rs.getString("category"),
                    rs.getString("quantity_per_unit"),
                    rs.getBigDecimal("standard_cost").toString(),
                    rs.getBigDecimal("list_price").toString()
                };
                model.addRow(row);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showAddProductDialog() {
        JDialog dialog = new JDialog();
        dialog.setTitle("Add New Product");
        dialog.setSize(400, 400);
        dialog.setModal(true);
        dialog.setLayout(new GridLayout(0, 2, 5, 5));

        JTextField codeField = new JTextField();
        JTextField nameField = new JTextField();
        JComboBox<String> supplierCombo = new JComboBox<>(suppliers.values().toArray(new String[0]));
        JTextField categoryField = new JTextField();
        JTextField quantityField = new JTextField();
        JTextField costField = new JTextField();
        JTextField priceField = new JTextField();
        JButton saveButton = new JButton("Save");

        dialog.add(new JLabel("Product Code:"));
        dialog.add(codeField);
        dialog.add(new JLabel("Product Name:"));
        dialog.add(nameField);
        dialog.add(new JLabel("Supplier:"));
        dialog.add(supplierCombo);
        dialog.add(new JLabel("Category:"));
        dialog.add(categoryField);
        dialog.add(new JLabel("Quantity Per Unit:"));
        dialog.add(quantityField);
        dialog.add(new JLabel("Standard Cost:"));
        dialog.add(costField);
        dialog.add(new JLabel("List Price:"));
        dialog.add(priceField);
        dialog.add(new JLabel(""));
        dialog.add(saveButton);

        saveButton.addActionListener(e -> {
            try {
                if (nameField.getText().trim().isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "Product name is required");
                    return;
                }

                if (priceField.getText().trim().isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "List price is required");
                    return;
                }

                int supplierId = suppliers.entrySet().stream()
                    .filter(entry -> entry.getValue().equals(supplierCombo.getSelectedItem()))
                    .map(Map.Entry::getKey)
                    .findFirst().orElse(-1);

                if (supplierId == -1) {
                    JOptionPane.showMessageDialog(dialog, "Please select a valid supplier");
                    return;
                }

                java.math.BigDecimal cost;
                java.math.BigDecimal price;

                try {
                    cost = new java.math.BigDecimal(costField.getText().trim());
                    price = new java.math.BigDecimal(priceField.getText().trim());
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(dialog, "Enter valid numeric values for cost and price");
                    return;
                }

                String query = "INSERT INTO products (product_code, product_name, supplier_ids, category, " +
                              "quantity_per_unit, standard_cost, list_price) VALUES (?, ?, ?, ?, ?, ?, ?)";

                try (PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
                    stmt.setString(1, codeField.getText().trim());
                    stmt.setString(2, nameField.getText().trim());
                    stmt.setInt(3, supplierId);
                    stmt.setString(4, categoryField.getText().trim());
                    stmt.setString(5, quantityField.getText().trim());
                    stmt.setBigDecimal(6, cost);
                    stmt.setBigDecimal(7, price);

                    int rowsAffected = stmt.executeUpdate();
                    if (rowsAffected > 0) {
                        JOptionPane.showMessageDialog(dialog, "Product added successfully!");
                        dialog.dispose();
                        loadProducts();
                    }
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(dialog, "Error saving product: " + ex.getMessage());
                ex.printStackTrace();
            }
        });

        dialog.setVisible(true);
    }
}
