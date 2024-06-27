package com.gigamonkeys.bhs.testing;

import java.util.List;

/**
 * Interface for classes that can produce test results.
 */
public interface Tester {

  public List<Testable> testables();

  /**
   * Make a instane of teh given class with a no-args constructor.
   */
  public static Object testObject(Class<?> clazz) {
    try {
      return clazz.getDeclaredConstructor(new Class[0]).newInstance();
    } catch (ReflectiveOperationException roe) {
      // FIXME: probably should make a custom exceeption that TestRunner catches
      // and reports the failure in some reasonable way
      throw new RuntimeException("Can't make test object", roe);
    }
  }

}
