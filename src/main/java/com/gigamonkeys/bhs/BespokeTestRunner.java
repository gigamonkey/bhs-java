package com.gigamonkeys.bhs;

import com.gigamonkeys.bhs.testing.Testable;
import com.gigamonkeys.bhs.testing.Tester;
import com.gigamonkeys.bhs.testing.TestResult;
import com.google.gson.Gson;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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
 *
 * This class can be used as a main class getting passed the classname of the
 * Tester class on the command line which it then instantiates and runs. Or it
 * can be used programatically and passed an already instantiate Tester
 * instance.
 */
public class BespokeTestRunner {

  // For pretty-printed JSON use new GsonBuilder().setPrettyPrinting().create() instead of new
  // Gson();
  private static final Gson gson = new Gson();

  // private final Class<Tester> testerClass;
  private final Tester tester;

  public BespokeTestRunner(Tester tester) {
    this.tester = tester;
  }

  @SuppressWarnings("unchecked")
  public BespokeTestRunner(String testerClassName) throws Exception {
    this(loadTester(testerClassName));
  }

  private static Tester loadTester(String name) throws Exception {
    var testerClass = (Class<Tester>) Class.forName(name);
    return testerClass.getConstructor(new Class[0]).newInstance();
  }

  @SuppressWarnings("unchecked")
  public static <T> T getProxy(Class<T> clazz, Object testObject) {
    // Argh. This is so gross!
    return (T)
        Proxy.newProxyInstance(
            clazz.getClassLoader(),
            new Class<?>[] {clazz},
            new InvocationHandler() {
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
      return Optional.of(
          testObject.getClass().getDeclaredMethod(m.getName(), m.getParameterTypes()));
    } catch (NoSuchMethodException | SecurityException e) {
      return Optional.empty();
    }
  }

  private Map<String, TestResult[]> results() throws Exception {
    var allResults = new HashMap<String, TestResult[]>();
    for (Testable t : tester.testables()) {
      allResults.put(
          t.name(),
          Arrays.stream(t.results()).collect(Collectors.toList()).toArray(new TestResult[0]));
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
