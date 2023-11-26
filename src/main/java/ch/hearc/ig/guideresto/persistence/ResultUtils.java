package ch.hearc.ig.guideresto.persistence;

import java.io.Reader;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;

/**
 * Provide helper methods to fetch data from result sets
 */
public class ResultUtils {

    /**
     * Let's work with column names rather indices
     * (improves readability/maintenability)
     */
    public static Map<String, Object> fetchOne(ResultSet rs) {
        // inspired from: https://stackoverflow.com/questions/7507121/efficient-way-to-handle-resultset-in-java
        try {
            if (rs != null && !rs.isClosed() && rs.next()) {
                ResultSetMetaData md = rs.getMetaData();
                int nbColumns = md.getColumnCount();
                HashMap<String, Object> row = new HashMap<>();
                for(int i = 1 ; i <= nbColumns ; ++i) {
                    row.put(md.getColumnName(i),rs.getObject(i));
                }
                return row;
            }
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
        return null;
    }

    public static List<Map<String, Object>> fetchAll(ResultSet rs) {
        List<Map<String, Object>> rows = new ArrayList<>();
        try {
            if (rs != null && !rs.isClosed()) {
                Map<String, Object> row;
                do {
                    row = ResultUtils.fetchOne(rs);
                    if (row != null) {
                        rows.add(row);
                    }
                } while (row != null);
            }
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
        return rows;
    }

    public static String clobToString(java.sql.Clob clob) {
        try {
            if (clob == null || clob.length() == 0) {
                return null;
            }
            try (Reader reader = clob.getCharacterStream()) {
                char[] buffer = new char[(int)clob.length()];
                reader.read(buffer);
                return new String(buffer);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static LocalDate toLocalDate(String dateString) {
        return java.sql.Date.valueOf(dateString).toLocalDate();
    }
}
