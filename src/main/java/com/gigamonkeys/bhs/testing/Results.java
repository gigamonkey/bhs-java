package com.gigamonkeys.bhs.testing;

import com.gigamonkeys.bhs.BespokeTestRunner;
import java.util.*;

public class Results {

  private List<BespokeTestRunner.TestResult> results = new ArrayList<>();

  public BespokeTestRunner.TestResult[] results() {
    return results.toArray(new BespokeTestRunner.TestResult[0]);
  }

  public Results add(String label, String expected, String unexpected, boolean passed) {
    results.add(
        new BespokeTestRunner.TestResult(label, passed ? expected : unexpected, expected, passed));
    return this;
  }

  public Results expectEquals(String label, Object expected, Optional<Object> got) {
    return add(
        label,
        String.valueOf(expected),
        String.valueOf(got.map(v -> String.valueOf(v)).orElse(null)),
        got.map(v -> v.equals(expected)).orElse(false));
  }
}
