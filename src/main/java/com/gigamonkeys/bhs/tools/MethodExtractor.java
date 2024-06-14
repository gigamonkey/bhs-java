package com.gigamonkeys.bhs.tools;

import com.sun.source.tree.*;
import com.sun.source.util.*;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import javax.tools.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


// Original code supplied by ChatGPT 4o: https://chatgpt.com/share/17e61a89-9bc0-43d1-8a99-08625048ecb3

public class MethodExtractor {

  public static void main(String[] args) {
    if (args.length != 2) {
      System.out.println("Usage: java MethodExtractorWithJavac <filename> <methodName>");
      return;
    }

    String filename = args[0];
    String methodName = args[1];

    try {
      String methodText = extractMethod(filename, methodName);
      if (methodText != null) {
        System.out.println(filename + "\t" + methodName + "\t" + getSHA1Hash(methodText));
      } else {
        System.out.println(filename + "\t" + methodName + "\t");
      }
    } catch (IOException | NoSuchAlgorithmException e) {
      e.printStackTrace();
    }
  }

  private static String extractMethod(String filename, String methodName) throws IOException {
    JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
    StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);
    Iterable<? extends JavaFileObject> fileObjects =
        fileManager.getJavaFileObjectsFromStrings(Arrays.asList(filename));

    JavacTask task = (JavacTask) compiler.getTask(null, fileManager, null, null, null, fileObjects);
    MethodFinder finder = new MethodFinder(methodName);

    try {
      Iterable<? extends CompilationUnitTree> parseResults = task.parse();
      for (CompilationUnitTree compilationUnitTree : parseResults) {
        finder.scan(compilationUnitTree, null);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }

    return finder.getMethodSource();
  }


  private static String getSHA1Hash(String input) throws NoSuchAlgorithmException {
    MessageDigest md = MessageDigest.getInstance("SHA-1");
    byte[] messageDigest = md.digest(input.getBytes());
    StringBuilder sb = new StringBuilder();
    for (byte b : messageDigest) {
      sb.append(String.format("%02x", b));
    }
    return sb.toString();
  }

  private static class MethodFinder extends TreePathScanner<Void, Void> {
    private final String methodName;
    private String methodSource;

    public MethodFinder(String methodName) {
      this.methodName = methodName;
    }

    @Override
    public Void visitMethod(MethodTree methodTree, Void p) {
      if (methodTree.getName().toString().equals(methodName)) {
        methodSource = methodTree.toString();
      }
      return super.visitMethod(methodTree, p);
    }

    public String getMethodSource() {
      return methodSource;
    }
  }
}
