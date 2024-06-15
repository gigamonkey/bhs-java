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

// Starter code supplied by ChatGPT 4o:
// https://chatgpt.com/share/17e61a89-9bc0-43d1-8a99-08625048ecb3

/**
 * Tool for extracting bits of source code from .java files. Originally written to find the
 * duplicate code I had created by copying and pasting test classes for assignments during the
 * school year. May also be useful for analyzing student code.
 */
public class ThingExtractor {

  private static final JavaCompiler COMPILER = ToolProvider.getSystemJavaCompiler();
  private static final StandardJavaFileManager FILE_MANAGER =
      COMPILER.getStandardFileManager(null, null, null);

  private static record Thing(String what, String name, String code) {

    public String asTSV(String filename) {
      return what + "\t" + filename + "\t" + name + "\t" + getSHA1Hash(code);
    }

    public String asText(String filename) {
      return filename + ": " + what + " (" + getSHA1Hash(code) + ")\n\n" + code;
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
      BiFunction<Thing, String, String> emitter = Thing::asTSV;

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

  private void emit(List<String> filenames, BiFunction<Thing, String, String> f) {
    for (var filename : filenames) {
      for (var t : allThings(filename)) {
        System.out.println(f.apply(t, filename));
      }
    }
  }

  private List<Thing> allThings(String filename) {
    try {
      var fileObjects = FILE_MANAGER.getJavaFileObjectsFromStrings(List.of(filename));
      var task = (JavacTask) COMPILER.getTask(null, FILE_MANAGER, null, null, null, fileObjects);
      var finder = new ThingFinder();

      List<Thing> things = new ArrayList<>();

      Iterable<? extends CompilationUnitTree> parseResults = task.parse();
      for (CompilationUnitTree compilationUnitTree : parseResults) {
        finder.scan(compilationUnitTree, things);
      }
      return things;
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

  private static class ThingFinder extends TreePathScanner<Void, List<Thing>> {

    @Override
    public Void visitMethod(MethodTree tree, List<Thing> list) {
      list.add(new Thing("method", tree.getName().toString(), tree.toString()));
      return super.visitMethod(tree, list);
    }

    @Override
    public Void visitClass(ClassTree tree, List<Thing> list) {
      list.add(new Thing("class", tree.getSimpleName().toString(), tree.toString()));
      return super.visitClass(tree, list);
    }
  }
}
