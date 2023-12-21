package ch.hearc.ig.guideresto.business;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public abstract class Evaluation {

  @Id
  @Column(name = "NUMERO")
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_EVAL")
  @SequenceGenerator(name = "SEQ_EVAL", allocationSize = 1)
  private Integer id;
  @Column(name = "DATE_EVAL")
  private LocalDate visitDate;
  @ManyToOne
  @JoinColumn(name = "FK_REST")
  private Restaurant restaurant;

  // Empty constructor for Hibernate
  public Evaluation() {}

  public Evaluation(Integer id, LocalDate visitDate, Restaurant restaurant) {
    this.id = id;
    this.visitDate = visitDate;
    this.restaurant = restaurant;
  }

  public Integer getId() {
    return id;
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