
package org.m2latex.core;

import java.io.File;

/**
 * Common interface to pass parameters from ant and from maven. 
 * The core method is {@link #initialize()}. 
 * TODO: The other two shall be removed later on. 
 *
 * @see org.m2latex.antTask.LatexTask
 * @see org.m2latex.mojo.AbstractLatexMojo
 */
public interface ParameterAdapter {

    /**
     * Sets up the parameters and initializes 
     * {@link org.m2latex.core.LatexProcessor}. 
     */
    void initialize();

    /**
     * Processes the source file <code>texFile</code> 
     * according to the concrete Mojo. 
     */
     void processSource(File texFile) throws BuildExecutionException;

    /**
     * Returns the suffixes and wildcards of the output files. 
     * For example if creating pdf and postscript, 
     * this is just <code>.pdf, .ps</code> 
     * but if various html files are created, it is <code>*.html</code>, 
     * the asterisk representing a wildcard. 
     */
    String[] getOutputFileSuffixes();
}
