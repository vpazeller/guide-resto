package ch.hearc.ig.guideresto.application;

import ch.hearc.ig.guideresto.presentation.CLI;
import ch.hearc.ig.guideresto.service.CityService;
import ch.hearc.ig.guideresto.service.EvaluationCriteriaService;
import ch.hearc.ig.guideresto.service.RestaurantService;
import ch.hearc.ig.guideresto.service.RestaurantTypeService;

import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        // using of var is a bad practice (loss of readability)
        var scanner = new Scanner(System.in);
        var printStream = System.out;
        // inject service dependencies to the presentation layer:
        var cli = new CLI(
            scanner,
            printStream,
            RestaurantService.getInstance(),
            CityService.getInstance(),
            RestaurantTypeService.getInstance(),
            EvaluationCriteriaService.getInstance()
        );

        cli.start();
    }
}
