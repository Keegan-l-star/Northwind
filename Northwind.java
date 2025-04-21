/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.mycompany.northwind;

/**
 *
 * @author kllew
 *
 */

import javax.swing.*;
public class Northwind {

    public static void main(String[] args) {
        
        String proto = System.getenv("dvdrental_DB_PROTO");
        String host = System.getenv("dvdrental_DB_HOST");
        String port = System.getenv("dvdrental_DB_PORT");
        String dbName = System.getenv("dvdrental_DB_NAME");
        String username = System.getenv("dvdrental_DB_USERNAME");
        String password = System.getenv("dvdrental_DB_PASSWORD");

        // Check for missing variables
        if (proto == null || host == null || port == null || dbName == null || username == null || password == null) {
            System.err.println("Missing one or more required environment variables.");
            System.exit(1);
        }
        String url = String.format("jdbc:%s://%s:%s/%s", proto, host, port, dbName);
        TheConnection connect=new TheConnection(url,username,password);
        
        JFrame frame = new JFrame("Northwind Application");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        frame.setSize(1100, 800);
        // Create a tabbed pane
        JTabbedPane tabbedPane = new JTabbedPane();
        // Creating the tabs 
        JPanel homePanel = new JPanel();
        EmployeesTab employeesPanel = new EmployeesTab(connect);
        JPanel productsPanel = new JPanel();
        JPanel reportPanel = new JPanel();
        JPanel notiTab = new JPanel();
        //Giving the tabs names
        // links the tabs to the tabbed pane like jquery
        tabbedPane.addTab("Home", homePanel);
        tabbedPane.addTab("Employees", employeesPanel);
        tabbedPane.addTab("Products", productsPanel);
        tabbedPane.addTab("Report",reportPanel);
        tabbedPane.addTab("Notifications",notiTab);

        
        frame.add(tabbedPane);
        frame.setVisible(true);
    }
}
