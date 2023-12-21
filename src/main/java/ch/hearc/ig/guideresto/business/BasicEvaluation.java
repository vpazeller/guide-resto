package ch.hearc.ig.guideresto.business;

import ch.hearc.ig.guideresto.persistence.BooleanConverter;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.time.LocalDate;

@Entity
@Table(name = "LIKES")
public class BasicEvaluation extends Evaluation {

  @Column(name = "APPRECIATION")
  @Convert(converter = BooleanConverter.class)
  private boolean likeRestaurant;
  @Column(name = "ADRESSE_IP")
  private String ipAddress;

  // Empty constructor for Hibernate
  public BasicEvaluation() {
    super();
  }

  public BasicEvaluation(Integer id, LocalDate visitDate, Restaurant restaurant, boolean likeRestaurant,
      String ipAddress) {
    super(id, visitDate, restaurant);
    this.likeRestaurant = likeRestaurant;
    this.ipAddress = ipAddress;
  }

  public Boolean isLikeRestaurant() {
    return likeRestaurant;
  }

  public void setLikeRestaurant(boolean likeRestaurant) {
    this.likeRestaurant = likeRestaurant;
  }

  public String getIpAddress() {
    return ipAddress;
  }

  public void setIpAddress(String ipAddress) {
    this.ipAddress = ipAddress;
  }
}