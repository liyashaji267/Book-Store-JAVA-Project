import java.sql.*;

public class TestDB {
    public static void main(String[] args) {
        try (Connection con = DBUtil.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM books")) {

            if (rs.next()) {
                System.out.println("✅ Books in DB: " + rs.getInt(1));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
