package com.gigamonkeys.bhs.tools;

import com.sun.net.httpserver.*;
import java.io.*;
import java.net.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Testing trivial web server. Probably better to use Jetty.
 */
public class Web {

  private static final Logger log = Logger.getLogger(Web.class.getName());

  static {
    System.setProperty("java.util.logging.SimpleFormatter.format",
                       "%1$tF %1$tT %4$s %2$s %5$s%6$s%n");
  }

  private static class MyHandler implements HttpHandler {
    public void handle(HttpExchange t) throws IOException {
      log.info("Received request: " + t.getRequestURI());

      InputStream is = t.getRequestBody();
      //read(is); // .. read the request body
      String response = "This is the response";
      t.sendResponseHeaders(200, response.length());
      OutputStream os = t.getResponseBody();
      os.write(response.getBytes());
      os.close();

      log.info("Response sent: " + response);
    }
  }

  public static void main(String[] args) throws Exception {
    log.setLevel(Level.ALL);

    HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
    server.createContext("/", new MyHandler());
    server.setExecutor(null); // creates a default executor
    server.start();

    log.info("Server started on port 8000");

  }

}
