/**
 * Common code base of ant tasks 
 * given by {@link eu.simuline.m2latex.antTask} and 
 * (goals of) the maven plugin located at {@link eu.simuline.m2latex.mojo}.
 * Both are defined by classes  
 * with common interface {@link ParameterAdapter}. 
 * Both could be called 'target' in the sense as understood in a makefile, 
 * for short "make target". 
 * Dont mix this up with a (creational) target represented by {@link Target}, 
 * which is a transformation of a tex document into a document in a target format 
 * stored in the <code>target</code> folder for delivery (thus the name). 
 * To illustrate the difference, note that the maven goal 
 * defined by {@link eu.simuline.m2latex.mojo.CfgLatexMojo} 
 * may include any subset of (creational) targets, depending on the configuration. 
 * On the other hand, the goals defined by 
 * {@link eu.simuline.m2latex.mojo.ClearMojo}, 
 * {@link eu.simuline.m2latex.mojo.ChkMojo} (just checkstyle) 
 * {@link eu.simuline.m2latex.mojo.GraphicsMojo} 
 * performing preprocessing of graphics without deliverable, and 
 * {@link eu.simuline.m2latex.mojo.VersionMojo} 
 * which displays versions of the applications 
 * this software relies on and whether these versions are valid, 
 * do not correspond with any creational target. 
 * On the other hand, e.g. {@link eu.simuline.m2latex.mojo.ChkMojo} 
 * corresponds with target {@link Target#pdf}. 
 * <p>
 * From a functional point of view, the main classes of this package 
 * are {@link LatexPreProcessor} processing graphic files which need pre-processing 
 * and subsequent proper latex processing including bibliography and that like, 
 * in {@link LatexProcessor} with common superclass {@link AbstractLatexProcessor}. 
 * All processing steps are executed by external programs 
 * initiated by {@link CommandExecutor} via command line. 
 * <p>
 * Parameters of both processing steps are collected in class {@link Settings}. 
 * <p>
 * The build process is supervised and {@link LogWrapper} 
 * logs errors, warnings, infos and debug infos to the abstract build tool, 
 * which may be maven or ant. 
 * If either the build step or whole build must be aborted, 
 * {@link BuildFailureException} and {@link BuildExecutionException}, respectively 
 * must be thrown, both subclasses of {@link AbstractBuildException}. 
 * <p>
 * The rest of this package are auxiliary classes: 
 * {@link DirNode} represents the contents of a directory, 
 * {@link TexFileUtils} provides utilities to deal with files 
 * and finally {@link LatexDev} abstracts the two ways to get to a pdf: 
 * directly or via dvi format.  
 */
package eu.simuline.m2latex.core;
// rename antTask-->antTasks
