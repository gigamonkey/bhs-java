package com.gigamonkeys.bhs;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.google.gson.Gson;

/*
 * A test runner that basically lets a test class exercise an instance of the
 * class being tested. Uses some reflection tricks to allow us to treat the
 * object under test as an instance of an interface provided by the test class
 * even though the class doesn't actually implement the interface and may not
 * have all the necessary methods since we want to be able to test a class
 * before all the methods we can test are written.
 *
 * This style of tester is more flexible than the regular TestRunner since it
 * can check for things like methods that have side effects on their arguments
 * and changes to the state of the object whereas TestRunner is really for
 * testing methods that are pure functions.
 */
public class BespokeTestRunner {

  public static interface Tester {
    public List<Testable> testables();
  }

  // For pretty-printed JSON use new GsonBuilder().setPrettyPrinting().create() instead of new Gson();
  private static final Gson gson = new Gson();

  //private final Class<Tester> testerClass;
  private final Tester tester;

  public BespokeTestRunner(Tester tester) {
    this.tester = tester;
  }

  @SuppressWarnings("unchecked")
  public BespokeTestRunner(String testerClassName) throws Exception {
    this(loadTester(testerClassName));
  }

  private static Tester loadTester(String name) throws Exception {
    var testerClass = (Class<Tester>)Class.forName(name);
    return testerClass.getConstructor(new Class[0]).newInstance();
  }

  public static record TestResult(String label, String got, String expected, boolean passed) {}

  @SuppressWarnings("unchecked")
  public static <T> T getProxy(Class<T> clazz, Object testObject) {
    // Argh. This is so gross!
    return (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class<?>[] { clazz }, new InvocationHandler() {
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
          // The .get() should be safe here because the only methods we should
          // try to invoke via the proxy are ones that we identified with
          // methodToTest in the first place.
          return methodToTest(method, testObject).get().invoke(testObject, args);
        }
      });
  }

  private static Optional<Method> methodToTest(Method m, Object testObject) {
    try {
      return Optional.of(testObject.getClass().getDeclaredMethod(m.getName(), m.getParameterTypes()));
    } catch (NoSuchMethodException | SecurityException e) {
      return Optional.empty();
    }
  }

  /*
   * Anything that is testable. In simple cases may be just the names of
   * specific methods. But could be properties of the class being tested that
   * are assessed without invoke methods on the test object.
   */
  public static interface Testable {

    public String name();

    public TestResult[] results() throws Exception;
  }

  private Map<String, TestResult[]> results() throws Exception {
    var allResults = new HashMap<String, TestResult[]>();
    for (Testable t : tester.testables()) {
      allResults.put(t.name(), Arrays.stream(t.results()).collect(Collectors.toList()).toArray(new TestResult[0]));
    }
    return allResults;
  }

  public String resultsAsJson() throws Exception {
    return gson.toJson(results());
  }

  public void outputResults() throws Exception {
    System.out.println(resultsAsJson());
  }

  public static void main(String[] args) {
    try {
      new BespokeTestRunner(args[0]).outputResults();
    } catch (Exception e) {
      System.err.println("Exception while running tests.");
      e.printStackTrace(System.err);
    }
  }
}
