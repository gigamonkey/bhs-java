package com.gigamonkeys.bhs.testing;

/**
 * Special checks used in MethodTestable to check qualities of the return value and the arguments
 * (e.g. for side effects on the args.)
 */
public abstract class SpecialCheck {

  private final String label;
  private final String expected;

  SpecialCheck(String label, String expected) {
    this.label = label;
    this.expected = expected;
  }

  public final String label() {
    return label;
  }

  public final String expected() {
    return expected;
  }

  /** Do the check. Return true if it passed, false otherwise. */
  public abstract boolean passed(Object returned, Object[] args);

  /**
   * The string to be displayed as 'got' result if passed return false. May be based on the returned
   * value or the args or may just be a fixed string.
   */
  public abstract String got(Object returned, Object[] args);
}
