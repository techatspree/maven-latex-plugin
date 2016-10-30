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

public class LatexCfgTask extends Task implements ParameterAdapter {

    /**
     * Contains all parameters for executing this task. 
     */
    private Settings settings;

    // set by {@link #initialize()}. 
    protected LatexProcessor latexProcessor;

    /**
     * Invoked by ant returning a container for all parameters 
     * and initializing {@link #settings}. 
     */
    public Settings createSettings() {
	return this.settings = new Settings();
    }

    private File getPropertyFile(String prop) {
	return new File(getProject().getProperty(prop));
    }

    // api-docs inherited from ParameterAdapter 
    public void initialize() {
	// use of the reference to Project-instance
        //String message = getProperty("ant.project.name");
        // Task's log method
        //log("Here is project '" + message + "'. ");
	// almost the same as getProject().log(this, msg, msgLevel)

        // where this task is used?
        //log("I am used in: " + getLocation() + "'. ");
	if (this.settings == null) {
            throw new BuildException("No settings found. ");
        }
	this.settings.setBaseDirectory(getPropertyFile("basedir"));
	this.settings.setTargetSiteDirectory(getPropertyFile("targetSiteDir"));
	this.settings.setTargetDirectory(getPropertyFile("targetDir"));

 	//log("settings: \n" + this.settings);

	 this.latexProcessor = 
	     new LatexProcessor(this.settings,  
				new AntLogWrapper(getProject()), 
				this);
    }

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
