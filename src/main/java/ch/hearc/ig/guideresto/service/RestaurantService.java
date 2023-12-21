package ch.hearc.ig.guideresto.service;

import ch.hearc.ig.guideresto.business.BasicEvaluation;
import ch.hearc.ig.guideresto.business.CompleteEvaluation;
import ch.hearc.ig.guideresto.business.Evaluation;
import ch.hearc.ig.guideresto.business.Restaurant;
import ch.hearc.ig.guideresto.persistence.JpaUtils;

import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class RestaurantService {

    private static RestaurantService instance;

    // Service objects in a service layer can have inter-dependencies.
    // Frameworks usually solve this by using a service container and
    // dependency injection (DI)
    public RestaurantService() {}

    public static RestaurantService getInstance() {
        if (RestaurantService.instance == null) {
            RestaurantService.instance = new RestaurantService();
        }
        return RestaurantService.instance;
    }

    public List<Restaurant> getAll() {
        return JpaUtils.getEntityManager().createQuery(
                "SELECT r FROM Restaurant r INNER JOIN FETCH r.type t INNER JOIN FETCH r.address.city c",
                Restaurant.class
        ).getResultList();
    }

    public void evaluate(Restaurant restaurant, CompleteEvaluation eval) {

        JpaUtils.inTransaction((em) -> {
            Set<Evaluation> existingEvaluations = restaurant.getEvaluations();
            existingEvaluations.add(eval);
            em.persist(eval);
        });
    }

    public void save(Restaurant restaurant) {
        JpaUtils.inTransaction((em) -> {
            em.persist(restaurant);
        });
    }

    public void delete(Restaurant restaurant) {
        restaurant.getAddress().getCity().getRestaurants().remove(restaurant);
        restaurant.getType().getRestaurants().remove(restaurant);
        JpaUtils.inTransaction((em) -> {
            em.remove(restaurant);
        });
    }

    public void addLike(Restaurant restaurant) {
        this.addBasicEval(restaurant, true);
    }

    public void addDislike(Restaurant restaurant) {
        this.addBasicEval(restaurant, false);
    }

    // it's also possible to create service methods not related to persistence
    // as long as it does not step on another layer (e.g. presentation)
    // In our case, this helps improve the presentation's layer readability
    // (which is quite cluttered already)
    public long getLikes(Restaurant restaurant) {
        return this.getBasicEvalCount(restaurant, true);
    }

    public long getDislikes(Restaurant restaurant) {
        return this.getBasicEvalCount(restaurant, false);
    }

    public static Optional<Restaurant> filterByName(List<Restaurant> restaurants, String name) {
        // Possible improvement:
        // it would be interesting to cache restaurants in the service (e.g. upon getAll calls) so that
        // 1) the first argument would not be needed
        // 2) avoid extra calls from the presentation layer to the above getAll methods (would avoid uneeded queries)
        return restaurants.stream()
            .filter(current -> current.getName().equalsIgnoreCase(name.toUpperCase()))
            .findFirst();
    }

    private void addBasicEval(Restaurant restaurant, boolean isLike) {
        JpaUtils.inTransaction((em) -> {
            BasicEvaluation eval = new BasicEvaluation(null, LocalDate.now(), restaurant, isLike, this.getIpAddress());
            restaurant.getEvaluations().add(eval);
            em.persist(eval);
        });
    }

    private long getBasicEvalCount(Restaurant restaurant, boolean isLike) {
        return restaurant.getEvaluations().stream()
                .filter(BasicEvaluation.class::isInstance)
                .map(BasicEvaluation.class::cast)
                .filter(eval -> isLike == eval.isLikeRestaurant())
                .count();
    }

    private String getIpAddress() {
        try {
            return Inet4Address.getLocalHost().toString();
        } catch (UnknownHostException ex) {
            throw new RuntimeException(ex);
        }
    }


}
