package eu.simuline.m2latex.core;

import java.io.File;

/**
 * Enumeration of the (conversion of latex to a) target format, called a 'device'. 
 * Currently, there are two, <code>pdf</code> and <code>dvips</code>, 
 * the latter representing <code>dvi</code> and code>xdv</code> 
 * but <code>html</code> and <code>odt</code> are also desirable. 
 * The backend also affects the natural graphic formats: 
 * Whereas for the backend <code>pdf</code>, 
 * also <code>pdf</code> is used, 
 * <code>dvips</code> uses postscript-based formats. 
 * Note that <code>xelatex</code> xdv format 
 * corresponds with the other converters dvi format. 
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

    boolean isDefault() {
      return true;
    }

    File latexTargetFile(LatexMainDesc desc, boolean isTypeXelatex) {
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

    boolean isDefault() {
      return false;
    }

    File latexTargetFile(LatexMainDesc desc, boolean isTypeXelatex) {
      return isTypeXelatex ? desc.xdvFile : desc.dviFile;
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
   * The format created without specific command line option. 
   * This is true just pdf. 
   * It is false for dvi like formats; besides dvi itself xdv. 
   */
  abstract boolean isDefault();

  /**
   * Returns the target file of a LaTeX run. 
   * This has the suffix given by {@link #getLatexOutputFormat()}. 
   * 
   * @param desc
   *    the latex main description. 
   * @param isTypeXelatex
   *    This is relevant only for {@link #dvips} 
   *    returning xdv or dvi. 
   * @return
   *    the latex target file. 
   *    If <code>xxx.tex</code> is the latex main file, 
   *    then the target file is <code>xxx.dvi</code>, <code>xxx.xdv</code> 
   *    or <code>xxx.pdf</code>. 
   */
  abstract File latexTargetFile(LatexMainDesc desc, boolean isTypeXelatex);

  /**
   * Invoked in settings. 
   * 
   * @param pdfViaDvi
   *    whether the switch {@link Settings#pdfViaDvi} is set. 
   * @return
   *    {@link LatexDev#dvips} if <code>pdfViaDvi</code>; else {@link LatexDev#pdf};
   */
  static LatexDev devViaDvi(boolean pdfViaDvi) {
    LatexDev res = pdfViaDvi ? LatexDev.dvips : LatexDev.pdf;
    assert res.isViaDvi() == pdfViaDvi;
    return res;
  }


}
