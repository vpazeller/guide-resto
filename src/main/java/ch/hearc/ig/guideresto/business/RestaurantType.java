package ch.hearc.ig.guideresto.business;

import ch.hearc.ig.guideresto.persistence.RestaurantMapper;

import java.util.HashSet;
import java.util.Set;

public class RestaurantType {

    private Integer id;
    private String label;
    private String description;
    private Set<Restaurant> restaurants = null; // lazy evaluation...

    // Having an empty constructor is handy to work with identity maps / entity registries
    public RestaurantType() {
        this.restaurants = new HashSet<>();
    }

    public RestaurantType(Integer id, String label, String description) {
        this.id = id;
        this.label = label;
        this.description = description;
        this.restaurants = null; // lazy evaluation...
    }

    public Integer getId() { return this.id; }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Set<Restaurant> getRestaurants() {
        if (this.restaurants == null) {
            this.restaurants = RestaurantMapper.findByType(this);
        }
        return this.restaurants;
    }
}