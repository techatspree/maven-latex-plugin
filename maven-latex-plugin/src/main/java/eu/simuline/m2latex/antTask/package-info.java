/**
 * Code specific for ant tasks defined by this latex converter. 
 * Note that the twin package 
 * specific for an according maven plugin is {@link eu.simuline.m2latex.mojo}, 
 * whereas the common code base is in {@link eu.simuline.m2latex.mojo}. 
 * <p>
 * This package consists of one class per task, 
 * <ul>
 * <li>{@link LatexCfgTask} for the configurable create task</li>
 * <li>{@link LatexClrTask} for the clean task eliminating the created files.</li>
 * </ul>
 * In addition, there is a common base class for these task classes 
 * {@link AbstractLatexTask} and an implementation for a logger 
 * {@link AntLogWrapper} extending {@link eu.simuline.m2latex.core.LogWrapper}.
 */
package eu.simuline.m2latex.antTask;
// rename antTask-->antTasks