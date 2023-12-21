package ch.hearc.ig.guideresto.business;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "TYPES_GASTRONOMIQUES")
public class RestaurantType {

    @Id
    @Column(name = "NUMERO")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TYPES_GASTRONOMIQUES")
    @SequenceGenerator(name = "SEQ_TYPES_GASTRONOMIQUES", allocationSize = 1)
    private Integer id;
    @Column(name = "LIBELLE")
    private String label;
    @Column(name = "DESCRIPTION")
    private String description;
    @OneToMany(mappedBy = "type")
    private Set<Restaurant> restaurants;

    // Empty constructor for Hibernate
    public RestaurantType() {}

    public RestaurantType(Integer id, String label, String description) {
        this.id = id;
        this.label = label;
        this.description = description;
    }

    public Integer getId() { return this.id; }

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
        // we don't need lazy loading logic anymore (hibernate is doing it)
        return this.restaurants;
    }
}