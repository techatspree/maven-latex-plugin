package org.m2latex.core;

import java.io.File;

/**
 * Describe enum Target here.
 *
 *
 * Created: Fri Oct  7 13:32:21 2016
 *
 * @author <a href="mailto:rei3ner@arcor.de">Ernst Reissner</a>
 * @version 1.0
 */
public enum Target {

    /**
     * 
     *
     */
    Docx() {
	private final String[] MSWORD_OUTPUT_FILES = new String[] {
	    ".doc", ".docx", ".rtf"
	};
	public void processSource(LatexProcessor latexProcessor, 
				  File texFile) throws BuildExecutionException {
	    latexProcessor.processLatex2docx(texFile);
	}
	public String[] getOutputFileSuffixes() {
	    return MSWORD_OUTPUT_FILES;
	}

    },
    Html() {
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
    Rtf() {
	private final String[] RTF_OUTPUT_FILES = new String[] {".rtf"};
	public void processSource(LatexProcessor latexProcessor, 
				  File texFile) throws BuildExecutionException {
	    latexProcessor.processLatex2rtf(texFile);
	}
	public String[] getOutputFileSuffixes() {
	    return RTF_OUTPUT_FILES;
	}

    },
    Txt() {
	private final String[] TXT_OUTPUT_FILES = new String[] {".txt"};
	public void processSource(LatexProcessor latexProcessor, 
				  File texFile) throws BuildExecutionException {
	    latexProcessor.processLatex2txt(texFile);
	}
	public String[] getOutputFileSuffixes() {
	    return TXT_OUTPUT_FILES;
	}

    },
    Odt() {
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
    Pdf() {
	private final String[] LATEX_OUTPUT_FILES = new String[] {
	    ".pdf", ".dvi", ".ps"
	};
	public void processSource(LatexProcessor latexProcessor, 
				  File texFile) throws BuildExecutionException {
	    latexProcessor.processLatex2pdf(texFile);
	}
	public String[] getOutputFileSuffixes() {
	    return LATEX_OUTPUT_FILES;
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
