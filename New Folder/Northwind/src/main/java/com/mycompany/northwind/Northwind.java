/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.mycompany.northwind;

/**
 *
 * @author kllew
 *
 */

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;


public class Northwind {

    public static void main(String[] args) {
        System.out.println("Program opened");
        Connection connect = getDatabaseConnection();
        
        if (connect == null) {
            JOptionPane.showMessageDialog(null,
                "Unable to connect to the database. Please check your environment variables.",
                "Database Connection Error",
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        JFrame frame = new JFrame("Northwind Application");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1100, 800);

        // Create a tabbed pane
        JTabbedPane tabbedPane = new JTabbedPane();

        // Creating the tabs
        JPanel homePanel = new JPanel();
        
        // Creation of the employees Panel
        JPanel employeesPanel = new JPanel();
        
        String[] columns = {
            "First Name", "Last Name", "Address", "Address Line 2", "City",
            "Region", "Postal Code", "Phone", "Office", "Active"
        };
        
        employeesPanel.setLayout(new BoxLayout(employeesPanel, BoxLayout.Y_AXIS));
        DefaultTableModel model = new DefaultTableModel(columns, 0);
        JTable table= new JTable(model);
        JScrollPane scrollPane= new JScrollPane(table);
        employeesPanel.add(scrollPane);
        
        //End of the connection of the employess Panel
        //Beginning of the Product creation 
        JPanel productsPanel = new JPanel();
        JPanel reportPanel = new JPanel();
        JPanel notiTab = new JPanel();

        // Add tabs
        tabbedPane.addTab("Home", homePanel);
        tabbedPane.addTab("Employees", employeesPanel);
        tabbedPane.addTab("Products", productsPanel);
        tabbedPane.addTab("Report", reportPanel);
        tabbedPane.addTab("Notifications", notiTab);

        frame.add(tabbedPane);
        frame.setVisible(true);
    }
    
    
    private static Connection getDatabaseConnection() {
        String url = "jdbc:mariadb://localhost:3306/u24594522_u24641342_northwind [root on Default schema]";
        String username = "root";
        String password = "password";

        try {
            Connection conn = DriverManager.getConnection(url, username, password);
            System.out.println("Database connected successfully!");
            return conn;
        } catch (SQLException e) {
            System.out.println("Error connecting to the database: " + e.getMessage());
            return null;
        }
    }
}
