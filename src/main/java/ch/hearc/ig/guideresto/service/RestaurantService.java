package ch.hearc.ig.guideresto.service;

import ch.hearc.ig.guideresto.business.BasicEvaluation;
import ch.hearc.ig.guideresto.business.CompleteEvaluation;
import ch.hearc.ig.guideresto.business.Evaluation;
import ch.hearc.ig.guideresto.business.Restaurant;
import ch.hearc.ig.guideresto.persistence.BasicEvaluationMapper;
import ch.hearc.ig.guideresto.persistence.CompleteEvaluationMapper;
import ch.hearc.ig.guideresto.persistence.ConnectionUtils;
import ch.hearc.ig.guideresto.persistence.RestaurantMapper;

import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.time.LocalDate;
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

    public Set<Restaurant> getAll() {
        // since we are only reading, there is no need for a transaction here
        return RestaurantMapper.findAll();
    }

    public void evaluate(Restaurant restaurant, CompleteEvaluation eval) {
        Set<Evaluation> existingEvaluations = restaurant.getEvaluations();
        // let's make it a bit more robust and avoid problems with misusage (duplicates)
        if (!existingEvaluations.contains(eval)) {
            existingEvaluations.add(eval);
        }

        // let's keep the transaction short and insert everything at the end:
        ConnectionUtils.inTransaction(() -> {
            CompleteEvaluationMapper.insert(eval);
        });
    }

    public void save(Restaurant restaurant) {
        ConnectionUtils.inTransaction(() -> {
            // let's have a clever service which guesses
            // what's need to be done (totally optional)
            if (restaurant.getId() == null) {
                RestaurantMapper.insert(restaurant);
            } else {
                RestaurantMapper.update(restaurant);
            }
        });
    }

    public void delete(Restaurant restaurant) {
        restaurant.getAddress().getCity().getRestaurants().remove(restaurant);
        restaurant.getType().getRestaurants().remove(restaurant);
        ConnectionUtils.inTransaction(() -> {
            RestaurantMapper.delete(restaurant);
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

    public static Optional<Restaurant> filterByName(Set<Restaurant> restaurants, String name) {
        // Possible improvement:
        // it would be interesting to cache restaurants in the service (e.g. upon getAll calls) so that
        // 1) the first argument would not be needed
        // 2) avoid extra calls from the presentation layer to the above getAll methods (would avoid uneeded queries)
        return restaurants.stream()
            .filter(current -> current.getName().equalsIgnoreCase(name.toUpperCase()))
            .findFirst();
    }

    private void addBasicEval(Restaurant restaurant, boolean isLike) {
        BasicEvaluation eval = new BasicEvaluation(null, LocalDate.now(), restaurant, isLike, this.getIpAddress());
        restaurant.getEvaluations().add(eval);
        ConnectionUtils.inTransaction(() -> {
            BasicEvaluationMapper.insert(eval);
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
