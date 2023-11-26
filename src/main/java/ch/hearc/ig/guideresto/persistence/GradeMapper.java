package ch.hearc.ig.guideresto.persistence;

import ch.hearc.ig.guideresto.business.*;
import oracle.sql.CLOB;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class GradeMapper {

    private static final String QUERY_FIND_BY_EVALUATION = "SELECT " +
            "N.NUMERO AS N_NUMERO, N.NOTE AS N_NOTE, N.FK_CRIT AS N_FK_CRIT, N.FK_COMM AS N_FK_COMM, " +
            "C.NOM AS C_NOM, C.DESCRIPTION AS C_DESCRIPTION " +
            "FROM NOTES N " +
            "LEFT JOIN CRITERES_EVALUATION C ON C.NUMERO = N.FK_CRIT " +
            "WHERE N.FK_COMM = ?";

    private static final String QUERY_INSERT = "INSERT INTO NOTES " +
            // optional, but adds safety if the table structure changes
            "(NOTE, FK_COMM, FK_CRIT) " +
            "VALUES (?, ?, ?)";

    private static final String QUERY_DELETE_BY_EVALUATION = "DELETE NOTES WHERE FK_COMM = ?";

    public static Set<Grade> findByEvaluation(CompleteEvaluation evaluation) {
        List<Map<String, Object>> rows = QueryUtils.findAllByForeignKey(GradeMapper.QUERY_FIND_BY_EVALUATION, evaluation.getId());
        Set<Grade> grades = new HashSet<>();
        for (Map<String, Object> row: rows) {
            Grade grade = new Grade(
                ((BigDecimal) row.get("N_NUMERO")).intValue(),
                    ((BigDecimal) row.get("N_NOTE")).intValue(),
                evaluation,
                GradeMapper.fetchCriterion(row)
            );
            grades.add(grade);
        }
        return grades;
    }

    public static void insert(Grade grade) {
        CompleteEvaluation eval = grade.getEvaluation();
        EvaluationCriteria criterion = grade.getCriteria();
        if (eval == null) {
            throw new RuntimeException("Grade is orphan (not bound to an evaluation)!");
        }
        if (eval.getId() == null) {
            throw new RuntimeException("Grade's evaluation must be persisted!");
        }
        if (criterion == null) {
            throw new RuntimeException("Grade is not bound to a criterion!");
        }
        if (criterion.getId() == null) {
            throw new RuntimeException("Grade's criterion must be persisted!");
        }
        if (grade.getId() != null) {
            throw new RuntimeException("Grade has already been persisted!");
        }
        Integer id = QueryUtils.insert(GradeMapper.QUERY_INSERT, (PreparedStatement s) -> {
            try {
                s.setInt(1, grade.getGrade());
                s.setInt(2, eval.getId());
                s.setInt(3, criterion.getId());
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
        grade.setId(id);
    }

    public static void deleteForEvaluation(CompleteEvaluation evaluation) {
        QueryUtils.deleteByPkOrFk(
            GradeMapper.QUERY_DELETE_BY_EVALUATION,
            evaluation.getId()
        );
    }

    private static EvaluationCriteria fetchCriterion(Map<String, Object> row) {
        Object criterionPk = row.get("N_FK_CRIT");
        if (criterionPk == null) {
            return null;
        }
        // TODO: use registry to avoid duplicate instances
        return new EvaluationCriteria(
            ((BigDecimal) criterionPk).intValue(),
            (String) row.get("C_NOM"),
            (String) row.get("C_DESCRIPTION")
        );
    }
}
