package ch.hearc.ig.guideresto.business;

import javax.persistence.*;
import java.util.Objects;

@Embeddable
public class Localisation {

    @Column(name = "ADRESSE")
    private String street;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "FK_VILL")
    private City city;

    // Empty constructor for Hibernate
    public Localisation() {}

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