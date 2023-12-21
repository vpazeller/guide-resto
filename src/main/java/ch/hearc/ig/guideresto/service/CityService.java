package ch.hearc.ig.guideresto.service;

import ch.hearc.ig.guideresto.business.City;
import ch.hearc.ig.guideresto.persistence.JpaUtils;

import java.util.List;
import java.util.Optional;

public class CityService {

    private static CityService instance;

    // Service objects in a service layer can have inter-dependencies.
    // Frameworks usually solve this by using a service container and
    // dependency injection (DI)
    public CityService() {}

    public static CityService getInstance() {
        if (CityService.instance == null) {
            CityService.instance = new CityService();
        }
        return CityService.instance;
    }

    public List<City> getAll() {
        return JpaUtils.getEntityManager().createQuery(
                "SELECT c FROM City c",
                City.class
        ).getResultList();
    }

    // it's also possible to create service methods not related to persistence
    // as long as it does not step on another layer (e.g. presentation)
    // In our case, this helps improve the presentation's layer readability
    // (which is quite cluttered already)
    public static Optional<City> filterByZipCode(List<City> cities, String zipCode) {
        // Possible improvement:
        // it would be interesting to cache cities in the service so that
        // 1) the first argument would not be needed
        // 2) avoid extra calls from the presentation layer to the above getAll methods (would avoid uneeded queries)
        return cities.stream()
            .filter(current -> current.getZipCode().equalsIgnoreCase(zipCode.toUpperCase()))
            .findFirst();
    }


}
