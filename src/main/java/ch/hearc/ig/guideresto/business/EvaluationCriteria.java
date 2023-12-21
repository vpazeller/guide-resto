package ch.hearc.ig.guideresto.business;

import javax.persistence.*;

@Entity
@Table(name = "CRITERES_EVALUATION")
public class EvaluationCriteria {

    @Id
    @Column(name = "NUMERO")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_CRITERES_EVALUATION")
    @SequenceGenerator(name = "SEQ_CRITERES_EVALUATION", allocationSize = 1)
    private Integer id;
    @Column(name = "NOM")
    private String name;
    @Column(name = "DESCRIPTION")
    private String description;

    // Empty constructor for Hibernate
    public EvaluationCriteria() {}

    public EvaluationCriteria(Integer id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}