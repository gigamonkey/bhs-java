package com.gigamonkeys.bhs.testing;

import com.gigamonkeys.bhs.BespokeTestRunner;

/**
 * Anything that is testable. In simple cases may be just the names of
 * specific methods. But could be properties of the class being tested that
 * are assessed without invoke methods on the test object.
 */
public interface Testable {

    public String name();

    public BespokeTestRunner.TestResult[] results() throws Exception;
}
