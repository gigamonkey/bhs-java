package com.gigamonkeys.bhs;

public class CanvasRunner {

  public static void main(String[] args) throws Exception {
    @SuppressWarnings("unchecked") // We want to throw the Class Cast Exception for now
    Class<Picture> clazz = (Class<Picture>) Class.forName(args[0]);
    double width = Double.parseDouble(args[1]);
    double height = Double.parseDouble(args[2]);
    var c = new Canvas(width, height);
    ((Picture) clazz.getDeclaredConstructor().newInstance()).draw(c);
    System.out.println(c.code());
  }
}
