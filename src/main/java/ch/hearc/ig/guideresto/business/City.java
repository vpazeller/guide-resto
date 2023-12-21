package ch.hearc.ig.guideresto.business;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "VILLES")
public class City {
    @Id
    @Column(name = "NUMERO")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_VILLES")
    @SequenceGenerator(name = "SEQ_VILLES", allocationSize = 1)
    private Integer id;
    @Column(name = "CODE_POSTAL")
    private String zipCode;
    // naming: name (i.s.o cityName) would have been enough since the Class name
    // already gives context
    @Column(name = "NOM_VILLE")
    private String cityName;
    @OneToMany(mappedBy = "address.city")
    private Set<Restaurant> restaurants = new HashSet<>();

    // Empty constructor for Hibernate
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