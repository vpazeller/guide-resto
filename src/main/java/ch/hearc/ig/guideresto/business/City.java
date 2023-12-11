package ch.hearc.ig.guideresto.business;

import java.util.HashSet;
import java.util.Set;

public class City {
    private Integer id;
    private String zipCode;
    // naming: name (i.s.o cityName) would have been enough since the Class name
    // already gives context
    private String cityName;
    private Set<Restaurant> restaurants = new HashSet<>();

    // Having an empty constructor is handy to work with identity maps / entity registries
    public City() {
    }

    public City(Integer id, String zipCode, String cityName) {
        this.id = id;
        this.zipCode = zipCode;
        this.cityName = cityName;
    }

    public Integer getId() {
        return id;
    }

    // ideally, this setter could be avoided by using reflection instead
    // since this is a basic solution, this is acceptable though
    public void setId(Integer id) {
        this.id = id;
    }

    public String getZipCode() {
        return zipCode;
    }

    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public Set<Restaurant> getRestaurants() {
        return restaurants;
    }
}