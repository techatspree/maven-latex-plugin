package org.m2latex.core;

/**
 * This is needed as a wrapper 
 * to {@link org.apache.maven.plugin.MojoExecutionException} 
 * in {@link org.m2latex.mojo.AbstractLatexMojo#execute()} 
 * to avoid maven-specific classes. 
 *
 * Created: Fri Sep 30 15:01:16 2016
 *
 * @author <a href="mailto:rei3ner@arcor.de">Ernst Reissner</a>
 * @version 1.0
 */
public class BuildExecutionException extends MyBuildException {


    public BuildExecutionException(String message) {
	super(message);
    }
    public BuildExecutionException(String message, Throwable cause) {
	super(message, cause);
    }
}
