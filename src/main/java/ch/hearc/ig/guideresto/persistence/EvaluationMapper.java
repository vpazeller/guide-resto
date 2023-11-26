package ch.hearc.ig.guideresto.persistence;

import ch.hearc.ig.guideresto.business.Evaluation;
import ch.hearc.ig.guideresto.business.Restaurant;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class EvaluationMapper {
    public static Set<Evaluation> findByRestaurant(Restaurant restaurant) {
        // See https://stackoverflow.com/questions/2745265/is-listdog-a-subclass-of-listanimal-why-are-java-generics-not-implicitly-po
        Set<? extends Evaluation> likes = BasicEvaluationMapper.findByRestaurant(restaurant);
        Set<? extends Evaluation> comments = CompleteEvaluationMapper.findByRestaurant(restaurant);
        return Stream.concat(likes.stream(),comments.stream()).collect(Collectors.toSet());
    }
}
