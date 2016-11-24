package org.m2latex.core;

import java.io.File;

/**
 * The enumeration of all supported creational targets. 
 * Additional targets are <code>clr</code> and <code>grp</code>. 
 * The latter is not creational. 
 *
 *
 * Created: Fri Oct  7 13:32:21 2016
 *
 * @author <a href="mailto:rei3ner@arcor.de">Ernst Reissner</a>
 * @version 1.0
 */
public enum Target {

   /**
     * standalone
     *
     */
    rtf() {
	private final String RTF_OUTPUT_FILES = "^T$T\\.rtf$";

	public void processSource(LatexProcessor latexProcessor, 
				  File texFile) throws BuildExecutionException {
	    latexProcessor.processLatex2rtf(texFile);
	}

	public String getPatternOutputFiles(Settings settings) {
	    return RTF_OUTPUT_FILES;
	}

    },
    /**
     * standalone. 
     */
    pdf() {
	private final String LATEX_OUTPUT_FILES = 
	    "^T$T\\.(pdf|dvi|ps)$";

	public void processSource(LatexProcessor latexProcessor, 
				  File texFile) throws BuildExecutionException {
	    latexProcessor.processLatex2pdf(texFile);
	}

	public String getPatternOutputFiles(Settings settings) {
	    return LATEX_OUTPUT_FILES;
	}
    },
    /**
     * Based on {@link #pdf}
     *
     */
    html() {
	public void processSource(LatexProcessor latexProcessor, 
				  File texFile) throws BuildExecutionException {
	    latexProcessor.processLatex2html(texFile);
	}

	public String getPatternOutputFiles(Settings settings) {
	    return settings.getPatternT4htOutputFiles();
	}
    },
    /**
     * Based on {@link #pdf}
     *
     */
    odt() {
	private final String OOFFICE_OUTPUT_FILES =
	    "^T$T\\.(odt|fodt|uot|uot)$";

	public void processSource(LatexProcessor latexProcessor, 
				  File texFile) throws BuildExecutionException {
	    latexProcessor.processLatex2odt(texFile);
	}

	public String getPatternOutputFiles(Settings settings) {
	    return OOFFICE_OUTPUT_FILES;
	}
    },
    /**
     * Based on {@link #odt}
     *
     */
    docx() {
	private final String MSWORD_OUTPUT_FILES = 
	    "^T$T\\.(doc(|6|.95|.x|.x7)|rtf)$";

	public void processSource(LatexProcessor latexProcessor, 
				  File texFile) throws BuildExecutionException {
	    latexProcessor.processLatex2docx(texFile);
	}

	public String getPatternOutputFiles(Settings settings) {
	    return MSWORD_OUTPUT_FILES;
	}
    },
    /**
     * Based on {@link #pdf}
     */
    txt() {
	private final String TXT_OUTPUT_FILES = "^T$T\\.txt$";

	public void processSource(LatexProcessor latexProcessor, 
				  File texFile) throws BuildExecutionException {
	    latexProcessor.processLatex2txt(texFile);
	}

	public String getPatternOutputFiles(Settings settings) {
	    return TXT_OUTPUT_FILES;
	}
    };

    /**
     * Processes the latex main file <code>texFile</code> 
     * delegating to <code>latexProcessor</code>. 
     *
     * @param latexProcessor
     *    the processor to process <code>texFile</code> 
     * @param texFile
     *    the latex main file to be processed. 
     */
    public abstract void processSource(LatexProcessor latexProcessor, 
    				       File texFile) 
    throws BuildExecutionException;

    /**
     * Returns the pattern of the output files. 
     * For example if creating pdf, 
     * this is just <code>^T$T\.pdf$</code>, 
     * where <code>T$T</code> represents the name of the latex main file 
     * without suffix. 
     * For target {@link #html}, this is much more complicated, 
     * because a lot of files are created in general, 
     * not only <code>^T$T\.h?tml?$</code>. 
     *
     * @param settings
     *    the settings required to determine the pattern. 
     *    This depends on the settings for {@link #html} only. 
     */
    public abstract String getPatternOutputFiles(Settings settings);

}
 
