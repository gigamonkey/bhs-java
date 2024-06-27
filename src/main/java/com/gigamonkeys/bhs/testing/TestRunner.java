package com.gigamonkeys.bhs.testing;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * API for running tests from either Java source code text, the name of a .java
 * file, a classname, or instantiated instance of the class to test. Note that
 * the Testers are not written in terms of any specific class or interface
 * because we want to be able to test partial code (and also don't want to have
 * to introduce students to interfaces early on since they're not even part of
 * the AP curriculum.)
 */
public class TestRunner {

  private static final boolean PRETTY_JSON = true;

  private static final Gson gson = PRETTY_JSON
    ? new GsonBuilder().setPrettyPrinting().create()
    : new Gson();

  /**
   * Compile a string of Java source code and load as class.
   */
  public static Class<?> classFromSource(String code) throws ClassNotFoundException {
    throw new Error("nyi");
  }

  /**
   * Compile the source in a file and load it as a class.
   */
  public static Class<?> classFromPath(Path path) throws ClassNotFoundException, IOException {
    return classFromSource(Files.readString(path));
  }

  /**
   * Load the named class from the current classpath.
   */
  public static Class<?> classFromClassname(String name) throws ClassNotFoundException {
    return Class.forName(name);
  }

  /**
   * Load the named class using the given ClassLoader. Useful if some code wants
   * to run tests using classes in a jar or directory.
   */
  public static Class<?> classFromClassname(String name, ClassLoader loader) throws ClassNotFoundException {
    return loader.loadClass(name);
  }



  public void runTests(Class<Tester> testerClass, Class<?> toTestClass) throws Exception {
    // This is the basic protocol. Tester classes need to have a constructor
    // that takes the class to be tested. The Tester class is responsible for
    // instantiating the class to be tested. Often that will just mean invoking
    // the no-args constructor but in some cases the tester may make multiple
    // instances with different arguments or something. (We haven't actually
    // done that yet.)
    Tester tester = testerClass.getDeclaredConstructor(Class.class).newInstance(toTestClass);
    System.out.println(resultsAsJson(tester));
  }

  private Map<String, TestResult[]> results(Tester tester) throws Exception {
    var allResults = new HashMap<String, TestResult[]>();
    for (Testable t : tester.testables()) {
      allResults.put(
          t.name(),
          Arrays.stream(t.results()).collect(Collectors.toList()).toArray(new TestResult[0]));
    }
    return allResults;
  }

  public String resultsAsJson(Tester tester) throws Exception {
    return gson.toJson(results(tester));
  }

  public static void main(String[] args) throws Exception {
    Class<Tester> testerClass = (Class<Tester>)classFromClassname(args[0]);
    Class<?> toTestClass = classFromClassname(args[1]);
    new TestRunner().runTests(testerClass, toTestClass);

  }

}
