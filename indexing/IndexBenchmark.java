import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;
import java.util.Random;
import java.util.logging.Logger;

public class IndexBenchmark {

    private static final String CSV_FILE = "employees.csv";
    private static final int NUM_ROWS = 1_000_000;
    private static final String DB_URL = "jdbc:mysql://localhost:3306/labdb?allowLoadLocalInfile=true";
    private static final String ROOT_USER = "root";
    private static final String ROOT_PASS = "root";
    private static final Logger logger = Logger.getLogger("IndexBenchmark");

    public static void main(String[] args) {
        try (Connection conn = DriverManager.getConnection(DB_URL, ROOT_USER, ROOT_PASS);
             Statement stmt = conn.createStatement()) {

            // Administrative setup
            logger.info("Setting up database environment...");
            stmt.execute("CREATE TABLE IF NOT EXISTS employees (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "name VARCHAR(50), " +
                    "department VARCHAR(50), " +
                    "salary INT)");
            stmt.execute("TRUNCATE TABLE employees");

            // Drop leftover index if it exists
            try {
                stmt.execute("DROP INDEX idx_department ON employees");
                logger.info("Existing index dropped.");
            } catch (SQLException e) {
                logger.info("No existing index to drop.");
            }

            logger.info("Generating and loading data...");
            generateCSVAndLoad(stmt);

            // Benchmark without index
            logger.info("Running query without index...");
            explainQuery(stmt, "SELECT * FROM employees WHERE department='IT'");
            long timeWithoutIndex = timeQuery(stmt, "SELECT * FROM employees WHERE department='IT'");
            logger.info("Time without index: " + timeWithoutIndex + " ms");

            // Create index
            stmt.execute("CREATE INDEX idx_department ON employees(department)");
            logger.info("Index created.");

            // Benchmark with index
            logger.info("Running query with index...");
            explainQuery(stmt, "SELECT * FROM employees WHERE department='IT'");
            long timeWithIndex = timeQuery(stmt, "SELECT * FROM employees WHERE department='IT'");
            logger.info("Time with index: " + timeWithIndex + " ms");

            // Cleanup table
            stmt.execute("TRUNCATE TABLE employees");
            logger.info("Benchmark finished. Table cleared.");

        } catch (SQLException | IOException e) {
            e.printStackTrace();
        } finally {
            // Cleanup CSV file
            File csv = new File(CSV_FILE);
            if (csv.exists() && csv.delete()) {
                logger.info("Temporary CSV file deleted: " + CSV_FILE);
            } else if (csv.exists()) {
                logger.warning("Could not delete temporary CSV file.");
            }
        }
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
                    logger.info("Generated " + i + " rows...");
                }
            }
        }

        logger.info("Loading data into the table");
        stmt.execute("LOAD DATA LOCAL INFILE '" + CSV_FILE.replace("\\", "/") + "' " +
                "INTO TABLE employees " +
                "FIELDS TERMINATED BY ',' " +
                "LINES TERMINATED BY '\\n' " +
                "(name, department, salary)");
    }

    private static long timeQuery(Statement stmt, String sql) throws SQLException {
        long start = System.currentTimeMillis();
        try (ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) { }
        }
        return System.currentTimeMillis() - start;
    }

    private static void explainQuery(Statement stmt, String sql) throws SQLException {
        try (ResultSet rs = stmt.executeQuery("EXPLAIN " + sql)) {
            ResultSetMetaData meta = rs.getMetaData();
            int colCount = meta.getColumnCount();

            // Print column headers
            StringBuilder header = new StringBuilder();
            for (int i = 1; i <= colCount; i++) {
                header.append(meta.getColumnName(i)).append("\t");
            }
            logger.info(header.toString());

            // Print rows
            while (rs.next()) {
                StringBuilder row = new StringBuilder();
                for (int i = 1; i <= colCount; i++) {
                    row.append(rs.getString(i)).append("\t");
                }
                logger.info(row.toString());
            }
        }
    }
}
