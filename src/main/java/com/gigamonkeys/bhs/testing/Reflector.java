package com.gigamonkeys.bhs.testing;

import com.gigamonkeys.bhs.Either;
import java.lang.reflect.*;
import java.util.*;
import java.util.function.*;

public class Reflector {

  private Class<?> clazz;

  public Reflector(Class<?> clazz) {
    this.clazz = clazz;
  }

  public Field getField(String name) {
    try {
      Field f = clazz.getDeclaredField(name);
      f.setAccessible(true);
      return f;
    } catch (NoSuchFieldException nsfe) {
      return null;
    }
  }

  public Optional<Object> getFieldValue(String name, Optional<Object> obj) {
    Field f = getField(name);
    if (f != null) {
      return obj.map(
          o -> {
            try {
              return f.get(o);
            } catch (IllegalAccessException iae) {
              throw new Error(iae);
            }
          });
    } else {
      return Optional.empty();
    }
  }

  public Constructor<?> getConstructor(Class<?>... classes) {
    try {
      Constructor<?> c = clazz.getDeclaredConstructor(classes);
      c.setAccessible(true);
      return c;
    } catch (NoSuchMethodException nsfe) {
      return null;
    }
  }

  public Method getMethod(String name, Class<?>... classes) {
    try {
      Method m = clazz.getDeclaredMethod(name, classes);
      m.setAccessible(true);
      return m;
    } catch (NoSuchMethodException nsfe) {
      return null;
    }
  }

  public Method[] actuallyDeclaredMethods() {
    return Arrays.stream(clazz.getDeclaredMethods())
        .filter(m -> !m.isBridge() && !m.isSynthetic())
        .toArray(Method[]::new);
  }

  public Optional<Object> callConstructor(Constructor<?> c, Object... args) {
    try {
      return Optional.of(c.newInstance(args));
    } catch (Exception e) {
      return Optional.empty();
    }
  }

  public Optional<Object> invokeMethod(Method m, Object obj, Object... args) {
    try {
      return Optional.of(m.invoke(obj, args));
    } catch (Exception e) {
      return Optional.empty();
    }
  }

  public Either<Throwable, Object> invokeMethodWithException(Method m, Object obj, Object... args) {
    try {
      return Either.right(m.invoke(obj, args));
    } catch (InvocationTargetException ite) {
      return Either.left(ite.getCause());
    } catch (Exception e) {
      return Either.left(e);
    }
  }

  public Optional<Object> size(Optional<Object> o) {
    Method m = getMethod("size", new Class<?>[0]);
    return o.flatMap(obj -> invokeMethod(m, obj));
  }

  public int getElement(Optional<Object> oo, int i) {
    Field f = getField("numbers");
    if (oo.isPresent() && f != null) {
      try {
        return ((int[]) f.get(oo.get()))[i];
      } catch (ArrayIndexOutOfBoundsException aioobe) {
        throw aioobe;
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    } else {
      throw new RuntimeException("Can't get element.");
    }
  }
}
