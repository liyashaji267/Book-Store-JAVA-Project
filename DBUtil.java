import java.sql.*;

public class DBUtil {
    private static final String URL = "jdbc:mysql://localhost:3306/bookstore_db";
    private static final String USER = "root";  // Change if needed
    private static final String PASS = "liyapass";  // Change if needed

    static {
        try {
            // ✅ Load MySQL JDBC Driver
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("❌ MySQL JDBC Driver not found. Make sure mysql-connector-j.jar is in your classpath.");
            e.printStackTrace();
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }
}
