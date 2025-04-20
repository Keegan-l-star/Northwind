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
        JFrame frame = new JFrame("Northwind Application");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        frame.setSize(1100, 800);
        // Create a tabbed pane
        JTabbedPane tabbedPane = new JTabbedPane();
        // Creating the tabs 
        JPanel homePanel = new JPanel();
        JPanel employeesPanel = new JPanel();
        JPanel productsPanel = new JPanel();
        JPanel reportPanel = new JPanel();
        JPanel notiTab = new JPanel();
        
        
        //Woking on the Employees tab
        String[] columns = {
            "First Name", "Last Name", "Address", "Address Line 2", "City",
            "Region", "Postal Code", "Phone", "Office", "Active"
        };
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
