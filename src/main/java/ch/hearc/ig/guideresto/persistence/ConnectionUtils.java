package ch.hearc.ig.guideresto.persistence;

import javax.sql.rowset.CachedRowSet;
import javax.sql.rowset.RowSetProvider;
import java.sql.*;

/**
 * Provide helper methods to deal with database connections
 */
public class ConnectionUtils {

    private static final String DBURL = "jdbc:oracle:thin:@localhost:1521:xe";
    private static final String USER = "system";
    private static final String PASSWORD = "oracle";

    private static Connection connection;

    public static Connection getConnection() {
        try {
            if (ConnectionUtils.connection == null || ConnectionUtils.connection.isClosed()) {
                Connection connection = DriverManager.getConnection(DBURL, USER, PASSWORD);
                connection.setAutoCommit(false);
                ConnectionUtils.connection = connection;
            }
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
        return ConnectionUtils.connection;
    }

    public static void inTransaction(Runnable runnable) {
        try(Connection connection = ConnectionUtils.getConnection()) {
            connection.setAutoCommit(false);
            runnable.run();
            connection.commit();
        } catch(SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
