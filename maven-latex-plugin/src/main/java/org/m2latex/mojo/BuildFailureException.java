package org.m2latex.mojo;

/**
 * Describe class BuildFailureException here.
 *
 *
 * Created: Fri Sep 30 15:01:16 2016
 *
 * @author <a href="mailto:rei3ner@arcor.de">Ernst Reissner</a>
 * @version 1.0
 */
public class BuildFailureException extends MyBuildException {

    public BuildFailureException(String message, Throwable cause) {
	super(message, cause);
    }
}
