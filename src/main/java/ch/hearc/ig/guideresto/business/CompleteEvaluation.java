package ch.hearc.ig.guideresto.business;

import ch.hearc.ig.guideresto.persistence.GradeMapper;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

public class CompleteEvaluation extends Evaluation {

  private String comment;
  private String username;
  private Set<Grade> grades;

  // Having an empty constructor is handy to work with identity maps / entity registries
  public CompleteEvaluation() {
    super();
    this.grades = new HashSet<>();
  }

  public CompleteEvaluation(Integer id, LocalDate visitDate, Restaurant restaurant, String comment,
      String username) {
    super(id, visitDate, restaurant);
    this.comment = comment;
    this.username = username;
    this.grades = null; // lazy loading
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
    // lazy loading
    if (this.grades == null) {
      this.grades = GradeMapper.findByEvaluation(this);
    }
    return this.grades;
  }

  public void setGrades(Set<Grade> grades) {
    this.grades = grades;
  }

}