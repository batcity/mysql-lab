package com.mysql.lab;

import java.sql.*;

public class IndexBenchmark {
    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/labdb";
        String user = "labuser";
        String pass = "labpass";

        try (Connection conn = DriverManager.getConnection(url, user, pass);
             Statement stmt = conn.createStatement()) {

            // 1. Create table if it doesn't exist
            stmt.execute("CREATE TABLE IF NOT EXISTS employees (" +
                         "id INT AUTO_INCREMENT PRIMARY KEY, " +
                         "name VARCHAR(50), " +
                         "department VARCHAR(50), " +
                         "salary INT)");

            // 2. Insert data if table is empty
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM employees");
            rs.next();
            if (rs.getInt(1) == 0) {
                System.out.println("Inserting data...");
                stmt.execute("INSERT INTO employees (name, department, salary) " +
                        "SELECT CONCAT('Employee', t.n), " +
                        "ELT(1 + (RAND() * 5), 'HR','IT','Sales','Finance','Marketing'), " +
                        "FLOOR(RAND() * 100000) " +
                        "FROM (SELECT @row := @row + 1 as n " +
                        "FROM information_schema.columns, (SELECT @row := 0) init " +
                        "LIMIT 1000000) t");
            }

            // 3. Query WITHOUT index
            long start = System.currentTimeMillis();
            stmt.executeQuery("SELECT * FROM employees WHERE department='IT'");
            long end = System.currentTimeMillis();
            System.out.println("Without index: " + (end - start) + " ms");

            // 4. CREATE INDEX (no IF NOT EXISTS)
            stmt.execute("CREATE INDEX idx_department ON employees(department)");

            // 5. Query WITH index
            start = System.currentTimeMillis();
            stmt.executeQuery("SELECT * FROM employees WHERE department='IT'");
            end = System.currentTimeMillis();
            System.out.println("With index: " + (end - start) + " ms");

            // 6. Clean up
            // stmt.execute("DROP TABLE IF EXISTS employees");
            // System.out.println("Table deleted after benchmark.");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
