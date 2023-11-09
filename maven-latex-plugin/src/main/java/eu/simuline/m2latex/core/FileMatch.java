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


  static class FileMatchGrouped extends FileMatch {

    String grpString;

    FileMatchGrouped(String grpString) {
      this.grpString = grpString;
    }

    boolean isFileReadable() {
      return true;
    }

    boolean matches() {
      return true;
    }

    String group() {
      return this.grpString;
    }

  } // class FileMatchGrouped 

  private FileMatch() {}

  static FileMatch unreadable() {
    return new FileMatch();
  }

  static FileMatch matches(boolean matches) {
    return new FileMatchReadable(matches);
  }

    static FileMatch matches(String grpString) {
    return new FileMatchGrouped(grpString);
  }

  // to be overwritten 
  boolean isFileReadable() {
    return false;
  }

  boolean matches() {
    throw new IllegalStateException("Unreadable cannot be asked for match. ");
  }

  String group() {
    throw new IllegalStateException("No group matched. ");
  }
}
