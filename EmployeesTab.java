import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.sql.*;

public class EmployeesTab{
    JPanel frame;
    String[] columns = {
            "First Name", "Last Name", "Address", "Address Line 2", "City",
            "Region", "Postal Code", "Phone", "Office", "Active"
        };
    
    Connection conn;
    
    public EmployeesTab(Connection connn){
        this.frame= new JPanel();
        this.frame.setLayout(new BoxLayout(this.frame, BoxLayout.Y_AXIS));
        DefaultTableModel model = new DefaultTableModel(this.columns, 0);
        JTable table= new JTable(model);
        JScrollPane scrollPane= new JScrollPane(table);
        frame.add(scrollPane);
        try{
          this.conn=connn;  
       
            String query = "SELECT first_name, last_name, address, address2, city, region, postal_code, phone, office, active FROM employees";
            Statement stmt = this.conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            // 4. Fill table
        while (rs.next()) {
            Object[] row = {
                rs.getString("first_name"),
                rs.getString("last_name"),
                rs.getString("address"),
                rs.getString("address2"),
                rs.getString("city"),
                rs.getString("region"),
                rs.getString("postal_code"),
                rs.getString("phone"),
                rs.getString("office"),
                rs.getBoolean("active")
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
    
}