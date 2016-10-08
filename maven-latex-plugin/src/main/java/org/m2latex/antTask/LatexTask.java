package org.m2latex.antTask;

import org.apache.tools.ant.Task;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.BuildException;

import org.apache.maven.plugin.AbstractMojoExecutionException;

import org.m2latex.core.Settings;
import org.m2latex.mojo.PdfMojo;
import org.m2latex.core.MyBuildException;
import org.m2latex.core.BuildExecutionException;
import org.m2latex.core.LatexProcessor;
import org.m2latex.core.ParameterAdapter;
import org.m2latex.core.Target;

import java.io.File;

import java.util.Set;

public class LatexTask extends Task implements ParameterAdapter {

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

    private String getProperty(String prop) {
	return getProject().getProperty(prop);
    }

    private File getPropertyFile(String prop) {
	return new File(getProperty(prop));
    }

    private static final String[] LATEX_OUTPUT_FILES = new String[] {
	 ".pdf", ".dvi", ".ps"
    };

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
    public Set<Target> getTargetSet() {
	return this.settings.getTargetSet();
    }

    /**
     * Invoked by ant executing the task. 
     */
    public void execute() throws BuildException {
 	initialize();
	try {
	    this.latexProcessor.execute();
	} catch (MyBuildException e) {
	    throw new BuildException(e.getMessage(), e.getCause());
	}
     }
 }
