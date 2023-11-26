package ch.hearc.ig.guideresto.persistence;

import ch.hearc.ig.guideresto.business.City;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CityMapper {

    private static final String QUERY_FIND_ALL = "SELECT " +
            "NUMERO, CODE_POSTAL, NOM_VILLE " +
            "FROM VILLES ";

    private static final String QUERY_INSERT = "INSERT INTO VILLES " +
            // optional, but adds safety if the table structure changes
            "(CODE_POSTAL, NOM_VILLE) " +
            "VALUES (?, ?)";
    public static Set<City> findAll() {
        Set<City> cities = new HashSet<>();
        List<Map<String, Object>> rows = QueryUtils.findAll(CityMapper.QUERY_FIND_ALL);
        for (Map<String, Object> row: rows) {
            City city = new City(
                ((BigDecimal) row.get("NUMERO")).intValue(),
                (String) row.get("CODE_POSTAL"),
                (String) row.get("NOM_VILLE")
            );
            cities.add(city);
        }
        return cities;
    }

    public static void insert(City city) {
        if (city.getId() != null) {
            throw new RuntimeException("City has already been persisted!");
        }
        Integer id = QueryUtils.insert(CityMapper.QUERY_INSERT, (PreparedStatement s) -> {
            try {
                s.setString(1, city.getZipCode());
                s.setString(2, city.getCityName());
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
        city.setId(id);
    }
}
