package org.m2latex.mojo;

/**
 * Describe class MyBuildException here.
 *
 *
 * Created: Fri Sep 30 15:01:16 2016
 *
 * @author <a href="mailto:rei3ner@arcor.de">Ernst Reissner</a>
 * @version 1.0
 */
public class MyBuildException extends Exception {

    private final String message;
    private final Throwable cause;

   public MyBuildException(String message) {
       this(message, null);
    }
    public MyBuildException(String message, Throwable cause) {
	this.message = message;
	this.cause = cause;
    }
}
