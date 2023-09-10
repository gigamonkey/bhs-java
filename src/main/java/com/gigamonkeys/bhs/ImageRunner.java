package com.gigamonkeys.bhs;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import javax.imageio.ImageIO;

public class ImageRunner {

  // Generate an image and then generate an <img> tag with a data: url that we
  // can inject into a web page.
  public static void main(String[] args) throws Exception {
    var w = Integer.parseInt(args[1]);
    var h = Integer.parseInt(args[2]);
    var image = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);

    generator(args[0]).draw(image.createGraphics(), w, h);

    try {
      System.out.println(imageTag(image));
    } catch (IOException ioe) {
      System.out.println("<p>Errors occured in image conversion</p>");
    }
  }

  private static ImageGenerator generator(String className) throws ReflectiveOperationException {
    @SuppressWarnings("unchecked") // We want to throw the Class Cast Exception for now
    Class<ImageGenerator> clazz = (Class<ImageGenerator>) Class.forName(className);
    return (ImageGenerator) clazz.getDeclaredConstructor().newInstance();
  }

  private static String imageTag(BufferedImage image) throws IOException {
    var output = new ByteArrayOutputStream();
    ImageIO.write(image, "png", output);
    var data = Base64.getEncoder().encodeToString(output.toByteArray());
    return "<img src='data:image.png;base64," + data + "'/>";
  }

}
