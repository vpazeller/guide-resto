package ch.hearc.ig.guideresto.service;

import ch.hearc.ig.guideresto.business.EvaluationCriteria;
import ch.hearc.ig.guideresto.persistence.EvaluationCriteriaMapper;

import java.util.Set;

public class EvaluationCriteriaService {

    private static EvaluationCriteriaService instance;

    // Service objects in a service layer can have inter-dependencies.
    // Frameworks usually solve this by using a service container and
    // dependency injection (DI)
    public EvaluationCriteriaService() {}

    public static EvaluationCriteriaService getInstance() {
        if (EvaluationCriteriaService.instance == null) {
            EvaluationCriteriaService.instance = new EvaluationCriteriaService();
        }
        return EvaluationCriteriaService.instance;
    }

    public Set<EvaluationCriteria> getAll() {
        // since we are only reading, there is no need for a transaction here
        return EvaluationCriteriaMapper.findAll();
    }

}
