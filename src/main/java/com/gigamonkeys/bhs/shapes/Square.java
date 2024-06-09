package com.gigamonkeys.bhs.shapes;

import java.awt.*;

/**
 * Simple square shape to be drawn on an image via a java.awt.Graphics object.
 */
public class Square {

  private final Graphics2D g;
  private int x;
  private int y;
  private int size;
  private Color color;
  private double rotation = 0;

  /**
   * Constructs a new Square with the specified graphics context, position,
   * size, and color.
   *
   * @param g      the graphics context to be used for drawing.
   * @param x      the x-coordinate of the top-left corner of the square.
   * @param y      the y-coordinate of the top-left corner of the square.
   * @param size   the side length of the square.
   * @param color  the color of the square.
   */
  public Square(Graphics g, int x, int y, int size, Color color) {
    this.g = (Graphics2D) g;
    this.x = x;
    this.y = y;
    this.size = size;
    this.color = color;
  }

  /**
   * Constructs a new Square with the specified graphics context, position,
   * size, and a default color of black.
   *
   * @param g      the graphics context to be used for drawing.
   * @param x      the x-coordinate of the top-left corner of the square.
   * @param y      the y-coordinate of the top-left corner of the square.
   * @param size   the side length of the square.
   */
  public Square(Graphics g, int x, int y, int size) {
    this(g, x, y, size, Color.BLACK);
  }

  /**
   * Returns the x-coordinate of the top-left corner of the square.
   *
   * @return the x-coordinate.
   */
  public int getX() {
    return x;
  }

  /**
   * Returns the y-coordinate of the top-left corner of the square.
   *
   * @return the y-coordinate.
   */
  public int getY() {
    return y;
  }

  /**
   * Returns the lenth of the side of the square.
   *
   * @return the size of the square.
   */
  public int getSize() {
    return size;
  }

  /**
   * Returns the color of the square.
   *
   * @return the current color.
   */
  public Color getColor() {
    return color;
  }

  /**
   * Returns the rotation angle of the square in radians.
   *
   * @return the rotation angle in radians.
   */
  public double getRotation() {
    return rotation;
  }

  /**
   * Sets the color of the square to the specified value.
   *
   * @param color the new color to set.
   */
  public void setColor(Color color) {
    this.color = color;
  }

  /**
   * Moves the square by the specified deltas along the x and y axes.
   *
   * @param dx the amount to move in the x direction.
   * @param dy the amount to move in the y direction.
   */
  public void move(int dx, int dy) {
    this.x += dx;
    this.y += dy;
  }

  /**
   * Sets the position of the square to the specified coordinates.
   *
   * @param x the new x-coordinate.
   * @param y the new y-coordinate.
   */
  public void moveTo(int x, int y) {
    this.x = x;
    this.y = y;
  }

  /**
   * Rotates the square by the specified radians around its center.
   *
   * @param radians the angle of rotation in radians.
   */
  public void rotate(double radians) {
    rotation += radians;
  }

  /**
   * Shrinks the size of the square by a percentage of its current size, keeping
   * its top-left corner in the same position.
   *
   * @param percentage the percentage to shrink, where 0.1 represents 10%.
   */
  public void shrink(double percentage) {
    size *= (1 - percentage);
  }

  /**
   * Shrinks the size of the square by a percentage of its current size, keeping
   * its center in the same position.
   *
   * @param percentage the percentage to shrink, where 0.1 represents 10%.
   */
  public void shrinkToCenter(double percentage) {
    int adj = (int) (size * percentage / 2);
    shrink(percentage);
    move(adj, adj);
  }

  /**
   * Increases the size of the square by a percentage of its current size,
   * keeping its top-left corner in the same position.
   *
   * @param percentage the percentage to increase, where 0.1 represents 10%.
   */
  public void embiggen(double percentage) {
    size *= (1 + percentage);
  }

  /**
   * Increases the size of the square by a percentage of its current size,
   * keeping its center in the same position.
   *
   * @param percentage the percentage to increase, where 0.1 represents 10%.
   */
  public void embiggenFromCenter(double percentage) {
    int adj = (int) (size * percentage / 2);
    embiggen(percentage);
    move(-adj, -adj);
  }

  /**
   * Draws the outline of the square using its current properties.
   */
  public void draw() {
    render(false);
  }

  /**
   * Fills the square using its current properties.
   */
  public void fill() {
    render(true);
  }

  /**
   * Renders the square on the graphics context, either filled or outlined.
   *
   * @param fill if true, the square is filled; otherwise, only the outline is drawn.
   */
  private void render(boolean fill) {
    var g2 = (Graphics2D) g.create();
    g2.setColor(color);
    g2.rotate(rotation, x + size / 2, y + size / 2);
    if (fill) {
      g2.fillRect(x, y, size, size);
    } else {
      g2.drawRect(x, y, size, size);
    }
    g2.dispose();
  }
}
