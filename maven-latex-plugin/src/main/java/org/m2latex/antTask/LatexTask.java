package org.m2latex.antTask;

import org.apache.tools.ant.Task;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.BuildException;

//import org.apache.maven.plugin.Mojo;
//import org.apache.maven.plugin.MojoFailureException;
//import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.AbstractMojoExecutionException;

import org.m2latex.mojo.Settings;
import org.m2latex.mojo.PdfMojo;
import java.io.File;

public class LatexTask extends Task {

    /**
     * Contains all parameters for executing this task. 
     */
    private Settings settings;

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

    /**
     * Invoked by ant executing the task. 
     */
    public void execute() throws BuildException {
        // use of the reference to Project-instance
        String message = getProperty("ant.project.name");
        // Task's log method
        log("Here is project '" + message + "'. ");

        // where this task is used?
        log("I am used in: " + getLocation() + "'. ");
	if (this.settings == null) {
            throw new BuildException("No settings found. ");
        }
	this.settings.setBaseDirectory(getPropertyFile("basedir"));
	this.settings.setTargetSiteDirectory(getPropertyFile("targetSiteDir"));
	this.settings.setTargetDirectory(getPropertyFile("targetDir"));

 	log("settings: \n" + this.settings);

	PdfMojo mojo = new PdfMojo();
	try {
	    mojo.execute();
	} catch (AbstractMojoExecutionException e) {
	    throw new BuildException(e.getMessage(), e.getCause());
	}
     }
 }
