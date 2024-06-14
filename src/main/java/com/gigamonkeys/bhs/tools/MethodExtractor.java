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

  private static final JavaCompiler COMPILER = ToolProvider.getSystemJavaCompiler();
  private static StandardJavaFileManager FILE_MANAGER = COMPILER.getStandardFileManager(null, null, null);
  private final String filename;

  public MethodExtractor(String filename) {
    this.filename = filename;
  }

  public String filename() {
    return filename;
  }

  private static record Method(String name, String code) {}

  public static void main(String[] args) {

    for (var filename: args) {
      MethodExtractor extractor = new MethodExtractor(filename);

      try {
        for (Method m: extractor.allMethods()) {
          System.out.println(extractor.filename() + "\t" + m.name() + "\t" + getSHA1Hash(m.code()));
        }
      } catch (IOException | NoSuchAlgorithmException e) {
        e.printStackTrace();
      }
    }
  }

  private List<Method> allMethods() throws IOException {
    var fileObjects = FILE_MANAGER.getJavaFileObjectsFromStrings(List.of(filename));
    JavacTask task = (JavacTask) COMPILER.getTask(null, FILE_MANAGER, null, null, null, fileObjects);
    MethodFinder finder = new MethodFinder();

    List<Method> methods = new ArrayList<>();

    Iterable<? extends CompilationUnitTree> parseResults = task.parse();
    for (CompilationUnitTree compilationUnitTree : parseResults) {
      finder.scan(compilationUnitTree, methods);
    }
    return methods;
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


  private static class MethodFinder extends TreePathScanner<Void, List<Method>> {

    @Override
    public Void visitMethod(MethodTree methodTree, List<Method> list) {
      list.add(new Method(methodTree.getName().toString(), methodTree.toString()));
      return super.visitMethod(methodTree, list);
    }

  }
}
