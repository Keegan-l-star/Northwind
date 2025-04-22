/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

//SELECT first_name,last_name,address,city,country_region,zip_postal_code,business_phone,home_phone,mobile_phone,company FROM employees;
package com.mycompany.northwind;

/**
 *
 * @author kllew
 *
 */

import java.sql.Connection;
import javax.swing.*;

public class Northwind {

    public static void main(String[] args) {
        System.out.println("Program opened");
        Connection conn = Connect.getInstance();
        
        if (conn == null) {
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
        
        
        // Creation of the employees Panel
        EmployeesTab employeesPanel = new EmployeesTab(conn); 
        //End of the connection of the employess Panel
        //Beginning of the Product creation 
        //ProductsTab productsPanel = new ProductsTab(connect);
       
        // Add tabs
        tabbedPane.addTab("Employees", employeesPanel.GetFrame());
        tabbedPane.addTab("Products",new ProductsTab(conn));
        tabbedPane.addTab("Report", new ReportTab(conn));
        tabbedPane.addTab("Notifications", new NotificationsTab(conn));

        frame.add(tabbedPane);
        frame.setVisible(true);
        
        
    }
}
