package ch.hearc.ig.guideresto.business;

import java.util.Objects;

public class Localisation {

    private String street;
    private City city;

    public Localisation(String street, City city) {
        this.street = street;
        this.city = city;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public City getCity() {
        return city;
    }

    public void setCity(City city) {
        this.city = city;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        Localisation that = (Localisation) o;
        return Objects.equals(this.street, that.street)&& Objects.equals(this.city, that.city);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.street, this.city);
    }
}