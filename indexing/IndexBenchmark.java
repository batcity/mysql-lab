import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;
import java.util.logging.Logger;

public class IndexBenchmark {

    private static final String CSV_FILE = "employees_good.csv";
    private static final int NUM_ROWS = 1_000_000;
    private static final String DB_URL = "jdbc:mysql://localhost:3306/labdb?allowLoadLocalInfile=true";
    private static final String ROOT_USER = "root";
    private static final String ROOT_PASS = "root";
    private static final Logger logger = Logger.getLogger("GoodIndexBenchmark");

    public static void main(String[] args) {
        try (Connection conn = DriverManager.getConnection(DB_URL, ROOT_USER, ROOT_PASS);
             Statement stmt = conn.createStatement()) {

            // Setup table
            logger.info("Setting up database for good index demo...");
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

            // Generate + load data
            logger.info("Generating and loading data...");
            generateCSVAndLoad(stmt);

            // Pick one employee to query
            String targetName = "Employee500000";

            // Benchmark without index
            logger.info("Running lookup query without index...");
            explainQuery(stmt, "SELECT * FROM employees_good WHERE name='" + targetName + "'");
            long timeWithoutIndex = timeQuery(stmt, "SELECT * FROM employees_good WHERE name='" + targetName + "'");
            logger.info("Time without index: " + timeWithoutIndex + " ms");

            // Add index
            stmt.execute("CREATE INDEX idx_name ON employees_good(name)");
            logger.info("Index created on name.");

            // Benchmark with index
            logger.info("Running lookup query with index...");
            explainQuery(stmt, "SELECT * FROM employees_good WHERE name='" + targetName + "'");
            long timeWithIndex = timeQuery(stmt, "SELECT * FROM employees_good WHERE name='" + targetName + "'");
            logger.info("Time with index: " + timeWithIndex + " ms");

            stmt.execute("TRUNCATE TABLE employees_good");
            logger.info("Benchmark finished. Table cleared.");

        } catch (SQLException | IOException e) {
            e.printStackTrace();
        } finally {
            // cleanup CSV
            File csv = new File(CSV_FILE);
            if (csv.exists()) csv.delete();
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
                    logger.info("Generated " + i + " rows...");
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
            StringBuilder table = new StringBuilder();
            table.append("id | type | key | rows | extra\n");

            while (rs.next()) {
                table.append(String.format("%s | %s | %s | %s | %s\n",
                        rs.getString("id"),
                        rs.getString("type"),
                        rs.getString("key"),
                        rs.getString("rows"),
                        rs.getString("Extra")));
            }

            logger.info("\n" + table.toString());
        }
    }
}
