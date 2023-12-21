package ch.hearc.ig.guideresto.service;

import ch.hearc.ig.guideresto.business.RestaurantType;
import ch.hearc.ig.guideresto.persistence.JpaUtils;

import java.util.List;
import java.util.Optional;

public class RestaurantTypeService {

    private static RestaurantTypeService instance;

    // Service objects in a service layer can have inter-dependencies.
    // Frameworks usually solve this by using a service container and
    // dependency injection (DI)
    public RestaurantTypeService() {}

    public static RestaurantTypeService getInstance() {
        if (RestaurantTypeService.instance == null) {
            RestaurantTypeService.instance = new RestaurantTypeService();
        }
        return RestaurantTypeService.instance;
    }

    public List<RestaurantType> getAll() {
        return JpaUtils.getEntityManager().createQuery(
                "SELECT t FROM RestaurantType t",
                RestaurantType.class
        ).getResultList();
    }

    // it's also possible to create service methods not related to persistence
    // as long as it does not step on another layer (e.g. presentation)
    // In our case, this helps improve the presentation's layer readability
    // (which is quite cluttered already)
    public static Optional<RestaurantType> filterByLabel(List<RestaurantType> types, String label) {
        // Possible improvement:
        // it would be interesting to cache restaurant types in the service so that
        // 1) the first argument would not be needed
        // 2) avoid extra calls from the presentation layer to the above getAll methods (would avoid uneeded queries)
        return types.stream()
            .filter(current -> current.getLabel().equalsIgnoreCase(label.toUpperCase()))
            .findFirst();
    }


}
