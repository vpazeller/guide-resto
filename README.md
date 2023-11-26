# Projet 1 - Data Mappers - Basic

Cette solution utilise une approche relativement simple des data mappers.

Les principes suivants ont principalement été pris en compte:

- Modifications minimales du code existant
- Concepts simples (pas d'héritage, pas de réflection)
- Réduction des requêtes lorsque cela est possible est simple (jointures)
- Utilisation du lazy loading

A noter que nous avons utilisé ici des DataMappers statiques avec des méthodes statiques car un seul DataMapper par classe métier est suffisant.
Une approche utilisant des méthodes d'instances via des Singletons aurait aussi pu être utilisée.

L'approche statique a l'avantage d'être simple lorsque l'on a pas besoin de polymorphisme, ou de méchanisme tels que l'injection de dépendances.
La réutilisation de code est mise en oeuvre grâce à des méthodes utilitaires.

## DEBUG MODE

Les requêtes peuvent être affichées/masquées en modifiant `QueryUtils.LOG_QUERIES`

## Diffs

Les `git diff`s ci-après montrent les modifications du code existant (hors data mappers). L'idée est de voire l'impact sur le code existant

### Diffs - Exercice 1

Note: meilleur affichage sur GitHub: https://github.com/vpazeller/guide-resto/tree/feature/basic-data-mappers

#### Modifications sur l'application

Interventions minimes pour enlever les dépendences sur les fake items:

`git diff main -- src/main/java/ch/hearc/ig/guideresto/application/`
```diff
diff --git a/src/main/java/ch/hearc/ig/guideresto/application/Main.java b/src/main/java/ch/hearc/ig/guideresto/application/Main.java
index 482da7b..7122ba1 100644
--- a/src/main/java/ch/hearc/ig/guideresto/application/Main.java
+++ b/src/main/java/ch/hearc/ig/guideresto/application/Main.java
@@ -1,16 +1,17 @@
 package ch.hearc.ig.guideresto.application;

-import ch.hearc.ig.guideresto.persistence.FakeItems;
 import ch.hearc.ig.guideresto.presentation.CLI;
+
 import java.util.Scanner;

 public class Main {

   public static void main(String[] args) {
+    // using of var is a bad practice (loss of readability)
     var scanner = new Scanner(System.in);
-    var fakeItems = new FakeItems();
+    // var fakeItems = new FakeItems();
     var printStream = System.out;
-    var cli = new CLI(scanner, printStream, fakeItems);
+    var cli = new CLI(scanner, printStream/*, fakeItems*/);

     cli.start();
   }
```

#### Modifications sur la couche métier

Modifications minimes:
- Ajout de getters
- Adaptations pour le lazy loading

`git diff main -- src/main/java/ch/hearc/ig/guideresto/business/`

```diff
diff --git a/src/main/java/ch/hearc/ig/guideresto/business/BasicEvaluation.java b/src/main/java/ch/hearc/ig/guideresto/business/BasicEvaluation.java
index a824b6f..732c098 100644
--- a/src/main/java/ch/hearc/ig/guideresto/business/BasicEvaluation.java
+++ b/src/main/java/ch/hearc/ig/guideresto/business/BasicEvaluation.java
@@ -18,4 +18,7 @@ public class BasicEvaluation extends Evaluation {
     return likeRestaurant;
   }

+  public String getIpAddress() {
+    return ipAddress;
+  }
 }
```

```diff
diff --git a/src/main/java/ch/hearc/ig/guideresto/business/City.java b/src/main/java/ch/hearc/ig/guideresto/business/City.java
index bf339a0..297ff25 100644
--- a/src/main/java/ch/hearc/ig/guideresto/business/City.java
+++ b/src/main/java/ch/hearc/ig/guideresto/business/City.java
@@ -6,6 +6,8 @@ import java.util.Set;
 public class City {
     private Integer id;
     private String zipCode;
+    // naming: name (i.s.o cityName) would have been enough since the Class name
+    // already gives context
     private String cityName;
     private Set<Restaurant> restaurants;

@@ -15,7 +17,17 @@ public class City {
         this.cityName = cityName;
         this.restaurants = new HashSet<>();
     }
-
+
+    public Integer getId() {
+        return id;
+    }
+
+    // ideally, this setter could be avoided by using reflection instead
+    // since this is a basic solution, this is acceptable though
+    public void setId(Integer id) {
+        this.id = id;
+    }
+
     public String getZipCode() {
         return zipCode;
     }
```

```diff
diff --git a/src/main/java/ch/hearc/ig/guideresto/business/CompleteEvaluation.java b/src/main/java/ch/hearc/ig/guideresto/business/CompleteEvaluation.java
index 732c0c5..6f8d04f 100644
--- a/src/main/java/ch/hearc/ig/guideresto/business/CompleteEvaluation.java
+++ b/src/main/java/ch/hearc/ig/guideresto/business/CompleteEvaluation.java
@@ -1,7 +1,8 @@
 package ch.hearc.ig.guideresto.business;

+import ch.hearc.ig.guideresto.persistence.GradeMapper;
+
 import java.time.LocalDate;
-import java.util.HashSet;
 import java.util.Set;

 public class CompleteEvaluation extends Evaluation {
@@ -15,7 +16,7 @@ public class CompleteEvaluation extends Evaluation {
     super(id, visitDate, restaurant);
     this.comment = comment;
     this.username = username;
-    this.grades = new HashSet<>();
+    this.grades = null; // lazy loading
   }

   public String getComment() {
@@ -27,6 +28,15 @@ public class CompleteEvaluation extends Evaluation {
   }

   public Set<Grade> getGrades() {
-    return grades;
+    // lazy loading
+    if (this.grades == null) {
+      this.grades = GradeMapper.findByEvaluation(this);
+    }
+    return this.grades;
+  }
+
+  public void setGrades(Set<Grade> grades) {
+    this.grades = grades;
   }
+
 }
```

```diff
diff --git a/src/main/java/ch/hearc/ig/guideresto/business/Evaluation.java b/src/main/java/ch/hearc/ig/guideresto/business/Evaluation.java
index 0d98c1a..946e773 100644
--- a/src/main/java/ch/hearc/ig/guideresto/business/Evaluation.java
+++ b/src/main/java/ch/hearc/ig/guideresto/business/Evaluation.java
@@ -13,4 +13,22 @@ public abstract class Evaluation {
     this.visitDate = visitDate;
     this.restaurant = restaurant;
   }
+
+  public Integer getId() {
+    return id;
+  }
+
+  // ideally, this setter could be avoided by using reflection instead
+  // since this is a basic solution, this is acceptable though
+  public void setId(Integer id) {
+    this.id = id;
+  }
+
+  public LocalDate getVisitDate() {
+    return visitDate;
+  }
+
+  public Restaurant getRestaurant() {
+    return restaurant;
+  }
 }
```

```diff
diff --git a/src/main/java/ch/hearc/ig/guideresto/business/EvaluationCriteria.java b/src/main/java/ch/hearc/ig/guideresto/business/EvaluationCriteria.java
index 6ecd94d..82c13f2 100644
--- a/src/main/java/ch/hearc/ig/guideresto/business/EvaluationCriteria.java
+++ b/src/main/java/ch/hearc/ig/guideresto/business/EvaluationCriteria.java
@@ -12,6 +12,10 @@ public class EvaluationCriteria {
         this.description = description;
     }

+    public Integer getId() {
+        return id;
+    }
+
     public String getName() {
         return name;
     }
```

```diff
diff --git a/src/main/java/ch/hearc/ig/guideresto/business/Grade.java b/src/main/java/ch/hearc/ig/guideresto/business/Grade.java
index 108ccc7..8b4cd0f 100644
--- a/src/main/java/ch/hearc/ig/guideresto/business/Grade.java
+++ b/src/main/java/ch/hearc/ig/guideresto/business/Grade.java
@@ -14,6 +14,16 @@ public class Grade {
         this.criteria = criteria;
     }

+    public Integer getId() {
+        return id;
+    }
+
+    // ideally, this setter could be avoided by using reflection instead
+    // since this is a basic solution, this is acceptable though
+    public void setId(Integer id) {
+        this.id = id;
+    }
+
     public Integer getGrade() {
         return grade;
     }
@@ -21,4 +31,8 @@ public class Grade {
     public EvaluationCriteria getCriteria() {
         return criteria;
     }
+
+    public CompleteEvaluation getEvaluation() {
+        return evaluation;
+    }
 }
```

```diff
diff --git a/src/main/java/ch/hearc/ig/guideresto/business/Restaurant.java b/src/main/java/ch/hearc/ig/guideresto/business/Restaurant.java
index fbfbbe9..c93b21f 100644
--- a/src/main/java/ch/hearc/ig/guideresto/business/Restaurant.java
+++ b/src/main/java/ch/hearc/ig/guideresto/business/Restaurant.java
@@ -1,6 +1,7 @@
 package ch.hearc.ig.guideresto.business;

-import java.util.HashSet;
+import ch.hearc.ig.guideresto.persistence.EvaluationMapper;
+
 import java.util.Set;

 public class Restaurant {
@@ -18,23 +19,46 @@ public class Restaurant {
         this.name = name;
         this.description = description;
         this.website = website;
-        this.evaluations = new HashSet<>();
+        this.evaluations = null; // lazy evaluation
         this.address = new Localisation(street, city);
         this.type = type;
     }

+    public Integer getId() { return this.id; }
+
+    // ideally, this setter could be avoided by using reflection instead
+    // since this is a basic solution, this is acceptable though
+    public void setId(Integer id) {
+        this.id = id;
+    }
+
     public String getName() {
         return name;
     }

+    // bad-practice: over-engineering
+    // this method should be removed because it causes more trouble than help
+    // it's not good to have multiple ways to reach things (decreases readability)
+    // -> don't use it (i.e. don't increase dependencies on it)
+    // -> instead call Restaurant.getAddress().getCity().getZipCode(); directly
     public String getZipCode() {
         return address.getCity().getZipCode();
     }

+    // bad-practice: over-engineering
+    // this method should be removed because it causes more trouble than help
+    // it's not good to have multiple ways to reach things (decreases readability)
+    // -> don't use it (i.e. don't increase dependencies on it)
+    // -> instead call Restaurant.getAddress().getStreet(); directly
     public String getStreet() {
         return address.getStreet();
     }

+    // bad-practice: over-engineering
+    // this method should be removed because it causes more trouble than help
+    // it's not good to have multiple ways to reach things (decreases readability)
+    // -> don't use it (i.e. don't increase dependencies on it)
+    // -> instead call Restaurant.getAddress().getCity().getCityName(); directly
     public String getCityName() {
         return address.getCity().getCityName();
     }
@@ -60,7 +84,11 @@ public class Restaurant {
     }

     public Set<Evaluation> getEvaluations() {
-        return evaluations;
+        // lazy loading
+        if (this.evaluations == null) {
+            this.evaluations = EvaluationMapper.findByRestaurant(this);
+        }
+        return this.evaluations;
     }
```

```diff
diff --git a/src/main/java/ch/hearc/ig/guideresto/business/RestaurantType.java b/src/main/java/ch/hearc/ig/guideresto/business/RestaurantType.java
index 8eee8e2..1397ed3 100644
--- a/src/main/java/ch/hearc/ig/guideresto/business/RestaurantType.java
+++ b/src/main/java/ch/hearc/ig/guideresto/business/RestaurantType.java
@@ -1,6 +1,7 @@
 package ch.hearc.ig.guideresto.business;

-import java.util.HashSet;
+import ch.hearc.ig.guideresto.persistence.RestaurantMapper;
+
 import java.util.Set;

 public class RestaurantType {
@@ -14,8 +15,10 @@ public class RestaurantType {
         this.id = id;
         this.label = label;
         this.description = description;
-        this.restaurants = new HashSet<>();
+        this.restaurants = null; // lazy evaluation...
     }
+
+    public Integer getId() { return this.id; }

     public String getLabel() {
         return label;
@@ -26,6 +29,9 @@ public class RestaurantType {
     }

     public Set<Restaurant> getRestaurants() {
-        return restaurants;
+        if (this.restaurants == null) {
+            this.restaurants = RestaurantMapper.findByType(this);
+        }
+        return this.restaurants;
     }
 }
```
#### Modifications sur la couche de persistence

Création entière de la couche (voir code)

#### Modifications sur la couche de présentation

Interventions ciblées:
- Remplacement des utilisations des Fake items (par les mappers)


`git diff main -- src/main/java/ch/hearc/ig/guideresto/presentation`


```diff
diff --git a/src/main/java/ch/hearc/ig/guideresto/presentation/CLI.java b/src/main/java/ch/hearc/ig/guideresto/presentation/CLI.java
index 766f135..79ebd1b 100644
--- a/src/main/java/ch/hearc/ig/guideresto/presentation/CLI.java
+++ b/src/main/java/ch/hearc/ig/guideresto/presentation/CLI.java
@@ -11,27 +11,25 @@ import ch.hearc.ig.guideresto.business.EvaluationCriteria;
 import ch.hearc.ig.guideresto.business.Grade;
 import ch.hearc.ig.guideresto.business.Restaurant;
 import ch.hearc.ig.guideresto.business.RestaurantType;
-import ch.hearc.ig.guideresto.persistence.FakeItems;
+import ch.hearc.ig.guideresto.persistence.*;
+
 import java.io.PrintStream;
 import java.net.Inet4Address;
 import java.net.UnknownHostException;
 import java.time.LocalDate;
-import java.util.InputMismatchException;
-import java.util.Optional;
-import java.util.Scanner;
-import java.util.Set;
+import java.util.*;

 public class CLI {

   private final Scanner scanner;
   private final PrintStream printStream;
-  private final FakeItems fakeItems;
+  // private final FakeItems fakeItems;

   // Injection de dépendances
-  public CLI(Scanner scanner, PrintStream printStream, FakeItems fakeItems) {
+  public CLI(Scanner scanner, PrintStream printStream/*, FakeItems fakeItems*/) {
     this.scanner = scanner;
     this.printStream = printStream;
-    this.fakeItems = fakeItems;
+    // this.fakeItems = fakeItems;
   }

   public void start() {
@@ -103,7 +101,7 @@ public class CLI {
   private void showRestaurantsList() {
     println("Liste des restaurants : ");

-    Set<Restaurant> restaurants = fakeItems.getAllRestaurants();
+    Set<Restaurant> restaurants = RestaurantMapper.findAll(); // fakeItems.getAllRestaurants();

     Optional<Restaurant> maybeRestaurant = pickRestaurant(restaurants);
     // Si l'utilisateur a choisi un restaurant, on l'affiche, sinon on ne fait rien et l'application va réafficher le menu principal
@@ -114,7 +112,7 @@ public class CLI {
     println("Veuillez entrer une partie du nom recherché : ");
     String research = readString();

-    Set<Restaurant> restaurants = fakeItems.getAllRestaurants()
+    Set<Restaurant> restaurants = RestaurantMapper.findAll() // fakeItems.getAllRestaurants()
         .stream()
         .filter(r -> r.getName().equalsIgnoreCase(research))
         .collect(toUnmodifiableSet());
@@ -131,7 +129,12 @@ public class CLI {
     println("Veuillez entrer une partie du nom de la ville désirée : ");
     String research = readString();

-    Set<Restaurant> restaurants = fakeItems.getAllRestaurants()
+    // Another (better) option could be to have two finder methods:
+    // 1. CityMapper.findOneByName(String name) to find a City Entity by its name (case insensitively)
+    // 2. RestaurantMapper.findByCity(City city) to find all restaurants in a city
+    // The option below has been selected in the solution simply because it less intrusive in the CLI's code
+    // (less code modifications)
+    Set<Restaurant> restaurants = RestaurantMapper.findAll() // fakeItems.getAllRestaurants()
         .stream()
         .filter(r -> r.getAddress().getCity().getCityName().toUpperCase().contains(research.toUpperCase()))
         .collect(toUnmodifiableSet());
@@ -154,8 +157,8 @@ public class CLI {
       String zipCode = readString();
       println("Veuillez entrer le nom de la nouvelle ville : ");
       String cityName = readString();
-      City city = new City(1, zipCode, cityName);
-      fakeItems.getCities().add(city);
+      City city = new City(null, zipCode, cityName);
+      // fakeItems.getCities().add(city);
       return city;
     }

@@ -178,10 +181,15 @@ public class CLI {
   }

   private void searchRestaurantByType() {
-    Set<RestaurantType> restaurantTypes = fakeItems.getRestaurantTypes();
+    Set<RestaurantType> restaurantTypes = RestaurantTypeMapper.findAll(); // fakeItems.getRestaurantTypes();
     RestaurantType chosenType = pickRestaurantType(restaurantTypes);

-    Set<Restaurant> restaurants = fakeItems.getAllRestaurants()
+    // Another (better) option could be to have two finder methods:
+    // 1. RestaurantTypeMapper.findOneByName(String name) to find a RestaurantType Entity by its name (case insensitively)
+    // 2. RestaurantMapper.findByType(RestaurantType type) to find all restaurants of a given type
+    // The option below has been selected in the solution simply because it less intrusive in the CLI's code
+    // (less code modifications)
+    Set<Restaurant> restaurants = RestaurantMapper.findAll() // fakeItems.getAllRestaurants()
         .stream()
         .filter(r -> r.getType().getLabel().equalsIgnoreCase(chosenType.getLabel()))
         .collect(toUnmodifiableSet());
@@ -203,21 +211,25 @@ public class CLI {
     City city;
     do
     { // La sélection d'une ville est obligatoire, donc l'opération se répètera tant qu'aucune ville n'est sélectionnée.
-      Set<City> cities = fakeItems.getCities();
+      Set<City> cities = CityMapper.findAll(); // fakeItems.getCities();
       city = pickCity(cities);
     } while (city == null);

     RestaurantType restaurantType;

     // La sélection d'un type est obligatoire, donc l'opération se répètera tant qu'aucun type n'est sélectionné.
-    Set<RestaurantType> restaurantTypes = fakeItems.getRestaurantTypes();
+    Set<RestaurantType> restaurantTypes = RestaurantTypeMapper.findAll(); // fakeItems.getRestaurantTypes();
     restaurantType = pickRestaurantType(restaurantTypes);

     Restaurant restaurant = new Restaurant(null, name, description, website, street, city,
         restaurantType);
     city.getRestaurants().add(restaurant);
     restaurant.getAddress().setCity(city);
-    fakeItems.getAllRestaurants().add(restaurant);
+
+    ConnectionUtils.inTransaction(() -> {
+      RestaurantMapper.insert(restaurant);
+    });
+    // fakeItems.getAllRestaurants().add(restaurant);

     showRestaurant(restaurant);
   }
@@ -311,6 +323,9 @@ public class CLI {
   private void addBasicEvaluation(Restaurant restaurant, Boolean like) {
     BasicEvaluation eval = new BasicEvaluation(null, LocalDate.now(), restaurant, like, getIpAddress());
     restaurant.getEvaluations().add(eval);
+    ConnectionUtils.inTransaction(() -> {
+      BasicEvaluationMapper.insert(eval);
+    });
     println("Votre vote a été pris en compte !");
   }

@@ -329,19 +344,24 @@ public class CLI {
     println("Quel commentaire aimeriez-vous publier ?");
     String comment = readString();

-    CompleteEvaluation eval = new CompleteEvaluation(null, LocalDate.now(), restaurant, comment,
-        username);
-    restaurant.getEvaluations().add(eval);
-
     println("Veuillez svp donner une note entre 1 et 5 pour chacun de ces critères : ");

-    Set<EvaluationCriteria> evaluationCriterias = fakeItems.getEvaluationCriterias();
+    Set<EvaluationCriteria> evaluationCriterias = EvaluationCriteriaMapper.findAll(); // fakeItems.getEvaluationCriterias();

+    CompleteEvaluation eval = new CompleteEvaluation(null, LocalDate.now(), restaurant, comment, username);
+    Set<Grade> grades = new HashSet<>();
     evaluationCriterias.forEach(currentCriteria -> {
       println(currentCriteria.getName() + " : " + currentCriteria.getDescription());
       Integer note = readInt();
       Grade grade = new Grade(null, note, eval, currentCriteria);
-      eval.getGrades().add(grade);
+      grades.add(grade);
+    });
+    eval.setGrades(grades);
+    restaurant.getEvaluations().add(eval);
+
+    // let's keep the transaction short and insert everything at the end:
+    ConnectionUtils.inTransaction(() -> {
+      CompleteEvaluationMapper.insert(eval);
     });

     println("Votre évaluation a bien été enregistrée, merci !");
@@ -358,7 +378,7 @@ public class CLI {
     restaurant.setWebsite(readString());
     println("Nouveau type de restaurant : ");

-    Set<RestaurantType> restaurantTypes = fakeItems.getRestaurantTypes();
+    Set<RestaurantType> restaurantTypes = RestaurantTypeMapper.findAll();

     RestaurantType newType = pickRestaurantType(restaurantTypes);
     if (newType != restaurant.getType()) {
@@ -367,6 +387,10 @@ public class CLI {
       restaurant.setType(newType);
     }

+    ConnectionUtils.inTransaction(() -> {
+      RestaurantMapper.update(restaurant);
+    });
+
     println("Merci, le restaurant a bien été modifié !");
   }

@@ -376,14 +400,27 @@ public class CLI {
     println("Nouvelle rue : ");
     restaurant.getAddress().setStreet(readString());

-    Set<City> cities = fakeItems.getCities();
-
-    City newCity = pickCity(cities);
-    if (newCity.equals(restaurant.getAddress().getCity())) {
+    Set<City> cities = CityMapper.findAll();
+
+    // pickCity is interacting with the user -> it's not good
+    // to have this logic in a transaction.
+    // instead, let the RestaurantMapper insert the city
+    // automagically if needed (if City's id is null)
+    // ConnectionUtils.inTransaction(() -> {
+      City newCity = pickCity(cities);
+      // if (newCity.equals(restaurant.getAddress().getCity())) {
+      //    restaurant.getAddress().getCity().getRestaurants().remove(restaurant);
+      //    newCity.getRestaurants().add(restaurant);
+      //    restaurant.getAddress().setCity(newCity);
+      // }
       restaurant.getAddress().getCity().getRestaurants().remove(restaurant);
       newCity.getRestaurants().add(restaurant);
       restaurant.getAddress().setCity(newCity);
-    }
+    // });
+
+    ConnectionUtils.inTransaction(() -> {
+      RestaurantMapper.update(restaurant);
+    });

     println("L'adresse a bien été modifiée ! Merci !");
   }
@@ -394,7 +431,10 @@ public class CLI {
     if ("o".equalsIgnoreCase(choice)) {
       restaurant.getAddress().getCity().getRestaurants().remove(restaurant);
       restaurant.getType().getRestaurants().remove(restaurant);
-      fakeItems.getAllRestaurants().remove(restaurant);
+      ConnectionUtils.inTransaction(() -> {
+        RestaurantMapper.delete(restaurant);
+      });
+      // fakeItems.getAllRestaurants().remove(restaurant);
       println("Le restaurant a bien été supprimé !");
     }
   }
```