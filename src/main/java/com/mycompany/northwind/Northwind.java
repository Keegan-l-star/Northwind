/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.mycompany.northwind;

/**
 *
 * @author michael
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

        // Add all tabs
        tabbedPane.addTab("Home", new HomeTab());
        tabbedPane.addTab("Employees", new EmployeesTab(connect));
        tabbedPane.addTab("Products", new ProductsTab(connect));
        tabbedPane.addTab("Report", new ReportTab(connect));
        tabbedPane.addTab("Notifications", new NotificationsTab(connect));

        frame.add(tabbedPane);
        frame.setVisible(true);
    }
    
    private static Connection getDatabaseConnection() {
        String protocol = System.getenv("dvdrental_DB_PROTO") != null ? 
            System.getenv("dvdrental_DB_PROTO") : "mariadb";
        String host = System.getenv("dvdrental_DB_HOST") != null ? 
            System.getenv("dvdrental_DB_HOST") : "172.19.0.34";
        String port = System.getenv("dvdrental_DB_PORT") != null ? 
            System.getenv("dvdrental_DB_PORT") : "3306";
        String dbName = System.getenv("dvdrental_DB_NAME") != null ? 
            System.getenv("dvdrental_DB_NAME") : "u24594522_u24641342_northwind";
        String username = System.getenv("dvdrental_DB_USERNAME") != null ? 
            System.getenv("dvdrental_DB_USERNAME") : "netbeans";
        String password = System.getenv("dvdrental_DB_PASSWORD") != null ? 
            System.getenv("dvdrental_DB_PASSWORD") : "your_password";

        String url = "jdbc:" + protocol + "://" + host + ":" + port + "/" + dbName;

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
