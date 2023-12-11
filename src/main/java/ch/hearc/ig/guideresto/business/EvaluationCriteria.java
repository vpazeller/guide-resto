package ch.hearc.ig.guideresto.business;

public class EvaluationCriteria {

    private Integer id;
    private String name;
    private String description;

    // Having an empty constructor is handy to work with identity maps / entity registries
    public EvaluationCriteria() {}

    public EvaluationCriteria(Integer id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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