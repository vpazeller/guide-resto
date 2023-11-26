package ch.hearc.ig.guideresto.persistence;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Provide helper methods to execute queries
 */
public class QueryUtils {

    private static final boolean LOG_QUERIES = true;

    public static List<Map<String, Object>> findAll(String sql) {
        try (PreparedStatement s = ConnectionUtils.getConnection().prepareStatement(sql)) {
            QueryUtils.log(sql);
            ResultSet resultSet = s.executeQuery();
            List<Map<String, Object>> rows = ResultUtils.fetchAll(resultSet);
            // since the resultSet is created by this method, it is also its responsibility to close it
            resultSet.close();
            return rows;
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static List<Map<String, Object>> findAllByForeignKey(String sql, int fkVal) {
        try (PreparedStatement s = ConnectionUtils.getConnection().prepareStatement(sql)) {
            s.setInt(1, fkVal);
            QueryUtils.log(sql);
            ResultSet resultSet = s.executeQuery();
            List<Map<String, Object>> rows = ResultUtils.fetchAll(resultSet);
            // since the resultSet is created by this method, it is also its responsibility to close it
            resultSet.close();
            return rows;
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static Map<String, Object> findOneById(String sql, int id) {
        try (PreparedStatement s = ConnectionUtils.getConnection().prepareStatement(sql)) {
            s.setInt(1, id);
            QueryUtils.log(sql);
            ResultSet resultSet = s.executeQuery();
            Map<String, Object> row = ResultUtils.fetchOne(resultSet);
            // since the resultSet is created by this method, it is also its responsibility to close it
            resultSet.close();
            return row;
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static void updateById(String sql, int id, Consumer<PreparedStatement> paramsHandler) {
        try (PreparedStatement s = ConnectionUtils.getConnection().prepareStatement(sql)) {
            paramsHandler.accept(s);
            QueryUtils.log(sql);
            int nbParams = s.getParameterMetaData().getParameterCount();
            s.setInt(nbParams, id);
            s.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    public static Integer insert(String sql, Consumer<PreparedStatement> paramsHandler) {
        try (PreparedStatement s = ConnectionUtils.getConnection().prepareStatement(sql, new String[]{"NUMERO"})) {
            paramsHandler.accept(s);
            QueryUtils.log(sql);
            s.executeUpdate();
            try (ResultSet keys = s.getGeneratedKeys()) {
                keys.next();
                return keys.getInt(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    public static void deleteByPkOrFk(String sql, int id) {
        try (PreparedStatement s = ConnectionUtils.getConnection().prepareStatement(sql)) {
            s.setInt(1, id);
            QueryUtils.log(sql);
            s.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    private static void log(String sql) {
        if (QueryUtils.LOG_QUERIES) {
            // sadly the Oracle JDBC driver does not look good to display
            // the real sql (with parameters), see https://stackoverflow.com/questions/2683214/get-query-from-java-sql-preparedstatement
            // -> display the sql string which is already helpfull
            System.err.println("Query: " + sql);
        }
    }
}
