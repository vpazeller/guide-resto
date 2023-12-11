package ch.hearc.ig.guideresto.persistence;

import ch.hearc.ig.guideresto.business.CompleteEvaluation;
import ch.hearc.ig.guideresto.business.EvaluationCriteria;
import ch.hearc.ig.guideresto.business.Grade;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class GradeMapper {

    private static final EntityRegistry<Grade> registry = new EntityRegistry<>();

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
            Integer gradeId = ((BigDecimal) row.get("N_NUMERO")).intValue();
            Grade grade = registry.get(gradeId).orElse(new Grade());
            grade.setId(gradeId);
            grade.setGrade(((BigDecimal) row.get("N_NOTE")).intValue());
            grade.setEvaluation(evaluation);
            grade.setCriteria(GradeMapper.fetchCriterion(row));
            grades.add(grade);
            registry.set(gradeId, grade);
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
        registry.set(id, grade);
    }

    public static void deleteForEvaluation(CompleteEvaluation evaluation) {
        QueryUtils.deleteByPkOrFk(
            GradeMapper.QUERY_DELETE_BY_EVALUATION,
            evaluation.getId()
        );
        for (Grade grade: evaluation.getGrades()) {
            registry.delete(grade.getId());
        }
    }

    private static EvaluationCriteria fetchCriterion(Map<String, Object> row) {
        Object criterionPk = row.get("N_FK_CRIT");
        if (criterionPk == null) {
            return null;
        }
        Integer criterionId = ((BigDecimal) criterionPk).intValue();
        EvaluationCriteria criterion = EvaluationCriteriaMapper.getRegistry().get(criterionId)
            .orElse(new EvaluationCriteria());
        criterion.setId(criterionId);
        criterion.setName((String) row.get("C_NOM"));
        criterion.setDescription((String) row.get("C_DESCRIPTION"));
        // Ensure instance is saved in the identity map
        EvaluationCriteriaMapper.getRegistry().set(criterionId, criterion);
        return criterion;
    }
}
