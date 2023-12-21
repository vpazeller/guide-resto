package ch.hearc.ig.guideresto.business;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.time.LocalDate;
import java.util.Set;

@Entity
@Table(name = "COMMENTAIRES")
public class CompleteEvaluation extends Evaluation {

  @Column(name = "COMMENTAIRE")
  private String comment;
  @Column(name = "NOM_UTILISATEUR")
  private String username;
  @OneToMany(mappedBy = "evaluation")
  private Set<Grade> grades;

  // Empty constructor for Hibernate
  public CompleteEvaluation() {
    super();
  }

  public CompleteEvaluation(Integer id, LocalDate visitDate, Restaurant restaurant, String comment,
      String username) {
    super(id, visitDate, restaurant);
    this.comment = comment;
    this.username = username;
  }

  public String getComment() {
    return comment;
  }

  public void setComment(String comment) {
    this.comment = comment;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public Set<Grade> getGrades() {
    //  we don't need lazy loading logic anymore (hibernate is handling it)
    return this.grades;
  }

  public void setGrades(Set<Grade> grades) {
    this.grades = grades;
  }

}