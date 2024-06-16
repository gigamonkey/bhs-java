package com.gigamonkeys.bhs.testing;

import static com.gigamonkeys.bhs.testing.Utils.*;

import com.gigamonkeys.bhs.BespokeTestRunner;
import com.gigamonkeys.bhs.Either;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.Map;
import java.util.function.Function;

/** Testable that tests a single method against testcases. */
public class MethodTestable implements Testable {

  private final Method method;
  private final Object proxy;
  private final Object referenceObject;
  private final Map<String, Object[][]> tests;
  private final Map<String, Function<Object[], String>> labelers;
  private final Map<String, SpecialCheck> specialChecks;

  public MethodTestable(
      Method method,
      Object proxy,
      Object referenceObject,
      Map<String, Object[][]> tests,
      Map<String, Function<Object[], String>> labelers,
      Map<String, SpecialCheck> specialChecks) {
    this.method = method;
    this.proxy = proxy;
    this.referenceObject = referenceObject;
    this.tests = tests;
    this.labelers = labelers;
    this.specialChecks = specialChecks;
  }

  public String name() {
    return method.getName();
  }

  public BespokeTestRunner.TestResult[] results() throws Exception {
    var r = new Results();
    var testcases = tests.get(name());

    if (testcases != null) {
      for (Object[] args : testcases) {
        String label = getLabel(name(), args);
        Object[] gotArgs = (Object[]) deepArrayCopy(args);
        Object[] expectedArgs = (Object[]) deepArrayCopy(args);
        boolean isVoid = method.getReturnType() == void.class;

        Object got;
        Object expected;
        boolean exception = false;

        if (!isVoid) {
          Either<Throwable, Object> eitherGot = invokeMethodWithException(method, proxy, gotArgs);
          if (eitherGot.isRight()) {
            got = eitherGot.getRight();

            var specialCheck = specialChecks.get(name());
            if (specialCheck != null) {
              r.add(
                  label + specialCheck.label(),
                  specialCheck.expected(),
                  specialCheck.got(got, gotArgs),
                  specialCheck.passed(got, gotArgs));
            }

          } else {
            got = eitherGot.getLeft();
            exception = true;
          }
          // FIXME: possibly should handle expected exceptions?
          expected = method.invoke(referenceObject, expectedArgs);
        } else {
          // void method we assume are intended to modify their arguments in
          // some way so we check whether the arguments we passed to each
          // version of the method are equivalent.
          method.invoke(proxy, gotArgs);
          method.invoke(referenceObject, expectedArgs);
          got = gotArgs;
          expected = expectedArgs;
        }

        r.add(label, limited(expected), limited(got), !exception && equivalent(got, expected));
      }
    } else {
      throw new Error("No tests for " + name());
    }
    return r.results();
  }

  private String getLabel(String name, Object[] args) {
    if (labelers.containsKey(name)) {
      return labelers.get(name).apply(args);
    } else {
      return name + "(" + argsToString(args) + ")";
    }
  }

  private Either<Throwable, Object> invokeMethodWithException(
      Method m, Object obj, Object... args) {
    try {
      return Either.right(m.invoke(obj, args));
    } catch (InvocationTargetException ite) {
      return Either.left(ite.getCause());
    } catch (Exception e) {
      return Either.left(unwrap(e));
    }
  }

  private Throwable unwrap(Throwable t) {
    if (t instanceof InvocationTargetException) {
      return unwrap(t.getCause());
    } else if (t instanceof UndeclaredThrowableException) {
      return unwrap(((UndeclaredThrowableException) t).getUndeclaredThrowable());
    } else {
      return t;
    }
  }

  // Copied from BespokeTestRunner.Tester for the moment. May end up living here
}
