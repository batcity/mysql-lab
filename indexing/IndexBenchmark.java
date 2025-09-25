import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;

public class IndexBenchmark {

    private static final String CSV_FILE = "employees_good.csv";
    private static final int NUM_ROWS = 1_000_000;
    private static final String DB_URL = "jdbc:mysql://localhost:3306/labdb?allowLoadLocalInfile=true";
    private static final String ROOT_USER = "root";
    private static final String ROOT_PASS = "root";

    public static void main(String[] args) {
        long timeWithoutIndex = 0;
        long timeWithIndex = 0;

        try (Connection conn = DriverManager.getConnection(DB_URL, ROOT_USER, ROOT_PASS);
             Statement stmt = conn.createStatement()) {

            section("Setup");
            log("Setting up database for good index demo...");
            stmt.execute("CREATE TABLE IF NOT EXISTS employees_good (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "name VARCHAR(50), " +
                    "department VARCHAR(50), " +
                    "salary INT)");
            stmt.execute("TRUNCATE TABLE employees_good");

            // Drop leftover index if exists
            try {
                stmt.execute("DROP INDEX idx_name ON employees_good");
            } catch (SQLException ignored) { }

            section("Data Generation");
            log("Generating and loading data...");
            generateCSVAndLoad(stmt);

            // Pick one employee to query
            String targetName = "Employee500000";

            // Benchmark without index
            section("Benchmark Without Index");
            explainQuery(stmt, "SELECT * FROM employees_good WHERE name='" + targetName + "'");
            timeWithoutIndex = timeQuery(stmt, "SELECT * FROM employees_good WHERE name='" + targetName + "'");
            result("Time without index: " + timeWithoutIndex + " ms");

            // Add index
            log("Creating index on name...");
            stmt.execute("CREATE INDEX idx_name ON employees_good(name)");

            // Benchmark with index
            section("Benchmark With Index");
            explainQuery(stmt, "SELECT * FROM employees_good WHERE name='" + targetName + "'");
            timeWithIndex = timeQuery(stmt, "SELECT * FROM employees_good WHERE name='" + targetName + "'");
            result("Time with index: " + timeWithIndex + " ms");

            stmt.execute("TRUNCATE TABLE employees_good");
            log("Benchmark finished. Table cleared.");

        } catch (SQLException | IOException e) {
            e.printStackTrace();
        } finally {
            // cleanup CSV
            File csv = new File(CSV_FILE);
            if (csv.exists()) csv.delete();
        }

        // Show summary
        section("Benchmark Summary");
        System.out.println(" - Time without index: " + timeWithoutIndex + " ms");
        System.out.println(" - Time with index:    " + timeWithIndex + " ms");
        if (timeWithIndex > 0) {
            System.out.println(" 🎯 Speedup: ~" + (timeWithoutIndex / timeWithIndex) + "x faster with index.");
        }
    }

    private static void generateCSVAndLoad(Statement stmt) throws IOException, SQLException {
        String[] depts = {"HR", "IT", "Sales", "Finance", "Marketing"};

        try (FileWriter writer = new FileWriter(CSV_FILE)) {
            for (int i = 1; i <= NUM_ROWS; i++) {
                writer.write("Employee" + i + "," +
                        depts[i % depts.length] + "," +
                        (i % 100_000) + "\n");
                if (i % 100_000 == 0) {
                    result("Generated " + i + " rows...");
                }
            }
        }

        stmt.execute("LOAD DATA LOCAL INFILE '" + CSV_FILE.replace("\\", "/") + "' " +
                "INTO TABLE employees_good " +
                "FIELDS TERMINATED BY ',' " +
                "LINES TERMINATED BY '\\n' " +
                "(name, department, salary)");
    }

    private static long timeQuery(Statement stmt, String sql) throws SQLException {
        long start = System.currentTimeMillis();
        try (ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) { } // simulate reading
        }
        return System.currentTimeMillis() - start;
    }

    private static void explainQuery(Statement stmt, String sql) throws SQLException {
        try (ResultSet rs = stmt.executeQuery("EXPLAIN " + sql)) {
            System.out.println("\n📊 Query Plan:");
            System.out.printf("%-3s | %-10s | %-10s | %-8s | %-20s%n",
                    "id", "type", "key", "rows", "extra");
            System.out.println("----+------------+------------+----------+----------------------");

            while (rs.next()) {
                System.out.printf("%-3s | %-10s | %-10s | %-8s | %-20s%n",
                        rs.getString("id"),
                        rs.getString("type"),
                        rs.getString("key"),
                        rs.getString("rows"),
                        rs.getString("Extra"));
            }
            System.out.println();
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

    private static void result(String message) {
        System.out.println("✅ " + message);
    }
}
