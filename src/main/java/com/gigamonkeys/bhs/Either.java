package com.gigamonkeys.bhs;

import java.util.Optional;

public class Either<T, U> {

  public boolean isLeft() {
    return false;
  }

  public boolean isRight() {
    return false;
  }

  public T getLeft() {
    return null;
  }

  public U getRight() {
    return null;
  }

  public Optional<T> maybeLeft() {
    return Optional.ofNullable(getLeft());
  }

  public Optional<U> maybeRight() {
    return Optional.ofNullable(getRight());
  }

  private static class Left<T, U> extends Either<T, U> {
    private final T value;

    private Left(T value) {
      this.value = value;
    }

    public boolean isLeft() {
      return true;
    }

    public T getLeft() {
      return value;
    }
  }

  private static class Right<T, U> extends Either<T, U> {
    private final U value;

    private Right(U value) {
      this.value = value;
    }

    public boolean isRight() {
      return true;
    }

    public U getRight() {
      return value;
    }
  }

  public static <T, U> Either<T, U> left(T left) {
    return new Left<>(left);
  }

  public static <T, U> Either<T, U> right(U right) {
    return new Right<>(right);
  }
}
