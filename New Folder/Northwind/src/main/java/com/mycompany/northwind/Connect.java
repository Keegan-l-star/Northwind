package com.mycompany.northwind;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Connect {
    private static Connection conn;

    private Connect() {
        
    }

    public static Connection getInstance() {
        if (conn == null) {
            String url = "database";
            String username = "root";
            String password = "password";

            try {
                conn = DriverManager.getConnection(url, username, password);
                System.out.println("Database connected successfully!");
            } catch (SQLException e) {
                System.out.println("Error connecting to the database: " + e.getMessage());
            
                
            }
        }
        return conn;
    }
    
    public void close() {
        if (conn != null) {
            try {
                conn.close();
                System.out.println("Database connection closed.");
            } catch (SQLException e) {
                System.out.println("Error closing connection: " + e.getMessage());
            }
        }
    }
}