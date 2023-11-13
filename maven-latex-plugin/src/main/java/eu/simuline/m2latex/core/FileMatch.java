package eu.simuline.m2latex.core;

import java.util.regex.Matcher;

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

  static class FileMatchReadableNoMatch extends FileMatch {


    private FileMatchReadableNoMatch() {}

    boolean isFileReadable() {
      return true;
    }

    boolean doesExprMatch() {
      return false;
    }
  } // FileMatchReadable 

  static class FileMatchWithMatcher extends FileMatch {

    private final Matcher matcher;

    FileMatchWithMatcher(Matcher matcher) {
      assert matcher.matches();
      this.matcher = matcher;
    }

    boolean isFileReadable() {
      return true;
    }

    boolean doesExprMatch() {
      return true;
    }

    String groupMatch(LatexMainParameterNames groupName) {
      return this.matcher.group(groupName.toString());
    }

    Matcher getMatcher() {
      return this.matcher;
    }

  } // class FileMatchWithMatcher 

  private final static FileMatch UNREADABLE = new FileMatch();

  private final static FileMatch NO_MATCH = new FileMatchReadableNoMatch();

  private FileMatch() {}

  /**
   * Returns a file match with an unreadable file. 
   * This is the singleton {@link #UNREADABLE}. 
   */
  static FileMatch unreadable() {
    return UNREADABLE;
  }

  /**
   * Returns a file match with readable file but without match. 
   */
  static FileMatch noMatch() {
    return NO_MATCH;
  }

  /**
   * Returns a file match with the given matcher. 
   * In particular, the file is readable and matches. 
   * 
   * @param matcher
   *    The matcher of this file. 
   */
  static FileMatch matches(Matcher matcher) {
    return new FileMatchWithMatcher(matcher);
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
   * @param groupName
   *    the name of a group in a regular expression. 
   * @return
   *    the text matched in the group or <code>null</code>, 
   *    the latter if the pattern has the group but nothing matches, 
   *    e.g. because of an alternative like <code>(?&lt;name&gt;x)?</code>. 
   * @throws IllegalStateException
   *    if 
   *    <ul>
   *    <li>the file is unreadable or 
   *    <li>the patter does ot match or 
   *    <li>if the pattern has no named group. 
   *    </ul>
   * @throws IllegalArgumentException
   *    If no IllegalStateException but there is no group with the given name 
   */
  String groupMatch(LatexMainParameterNames groupName) {
    throw new IllegalStateException("No group matched. ");
  }

  Matcher getMatcher() {
    throw new IllegalStateException("No group matched. ");
  }
}
