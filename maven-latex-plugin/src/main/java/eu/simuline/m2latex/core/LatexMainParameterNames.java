package eu.simuline.m2latex.core;

/**
 * Enumerates the names of the parameters of a latex main file. 
 * All these must occur as names 
 * of named capturing groups in {@link Settings#patternLatexMainFile}. 
 * Currently, only one, {@link #docClass} must be matched by the pattern. 
 * Part of the names match parameters in a magic comment. 
 * These have the ending <code>Magic</code>. 
 * Currently, there are two examples for this, 
 * {@link #programMagic} and {@link #targetsMagic}. 
 */
public enum LatexMainParameterNames {
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
