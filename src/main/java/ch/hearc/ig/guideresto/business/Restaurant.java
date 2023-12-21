package ch.hearc.ig.guideresto.business;

import javax.persistence.*;
import java.util.Set;

@Entity
@Table(name = "RESTAURANTS")
public class Restaurant {

    @Id
    @Column(name = "NUMERO")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_RESTAURANTS")
    @SequenceGenerator(name = "SEQ_RESTAURANTS", allocationSize = 1)
    private Integer id;
    @Column(name = "NOM")
    private String name;
    @Column(name="DESCRIPTION")
    private String description;
    @Column(name = "SITE_WEB")
    private String website;
    @OneToMany(mappedBy = "restaurant")
    private Set<Evaluation> evaluations;
    @Embedded
    private Localisation address;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "FK_TYPE")
    private RestaurantType type;

    // Empty constructor for Hibernate
    public Restaurant() {}

    public Restaurant(Integer id, String name, String description, String website, String street, City city, RestaurantType type) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.website = website;
        this.address = new Localisation(street, city);
        this.type = type;
    }

    public Integer getId() { return this.id; }

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
        // we don't need lazy loading logic anymore (hibernate is doing it)
        return this.evaluations;
    }

    public void setEvaluations(Set<Evaluation> evaluations) {
        this.evaluations = evaluations;
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