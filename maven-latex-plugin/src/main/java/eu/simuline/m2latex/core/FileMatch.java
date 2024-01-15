package eu.simuline.m2latex.core;

import com.florianingerl.util.regex.MatchResult;


/**
 * Describes a match in a file. 
 * The first question is whether the file is readable 
 * which is represented by {@link #isFileReadable()}. 
 * If it is readable, then it can be asked whether it matches a regular expression 
 * by {@link #doesExprMatch()}. 
 * Of course, if the file is not readable one cannot ask for a match. 
 * If it matches, then, one can query the match result via {@link #getMatchResult()}. 
 * Of course without a match, it cannot be asked for a match result. 
 * <p>
 * To create a file match accordingly, there are static methods: 
 * {@link #unreadable()} returns an instance representing an unreadable file, 
 * whereas all other creator methods represent readable files. 
 * {@link #noMatch()} represents a file readable but without a match. 
 * Finally, {@link #fileMatch(MatchResult)} represents a match 
 * given by the match result. 
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

    private final MatchResult matchRes;

    FileMatchWithMatcher(MatchResult matchRes) {
      this.matchRes = matchRes;
    }

    boolean isFileReadable() {
      return true;
    }

    boolean doesExprMatch() {
      return true;
    }

    MatchResult getMatchResult() {
      return this.matchRes;
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
   * Returns a file match with the given match result. 
   * In particular, the file is readable and matches. 
   * 
   * @param matchRes
   *    The match result of an existing file. 
   */
  static FileMatch fileMatch(MatchResult matchRes) {
    return new FileMatchWithMatcher(matchRes);
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
   * Returns a match result if there is one. 
   * 
   * @return
   *    the match result if the file is readable and matches the underlying regular expression. 
   * @throws IllegalStateException
   *    if 
   *    <ul>
   *    <li>the file is unreadable or 
   *    <li>the pattern does not match 
   *    </ul>
   * @throws IllegalArgumentException
   *    If no IllegalStateException but there is no group matched. 
   */
  MatchResult getMatchResult() {
    throw new IllegalStateException("No group matched. ");
  }

}
