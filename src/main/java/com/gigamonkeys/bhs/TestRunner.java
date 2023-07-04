package com.gigamonkeys.bhs;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

/*
 * A test runner that uses reflection to invoke methods on both a test class and
 * a reference class for a bunch of test cases arguments provided as JSON and
 * emits JSON of the results. Can run this as a main class passing the names of
 * the two classes and the path to the test case file or can instantiate one and
 * call outputResults()
 */
public class TestRunner {

  // For pretty-printed JSON use new GsonBuilder().setPrettyPrinting().create() instead of new Gson();
  private static final Gson gson = new Gson();
  private static final Type TEST_CASES_TYPE = new TypeToken<Map<String, TestCase[]>>() {}.getType();

  private final Class<?> testClass;
  private final Class<?> referenceClass;
  private final Object testObject;
  private final Object referenceObject;
  private final List<Method> referenceMethods;
  private final Map<String, TestCase[]> allTestCases;

  public TestRunner(String testClassName, String referenceClassName, String testCasesFile)
    throws Exception {
    this.testClass = Class.forName(testClassName);
    this.referenceClass = Class.forName(referenceClassName);
    this.testObject = testClass.getConstructor(new Class[0]).newInstance();
    this.referenceObject = referenceClass.getConstructor(new Class[0]).newInstance();
    this.allTestCases = gson.fromJson(Files.readString(Paths.get(testCasesFile)), TEST_CASES_TYPE);

    this.referenceMethods =
      Arrays
        .stream(referenceClass.getDeclaredMethods())
        .filter(method -> Modifier.isPublic(method.getModifiers()))
        .collect(Collectors.toList());
  }

  /*
   * A test case as it comes in in the JSON. We may add fields to this in future
   * for things like expected exceptions.
   */
  static record TestCase(JsonElement[] args) {
    public Object[] argsFor(Method m) {
      var types = m.getParameterTypes();
      var actualArgs = new Object[args.length];
      for (var i = 0; i < args.length; i++) {
        actualArgs[i] = gson.fromJson(args[i], types[i]);
      }
      return actualArgs;
    }
  }

  /*
   * The result of running one test case as it goes back in JSON. This structure
   * needs to match what the test result display code on the web expects.
   */
  static record TestResult(
    JsonElement[] args,
    JsonElement got,
    JsonElement expected,
    boolean passed
  ) {}

  /*
   * Actually testable combinations of a test method, a reference method, and
   * some test cases.
   */
  class Testable {

    private final Method testMethod;
    private final Method referenceMethod;
    private final TestCase[] cases;

    Testable(Method testMethod, Method referenceMethod, TestCase[] cases) {
      this.testMethod = testMethod;
      this.referenceMethod = referenceMethod;
      this.cases = cases;
    }

    public TestResult[] results() throws Exception {
      var results = new TestResult[cases.length];
      for (var i = 0; i < cases.length; i++) {
        results[i] = test(cases[i]);
      }
      return results;
    }

    public String name() {
      return testMethod.getName();
    }

    public TestResult test(TestCase testCase) throws Exception {
      var args = testCase.argsFor(testMethod);
      var got = testMethod.invoke(testObject, args);
      var expected = referenceMethod.invoke(referenceObject, args);
      return new TestResult(
        testCase.args(),
        gson.toJsonTree(got),
        gson.toJsonTree(expected),
        got.equals(expected)
      );
    }
  }

  public Map<String, TestResult[]> results() throws Exception {
    var allResults = new HashMap<String, TestResult[]>();
    for (Testable t : testables()) {
      allResults.put(t.name(), t.results());
    }
    return allResults;
  }

  public List<Testable> testables() {
    var testables = new ArrayList<Testable>();

    for (Method m : referenceMethods) {
      testMethod(m)
        .ifPresent(testMethod -> {
          TestCase[] cases = allTestCases.get(m.getName());
          if (cases != null) {
            testables.add(new Testable(testMethod, m, cases));
          }
        });
    }
    return testables;
  }

  public Optional<Method> testMethod(Method m) {
    try {
      return Optional.of(testClass.getDeclaredMethod(m.getName(), m.getParameterTypes()));
    } catch (NoSuchMethodException | SecurityException e) {
      return Optional.empty();
    }
  }

  public String resultsAsJson() throws Exception {
    return gson.toJson(results());
  }

  public void outputResults() throws Exception {
    System.out.println(resultsAsJson());
  }

  public static void main(String[] args) {
    try {
      new TestRunner(args[0], args[1], args[2]).outputResults();
    } catch (Exception e) {
      System.err.println("Exception while running tests.");
      e.printStackTrace(System.err);
    }
  }
}
