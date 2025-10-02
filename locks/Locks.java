import java.io.IOException;
import java.sql.*;

public class Locks {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/labdb?allowLoadLocalInfile=true";
    private static final String ROOT_USER = "root";
    private static final String ROOT_PASS = "root";

    public static void main(String[] args) {
        long timeWithoutIndex = 0;
        long timeWithIndex = 0;

        try (Connection conn = DriverManager.getConnection(DB_URL, ROOT_USER, ROOT_PASS);
             Statement stmt = conn.createStatement()) {

            section("Setup");
            log("Setting up database for lock demo...");
            stmt.execute("CREATE TABLE IF NOT EXISTS lock_lab (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "name VARCHAR(50))");
            stmt.execute("TRUNCATE TABLE employees_good");
        } catch (SQLException e) {
            e.printStackTrace();
        }
 
    }

    // === Utility methods for clean logging ===
    private static void section(String title) {
        System.out.println("\n==============================");
        System.out.println(" " + title);
        System.out.println("==============================");
    }

    private static void log(String message) {
        System.out.println("👉 " + message);
    }
    
}
