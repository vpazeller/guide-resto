package ch.hearc.ig.guideresto.presentation;

import ch.hearc.ig.guideresto.business.*;
import ch.hearc.ig.guideresto.service.CityService;
import ch.hearc.ig.guideresto.service.EvaluationCriteriaService;
import ch.hearc.ig.guideresto.service.RestaurantService;
import ch.hearc.ig.guideresto.service.RestaurantTypeService;

import java.io.PrintStream;
import java.time.LocalDate;
import java.util.*;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toUnmodifiableSet;

public class CLI {

  private final Scanner scanner;
  private final PrintStream printStream;

  private final RestaurantService restaurantService;
  private final CityService cityService;
  private final RestaurantTypeService restaurantTypeService;
  private final EvaluationCriteriaService evaluationCriteriaService;

  // Injection de dépendances
  public CLI(
          Scanner scanner,
          PrintStream printStream,
          RestaurantService restaurantService,
          CityService cityService,
          RestaurantTypeService restaurantTypeService,
          EvaluationCriteriaService evaluationCriteriaService
  ) {
    this.scanner = scanner;
    this.printStream = printStream;
    this.restaurantService = restaurantService;
    this.cityService = cityService;
    this.restaurantTypeService = restaurantTypeService;
    this.evaluationCriteriaService = evaluationCriteriaService;
  }

  public void start() {
    println("Bienvenue dans GuideResto ! Que souhaitez-vous faire ?");
    int choice;
    do {
      printMainMenu();
      choice = readInt();
      proceedMainMenu(choice);
    } while (choice != 0);
  }

  private void printMainMenu() {
    println("======================================================");
    println("Que voulez-vous faire ?");
    println("1. Afficher la liste de tous les restaurants");
    println("2. Rechercher un restaurant par son nom");
    println("3. Rechercher un restaurant par ville");
    println("4. Rechercher un restaurant par son type de cuisine");
    println("5. Saisir un nouveau restaurant");
    println("0. Quitter l'application");
  }

  private void proceedMainMenu(int choice) {
    switch (choice) {
      case 1:
        showRestaurantsList();
        break;
      case 2:
        searchRestaurantByName();
        break;
      case 3:
        searchRestaurantByCity();
        break;
      case 4:
        searchRestaurantByType();
        break;
      case 5:
        addNewRestaurant();
        break;
      case 0:
        println("Au revoir !");
        break;
      default:
        println("Erreur : saisie incorrecte. Veuillez réessayer");
        break;
    }
  }

  private Optional<Restaurant> pickRestaurant(Set<ch.hearc.ig.guideresto.business.Restaurant> restaurants) {
    if (restaurants.isEmpty()) {
      println("Aucun restaurant n'a été trouvé !");
      return Optional.empty();
    }

    String restaurantsText = restaurants.stream()
        .map(r -> "\"" + r.getName() + "\" - " + r.getStreet() + " - "
            + r.getZipCode() + " " + r.getCityName())
        .collect(joining("\n", "", "\n"));

    println(restaurantsText);
    println(
        "Veuillez saisir le nom exact du restaurant dont vous voulez voir le détail, ou appuyez sur Enter pour revenir en arrière");
    String choice = readString();

    return RestaurantService.filterByName(restaurants, choice);
  }

  private void showRestaurantsList() {
    println("Liste des restaurants : ");

    Set<Restaurant> restaurants = this.restaurantService.getAll();

    Optional<Restaurant> maybeRestaurant = pickRestaurant(restaurants);
    // Si l'utilisateur a choisi un restaurant, on l'affiche, sinon on ne fait rien et l'application va réafficher le menu principal
    maybeRestaurant.ifPresent(this::showRestaurant);
  }

  private void searchRestaurantByName() {
    println("Veuillez entrer une partie du nom recherché : ");
    String research = readString();

    Set<Restaurant> restaurants = this.restaurantService.getAll()
        .stream()
        .filter(r -> r.getName().equalsIgnoreCase(research))
        .collect(toUnmodifiableSet());

    Optional<Restaurant> maybeRestaurant = pickRestaurant(restaurants);
    maybeRestaurant.ifPresent(this::showRestaurant);
  }

  /**
   * Affiche une liste de restaurants dont le nom de la ville contient une chaîne de caractères
   * saisie par l'utilisateur
   */
  private void searchRestaurantByCity() {
    println("Veuillez entrer une partie du nom de la ville désirée : ");
    String research = readString();

    // Another (better) option could be to have two finder methods:
    // 1. CityMapper.findOneByName(String name) to find a City Entity by its name (case insensitively)
    // 2. RestaurantMapper.findByCity(City city) to find all restaurants in a city
    // The option below has been selected in the solution simply because it less intrusive in the CLI's code
    // (less code modifications)
    Set<Restaurant> restaurants = this.restaurantService.getAll()
        .stream()
        .filter(r -> r.getAddress().getCity().getCityName().toUpperCase().contains(research.toUpperCase()))
        .collect(toUnmodifiableSet());

    Optional<Restaurant> maybeRestaurant = pickRestaurant(restaurants);
    maybeRestaurant.ifPresent(this::showRestaurant);
  }

  private City pickCity(Set<City> cities) {
    println(
        "Voici la liste des villes possibles, veuillez entrer le NPA de la ville désirée : ");

    cities.forEach((currentCity) -> System.out
        .println(currentCity.getZipCode() + " " + currentCity.getCityName()));
    println("Entrez \"NEW\" pour créer une nouvelle ville");
    String choice = readString();

    if (choice.equalsIgnoreCase("NEW")) {
      println("Veuillez entrer le NPA de la nouvelle ville : ");
      String zipCode = readString();
      println("Veuillez entrer le nom de la nouvelle ville : ");
      String cityName = readString();
      return new City(null, zipCode, cityName);
    }

    return CityService.filterByZipCode(cities, choice).orElseGet(() -> pickCity(cities));
  }

  private RestaurantType pickRestaurantType(Set<RestaurantType> types) {

    String typesText = types.stream()
        .map(currentType -> "\"" + currentType.getLabel() + "\" : " + currentType.getDescription())
        .collect(joining("\n"));

    println(
        "Voici la liste des types possibles, veuillez entrer le libellé exact du type désiré : ");
    println(typesText);
    String choice = readString();

    Optional<RestaurantType> maybeRestaurantType = RestaurantTypeService.filterByLabel(types, choice);
    return maybeRestaurantType.orElseGet(() -> pickRestaurantType(types));
  }

  private void searchRestaurantByType() {
    Set<RestaurantType> restaurantTypes = this.restaurantTypeService.getAll();
    RestaurantType chosenType = pickRestaurantType(restaurantTypes);

    // Another (better) option could be to have two finder methods:
    // 1. RestaurantTypeMapper.findOneByName(String name) to find a RestaurantType Entity by its name (case insensitively)
    // 2. RestaurantMapper.findByType(RestaurantType type) to find all restaurants of a given type
    // The option below has been selected in the solution simply because it less intrusive in the CLI's code
    // (less code modifications)
    Set<Restaurant> restaurants = this.restaurantService.getAll()
        .stream()
        .filter(r -> r.getType().getLabel().equalsIgnoreCase(chosenType.getLabel()))
        .collect(toUnmodifiableSet());

    Optional<Restaurant> maybeRestaurant = pickRestaurant(restaurants);
    maybeRestaurant.ifPresent(this::showRestaurant);
  }

  private void addNewRestaurant() {
    println("Vous allez ajouter un nouveau restaurant !");
    println("Quel est son nom ?");
    String name = readString();
    println("Veuillez entrer une courte description : ");
    String description = readString();
    println("Veuillez entrer l'adresse de son site internet : ");
    String website = readString();
    println("Rue : ");
    String street = readString();
    City city;
    Set<City> cities = this.cityService.getAll();
    do
    { // La sélection d'une ville est obligatoire, donc l'opération se répètera tant qu'aucune ville n'est sélectionnée.
      city = pickCity(cities);
    } while (city == null);

    RestaurantType restaurantType;

    // La sélection d'un type est obligatoire, donc l'opération se répètera tant qu'aucun type n'est sélectionné.
    Set<RestaurantType> restaurantTypes = this.restaurantTypeService.getAll();
    restaurantType = pickRestaurantType(restaurantTypes);

    Restaurant restaurant = new Restaurant(null, name, description, website, street, city,
        restaurantType);
    restaurant.getAddress().setCity(city);

    this.restaurantService.save(restaurant);

    showRestaurant(restaurant);
  }

  private void showRestaurant(Restaurant restaurant) {
    String sb = restaurant.getName() + "\n" +
        restaurant.getDescription() + "\n" +
        restaurant.getType().getLabel() + "\n" +
        restaurant.getWebsite() + "\n" +
        restaurant.getAddress().getStreet() + ", " +
        restaurant.getAddress().getCity().getZipCode() + " " + restaurant.getAddress().getCity()
        .getCityName() + "\n" +
        "Nombre de likes : " + this.restaurantService.getLikes(restaurant) + "\n" +
        "Nombre de dislikes : " + this.restaurantService.getDislikes(restaurant) + "\n" +
        "\nEvaluations reçues : " + "\n";

    String text = restaurant.getEvaluations()
        .stream()
        .filter(CompleteEvaluation.class::isInstance)
        .map(CompleteEvaluation.class::cast)
        .map(this::getCompleteEvaluationDescription)
        .collect(joining("\n"));

    println("Affichage d'un restaurant : ");
    println(sb);
    println(text);

    int choice;
    do { // Tant que l'utilisateur n'entre pas 0 ou 6, on lui propose à nouveau les actions
      showRestaurantMenu();
      choice = readInt();
      proceedRestaurantMenu(choice, restaurant);
    } while (choice != 0 && choice != 6); // 6 car le restaurant est alors supprimé...
  }

  private String getCompleteEvaluationDescription(CompleteEvaluation eval) {
    String result = "Evaluation de : " + eval.getUsername() + "\n";
    result += "Commentaire : " + eval.getComment() + "\n";

    return result + eval.getGrades().stream()
        .map(g-> g.getCriteria().getName() + " : " + g.getGrade() + "/5")
        .collect(joining("\n", "\n", "\n"));
  }

  private void showRestaurantMenu() {
    println("======================================================");
    println("Que souhaitez-vous faire ?");
    println("1. J'aime ce restaurant !");
    println("2. Je n'aime pas ce restaurant !");
    println("3. Faire une évaluation complète de ce restaurant !");
    println("4. Editer ce restaurant");
    println("5. Editer l'adresse du restaurant");
    println("6. Supprimer ce restaurant");
    println("0. Revenir au menu principal");
  }

  private void proceedRestaurantMenu(int choice, Restaurant restaurant) {
    switch (choice) {
      case 1:
        this.restaurantService.addLike(restaurant);
        println("Votre vote a été pris en compte !");
        break;
      case 2:
        this.restaurantService.addDislike(restaurant);
        println("Votre vote a été pris en compte !");
        break;
      case 3:
        evaluateRestaurant(restaurant);
        break;
      case 4:
        editRestaurant(restaurant);
        break;
      case 5:
        editRestaurantAddress(restaurant);
        break;
      case 6:
        deleteRestaurant(restaurant);
        break;
      case 0:
      default:
        break;
    }
  }

  private void evaluateRestaurant(Restaurant restaurant) {
    println("Merci d'évaluer ce restaurant !");
    println("Quel est votre nom d'utilisateur ? ");
    String username = readString();
    println("Quel commentaire aimeriez-vous publier ?");
    String comment = readString();

    println("Veuillez svp donner une note entre 1 et 5 pour chacun de ces critères : ");

    Set<EvaluationCriteria> evaluationCriterias = this.evaluationCriteriaService.getAll();

    CompleteEvaluation eval = new CompleteEvaluation(null, LocalDate.now(), restaurant, comment, username);
    Set<Grade> grades = new HashSet<>();
    evaluationCriterias.forEach(currentCriteria -> {
      println(currentCriteria.getName() + " : " + currentCriteria.getDescription());
      Integer note = readInt();
      Grade grade = new Grade(null, note, eval, currentCriteria);
      grades.add(grade);
    });
    eval.setGrades(grades);

    this.restaurantService.evaluate(restaurant, eval);

    println("Votre évaluation a bien été enregistrée, merci !");
  }

  private void editRestaurant(Restaurant restaurant) {
    println("Edition d'un restaurant !");

    println("Nouveau nom : ");
    restaurant.setName(readString());
    println("Nouvelle description : ");
    restaurant.setDescription(readString());
    println("Nouveau site web : ");
    restaurant.setWebsite(readString());
    println("Nouveau type de restaurant : ");

    Set<RestaurantType> restaurantTypes = this.restaurantTypeService.getAll();

    RestaurantType newType = pickRestaurantType(restaurantTypes);
    restaurant.setType(newType);

    this.restaurantService.save(restaurant);

    println("Merci, le restaurant a bien été modifié !");
  }

  private void editRestaurantAddress(Restaurant restaurant) {
    println("Edition de l'adresse d'un restaurant !");
    println("Nouvelle rue : ");
    String newStreet = readString();
    Set<City> cities = this.cityService.getAll();
    // pickCity is interacting with the user -> it's not good
    // to have this logic in a transaction.
    // instead, let the RestaurantMapper insert the city
    // automagically if needed (if City's id is null)
    // ConnectionUtils.inTransaction(() -> {
      City newCity = pickCity(cities);
      restaurant.setAddress(new Localisation(newStreet, newCity));
    // });
    this.restaurantService.save(restaurant);
    println("L'adresse a bien été modifiée ! Merci !");
  }

  private void deleteRestaurant(Restaurant restaurant) {
    println("Etes-vous sûr de vouloir supprimer ce restaurant ? (O/n)");
    String choice = readString();
    if ("o".equalsIgnoreCase(choice)) {
      this.restaurantService.delete(restaurant);
      println("Le restaurant a bien été supprimé !");
    }
  }

  private int readInt() {
    int i = 0;
    boolean success = false;
    do
    { // Tant que l'utilisateur n'aura pas saisi un nombre entier, on va lui demander une nouvelle saisie
      try {
        i = scanner.nextInt();
        success = true;
      } catch (InputMismatchException e) {
        println("Erreur ! Veuillez entrer un nombre entier s'il vous plaît !");
      } finally {
        scanner.nextLine();
      }

    } while (!success);

    return i;
  }

  private String readString() {
    return scanner.nextLine();
  }

  private void println(String text) {
    printStream.println(text);
  }
}
