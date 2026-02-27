package eu.simuline.m2latex.core;

/**
 * Enumerates the names of the parameters of a latex main file. 
 * All these must occur as names 
 * of named capturing groups in {@link Settings#patternLatexMainFile}. 
 * Currently, only one, {@link #docClass} must be matched by the pattern. 
 * Part of the names match parameters in a magic comment. 
 * These have the ending <code>Magic</code>. 
 * Currently, {@link #chkDiffMagic} and {@link #latexmkMagic} 
 * are special as they do not require values: 
 * The boolean values {@link #chkDiffMagicVal} and {@link #latexmkMagicVal} 
 * are optional and default to <code>true</code>. 
 */
public enum LatexMainParameterNames {

  /**
   * The name of the capturing group 
   * indicating whether after creating the artifact 
   * and copying it to the output directory {@link Settings#outputDirectoryFile} 
   * the artifact is checked by diffing against a preexisting artifact 
   * as described for {@link Settings#chkDiff}. 
   * Essentially, this overwrites the settings in the pom 
   * for individual latex main files. 
   * This magic comment can be given in conjunction with a Boolean value 
   * which is captured by group {@link #chkDiffMagicVal}, 
   * or without an explicit value in which case it defaults to <code>true</code>. 
   */
  chkDiffMagic,

  /**
   * The name of the capturing group capturing the boolean value 
   * for check described for {@link #chkDiffMagic}. 
   * Note that the value is optional and defaults to <code>true</code>. 
   */
  chkDiffMagicVal,

  /**
   * The name of the capturing group 
   * indicating whether <code>latexmk</code> or 
   * to be more precise the latexmk like-tool 
   * given by {@link Settings#getLatexmkCommand()} shall be used 
   * bypassing the direct invocation of more basic tools. 
   * Essentially, this overwrites {@link Settings#getLatexmkUsage()} 
   * but this has influence on when graphic files are created: 
   * In case of {@link LatexmkUsage#Fully}, these are not preprocessed. 
   */
  latexmkMagic,

  /**
   * The name of the capturing group capturing the boolean value 
   * for check described for {@link #latexmkMagic}. 
   * Note that the value is optional and defaults to <code>true</code>. 
   */
  latexmkMagicVal,

  /**
   * The name of the capturing group 
   * representing the document class specified by the commands 
   * <code>documentclass</code> or <code>documentstyle</code>. 
   */
  docClass,

  /**
   * The name of the capturing group 
   * representing the target set 
   * specified by the magic comment <code>% !LMP targets=...</code>. 
   */
  targetsMagic,

  /**
   * The name of the capturing group 
   * representing the target set 
   * specified by the magic comment <code>% !TEX program=...</code>. 
   */
  programMagic;
}
