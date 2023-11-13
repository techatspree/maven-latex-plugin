package eu.simuline.m2latex.core;

/**
 * Enumerates the names of the parameters of a latex main file. 
 * All these must occur as names 
 * of named capturing groups in {@link Settings#patternLatexMainFile}. 
 * Not all of them must be matched by the pattern. 
 * Part of the names match parameters in a magic comment. 
 * These have the ending <code>Magic</code>. 
 * Currently, there are two examples for this, 
 * {@link #docClassMagic} and {@link #targetsMagic}. 
 * Typically, the document class is known 
 * and the name of this parameter is {@link #docClass}. 
 */
public enum LatexMainParameterNames {
  docClass, docClassMagic, targetsMagic;
}
