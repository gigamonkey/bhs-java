package com.gigamonkeys.bhs;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

public class TestRunner {

  private static final Type TEST_CASES_TYPE = new TypeToken<Map<String, TestCase[]>>() {}.getType();
  private static final Type TEST_RESULTS_TYPE = new TypeToken<Map<String, TestResult[]>>() {}
    .getType();

  private final Gson gson = new Gson();

  static record TestCase(JsonElement[] args) {}

  static record TestResult(JsonElement[] args, JsonElement got, JsonElement expected) {}

  static record TestClasses(Class<?> testClass, Class<?> referenceClass) {}

  static record TestRun(String testClass, String referenceClass, String testCasesFile) {}

  public void run(TestRun run) throws ClassNotFoundException, IOException {
    //var classes = loadClasses(run);
    var cases = loadTestCases(run.testCasesFile());

    for (Map.Entry<String, TestCase[]> entry : cases.entrySet()) {
      System.out.println("Method: " + entry.getKey());
      for (TestCase testCase : entry.getValue()) {
        System.out.println("Args: " + Arrays.deepToString(testCase.args()));
      }
    }
    // Find all the public methods on the reference class
    // Find all the corresponding methods on the test class.
    // Look up the test cases by the method's name.
    // For each set of args, coerce array of JsonElements into an array of the appropriate types for the method arguments.
    // Make a TestResult object from the original args, and a JsonElement representing the got and expected values.

  }

  private TestClasses loadClasses(TestRun run) throws ClassNotFoundException {
    return new TestClasses(Class.forName(run.testClass()), Class.forName(run.referenceClass()));
  }

  private Map<String, TestCase[]> loadTestCases(String filename) throws IOException {
    return gson.fromJson(Files.readString(Paths.get(filename)), TEST_CASES_TYPE);
  }

  private String testResultsToJson(Map<String, TestResult[]> results) {
    return gson.toJson(results);
  }

  public static void main(String[] args) throws Exception {
    var run = new TestRun(args[0], args[1], args[2]);
    new TestRunner().run(run);
  }
}
