package com.gigamonkeys.bhs.testing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Generator {

  public static final String ALPHABET = "abcdefghijklmnopqrstuvwxyz";

  public static final String[] WORDS = {
    "COW", "DOG", "CAT", "BAT", "COG", "COT", "BOOT", "PENCIL", "TUNA", "UNITE", "PIG", "ZEBRA",
    "TUBE"
  };

  public static final String[] OTHER_WORDS = {"FISH", "FROG", "TURTLE", "EGG", "MARKER"};

  public static char randomChar() {
    return ALPHABET.charAt((int) (Math.random() * ALPHABET.length()));
  }

  public static char randomOtherCharacter(int c) {
    while (true) {
      char o = ALPHABET.charAt((int) (Math.random() * ALPHABET.length()));
      if (o != c) return o;
    }
  }

  public static String randomWord() {
    if (Math.random() < 0.5) {
      return randomElement(WORDS);
    } else {
      return randomElement(OTHER_WORDS);
    }
  }

  public static int randomCase(int c) {
    return Math.random() < 0.5 ? Character.toUpperCase(c) : Character.toLowerCase(c);
  }

  public static String randomCase(String s) {
    return s.codePoints()
        .map(Generator::randomCase)
        .mapToObj(Character::toString)
        .collect(Collectors.joining());
  }

  public static String maybePlural(String s) {
    return Math.random() < 0.5 ? s : s + "s";
  }

  public static String string(int min, int max) {
    char[] chars = new char[between(min, max)];
    for (int i = 0; i < chars.length; i++) {
      chars[i] = randomChar();
    }
    return new String(chars);
  }

  public static Stream<String> strings(int min, int max) {
    return Stream.generate(() -> string(min, max));
  }

  public static String palindrome(String s) {
    return s + new StringBuilder(s).reverse();
  }

  public static int[] palindromeInts(int min, int max) {
    int[] ns = new int[between(min, max)];
    for (int i = 0; i < ns.length / 2; i++) {
      int n = between(0, 100);
      ns[i] = n;
      ns[ns.length - 1 - i] = n;
    }
    return ns;
  }

  public static Stream<String> maybePalindromes(int min, int max) {
    return Stream.generate(
        () -> Math.random() < 0.5 ? string(min, max) : palindrome(string(min, max / 2)));
  }

  public static Stream<int[]> maybeIntsPalindromes(int min, int max) {
    return Stream.generate(
        () -> Math.random() < 0.5 ? randomInts(min, max) : palindromeInts(min, max));
  }

  public static ArrayList<String> randomStrings(String letters, int size) {
    return IntStream.range(0, size)
        .mapToObj(i -> randomString(letters))
        .collect(Collectors.toCollection(ArrayList<String>::new));
  }

  public static String randomString(String letters) {
    return randomString(letters, 0, 10);
  }

  public static String randomString(String letters, int min, int max) {
    return IntStream.range(0, min + random(max - min))
        .mapToObj(i -> randomLetter(letters))
        .collect(Collectors.joining());
  }

  public static String randomSubstring(String s) {
    int start = random(s.length());
    int left = s.length() - start;
    int end = start + Math.max(1, random(left));
    return s.substring(start, end);
  }

  public static String randomLetter(String s) {
    int i = random(s.length());
    return s.substring(i, i + 1);
  }

  public static String randomLetter() {
    return randomLetter(ALPHABET);
  }

  public static String randomWithXs() {
    List<String> s = new ArrayList<>(Arrays.asList(randomString(ALPHABET).split("")));
    int num = random(10);
    for (int i = 0; i < num; i++) {
      s.add("x");
    }
    Collections.shuffle(s);
    return s.stream().collect(Collectors.joining());
  }

  public static int random(int min, int max) {
    return min + (int) (Math.random() * (max - min));
  }

  public static int random(int max) {
    return random(0, max);
  }

  public static int randomInt(int limit) {
    return (int) (Math.random() * limit);
  }

  public static int randomSign() {
    return Math.random() < 0.5 ? 1 : -1;
  }

  public static int[] randomInts(int size) {
    return IntStream.range(0, size).map(i -> (int) (Math.random() * 100)).toArray();
  }

  public static int[] randomInts(int minLen, int maxLen) {
    return randomInts(minLen, maxLen, 0, 100);
  }

  public static int[] randomInts(int minLen, int maxLen, int min, int max) {
    int[] ns = new int[between(minLen, maxLen)];
    for (int i = 0; i < ns.length; i++) {
      ns[i] = between(min, max);
    }
    return ns;
  }

  public static ArrayList<Integer> randomList(int size) {
    return IntStream.range(0, size)
        .map(i -> (int) (Math.random() * 100))
        .boxed()
        .collect(Collectors.toCollection(ArrayList<Integer>::new));
  }

  public static ArrayList<Integer> randomList(int min, int max) {
    return IntStream.range(0, random(min, max))
        .map(i -> (int) (Math.random() * 100))
        .boxed()
        .collect(Collectors.toCollection(ArrayList<Integer>::new));
  }

  public static ArrayList<Integer> randomListOfIntegers(int size, int min, int max) {
    return Stream.generate(() -> random(min, max))
        .limit(size)
        .collect(Collectors.toCollection(ArrayList::new));
  }

  public static Stream<ArrayList<String>> randomStrings(int num, String[] words) {
    return Stream.generate(() -> randomListOfStrings((int) (Math.random() * 10), words)).limit(num);
  }

  public static String[] randomWords(int size, String[] words) {
    return IntStream.range(0, size).mapToObj(i -> randomElement(words)).toArray(String[]::new);
  }

  public static ArrayList<String> randomListOfStrings(int num, String[] words) {
    return Stream.generate(() -> randomElement(words))
        .map(Generator::randomCase)
        .limit(num)
        .collect(Collectors.toCollection(ArrayList::new));
  }

  public static String[][] randomGrid(int rows, int cols) {
    String[][] grid = new String[rows][cols];
    for (int i = 0; i < rows; i++) {
      for (int j = 0; j < cols; j++) {
        grid[i][j] = randomLetter(ALPHABET);
      }
    }
    return grid;
  }

  public static int[][] randomIntGrid(int rows, int cols) {
    int[][] grid = new int[rows][cols];
    for (int i = 0; i < rows; i++) {
      for (int j = 0; j < cols; j++) {
        grid[i][j] = random(-10, 10);
      }
    }
    return grid;
  }

  public static boolean randomBoolean() {
    return Math.random() < 0.5;
  }

  public static boolean[][] randomBooleanGrid(int rows, int cols) {
    boolean[][] grid = new boolean[rows][cols];
    for (int i = 0; i < rows; i++) {
      for (int j = 0; j < cols; j++) {
        grid[i][j] = Math.random() < 0.5;
      }
    }
    return grid;
  }

  public static boolean[][] fullGrid(int rows, int cols) {
    boolean[][] grid = new boolean[rows][cols];
    for (int i = 0; i < rows; i++) {
      for (int j = 0; j < cols; j++) {
        grid[i][j] = true;
      }
    }
    return grid;
  }

  public static <T> T randomElement(T[] ts) {
    return ts[(int) (Math.random() * ts.length)];
  }

  public static <T> T randomElement(List<T> ts) {
    return ts.get((int) (Math.random() * ts.size()));
  }

  public static int randomElement(int[] ints) {
    return ints[(int) (Math.random() * ints.length)];
  }

  public static <T> T randomElementNot(T[] ts, T exclude) {
    while (true) {
      T t = randomElement(ts);
      if (!t.equals(exclude)) return t;
    }
  }

  public static Object[][] args(Supplier<Object[]> s) {
    return args(Stream.generate(s));
  }

  public static Object[][] args1(Supplier<Object> s) {
    return args1(Stream.generate(s));
  }

  public static Object[][] args(Stream<?> s) {
    return s.limit(10).toArray(Object[][]::new);
  }

  public static Object[][] args1(Stream<?> s) {
    return args(s.map(x -> new Object[] {x}));
  }

  public static Object[][] args(IntStream s) {
    return s.mapToObj(n -> new Object[] {n}).limit(10).toArray(Object[][]::new);
  }

  public static Object[][] args(DoubleStream s) {
    return s.mapToObj(n -> new Object[] {n}).limit(10).toArray(Object[][]::new);
  }

  public static int[] randomSorted(int size) {
    int[] nums = new int[size];
    int start = random(-50, 50);
    int n = start;
    for (int i = 0; i < size; i++) {
      nums[i] = n;
      n += random(2, 10);
    }
    return nums;
  }

  public static int randomNotInNums(int[] nums) {
    if (nums.length == 0) {
      return random(100);
    } else {
      int idx = random(nums.length + 1);
      return idx < nums.length ? nums[idx] - 1 : nums[idx - 1] + 1;
    }
  }

  public static Stream<int[]> ints(int minLen, int maxLen) {
    return ints(minLen, maxLen, 0, 100);
  }

  public static Stream<int[]> ints(int minLen, int maxLen, int min, int max) {
    return Stream.generate(() -> randomInts(minLen, maxLen, min, max));
  }

  public static IntStream intsBetween(int a, int b) {
    return IntStream.generate(() -> between(a, b));
  }

  public static int between(int min, int max) {
    return (int) doubleBetween(min, max);
  }

  public static double doubleBetween(double min, double max) {
    return min + (Math.random() * (max - min));
  }

  public static <T> T[] permute(T[] ts) {
    List<T> copy = new ArrayList<>(Arrays.asList(ts));
    Collections.shuffle(copy);
    return copy.toArray(Arrays.copyOf(ts, 0));
  }

  public static <T> Stream<T[]> permutations(T[] ts) {
    return Stream.generate(() -> permute(ts));
  }

  public static Stream<String> shuffledWords(String[] words) {
    var list = new ArrayList<>(Arrays.asList(words));
    return Stream.generate(
        () -> {
          Collections.shuffle(list);
          return String.join(" ", list);
        });
  }

  public static double[] randomDoubles(int min, int max) {
    double[] ds = new double[between(min, max)];
    for (int i = 0; i < ds.length; i++) {
      ds[i] = doubleBetween(-100.0, 100.0);
    }
    return ds;
  }

  public static Stream<double[]> doubles(int min, int max) {
    return Stream.generate(() -> randomDoubles(min, max));
  }

  public static DoubleStream doublesBetween(int a, int b) {
    return DoubleStream.generate(() -> doubleBetween(a, b));
  }

  public static DoubleStream percentages() {
    return DoubleStream.generate(() -> Math.random());
  }

  public static int[] smallDigits(int size) {
    int[] ds = new int[size];
    for (int i = 0; i < ds.length; i++) {
      ds[i] = (int) (Math.random() * 4);
    }
    return ds;
  }

  public static int[] digits(int size) {
    int[] ds = new int[size];
    for (int i = 0; i < ds.length; i++) {
      ds[i] = (int) (Math.random() * 10);
    }
    return ds;
  }

  public static int[] numbers(int size, int min, int max) {
    int[] ns = new int[size];
    for (int i = 0; i < ns.length; i++) {
      ns[i] = min + (int) (Math.random() * (max - min));
    }
    return ns;
  }

  public static Object[][] tk() {
    return new Object[0][];
  }

  @SafeVarargs
  @SuppressWarnings("varargs")
  public static final Supplier<Object[]> gen(Supplier<Object>... suppliers) {
    return () -> {
      return Arrays.stream(suppliers).map(Supplier::get).toArray();
    };
  }
}
