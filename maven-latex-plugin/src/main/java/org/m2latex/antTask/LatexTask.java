package org.m2latex.antTask;

import org.apache.tools.ant.Task;
import org.apache.tools.ant.BuildException;

//import org.apache.maven.plugin.Mojo;
//import org.apache.maven.plugin.MojoFailureException;
//import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.AbstractMojoExecutionException;

import org.m2latex.mojo.Settings;
import org.m2latex.mojo.PdfMojo;

public class LatexTask extends Task {

    private Settings settings;

    public Settings createSettings() {
	return this.settings = new Settings();
    }

    public void execute() throws BuildException {
        // use of the reference to Project-instance
        String message = getProject().getProperty("ant.project.name");

        // Task's log method
        log("Here is project '" + message + "'.");

        // where this task is used?
        log("I am used in: " +  getLocation() );
	if (this.settings == null) {
            throw new BuildException("No settings found.");
        }
 	log("settings: \n" +  this.settings );

	PdfMojo mojo = new PdfMojo();
	try {
	    mojo.execute();
	} catch (AbstractMojoExecutionException e) {
	    throw new BuildException(e.getMessage(), e.getCause());
	}
     }
 }
