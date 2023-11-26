package ch.hearc.ig.guideresto.application;

import ch.hearc.ig.guideresto.presentation.CLI;

import java.util.Scanner;

public class Main {

  public static void main(String[] args) {
    // using of var is a bad practice (loss of readability)
    var scanner = new Scanner(System.in);
    // var fakeItems = new FakeItems();
    var printStream = System.out;
    var cli = new CLI(scanner, printStream/*, fakeItems*/);

    cli.start();
  }
}
