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
    return text.append(FOOTER).toString();
  }

  /**
   * Draws a line from x1,y1 to x2,y2 using the give color and the given line width.
   */
  public void drawLine(double x1, double y1, double x2, double y2, String color, double lineWidth) {
    strokeStyle(color);
    lineWidth(lineWidth);
    beginPath();
    text.append("ctx.moveTo(").append(x1).append(", ").append(y1).append(");\n");
    text.append("ctx.lineTo(").append(x2).append(", ").append(y2).append(");\n");
    stroke();
  }

  /**
   * Draws a circle centered at x,y with radius r using the given color and the given lineWidth.
   */
  public void drawCircle(double x, double y, double r, String color, double lineWidth) {
    strokeStyle(color);
    lineWidth(lineWidth);
    beginPath();
    circle(x, y, r);
    stroke();
  }

  /**
   * Draws a rectangle starting at x,y with the given width, height, color, and
   * lineWidth. Positive widths go to the right and negative to the left;
   * positive heights go down and negative heights go up.
   */
  public void drawRect(
    double x,
    double y,
    double width,
    double height,
    String color,
    double lineWidth
  ) {
    strokeStyle(color);
    lineWidth(lineWidth);
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
    fillStyle(color);
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
    fillStyle(color);
    beginPath();
    circle(x, y, r);
    fill();
  }

  // Primimitives. May replace these with something smarter later so donn't want
  // to make them public.

  private void strokeStyle(String color) {
    text.append("ctx.strokeStyle = '").append(color).append("';\n");
  }

  private void fillStyle(String color) {
    text.append("ctx.fillStyle = '").append(color).append("';\n");
  }

  private void lineWidth(double lineWidth) {
    text.append("ctx.lineWidth = ").append(lineWidth).append(";\n");
  }

  private void circle(double x, double y, double r) {
    ellipse(x, y, r, r, 0, 0, 2 * Math.PI);
  }

  private void ellipse(
    double x,
    double y,
    double radiusX,
    double radiusY,
    double rotation,
    double startAngle,
    double endAngle
  ) {
    text
      .append("ctx.ellipse(")
      .append(x)
      .append(", ")
      .append(y)
      .append(", ")
      .append(radiusX)
      .append(", ")
      .append(radiusY)
      .append(", ")
      .append(rotation)
      .append(", ")
      .append(startAngle)
      .append(", ")
      .append(endAngle)
      .append(");\n");
  }

  private void beginPath() {
    text.append("ctx.beginPath();\n");
  }

  private void stroke() {
    text.append("ctx.stroke();\n");
  }

  private void fill() {
    text.append("ctx.fill();\n");
  }
}
