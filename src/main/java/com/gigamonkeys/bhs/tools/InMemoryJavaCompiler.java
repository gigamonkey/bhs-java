package com.gigamonkeys.bhs.tools;

import javax.tools.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

// Original code from ChatGPT 4o https://chatgpt.com/share/c4fb081d-c198-412a-9071-0e261c179e94

public class InMemoryJavaCompiler {

  private static JavaFileManager.Location dummyLocation = new JavaFileManager.Location() {
      @Override public String getName() { return "dummy"; }

      @Override public boolean isOutputLocation() { return true; }

      @Override public boolean isModuleOrientedLocation() { return true; }
    };


  private Map<String, byte[]> classes = new HashMap<>();
  private Map<String, String> sources = new HashMap<>();

  /**
   * Compile a single .java file and save the compiled bytecodes in classes.
   */
  public boolean compile(Path file) throws IOException {
    JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
    DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();

    try (StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, null, null)) {

      Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjectsFromPaths(List.of(file));
      JavaFileManager files = new InMemoryJavaFileManager(fileManager, classes, sources);
      JavaCompiler.CompilationTask task = compiler.getTask(null, files, diagnostics, null, null, compilationUnits);

      if (task.call()) {
        return true;
      } else {
        // FIXME: should collect these some better way
        for (Diagnostic<? extends JavaFileObject> diagnostic : diagnostics.getDiagnostics()) {
          System.out.format("Error on line %d in %s%n", diagnostic.getLineNumber(), diagnostic.getSource().toUri());
        }
        return false;
      }
    }
  }

  public boolean compileInMemorySource(String className) throws IOException {
    JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
    DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();

    try (StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, null, null)) {

      JavaFileManager files = new InMemoryJavaFileManager(fileManager, classes, sources);
      Iterable<? extends JavaFileObject> compilationUnits = List.of(files.getJavaFileForInput(dummyLocation, className, JavaFileObject.Kind.SOURCE));
      System.err.println("compilationUnits: " + compilationUnits);
      JavaCompiler.CompilationTask task = compiler.getTask(null, files, diagnostics, null, null, compilationUnits);

      if (task.call()) {
        return true;
      } else {
        // FIXME: should collect these some better way
        for (Diagnostic<? extends JavaFileObject> diagnostic : diagnostics.getDiagnostics()) {
          System.out.format("Error on line %d in %s%n", diagnostic.getLineNumber(), diagnostic.getSource().toUri());
        }
        return false;
      }
    }
  }

  public Map<String, byte[]> classes() { return classes; }


  public static void main(String[] args) throws IOException {
    InMemoryJavaCompiler compiler = new InMemoryJavaCompiler();

    compiler.sources.put("Foo", "public class Foo {}");

    //Path path = Path.of(args[0]);
    if (compiler.compileInMemorySource("Foo")) {
      Map<String, byte[]> compiled = compiler.classes();

      System.out.println("Compilation successful, number of classes: " + compiled.size());
      for (Map.Entry<String, byte[]> entry : compiled.entrySet()) {
        System.out.println("Class: " + entry.getKey() + ", byte array length: " + entry.getValue().length);
      }
    } else {
      System.out.println("Compilation failed.");
    }
  }
}
