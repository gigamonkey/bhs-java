package com.gigamonkeys.bhs;

import java.awt.Graphics;

public interface ImageGenerator {
  /**
   * Draw a picture on the given Graphics object.
   *
   * @param g Graphics on which to draw.
   */
  public void draw(Graphics g, int width, int height);
}
