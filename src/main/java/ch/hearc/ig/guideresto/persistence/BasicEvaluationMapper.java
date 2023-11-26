package ch.hearc.ig.guideresto.persistence;

import ch.hearc.ig.guideresto.business.BasicEvaluation;
import ch.hearc.ig.guideresto.business.Restaurant;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class BasicEvaluationMapper {

    private static final String QUERY_BY_RESTAURANT = "SELECT " +
    "NUMERO, APPRECIATION, DATE_EVAL, ADRESSE_IP " +
    "FROM LIKES " +
    "WHERE FK_REST = ?";

    private static final String QUERY_DELETE_BY_RESTAURANT = "DELETE LIKES WHERE FK_REST = ?";

    private static final String QUERY_INSERT = "INSERT INTO LIKES " +
            // optional, but adds safety if the table structure changes
            "(APPRECIATION, DATE_EVAL, ADRESSE_IP, FK_REST) " +
            "VALUES (?, ?, ?, ?)";

    public static Set<BasicEvaluation> findByRestaurant(Restaurant restaurant) {
        Set<BasicEvaluation> likes = new HashSet<>();
        List<Map<String, Object>> rows = QueryUtils.findAllByForeignKey(BasicEvaluationMapper.QUERY_BY_RESTAURANT, restaurant.getId());
        for (Map<String, Object> row: rows) {
            BasicEvaluation like = new BasicEvaluation(
                    ((BigDecimal) row.get("NUMERO")).intValue(),
                    ((Timestamp) row.get("DATE_EVAL")).toLocalDateTime().toLocalDate(),
                    restaurant,
                    row.get("APPRECIATION").equals("T"),
                    (String) row.get("ADRESSE_IP")
            );
            likes.add(like);
        }
        return likes;
    }

    public static void insert(BasicEvaluation like) {
        if (like.getId() != null) {
            throw new RuntimeException("Like has already been persisted!");
        }
        // check restaurant is correctly set
        Restaurant restaurant = like.getRestaurant();
        if (restaurant == null || restaurant.getId() == null) {
            throw new RuntimeException("Cannot evaluate unpersisted restaurant!");
        }
        Integer id = QueryUtils.insert(BasicEvaluationMapper.QUERY_INSERT, (PreparedStatement s) -> {
            try {
                s.setString(1, like.isLikeRestaurant() ? "T" : "F");
                s.setDate(2, java.sql.Date.valueOf(like.getVisitDate()));
                s.setString(3, like.getIpAddress());
                s.setInt(4, restaurant.getId());
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
        like.setId(id);
    }

    public static void deleteForRestaurant(Restaurant restaurant) {
        QueryUtils.deleteByPkOrFk(
            BasicEvaluationMapper.QUERY_DELETE_BY_RESTAURANT,
            restaurant.getId()
        );
    }
}
