package com.mycompany.northwind;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Connect {
    private static Connection conn;

    private Connect() {}

    public static Connection getInstance() {
        if (conn == null) {
            synchronized (Connect.class) {
                if (conn == null) {
                    String proto= System.getenv("dvdrental_DVDB_PROTO");
                    System.out.println("Proto: " + System.getenv("dvdrental_DVDB_PROTO"));
                    System.out.println("Host: " + System.getenv("dvdrental_DVDB_HOST"));
                    System.out.println("Port: " + System.getenv("dvdrental_DVDB_PORT"));
                    System.out.println("DB Name: " + System.getenv("dvdrental_DVDB_NAME"));
                    System.out.println("Username: " + System.getenv("dvdrental_DVDB_USERNAME"));
                    System.out.println("Password: " + System.getenv("dvdrental_DVDB_PASSWORD"));
                    String port= System.getenv("dvdrental_DVDB_PORT");
                    String host= System.getenv("dvdrental_DVDB_HOST");
                    String dbname= System.getenv("dvdrental_DVDB_NAME");
                    String name= System.getenv("dvdrental_DVDB_USERNAME");
                    String password= System.getenv("dvdrental_DVDB_PASSWORD");
                    
                    if(proto==null || host == null || port==null || name==null ||password==null){
                        throw new RuntimeException("Missing environment variables for the database connection");
                    }
                    
                    String url =String.format("%s://%s:%s/%s",proto,host,port,dbname);
                    
                    
                   
                    

                    try {
                        conn = DriverManager.getConnection(url, name, password);
                        System.out.println("Database connected successfully!");

                        // Register shutdown hook to close connection
                        Runtime.getRuntime().addShutdownHook(new Thread(() -> closeConnection()));

                    } catch (SQLException e) {
                        System.out.println("Error connecting to the database: " + e.getMessage());
                    }
                }
            }
        }
        return conn;
    }

    public static void closeConnection() {
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
