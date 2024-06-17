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

  public char randomChar() {
    return ALPHABET.charAt((int) (Math.random() * ALPHABET.length()));
  }

  public char randomOtherCharacter(int c) {
    while (true) {
      char o = ALPHABET.charAt((int) (Math.random() * ALPHABET.length()));
      if (o != c) return o;
    }
  }

  public String randomWord() {
    if (Math.random() < 0.5) {
      return randomElement(WORDS);
    } else {
      return randomElement(OTHER_WORDS);
    }
  }

  public int randomCase(int c) {
    return Math.random() < 0.5 ? Character.toUpperCase(c) : Character.toLowerCase(c);
  }

  public String randomCase(String s) {
    return s.codePoints()
        .map(this::randomCase)
        .mapToObj(Character::toString)
        .collect(Collectors.joining());
  }

  public String maybePlural(String s) {
    return Math.random() < 0.5 ? s : s + "s";
  }

  public String string(int min, int max) {
    char[] chars = new char[between(min, max)];
    for (int i = 0; i < chars.length; i++) {
      chars[i] = randomChar();
    }
    return new String(chars);
  }

  public Stream<String> strings(int min, int max) {
    return Stream.generate(() -> string(min, max));
  }

  public String palindrome(String s) {
    return s + new StringBuilder(s).reverse();
  }

  public int[] palindromeInts(int min, int max) {
    int[] ns = new int[between(min, max)];
    for (int i = 0; i < ns.length / 2; i++) {
      int n = between(0, 100);
      ns[i] = n;
      ns[ns.length - 1 - i] = n;
    }
    return ns;
  }

  public Stream<String> maybePalindromes(int min, int max) {
    return Stream.generate(
        () -> Math.random() < 0.5 ? string(min, max) : palindrome(string(min, max / 2)));
  }

  public Stream<int[]> maybeIntsPalindromes(int min, int max) {
    return Stream.generate(
        () -> Math.random() < 0.5 ? randomInts(min, max) : palindromeInts(min, max));
  }

  public ArrayList<String> randomStrings(String letters, int size) {
    return IntStream.range(0, size)
        .mapToObj(i -> randomString(letters))
        .collect(Collectors.toCollection(ArrayList<String>::new));
  }

  public String randomString(String letters) {
    return randomString(letters, 0, 10);
  }

  public String randomString(String letters, int min, int max) {
    return IntStream.range(0, min + random(max - min))
        .mapToObj(i -> randomLetter(letters))
        .collect(Collectors.joining());
  }

  public String randomSubstring(String s) {
    int start = random(s.length());
    int left = s.length() - start;
    int end = start + Math.max(1, random(left));
    return s.substring(start, end);
  }

  public String randomLetter(String s) {
    int i = random(s.length());
    return s.substring(i, i + 1);
  }

  public String randomLetter() {
    return randomLetter(ALPHABET);
  }

  public String randomWithXs() {
    List<String> s = new ArrayList<>(Arrays.asList(randomString(ALPHABET).split("")));
    int num = random(10);
    for (int i = 0; i < num; i++) {
      s.add("x");
    }
    Collections.shuffle(s);
    return s.stream().collect(Collectors.joining());
  }

  public int random(int min, int max) {
    return min + (int) (Math.random() * (max - min));
  }

  public int random(int max) {
    return random(0, max);
  }

  public int randomInt(int limit) {
    return (int) (Math.random() * limit);
  }

  public int randomSign() {
    return Math.random() < 0.5 ? 1 : -1;
  }

  public int[] randomInts(int size) {
    return IntStream.range(0, size).map(i -> (int) (Math.random() * 100)).toArray();
  }

  public int[] randomInts(int minLen, int maxLen) {
    return randomInts(minLen, maxLen, 0, 100);
  }

  public int[] randomInts(int minLen, int maxLen, int min, int max) {
    int[] ns = new int[between(minLen, maxLen)];
    for (int i = 0; i < ns.length; i++) {
      ns[i] = between(min, max);
    }
    return ns;
  }

  public ArrayList<Integer> randomList(int size) {
    return IntStream.range(0, size)
        .map(i -> (int) (Math.random() * 100))
        .boxed()
        .collect(Collectors.toCollection(ArrayList<Integer>::new));
  }

  public ArrayList<Integer> randomList(int min, int max) {
    return IntStream.range(0, random(min, max))
        .map(i -> (int) (Math.random() * 100))
        .boxed()
        .collect(Collectors.toCollection(ArrayList<Integer>::new));
  }

  public ArrayList<Integer> randomListOfIntegers(int size, int min, int max) {
    return Stream.generate(() -> random(min, max))
        .limit(size)
        .collect(Collectors.toCollection(ArrayList::new));
  }

  public Stream<ArrayList<String>> randomStrings(int num, String[] words) {
    return Stream.generate(() -> randomListOfStrings((int) (Math.random() * 10), words)).limit(num);
  }

  public String[] randomWords(int size, String[] words) {
    return IntStream.range(0, size).mapToObj(i -> randomElement(words)).toArray(String[]::new);
  }

  public ArrayList<String> randomListOfStrings(int num, String[] words) {
    return Stream.generate(() -> randomElement(words))
        .map(this::randomCase)
        .limit(num)
        .collect(Collectors.toCollection(ArrayList::new));
  }

  public String[][] randomGrid(int rows, int cols) {
    String[][] grid = new String[rows][cols];
    for (int i = 0; i < rows; i++) {
      for (int j = 0; j < cols; j++) {
        grid[i][j] = randomLetter(ALPHABET);
      }
    }
    return grid;
  }

  public int[][] randomIntGrid(int rows, int cols) {
    int[][] grid = new int[rows][cols];
    for (int i = 0; i < rows; i++) {
      for (int j = 0; j < cols; j++) {
        grid[i][j] = random(-10, 10);
      }
    }
    return grid;
  }

  public boolean[][] randomBooleanGrid(int rows, int cols) {
    boolean[][] grid = new boolean[rows][cols];
    for (int i = 0; i < rows; i++) {
      for (int j = 0; j < cols; j++) {
        grid[i][j] = Math.random() < 0.5;
      }
    }
    return grid;
  }

  public boolean[][] fullGrid(int rows, int cols) {
    boolean[][] grid = new boolean[rows][cols];
    for (int i = 0; i < rows; i++) {
      for (int j = 0; j < cols; j++) {
        grid[i][j] = true;
      }
    }
    return grid;
  }

  public <T> T randomElement(T[] ts) {
    return ts[(int) (Math.random() * ts.length)];
  }

  public <T> T randomElement(List<T> ts) {
    return ts.get((int) (Math.random() * ts.size()));
  }

  public int randomElement(int[] ints) {
    return ints[(int) (Math.random() * ints.length)];
  }

  public <T> T randomElementNot(T[] ts, T exclude) {
    while (true) {
      T t = randomElement(ts);
      if (!t.equals(exclude)) return t;
    }
  }

  public Object[][] args(Supplier<Object[]> s) {
    return args(Stream.generate(s));
  }

  public Object[][] args1(Supplier<Object> s) {
    return args1(Stream.generate(s));
  }

  public Object[][] args(Stream<?> s) {
    return s.limit(10).toArray(Object[][]::new);
  }

  public Object[][] args1(Stream<?> s) {
    return args(s.map(x -> new Object[] {x}));
  }

  public Object[][] args(IntStream s) {
    return s.mapToObj(n -> new Object[] {n}).limit(10).toArray(Object[][]::new);
  }

  public Object[][] args(DoubleStream s) {
    return s.mapToObj(n -> new Object[] {n}).limit(10).toArray(Object[][]::new);
  }

  public int[] randomSorted(int size) {
    int[] nums = new int[size];
    int start = random(-50, 50);
    int n = start;
    for (int i = 0; i < size; i++) {
      nums[i] = n;
      n += random(2, 10);
    }
    return nums;
  }

  public int randomNotInNums(int[] nums) {
    if (nums.length == 0) {
      return random(100);
    } else {
      int idx = random(nums.length + 1);
      return idx < nums.length ? nums[idx] - 1 : nums[idx - 1] + 1;
    }
  }

  public Stream<int[]> ints(int minLen, int maxLen) {
    return ints(minLen, maxLen, 0, 100);
  }

  public Stream<int[]> ints(int minLen, int maxLen, int min, int max) {
    return Stream.generate(() -> randomInts(minLen, maxLen, min, max));
  }

  public IntStream intsBetween(int a, int b) {
    return IntStream.generate(() -> between(a, b));
  }

  public int between(int min, int max) {
    return (int) doubleBetween(min, max);
  }

  public double doubleBetween(double min, double max) {
    return min + (Math.random() * (max - min));
  }

  public <T> T[] permute(T[] ts) {
    List<T> copy = new ArrayList<>(Arrays.asList(ts));
    Collections.shuffle(copy);
    return copy.toArray(Arrays.copyOf(ts, 0));
  }

  public <T> Stream<T[]> permutations(T[] ts) {
    return Stream.generate(() -> permute(ts));
  }

  public Stream<String> shuffledWords(String[] words) {
    var list = new ArrayList<>(Arrays.asList(words));
    return Stream.generate(
        () -> {
          Collections.shuffle(list);
          return String.join(" ", list);
        });
  }

  public double[] randomDoubles(int min, int max) {
    double[] ds = new double[between(min, max)];
    for (int i = 0; i < ds.length; i++) {
      ds[i] = doubleBetween(-100.0, 100.0);
    }
    return ds;
  }

  public Stream<double[]> doubles(int min, int max) {
    return Stream.generate(() -> randomDoubles(min, max));
  }

  public DoubleStream doublesBetween(int a, int b) {
    return DoubleStream.generate(() -> doubleBetween(a, b));
  }

  public DoubleStream percentages() {
    return DoubleStream.generate(() -> Math.random());
  }

  public int[] smallDigits(int size) {
    int[] ds = new int[size];
    for (int i = 0; i < ds.length; i++) {
      ds[i] = (int) (Math.random() * 4);
    }
    return ds;
  }

  public int[] digits(int size) {
    int[] ds = new int[size];
    for (int i = 0; i < ds.length; i++) {
      ds[i] = (int) (Math.random() * 10);
    }
    return ds;
  }

  public int[] numbers(int size, int min, int max) {
    int[] ns = new int[size];
    for (int i = 0; i < ns.length; i++) {
      ns[i] = min + (int) (Math.random() * (max - min));
    }
    return ns;
  }

  public Object[][] tk() {
    return new Object[0][];
  }

  @SafeVarargs
  @SuppressWarnings("varargs")
  public final Supplier<Object[]> gen(Supplier<Object>... suppliers) {
    return () -> {
      return Arrays.stream(suppliers).map(Supplier::get).toArray();
    };
  }
}
