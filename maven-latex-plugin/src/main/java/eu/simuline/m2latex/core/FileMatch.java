package eu.simuline.m2latex.core;

/**
 * Describes a match in a file. 
 */
public class FileMatch {
  private final boolean isFileReadable;
  private final boolean matches;

  private FileMatch(boolean isFileReadable, boolean matches) {
    this.isFileReadable = isFileReadable;
    this.matches = matches;
  }
  
  static FileMatch unreadable() {
    return new FileMatch(false, false);
  }

  static FileMatch matches(boolean matches) {
    return new FileMatch(true, matches);
  }

  boolean isFileReadable() {
    return this.isFileReadable;
  }

  boolean matches() {
    return this.matches;
  }

}
