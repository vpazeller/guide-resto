package ch.hearc.ig.guideresto.business;

import ch.hearc.ig.guideresto.persistence.EvaluationMapper;

import java.util.HashSet;
import java.util.Set;

public class Restaurant {
    private Integer id;
    private String name;
    private String description;
    private String website;
    private Set<Evaluation> evaluations;
    private Localisation address;
    private RestaurantType type;

    // Having an empty constructor is handy to work with identity maps / entity registries
    public Restaurant() {
        this.evaluations = new HashSet<>();
    }

    public Restaurant(Integer id, String name, String description, String website, String street, City city, RestaurantType type) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.website = website;
        this.address = new Localisation(street, city);
        this.type = type;
        this.evaluations = null; // lazy evaluation
    }

    public Integer getId() { return this.id; }

    // ideally, this setter could be avoided by using reflection instead
    // since this is a basic solution, this is acceptable though
    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    // bad-practice: over-engineering
    // this method should be removed because it causes more trouble than help
    // it's not good to have multiple ways to reach things (decreases readability)
    // -> don't use it (i.e. don't increase dependencies on it)
    // -> instead call Restaurant.getAddress().getCity().getZipCode(); directly
    public String getZipCode() {
        return address.getCity().getZipCode();
    }

    // bad-practice: over-engineering
    // this method should be removed because it causes more trouble than help
    // it's not good to have multiple ways to reach things (decreases readability)
    // -> don't use it (i.e. don't increase dependencies on it)
    // -> instead call Restaurant.getAddress().getStreet(); directly
    public String getStreet() {
        return address.getStreet();
    }

    // bad-practice: over-engineering
    // this method should be removed because it causes more trouble than help
    // it's not good to have multiple ways to reach things (decreases readability)
    // -> don't use it (i.e. don't increase dependencies on it)
    // -> instead call Restaurant.getAddress().getCity().getCityName(); directly
    public String getCityName() {
        return address.getCity().getCityName();
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

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public Set<Evaluation> getEvaluations() {
        // lazy loading
        if (this.evaluations == null) {
            this.evaluations = EvaluationMapper.findByRestaurant(this);
        }
        return this.evaluations;
    }

    public Localisation getAddress() {
        return address;
    }

    public void setAddress(Localisation newAddress) {
        // it's a good practice to move graph complexity in setters/adders/removers
        if (this.address == null || !this.address.equals(newAddress)) {
            City oldCity = this.address == null ? null : this.address.getCity();
            City newCity = newAddress.getCity();
            if (oldCity != newCity) {
                if (oldCity != null) {
                    oldCity.getRestaurants().remove(this);
                }
                newCity.getRestaurants().add(this);
            }
            this.address = newAddress;
        }
    }

    public RestaurantType getType() {
        return type;
    }

    public void setType(RestaurantType newType) {
        // it's a good practice to move graph complexity in setters/adders/removers
        if (this.type == null || !this.type.equals(newType)) {
            if (this.type != null) {
                this.type.getRestaurants().remove(this);
            }
            this.type = newType;
            newType.getRestaurants().add(this);
        }
    }
}