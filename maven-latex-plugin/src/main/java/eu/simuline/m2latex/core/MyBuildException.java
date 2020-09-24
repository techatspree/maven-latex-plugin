package eu.simuline.m2latex.core;
// TBD: rename: LatexBuildException, maybe abstract 
/**
 * The base class for {@link BuildExecutionException} 
 * and for {@link BuildFailureException}. 
 * This is needed as a wrapper to {@link org.apache.tools.ant.BuildException} 
 * in {@link eu.simuline.m2latex.antTask.LatexCfgTask#execute()} 
 * to avoid ant-specific classes. 
 *
 *
 * Created: Fri Sep 30 15:01:16 2016
 *
 * @author <a href="mailto:rei3ner@arcor.de">Ernst Reissner</a>
 * @version 1.0
 */
class MyBuildException extends Exception {

   protected MyBuildException(String message) {
       super(message);
    }

    protected MyBuildException(String message, Throwable cause) {
	super(message, cause);
    }
}
