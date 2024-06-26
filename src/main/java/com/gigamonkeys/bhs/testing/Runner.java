package com.gigamonkeys.bhs.testing;

import com.gigamonkeys.bhs.BespokeTestRunner;
import com.gigamonkeys.bhs.testing.Tester;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.UndeclaredThrowableException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

/**
 * A generic runner that can compile and test a bunch of source files. Probably
 * this should be merged with BespokeTestRunner and used both in the Jobe server
 * and in my bulk grading. Or possibly as the heart of a grading server. It
 * compiles now (2024-06-16) but may or may not work in any useful way. I just
 * tried to generalize from the test specific one I used last year to grade the
 * word-search assignment.
 */
public class Runner {

  // FIXME: this needs to be an argument to the runner.
  public static final String TEST_CLASS_NAME = "Solver";

  static class PathClassLoader extends ClassLoader {

    private Path path;

    public PathClassLoader(Path path) {
      this.path = path;
    }

    @Override
    public Class<?> findClass(String name) throws ClassNotFoundException {
      try {
        byte[] bytes = Files.readAllBytes(path.resolve(name + ".class"));
        return defineClass(name, bytes, 0, bytes.length);
      } catch (IOException ioe) {
        throw new ClassNotFoundException("Can't find file", ioe);
      }
    }
  }

  public static boolean compile(Path file) throws IOException {
    JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
    DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();

    try (StandardJavaFileManager fileManager =
        compiler.getStandardFileManager(diagnostics, null, null)) {

      Iterable<? extends JavaFileObject> compilationUnits =
          fileManager.getJavaFileObjectsFromPaths(List.of(file));
      JavaCompiler.CompilationTask task =
          compiler.getTask(null, fileManager, diagnostics, null, null, compilationUnits);

      if (task.call()) {
        return true;
      } else {
        return false;
        // System.out.println("Compilation failed.");
        // for (Diagnostic<? extends JavaFileObject> diagnostic : diagnostics.getDiagnostics()) {
        //   System.out.format("Error on line %d in %s%n", diagnostic.getLineNumber(),
        // diagnostic.getSource().toUri());
        // }
      }
    }
  }

  static class Generator {
    private final Path dir;
    private Class<Tester> testerClass;
    private Tester tester = null;

    Generator(Path dir, Class<Tester> testerClass) {
      this.dir = dir;
      this.testerClass = testerClass;
    }

    Path dir() {
      return dir;
    }

    // Do this lazily so we can recompile the Java file first if needed.
    Tester tester() {
      if (tester == null) {
        try {
          var classToTest = new PathClassLoader(dir).loadClass(TEST_CLASS_NAME);
          var testObject = classToTest.getDeclaredConstructor().newInstance();
          tester = testerClass.getConstructor(new Class[] {Object.class}).newInstance(testObject);
        } catch (ReflectiveOperationException roe) {
          throw new RuntimeException(roe);
        }
      }
      return tester;
    }

    Path javaFile() {
      return dir.resolve(TEST_CLASS_NAME + ".java");
    }

    Path classFile() {
      return dir.resolve(TEST_CLASS_NAME + ".class");
    }

    Path resultsFile() {
      return dir.resolve("results.json");
    }

    boolean maybeCompile() throws IOException {
      boolean ok = true;
      if (needsRebuild(javaFile(), classFile())) {
        System.out.print(javaFile() + ": compiling ... ");
        ok = compile(javaFile());
        System.out.println(ok ? "ok." : " failed.");
      } else {
        // System.out.println(classFile() + " up to date.");
      }
      return ok;
    }

    void maybeTest() throws IOException {
      if (needsRebuild(classFile(), resultsFile())) {
        System.out.print(resultsFile() + ": generating ...");
        try {
          Files.writeString(resultsFile(), new BespokeTestRunner(tester()).resultsAsJson());
          System.out.println(" ok.");
        } catch (InvocationTargetException ite) {
          System.out.println(" failed: " + ite.getCause());
          if (ite.getCause() instanceof UndeclaredThrowableException) {
            System.out.println("Failure due to: " + ite.getCause().getCause());
          }
        } catch (Exception e) {
          System.out.println(" failed: " + e);
        }
      } else {
        System.out.println(resultsFile() + " up to date.");
      }
    }

    boolean needsRebuild(Path source, Path output) throws IOException {
      if (!Files.exists(output)) {
        return true;
      } else {
        var sourceMod = Files.getLastModifiedTime(source);
        var outputMod = Files.getLastModifiedTime(output);
        return sourceMod.compareTo(outputMod) > 0;
      }
    }
  }

  public static Stream<Path> dirs(String root) throws IOException {
    var matcher = FileSystems.getDefault().getPathMatcher("glob:**/" + TEST_CLASS_NAME + ".java");
    return Files.walk(Path.of("."))
        .filter(Files::isRegularFile)
        .filter(matcher::matches)
        .map(Path::getParent);
  }

  public static void main(String[] args) throws Exception {

    var testerClass = (Class<Tester>) Class.forName(args[0]);
    var tester = testerClass.getConstructor(new Class[0]).newInstance();

    var gens = dirs(".").map(path -> new Generator(path, testerClass)).collect(Collectors.toList());

    for (var g : gens) {
      try {
        if (g.maybeCompile()) {
          g.maybeTest();
        }
      } catch (Exception e) {
        System.err.println(g.dir() + ": problem testing.");
        e.printStackTrace();
      }
    }
  }
}
