package org.m2latex.core;

/**
 * The base class for {@link BuildExecutionException} 
 * and for {@link BuildFailureException}. 
 * This is needed as a wrapper to {@link org.apache.tools.ant.BuildException} 
 * in {@link org.m2latex.antTask.LatexCfgTask#execute()} 
 * to avoid ant-specific classes. 
 *
 *
 * Created: Fri Sep 30 15:01:16 2016
 *
 * @author <a href="mailto:rei3ner@arcor.de">Ernst Reissner</a>
 * @version 1.0
 */
class MyBuildException extends Exception {

   public MyBuildException(String message) {
       super(message);
    }

    public MyBuildException(String message, Throwable cause) {
	super(message, cause);
    }
}
