package org.m2latex.core;

/**
 * This is needed as a wrapper 
 * to {@link org.apache.maven.plugin.MojoExecutionException} 
 * in {@link org.m2latex.mojo.AbstractLatexMojo#execute()} 
 * to avoid maven-specific classes. 
 * <p>
 * We read from the documentation which applies to this exception type also: 
 * A MojoExecutionException is a fatal exception, 
 * something unrecoverable happened. 
 * You would throw a MojoExecutionException 
 * if something happens that warrants a complete stop in a build; 
 * you re trying to write to disk, but there is no space left, 
 * or you were trying to publish to a remote repository, 
 * but you can’t connect to it. 
 * Throw a MojoExecutionException if there is no chance of a build continuing; 
 * something terrible has happened 
 * and you want the build to stop and the user to see a "BUILD ERROR" message.
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
