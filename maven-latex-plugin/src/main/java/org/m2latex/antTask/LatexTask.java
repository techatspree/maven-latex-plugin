package org.m2latex.antTask;

import org.apache.tools.ant.Task;
import org.apache.tools.ant.BuildException;
import org.m2latex.mojo.Settings;

public class LatexTask extends Task {

    private Settings settings;

    public Settings createSettings() {
	return this.settings = new Settings();
    }

    public void execute() {
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
    }

 }
