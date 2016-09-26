package org.m2latex.antTask;

import org.apache.tools.ant.Task;

public class LatexTask extends Task {

    private String message;

    public void execute() {
        // use of the reference to Project-instance
        String message = getProject().getProperty("ant.project.name");

        // Task's log method
        log("Here is project '" + message + "'.");

        // where this task is used?
        log("I am used in: " +  getLocation() );
	log("message is: " +  this.message );
    }

    public void setMessage(String message) {
	this.message = message;
    }
}
