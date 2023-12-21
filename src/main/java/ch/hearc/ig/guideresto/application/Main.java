package ch.hearc.ig.guideresto.application;

import ch.hearc.ig.guideresto.persistence.JpaUtils;
import ch.hearc.ig.guideresto.presentation.CLI;
import ch.hearc.ig.guideresto.service.CityService;
import ch.hearc.ig.guideresto.service.EvaluationCriteriaService;
import ch.hearc.ig.guideresto.service.RestaurantService;
import ch.hearc.ig.guideresto.service.RestaurantTypeService;

import java.io.PrintStream;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        // Rule of thumb: 1 Entity Manager per thread
        // => CLI application -> 1 EM is enough for the whole app lifecycle
        // => Web app -> 1 EM per request (~ thread)
        JpaUtils.withEntityManager((_em) -> {
            Scanner scanner = new Scanner(System.in);
            PrintStream printStream = System.out;
            var cli = new CLI(
                scanner,
                printStream,
                RestaurantService.getInstance(),
                CityService.getInstance(),
                RestaurantTypeService.getInstance(),
                EvaluationCriteriaService.getInstance()
            );
            cli.start();
        });
    }
}
