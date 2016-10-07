package org.m2latex.core;

/**
 * Describe interface LogWrapper here.
 *
 *
 * Created: Fri Oct  7 00:40:27 2016
 *
 * @author <a href="mailto:rei3ner@arcor.de">Ernst Reissner</a>
 * @version 1.0
 */
public interface LogWrapper {

    public void error(String msg);
    public void warn(String msg);
    public void warn(String msg, Throwable thrw);
    public void info(String msg);
    //void verbose(String msg);

    // short for debug(null, msg)
    public void debug(String msg);
    //public void debug(String msg, Throwable thrw);


}
