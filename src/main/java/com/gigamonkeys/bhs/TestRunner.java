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

public class TestRunner {

  private static final Type TEST_CASES_TYPE = new TypeToken<Map<String, TestCase[]>>() {}.getType();

  // For pretty-printed JSON use new GsonBuilder().setPrettyPrinting().create() instead of new Gson();
  private static final Gson gson = new Gson();

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

  private static class TestClasses {

    private final Class<?> testClass;
    private final Class<?> referenceClass;
    private final Object testObject;
    private final Object referenceObject;
    private final List<Method> referenceMethods;

    TestClasses(String testClassName, String referenceClassName) throws Exception {
      this.testClass = Class.forName(testClassName);
      this.referenceClass = Class.forName(referenceClassName);
      this.testObject = testClass.getConstructor(new Class[0]).newInstance();
      this.referenceObject = referenceClass.getConstructor(new Class[0]).newInstance();
      this.referenceMethods =
        Arrays
          .stream(referenceClass.getDeclaredMethods())
          .filter(method -> Modifier.isPublic(method.getModifiers()))
          .collect(Collectors.toList());
    }

    /*
     * Methods from the reference class that exist both in the test class and
     * the test cases.
     */
    public List<Method> testableMethods(Map<String, TestCase[]> cases) {
      var methods = new ArrayList<Method>();

      for (Method m : referenceMethods) {
        testMethod(m)
          .ifPresent(tm -> {
            TestCase[] cs = cases.get(m.getName());
            if (cs != null) {
              methods.add(m);
            }
          });
      }
      return methods;
    }

    public TestResult test(Method method, TestCase testCase) throws Exception {
      var tm = testMethod(method);
      if (tm.isPresent()) {
        var testMethod = tm.get();
        var args = testCase.argsFor(method);
        var got = testMethod.invoke(testObject, args);
        var expected = method.invoke(referenceObject, args);
        return new TestResult(
          testCase.args(),
          gson.toJsonTree(got),
          gson.toJsonTree(expected),
          got.equals(expected)
        );
      } else {
        throw new IllegalArgumentException("Called test on nontestable method.");
      }
    }

    public Optional<Method> testMethod(Method m) {
      return methodFromClass(m, testClass);
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

  public void run(TestRun run) throws Exception {
    var classes = new TestClasses(run.testClass(), run.referenceClass());
    var cases = loadCases(run);

    var allResults = new HashMap<String, TestResult[]>();

    for (Method m : classes.testableMethods(cases)) {
      TestCase[] cs = cases.get(m.getName());
      if (cs != null) {
        var results = new TestResult[cs.length];
        for (var i = 0; i < cs.length; i++) {
          results[i] = classes.test(m, cs[i]);
        }
        allResults.put(m.getName(), results);
      } else {
        System.err.println("No test cases for " + m.getName());
      }
    }
    System.out.println(gson.toJson(allResults));
  }

  private Map<String, TestCase[]> loadCases(TestRun run) throws Exception {
    return gson.fromJson(Files.readString(Paths.get(run.testCasesFile())), TEST_CASES_TYPE);
  }

  public static void main(String[] args) {
    try {
      new TestRunner().run(new TestRun(args[0], args[1], args[2]));
    } catch (Exception e) {
      System.err.println("Exception while running tests.");
      e.printStackTrace(System.err);
    }
  }
}
