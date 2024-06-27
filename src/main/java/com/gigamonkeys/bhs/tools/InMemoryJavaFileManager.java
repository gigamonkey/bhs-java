package com.gigamonkeys.bhs.tools;

import javax.tools.*;
import java.io.*;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.net.URI;

/**
 * File manager that lets us "write" files by saving their contents to a map we
 * are given.
 */
public class InMemoryJavaFileManager extends ForwardingJavaFileManager<StandardJavaFileManager> {

  private final Map<String, byte[]> classFiles;
  private final Map<String, String> sourceFiles;

  protected InMemoryJavaFileManager(StandardJavaFileManager fileManager, Map<String, byte[]> classFiles, Map<String, String> sourceFiles) {
    super(fileManager);
    this.classFiles = classFiles;
    this.sourceFiles = sourceFiles;
  }

  public void addSourceFile(String className, String source) {
    sourceFiles.put(className, source);
  }

  @Override
  public JavaFileObject getJavaFileForOutput(Location location, String className, JavaFileObject.Kind kind, FileObject sibling) throws IOException {

    if (kind != JavaFileObject.Kind.CLASS) {
      throw new Error("Expectod only CLASS got " + kind + " for " + className);
    }

    return new SimpleJavaFileObject(URI.create("string:///" + className.replace('.', '/') + kind.extension), kind) {
      @Override
      public OutputStream openOutputStream() {
        return new ByteArrayOutputStream() {
          @Override
          public void close() throws IOException {
            classFiles.put(className, toByteArray());
            super.close();
          }
        };
      }
    };
  }

  @Override
  public JavaFileObject getJavaFileForInput(Location location, String className, JavaFileObject.Kind kind) throws IOException {
    //System.err.println("Getting java file for input. location: " + location + "; className: " + className + "; kind: " + kind);
    if (kind == JavaFileObject.Kind.SOURCE) {
      if (sourceFiles.containsKey(className)) {
        //System.err.println("Found source");
        return new SimpleJavaFileObject(URI.create("string:///" + className.replace('.', '/') + kind.extension), kind) {
          @Override
          public CharSequence getCharContent(boolean ignoreEncodingErrors) {
            return sourceFiles.get(className);
          }
        };
      } else {
        //System.err.println("Didn't find source");
      }
    } else if (kind == JavaFileObject.Kind.CLASS) {
      if (classFiles.containsKey(className)) {
        //System.err.println("Found bytecodes");
        return new SimpleJavaFileObject(URI.create("string:///" + className.replace('.', '/') + kind.extension), kind) {
          @Override
          public InputStream openInputStream() {
            return new ByteArrayInputStream(classFiles.get(className));
          }
        };
      } else {
        //System.err.println("Didn't find bytecodes");
      }
    }
    return super.getJavaFileForInput(location, className, kind);
  }
}
