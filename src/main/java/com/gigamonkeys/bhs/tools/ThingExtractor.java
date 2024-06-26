package com.gigamonkeys.bhs.tools;

import com.sun.source.tree.*;
import com.sun.source.util.*;
import java.io.IOException;
import java.nio.file.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.function.*;
import javax.tools.*;

/**
 * Tool for extracting bits of source code from .java files. Originally written
 * to find the duplicate code I had created by copying and pasting test classes
 * for assignments during the school year. May also be useful for analyzing
 * student code.
 *
 * Adapted from starter code supplied by ChatGPT 4o:
 * https://chatgpt.com/share/17e61a89-9bc0-43d1-8a99-08625048ecb3
 *
 */
public class ThingExtractor {

  private static final JavaCompiler COMPILER = ToolProvider.getSystemJavaCompiler();
  private static final StandardJavaFileManager FILE_MANAGER =
      COMPILER.getStandardFileManager(null, null, null);

  private static record Thing(
      String filename, String what, String name, String fullName, String code) {

    public String asTSV() {
      return String.join("\t", what, filename, name, fullName, getSHA1Hash(code));
    }

    public String asText() {
      return String.join(
          "\n",
          "What: " + what,
          "Name: " + name,
          "Full name: " + fullName,
          "File: " + filename,
          "Hash: " + getSHA1Hash(code),
          "\n",
          code);
    }
  }

  private static class ThingCollector {

    final String filename;
    List<Thing> things = new ArrayList<>();

    ThingCollector(String filename) {
      this.filename = filename;
    }

    void collect(String what, String name, String fullName, String code) {
      things.add(new Thing(filename, what, name, fullName, code));
    }
  }

  /** Exception thrown to generate a user-visible failure message. */
  private static class Failure extends RuntimeException {
    Failure(String msg, Throwable cause) {
      super(msg, cause);
    }
  }

  public static void main(String[] argv) {
    try {

      ThingExtractor extractor = new ThingExtractor();
      List<String> args = new ArrayList<>(Arrays.asList(argv));
      Function<Thing, String> emitter = Thing::asTSV;

      if (args.getFirst().equals("--text")) {
        emitter = Thing::asText;
      }

      if (args.getFirst().startsWith("--")) {
        args = args.subList(1, args.size());
      }

      extractor.emit(args, emitter);

    } catch (Failure f) {
      System.out.println(f.getMessage() + ": " + f.getCause());
      System.exit(1);
    }
  }

  private void emit(List<String> filenames, Function<Thing, String> f) {
    for (var filename : filenames) {
      for (var t : allThings(filename)) {
        System.out.println(f.apply(t));
      }
    }
  }

  private List<Thing> allThings(String filename) {
    try {
      var fileObjects = FILE_MANAGER.getJavaFileObjectsFromStrings(List.of(filename));
      var task = (JavacTask) COMPILER.getTask(null, FILE_MANAGER, null, null, null, fileObjects);
      var finder = new ThingFinder();

      ThingCollector collector = new ThingCollector(filename);

      Iterable<? extends CompilationUnitTree> parseResults = task.parse();
      for (CompilationUnitTree compilationUnitTree : parseResults) {
        finder.scan(compilationUnitTree, collector);
      }
      return collector.things;
    } catch (IOException ioe) {
      throw new Failure("Problem reading/parsing source", ioe);
    }
  }

  private static String getSHA1Hash(String input) {
    try {
      MessageDigest md = MessageDigest.getInstance("SHA-1");
      byte[] messageDigest = md.digest(input.getBytes());
      StringBuilder sb = new StringBuilder();
      for (byte b : messageDigest) {
        sb.append(String.format("%02x", b));
      }
      return sb.toString();
    } catch (NoSuchAlgorithmException e) {
      throw new Error(e);
    }
  }

  private String getTypeName(Tree returnType) {
    if (returnType instanceof IdentifierTree) {
      return ((IdentifierTree) returnType).getName().toString();
    } else if (returnType instanceof ParameterizedTypeTree) {
      return ((ParameterizedTypeTree) returnType).getType().toString();
    } else if (returnType instanceof PrimitiveTypeTree) {
      return ((PrimitiveTypeTree) returnType).getPrimitiveTypeKind().toString();
    } else if (returnType instanceof ArrayTypeTree) {
      ArrayTypeTree arrayTypeTree = (ArrayTypeTree) returnType;
      return getTypeName(arrayTypeTree.getType()) + "[]";
    }
    // Add more cases if needed for other types of trees
    return returnType.toString();
  }

  private static class ThingFinder extends TreePathScanner<Void, ThingCollector> {

    List<String> classStack = new ArrayList<>();

    @Override
    public Void visitMethod(MethodTree tree, ThingCollector things) {
      String name = tree.getName().toString();
      String fullName = String.join(".", classStack) + "." + name;
      List<? extends VariableTree> types = tree.getParameters();
      things.collect("method", name + "(" + types + ")", fullName, tree.toString());
      return super.visitMethod(tree, things);
    }

    @Override
    public Void visitClass(ClassTree tree, ThingCollector things) {
      try {
        String name = tree.getSimpleName().toString();
        classStack.add(name);
        String fullName = String.join(".", classStack);
        things.collect("class", name, fullName, tree.toString());
        return super.visitClass(tree, things);
      } finally {
        classStack.removeLast();
      }
    }
  }

  // private static class TypeFinder extends TreePathScanner<Void, List<String>> {
  //   @Override
  // }
}
