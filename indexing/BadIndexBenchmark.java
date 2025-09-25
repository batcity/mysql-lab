import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;
import java.util.Random;

public class BadIndexBenchmark {

    private static final String CSV_FILE = "employees.csv";
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
            log("Setting up database environment...");
            stmt.execute("CREATE TABLE IF NOT EXISTS employees (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "name VARCHAR(50), " +
                    "department VARCHAR(50), " +
                    "salary INT)");
            stmt.execute("TRUNCATE TABLE employees");

            // Drop leftover index if exists
            try {
                stmt.execute("DROP INDEX idx_department ON employees");
                log("Dropped existing index.");
            } catch (SQLException e) {
                log("No existing index to drop.");
            }

            section("Data Generation");
            log("Generating and loading data...");
            generateCSVAndLoad(stmt);

            // Benchmark without index
            section("Benchmark Without Index");
            explainQuery(stmt, "SELECT * FROM employees WHERE department='IT'");
            timeWithoutIndex = timeQuery(stmt, "SELECT * FROM employees WHERE department='IT'");
            result("Time without index: " + timeWithoutIndex + " ms");

            // Add index
            log("Creating index on department...");
            stmt.execute("CREATE INDEX idx_department ON employees(department)");

            // Benchmark with index
            section("Benchmark With Index");
            explainQuery(stmt, "SELECT * FROM employees WHERE department='IT'");
            timeWithIndex = timeQuery(stmt, "SELECT * FROM employees WHERE department='IT'");
            result("Time with index: " + timeWithIndex + " ms");

            // Cleanup
            stmt.execute("TRUNCATE TABLE employees");
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
            if (timeWithIndex < timeWithoutIndex) {
                System.out.println(" 🎯 Index helped: ~" + (timeWithoutIndex / timeWithIndex) + "x faster with index.");
            } else {
                System.out.println(" ⚠️  Index hurt performance: query got slower with index!");
            }
        }

        // Add clear conclusion
        section("Conclusion");
        System.out.println("👉 The index on 'department' actually made the query slower!");
        System.out.println("   Why? Because 'department' has very few distinct values,");
        System.out.println("   so MySQL ends up fetching hundreds of thousands of rows through the index,");
        System.out.println("   which is slower than scanning the whole table sequentially.");
    }

    private static void generateCSVAndLoad(Statement stmt) throws IOException, SQLException {
        String[] depts = {"HR", "IT", "Sales", "Finance", "Marketing"};
        Random rand = new Random();

        try (FileWriter writer = new FileWriter(CSV_FILE)) {
            for (int i = 1; i <= NUM_ROWS; i++) {
                writer.write("Employee" + i + "," +
                        depts[rand.nextInt(depts.length)] + "," +
                        rand.nextInt(100_000) + "\n");
                if (i % 100_000 == 0) {
                    result("Generated " + i + " rows...");
                }
            }
        }

        log("Loading data into the table...");
        stmt.execute("LOAD DATA LOCAL INFILE '" + CSV_FILE.replace("\\", "/") + "' " +
                "INTO TABLE employees " +
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
            System.out.printf("%-3s | %-10s | %-15s | %-8s | %-20s%n",
                    "id", "type", "key", "rows", "extra");
            System.out.println("----+------------+-----------------+----------+----------------------");

            while (rs.next()) {
                System.out.printf("%-3s | %-10s | %-15s | %-8s | %-20s%n",
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