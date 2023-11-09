package eu.simuline.m2latex.core;

/**
 * Describes a match in a file. 
 * The first question is whether the file is readable 
 * which is represented by {@link #isFileReadable()}. 
 * If it is readable, then it can be asked whether it matches a regular expression 
 * by {@link #doesExprMatch()}. 
 * Of course, if the file is not readable one cannot ask for a match. 
 * If it matches, then, if the regular expression has named groups, 
 * one can ask for the group matches. 
 * Currently, only match with a single group is supported via {@link #groupMatch()}. 
 * Of course without a match, it cannot be asked for a group match. 
 * <p>
 * To create a file match accordingly, there are static methods: 
 * {@link #unreadable()} returns an instance representing an unreadable file, 
 * whereas all other creator methods represent readable files. 
 * {@link #matches(boolean)} represents a file match according to the parameter 
 * but without matching a group. 
 * Finally, {@link #matches(String)} represents a match including a group matching the parameter value. 
 * <p>
 * This class is both the base class for all kins of file matches 
 * and also represents the case with an unreadable file as returned by {@link #unreadable()}. 
 */
public class FileMatch {

  /**
   * Represents 
   */
  static class FileMatchReadable extends FileMatch {
    private final boolean matches;

    private FileMatchReadable(boolean matches) {

      this.matches = matches;
    }

    boolean isFileReadable() {
      return true;
    }

    boolean doesExprMatch() {
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

    boolean doesExprMatch() {
      return true;
    }

    String groupMatch() {
      return this.grpString;
    }

  } // class FileMatchGrouped 

  private final static FileMatch UNREADABLE = new FileMatch();
  private final static FileMatch DO_MATCH = new FileMatchReadable(true);
  private final static FileMatch NO_MATCH = new FileMatchReadable(false);

  private FileMatch() {}

  /**
   * Returns a file match with an unreadable file. 
   * This is the singleton {@link #UNREADABLE}. 
   */
  static FileMatch unreadable() {
    return UNREADABLE;
  }

  /**
   * Represents a match with a pattern in a readable file but without matching groups. 
   * 
   * @param matches
   *    whether this matches. 
   * @return
   *    Either {@link #DO_MATCH} or {@link #NO_MATCH}, depending on <code>matches</code>. 
   */
  static FileMatch matches(boolean matches) {
    return matches ? DO_MATCH : NO_MATCH;
  }

  /**
   * Represents a match with a pattern with at least one named group 
   * in a readable file. 
   * @param grpString
   *    the string matched in the named group. 
   * @return
   *    a match as described above as an instance of {@link FileMatch.FileMatchGrouped}. 
   */
  static FileMatch matches(String grpString) {
    return new FileMatchGrouped(grpString);
  }

  // to be overwritten 
  /**
   * Returns whether the underlying file is readable. 
   * 
   * @return
   *    whether the underlying file is readable. 
   */
  boolean isFileReadable() {
    return false;
  }

  // to be overwritten 
  /**
   * Returns whether the pattern matches, provided the file is readable. 
   * 
   * @return
   *    whether the pattern matches. 
   * @throws IllegalStateException
   *    if the file is unreadable. 
   */
  boolean doesExprMatch() {
    throw new IllegalStateException("Unreadable cannot be asked for match. ");
  }

 // to be overwritten 
  /**
   * Returns the string matching with the group if any. 
   * 
   * @return
   *    whether the pattern matches. 
   * @throws IllegalStateException
   *    if 
   *    <ul>
   *    <li>the file is unreadable or 
   *    <li>the patter does ot match or 
   *    <li>if the pattern has no named group. 
   *    </ul>
   */
 String groupMatch() {
    throw new IllegalStateException("No group matched. ");
  }
}
