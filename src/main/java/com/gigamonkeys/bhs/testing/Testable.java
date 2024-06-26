package com.gigamonkeys.bhs.testing;

import com.gigamonkeys.bhs.BespokeTestRunner;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Anything that is testable. In simple cases may be just the names of specific
 * methods. But could be properties of the class being tested that are assessed
 * without invoke methods on the test object.
 */
public interface Testable {

  public String name();

  public TestResult[] results() throws Exception;

  /** With default labelers and special checks */
  public static List<Testable> methodTestables(
      Class<?> testInterface,
      Object testObject,
      Object referenceObject,
      Map<String, Object[][]> tests) {
    return methodTestables(testInterface, testObject, referenceObject, tests, Map.of(), Map.of());
  }

  /** With specified labelers and default specialChecks. */
  public static List<Testable> methodTestables(
      Class<?> testInterface,
      Object testObject,
      Object referenceObject,
      Map<String, Object[][]> tests,
      Map<String, Function<Object[], String>> labelers) {

    Object proxy = BespokeTestRunner.getProxy(testInterface, testObject);
    return Arrays.stream(testInterface.getDeclaredMethods())
        .map(m -> methodToTest(m, testObject))
        .flatMap(Optional::stream)
        .map(m -> new MethodTestable(m, proxy, referenceObject, tests, labelers, Map.of()))
        .collect(Collectors.toList());
  }

  /** With specified labelers and special checks */
  public static List<Testable> methodTestables(
      Class<?> testInterface,
      Object testObject,
      Object referenceObject,
      Map<String, Object[][]> tests,
      Map<String, Function<Object[], String>> labelers,
      Map<String, SpecialCheck> specialChecks) {

    Object proxy = BespokeTestRunner.getProxy(testInterface, testObject);
    return Arrays.stream(testInterface.getDeclaredMethods())
        .map(m -> methodToTest(m, testObject))
        .flatMap(Optional::stream)
        .map(m -> new MethodTestable(m, proxy, referenceObject, tests, labelers, specialChecks))
        .collect(Collectors.toList());
  }

  // Check that the method exists on the class we are testing.
  private static Optional<Method> methodToTest(Method m, Object testObject) {
    try {
      testObject.getClass().getDeclaredMethod(m.getName(), m.getParameterTypes());
      return Optional.of(m);
    } catch (NoSuchMethodException | SecurityException e) {
      return Optional.empty();
    }
  }
}
