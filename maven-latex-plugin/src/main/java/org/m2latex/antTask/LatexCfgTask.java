package org.m2latex.antTask;

import org.apache.tools.ant.Task;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.BuildException;

import org.m2latex.core.Settings;
import org.m2latex.core.MyBuildException;
import org.m2latex.core.LatexProcessor;
import org.m2latex.core.ParameterAdapter;
import org.m2latex.core.Target;

import java.io.File;

import java.util.SortedSet;

public class LatexCfgTask extends AbstractLatexTask {

    // api-docs inherited from ParameterAdapter
    public SortedSet<Target> getTargetSet() {
	return this.settings.getTargetSet();
    }

    /**
     * Invoked by ant executing the task. 
     */
    public void execute() throws BuildException {
 	initialize();
	try {
	    this.latexProcessor.create();
	} catch (MyBuildException e) {
	    throw new BuildException(e.getMessage(), e.getCause());
	}
     }
 }
