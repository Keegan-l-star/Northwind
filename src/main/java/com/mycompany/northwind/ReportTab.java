package com.mycompany.northwind;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class ReportTab extends JPanel {
    private final DefaultTableModel model;
    private final JTable table;
    private final Connection conn;
    
    public ReportTab(Connection conn) {
        this.conn = conn;
        setLayout(new BorderLayout());
        
        // Create components
        String[] columns = {"Warehouse", "Category", "Product Count"};
        model = new DefaultTableModel(columns, 0) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                switch (columnIndex) {
                    case 2: return Integer.class; // Product Count is numeric
                    default: return String.class; // Others are text
                }
            }
        };
        table = new JTable(model);
        table.setAutoCreateRowSorter(true);
        
        // Add components to panel
        add(new JScrollPane(table), BorderLayout.CENTER);
        
        // Load data when tab is shown
        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentShown(java.awt.event.ComponentEvent evt) {
                loadReport();
            }
        });
    }
    
    private void loadReport() {
        model.setRowCount(0); // Clear existing data
        
        String query = "SELECT w.warehouse_name, c.category_name, COUNT(p.product_id) as product_count " +
                      "FROM products p " +
                      "JOIN categories c ON p.category_id = c.category_id " +
                      "JOIN warehouses w ON p.warehouse_id = w.warehouse_id " +
                      "GROUP BY w.warehouse_name, c.category_name " +
                      "ORDER BY w.warehouse_name, c.category_name";
        
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                Object[] row = {
                    rs.getString("warehouse_name"),
                    rs.getString("category_name"),
                    rs.getInt("product_count")
                };
                model.addRow(row);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, 
                "Error loading report data: " + e.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
}