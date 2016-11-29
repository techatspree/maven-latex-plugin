package org.m2latex.core;

import java.io.File;
import java.util.Arrays;

/**
 * Describe class AbstractLatexProcessor here.
 *
 *
 * Created: Thu Nov 17 12:29:36 2016
 *
 * @author <a href="mailto:rei3ner@arcor.de">Ernst Reissner</a>
 * @version 1.0
 */
abstract class AbstractLatexProcessor {

    // both LatexProcessor and LatexPreProcessor 
    // LaTeX and mpost with option -recorder 
    final static String SUFFIX_FLS = ".fls";
    // both LatexProcessor and LatexPreProcessor 
    // for LaTeX but also for mpost 
    final static String SUFFIX_LOG = ".log";

    // both LatexProcessor and LatexPreProcessor 
    final static String SUFFIX_PDF = ".pdf";


    // both LatexProcessor and LatexPreProcessor 
    protected final Settings settings;

    // both LatexProcessor and LatexPreProcessor 
    protected final CommandExecutor executor;

    // both LatexProcessor and LatexPreProcessor 
    protected final LogWrapper log;

    // both LatexProcessor and LatexPreProcessor 
    protected final TexFileUtils fileUtils;

    /**
     * Creates a new <code>AbstractLatexProcessor</code> instance.
     *
     */
    public AbstractLatexProcessor(Settings settings, 
				  CommandExecutor executor, 
				  LogWrapper log, 
				  TexFileUtils fileUtils) {
        this.settings = settings;
        this.log = log;
        this.executor = executor;
        this.fileUtils = fileUtils;
    }

    /**
     * Logs if an error occurred running <code>command</code> 
     * by detecting that the log file <code>logFile</code> has not been created 
     * or by detecting the error pattern <code>pattern</code> 
     * in <code>logFile</code>. 
     *
     * @throws BuildFailureException
     *    if <code>logFile</code> does not exist or is not readable. 
     * @see #logWarns(File, String, String) 
     */
     protected void logErrs(File logFile, String command, String pattern) 
    	throws BuildFailureException {

    	if (logFile.exists()) {
    	    // matchInFile may throw BuildFailureException
    	    if (this.fileUtils.matchInFile(logFile, pattern)) {
    		log.warn("Running " + command + " failed. For details see " + 
    			 logFile.getName() + ". ");
    	    }
    	} else {
    	    this.log.error("Running " + command + " failed: no log file " + 
    			   logFile.getName() + " found. ");
    	}
    }

    /**
     * Logs if a warning occurred running <code>command</code> 
     * by detecting the warning pattern <code>pattern</code> 
     * in <code>logFile</code>. 
     * If <code>logFile</code> then an error occurred 
     * making detection of warnings obsolete. 
     *
     * @see #logErrs(File, String, String) 
     */
    // for both LatexProcessor and LatexPreProcessor 
    protected void logWarns(File logFile, String command, String pattern) 
    	throws BuildFailureException {
    	if (logFile.exists() && this.fileUtils.matchInFile(logFile, pattern)) {
    	    log.warn("Running " + command + 
    		     " emitted warnings. For details see " + 
    		     logFile.getName() + ". ");
    	}
    }

    // for both LatexProcessor and LatexPreProcessor 
    protected boolean update(File source, File target) {
	if (!target.exists()) {
	    return true;
	}
	assert source.exists();

	return source.lastModified() > target.lastModified();
    }

    /**
     * Returns an array of strings, 
     * each entry with a single option given by <code>options</code> 
     * except the last one which is the name of <code>file</code>. 
     *
     * @param options
     *    the options string. The individual options 
     *    are expected to be separated by a single blank. 
     * @param file
     *    
     * @return
     *    An array of strings: 
     *    The 0th entry is the file name, 
     *    The others, if <code>options</code> is not empty, 
     *    are the options in <code>options</code>. 
     */
    // for both LatexProcessor and LatexPreProcessor 
    protected static String[] buildArguments(String options, File file) {
    	if (options.isEmpty()) {
    	    return new String[] {file.getName()};
    	}
        String[] optionsArr = options.split(" ");
        String[] args = Arrays.copyOf(optionsArr, optionsArr.length + 1);
        args[optionsArr.length] = file.getName();
	
    	return args;
     }
}
