package com.gigamonkeys.bhs.testing;

import java.util.List;

/**
 * Interface for classes that can produce test results.
 */
public interface Tester {

  public List<Testable> testables();

}
