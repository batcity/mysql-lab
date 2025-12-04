import java.sql.*;

public class IsolationExample {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/labdb?allowLoadLocalInfile=true";
    private static final String ROOT_USER = "root";
    private static final String ROOT_PASS = "root";

    public static void main(String args[]) {

        try (Connection cleanup = DriverManager.getConnection(DB_URL, ROOT_USER, ROOT_PASS);
            Statement s = cleanup.createStatement()) {
            s.executeUpdate("DELETE FROM employees WHERE id IN (50,51)");
            // optionally VACUUM / OPTIMIZE for specific DBs if needed
            System.out.println("Cleaned demo rows.");
        } catch(Exception exception) {
            System.out.println(exception.getMessage());
        }

        try {
            // Read uncommmitted example
            Connection writer = DriverManager.getConnection(DB_URL, ROOT_USER, ROOT_PASS);
            writer.setAutoCommit(false);
            writer.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
            Statement stmt = writer.createStatement();

            stmt.execute("Insert into employees values(50, 'Zeus', 'god', 10000)");
            ResultSet results = stmt.executeQuery("SELECT * FROM employees");
            System.out.println("read uncommitted isolation level picks up uncommitted changes");
            while (results.next()) {
                System.out.println(results.getString("name"));
            }

            // Read committed example
            Connection reader = DriverManager.getConnection(DB_URL, ROOT_USER, ROOT_PASS);
            reader.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
            Statement stmtTwo = reader.createStatement();
            System.out.println("This block doesn't print anything since the transaction wasn't committed");
            ResultSet resultsTwo = stmtTwo.executeQuery("SELECT * FROM employees");
            while (resultsTwo.next()) {
                System.out.println(resultsTwo.getString("name"));
            }

            // Repeatable read example
            writer.commit();
            Connection repeatableReader = DriverManager.getConnection(DB_URL, ROOT_USER, ROOT_PASS);
            repeatableReader.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
            repeatableReader.setAutoCommit(false);
            Statement repeatableStmt = repeatableReader.createStatement();
            System.out.println("This repeatable block should return the same data twice even though there's an"
            + " additional insertion between the two reads");
            ResultSet repeatableResult = repeatableStmt.executeQuery("SELECT * FROM employees");
            while (repeatableResult.next()) {
                System.out.println(repeatableResult.getString("name"));
            }

            stmt.execute("Insert into employees values(51, 'Apollo', 'god', 10000)");
            writer.commit();

            ResultSet repeatableResultPartTwo = repeatableStmt.executeQuery("SELECT * FROM employees");
            while (repeatableResultPartTwo.next()) {
                System.out.println(repeatableResultPartTwo.getString("name"));
            }

            writer.rollback();
        } catch(Exception exception) {
            System.out.println(exception.getMessage());
        }
    }
}



