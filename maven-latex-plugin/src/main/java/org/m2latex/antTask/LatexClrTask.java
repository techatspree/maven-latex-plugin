package org.m2latex.antTask;

import org.apache.tools.ant.BuildException;

import org.m2latex.core.BuildFailureException;
import org.m2latex.core.ParameterAdapter;
import org.m2latex.core.Target;

import java.util.SortedSet;

public class LatexClrTask extends AbstractLatexTask {

    // api-docs inherited from ParameterAdapter
    public SortedSet<Target> getTargetSet() {
	throw new IllegalStateException();
    }

    /**
     * Invoked by ant executing the task. 
     * <p>
     * Logging: 
     * <ul>
     * <li> WPP02: tex file may be latex main file 
     * <li> WFU01: Cannot read directory...
     * <li> WFU03: cannot close tex file 
     * <li> EFU05: Failed to delete file 
     * </ul>
     *
     * @throws BuildException 
     *    TSS01 if the tex source directory does either not exist 
     *    or is not a directory. 
     */
    public void execute() throws BuildException {
 	initialize();
	try {
	    // may throw BuildFailureException TSS01 
	    // may log warnings WPP02, WFU01, WFU03, EFU05 
	    this.latexProcessor.clearAll();
	} catch (BuildFailureException e) {
	    throw new BuildException(e.getMessage(), e.getCause());
	}
     }
 }
