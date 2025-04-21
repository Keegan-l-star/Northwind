import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class TheConnection{
    private String username;
    private String password;
    private String url;
    private Connection conn;
            
    public TheConnection(String url, String username, String password) {
        this.url = url;
        this.username = username;
        this.password = password;
            try {
            conn = DriverManager.getConnection(this.url, this.username, this.password);
            System.out.println("Database connected successfully!");
        } catch (SQLException e) {
            System.out.println("Error connecting to the database: " + e.getMessage());
        }
    }

    public Connection getConnection() {
        return conn;
    }
    
    public void closeConnection() {
        if (conn != null) {
            try {
                conn.close();
                System.out.println("Database connection closed.");
            } catch (SQLException e) {
                System.out.println("Error closing the connection: " + e.getMessage());
            }
        }
    }
            
}
