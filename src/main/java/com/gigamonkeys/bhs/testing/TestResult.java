package com.gigamonkeys.bhs.testing;

/**
 * A single test result.
 */
public record TestResult(String label, String got, String expected, boolean passed) {}
