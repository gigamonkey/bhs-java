package com.gigamonkeys.bhs;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

public class TestRunner {

  private static final Type TEST_CASES_TYPE = new TypeToken<Map<String, TestCase[]>>() {}.getType();

  private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

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

  static record TestResult(
    JsonElement[] args,
    JsonElement got,
    JsonElement expected,
    boolean passed
  ) {}

  static record TestClasses(Class<?> testClass, Class<?> referenceClass) {
    public Object testObject()
      throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
      return testClass.getConstructor(new Class[0]).newInstance();
    }

    public Object referenceObject()
      throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
      return referenceClass.getConstructor(new Class[0]).newInstance();
    }

    public List<Method> referenceMethods() {
      return Arrays
        .stream(referenceClass.getDeclaredMethods())
        .filter(method -> Modifier.isPublic(method.getModifiers()))
        .collect(Collectors.toList());
    }

    public Optional<Method> testMethod(Method m) {
      return methodFromClass(m, testClass);
    }

    public Optional<Method> referenceMethod(Method m) {
      return methodFromClass(m, referenceClass);
    }

    private Optional<Method> methodFromClass(Method m, Class<?> testClass) {
      try {
        return Optional.of(testClass.getDeclaredMethod(m.getName(), m.getParameterTypes()));
      } catch (NoSuchMethodException | SecurityException e) {
        return Optional.empty();
      }
    }
  }

  static record TestRun(String testClass, String referenceClass, String testCasesFile) {}

  public void run(TestRun run)
    throws ClassNotFoundException, IOException, NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
    var classes = loadClasses(run);
    var cases = loadTestCases(run.testCasesFile());

    var allResults = new HashMap<String, TestResult[]>();

    // Find all the public methods on the reference class
    // Find all the corresponding methods on the test class.
    // Look up the test cases by the method's name.

    Object testObject = classes.testObject();
    Object referenceObject = classes.referenceObject();

    for (Method m : classes.referenceMethods()) {
      var tm = classes.testMethod(m);
      System.out.println(m.getName() + ":\n  reference: " + m + "\n  test:" + tm);

      TestCase[] cs = cases.get(m.getName());
      System.out.print("  cases:");
      if (cs == null) {
        System.out.println(" none");
      } else {
        System.out.println();
        for (var i = 0; i < cs.length; i++) {
          System.out.println("    args: " + Arrays.deepToString(cs[i].args()));
        }

        if (tm.isPresent()) {
          var results = new TestResult[cs.length];
          for (var i = 0; i < cs.length; i++) {
            results[i] = test(testObject, referenceObject, tm.get(), m, cs[i]);
          }
          allResults.put(m.getName(), results);
        }
      }
    }
    // For each set of args, coerce array of JsonElements into an array of the appropriate types for the method arguments.
    // Make a TestResult object from the original args, and a JsonElement representing the got and expected values.
    System.out.println(gson.toJson(allResults));
  }

  private TestClasses loadClasses(TestRun run) throws ClassNotFoundException {
    return new TestClasses(Class.forName(run.testClass()), Class.forName(run.referenceClass()));
  }

  private Map<String, TestCase[]> loadTestCases(String filename) throws IOException {
    return gson.fromJson(Files.readString(Paths.get(filename)), TEST_CASES_TYPE);
  }

  private TestResult test(
    Object testObject,
    Object referenceObject,
    Method testMethod,
    Method referenceMethod,
    TestCase testCase
  ) throws IllegalAccessException, InvocationTargetException {
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

  public static void main(String[] args) throws Exception {
    var run = new TestRun(args[0], args[1], args[2]);
    new TestRunner().run(run);
  }
}
