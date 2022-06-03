package eu.simuline.m2latex.antTask;

import eu.simuline.m2latex.core.LogWrapper;

import org.apache.tools.ant.Project;

/**
 * Performs logging in an ant task. 
 *
 *
 * Created: Fri Oct  7 00:40:27 2016
 *
 * @author <a href="mailto:rei3ner@arcor.de">Ernst Reissner</a>
 * @version 1.0
 */
public class AntLogWrapper implements LogWrapper {

    private final Project project;

    public AntLogWrapper(Project project) {
	this.project = project;
    }

    public void error(String msg) {
	this.project.log("[error] " + msg, Project.MSG_ERR);
    }

    public void warn(String msg) {
	this.project.log("[warning] " + msg, Project.MSG_WARN);
    }

    public void warn(String msg, Throwable thrw) {
	this.project.log("[warning] " + msg, thrw, Project.MSG_WARN);
    }

    public void info(String msg) {
	this.project.log("[info] " + msg, Project.MSG_INFO);
    }

    //void verbose(String msg);
    public void debug(String msg) {
	this.project.log("[debug] " + msg, Project.MSG_DEBUG);
    }

    // public void debug(String msg, Throwable thrw) {
    // 	this.project.log(msg, thrw, Project.MSG_DEBUG);
    // }


}
