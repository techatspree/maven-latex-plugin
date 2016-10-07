

package org.m2latex.mojo;

import java.io.File;

public interface ParameterAdapter {

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
