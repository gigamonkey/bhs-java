package com.gigamonkeys.bhs.tools;

import javax.tools.*;
import java.io.*;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.net.URI;

/**
 * File manager that saves compiled bytecodes in memory and can provide the
 * source of some classes from memory.
 */
public class InMemoryJavaFileManager extends ForwardingJavaFileManager<StandardJavaFileManager> {

  private final InMemoryJavaCompiler compiler;

  protected InMemoryJavaFileManager(StandardJavaFileManager fileManager, InMemoryJavaCompiler compiler) {
    super(fileManager);
    this.compiler = compiler;
  }

  @Override
  public JavaFileObject getJavaFileForOutput(Location location, String classname, JavaFileObject.Kind kind, FileObject sibling) throws IOException {

    if (kind != JavaFileObject.Kind.CLASS) {
      throw new Error("Expected only CLASS got " + kind + " for " + classname);
    }

    return new SimpleJavaFileObject(URI.create("string:///" + classname.replace('.', '/') + kind.extension), kind) {
      @Override
      public OutputStream openOutputStream() {
        return new ByteArrayOutputStream() {
          @Override
          public void close() throws IOException {
            compiler.saveBytecodes(classname, toByteArray());
            super.close();
          }
        };
      }
    };
  }

  @Override
  public JavaFileObject getJavaFileForInput(Location location, String classname, JavaFileObject.Kind kind) throws IOException {
    if (kind == JavaFileObject.Kind.SOURCE) {
      String source = compiler.getSource(classname);
      if (source != null) {
        return new SimpleJavaFileObject(URI.create("string:///" + classname.replace('.', '/') + kind.extension), kind) {
          @Override
          public CharSequence getCharContent(boolean ignoreEncodingErrors) {
            return source;
          }
        };
      }
    } else if (kind == JavaFileObject.Kind.CLASS) {
      byte[] bytes = compiler.getBytecodes(classname);
      if (bytes != null) {
        return new SimpleJavaFileObject(URI.create("string:///" + classname.replace('.', '/') + kind.extension), kind) {
          @Override
          public InputStream openInputStream() {
            return new ByteArrayInputStream(bytes);
          }
        };
      }
    }
    return super.getJavaFileForInput(location, classname, kind);
  }
}
