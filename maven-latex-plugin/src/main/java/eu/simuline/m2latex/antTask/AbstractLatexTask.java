package eu.simuline.m2latex.antTask;

import org.apache.tools.ant.Task;

import eu.simuline.m2latex.core.Settings;
import eu.simuline.m2latex.core.LatexProcessor;
import eu.simuline.m2latex.core.ParameterAdapter;

import java.io.File;

/**
 * Abstract base class of all tasks provided by this latex converter. 
 * @author ernst
 *
 */
abstract class AbstractLatexTask extends Task implements ParameterAdapter {

  /**
   * Contains all parameters for executing this task. 
   */
  protected Settings settings;

  // set by {@link #initialize()}. 
  protected LatexProcessor latexProcessor;

  /**
   * Invoked by ant returning a container for all parameters 
   * and initializing {@link #settings}. 
   */
  public Settings createSettings() {
    return this.settings = new Settings();
  }

  private File getPropertyFile(String prop) {
    return new File(getProject().getProperty(prop));
  }

  // api-docs inherited from ParameterAdapter 
  public final void initialize() {
    // use of the reference to Project-instance
    //String message = getProperty("ant.project.name");
    // Task's log method
    //log("Here is project '" + message + "'. ");
    // almost the same as getProject().log(this, msg, msgLevel)

    // where this task is used?
    //log("I am used in: " + getLocation() + "'. ");
    if (this.settings == null) {
      // Here, no configuration is defined in build file, 
      // i.e. object is not created by ant
      this.settings = new Settings();
    }
    this.settings.setBaseDirectory(getPropertyFile("basedir"));
    this.settings.setTargetSiteDirectory(getPropertyFile("targetSiteDir"));
    this.settings.setTargetDirectory(getPropertyFile("targetDir"));

    //log("settings: \n" + this.settings);

    this.latexProcessor = new LatexProcessor(this.settings,
        new AntLogWrapper(getProject()), this);
  }

}
