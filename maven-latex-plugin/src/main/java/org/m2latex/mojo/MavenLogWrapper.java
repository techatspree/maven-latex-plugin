package org.m2latex.mojo;

import org.m2latex.core.LogWrapper;

import org.apache.maven.plugin.logging.Log;

/**
 * Describe interface LogWrapper here.
 *
 *
 * Created: Fri Oct  7 00:40:27 2016
 *
 * @author <a href="mailto:rei3ner@arcor.de">Ernst Reissner</a>
 * @version 1.0
 */
public class MavenLogWrapper implements LogWrapper {

    private final Log log;

    // public for tests only. 
    public MavenLogWrapper(Log log) {
	this.log = log;
    }

    public void error(String msg) {
	this.log.error(msg);
    }

    public void warn(String msg) {
	this.log.warn(msg);
    }

    public void warn(String msg, Throwable thrw) {
	this.log.warn(msg, thrw);
    }

    public void info(String msg) {
	this.log.info(msg);
    }
    //void verbose(String msg);

    public void debug(String msg) {
	this.log.debug(msg);
    }

    // public void debug(String msg, Throwable thrw) {
    // 	this.log.debug(msg, thrw);
    // }


}
