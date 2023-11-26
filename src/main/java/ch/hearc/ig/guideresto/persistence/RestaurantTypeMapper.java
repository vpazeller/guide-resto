package ch.hearc.ig.guideresto.persistence;

import ch.hearc.ig.guideresto.business.RestaurantType;
import oracle.sql.CLOB;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RestaurantTypeMapper {

    private static final String QUERY_ALL = "SELECT " +
    "NUMERO, LIBELLE, DESCRIPTION " +
    "FROM TYPES_GASTRONOMIQUES";
    public static Set<RestaurantType> findAll() {
        Set<RestaurantType> types = new HashSet<>();
        List<Map<String, Object>> rows = QueryUtils.findAll(RestaurantTypeMapper.QUERY_ALL);
        for (Map<String, Object> row: rows) {
            RestaurantType type = new RestaurantType(
                ((BigDecimal) row.get("NUMERO")).intValue(),
                (String) row.get("LIBELLE"),
                ResultUtils.clobToString((CLOB) row.get("DESCRIPTION"))
            );
            types.add(type);
        }
        return types;
    }
}
