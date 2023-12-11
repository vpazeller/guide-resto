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

### Diffs - Exercice 2

Note: meilleur affichage sur GitHub: https://github.com/vpazeller/guide-resto/tree/feature/identity-maps


#### Création d'une classe utilitaire

C'est optionel: créer des `java.util.HashMap` dans chaque classe est tout à fait valable.

La création d'une classe spécialisée permet de masquer et réutiliser certains détails d'implémentation
ainsi que d'assurer un typage spécialisé et correct.


```diff
diff --git a/src/main/java/ch/hearc/ig/guideresto/persistence/EntityRegistry.java b/src/main/java/ch/hearc/ig/guideresto/persistence/EntityRegistry.java
new file mode 100644
index 0000000..a85207a
--- /dev/null
+++ b/src/main/java/ch/hearc/ig/guideresto/persistence/EntityRegistry.java
@@ -0,0 +1,26 @@
+package ch.hearc.ig.guideresto.persistence;
+
+import java.util.*;
+
+public class EntityRegistry<T> {
+
+    protected final Map<Integer, T> identityMap = new HashMap<Integer, T>();
+
+    public Optional<T> get(Integer id) {
+        return this.identityMap.containsKey(id)
+            ? Optional.of(this.identityMap.get(id))
+            : Optional.empty();
+    }
+
+    public void set(Integer id, T entity) {
+        if (entity == null) {
+            this.identityMap.remove(id);
+        } else {
+            this.identityMap.put(id, entity);
+        }
+    }
+
+    public void delete(Integer id) {
+        this.identityMap.remove(id);
+    }
+}
```

#### Modifications sur la couche de persistence

On ajoute la logique de registres à chaque fois que l'on crée une nouvelle instance.

```diff
diff --git a/src/main/java/ch/hearc/ig/guideresto/persistence/BasicEvaluationMapper.java b/src/main/java/ch/hearc/ig/guideresto/persistence/BasicEvaluationMapper.java
index 5905ef4..66969c6 100644
--- a/src/main/java/ch/hearc/ig/guideresto/persistence/BasicEvaluationMapper.java
+++ b/src/main/java/ch/hearc/ig/guideresto/persistence/BasicEvaluationMapper.java
@@ -1,6 +1,8 @@
 package ch.hearc.ig.guideresto.persistence;

 import ch.hearc.ig.guideresto.business.BasicEvaluation;
+import ch.hearc.ig.guideresto.business.CompleteEvaluation;
+import ch.hearc.ig.guideresto.business.Evaluation;
 import ch.hearc.ig.guideresto.business.Restaurant;

 import java.math.BigDecimal;
@@ -14,6 +16,8 @@ import java.util.Set;

 public class BasicEvaluationMapper {

+    private static final EntityRegistry<BasicEvaluation> registry = new EntityRegistry<>();
+
     private static final String QUERY_BY_RESTAURANT = "SELECT " +
     "NUMERO, APPRECIATION, DATE_EVAL, ADRESSE_IP " +
     "FROM LIKES " +
@@ -30,14 +34,15 @@ public class BasicEvaluationMapper {
         Set<BasicEvaluation> likes = new HashSet<>();
         List<Map<String, Object>> rows = QueryUtils.findAllByForeignKey(BasicEvaluationMapper.QUERY_BY_RESTAURANT, restaurant.getId());
         for (Map<String, Object> row: rows) {
-            BasicEvaluation like = new BasicEvaluation(
-                    ((BigDecimal) row.get("NUMERO")).intValue(),
-                    ((Timestamp) row.get("DATE_EVAL")).toLocalDateTime().toLocalDate(),
-                    restaurant,
-                    row.get("APPRECIATION").equals("T"),
-                    (String) row.get("ADRESSE_IP")
-            );
+            Integer likeId = ((BigDecimal) row.get("NUMERO")).intValue();
+            BasicEvaluation like = registry.get(likeId).orElse(new BasicEvaluation());
+            like.setId(likeId);
+            like.setVisitDate(((Timestamp) row.get("DATE_EVAL")).toLocalDateTime().toLocalDate());
+            like.setRestaurant(restaurant);
+            like.setLikeRestaurant(row.get("APPRECIATION").equals("T"));
+            like.setIpAddress((String) row.get("ADRESSE_IP"));
             likes.add(like);
+            registry.set(likeId, like);
         }
         return likes;
     }
@@ -62,6 +67,7 @@ public class BasicEvaluationMapper {
             }
         });
         like.setId(id);
+        registry.set(id, like);
     }

     public static void deleteForRestaurant(Restaurant restaurant) {
@@ -69,5 +75,11 @@ public class BasicEvaluationMapper {
             BasicEvaluationMapper.QUERY_DELETE_BY_RESTAURANT,
             restaurant.getId()
         );
+
+        for (Evaluation evaluation: restaurant.getEvaluations()) {
+            if (evaluation instanceof BasicEvaluation) {
+                registry.delete(evaluation.getId());
+            }
+        }
     }
 }
```


```diff
diff --git a/src/main/java/ch/hearc/ig/guideresto/persistence/CityMapper.java b/src/main/java/ch/hearc/ig/guideresto/persistence/CityMapper.java
index d8c5e59..432a912 100644
--- a/src/main/java/ch/hearc/ig/guideresto/persistence/CityMapper.java
+++ b/src/main/java/ch/hearc/ig/guideresto/persistence/CityMapper.java
@@ -5,13 +5,12 @@ import ch.hearc.ig.guideresto.business.City;
 import java.math.BigDecimal;
 import java.sql.PreparedStatement;
 import java.sql.SQLException;
-import java.util.HashSet;
-import java.util.List;
-import java.util.Map;
-import java.util.Set;
+import java.util.*;

 public class CityMapper {


 public class CityMapper {

+    private static final EntityRegistry<City> registry = new EntityRegistry<>();
+
     private static final String QUERY_FIND_ALL = "SELECT " +
             "NUMERO, CODE_POSTAL, NOM_VILLE " +
             "FROM VILLES ";
@@ -20,16 +19,22 @@ public class CityMapper {
             // optional, but adds safety if the table structure changes
             "(CODE_POSTAL, NOM_VILLE) " +
             "VALUES (?, ?)";
+
+    public static EntityRegistry<City> getRegistry() {
+        return registry;
+    }
+
     public static Set<City> findAll() {
         Set<City> cities = new HashSet<>();
         List<Map<String, Object>> rows = QueryUtils.findAll(CityMapper.QUERY_FIND_ALL);
         for (Map<String, Object> row: rows) {
-            City city = new City(
-                ((BigDecimal) row.get("NUMERO")).intValue(),
-                (String) row.get("CODE_POSTAL"),
-                (String) row.get("NOM_VILLE")
-            );
+            Integer cityId = ((BigDecimal) row.get("NUMERO")).intValue();
+            City city = registry.get(cityId).orElse(new City());
+            city.setId(cityId);
+            city.setZipCode((String) row.get("CODE_POSTAL"));
+            city.setCityName((String) row.get("NOM_VILLE"));
             cities.add(city);
+            registry.set(cityId, city);
         }
         return cities;
     }
@@ -47,5 +52,6 @@ public class CityMapper {
             }
         });
         city.setId(id);
+        registry.set(id, city);
     }
 }
```


```diff
diff --git a/src/main/java/ch/hearc/ig/guideresto/persistence/CompleteEvaluationMapper.java b/src/main/java/ch/hearc/ig/guideresto/persistence/CompleteEvaluationMapper.java
index d74af98..65bddd7 100644
--- a/src/main/java/ch/hearc/ig/guideresto/persistence/CompleteEvaluationMapper.java
+++ b/src/main/java/ch/hearc/ig/guideresto/persistence/CompleteEvaluationMapper.java
@@ -1,6 +1,9 @@
 package ch.hearc.ig.guideresto.persistence;

-import ch.hearc.ig.guideresto.business.*;
+import ch.hearc.ig.guideresto.business.CompleteEvaluation;
+import ch.hearc.ig.guideresto.business.Evaluation;
+import ch.hearc.ig.guideresto.business.Grade;
+import ch.hearc.ig.guideresto.business.Restaurant;
 import oracle.sql.CLOB;

 import java.math.BigDecimal;
@@ -14,6 +17,8 @@ import java.util.Set;

 public class CompleteEvaluationMapper {

+    private static final EntityRegistry<CompleteEvaluation> registry = new EntityRegistry<>();
+
     private static final String QUERY_DELETE_BY_RESTAURANT = "DELETE COMMENTAIRES WHERE FK_REST = ?";

     private static final String QUERY_BY_RESTAURANT = "SELECT " +
@@ -30,14 +35,15 @@ public class CompleteEvaluationMapper {
         Set<CompleteEvaluation> comments = new HashSet<>();
         List<Map<String, Object>> rows = QueryUtils.findAllByForeignKey(CompleteEvaluationMapper.QUERY_BY_RESTAURANT, restaurant.getId());
         for (Map<String, Object> row: rows) {
-            CompleteEvaluation comment = new CompleteEvaluation(
-                    ((BigDecimal) row.get("NUMERO")).intValue(),
-                    ((Timestamp) row.get("DATE_EVAL")).toLocalDateTime().toLocalDate(),
-                    restaurant,
-                    ResultUtils.clobToString((CLOB) row.get("COMMENTAIRE")),
-                    (String) row.get("NOM_UTILISATEUR")
-            );
-            );
+            Integer commentId = ((BigDecimal) row.get("NUMERO")).intValue();
+            CompleteEvaluation comment = registry.get(commentId).orElse(new CompleteEvaluation());
+            comment.setId(commentId);
+            comment.setVisitDate(((Timestamp) row.get("DATE_EVAL")).toLocalDateTime().toLocalDate());
+            comment.setRestaurant(restaurant);
+            comment.setComment(ResultUtils.clobToString((CLOB) row.get("COMMENTAIRE")));
+            comment.setUsername((String) row.get("NOM_UTILISATEUR"));
             comments.add(comment);
+            registry.set(commentId, comment);
         }
         return comments;
     }
@@ -67,6 +73,8 @@ public class CompleteEvaluationMapper {
         for (Grade grade: comment.getGrades()) {
             GradeMapper.insert(grade);
         }
+
+        registry.set(id, comment);
     }

     public static void deleteForRestaurant(Restaurant restaurant) {
@@ -74,6 +82,7 @@ public class CompleteEvaluationMapper {
         for (Evaluation evaluation: restaurant.getEvaluations()) {
             if (evaluation instanceof CompleteEvaluation) {
                 GradeMapper.deleteForEvaluation((CompleteEvaluation) evaluation);
+                registry.delete(evaluation.getId());
             }
         }
```


```diff
diff --git a/src/main/java/ch/hearc/ig/guideresto/persistence/EvaluationCriteriaMapper.java b/src/main/java/ch/hearc/ig/guideresto/persistence/EvaluationCriteriaMapper.java
index ba2ffd4..7f4dfe1 100644
--- a/src/main/java/ch/hearc/ig/guideresto/persistence/EvaluationCriteriaMapper.java
+++ b/src/main/java/ch/hearc/ig/guideresto/persistence/EvaluationCriteriaMapper.java
@@ -1,12 +1,8 @@
 package ch.hearc.ig.guideresto.persistence;

-import ch.hearc.ig.guideresto.business.City;
 import ch.hearc.ig.guideresto.business.EvaluationCriteria;
-import oracle.sql.CLOB;

 import java.math.BigDecimal;
-import java.sql.PreparedStatement;
-import java.sql.SQLException;
 import java.util.HashSet;

-import ch.hearc.ig.guideresto.business.City;
 import ch.hearc.ig.guideresto.business.EvaluationCriteria;
-import oracle.sql.CLOB;

 import java.math.BigDecimal;
-import java.sql.PreparedStatement;
-import java.sql.SQLException;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
@@ -14,19 +10,27 @@ import java.util.Set;

 public class EvaluationCriteriaMapper {

+    private static final EntityRegistry<EvaluationCriteria> registry = new EntityRegistry<>();
+
     private static final String QUERY_FIND_ALL = "SELECT " +
             "NUMERO, NOM, DESCRIPTION " +
             "FROM CRITERES_EVALUATION ";
+
+    public static EntityRegistry<EvaluationCriteria> getRegistry() {
+        return registry;
+    }
+
     public static Set<EvaluationCriteria> findAll() {
         Set<EvaluationCriteria> criteria = new HashSet<>();
         List<Map<String, Object>> rows = QueryUtils.findAll(EvaluationCriteriaMapper.QUERY_FIND_ALL);
         for (Map<String, Object> row: rows) {
-            EvaluationCriteria criterion = new EvaluationCriteria(
-                ((BigDecimal) row.get("NUMERO")).intValue(),
-                (String) row.get("NOM"),
-                (String) row.get("DESCRIPTION")
-            );
+            Integer criterionId = ((BigDecimal) row.get("NUMERO")).intValue();
+            EvaluationCriteria criterion = registry.get(criterionId).orElse(new EvaluationCriteria());
+            criterion.setId(criterionId);
+            criterion.setName((String) row.get("NOM"));
+            criterion.setDescription((String) row.get("DESCRIPTION"));
             criteria.add(criterion);
+            registry.set(criterionId, criterion);
         }
         return criteria;
     }
```


```diff
diff --git a/src/main/java/ch/hearc/ig/guideresto/persistence/GradeMapper.java b/src/main/java/ch/hearc/ig/guideresto/persistence/GradeMapper.java
index 3f70bc2..0074216 100644
--- a/src/main/java/ch/hearc/ig/guideresto/persistence/GradeMapper.java
+++ b/src/main/java/ch/hearc/ig/guideresto/persistence/GradeMapper.java
@@ -1,7 +1,8 @@
 package ch.hearc.ig.guideresto.persistence;

-import ch.hearc.ig.guideresto.business.*;
-import oracle.sql.CLOB;
+import ch.hearc.ig.guideresto.business.CompleteEvaluation;
+import ch.hearc.ig.guideresto.business.EvaluationCriteria;
+import ch.hearc.ig.guideresto.business.Grade;

 import java.math.BigDecimal;
 import java.sql.PreparedStatement;
@@ -13,6 +14,8 @@ import java.util.Set;

 public class GradeMapper {

+    private static final EntityRegistry<Grade> registry = new EntityRegistry<>();
+
     private static final String QUERY_FIND_BY_EVALUATION = "SELECT " +
             "N.NUMERO AS N_NUMERO, N.NOTE AS N_NOTE, N.FK_CRIT AS N_FK_CRIT, N.FK_COMM AS N_FK_COMM, " +
             "C.NOM AS C_NOM, C.DESCRIPTION AS C_DESCRIPTION " +
@@ -31,13 +34,14 @@ public class GradeMapper {
         List<Map<String, Object>> rows = QueryUtils.findAllByForeignKey(GradeMapper.QUERY_FIND_BY_EVALUATION, evaluation.getId());
         Set<Grade> grades = new HashSet<>();
         for (Map<String, Object> row: rows) {
-            Grade grade = new Grade(
-                ((BigDecimal) row.get("N_NUMERO")).intValue(),
-                    ((BigDecimal) row.get("N_NOTE")).intValue(),
-                evaluation,
-                GradeMapper.fetchCriterion(row)
-            );
+            Integer gradeId = ((BigDecimal) row.get("N_NUMERO")).intValue();
+            Grade grade = registry.get(gradeId).orElse(new Grade());
+            grade.setId(gradeId);
+            grade.setGrade(((BigDecimal) row.get("N_NOTE")).intValue());
+            grade.setEvaluation(evaluation);
+            grade.setCriteria(GradeMapper.fetchCriterion(row));
             grades.add(grade);
+            registry.set(gradeId, grade);
         }
         return grades;
     }
@@ -70,6 +74,7 @@ public class GradeMapper {
             }
         });
         grade.setId(id);
+            grade.setCriteria(GradeMapper.fetchCriterion(row));
             grades.add(grade);
+            registry.set(gradeId, grade);
         }
         return grades;
     }
@@ -70,6 +74,7 @@ public class GradeMapper {
             }
         });
         grade.setId(id);
+        registry.set(id, grade);
     }

     public static void deleteForEvaluation(CompleteEvaluation evaluation) {
@@ -77,6 +82,9 @@ public class GradeMapper {
             GradeMapper.QUERY_DELETE_BY_EVALUATION,
             evaluation.getId()
         );
+        for (Grade grade: evaluation.getGrades()) {
+            registry.delete(grade.getId());
+        }
     }

     private static EvaluationCriteria fetchCriterion(Map<String, Object> row) {
@@ -84,11 +92,14 @@ public class GradeMapper {
         if (criterionPk == null) {
             return null;
         }
-        // TODO: use registry to avoid duplicate instances
-        return new EvaluationCriteria(
-            ((BigDecimal) criterionPk).intValue(),
-            (String) row.get("C_NOM"),
-            (String) row.get("C_DESCRIPTION")
-        );
+        Integer criterionId = ((BigDecimal) criterionPk).intValue();
+        EvaluationCriteria criterion = EvaluationCriteriaMapper.getRegistry().get(criterionId)
+            .orElse(new EvaluationCriteria());
+        criterion.setId(criterionId);
+        criterion.setName((String) row.get("C_NOM"));
+        criterion.setDescription((String) row.get("C_DESCRIPTION"));
+        // Ensure instance is saved in the identity map
+        EvaluationCriteriaMapper.getRegistry().set(criterionId, criterion);
+        return criterion;
     }
 }
```


```diff
diff --git a/src/main/java/ch/hearc/ig/guideresto/persistence/RestaurantMapper.java b/src/main/java/ch/hearc/ig/guideresto/persistence/RestaurantMapper.java
index 15e035c..b4213b4 100644
--- a/src/main/java/ch/hearc/ig/guideresto/persistence/RestaurantMapper.java
+++ b/src/main/java/ch/hearc/ig/guideresto/persistence/RestaurantMapper.java
@@ -7,12 +7,18 @@ import ch.hearc.ig.guideresto.business.RestaurantType;
 import oracle.sql.CLOB;

 import java.math.BigDecimal;
+import java.rmi.registry.Registry;
 import java.sql.PreparedStatement;
 import java.sql.SQLException;
-import java.util.*;
+import java.util.HashSet;
+import java.util.List;
+import java.util.Map;
+import java.util.Set;

 public class RestaurantMapper {

+    private static final EntityRegistry<Restaurant> registry = new EntityRegistry<>();
+
     private static final String QUERY_FIND_ALL = "SELECT " +
             "R.NUMERO AS R_NUMERO, R.NOM AS R_NOM, R.ADRESSE AS R_ADRESSE, R.DESCRIPTION AS R_DESCRIPTION, R.SITE_WEB AS R_SITE_WEB, " +
             "V.NUMERO AS V_NUMERO, V.NOM_VILLE AS V_NOM, V.CODE_POSTAL AS V_CODE, " +
@@ -35,6 +41,7 @@ public class RestaurantMapper {
             "WHERE NUMERO = ?";

     private static final String QUERY_DELETE_BY_ID = "DELETE RESTAURANTS WHERE NUMERO = ?";
+
     public static Set<Restaurant> findAll() {
         List<Map<String, Object>> rows = QueryUtils.findAll(RestaurantMapper.QUERY_FIND_ALL);
         return RestaurantMapper.fetchRestaurants(rows);
@@ -55,6 +62,7 @@ public class RestaurantMapper {
         CompleteEvaluationMapper.deleteForRestaurant(restaurant);

         QueryUtils.deleteByPkOrFk(RestaurantMapper.QUERY_DELETE_BY_ID, restaurantId);
+        registry.delete(restaurantId);
     }
     public static void insert(Restaurant restaurant) {
         if (restaurant.getId() != null) {
@@ -75,6 +83,7 @@ public class RestaurantMapper {
             }
         });
         restaurant.setId(id);
+        registry.set(id, restaurant);
     }

     public static void update(Restaurant restaurant) {
@@ -122,16 +131,24 @@ public class RestaurantMapper {
     private static Set<Restaurant> fetchRestaurants(List<Map<String, Object>> rows) {
         Set<Restaurant> restaurants = new HashSet<>();
         for (Map<String, Object> row: rows) {
-            Restaurant restaurant = new Restaurant(
-                    ((BigDecimal) row.get("R_NUMERO")).intValue(),
-                    (String) row.get("R_NOM"),
-                    ResultUtils.clobToString((CLOB) row.get("R_DESCRIPTION")),
-                    (String) row.get("R_SITE_WEB"),
-                    (String) row.get("R_ADRESSE"),
-                    RestaurantMapper.fetchCity(row),
-                    RestaurantMapper.fetchType(row)
+            // ensure no duplicate instances are created
+            Integer restaurantId = ((BigDecimal) row.get("R_NUMERO")).intValue();
+            Restaurant restaurant = registry.get(restaurantId).orElse(new Restaurant());
+
+            restaurant.setId(restaurantId);
+            restaurant.setName((String) row.get("R_NOM"));
+            restaurant.setDescription(ResultUtils.clobToString((CLOB) row.get("R_DESCRIPTION")));
+            restaurant.setWebsite((String) row.get("R_SITE_WEB"));
+            Localisation address = new Localisation(
+                (String) row.get("R_ADRESSE"),
+                RestaurantMapper.fetchCity(row)
             );
+            restaurant.setAddress(address);
+            restaurant.setType(RestaurantMapper.fetchType(row));
             restaurants.add(restaurant);
+            // Ensure instance is saved in the identity map
+            // (if it was already there, it will be overrided to the same value -> no effect)
+            registry.set(restaurantId, restaurant);
         }
         return restaurants;
     }
@@ -141,11 +158,14 @@ public class RestaurantMapper {
         if (cityFk == null) {
             return null;
         }
-        return new City(
-            ((BigDecimal) cityFk).intValue(),
-            (String) row.get("V_CODE"),
-            (String) row.get("V_NOM")
-        );
+        Integer cityId = ((BigDecimal) cityFk).intValue();
+        City city = CityMapper.getRegistry().get(cityId).orElse(new City());
+        city.setId(cityId);
+        city.setZipCode((String) row.get("V_CODE"));
+        city.setCityName((String) row.get("V_NOM"));
+        // Ensure instance is saved in the identity map
+        CityMapper.getRegistry().set(cityId, city);
+        return city;
     }

     private static RestaurantType fetchType(Map<String, Object> row) {
@@ -153,11 +173,11 @@ public class RestaurantMapper {
         if (typeFk == null) {
             return null;
         }
-        // TODO: use registry to avoid duplicate instances
-        return new RestaurantType(
-            ((BigDecimal) typeFk).intValue(),
-            (String) row.get("T_LABEL"),
-            ResultUtils.clobToString((CLOB) row.get("T_DESCRIPTION"))
-        );
+        Integer typeId = ((BigDecimal) typeFk).intValue();
+        RestaurantType type = RestaurantTypeMapper.getRegistry().get(typeId)
+            .orElse(new RestaurantType());
+        type.setId(typeId);
+        type.setLabel((String) row.get("T_LABEL"));
+        type.setDescription(ResultUtils.clobToString((CLOB) row.get("T_DESCRIPTION")));
+        // Ensure instance is saved in the identity map
+        RestaurantTypeMapper.getRegistry().set(typeId, type);
+        return type;
     }
 }

```


```diff
diff --git a/src/main/java/ch/hearc/ig/guideresto/persistence/RestaurantTypeMapper.java b/src/main/java/ch/hearc/ig/guideresto/persistence/RestaurantTypeMapper.java
index 17a5faa..56adf31 100644
--- a/src/main/java/ch/hearc/ig/guideresto/persistence/RestaurantTypeMapper.java
+++ b/src/main/java/ch/hearc/ig/guideresto/persistence/RestaurantTypeMapper.java
@@ -1,5 +1,6 @@
 package ch.hearc.ig.guideresto.persistence;

+import ch.hearc.ig.guideresto.business.City;
 import ch.hearc.ig.guideresto.business.RestaurantType;
 import oracle.sql.CLOB;

@@ -11,19 +12,27 @@ import java.util.Set;

 public class RestaurantTypeMapper {

+    private static final EntityRegistry<RestaurantType> registry = new EntityRegistry<>();
+
     private static final String QUERY_ALL = "SELECT " +
     "NUMERO, LIBELLE, DESCRIPTION " +
     "FROM TYPES_GASTRONOMIQUES";
+
+    public static EntityRegistry<RestaurantType> getRegistry() {
+        return RestaurantTypeMapper.registry;
+    }
+
     public static Set<RestaurantType> findAll() {
         Set<RestaurantType> types = new HashSet<>();
         List<Map<String, Object>> rows = QueryUtils.findAll(RestaurantTypeMapper.QUERY_ALL);
         for (Map<String, Object> row: rows) {
-            RestaurantType type = new RestaurantType(
-                ((BigDecimal) row.get("NUMERO")).intValue(),
-                (String) row.get("LIBELLE"),
-                ResultUtils.clobToString((CLOB) row.get("DESCRIPTION"))
-            );
+            Integer typeId = ((BigDecimal) row.get("NUMERO")).intValue();
+            RestaurantType type = registry.get(typeId).orElse(new RestaurantType());
+            type.setId(typeId);
+            type.setLabel((String) row.get("LIBELLE"));
+            type.setDescription(ResultUtils.clobToString((CLOB) row.get("DESCRIPTION")));
             types.add(type);
+            registry.set(typeId, type);
         }
         return types;
     }
```


#### Modifications sur la couche métier

La création de constructeurs vides et l'ajout de setter dans la couche métier peuvent aider:

`git diff feature/basic-data-mappers feature/identity-maps -- src/main/java/ch/hearc/ig/guideresto/business/`

```diff
diff --git a/src/main/java/ch/hearc/ig/guideresto/business/BasicEvaluation.java b/src/main/java/ch/hearc/ig/guideresto/business/BasicEvaluation.java
index 732c098..a580086 100644
--- a/src/main/java/ch/hearc/ig/guideresto/business/BasicEvaluation.java
+++ b/src/main/java/ch/hearc/ig/guideresto/business/BasicEvaluation.java
@@ -7,6 +7,11 @@ public class BasicEvaluation extends Evaluation {
   private boolean likeRestaurant;
   private String ipAddress;

+  // Having an empty constructor is handy to work with identity maps / entity registries
+  public BasicEvaluation() {
+    super();
+  }
+
   public BasicEvaluation(Integer id, LocalDate visitDate, Restaurant restaurant, boolean likeRestaurant,
       String ipAddress) {
     super(id, visitDate, restaurant);
@@ -18,7 +23,15 @@ public class BasicEvaluation extends Evaluation {
     return likeRestaurant;
   }

+  public void setLikeRestaurant(boolean likeRestaurant) {
+    this.likeRestaurant = likeRestaurant;
+  }
+
   public String getIpAddress() {
     return ipAddress;
   }
+
+  public void setIpAddress(String ipAddress) {
+    this.ipAddress = ipAddress;
+  }
 }
```


```diff
diff --git a/src/main/java/ch/hearc/ig/guideresto/business/City.java b/src/main/java/ch/hearc/ig/guideresto/business/City.java
index 297ff25..04eb6ae 100644
--- a/src/main/java/ch/hearc/ig/guideresto/business/City.java
+++ b/src/main/java/ch/hearc/ig/guideresto/business/City.java
@@ -9,13 +9,16 @@ public class City {
     // naming: name (i.s.o cityName) would have been enough since the Class name
     // already gives context
     private String cityName;
-    private Set<Restaurant> restaurants;
+    private Set<Restaurant> restaurants = new HashSet<>();
+
+    // Having an empty constructor is handy to work with identity maps / entity registries
+    public City() {
+    }

     public City(Integer id, String zipCode, String cityName) {
         this.id = id;
         this.zipCode = zipCode;
         this.cityName = cityName;
-        this.restaurants = new HashSet<>();
     }

     public Integer getId() {
@@ -32,10 +35,18 @@ public class City {
         return zipCode;
     }

+    public void setZipCode(String zipCode) {
+        this.zipCode = zipCode;
+    }
+
     public String getCityName() {
         return cityName;
     }

+    public void setCityName(String cityName) {
+        this.cityName = cityName;
+    }
+
     public Set<Restaurant> getRestaurants() {
         return restaurants;
     }
```

```diff
diff --git a/src/main/java/ch/hearc/ig/guideresto/business/CompleteEvaluation.java b/src/main/java/ch/hearc/ig/guideresto/business/CompleteEvaluation.java
index 6f8d04f..b8887f8 100644
--- a/src/main/java/ch/hearc/ig/guideresto/business/CompleteEvaluation.java
+++ b/src/main/java/ch/hearc/ig/guideresto/business/CompleteEvaluation.java
@@ -3,6 +3,7 @@ package ch.hearc.ig.guideresto.business;
 import ch.hearc.ig.guideresto.persistence.GradeMapper;

 import java.time.LocalDate;
+import java.util.HashSet;
 import java.util.Set;

 public class CompleteEvaluation extends Evaluation {
@@ -11,6 +12,12 @@ public class CompleteEvaluation extends Evaluation {
   private String username;
   private Set<Grade> grades;

+  // Having an empty constructor is handy to work with identity maps / entity registries
+  public CompleteEvaluation() {
+    super();
+    this.grades = new HashSet<>();
+  }
+
   public CompleteEvaluation(Integer id, LocalDate visitDate, Restaurant restaurant, String comment,
       String username) {
     super(id, visitDate, restaurant);
@@ -23,10 +30,18 @@ public class CompleteEvaluation extends Evaluation {
     return comment;
   }

+  public void setComment(String comment) {
+    this.comment = comment;
+  }
+
   public String getUsername() {
     return username;
   }

+  public void setUsername(String username) {
+    this.username = username;
+  }
+
   public Set<Grade> getGrades() {
     // lazy loading
     if (this.grades == null) {
```

```diff
diff --git a/src/main/java/ch/hearc/ig/guideresto/business/Evaluation.java b/src/main/java/ch/hearc/ig/guideresto/business/Evaluation.java
index 946e773..2db1f76 100644
--- a/src/main/java/ch/hearc/ig/guideresto/business/Evaluation.java
+++ b/src/main/java/ch/hearc/ig/guideresto/business/Evaluation.java
@@ -8,6 +8,8 @@ public abstract class Evaluation {
   private LocalDate visitDate;
   private Restaurant restaurant;

+  public Evaluation() {}
+
   public Evaluation(Integer id, LocalDate visitDate, Restaurant restaurant) {
     this.id = id;
     this.visitDate = visitDate;
@@ -28,7 +30,15 @@ public abstract class Evaluation {
     return visitDate;
   }

+  public void setVisitDate(LocalDate visitDate) {
+    this.visitDate = visitDate;
+  }
+
   public Restaurant getRestaurant() {
     return restaurant;
   }
+
+  public void setRestaurant(Restaurant restaurant) {
+    this.restaurant = restaurant;
+  }
 }
```

```diff
diff --git a/src/main/java/ch/hearc/ig/guideresto/business/EvaluationCriteria.java b/src/main/java/ch/hearc/ig/guideresto/business/EvaluationCriteria.java
index 82c13f2..9df8e5e 100644
--- a/src/main/java/ch/hearc/ig/guideresto/business/EvaluationCriteria.java
+++ b/src/main/java/ch/hearc/ig/guideresto/business/EvaluationCriteria.java
@@ -6,6 +6,9 @@ public class EvaluationCriteria {
     private String name;
     private String description;

+    // Having an empty constructor is handy to work with identity maps / entity registries
+    public EvaluationCriteria() {}
+
     public EvaluationCriteria(Integer id, String name, String description) {
         this.id = id;
         this.name = name;
@@ -16,11 +19,23 @@ public class EvaluationCriteria {
         return id;
     }

+    public void setId(Integer id) {
+        this.id = id;
+    }
+
     public String getName() {
         return name;
     }

+    public void setName(String name) {
+        this.name = name;
+    }
+
     public String getDescription() {
         return description;
     }
+
+    public void setDescription(String description) {
+        this.description = description;
+    }
 }
```

```diff
diff --git a/src/main/java/ch/hearc/ig/guideresto/business/Grade.java b/src/main/java/ch/hearc/ig/guideresto/business/Grade.java
index 8b4cd0f..ad70a5e 100644
--- a/src/main/java/ch/hearc/ig/guideresto/business/Grade.java
+++ b/src/main/java/ch/hearc/ig/guideresto/business/Grade.java
@@ -7,6 +7,9 @@ public class Grade {
     private CompleteEvaluation evaluation;
     private EvaluationCriteria criteria;

+    // Having an empty constructor is handy to work with identity maps / entity registries
+    public Grade() {}
+
     public Grade(Integer id, Integer grade, CompleteEvaluation evaluation, EvaluationCriteria criteria) {
         this.id = id;
         this.grade = grade;
@@ -28,11 +31,23 @@ public class Grade {
         return grade;
     }

+    public void setGrade(Integer grade) {
+        this.grade = grade;
+    }
+
     public EvaluationCriteria getCriteria() {
         return criteria;
     }

+    public void setCriteria(EvaluationCriteria criteria) {
+        this.criteria = criteria;
+    }
+
     public CompleteEvaluation getEvaluation() {
         return evaluation;
     }
+
+    public void setEvaluation(CompleteEvaluation evaluation) {
+        this.evaluation = evaluation;
+    }
 }
```

```diff
diff --git a/src/main/java/ch/hearc/ig/guideresto/business/Restaurant.java b/src/main/java/ch/hearc/ig/guideresto/business/Restaurant.java
index c93b21f..2cdd183 100644
--- a/src/main/java/ch/hearc/ig/guideresto/business/Restaurant.java
+++ b/src/main/java/ch/hearc/ig/guideresto/business/Restaurant.java
@@ -2,10 +2,10 @@ package ch.hearc.ig.guideresto.business;

 import ch.hearc.ig.guideresto.persistence.EvaluationMapper;

+import java.util.HashSet;
 import java.util.Set;

 public class Restaurant {
-
     private Integer id;
     private String name;
     private String description;
@@ -14,14 +14,19 @@ public class Restaurant {
     private Localisation address;
 import java.util.Set;

 public class Restaurant {
-
     private Integer id;
     private String name;
     private String description;
@@ -14,14 +14,19 @@ public class Restaurant {
     private Localisation address;
     private RestaurantType type;

+    // Having an empty constructor is handy to work with identity maps / entity registries
+    public Restaurant() {
+        this.evaluations = new HashSet<>();
+    }
+
     public Restaurant(Integer id, String name, String description, String website, String street, City city, RestaurantType type) {
         this.id = id;
         this.name = name;
         this.description = description;
         this.website = website;
-        this.evaluations = null; // lazy evaluation
         this.address = new Localisation(street, city);
         this.type = type;
+        this.evaluations = null; // lazy evaluation
     }

     public Integer getId() { return this.id; }
@@ -95,6 +100,10 @@ public class Restaurant {
         return address;
     }

+    public void setAddress(Localisation address) {
+        this.address = address;
+    }
+
     public RestaurantType getType() {
         return type;
     }
```

```diff
diff --git a/src/main/java/ch/hearc/ig/guideresto/business/RestaurantType.java b/src/main/java/ch/hearc/ig/guideresto/business/RestaurantType.java
index 1397ed3..23a45c7 100644
--- a/src/main/java/ch/hearc/ig/guideresto/business/RestaurantType.java
+++ b/src/main/java/ch/hearc/ig/guideresto/business/RestaurantType.java
@@ -2,6 +2,7 @@ package ch.hearc.ig.guideresto.business;

 import ch.hearc.ig.guideresto.persistence.RestaurantMapper;
index 1397ed3..23a45c7 100644
--- a/src/main/java/ch/hearc/ig/guideresto/business/RestaurantType.java
+++ b/src/main/java/ch/hearc/ig/guideresto/business/RestaurantType.java
@@ -2,6 +2,7 @@ package ch.hearc.ig.guideresto.business;

 import ch.hearc.ig.guideresto.persistence.RestaurantMapper;

+import java.util.HashSet;
 import java.util.Set;

 public class RestaurantType {
@@ -9,7 +10,12 @@ public class RestaurantType {
     private Integer id;
     private String label;
     private String description;
-    private Set<Restaurant> restaurants;
+    private Set<Restaurant> restaurants = null; // lazy evaluation...
+
+    // Having an empty constructor is handy to work with identity maps / entity registries
+    public RestaurantType() {
+        this.restaurants = new HashSet<>();
+    }

     public RestaurantType(Integer id, String label, String description) {
         this.id = id;
@@ -19,15 +25,27 @@ public class RestaurantType {
     }

     public Integer getId() { return this.id; }
-
+
+    public void setId(Integer id) {
+        this.id = id;
+    }
+
     public String getLabel() {
         return label;
     }

+    public void setLabel(String label) {
+        this.label = label;
+    }
+
     public String getDescription() {
         return description;
     }

+    public void setDescription(String description) {
+        this.description = description;
+    }
+
     public Set<Restaurant> getRestaurants() {
         if (this.restaurants == null) {
             this.restaurants = RestaurantMapper.findByType(this);
```