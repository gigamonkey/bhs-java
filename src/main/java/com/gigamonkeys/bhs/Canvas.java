package com.gigamonkeys.bhs;

public class Canvas {

  private static final String HEADER =
    """
      (() => {
        const canvas = document.querySelector('canvas');
        const r = canvas.parentElement.getBoundingClientRect();
        canvas.setAttribute('width', r.width - 2);
        canvas.setAttribute('height', r.height - 2);
        const ctx = canvas.getContext('2d');
        const width = canvas.width;
        const height = canvas.height;
        ctx.clearRect(0, 0, width, height);
        """;

  private static final String FOOTER = "})();";

  private final StringBuilder text = new StringBuilder();
  private final double width;
  private final double height;

  public Canvas(double width, double height) {
    this.width = width;
    this.height = height;
    text.append(HEADER);
  }

  public double width() {
    return width;
  }

  public double height() {
    return height;
  }

  public String code() {
    text.append(FOOTER);
    return text.toString();
  }

  /**
   * Draws a line from x1,y1 to x2,y2 using the give color and the give line width.
   */
  public void drawLine(double x1, double y1, double x2, double y2, String color, double lineWidth) {
    text.append("ctx.strokeStyle = '").append(color).append("';\n");
    text.append("ctx.lineWidth = ").append(lineWidth).append(";\n");
    text.append("ctx.beginPath();\n");
    text.append("ctx.moveTo(").append(x1).append(", ").append(y1).append(");\n");
    text.append("ctx.lineTo(").append(x2).append(", ").append(y2).append(");\n");
    text.append("ctx.stroke();\n");
  }

  /**
   * Draws a circle centered at x,y with radius r using the given color. The
   * fith argument, lineWidth, is optional and defaults to 1.
   */
  public void drawCircle(double x, double y, double r, String color, double lineWidth) {
    text.append("ctx.strokeStyle = '").append(color).append("';\n");
    text.append("ctx.lineWidth = ").append(lineWidth).append(";\n");
    text.append("ctx.beginPath();\n");
    text
      .append("ctx.ellipse(")
      .append(x)
      .append(", ")
      .append(y)
      .append(", ")
      .append(r)
      .append(", ")
      .append(r)
      .append(", 0, 0, 2 * Math.PI);\n");
    text.append("ctx.stroke();\n");
  }

  /**
   * Draws a rectangle starting at x,y with the given width, height, and color.
   * Positive widths go to the right and negative to the left; positive heights
   * go down and negative heights go up. The sixth argument, lineWidth, is
   * optional and defaults to 1.
   */
  public void drawRect(
    double x,
    double y,
    double width,
    double height,
    String color,
    double lineWidth
  ) {
    text.append("ctx.strokeStyle = '").append(color).append("';\n");
    text.append("ctx.lineWidth = ").append(lineWidth).append(";\n");
    text
      .append("ctx.strokeRect(")
      .append(x)
      .append(", ")
      .append(y)
      .append(", ")
      .append(width)
      .append(", ")
      .append(height)
      .append(");\n");
  }

  /**
   * Draws a filled rectangle starting at x,y with the given width, height, and
   * color. Positive widths go to the right and negative to the left; positive
   * heights go down and negative heights go up.
   */
  public void drawFilledRect(double x, double y, double width, double height, String color) {
    text.append("ctx.fillStyle = '").append(color).append("';\n");
    text
      .append("ctx.fillRect(")
      .append(x)
      .append(", ")
      .append(y)
      .append(", ")
      .append(width)
      .append(", ")
      .append(height)
      .append(");\n");
  }

  /**
   * Draws a filled circle centered at x,y with radius r using the given color.
   */
  public void drawFilledCircle(double x, double y, double r, String color) {
    text.append("ctx.fillStyle = '").append(color).append("';\n");
    text.append("ctx.beginPath();\n");
    text
      .append("ctx.ellipse(")
      .append(x)
      .append(", ")
      .append(y)
      .append(", ")
      .append(r)
      .append(", ")
      .append(r)
      .append(", 0, 0, 2 * Math.PI);\n");
    text.append("ctx.fill();\n");
  }
}
