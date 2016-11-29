package org.m2latex.core;

/**
 * This is needed as a wrapper 
 * to {@link org.apache.maven.plugin.MojoFailureException} 
 * in {@link org.m2latex.mojo.AbstractLatexMojo#execute()} 
 * to avoid maven-specific classes. 
 * <p>
 * We read from the documentation which applies to this exception type also: 
 * A MojoFailureException is something less catastrophic 
 * (compared to a {@link org.apache.maven.plugin.MojoExecutionException}), 
 * a goal can fail, but it might not be the end of the world 
 * for your Maven build. 
 * A unit test can fail, or a MD5 checksum can fail; 
 * both of these are potential problems, 
 * but you don’t want to return an exception 
 * that is going to kill the entire build. 
 * In this situation you would throw a MojoFailureException. 
 * Maven provides for different "resiliency" settings 
 * when it comes to project failure. 
 *
 * Created: Fri Sep 30 15:01:16 2016
 *
 * @author <a href="mailto:rei3ner@arcor.de">Ernst Reissner</a>
 * @version 1.0
 */
public class BuildFailureException extends MyBuildException {

    public BuildFailureException(String message) {
	super(message);
    }

    public BuildFailureException(String message, Throwable cause) {
	super(message, cause);
    }
}
