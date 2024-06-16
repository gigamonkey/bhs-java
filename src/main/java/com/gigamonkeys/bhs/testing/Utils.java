package com.gigamonkeys.bhs.testing;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;

/** Static utility methods. */
public class Utils {

  public static String argsToString(Object[] args) {
    var sb = new StringBuilder();
    for (int i = 0; i < args.length; i++) {
      sb.append(anyToString(args[i]));
      if (i < args.length - 1) {
        sb.append(", ");
      }
    }
    return sb.toString();
  }

  public static String anyToString(Object o) {
    if (o == null) {
      return String.valueOf(o);
    } else {
      var c = o.getClass();
      if (c.isArray()) {
        var comp = c.getComponentType();
        if (comp == int.class) {
          return Arrays.toString((int[]) o);
        } else if (comp == long.class) {
          return Arrays.toString((long[]) o);
        } else if (comp == short.class) {
          return Arrays.toString((short[]) o);
        } else if (comp == char.class) {
          return Arrays.toString((char[]) o);
        } else if (comp == byte.class) {
          return Arrays.toString((byte[]) o);
        } else if (comp == boolean.class) {
          return Arrays.toString((boolean[]) o);
        } else if (comp == double.class) {
          return Arrays.toString((double[]) o);
        } else if (comp == float.class) {
          return Arrays.toString((float[]) o);
        } else {
          Object[] ss = Arrays.stream((Object[]) o).map(Utils::anyToString).toArray();
          return Arrays.toString(ss);
        }
      } else if (c == String.class) {
        return "\"" + o + "\"";
      } else {
        return String.valueOf(o);
      }
    }
  }

  public static String limited(Object o) {
    return anyToString(o).replaceFirst("(?<=^.{70,}) .+? (?=.{10,20}$)", " ... ");
  }

  public static Object deepArrayCopy(Object o) {
    if (o == null) {
      return o;
    } else {
      var c = o.getClass();
      if (c.isArray()) {
        var comp = c.getComponentType();
        if (comp == int.class) {
          return Arrays.copyOf((int[]) o, ((int[]) o).length);
        } else if (comp == long.class) {
          return Arrays.copyOf((long[]) o, ((long[]) o).length);
        } else if (comp == short.class) {
          return Arrays.copyOf((short[]) o, ((short[]) o).length);
        } else if (comp == char.class) {
          return Arrays.copyOf((char[]) o, ((char[]) o).length);
        } else if (comp == byte.class) {
          return Arrays.copyOf((byte[]) o, ((byte[]) o).length);
        } else if (comp == boolean.class) {
          return Arrays.copyOf((boolean[]) o, ((boolean[]) o).length);
        } else if (comp == double.class) {
          return Arrays.copyOf((double[]) o, ((double[]) o).length);
        } else if (comp == float.class) {
          return Arrays.copyOf((float[]) o, ((float[]) o).length);
        } else if (comp == String.class) {
          return Arrays.copyOf((String[]) o, ((String[]) o).length);
        } else {
          Object[] orig = (Object[]) o;
          Object[] copy = (Object[]) Array.newInstance(comp, orig.length);
          for (int i = 0; i < copy.length; i++) {
            copy[i] = deepArrayCopy(orig[i]);
          }
          return copy;
        }
      } else if (o instanceof ArrayList<?>) {
        return new ArrayList<>((ArrayList<?>) o);
      } else {
        return o;
      }
    }
  }

  public static boolean equivalent(Object got, Object expected) {
    if (got == expected) {
      return true;
    } else if (got == null || expected == null) {
      return false;

    } else if (expected.getClass() != got.getClass()) {
      return false;

    } else {
      var c = expected.getClass();
      if (c.isArray()) {
        var comp = c.getComponentType();
        if (comp == int.class) {
          return Arrays.equals((int[]) got, (int[]) expected);
        } else if (comp == long.class) {
          return Arrays.equals((long[]) got, (long[]) expected);
        } else if (comp == short.class) {
          return Arrays.equals((short[]) got, (short[]) expected);
        } else if (comp == char.class) {
          return Arrays.equals((char[]) got, (char[]) expected);
        } else if (comp == byte.class) {
          return Arrays.equals((byte[]) got, (byte[]) expected);
        } else if (comp == boolean.class) {
          return Arrays.equals((boolean[]) got, (boolean[]) expected);
        } else if (comp == double.class) {
          return Arrays.equals((double[]) got, (double[]) expected);
        } else if (comp == float.class) {
          return Arrays.equals((float[]) got, (float[]) expected);
        } else {
          return Arrays.deepEquals((Object[]) got, (Object[]) expected);
        }
      } else {
        return expected.equals(got);
      }
    }
  }
}
