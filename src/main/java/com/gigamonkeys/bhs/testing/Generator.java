package com.gigamonkeys.bhs.testing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.*;
import java.util.regex.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Generator {

  public static final String ALPHABET = "abcdefghijklmnopqrstuvwxyz";

  public static final String[] WORDS = {
    "COW", "DOG", "CAT", "BAT", "COG", "COT", "BOOT", "PENCIL", "TUNA", "UNITE", "PIG", "ZEBRA",
    "TUBE"
  };

  public static final String[] OTHER_WORDS = {"FISH", "FROG", "TURTLE", "EGG", "MARKER"};

  public String randomWord() {
    if (Math.random() < 0.5) {
      return randomElement(WORDS);
    } else {
      return randomElement(OTHER_WORDS);
    }
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

  public int[] randomInts(int size) {
    return IntStream.range(0, size).map(i -> (int) (Math.random() * 100)).toArray();
  }

  public ArrayList<Integer> randomList(int size) {
    return IntStream.range(0, size)
        .map(i -> (int) (Math.random() * 100))
        .boxed()
        .collect(Collectors.toCollection(ArrayList<Integer>::new));
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

  public Object[][] args(Supplier<Object[]> s) {
    return args(Stream.generate(s));
  }

  public Object[][] args1(Stream<?> s) {
    return args(s.map(x -> new Object[] {x}));
  }

  public Object[][] args(Stream<?> s) {
    return s.limit(10).toArray(Object[][]::new);
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
}
