package eu.simuline.m2latex.core;

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
    // Used only to clean, 
    // but only in LatexPreProcessor, because else clean mechanism is just xxx.yyy 
    // In the long run, this will be used by both, 
    // because it is scanned to find out dependencies 
    final static String SUFFIX_FLS = ".fls";
    // both LatexProcessor and LatexPreProcessor, used in LatexMainDesc
    // for LaTeX but also for mpost 
    // used to 
    final static String SUFFIX_LOG = ".log";

    // both LatexProcessor and LatexPreProcessor 
    final static String SUFFIX_PDF = ".pdf";

    // makeindex for glossary 
    // needed by makeglossaries and for svg-conversion (hack) 
    final static String SUFFIX_VOID = "";
 
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
     * <p>
     * Logging: 
     * <ul>
     * <li> EAP01 Running <code>command</code> failed. For details...
     * <li> EAP02 Running <code>command</code> failed. No log file 
     * <li> WAP04 if <code>logFile</code> is not readable. 
     * <li> WFU03 cannot close 
     * </ul>
     * @see #logWarns(File, String, String) 
     */
     protected void logErrs(File logFile, String command, String pattern) {
    	if (logFile.exists()) {
	    // hasErrsWarns may log warnings WFU03, WAP04 
    	    if (hasErrsWarns(logFile, pattern)) {
    		this.log.error("EAP01: Running " + command + 
			       " failed. Errors logged in '" + 
			       logFile.getName() + "'. ");
    	    }
    	} else {
    	    this.log.error("EAP02: Running " + command + 
			   " failed: No log file '" + 
    			   logFile.getName() + "' written. ");
    	}
    }

    /**
     * Logs if a warning occurred running <code>command</code> 
     * by detecting the warning pattern <code>pattern</code> 
     * in <code>logFile</code>. 
     * If <code>logFile</code> then an error occurred 
     * making detection of warnings obsolete. 
     * <p>
     * Logging: 
     * <ul>
     * <li> WAP03 Running <code>command</code> emitted warnings. 
     * <li> WAP04 if <code>logFile</code> is not readable. 
     * <li> WFU03 cannot close 
     * </ul>
     *
     * @see #logErrs(File, String, String) 
     */
    // for both LatexProcessor and LatexPreProcessor 
    // FIXME: for LatexPreProcessor never needed. 
    protected void logWarns(File logFile, String command, String pattern) {
	// hasErrsWarns may log warnings WFU03, WAP04 
    	if (logFile.exists() && hasErrsWarns(logFile, pattern)) {
	    // logs warning WAP03: emitted warnings 
	    logWarn(logFile, command);
    	}
    }

    /**
     * <p>
     * Logging: 
     * WAP03 Running <code>command</code> emitted warnings. 
     */
    // invoked by logWarns(File, String, String) and 
    // LatexProcessor.logWarns(File, String)
    protected void logWarn(File logFile, String command) {
	this.log.warn("WAP03: Running " + command + 
		      " emitted warnings logged in '" + 
		      logFile.getName() + "'. ");
    }

    /**
     *
     * Logging: 
     * <ul>
     * <li> WFU03 cannot close 
     * <li> WAP04 if <code>logFile</code> is not readable. 
     * </ul>
     */
    // FIXME: not clear whether error or warning; also command not clear. 
    // used in 
    // logErrs (File, String, String)
    // logWarns(File, String, String)
    protected boolean hasErrsWarns(File logFile, String pattern) {
        assert logFile.exists() && !logFile.isDirectory()
                : "Expected existing (regular) log file " + logFile;
        // may log warning WFU03 cannot close
        Boolean res = this.fileUtils.matchInFile(logFile, pattern);
        if (res == null) {
            this.log.warn("WAP04: Cannot read log file '" + logFile.getName() +
                    "'; may hide warnings/errors. ");
            return false;
        }
        return res;
    }

    // for both LatexProcessor and LatexPreProcessor 
    protected boolean update(File source, File target) {
	if (!target.exists()) {
	    return true;
	}
	assert source.exists() && !source.isDirectory()
	    : "Expected existing (regular) source "+source;

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
     *    the file argument 
     * @return
     *    An array of strings: 
     *    The 0th entry is the file name, 
     *    The others, if <code>options</code> is not empty, 
     *    are the options in <code>options</code>. 
     */
    // for both LatexProcessor and LatexPreProcessor 
    // and in tests 
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
