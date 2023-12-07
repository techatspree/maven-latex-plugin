package eu.simuline.m2latex.core;

import java.io.File;

import java.nio.file.Files;
import java.nio.file.LinkOption;

import java.util.Set;
import java.util.TreeSet;
import java.util.Map;
import java.util.TreeMap;

/**
 * Represents the contents of a directory. 
 *
 *
 * Created: Tue Dec  6 03:05:24 2016
 *
 * @author <a href="mailto:rei3ner@arcor.de">Ernst Reissner</a>
 * @version 1.0
 */
public class DirNode {

  // null iff this DirNode is invalid according to isValid() 
  /**
   * The set of names of regular files, i.e. files except directories 
   * in the directory described by this node. 
   * If the directory described by this node is not readable, 
   * this field is <code>null</code>. 
   *
   * @see #isValid()
   */
  private final Set<String> regularFileNames;

  /**
   * The set of subdirectories 
   * in the directory described by this node: 
   * The keys are the names and the according values 
   * are the nodes describing the subdirectories. 
   * If the directory described by this node is not readable, 
   * this field is <code>null</code>. 
   *
   * @see #isValid()
   */
  private final Map<String, DirNode> name2node;

  /**
   * Creates a new <code>DirNode</code> instance.
   * <p>
   * Logging: 
   * WFU01: Cannot read directory 
   *
   * @param dir
   *    The directory this node represents 
   *    including subdirectories recursively. 
   *    This is the latex source directory or a subdirectory recursively. 
   * @param fileUtils
   *    
   */
  // used recursively but in addition only in 
  // TexFileUtils.cleanUpRec,
  // LatexProcessor.create()
  // LatexProcessor.processGraphics()
  // LatexProcessor.clearAll()
  public DirNode(File dir, TexFileUtils fileUtils) {
    assert dir.isDirectory() : "The file '" + dir + "' is no directory. ";
    // may log WFU01 Cannot read directory
    File[] files = fileUtils.listFilesOrWarn(dir);
    if (files == null) {
      // Here, this node is irregular
      // TBD: clarify whether this may occur 
      assert false;// should only be the case if dir is no directory 
      this.regularFileNames = null;
      this.name2node = null;
      return;
    }
    this.regularFileNames = new TreeSet<String>();
    this.name2node = new TreeMap<String, DirNode>();
    DirNode node;
    for (File file : files) {
      // with link option because file.exists() is false 
      // if link with non-existing target
      assert Files.exists(file.toPath(), LinkOption.NOFOLLOW_LINKS)
        : "The file '" + file + "' does not exist. ";
      if (file.isDirectory()) {
        // may log WFU01 Cannot read directory
        node = new DirNode(file, fileUtils);
        if (node.isValid()) {
          this.name2node.put(file.getName(), node);
        }
      } else {
        // FIXME: skip hidden files
        this.regularFileNames.add(file.getName());
      }
    }
  }

  /**
   * Whether the directory described by this node is readable. 
   */
  boolean isValid() {
    assert (this.regularFileNames == null) == (this.name2node == null);
    return this.regularFileNames != null;
  }

  Set<String> getRegularFileNames() {
    return this.regularFileNames;
  }

  Map<String, DirNode> getSubdirs() {
    return this.name2node;
  }
}
