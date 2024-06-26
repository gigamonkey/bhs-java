package com.gigamonkeys.bhs.testing;

import com.gigamonkeys.bhs.BespokeTestRunner;
import com.gigamonkeys.bhs.Either;
import java.lang.reflect.*;
import java.util.*;
import java.util.function.*;

public class Results {

  private static class Empty {}

  private List<TestResult> results = new ArrayList<>();

  private Reflector r;

  public Results() {
    // Kludge for now. Some tests don't use any of the reflection methods so
    // don't need a class.
    this(Empty.class);
  }

  public Results(Class<?> clazz) {
    this.r = new Reflector(clazz);
  }

  public TestResult[] results() {
    return results.toArray(new TestResult[0]);
  }

  public Results add(String label, String expected, String unexpected, boolean passed) {
    results.add(
        new TestResult(label, passed ? expected : unexpected, expected, passed));
    return this;
  }

  public Results expectPrivateInstanceVar(String name, Class<?> type) {
    Field f = r.getField(name);
    add(name, "Has " + name + " instance variable.", "No variable named " + name + ".", f != null);
    if (f != null) {
      expectPrivate(f);
      expectNotStatic(f);
      expectType(name + " type", type, f.getType());
    }
    return this;
  }

  public Results expectGetter(Optional<Object> oo, String name, String fieldName, Class<?> type) {
    Method m = r.getMethod(name);
    add(name + " exists", "Has getter " + name, "No getter " + name, m != null);
    if (m != null) {
      expectPublic(m);
      expectNotStatic(m);
      expectType(name + " return type", type, m.getReturnType());

      Optional<Object> fieldValue = r.getFieldValue(fieldName, oo);
      Optional<Object> got = oo.flatMap(o -> r.invokeMethod(m, o));
      expectEquals("return value", fieldValue.orElse(null), got);
    }
    return this;
  }

  public Results expectSetter(
      Optional<Object> oo, String name, String fieldName, Object value, Class<?> type) {
    Method m = r.getMethod(name, new Class<?>[] {type});

    add(name + " exists", "Has setter " + name, "No setter " + name, m != null);
    if (m != null) {
      expectPublic(m);
      expectNotStatic(m);
      expectType(name + " return type", void.class, m.getReturnType());
      Class<?>[] params = m.getParameterTypes();
      add(
          name + " one arg",
          "Takes one argument",
          "Takes " + params.length + " argument(s)",
          params.length == 1);
      if (params.length == 1) {
        expectType(name + " arg type", type, params[0]);
      }

      oo.flatMap(o -> r.invokeMethod(m, o, value));
      expectFieldValue("value after set", oo, fieldName, value);
    }
    return this;
  }

  public Results expectPublic(Class<?> m) {
    String expected = "public access modifier";
    return add(
        "is public", expected, "no " + expected, m.accessFlags().contains(AccessFlag.PUBLIC));
  }

  public Results expectPublic(Member m) {
    String expected = "public access modifier";
    return add(
        "is public", expected, "no " + expected, m.accessFlags().contains(AccessFlag.PUBLIC));
  }

  public Results expectPrivate(Member m) {
    String expected = "private access modifier";
    return add(
        m.getName() + " is private",
        expected,
        "no " + expected,
        m.accessFlags().contains(AccessFlag.PRIVATE));
  }

  public Results expectNotStatic(Member m) {
    String notExpected = "static modifier";
    return add(
        m.getName() + " is not static",
        "no " + notExpected,
        notExpected,
        !m.accessFlags().contains(AccessFlag.STATIC));
  }

  public Results expectType(String label, Class<?> expected, Class<?> got) {
    return add(label, readableClassName(expected), readableClassName(got), expected == got);
  }

  public Results expectEquals(String label, Object expected, Optional<Object> got) {
    return add(
        label,
        String.valueOf(expected),
        String.valueOf(got.map(v -> String.valueOf(v)).orElse(null)),
        got.map(v -> v.equals(expected)).orElse(false));
  }

  public <T, U> Results expectEquals(String label, Object expected, Either<T, U> got) {
    return add(
        label,
        String.valueOf(expected),
        String.valueOf(got.getRight()),
        got.maybeRight().map(v -> v.equals(expected)).orElse(false));
  }

  public Results expectEqualsObj(String label, Object expected, Object got) {
    return add(label, String.valueOf(expected), String.valueOf(got), Objects.equals(expected, got));
  }

  public Results expectDoubleEquals(String label, double expected, Optional<Object> got) {
    Optional<Double> gotD =
        got.flatMap(
            obj -> {
              return obj instanceof Double ? Optional.of((Double) obj) : Optional.empty();
            });

    return add(
        label,
        String.valueOf(expected),
        String.valueOf(got.map(v -> String.valueOf(v)).orElse(null)),
        gotD.map(v -> Math.abs(v - expected) < 0.0001).orElse(false));
  }

  public <T, U> Results expectException(
      String label, Either<T, U> got, Class<? extends Exception> ex) {
    return add(
        label,
        readableClassName(ex),
        got.maybeLeft().map(v -> readableClassName(v.getClass())).orElse("null"),
        got.maybeLeft().map(v -> ex.equals(v.getClass())).orElse(false));
  }

  public Results expectFieldValue(String label, Optional<Object> o, String field, Object expected) {
    Field f = r.getField(field);
    if (o.isPresent()) {
      try {
        Object got = f != null ? f.get(o.get()) : null;
        add(label, String.valueOf(expected), String.valueOf(got), expected.equals(got));
      } catch (ReflectiveOperationException roe) {
        // ignoring
      }
    } else {
      add(label, String.valueOf(expected), "<no object>", false);
    }
    return this;
  }

  public Results expectFieldValueIf(
      String label, Optional<Object> o, String field, Predicate<Object> expected) {
    Field f = r.getField(field);
    if (o.isPresent()) {
      try {
        Object got = f != null ? f.get(o.get()) : null;
        add(label, "as expected", String.valueOf(got), expected.test(got));
      } catch (ReflectiveOperationException roe) {
        // ignoring
      }
    } else {
      add(label, String.valueOf(expected), "<no object>", false);
    }
    return this;
  }

  public Results expectElement(String label, Optional<Object> o, int i, int v) {
    int got = r.getElement(o, i);
    String base = "number at " + i + " ";
    return add(label, base + "correct", base + "incorrect", got == v);
  }

  private static String readableClassName(Class<?> clazz) {
    if (clazz == null) return "null";
    if (clazz.isArray()) {
      Class<?> arrayComponentType = clazz.getComponentType();
      return readableClassName(arrayComponentType) + "[]";
    } else {
      String full = clazz.getName();
      int dot = full.lastIndexOf(".");
      return dot == -1 ? full : full.substring(dot + 1);
    }
  }
}
