package eu.simuline.m2latex.mojo;

import eu.simuline.m2latex.core.LogWrapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Performs logging in a maven plugin. 
 *
 *
 * Created: Fri Oct  7 00:40:27 2016
 *
 * @author <a href="mailto:rei3ner@arcor.de">Ernst Reissner</a>
 * @version 1.0
 */
public class MavenLogWrapper implements LogWrapper {

  private final Logger log;

  public MavenLogWrapper(Class<?> cls) {
    this.log = LoggerFactory.getLogger(cls);
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
