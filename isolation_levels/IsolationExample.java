import java.sql.*;

public class IsolationExample {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/labdb?allowLoadLocalInfile=true";
    private static final String ROOT_USER = "root";
    private static final String ROOT_PASS = "root";

    public static void main(String args[]) {
        try {
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

            Connection reader = DriverManager.getConnection(DB_URL, ROOT_USER, ROOT_PASS);
            reader.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
            Statement stmtTwo = reader.createStatement();
            System.out.println("This block doesn't print anything since the transaction wasn't committed");
            ResultSet resultsTwo = stmtTwo.executeQuery("SELECT * FROM employees");
            while (resultsTwo.next()) {
                System.out.println(resultsTwo.getString("name"));
            }
        } catch(Exception exception) {
            System.out.println(exception.getMessage());
        }
    }
}



