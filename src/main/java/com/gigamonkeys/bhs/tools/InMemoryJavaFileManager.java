package com.gigamonkeys.bhs.tools;

import javax.tools.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
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

  private final Map<String, byte[]> compiledClasses;

  protected InMemoryJavaFileManager(StandardJavaFileManager fileManager, Map<String, byte[]> compiledClasses) {
    super(fileManager);
    this.compiledClasses = compiledClasses;
  }

  @Override
  public JavaFileObject getJavaFileForOutput(Location location, String className, JavaFileObject.Kind kind, FileObject sibling) throws IOException {
    return new SimpleJavaFileObject(URI.create("string:///" + className.replace('.', '/') + kind.extension), kind) {
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
