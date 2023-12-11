package ch.hearc.ig.guideresto.persistence;

import ch.hearc.ig.guideresto.business.CompleteEvaluation;
import ch.hearc.ig.guideresto.business.Evaluation;
import ch.hearc.ig.guideresto.business.Grade;
import ch.hearc.ig.guideresto.business.Restaurant;
import oracle.sql.CLOB;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CompleteEvaluationMapper {

    private static final EntityRegistry<CompleteEvaluation> registry = new EntityRegistry<>();

    private static final String QUERY_DELETE_BY_RESTAURANT = "DELETE COMMENTAIRES WHERE FK_REST = ?";

    private static final String QUERY_BY_RESTAURANT = "SELECT " +
        "NUMERO, COMMENTAIRE, DATE_EVAL, NOM_UTILISATEUR " +
        "FROM COMMENTAIRES " +
        "WHERE FK_REST = ?";

    private static final String QUERY_INSERT = "INSERT INTO COMMENTAIRES " +
        // optional, but adds safety if the table structure changes
        "(COMMENTAIRE, DATE_EVAL, NOM_UTILISATEUR, FK_REST) " +
        "VALUES (?, ?, ?, ?)";

    public static Set<CompleteEvaluation> findByRestaurant(Restaurant restaurant) {
        Set<CompleteEvaluation> comments = new HashSet<>();
        List<Map<String, Object>> rows = QueryUtils.findAllByForeignKey(CompleteEvaluationMapper.QUERY_BY_RESTAURANT, restaurant.getId());
        for (Map<String, Object> row: rows) {
            Integer commentId = ((BigDecimal) row.get("NUMERO")).intValue();
            CompleteEvaluation comment = registry.get(commentId).orElse(new CompleteEvaluation());
            comment.setId(commentId);
            comment.setVisitDate(((Timestamp) row.get("DATE_EVAL")).toLocalDateTime().toLocalDate());
            comment.setRestaurant(restaurant);
            comment.setComment(ResultUtils.clobToString((CLOB) row.get("COMMENTAIRE")));
            comment.setUsername((String) row.get("NOM_UTILISATEUR"));
            comments.add(comment);
            registry.set(commentId, comment);
        }
        return comments;
    }

    public static void insert(CompleteEvaluation comment) {
        if (comment.getId() != null) {
            throw new RuntimeException("City has already been inserted!");
        }
        // check restaurant is correctly set
        Restaurant restaurant = comment.getRestaurant();
        if (restaurant == null || restaurant.getId() == null) {
            throw new RuntimeException("Cannot comment on unpersisted restaurant!");
        }
        Integer id = QueryUtils.insert(CompleteEvaluationMapper.QUERY_INSERT, (PreparedStatement s) -> {
            try {
                s.setString(1, comment.getComment());
                s.setDate(2, java.sql.Date.valueOf(comment.getVisitDate()));
                s.setString(3, comment.getUsername());
                s.setInt(4, restaurant.getId());
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
        comment.setId(id);

        // also insert grades:
        for (Grade grade: comment.getGrades()) {
            GradeMapper.insert(grade);
        }

        registry.set(id, comment);
    }

    public static void deleteForRestaurant(Restaurant restaurant) {
        // First delete dependencies
        for (Evaluation evaluation: restaurant.getEvaluations()) {
            if (evaluation instanceof CompleteEvaluation) {
                GradeMapper.deleteForEvaluation((CompleteEvaluation) evaluation);
                registry.delete(evaluation.getId());
            }
        }

        QueryUtils.deleteByPkOrFk(
            CompleteEvaluationMapper.QUERY_DELETE_BY_RESTAURANT,
            restaurant.getId()
        );
    }
}
