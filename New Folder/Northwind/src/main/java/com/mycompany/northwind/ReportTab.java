package com.mycompany.northwind;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import javax.swing.table.DefaultTableCellRenderer;

public class ReportTab extends JPanel {
    private final DefaultTableModel model;
    private final JTable table;
    private final Connection conn;
    
    public ReportTab(Connection conn) {
        this.conn = conn;
        setLayout(new BorderLayout());
        String[] columns = {"Category", "Product Count"};
        model = new DefaultTableModel(columns, 0) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return columnIndex == 1 ? Integer.class : String.class;
            }
            
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        table = new JTable(model);
        table.setAutoCreateRowSorter(true);
        
        table.setDefaultRenderer(Integer.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, 
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, 
                    isSelected, hasFocus, row, column);
                ((JLabel)c).setHorizontalAlignment(SwingConstants.RIGHT);
                return c;
            }
        });
        
        JButton refreshButton = new JButton("Refresh Report");
        refreshButton.addActionListener(e -> loadReport());
        add(refreshButton, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);

        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentShown(java.awt.event.ComponentEvent evt) {
                loadReport();
            }
        });
    }
    
    private void loadReport() {
        model.setRowCount(0); //clear existing data
        
        String query = "SELECT category, COUNT(*) as product_count " +
                      "FROM products " +
                      "GROUP BY category " +
                      "ORDER BY category";
        
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                Object[] row = {
                    rs.getString("category"),
                    rs.getInt("product_count")
                };
                model.addRow(row);
            }

            addTotalCount();
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, 
                "Error loading report data: " + e.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
    private void addTotalCount() throws SQLException {
        String totalQuery = "SELECT COUNT(*) as total_products FROM products";
        
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(totalQuery)) {
            
            if (rs.next()) {
                model.addRow(new Object[]{"", ""}); //seperator row
                model.addRow(new Object[]{"TOTAL PRODUCTS", rs.getInt("total_products")});
            }
        }
    }
}

