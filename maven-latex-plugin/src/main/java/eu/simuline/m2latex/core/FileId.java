package eu.simuline.m2latex.core;

import java.io.File;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Provides an immutable identifier for a file 
 * consisting of the number of relevant lines lines 
 * and a hash computed from these lines. 
 * The the hash is computed with fixed algorithm {@link #ALGORITHM}. 
 * <p>
 * The relevant parts of a file is given by an {@link Auxiliary}, 
 * So, the constructor has signature {@link #FileId()}. 
 * <p>
 * If two files coincide in their relevant lines, 
 * their {@link FileId}s coincide, 
 * i.e. they are equal according to {@link #equals(Object)}. 
 * If they don't coincide, then it is conceivable but still unlikely, 
 * that their {@link FileId}s coincide. 
 * We call this a collision. 
 * So the {@link FileId} may be used to detect file changes quite safely. 
 * <p>
 * This class is applied in rerun file check, 
 * i.e. to trigger the run of a tool 
 * if a file changed. 
 * In case of a collision, 
 * the execution of the tool is not triggered although needed. 
 */
public class FileId {

  /**
   * The algorithm used to compute the hash {@link #hash} 
   * from lines of a file determined by an {@link Auxiliary}. 
   * 
   * @see #FileId()
   */
  private static final String ALGORITHM = "MD5";

  /**
   * The number of lines written in a file relevant for the {@link Auxiliary} 
   * initialized by the constructor {@link #FileId()} 
   * and incremented by {@link #update(String)}. 
   */
  private int numLines;

  /**
   * The intermediate digest of a file relevant for the {@link Auxiliary} 
   * initialized by the constructor {@link #FileId()} 
   * and incremented by {@link #update(String)}. 
   * At the end, {@link #finalizFileId()} is invoked 
   * which hashes the result and writes it into {@link #hash}. 
   */
  private MessageDigest md;

  /**
   * The hash of lines the number of which is given by {@link #numLines} 
   * computed from {@link #md}. 
   * This is initially <code>null</code> 
   * and properly initialized by {@link #finalizFileId()}. 
   */
  private String hash;


  FileId() {
    try {
      this.md = MessageDigest.getInstance(ALGORITHM);
    } catch (NoSuchAlgorithmException nsae) {
      throw new IllegalStateException(
          "Algorithm " + ALGORITHM + " should be known. ");
    }
    this.numLines = 0;
    this.hash = null;
  }

  void update(String line) {
    this.numLines++;
    this.md.update(line.getBytes());
  }

  FileId finalizFileId() {
    this.hash = new String(md.digest());
    return this;
  }

  /**
   * The given object equals this one if it is an instance of {@link FileId} 
   * and both fields are equal. 
   */
  public boolean equals(Object obj) {
    if (!(obj instanceof FileId)) {
      return false;
    }
    FileId other = (FileId) obj;
    assert other.hash != null;
    return this.numLines == other.numLines 
    //&& new String(this.md.digest()).equals(new String(other.md.digest()));
    //&& Arrays.equals(this.md.digest(), other.md.digest());
    && this.hash.equals(other.hash);
  }

  public String toString() {
    return new String(this.hash) + " " + this.numLines;
    //return this.hash + " " + this.numLines;
  }
}

