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
	private final String RTF_OUTPUT_FILES = "\\.rtf";
	public void processSource(LatexProcessor latexProcessor, 
				  File texFile) throws BuildExecutionException {
	    latexProcessor.processLatex2rtf(texFile);
	}
	public String getPatternOutputFileSuffixes() {
	    return RTF_OUTPUT_FILES;
	}

    },
    /**
     * standalone. 
     */
    pdf() {
	private final String LATEX_OUTPUT_FILES = 
	    "(" + LatexProcessor.SUFFIX_PDF + "|\\.dvi|\\.ps)";

	public void processSource(LatexProcessor latexProcessor, 
				  File texFile) throws BuildExecutionException {
	    latexProcessor.processLatex2pdf(texFile);
	}
	public String getPatternOutputFileSuffixes() {
	    return LATEX_OUTPUT_FILES;
	}
    },
    /**
     * Based on {@link #pdf}
     *
     */
    html() {
	// FIXME: use class constants for suffixes 
	// Documentation: 
	// - xxx.(x)htm(l) is the main file. 
	// - xxxlid.(x)htm(l) with digit(s) d 
	//                    are lists: toc, lof, lot, indices, glossaries, 
	//                    NOT the bibliography. 
	// - xxxsed.(x)htm(l) with digit(s) d are sections. 
	// - xxxsud.(x)htm(l) with digit(s) d are subsections or below. 
	// - xxxd.(x)htm(l)   with digit(s) d are footnotes 
	private final String HTML_OUTPUT_FILES = 
	    "(((se|su|li)?\\d+)?\\.x?html?|" + 
	    "\\.css|" + 
	    ".\\d+\\.svg)";
	public void processSource(LatexProcessor latexProcessor, 
				  File texFile) throws BuildExecutionException {
	    latexProcessor.processLatex2html(texFile);
	}
	public String getPatternOutputFileSuffixes() {
	    return HTML_OUTPUT_FILES;
	}
	public String getPatternOutputFiles() {
	    return "(cmsy)\\d+(-c)?-\\d+c?\\.png";
	}

    },
    /**
     * Based on {@link #pdf}
     *
     */
    odt() {
	private final String OOFFICE_OUTPUT_FILES =
	    "(.odt|.fodt|.uot|.uot)";
	public void processSource(LatexProcessor latexProcessor, 
				  File texFile) throws BuildExecutionException {
	    latexProcessor.processLatex2odt(texFile);
	}

	public String getPatternOutputFileSuffixes() {
	    return OOFFICE_OUTPUT_FILES;
	}
    },
    /**
     * Based on {@link #odt}
     *
     */
    docx() {
	private final String MSWORD_OUTPUT_FILES = 
	    "(\\.doc(|6|.95|.x|.x7)|\\.rtf)";
	public void processSource(LatexProcessor latexProcessor, 
				  File texFile) throws BuildExecutionException {
	    latexProcessor.processLatex2docx(texFile);
	}
	public String getPatternOutputFileSuffixes() {
	    return MSWORD_OUTPUT_FILES;
	}

    },
    /**
     * Based on {@link #pdf}
     */
    txt() {
	private final String TXT_OUTPUT_FILES = ".txt";
	public void processSource(LatexProcessor latexProcessor, 
				  File texFile) throws BuildExecutionException {
	    latexProcessor.processLatex2txt(texFile);
	}
	public String getPatternOutputFileSuffixes() {
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
    public abstract String getPatternOutputFileSuffixes();

    public String getPatternOutputFiles() {
	return "^$";
    }

}
 
