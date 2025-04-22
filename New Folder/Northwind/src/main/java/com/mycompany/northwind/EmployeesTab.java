package com.mycompany.northwind;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.sql.*;
import java.awt.*;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
        
public class EmployeesTab{
    JPanel frame;
   
    String[] columns = {
            "First Name", "Last Name", "Address", "Address Line 2", "City",
            "Region", "Postal Code", "Home","business","Modile", "Office", "Active"
        };
    
    JTextField filterField1;
    
    DefaultTableModel model;
    Connection conn;
    
    public EmployeesTab(Connection connn){
        this.frame= new JPanel();
        this.frame.setLayout(new BoxLayout(this.frame, BoxLayout.Y_AXIS));
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel filterLabel1 = new JLabel("Filter by First Name:");
        filterField1 = new JTextField(20);
        JButton filterButton1 = new JButton("Apply Filter");
        filterPanel.add(filterLabel1);
        filterPanel.add(filterField1);
        filterPanel.add(filterButton1);
        this.frame.add(filterPanel);
        model = new DefaultTableModel(this.columns, 0);
        JTable table= new JTable(model);
        JScrollPane scrollPane= new JScrollPane(table);
        frame.add(scrollPane);
        filterField1.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { updateFilter(); }
            @Override
            public void removeUpdate(DocumentEvent e) { updateFilter(); }
            @Override
            public void changedUpdate(DocumentEvent e) { updateFilter(); }
            
            private void updateFilter() {
                String filterText = filterField1.getText().trim();
                populateTable(filterText);  // Ensure this updates the table dynamically
            }
        });
        try{
          this.conn=connn;  
       
            String query = "SELECT first_name,last_name,address,business_phone,home_phone,mobile_phone,city,country_region,zip_postal_code,job_title,company FROM employees";
            Statement stmt = this.conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            // 4. Fill table
        while (rs.next()) {
            Object[] row = {
                rs.getString("first_name"),
                rs.getString("last_name"),
                rs.getString("address"),
                "",
                rs.getString("city"),
                rs.getString("country_region"),
                rs.getString("zip_postal_code"),
                rs.getString("business_phone"),
                rs.getString("home_phone"),
                rs.getString("mobile_phone"),
                rs.getString("company"),
                "true"
            };
        model.addRow(row);
        
        }
            rs.close();
            stmt.close();
        }catch(Exception e){
            JOptionPane.showMessageDialog(null, "Database error: " + e.getMessage());
        }
        
    
    }
    
    public JPanel GetFrame(){
        return this.frame;
    }
    
    private void populateTable(String search){
         model.setRowCount(0);
        try{
            String query = "SELECT first_name,last_name,address,business_phone,home_phone,mobile_phone,"
                    + "city,country_region,zip_postal_code,job_title,company FROM employees"
                    + " WHERE first_name LIKE ? OR last_name LIKE ? OR address LIKE ? OR business_phone LIKE ? OR home_phone LIKE ?"
                    + "OR mobile_phone LIKE ? OR city LIKE ? OR country_region LIKE ? OR company LIKE ? OR zip_postal_code LIKE ?";
            try(PreparedStatement pstmt=this.conn.prepareStatement(query)){
                String searchPattern="%"+search+"%";
                
                for(int i=1;i<11;i++){
                    pstmt.setString(i, searchPattern);
                }
                try(ResultSet rs = pstmt.executeQuery()){
                    while (rs.next()) {
                    Object[] row = {
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("address"),
                        "",
                        rs.getString("city"),
                        rs.getString("country_region"),
                        rs.getString("zip_postal_code"),
                        rs.getString("business_phone"),
                        rs.getString("home_phone"),
                        rs.getString("mobile_phone"),
                        rs.getString("company"),
                        "true"
                    };
                    model.addRow(row);
                }
                rs.close();
                pstmt.close();   
            }
            
        
        }
            
        }
        catch(Exception e){
            System.out.println("Error: "+e.getMessage());
        }
        
    }
        
}