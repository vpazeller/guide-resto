package ch.hearc.ig.guideresto.business;

public class Grade {

    private Integer id;
    private Integer grade;
    private CompleteEvaluation evaluation;
    private EvaluationCriteria criteria;

    // Having an empty constructor is handy to work with identity maps / entity registries
    public Grade() {}

    public Grade(Integer id, Integer grade, CompleteEvaluation evaluation, EvaluationCriteria criteria) {
        this.id = id;
        this.grade = grade;
        this.evaluation = evaluation;
        this.criteria = criteria;
    }

    public Integer getId() {
        return id;
    }

    // ideally, this setter could be avoided by using reflection instead
    // since this is a basic solution, this is acceptable though
    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getGrade() {
        return grade;
    }

    public void setGrade(Integer grade) {
        this.grade = grade;
    }

    public EvaluationCriteria getCriteria() {
        return criteria;
    }

    public void setCriteria(EvaluationCriteria criteria) {
        this.criteria = criteria;
    }

    public CompleteEvaluation getEvaluation() {
        return evaluation;
    }

    public void setEvaluation(CompleteEvaluation evaluation) {
        this.evaluation = evaluation;
    }
}