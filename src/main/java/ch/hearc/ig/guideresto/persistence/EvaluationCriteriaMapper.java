package ch.hearc.ig.guideresto.persistence;

import ch.hearc.ig.guideresto.business.EvaluationCriteria;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class EvaluationCriteriaMapper {

    private static final EntityRegistry<EvaluationCriteria> registry = new EntityRegistry<>();

    private static final String QUERY_FIND_ALL = "SELECT " +
            "NUMERO, NOM, DESCRIPTION " +
            "FROM CRITERES_EVALUATION ";

    public static EntityRegistry<EvaluationCriteria> getRegistry() {
        return registry;
    }

    public static Set<EvaluationCriteria> findAll() {
        Set<EvaluationCriteria> criteria = new HashSet<>();
        List<Map<String, Object>> rows = QueryUtils.findAll(EvaluationCriteriaMapper.QUERY_FIND_ALL);
        for (Map<String, Object> row: rows) {
            Integer criterionId = ((BigDecimal) row.get("NUMERO")).intValue();
            EvaluationCriteria criterion = registry.get(criterionId).orElse(new EvaluationCriteria());
            criterion.setId(criterionId);
            criterion.setName((String) row.get("NOM"));
            criterion.setDescription((String) row.get("DESCRIPTION"));
            criteria.add(criterion);
            registry.set(criterionId, criterion);
        }
        return criteria;
    }
}
