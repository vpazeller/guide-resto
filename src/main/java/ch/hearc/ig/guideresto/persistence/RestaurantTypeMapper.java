package ch.hearc.ig.guideresto.persistence;

import ch.hearc.ig.guideresto.business.City;
import ch.hearc.ig.guideresto.business.RestaurantType;
import oracle.sql.CLOB;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RestaurantTypeMapper {

    private static final EntityRegistry<RestaurantType> registry = new EntityRegistry<>();

    private static final String QUERY_ALL = "SELECT " +
    "NUMERO, LIBELLE, DESCRIPTION " +
    "FROM TYPES_GASTRONOMIQUES";

    public static EntityRegistry<RestaurantType> getRegistry() {
        return RestaurantTypeMapper.registry;
    }

    public static Set<RestaurantType> findAll() {
        Set<RestaurantType> types = new HashSet<>();
        List<Map<String, Object>> rows = QueryUtils.findAll(RestaurantTypeMapper.QUERY_ALL);
        for (Map<String, Object> row: rows) {
            Integer typeId = ((BigDecimal) row.get("NUMERO")).intValue();
            RestaurantType type = registry.get(typeId).orElse(new RestaurantType());
            type.setId(typeId);
            type.setLabel((String) row.get("LIBELLE"));
            type.setDescription(ResultUtils.clobToString((CLOB) row.get("DESCRIPTION")));
            types.add(type);
            registry.set(typeId, type);
        }
        return types;
    }
}
