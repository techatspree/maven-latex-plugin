package org.m2latex.antTask;

import org.apache.tools.ant.BuildException;

import org.m2latex.core.BuildFailureException;
import org.m2latex.core.ParameterAdapter;
import org.m2latex.core.Target;

import java.util.SortedSet;

public class LatexCfgTask extends AbstractLatexTask {

    // api-docs inherited from ParameterAdapter
    public SortedSet<Target> getTargetSet() {
	return this.settings.getTargetSet();
    }

    /**
     * Invoked by ant executing the task. 
     * <p>
     * Logging: 
     * <ul>
     * <li> WFU01: Cannot read directory... 
     * <li> WFU03: cannot close 
     * <li> WPP02: tex file may be latex main file 
     * <li> WPP03: Skipped processing of files with suffixes ... 
     * <li> EEX01, EEX02, WEX03, WEX04, WEX05: 
     *      applications for preprocessing graphic files 
     *      or processing a latex main file fails. 
     * </ul>
     * @throws BuildException
     *    FIXME 
     */
    public void execute() throws BuildException {
 	initialize();
	try {
	    // may throw BuildFailureException FIXME 
	    // may log warning WFU01, WFU03, WPP02, WPP03, 
	    // EEX01, EEX02, WEX03, WEX04, WEX05 
	    this.latexProcessor.create();
	} catch (BuildFailureException e) {
	    throw new BuildException(e.getMessage(), e.getCause());
	}
     }
 }
