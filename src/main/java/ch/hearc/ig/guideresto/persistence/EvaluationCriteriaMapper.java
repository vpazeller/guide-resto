package ch.hearc.ig.guideresto.persistence;

import ch.hearc.ig.guideresto.business.City;
import ch.hearc.ig.guideresto.business.EvaluationCriteria;
import oracle.sql.CLOB;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class EvaluationCriteriaMapper {

    private static final String QUERY_FIND_ALL = "SELECT " +
            "NUMERO, NOM, DESCRIPTION " +
            "FROM CRITERES_EVALUATION ";
    public static Set<EvaluationCriteria> findAll() {
        Set<EvaluationCriteria> criteria = new HashSet<>();
        List<Map<String, Object>> rows = QueryUtils.findAll(EvaluationCriteriaMapper.QUERY_FIND_ALL);
        for (Map<String, Object> row: rows) {
            EvaluationCriteria criterion = new EvaluationCriteria(
                ((BigDecimal) row.get("NUMERO")).intValue(),
                (String) row.get("NOM"),
                (String) row.get("DESCRIPTION")
            );
            criteria.add(criterion);
        }
        return criteria;
    }
}
