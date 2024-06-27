package com.gigamonkeys.bhs.tools;

import javax.tools.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

// Original code from ChatGPT 4o https://chatgpt.com/share/c4fb081d-c198-412a-9071-0e261c179e94

public class InMemoryJavaCompiler {

  private static JavaFileManager.Location LOCATION = new JavaFileManager.Location() {
      @Override public String getName() { return "IN_MEMORY"; }

      @Override public boolean isOutputLocation() { return true; }

      @Override public boolean isModuleOrientedLocation() { return true; }
    };


  private Map<String, byte[]> classes = new HashMap<>();
  private Map<String, String> sources = new HashMap<>();

  public void saveBytecodes(String classname, byte[] bytecodes) {
    classes.put(classname, bytecodes);
  }

  public byte[] getBytecodes(String classname) {
    return classes.get(classname);
  }

  public void addSource(String classname, String source) {
    sources.put(classname, source);
  }

  public String getSource(String classname) {
    return sources.get(classname);
  }


  public ClassLoader getClassLoader() {
    return new ClassLoader() {
      @Override
      public Class<?> findClass(String name) throws ClassNotFoundException {
        byte[] bytes = getBytecodes(name);
        if (bytes != null) {
          return defineClass(name, bytes, 0, bytes.length);
        } else {
          return super.findClass(name);
        }
      }
    };
  }

  /**
   * Compile a single .java file and save the compiled bytecodes in classes.
   */
  public boolean compile(Path file) throws IOException {
    JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
    DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();

    try (StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, null, null)) {

      Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjectsFromPaths(List.of(file));
      JavaFileManager files = new InMemoryJavaFileManager(fileManager, this);
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


  public boolean compileCode(String className) throws IOException {
    JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
    DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();

    try (StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, null, null)) {

      JavaFileManager files = new InMemoryJavaFileManager(fileManager, this);
      Iterable<? extends JavaFileObject> compilationUnits = List.of(files.getJavaFileForInput(LOCATION, className, JavaFileObject.Kind.SOURCE));
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


  public static void main(String[] args) throws Exception {
    InMemoryJavaCompiler compiler = new InMemoryJavaCompiler();

    compiler.addSource("Foo", "public class Foo { public void hello() { System.out.println(\"hello\"); } }");

    //Path path = Path.of(args[0]);
    if (compiler.compileCode("Foo")) {
      Map<String, byte[]> compiled = compiler.classes();

      System.out.println("Compilation successful, number of classes: " + compiled.size());
      for (Map.Entry<String, byte[]> entry : compiled.entrySet()) {
        System.out.println("Class: " + entry.getKey() + ", byte array length: " + entry.getValue().length);
      }

      Class<?> fooClass = compiler.getClassLoader().loadClass("Foo");
      System.out.println(fooClass);
      Object foo = fooClass.getDeclaredConstructor(new Class[0]).newInstance();
      fooClass.getDeclaredMethod("hello").invoke(foo);


    } else {
      System.out.println("Compilation failed.");
    }
  }
}
