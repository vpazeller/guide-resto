package ch.hearc.ig.guideresto.business;

import javax.persistence.*;

@Entity
@Table(name = "NOTES")
public class Grade {

    @Id
    @Column(name = "NUMERO")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_NOTES")
    @SequenceGenerator(name = "SEQ_NOTES", allocationSize = 1)
    private Integer id;
    @Column(name = "NOTE")
    private Integer grade;
    @ManyToOne
    @JoinColumn(name = "FK_COMM")
    private CompleteEvaluation evaluation;
    @ManyToOne
    @JoinColumn(name = "FK_CRIT")
    private EvaluationCriteria criteria;

    // Empty constructor for Hibernate
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