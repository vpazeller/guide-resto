package ch.hearc.ig.guideresto.persistence;

import ch.hearc.ig.guideresto.business.City;
import ch.hearc.ig.guideresto.business.Localisation;
import ch.hearc.ig.guideresto.business.Restaurant;
import ch.hearc.ig.guideresto.business.RestaurantType;
import oracle.sql.CLOB;

import java.math.BigDecimal;
import java.rmi.registry.Registry;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RestaurantMapper {

    private static final EntityRegistry<Restaurant> registry = new EntityRegistry<>();

    private static final String QUERY_FIND_ALL = "SELECT " +
            "R.NUMERO AS R_NUMERO, R.NOM AS R_NOM, R.ADRESSE AS R_ADRESSE, R.DESCRIPTION AS R_DESCRIPTION, R.SITE_WEB AS R_SITE_WEB, " +
            "V.NUMERO AS V_NUMERO, V.NOM_VILLE AS V_NOM, V.CODE_POSTAL AS V_CODE, " +
            "T.NUMERO AS T_NUMERO, T.LIBELLE AS T_LABEL, T.DESCRIPTION AS T_DESCRIPTION " +
            "FROM RESTAURANTS R " +
            "LEFT JOIN VILLES V ON V.NUMERO = R.FK_VILL " +
            "LEFT JOIN TYPES_GASTRONOMIQUES T ON T.NUMERO = R.FK_TYPE ";

    private static final String QUERY_FIND_BY_TYPE = RestaurantMapper.QUERY_FIND_ALL +
            " WHERE R.FK_TYPE = ?";

    private static final String QUERY_INSERT = "INSERT INTO RESTAURANTS " +
            // optional, but adds safety if the table structure changes
            "(NOM, ADRESSE, DESCRIPTION, SITE_WEB, FK_TYPE, FK_VILL) " +
            " VALUES (?, ?, ?, ?, ?, ?)";

    private static final String QUERY_UPDATE = "UPDATE RESTAURANTS " +
            // optional, but adds safety if the table structure changes
            "SET NOM = ?, ADRESSE = ?, DESCRIPTION = ?, SITE_WEB = ?, FK_TYPE = ? , FK_VILL = ? " +
            "WHERE NUMERO = ?";

    private static final String QUERY_DELETE_BY_ID = "DELETE RESTAURANTS WHERE NUMERO = ?";

    public static Set<Restaurant> findAll() {
        List<Map<String, Object>> rows = QueryUtils.findAll(RestaurantMapper.QUERY_FIND_ALL);
        return RestaurantMapper.fetchRestaurants(rows);
    }

    public static Set<Restaurant> findByType(RestaurantType type) {
        List<Map<String, Object>> rows = QueryUtils.findAllByForeignKey(RestaurantMapper.QUERY_FIND_BY_TYPE, type.getId());
        return RestaurantMapper.fetchRestaurants(rows);
    }

    public static void delete(Restaurant restaurant) {
        Integer restaurantId = restaurant.getId();
        if (restaurantId == null) {
            throw new RuntimeException("Cannot delete unpersisted restaurant!");
        }
        // first delete dependencies:
        BasicEvaluationMapper.deleteForRestaurant(restaurant);
        CompleteEvaluationMapper.deleteForRestaurant(restaurant);

        QueryUtils.deleteByPkOrFk(RestaurantMapper.QUERY_DELETE_BY_ID, restaurantId);
        registry.delete(restaurantId);
    }
    public static void insert(Restaurant restaurant) {
        if (restaurant.getId() != null) {
            throw new RuntimeException("Restaurant has already been persisted!");
        }
        RestaurantMapper.checkRestaurant(restaurant);

        Integer id = QueryUtils.insert(RestaurantMapper.QUERY_INSERT, (PreparedStatement s) -> {
            try {
                s.setString(1, restaurant.getName());
                s.setString(2, restaurant.getAddress().getStreet());
                s.setString(3, restaurant.getDescription());
                s.setString(4, restaurant.getWebsite());
                s.setInt(5, restaurant.getType().getId());
                s.setInt(6, restaurant.getAddress().getCity().getId());
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
        restaurant.setId(id);
        registry.set(id, restaurant);
    }

    public static void update(Restaurant restaurant) {
        if (restaurant.getId() == null) {
            throw new RuntimeException("Restaurant has no been persisted!");
        }
        RestaurantMapper.checkRestaurant(restaurant);

        QueryUtils.updateById(RestaurantMapper.QUERY_UPDATE, restaurant.getId(), (PreparedStatement s) -> {
            try {
                s.setString(1, restaurant.getName());
                s.setString(2, restaurant.getAddress().getStreet());
                s.setString(3, restaurant.getDescription());
                s.setString(4, restaurant.getWebsite());
                s.setInt(5, restaurant.getType().getId());
                s.setInt(6, restaurant.getAddress().getCity().getId());
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private static void checkRestaurant(Restaurant restaurant) {
        // check dependencies are correctly persisted before saving restaurant
        RestaurantType type = restaurant.getType();
        if (type == null) {
            throw new RuntimeException("Restaurant has no type");
        }
        if (type.getId() == null) {
            throw new RuntimeException("Type is not yet persisted: " + type.getLabel());
        }
        Localisation address = restaurant.getAddress();
        if (address == null) {
            throw new RuntimeException("Restaurant has no address");
        }
        City city = address.getCity();
        if (city == null) {
            throw new RuntimeException("Restaurant has no city");
        }
        if (city.getId() == null) {
            CityMapper.insert(city);
        }
    }

    private static Set<Restaurant> fetchRestaurants(List<Map<String, Object>> rows) {
        Set<Restaurant> restaurants = new HashSet<>();
        for (Map<String, Object> row: rows) {
            // ensure no duplicate instances are created
            Integer restaurantId = ((BigDecimal) row.get("R_NUMERO")).intValue();
            Restaurant restaurant = registry.get(restaurantId).orElse(new Restaurant());

            restaurant.setId(restaurantId);
            restaurant.setName((String) row.get("R_NOM"));
            restaurant.setDescription(ResultUtils.clobToString((CLOB) row.get("R_DESCRIPTION")));
            restaurant.setWebsite((String) row.get("R_SITE_WEB"));
            Localisation address = new Localisation(
                (String) row.get("R_ADRESSE"),
                RestaurantMapper.fetchCity(row)
            );
            restaurant.setAddress(address);
            restaurant.setType(RestaurantMapper.fetchType(row));
            restaurants.add(restaurant);
            // Ensure instance is saved in the identity map
            // (if it was already there, it will be overrided to the same value -> no effect)
            registry.set(restaurantId, restaurant);
        }
        return restaurants;
    }

    private static City fetchCity(Map<String, Object> row) {
        Object cityFk = row.get("V_NUMERO");
        if (cityFk == null) {
            return null;
        }
        Integer cityId = ((BigDecimal) cityFk).intValue();
        City city = CityMapper.getRegistry().get(cityId).orElse(new City());
        city.setId(cityId);
        city.setZipCode((String) row.get("V_CODE"));
        city.setCityName((String) row.get("V_NOM"));
        // Ensure instance is saved in the identity map
        CityMapper.getRegistry().set(cityId, city);
        return city;
    }

    private static RestaurantType fetchType(Map<String, Object> row) {
        Object typeFk = row.get("T_NUMERO");
        if (typeFk == null) {
            return null;
        }
        Integer typeId = ((BigDecimal) typeFk).intValue();
        RestaurantType type = RestaurantTypeMapper.getRegistry().get(typeId)
            .orElse(new RestaurantType());
        type.setId(typeId);
        type.setLabel((String) row.get("T_LABEL"));
        type.setDescription(ResultUtils.clobToString((CLOB) row.get("T_DESCRIPTION")));
        // Ensure instance is saved in the identity map
        RestaurantTypeMapper.getRegistry().set(typeId, type);
        return type;
    }
}
