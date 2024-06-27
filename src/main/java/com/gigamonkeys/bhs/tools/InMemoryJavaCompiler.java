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

  public static Map<String, byte[]> compile(Path file) throws IOException {
    JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
    DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
    Map<String, byte[]> compiledClasses = new HashMap<>();

    try (StandardJavaFileManager fileManager =
         compiler.getStandardFileManager(diagnostics, null, null)) {

      Iterable<? extends JavaFileObject> compilationUnits =
        fileManager.getJavaFileObjectsFromPaths(List.of(file));

      JavaFileManager inMemoryFileManager = new InMemoryJavaFileManager(fileManager, compiledClasses);

      JavaCompiler.CompilationTask task =
        compiler.getTask(null, inMemoryFileManager, diagnostics, null, null, compilationUnits);

      if (task.call()) {
        return compiledClasses;
      } else {
        for (Diagnostic<? extends JavaFileObject> diagnostic : diagnostics.getDiagnostics()) {
          System.out.format("Error on line %d in %s%n", diagnostic.getLineNumber(), diagnostic.getSource().toUri());
        }
        return null;
      }
    }
  }

  private static class InMemoryJavaFileManager extends ForwardingJavaFileManager<StandardJavaFileManager> {
    private final Map<String, byte[]> compiledClasses;

    protected InMemoryJavaFileManager(StandardJavaFileManager fileManager, Map<String, byte[]> compiledClasses) {
      super(fileManager);
      this.compiledClasses = compiledClasses;
    }

    @Override
    public JavaFileObject getJavaFileForOutput(Location location, String className, JavaFileObject.Kind kind, FileObject sibling) throws IOException {
      return new SimpleJavaFileObject(java.net.URI.create("string:///" + className.replace('.', '/') + kind.extension), kind) {
        @Override
        public ByteArrayOutputStream openOutputStream() {
          return new ByteArrayOutputStream() {
            @Override
            public void close() throws IOException {
              compiledClasses.put(className, toByteArray());
              super.close();
            }
          };
        }
      };
    }
  }

  public static void main(String[] args) throws IOException {
    Path path = Path.of("path/to/your/JavaFile.java");
    Map<String, byte[]> compiledBytes = compile(path);

    if (compiledBytes != null) {
      System.out.println("Compilation successful, number of classes: " + compiledBytes.size());
      for (Map.Entry<String, byte[]> entry : compiledBytes.entrySet()) {
        System.out.println("Class: " + entry.getKey() + ", byte array length: " + entry.getValue().length);
      }
    } else {
      System.out.println("Compilation failed.");
    }
  }
}
