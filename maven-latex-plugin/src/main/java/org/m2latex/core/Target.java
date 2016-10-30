package org.m2latex.core;

import java.io.File;

/**
 * The enumeration of all supported targets. 
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
	private final String[] RTF_OUTPUT_FILES = new String[] {".rtf"};
	public void processSource(LatexProcessor latexProcessor, 
				  File texFile) throws BuildExecutionException {
	    latexProcessor.processLatex2rtf(texFile);
	}
	public String[] getOutputFileSuffixes() {
	    return RTF_OUTPUT_FILES;
	}

    },
    /**
     * standalone. 
     */
    pdf() {
	private final String[] LATEX_OUTPUT_FILES = new String[] {
	    LatexProcessor.SUFFIX_PDF, ".dvi", ".ps"
	};
	public void processSource(LatexProcessor latexProcessor, 
				  File texFile) throws BuildExecutionException {
	    latexProcessor.processLatex2pdf(texFile);
	}
	public String[] getOutputFileSuffixes() {
	    return LATEX_OUTPUT_FILES;
	}
    },
    /**
     * Based on {@link #pdf}
     *
     */
    html() {
	private final String[] HTML_OUTPUT_FILES = new String[] {
	    "*.html", "*.xhtml", "*.htm", ".css", "*.png", "*.svg"
	};
	public void processSource(LatexProcessor latexProcessor, 
				  File texFile) throws BuildExecutionException {
	    latexProcessor.processLatex2html(texFile);
	}
	public String[] getOutputFileSuffixes() {
	    return HTML_OUTPUT_FILES;
	}

    },
     /**
     * Based on {@link #pdf}
     *
     */
    odt() {
	private final String[] OOFFICE_OUTPUT_FILES = new String[] {
	    ".odt", ".fodt", ".uot", ".uot"
	};
	public void processSource(LatexProcessor latexProcessor, 
				  File texFile) throws BuildExecutionException {
	    latexProcessor.processLatex2odt(texFile);
	}

	public String[] getOutputFileSuffixes() {
	    return OOFFICE_OUTPUT_FILES;
	}
    },
    /**
     * Based on {@link #odt}
     *
     */
    docx() {
	private final String[] MSWORD_OUTPUT_FILES = new String[] {
	    ".doc", ".doc6", ".doc95", ".docx", ".docx7", ".rtf"
	};
	public void processSource(LatexProcessor latexProcessor, 
				  File texFile) throws BuildExecutionException {
	    latexProcessor.processLatex2docx(texFile);
	}
	public String[] getOutputFileSuffixes() {
	    return MSWORD_OUTPUT_FILES;
	}

    },
    /**
     * Based on {@link #pdf}
     */
    txt() {
	private final String[] TXT_OUTPUT_FILES = new String[] {".txt"};
	public void processSource(LatexProcessor latexProcessor, 
				  File texFile) throws BuildExecutionException {
	    latexProcessor.processLatex2txt(texFile);
	}
	public String[] getOutputFileSuffixes() {
	    return TXT_OUTPUT_FILES;
	}
    };

    /**
     * Processes the source file <code>texFile</code> 
     * according to the concrete Mojo. 
     */
    public abstract void processSource(LatexProcessor latexProcessor, 
    				       File texFile) 
    throws BuildExecutionException;


    /**
     * Returns the suffixes and wildcards of the output files. 
     * For example if creating pdf and postscript, 
     * this is just <code>.pdf, .ps</code> 
     * but if various html files are created, it is <code>*.html</code>, 
     * the asterisk representing a wildcard. 
     */
    public abstract String[] getOutputFileSuffixes();

}
