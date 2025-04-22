package com.mycompany.northwind;

import javax.swing.*;

public class HomeTab extends JPanel {
    public HomeTab() {
        JLabel welcomeLabel = new JLabel("<html><h1>Welcome to Northwind Traders</h1>" +
            "<p>Use the tabs above to navigate through the system</p></html>", 
            SwingConstants.CENTER);
        add(welcomeLabel);
    }
}