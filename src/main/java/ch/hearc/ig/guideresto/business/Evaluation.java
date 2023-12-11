package ch.hearc.ig.guideresto.business;

import java.time.LocalDate;

public abstract class Evaluation {

  private Integer id;
  private LocalDate visitDate;
  private Restaurant restaurant;

  public Evaluation() {}

  public Evaluation(Integer id, LocalDate visitDate, Restaurant restaurant) {
    this.id = id;
    this.visitDate = visitDate;
    this.restaurant = restaurant;
  }

  public Integer getId() {
    return id;
  }

  // ideally, this setter could be avoided by using reflection instead
  // since this is a basic solution, this is acceptable though
  public void setId(Integer id) {
    this.id = id;
  }

  public LocalDate getVisitDate() {
    return visitDate;
  }

  public void setVisitDate(LocalDate visitDate) {
    this.visitDate = visitDate;
  }

  public Restaurant getRestaurant() {
    return restaurant;
  }

  public void setRestaurant(Restaurant restaurant) {
    this.restaurant = restaurant;
  }
}