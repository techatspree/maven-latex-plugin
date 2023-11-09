package eu.simuline.m2latex.core;

/**
 * Describes a match in a file. 
 */
public class FileMatch {

  static class FileMatchReadable extends FileMatch {
    private final boolean matches;

    private FileMatchReadable(boolean matches) {

      this.matches = matches;
    }

    boolean isFileReadable() {
      return true;
    }

    boolean matches() {
      return this.matches;
    }
  } // FileMatchReadable 



  private FileMatch() {}

  static FileMatch unreadable() {
    return new FileMatch();
  }

  static FileMatch matches(boolean matches) {
    return new FileMatchReadable(matches);
  }

  // to be overwritten 
  boolean isFileReadable() {
    return false;
  }

  boolean matches() {
    throw new IllegalStateException("Unreadable cannot be asked for match. ");
  }

}
