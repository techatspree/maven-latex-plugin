package org.m2latex.core;

import java.io.File;

/**
 * Enumeration of the backends of latex. 
 * Currently, there are two, <code>pdf</code> and <code>dvips</code> 
 * the latter representing <code>dvi</code> 
 * but <code>html</code> and <code>odt</code> are also desirable. 
 * The backend also affects the natural graphic formats: 
 * Whereas for the backend <code>pdf</code>, 
 * also <code>pdf</code> is used, 
 * <code>dvips</code> uses postscript-based formats. 
 *
 * Created: Tue Oct 18 10:06:30 2016
 *
 * @author <a href="mailto:rei3ner@arcor.de">Ernst Reissner</a>
 * @version 1.0
 */
public enum LatexDev {

    // lualatex creates pdf 
    pdf {
	String  getXFigInTexLanguage() {
	    return "pdftex";
	}
	String getGraphicsInTexSuffix() {
	    return LatexPreProcessor.SUFFIX_PDF;
	}
	String getGnuplotInTexLanguage() {
	    return "pdf";
	}
	String getSvgExportOption() {
	    return "-A="; // --export-pdf=FILENAME
	}
	String getLatexOutputFormat() {
	    return "pdf";
	}
	boolean isViaDvi() {
	    return false;
	}
	File latexTargetFile(LatexProcessor.LatexMainDesc desc) {
	    return desc.pdfFile;
	}
    },
    // latex creates dvi but not with the given drivers. 
    dvips {
	String  getXFigInTexLanguage() {
	    return "pstex";
	}
	String getGraphicsInTexSuffix() {
	    return LatexPreProcessor.SUFFIX_EPS;
	}
	String getGnuplotInTexLanguage() {
	    return "eps";
	}
	String getSvgExportOption() {
	    return "-E="; // --export-eps=FILENAME
	}
	String getLatexOutputFormat() {
	    return "dvi";
	}
	boolean isViaDvi() {
	    return true;
	}
	File latexTargetFile(LatexProcessor.LatexMainDesc desc) {
	    return desc.dviFile;
	}
    };

    /**
     * Returns the name of the language 
     * {@link Settings#getFig2devCommand()} uses 
     * to convert the graphic without text of an xfig-picture into. 
     * In fact, a file of that format is created which is 
     * embedded with <code>\includegraphics</code> in latex-code 
     * representing text. 
     */
    abstract String getXFigInTexLanguage();

    /**
     * Returns the suffix of the file to be 
     * embedded with <code>\includegraphics</code> in latex-code 
     * representing all but text. 
     * This is used for processing fig-files and for processing svg-files. 
     */
    abstract String getGraphicsInTexSuffix();

    /**
     * Returns the name of the language <code>gnuplot</code> uses 
     * to convert the graphic without text of a gnuplot-picture into. 
     * In fact, there is a file of that format 
     * embedded with <code>\includegraphics</code> in latex-code 
     * representing text. 
     */
    abstract String getGnuplotInTexLanguage();

    /**
     * Returns the export option used by the {@link #getSvg2devCommand()} 
     * which determines the export format. 
     * In fact, there is a file of that format 
     * to be embedded with <code>\includegraphics</code> in latex-code. 
     */
    abstract String getSvgExportOption();

    /**
     * Returns the name of the target language <code>latex2dev</code> uses 
     * to convert the latex files into. 
     * This is set via option <code>-output-format=</code>. 
     */
     abstract String getLatexOutputFormat();

     abstract boolean isViaDvi();

    /**
     * Returns the target file of a LaTeX run. 
     * This has the suffix given by {@link #getLatexOutputFormat()}. 
     */
    abstract File latexTargetFile(LatexProcessor.LatexMainDesc desc);

    static LatexDev devViaDvi(boolean pdfViaDvi) {
	LatexDev res = pdfViaDvi ? LatexDev.dvips : LatexDev.pdf;
	assert res.isViaDvi() == pdfViaDvi;
	return res;
    }


}
