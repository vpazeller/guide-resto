package ch.hearc.ig.guideresto.business;

import java.time.LocalDate;

public class BasicEvaluation extends Evaluation {

  private boolean likeRestaurant;
  private String ipAddress;

  // Having an empty constructor is handy to work with identity maps / entity registries
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