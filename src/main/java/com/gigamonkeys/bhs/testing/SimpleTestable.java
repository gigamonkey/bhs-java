package com.gigamonkeys.bhs.testing;

import com.gigamonkeys.bhs.BespokeTestRunner;
import java.util.Map;
import java.util.function.Supplier;

/** Implementation of Testable that pushes all the work to the tests which just supply results. */
public class SimpleTestable implements Testable {

  private final String name;
  private final Map<String, Supplier<TestResult[]>> tests;

  public SimpleTestable(String name, Map<String, Supplier<TestResult[]>> tests) {
    this.name = name;
    this.tests = tests;
  }

  public String name() {
    return this.name;
  }

  public TestResult[] results() throws Exception {
    return tests.get(name).get();
  }
}
