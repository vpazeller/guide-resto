package ch.hearc.ig.guideresto.service;

import ch.hearc.ig.guideresto.business.EvaluationCriteria;
import ch.hearc.ig.guideresto.persistence.JpaUtils;

import java.util.List;

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

    public List<EvaluationCriteria> getAll() {
        return JpaUtils.getEntityManager().createQuery(
                "SELECT c FROM EvaluationCriteria c",
                EvaluationCriteria.class
        ).getResultList();
    }

}
