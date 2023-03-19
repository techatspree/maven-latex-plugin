package eu.simuline.m2latex.core;

import java.io.File;

/**
 * Enumeration of the (conversion of latex to a) target format, called a 'device'. 
 * Currently, there are two, <code>pdf</code> and <code>dvips</code>, 
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
    String getXFigInTexLanguage() {
      return "pdftex";
    }

    String getGnuplotInTexLanguage() {
      return "pdf";
    }

    String getInkscapeTexSuffix() {
      return LatexPreProcessor.SUFFIX_PDFTEX;
    }

    String getGraphicsInTexSuffix() {
      return LatexPreProcessor.SUFFIX_PDF;
    }

    String getLatexOutputFormat() {
      return "pdf";
    }

    boolean isViaDvi() {
      return false;
    }

    File latexTargetFile(LatexMainDesc desc) {
      return desc.pdfFile;
    }
  },
  // latex creates dvi but not with the given drivers. 
  // dvi can be created also with 
  // lualatex -output-format=dvi and also accordingly with 
  // pdflatex -output-format=dvi
  // pdf is the default but can also be created with 
  // lualatex -output-format=pdf and also with 
  // pdflatex -output-format=pdf 
  // xdv (!) can be created with xelatex -no-pdf
  // Without that option, i.e. by default, xelatex creates pdf like the other converter. 
  dvips {
    String getXFigInTexLanguage() {
      return "pstex";
    }

    String getGnuplotInTexLanguage() {
      return "eps";
    }

    String getInkscapeTexSuffix() {
      return LatexPreProcessor.SUFFIX_EPSTEX;
    }

    String getGraphicsInTexSuffix() {
      return LatexPreProcessor.SUFFIX_EPS;
    }

    String getLatexOutputFormat() {
      return "dvi";
    }

    boolean isViaDvi() {
      return true;
    }

    File latexTargetFile(LatexMainDesc desc) {
      return desc.dviFile;
    }
  };

  /**
   * Returns the name of the language 
   * used by the {@link Settings#getFig2devCommand()} 
   * to specify graphic without ``special'' text of an xfig-picture. 
   * The converse is specified 
   * by {@link LatexPreProcessor#XFIG_TEX_LANGUAGE}.
   * In fact, a file of that format is created which is 
   * embedded with <code>\includegraphics</code> in latex-code 
   * representing text. 
   */
  abstract String getXFigInTexLanguage();

  /**
   * Returns the name of the language 
   * used by the {@link Settings#getGnuplotCommand()} 
   * to specify graphic without text of a gnuplot-picture. 
   * In fact, there is a file of that format 
   * embedded with <code>\includegraphics</code> in latex-code 
   * representing text. 
   */
  abstract String getGnuplotInTexLanguage();

  /**
   * Returns file suffix for the tex part of the svg export 
   * created by the command {@link Settings#getSvg2devCommand()}.  
   */
  abstract String getInkscapeTexSuffix();

  /**
   * Returns the suffix of the file to be 
   * embedded with <code>\includegraphics</code> in latex-code 
   * representing all but text. 
   * This is used for processing fig-files and for processing svg-files 
   * in {@link LatexPreProcessor#runFig2DevInTex(File, LatexDev)} and 
   * in {@link LatexPreProcessor#runSvg2Dev(File, LatexDev, boolean)}, 
   * whereas for conversion of gnuplot-files, 
   * this suffix is set automatically. 
   * Note also that this is used to clear the created files 
   * in all three cases. 
   */
  abstract String getGraphicsInTexSuffix();

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
  abstract File latexTargetFile(LatexMainDesc desc);

  static LatexDev devViaDvi(boolean pdfViaDvi) {
    LatexDev res = pdfViaDvi ? LatexDev.dvips : LatexDev.pdf;
    assert res.isViaDvi() == pdfViaDvi;
    return res;
  }


}
