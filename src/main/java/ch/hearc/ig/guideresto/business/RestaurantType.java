package ch.hearc.ig.guideresto.business;

import ch.hearc.ig.guideresto.persistence.RestaurantMapper;

import java.util.Set;

public class RestaurantType {

    private Integer id;
    private String label;
    private String description;
    private Set<Restaurant> restaurants;

    public RestaurantType(Integer id, String label, String description) {
        this.id = id;
        this.label = label;
        this.description = description;
        this.restaurants = null; // lazy evaluation...
    }

    public Integer getId() { return this.id; }
    
    public String getLabel() {
        return label;
    }

    public String getDescription() {
        return description;
    }

    public Set<Restaurant> getRestaurants() {
        if (this.restaurants == null) {
            this.restaurants = RestaurantMapper.findByType(this);
        }
        return this.restaurants;
    }
}