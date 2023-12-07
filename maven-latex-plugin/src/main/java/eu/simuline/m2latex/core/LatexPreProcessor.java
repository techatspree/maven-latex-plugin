/*
 * The akquinet maven-latex-plugin project
 *
 * Copyright (c) 2011 by akquinet tech@spree GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package eu.simuline.m2latex.core;

import java.io.File;
import java.io.FileFilter;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * The latex pre-processor is for preprocessing graphic files
 * in formats which cannot be included directly into a latex-file
 * and in finding the latex main files
 * which is done in {@link #processGraphicsSelectMain(File, DirNode)}
 * and in clearing the created files from the latex source directory
 * in {@link #clearCreated(File)}.
 */
public class LatexPreProcessor extends AbstractLatexProcessor {

  /**
   * Maps the suffix to the according handler.
   * If the handler is <code>null</code>, there is no handler.
   */
  private final static Map<String, SuffixHandler> SUFFIX2HANDLER =
      new TreeMap<String, SuffixHandler>();

  static {
    for (SuffixHandler handler : SuffixHandler.values()) {
      SUFFIX2HANDLER.put(handler.getSuffix(), handler);
    }
  } // static

  // used in preprocessing only (once)
  private final static String SUFFIX_TEX = ".tex";

  // home-brewed ending to represent tex including postscript 
  // TBD: used internally in methods runFig2TexInclDev, clearTargetPtxPdfEps, runGnuplot2Dev
  // used outside only in TexFileTuils.filterInkscapeIncludeFile
  final static String SUFFIX_PTX = ".ptx";
  // the next two for preprocessing and in LatexDev only
  // TBD: analyze: also: PDF, EPS
  final static String SUFFIX_PDFTEX = ".pdf_tex";// LatexDev only 
  final static String SUFFIX_EPSTEX = ".eps_tex";// LatexDev

  // suffix for xfig
  private final static String SUFFIX_FIG = ".fig";
  // suffix for svg
  private final static String SUFFIX_SVG = ".svg";
  // suffix for gnuplot
  // FIXME: to be made configurable
  private final static String SUFFIX_GP = ".gp";
  // suffix for metapost
  private final static String SUFFIX_MP = ".mp";
  // from xxx.mp creates xxx1.mps, xxx.log and xxx.mpx
  private final static String SUFFIX_MPS = ".mps";
  private final static String SUFFIX_MPX = ".mpx";

  // just for message
  private final static String SUFFIX_JPG = ".jpg";
  private final static String SUFFIX_PNG = ".png";
  // just for silently skipping
  private final static String SUFFIX_BIB = ".bib";
  // for latex main file creating html and for graphics.
  static final String SUFFIX_EPS = ".eps";// LatexDev

  private final static String SUFFIX_XBB = ".xbb";
  private final static String SUFFIX_BB = ".bb";

  LatexPreProcessor(Settings settings, CommandExecutor executor, LogWrapper log,
      TexFileUtils fileUtils) {
    super(settings, executor, log, fileUtils);
  }

  // Formats that work with LaTeX (dvi mode, using dvips):
  // eps
  // Formats that work with LaTeX (dvi mode, using dvipdfm(x)):
  // pdf, png, jpeg, eps (the latter not taken into account)
  // eps-source files handled via package epstopdf:
  // seemingly automatically converted eps-->pdf during latex run
  // also there is a program epstopdf and epspdf
  // There is a lot of experiments to do!!
  // MISSING: pdf and eps
  // NOTE: graphics is typically only included via dvipdfm(x)
  // Formats that work with pdfLaTeX (pdf mode):
  // pdf, png, jpeg, jbig2 (the latter not taken into account)
  // LuaTeX can also read
  // jpeg 2000 (not taken into account)
  //
  // Seemingly, it makes sense to distinguish from pdfViaDvi-parameter:
  // if set, seemingly, pdf, pgn and jpg is includable only
  // creating .bb or .xbb.

  // mp: besides mpost we also have mptopdf creating pdf:
  // mptopdf 05someMetapost.mp creates 05someMetapost1.mps
  // mptopdf 05someMetapost1.mps creates 05someMetapost1-mps.pdf

  /**
   * Handler for each suffix of a source file.
   * Mostly, these represent graphic formats
   * but also {@link #SUFFIX_TEX} is required
   * to detect the latex main files
   * and {@link #SUFFIX_TEX} and {@link #SUFFIX_BIB}
   * are needed for proper cleaning of the tex souce directory.
   */
  enum SuffixHandler {
    /**
     * Handler for .fig-files representing the native xfig format.
     */
    fig {
      // converts a fig-file into pdf and ptx
      // invoking {@link #runFig2Dev(File, LatexDev)}
      // TEX01, EEX01, EEX02, EEX03, WEX04, WEX05
      void procSrc(File file, LatexPreProcessor proc)
          throws BuildFailureException {

        // may throw BuildFailureException TEX01,
        // may log EEX01, EEX02, EEX03, WEX04, WEX05
        proc.runFig2Dev(file);
      }

      void clearTarget(File file, LatexPreProcessor proc) {
        // may log EFU05
        proc.clearTargetPtxPdfEps(file);
      }

      String getSuffix() {
        return LatexPreProcessor.SUFFIX_FIG;
      }
    },
    /**
     * Handler for .gp-files representing the native gnuplot format.
     */
    gp {
      // converts a gnuplot-file into pdf and ptx
      // invoking {@link #runGnuplot2Dev(File, LatexDev)}
      // TEX01, EEX01, EEX02, EEX03, WEX04, WEX05
      void procSrc(File file, LatexPreProcessor proc)
          throws BuildFailureException {
        proc.runGnuplot2Dev(file);
      }

      void clearTarget(File file, LatexPreProcessor proc) {
        // may log EFU05
        proc.clearTargetPtxPdfEps(file);// TBD: clarify 
      }

      String getSuffix() {
        return LatexPreProcessor.SUFFIX_GP;
      }
    },
    /**
     * Handler for .mp-files representing the metapost format.
     */
    mp {
      // converts a metapost-file into mps-format
      // invoking {@link #runMetapost2mps(File)}
      // TEX01, EEX01, EEX02, EEX03, WEX04, WEX05
      void procSrc(File file, LatexPreProcessor proc)
          throws BuildFailureException {
        proc.runMetapost2mps(file);
      }

      void clearTarget(File file, LatexPreProcessor proc) {
        // may log WFU01, EFU05
        proc.clearTargetMp(file);
      }

      String getSuffix() {
        return LatexPreProcessor.SUFFIX_MP;
      }
    },
    /**
     * Handler for .svg-files representing scaleable vector graphics.
     */
    svg {
      // converts an svg-file into pdf and ptx
      // invoking {@link #runFig2Dev(File, LatexDev)}
      // TEX01, EEX01, EEX02, EEX03, WEX04, WEX05
      // EFU07, EFU08, EFU09 if filtering a file fails.
      void procSrc(File file, LatexPreProcessor proc)
          throws BuildFailureException {
        proc.runSvg2Dev(file);
        // proc.log.info("Processing svg-file '" + file +
        // "' deferred to LaTeX run by need. ");
        // FIXME: this works for pdf but not for dvi:
        // even in the latter case, .pdf and .pdf_tex are created
      }

      void clearTarget(File file, LatexPreProcessor proc) {
        // may log EFU05
        proc.clearTargetPtxPdfEps(file);
      }

      String getSuffix() {
        return LatexPreProcessor.SUFFIX_SVG;
      }
    },
    /**
     * Handler for .jpg-files representing a format
     * defined by the Joint Photographic Experts Group (jp(e)g).
     */
    jpg {
      void procSrc(File file, LatexPreProcessor proc)
          throws BuildFailureException {
        proc.runEbbByConfig(file);
      }

      // void clearTarget(File file,
      // LatexPreProcessor proc,
      // Map<File, SuffixHandler> file2handler) {
      // // do not add to file2handler
      // }
      void clearTarget(File file, LatexPreProcessor proc) {
        // throw new IllegalStateException
        // ("File '" + file + "' has no targets to be cleared. ");
        proc.clearTargetJpgPng(file);
      }

      String getSuffix() {
        return LatexPreProcessor.SUFFIX_JPG;
      }
    },
    /**
     * Handler for .png-files
     * representing the Portable Network Graphics format.
     */
    png {
      void procSrc(File file, LatexPreProcessor proc)
          throws BuildFailureException {
        proc.runEbbByConfig(file);

      }

      // void clearTarget(File file,
      // LatexPreProcessor proc,
      // Map<File, SuffixHandler> file2handler) {
      // // do not add to file2handler
      // }
      void clearTarget(File file, LatexPreProcessor proc) {
        // throw new IllegalStateException
        // ("File '" + file + "' has no targets to be cleared. ");
        proc.clearTargetJpgPng(file);
      }

      String getSuffix() {
        return LatexPreProcessor.SUFFIX_PNG;
      }
    },
    /**
     * Handler for .tex-files
     * representing the TeX format, to be more precise the LaTeX format.
     */
    tex {
      void scheduleProcSrc(File file,
          Map<File, SuffixHandler> file2handler,
          LatexPreProcessor proc,
          Collection<LatexMainDesc> latexMainDescs) {
        file2handler.put(file, this);// super
        // may log WFU03, WPP02
        proc.addIfLatexMain(file, latexMainDescs);
      }

      void procSrc(File file, LatexPreProcessor proc) {
        // do nothing: no source for the latex processor 
      }

      void clearTarget(File file,
          LatexPreProcessor proc,
          Map<File, SuffixHandler> file2handler) {
        // may log WPP02, WFU01, WFU03, EFU05
        boolean isLatexMain = proc.clearTargetTexIfLatexMain(file);
        if (!isLatexMain) {
          file2handler.put(file, this);
        }
      }

      void clearTarget(File file, LatexPreProcessor proc) {
        proc.clearIfTexNoLatexMain(file);
      }

      String getSuffix() {
        return LatexPreProcessor.SUFFIX_TEX;
      }
    },
    /**
     * Handler for .bib-files
     * representing the BibTeX format for bibliographies.
     */
    bib {
      void procSrc(File file, LatexPreProcessor proc) {
        proc.log.info("Found bibliography file '" + file + "'. ");
      }

      void clearTarget(File file, LatexPreProcessor proc,
          Map<File, SuffixHandler> file2handler) {
        // do not add to file2handler
      }

      void clearTarget(File file, LatexPreProcessor proc) {
        throw new IllegalStateException
          ("File '" + file + "' has no targets to be cleared. ");
      }

      String getSuffix() {
        return LatexPreProcessor.SUFFIX_BIB;
      }
    };

    // essentially, maps file to its handler
    // overwritten for tex: in addition add to latexMainFiles
    void scheduleProcSrc(File file,
        Map<File, SuffixHandler> file2handler,
        LatexPreProcessor proc,
        Collection<LatexMainDesc> latexMainDescs) {
      file2handler.put(file, this);
    }

    // FIXME: to be updated
    // if a graphic format: process source.
    // For tex and for bib: do nothing.
    /**
     * Typically, .i.e. for {@link #fig}-, {@link #gp}-, {@link #mp}-
     * 
     * and associates <code>file</code>
     * Does the transformation of the file <code>file</code>
     * using <code>proc</code> immediately, except for
     * <ul>
     * <li>
     * {@link #svg}-files for which an info message is logged,
     * that transformation is done by need in the course of a LaTeX run.
     * What occurs are files .pdf and .pdf_tex
     * even if {@link Settings#pdfViaDvi} indicates creation of dvi files.
     * <li>
     * {@link #tex}-files which are only scheduled for later translation
     * just by adding them to <code>latexMainFiles</code>
     * if they are latex main files, and ignored otherwise
     * (see {@link LatexPreProcessor#addIfLatexMain(File, Collection)}).
     * <li>
     * {@link #bib}-files for which just an info message
     * that a bib file was found is logged.
     * </ul>
     * <p>
     * Logging:
     * <ul>
     * <li>WFU03: cannot close
     * <li>WPP02: tex file may be latex main file
     * <li>EEX01, EEX02, EEX03, WEX04, WEX05:
     * if applications for preprocessing graphic files failed.
     * <li>EFU07, EFU08, EFU09 if filtering a file fails.
     * </ul>
     *
     * @param file
     *    a file with ending given by {@link #getSuffix()}.
     * @param proc
     *     a latex pre-processor.
     * @throws BuildFailureException
     *    TEX01 only for {@link #fig}, {@link #gp} and {@link #mp}
     *    because these invoke external programs.
     */
    abstract void procSrc(File file, LatexPreProcessor proc)
        throws BuildFailureException;

    /**
     * Typically, .i.e. for {@link #fig}-, {@link #gp}-, {@link #mp}-
     * and {@link #svg}-files just associates <code>file</code>
     * with this handler in <code>file2handler</code>
     * to schedule according targets for deletion except for
     * <ul>
     * <li>
     * {@link #tex}-files for which the target is cleared immediately
     * if it is a latex main file, otherwise ignoring
     * by invoking {@link #clearTargetTexIfLatexMain(File)}.
     * <li>
     * {@link #bib}-files
     * (maybe appropriate also for jpg-files and for png-files)
     * for which there are no targets
     * and so the association is not added to <code>file2handler</code>.
     * </ul>
     * <p>
     * Logging:
     * <ul>
     * <li>WPP02: tex file may be latex main file
     * <li>WFU01: Cannot read directory...
     * <li>WFU03: cannot close tex file
     * <li>EFU05: Failed to delete file
     * <ul>
     *
     * @param file
     *    a file with ending given by {@link #getSuffix()},
     *    i.e. a file which can be handled by this handler.
     * @param proc
     *    a latex pre-processor.
     * @param file2handler
     *    maps <code>file</code> to its handler.
     *    In general, this method adds
     *    <code>file</code> to <code>file2handler</code>
     *    together with its handler which is just <code>this</code>.
     * @see #clearTarget(File, LatexPreProcessor)
     */
    // overwritten for tex, jpg, png and for bib
    // appropriate for svg although file may be removed from map later
    // used in clearCreated(File, DirNode) only
    void clearTarget(File file,
        LatexPreProcessor proc,
        Map<File, SuffixHandler> file2handler) {
      file2handler.put(file, this);
    }

    /**
     * Deletes the files potentially
     * created from the source file <code>file</code>
     * using <code>proc</code>.
     * <p>
     * Logging:
     * <ul>
     * <li>WFU01: Cannot read directory...
     * <li>EFU05: Failed to delete file
     * <ul>
     *
     * @param file
     *    a file with ending given by {@link #getSuffix()}.
     * @param proc
     *    a latex pre-processor.
     * @throws IllegalStateException
     *    <ul>
     *    <li>
     *    if <code>file</code> has no targets to be deleted
     *    as for jpg-files, png-files and bib-files.
     *    <li>
     *    if targets of <code>file</code> should have been cleared already
     *    by {@link #clearTarget(File, LatexPreProcessor, Map)}
     *    as for tex-files.
     *    </ul>
     * @see #clearTarget(File, LatexPreProcessor, Map)
     */
    // used in clearCreated(File, DirNode) only
    abstract void clearTarget(File file, LatexPreProcessor proc);

    /**
     * Returns the suffix of the file type
     * of the file type, this is the handler for.
     */
    abstract String getSuffix();
  } // enum SuffixHandler

  // FIXME: CAUTION with including pictures in xfig:
  // This is done as reference to included file.
  // Thus it breaks depencency chain.

  // The following shows the supported formats:
  // l.10 \includegraphics{02gp2pdf000}
  // %
  // I could not locate the file with any of these extensions:
  // .pdf,.PDF,.ai,.AI,.png,.PNG,.jpg,.JPG,.jpeg,.JPEG,.bmp,.BMP,.ps,.PS,.eps,.EPS,.
  // pz,.eps.Z,.ps.Z,.ps.gz,.eps.gz
  // Try typing <return> to proceed.
  // If that doesn't work, type X <return> to quit.

  // )
  // :<-
  // Package srcltx Info: Expanded filename `03someGnuplot.ptx' to
  // `03someGnuplot.pt
  // x.tex' on input line 949.

  // FIXME: allow variants:
  // - pdfhandler on .pdf,.PDF, (includable directly with pdflatex)
  // - png/jpghandler on .png,.PNG,.jpg,.JPG,.jpeg,.JPEG,
  // - maybe also for .fig

  // FIXME: questions:
  // - how to include .pdf into .dvi?
  // - how to include .eps into .pdf?
  // Question: how to transform ps into eps?
  // Research on the following:
  // .ai,.AI,.bmp,.BMP,
  // .ps,.PS,.eps,.EPS,.
  // pz,.eps.Z,.ps.Z,.ps.gz,.eps.gz

  // FIXME: decide whether suffix .ptx is replaced by .tex:
  // Advantage: because this is what it is.
  // Disadvantage: Requires mechanism
  // to determine whether tex is created or original
  // but this works the same as for pdf and for svg.

  /**
   * Converts the fig-file <code>figFile</code>
   * into a tex-file with ending ptx
   * including a pdf-file or an eps-file also created.
   * To that end, invokes {@link #runFig2DevInTex(File, LatexDev)} twice
   * to create a pdf-file and an eps-file
   * and invokes {@link #runFig2TexInclDev(File)} (once)
   * to create the tex-file.
   * <p>
   * Logging:
   * <ul>
   * <li>EEX01, EEX02, EEX03, WEX04, WEX05:
   * if running the fig2dev command failed.
   * </ul>
   *
   * @param figFile
   *    the fig file to be processed.
   * @throws BuildFailureException
   *    TEX01 if invocation of the fig2dev command
   *    returned by {@link Settings#getFig2devCommand()} failed.
   *    This is invoked twice: once for creating the pdf-file
   *    and once for creating the pdf_t-file.
   * @see #processGraphicsSelectMain(File, DirNode)
   */
  // used in fig.procSrc(File, LatexPreProcessor) only
  private void runFig2Dev(File figFile) throws BuildFailureException {
    this.log.info("Processing fig-file '" + figFile + "'. ");

    // all three
    // may throw BuildFailureException TEX01,
    // may log EEX01, EEX02, EEX03, WEX04, WEX05
    runFig2DevInTex(figFile, LatexDev.pdf);
    runFig2DevInTex(figFile, LatexDev.dvips);
    runFig2TexInclDev(figFile);
  }

  /**
   * From <code>figFile</code> create pdf/eps-file
   * containing graphics without text with special flag set.
   * The output format depends on <code>dev</code>.
   * The resulting file is included in some tex-file
   * created by {@link #runFig2TexInclDev(File)}.
   * Conversion is done by {@link Settings#getFig2devCommand()}.
   * <p>
   * Logging: FIXME:
   * EEX01, EEX02, EEX03, WEX04, WEX05
   *
   * @param figFile
   *    the fig-file to be processed
   * @param dev
   *    represents the target: either a pdf-file or an eps-file.
   * @throws BuildFailureException
   *    FIXME: TEX01,
   */
  private void runFig2DevInTex(File figFile, LatexDev dev)
      throws BuildFailureException {

    // Result file: either .pdf or .eps
    File figInTexFile =
        TexFileUtils.replaceSuffix(figFile, dev.getGraphicsInTexSuffix());
    String command = this.settings.getCommand(ConverterCategory.Fig2Dev);

    // if (update(figFile, pdfFile)) {
    String[] args = buildArgumentsFig2PdfEps(dev.getXFigInTexLanguage(),
        this.settings.getFig2devGenOptions(),
        this.settings.getFig2devPdfEpsOptions(), figFile, figInTexFile);
    //this.log.info("Running fig2dev"+java.util.Arrays.asList(args));
    this.log.debug("Running " + command + " -L pdftex/pstex  ... on '"
        + figFile.getName() + "'. ");
    // may throw BuildFailureException TEX01,
    // may log EEX01, EEX02, EEX03, WEX04, WEX05
    this.executor.execute(figFile.getParentFile(),
        this.settings.getTexPath(), // ****
        command,
        args,
        figInTexFile);
    // }
  }

  //
  // PSTEX Options:
  // -b width specify width of blank border around figure (1/72 inch)
  // Found: affects clipping path and bounding box only.
  // Not usable if latex text is used because parts no longer fit.
  // -F use correct font sizes (points instead of 1/80inch)
  // Found: no effect
  // -g color background color
  // No idea in which format color is given.
  // -n name set title part of PostScript output to name
  // Found: works. Without it is just the name of xxx.fig
  //
  // the strange thing is that this is only a subset of the postscript options
  // to be verified whether all options apply or not.

  // The EPS driver has the following differences from PostScript:
  // o No showpage is generated
  // because the output is meant to be imported
  // into another program or document and not printed
  // o The landscape/portrait options are ignored
  // o The centering option is ignored
  // o The multiple-page option is ignored
  // o The paper size option is ignored
  // o The x/y offset options are ignored

  // The EPS driver has the following two special options:
  //
  // -B 'Wx [Wy X0 Y0]'
  // This specifies that the bounding box of the EPS file
  // should have the width Wx and the height Wy.
  // Note that it doesn't scale the figure to this size,
  // it merely sets the bounding box.
  // If a value less than or equal to 0 is specified for Wx or Wy,
  // these are set to the width/height respectively of the figure.
  // Origin is relative to screen (0,0) (upper-left).
  // Wx, Wy, X0 and Y0 are interpreted
  // in centimeters or inches depending on the measure
  // given in the fig-file.
  // Remember to put either quotes (") or apostrophes (')
  // to group the arguments to -B.
  // -R 'Wx [Wy X0 Y0]'
  // Same as the -B option except that X0 and Y0
  // is relative to the lower left corner of the figure.
  // Remember to put either quotes (") or apostrophes (')
  // to group the arguments to -R.

  // The PDF driver uses all the PostScript options.

  // Explanation: many of these options do not make sense.
  // Tried: -x, -y to shift: does not work and does not make sense
  // What makes sense is
  // -a don't output user's login name (anonymous)
  // Found: login name occurs nowhere with and without -a
  // -N convert all colors to grayscale
  // Found: works

  // No information on PDFTEX options.
  // Instead:
  //
  // PDF Options:
  // -a don't output user's login name (anonymous)
  // -b width specify width of blank border around figure (1/72 inch)
  // -F use correct font sizes (points instead of 1/80inch)
  // -g color background color
  //
  // seemingly not the same, so maybe separate options required.
  // -n is pstex but not in pdf,
  // -a is pdf but not pstex... strange: is postscript

  /**
   * Returns an array of options of the form
   * <code>-L &lt;language&gt; &lt;optionsGen&gt; &lt;optionsPdfEps&gt; xxx.fig xxx.pdf/xxx.eps 
   * </code> for invocation of {@link Settings#getFig2devCommand()}
   * for creation of the pdf/eps-part of a fig-figure
   * as done in {@link #runFig2DevInTex(File, LatexDev)}.
   *
   * @param language
   *    is the output language
   *    which is either <code>pdftex</code> or <code>pstex</code>
   * @param optionsGen
   *    the general options, applying to both the pdf/eps part
   *    and the tex part of the figure under consideration.
   * @param optionsPdfEps
   *    the options, specific for the pdf/eps part (which is the same)
   *    of the figure under consideration.
   * @param figFile
   *    the fig-file to be transformed.
   * @param grpFile
   *    the graphics file (pdf/eps-file)
   *    which is the result of the transformation.
   */
  private String[] buildArgumentsFig2PdfEps(String language,
      String optionsGen,
      String optionsPdfEps,
      File figFile,
      File grpFile) {
    String[] optionsGenArr = optionsGen.isEmpty()
        ? new String[0] : optionsGen.split(" ");
    String[] optionsPdfEpsArr = optionsPdfEps.isEmpty()
        ? new String[0] : optionsPdfEps.split(" ");
    int lenSum = optionsGenArr.length + optionsPdfEpsArr.length;

    // add the four additional options
    String[] args = new String[lenSum + 4];
    // language
    args[0] = "-L";
    args[1] = language;
    // general options
    System.arraycopy(optionsGenArr, 0, args, 2, optionsGenArr.length);
    // language specific options
    System.arraycopy(optionsPdfEpsArr, 0, args, 2 + optionsGenArr.length,
        optionsPdfEpsArr.length);
    // input: fig-file
    args[2 + lenSum] = figFile.getName();
    // output: pdf/eps-file
    args[3 + lenSum] = grpFile.getName();
    return args;
  }

  /**
   * From <code>figFile</code> create tex-file
   * containing text with special flag set and
   * including a graphic file containing the rest of <code>figFile</code>.
   * Inclusion is without file extension and so both possible results
   * of {@link #runFig2DevInTex(File, LatexDev)} can be included
   * when compiling with latex.
   * Conversion is done by {@link Settings#getFig2devCommand()}.
   * <p>
   * Logging: FIXME:
   * warning EEX01, EEX02, EEX03, WEX04, WEX05
   *
   * @param figFile
   *    the fig-file to be processed
   * @throws BuildFailureException
   *    FIXME: TEX01,
   */
  private void runFig2TexInclDev(File figFile) throws BuildFailureException {

    // result file: .ptx
    File ptxFile = TexFileUtils.replaceSuffix(figFile, SUFFIX_PTX);
    String command = this.settings.getCommand(ConverterCategory.Fig2Dev);

    // if (update(figFile, pdf_tFile)) {
    String[] args = buildArgumentsFig2Ptx(this.settings.getFig2devGenOptions(),
        this.settings.getFig2devPtxOptions(),
        figFile,
        ptxFile);
    this.log.debug("Running " + command + " -L (pdf/ps)tex_t... on '"
        + figFile.getName() + "'. ");
    // may throw BuildFailureException TEX01,
    // may log EEX01, EEX02, EEX03, WEX04, WEX05
    this.executor.execute(figFile.getParentFile(),
        this.settings.getTexPath(), // ****
        command,
        args,
        ptxFile);
    // }
  }

  /**
   * The name of the language
   * used by the {@link Settings#getFig2devCommand()}
   * to specify ``special'' text without graphic of an xfig-picture.
   * Note that the languages <code>pdftex_t</code> and <code>pstex_t</code>
   * are equivalent.
   */
  private final static String XFIG_TEX_LANGUAGE = "pdftex_t";

  // Since pstex_t is equivalent with pdftex_t,
  // also the options are the same (hopefully)
  //
  // PSTEX_T Options:
  // -b width specify width of blank border around figure (1/72 inch)
  // -E num set encoding for text translation (0 no translation,
  // 1 ISO-8859-1, 2 ISO-8859-2)
  // -F don't set font family/series/shape, so you can
  // set it from latex
  // -p name name of the PostScript file to be overlaid

  /**
   * Returns an array of options of the form
   * <code>-L &lt;language&gt; &lt;optionsGen&gt; &lt;optionsPdfEps&gt; -p xxx xxx.fig xxx.ptx</code>
   * for invocation of {@link Settings#getFig2devCommand()}
   * for creation of the tex-part of a fig-figure
   * as done in {@link #runFig2TexInclDev(File)}.
   * Note that the option <code>-p xxx</code>
   * specifies the name of the pdf/eps-file
   * included in the result file <code>ptxFile</code>
   * without suffix.
   *
   * @param optionsGen
   *    the general options, applying to both the pdf/eps part
   *    and the tex part of the figure under consideration.
   * @param optionsPtx
   *    the options, specific for the tex part
   *    of the figure under consideration (for the ptx-file).
   * @param figFile
   *    the fig-file to be transformed.
   * @param ptxFile
   *    the ptx-file which is the result of the transformation.
   */
  private String[] buildArgumentsFig2Ptx(String optionsGen, String optionsPtx,
      File figFile, File ptxFile) {
    String[] optionsGenArr = optionsGen.isEmpty()
        ? new String[0] : optionsGen.split(" ");
    String[] optionsPtxArr = optionsPtx.isEmpty()
        ? new String[0] : optionsPtx.split(" ");
    int lenSum = optionsGenArr.length + optionsPtxArr.length;

    // add the six additional options
    String[] args = new String[lenSum + 6];
    // language
    args[0] = "-L";
    args[1] = XFIG_TEX_LANGUAGE;
    // general options
    System.arraycopy(optionsGenArr, 0, args, 2, optionsGenArr.length);
    // language specific options
    System.arraycopy(optionsPtxArr, 0, args, 2 + optionsGenArr.length,
        optionsPtxArr.length);
    // -p pdf/eps-file name in ptx-file without suffix
    args[2 + lenSum] = "-p";
    // full path without suffix
    args[3 + lenSum] =
        TexFileUtils.replaceSuffix(figFile, SUFFIX_VOID).getName();
    // input: fig-file
    args[4 + lenSum] = figFile.getName();
    // output: ptx-file
    args[5 + lenSum] = ptxFile.getName();
    return args;
  }

  /**
   * Deletes the files <code>xxx.ptx</code>, <code>xxx.pdf</code> and
   * <code>xxx.eps</code>
   * created from the graphic file <code>grpFile</code>
   * of the form <code>xxx.y</code>.
   * <p>
   * Logging:
   * EFU05: Failed to delete file
   *
   * @param grpFile
   *    a graphic file.
   */
  // for formats fig, gp and svg: since all of these create ptx, pdf and eps
  private void clearTargetPtxPdfEps(File grpFile) {
    this.log.info("Deleting targets of file '" + grpFile + "'. ");
    // may log EFU05
    deleteIfExists(grpFile, SUFFIX_PTX);
    deleteIfExists(grpFile, LatexDev.pdf.getGraphicsInTexSuffix());// pdf
    deleteIfExists(grpFile, LatexDev.dvips.getGraphicsInTexSuffix());// eps
  }

  /**
   * Converts a gnuplot-file into a tex-file with ending ptx
   * including a pdf-file or an eps-file also created.
   * To that end, invokes {@link #runGnuplot2Dev(File, LatexDev)} twice
   * to create a pdf-file and an eps-file
   * and to create the tex-file which can include both.
   * <p>
   * Logging:
   * <ul>
   * <li>EEX01, EEX02, EEX03, WEX04, WEX05:
   * if running the ptx/pdf-conversion built-in in gnuplot fails.
   * </ul>
   *
   * @param gpFile
   *    the gp-file (gnuplot format) to be converted to pdf and ptx.
   * @throws BuildFailureException
   *    TEX01 if invocation of the ptx/pdf-conversion built-in
   *    in gnuplot fails.
   * @see #processGraphicsSelectMain(File, DirNode)
   */
  // used in gp.procSrc(File, LatexPreProcessor) only
  private void runGnuplot2Dev(File gpFile) throws BuildFailureException {
    this.log.info("Processing gnuplot-file '" + gpFile + "'. ");
    // both may throw BuildFailureException TEX01,
    // and may log EEX01, EEX02, EEX03, WEX04, WEX05
    runGnuplot2Dev(gpFile, LatexDev.dvips);
    runGnuplot2Dev(gpFile, LatexDev.pdf);
  }

  // may throw BuildFailureException TEX01,
  // may log EEX01, EEX02, EEX03, WEX04, WEX05
  private void runGnuplot2Dev(File gpFile, LatexDev dev)
      throws BuildFailureException {

    String command = this.settings.getCommand(ConverterCategory.Gnuplot2Dev);
    File grpFile =
        TexFileUtils.replaceSuffix(gpFile, dev.getGraphicsInTexSuffix());
    File ptxFile = TexFileUtils.replaceSuffix(gpFile, SUFFIX_PTX);

    // TBD: clarify how this is quoted 
    String[] args = new String[] {"-e", // run a command string "..." with commands separated by ';'
        //
        "set terminal cairolatex " + dev.getGnuplotInTexLanguage() + " "
            + this.settings.getGnuplotOptions() + ";set output '"
            + ptxFile.getName() + "';load '" + gpFile.getName() + "'"};

    // if (update(gpFile, ptxFile)) {
    this.log.debug(
        "Running " + command + " -e...  on '" + gpFile.getName() + "'. ");
    // may throw BuildFailureException TEX01,
    // may log EEX01, EEX02, EEX03, WEX04, WEX05
    this.executor.execute(gpFile.getParentFile(), // workingDir
        this.settings.getTexPath(), // ****
        command, args, grpFile, ptxFile);
    // }
    // no check: just warning that no output has been created.
  }

  /**
   * Runs mpost on mp-files to generate mps-files.
   * <p>
   * Logging:
   * <ul>
   * <li>WFU03: cannot close log file
   * <li>EAP01: Running <code>command</code> failed. For details...
   * <li>EAP02: Running <code>command</code> failed. No log file
   * <li>WAP04: if log file is not readable.
   * <li>EEX01, EEX02, EEX03, WEX04, WEX05:
   * if running the mpost command failed.
   * </ul>
   *
   * @param mpFile
   *    the metapost file to be processed.
   * @throws BuildFailureException
   *    TEX01 if invocation of the mpost command failed.
   * @see #processGraphicsSelectMain(File, DirNode)
   */
  // used in mp.procSrc(File, LatexPreProcessor) only
  private void runMetapost2mps(File mpFile) throws BuildFailureException {
    this.log.info("Processing metapost-file '" + mpFile + "'. ");
    String command = this.settings.getCommand(ConverterCategory.MetaPost);
    File workingDir = mpFile.getParentFile();
    // for more information just type mpost --help
    String[] args = buildArguments(this.settings.getMetapostOptions(), mpFile);
    this.log.debug("Running " + command + " on '" + mpFile.getName() + "'. ");
    // FIXME: not check on all created files,
    // but this is not worse than with latex

    // may throw BuildFailureException TEX01,
    // may log EEX01, EEX02, EEX03, WEX04, WEX05
    this.executor.execute(workingDir,
        this.settings.getTexPath(), // ****
        command,
        args,
        TexFileUtils.replaceSuffix(mpFile, SUFFIX_MPS));

    // from xxx.mp creates xxx.mps, xxx.log and for tex labels xxx.mpx
    // with -recorder option in addition xxx.fls 
    // FIXME: monitoring of those additional files. 
    // keep trying to be in one line with the latex processors 
    File logFile = TexFileUtils.replaceSuffix(mpFile, SUFFIX_LOG);
    // may log WFU03, EAP01, EAP02, WAP04
    logErrs (logFile, command, this.settings.getPatternErrMPost());
    // may log warnings WFU03, WAP03, WAP04
    logWarns(logFile, command, this.settings.getPatternWarnMPost());
  }

  /**
   * Deletes the graphic files
   * created from the metapost-file <code>mpFile</code>.
   * <p>
   * Logging:
   * <ul>
   * <li>WFU01: Cannot read directory ...
   * <li>EFU05: Failed to delete file
   * </ul>
   *
   * @param mpFile
   *    a metapost file.
   */
  private void clearTargetMp(File mpFile) {
    this.log.info("Deleting targets of graphic-file '" + mpFile + "'. ");
    // may log EFU05
    deleteIfExists(mpFile, SUFFIX_LOG);
    deleteIfExists(mpFile, SUFFIX_FLS);
    deleteIfExists(mpFile, SUFFIX_MPX);
    deleteIfExists(mpFile, SUFFIX_MPS);
    // // delete files xxxNumber.mps
    // String name1 = mpFile.getName();
    // final String root = name1.substring(0, name1.lastIndexOf("."));
    // FileFilter filter = new FileFilter() {
    // 	public boolean accept(File file) {
    // 		return !file.isDirectory()
    // 				&&  file.getName().matches(root + SUFFIX_MPS);
    // 	}
    // };
    // // may log WFU01, EFU05
    // this.fileUtils.deleteX(mpFile, filter, false);
  }

  /**
   * Converts an svg-file into a tex-file with ending ptx
   * including a pdf-file or an eps-file also created.
   * To that end, invokes {@link #runSvg2Dev(File, LatexDev, boolean)} twice
   * to create a pdf-file and an eps-file
   * and to create the tex-file which can include both.
   * <p>
   * Logging:
   * <ul>
   * <li>EEX01, EEX02, EEX03, WEX04, WEX05:
   * if running the ptx/pdf-conversion built-in in svg2dev fails.
   * <li>EFU07, EFU08, EFU09 if filtering a file fails.
   * </ul>
   *
   * @param svgFile
   *    the svg-file to be converted to a pdf-file and a ptx-file.
   * @throws BuildFailureException
   *    TEX01 if invocation of the ptx/pdf-conversion built-in
   *    in svg2dev fails.
   * @see #processGraphicsSelectMain(File, DirNode)
   */
  // used in svg.procSrc(File, LatexPreProcessor) only
  private void runSvg2Dev(File svgFile) throws BuildFailureException {
    this.log.info("Processing svg-file '" + svgFile + "'. ");
    // both may throw BuildFailureException TEX01,
    // and may log EEX01, EEX02, EEX03, WEX04, WEX05
    // EFU07, EFU08, EFU09
    runSvg2Dev(svgFile, LatexDev.pdf, false);
    runSvg2Dev(svgFile, LatexDev.dvips, true);
  }

  // FIXME: still the included pdf/eps-file does not occur
  // with full path in ptx-file
  // may throw BuildFailureException TEX01,
  // may log EEX01, EEX02, EEX03, WEX04, WEX05, EFU07, EFU08, EFU09
  private void runSvg2Dev(File svgFile, LatexDev dev, boolean filterTex)
      throws BuildFailureException {
    // current:
    // inkscape --export-filename=F4_07someSvg -D F4_07someSvg.svg
    // --export-type=pdf,eps
    // inkscape --export-filename=F4_07someSvg.pdf -D F4_07someSvg.svg
    //
    // --export-pdf-version=1.4 may be nice
    String command = this.settings.getCommand(ConverterCategory.Svg2Dev);

    File grpFile =
        TexFileUtils.replaceSuffix(svgFile, dev.getGraphicsInTexSuffix());
    File texFile =
        TexFileUtils.replaceSuffix(svgFile, dev.getInkscapeTexSuffix());

    String[] args = buildArgumentsInkscp(grpFile,
        this.settings.getSvg2devOptions(), svgFile);
    this.log.debug("Running " + command + " on '" + svgFile.getName() + "'. ");
    // may throw BuildFailureException TEX01,
    // may log EEX01, EEX02, EEX03, WEX04, WEX05
    this.executor.execute(svgFile.getParentFile(),
        this.settings.getTexPath(), // ****
        command,
        args,
        grpFile,
        texFile);

    if (filterTex) {
      // may log EFU07, EFU08, EFU09: cannot fiter
      // for eps only 
      String suffix = TexFileUtils.getSuffix(texFile);
      assert SUFFIX_EPSTEX.equals(suffix) : "Expected suffix '" + SUFFIX_EPSTEX
          + "' found '" + suffix + "'";
      File destFile = TexFileUtils.replaceSuffix(texFile, SUFFIX_PTX);
      File bareFile = TexFileUtils.replaceSuffix(texFile, SUFFIX_VOID);

      this.fileUtils.filterInkscapeIncludeFile(texFile, destFile,
          bareFile.getName(), SUFFIX_EPS);
    }
    this.fileUtils.deleteOrError(texFile, false);
  }

  protected static String[] buildArgumentsInkscp(File grpFile,
      String options,
      File file) {
    String optExp = "--export-filename=" + grpFile.getName();
    if (options.isEmpty()) {
      return new String[] {optExp, file.getName()};
    }
    String[] optionsArr = options.split(" ");
    String[] args = new String[optionsArr.length + 2];
    System.arraycopy(optionsArr, 0, args, 1, optionsArr.length);
    args[args.length - 1] = file.getName();
    args[0] = optExp;
    return args;
  }

  // Additional research:
  // Documentation says, that this is needed for interface eps,
  // but not for interface pdf.
  // Experiments show, that we can do without it in any case.

  private void runEbbByConfig(File file) throws BuildFailureException {
    if (!this.settings.getCreateBoundingBoxes()) {
      // suffix without dot 
      String suffix = TexFileUtils.getSuffix(file).substring(1);
      this.log.info(
          suffix.toUpperCase() + "-file '" + file + "' needs no processing. ");
      // FIXME: this works for pdf but not for dvi:
      // in the latter case:
      // ! LaTeX Error: Cannot determine size of graphic ...
      // FIXME: only for dvi
      return;
    }

    String command = this.settings.getCommand(ConverterCategory.EbbCmd);
    File workingDir = file.getParentFile();
    String[] args = buildNullArguments(this.settings.getEbbOptions(), file);

    // Creation of .xbb files for driver dvipdfmx
    // FIXME: literal
    args[0] = "-x";
    File resFile = TexFileUtils.replaceSuffix(file, SUFFIX_XBB);

    this.log
        .debug("Running " + command + " twice on '" + file.getName() + "'. ");
    // may throw BuildFailureException TEX01,
    // may log EEX01, EEX02, EEX03, WEX04, WEX05
    this.executor.execute(workingDir, this.settings.getTexPath(), //****
        command, args, resFile);

    // Creation of .bb files for driver dvipdfm
    // FIXME: literal
    args[0] = "-m";
    resFile = TexFileUtils.replaceSuffix(file, SUFFIX_BB);

    this.executor.execute(workingDir, this.settings.getTexPath(), //****
        command, args, resFile);
  }

  /**
   * Returns an array of strings,
   * where the 0th entry is <code>null</code>
   * and a placeholder for option <code>-x</code> or <code>-m</code>
   * when used by {@link #runEbbByConfig(File)}
   * and for the export option
   * when used by {@link #runSvg2Dev(File, LatexDev, boolean)}
   * then follow the options from <code>options</code>
   * and finally comes the name of <code>file</code>.
   */
  protected static String[] buildNullArguments(String options, File file) {
    if (options.isEmpty()) {
      return new String[] {null, file.getName()};
    }
    String[] optionsArr = options.split(" ");
    String[] args = new String[optionsArr.length + 2];
    System.arraycopy(optionsArr, 0, args, 1, optionsArr.length);
    args[args.length - 1] = file.getName();

    assert args[0] == null;
    return args;
  }

  /**
   * Deletes the graphic files
   * created from the svg-file <code>svgFile</code>.
   * <p>
   * Logging:
   * EFU05: Failed to delete file
   */
  private void clearTargetJpgPng(File file) {
    this.log
        .info("Deleting bounding boxes of file '" + file + "' if present. ");
    // may log EFU05
    deleteIfExists(file, SUFFIX_XBB);
    deleteIfExists(file, SUFFIX_BB);
    // deleteIfExists(svgFile, SUFFIX_PSTEX );
    // deleteIfExists(file, SUFFIX_PDF );
    // FIXME: this works for pdf but not for dvi:
    // even in the latter case, .pdf and .pdf_tex are created
  }

  /**
   *
   * <p>
   * Logging:
   * EFU05: Failed to delete file
   */
  private void deleteIfExists(File file, String suffix) {
    File delFile = TexFileUtils.replaceSuffix(file, suffix);
    if (!delFile.exists()) {
      return;
    }
    // may log EFU05
    this.fileUtils.deleteOrError(delFile, false);
    return;
  }

  /**
   * Returns an optional covering a <code>LatexMainDesc</code> 
  * if and only if <code>texFile</code> is recognized 
  * as latex main file which includes that it is readable.
   * If it is not readable, this method logs a warning 
  * and returns an empty optional.
   * <p>
   * Logging:
   * <ul>
   * <li>WFU03: cannot close
   * <li>WPP02: tex file may be latex main file
   * <ul>
   *
   * @param texFile
   *    the tex-file to decide on whether it is a latex main file. 
   * @return
  *    An {@link Optional} containing a {@link LatexMainDesc} if and only if 
  *    <code>texFile</code> is definitively a latex main file 
  *    in particular it is readable. 
  *    This can be asked by {@link Optional#isPresent()}. 
   */
  // used
  // by addIfLatexMain(File, Collection) and
  // by clearTargetTexIfLatexMain(File)
  private Optional<LatexMainDesc> optLatexMainFile(File texFile) {
    assert texFile.exists() && !texFile.isDirectory()
        : "Expected existing regular tex file " + texFile;
    // may log WFU03 cannot close
    FileMatch fileMatch = this.fileUtils.getMatchInFile(texFile,
        this.settings.getPatternLatexMainFile());
    if (!fileMatch.isFileReadable()) {
      this.log.warn("WPP02: Cannot read tex file '" + texFile
          + "'; may bear latex main file. ");
      return Optional.empty();
    }
    // System.out.println("readable and match: "+fileMatch.doesExprMatch()+" "+texFile);
    // if (!fileMatch.doesExprMatch()) {
    //   this.log.error("EPPXX: Pattern for detecting latex main file does not match,  '"
    //   + texFile + "' please correct; for the moment assume no latex main file. ");
    //   return Optional.empty();
    // }

    // return LatexMainDesc.getLatexMain(texFile, fileMatch.getMatcher());
    // // return fileMatch.doesExprMatch()
    // //   ? LatexMainDesc.getLatexMain(texFile, fileMatch.getMatcher())
    // //   : Optional.empty();
   return fileMatch.doesExprMatch()
      ? Optional.of(new LatexMainDesc(texFile, fileMatch.getMatcher()))
      : Optional.empty();
  }

  /**
   * If the tex-file <code>texFile</code> is a latex main file,
   * add the according description to <code>latexMainFiles</code>.
   * <p>
   * Logging:
   * <ul>
   * <li>WFU03: cannot close
   * <li>WPP02: tex file may be latex main file
   * <ul>
   *
   * @param texFile
   *    the tex-file the description of which 
  *    is to be added to <code>latexMainDescs</code>
   *    if <code>texFile</code> is a latex main file.
   * @param latexMainDescs
   *    the collection of descriptions of latex main files found so far.
   */
  // invoked only by tex.procSrc(File, LatexPreProcessor)
  private void addIfLatexMain(File texFile,
      Collection<LatexMainDesc> latexMainDescs) {
    // may log WFU03, WPP02
    Optional<LatexMainDesc> optTexFileDesc = optLatexMainFile(texFile);
    //System.out.println("is main file: "+optTexFileDesc.isPresent()+" "+texFile);
    if (optTexFileDesc.isPresent()) {
      LatexMainDesc desc = optTexFileDesc.get();
      String docClass = desc.getDocClass();
      this.log.info("Detected " + docClass + "-file '" + texFile + "'. ");
      latexMainDescs.add(desc);
    }
  }

  /**
   * Deletes the files
   * created from the tex-file <code>texFile</code>,
   * if that is a latex main file.
   * <p>
   * Logging:
   * <ul>
   * <li>WPP02: tex file may be latex main file
   * <li>WFU01: Cannot read directory...
   * <li>WFU03: cannot close tex file
   * <li>EFU05: Failed to delete file
   * </ul>
   *
   * @param texFile
   *    the tex-file of which the created files shall be deleted
   *    if it is a latex main file.
  * @return
  *    whether <code>texFile</code> is a latex main file. 
   */
  private boolean clearTargetTexIfLatexMain(File texFile) {
    // exclude files which are no latex main files
    // may log WFU03, WPP02
    if (optLatexMainFile(texFile).isEmpty()) {
      return false;
    }
    this.log.info("Deleting targets of latex main file '" + texFile + "'. ");
    boolean allowsDir = true;
    FileFilter filter = TexFileUtils.getFileFilter(texFile,
        this.settings.getPatternCreatedFromLatexMain(), allowsDir);
    // may log WFU01, EFU05
    this.fileUtils.deleteX(texFile, filter, true);
    return true;
  }

  private void clearIfTexNoLatexMain(File texFile) {
    this.log.info("Deleting aux file of tex file '" + texFile
        + "' if present (included). ");
    deleteIfExists(texFile, LatexProcessor.SUFFIX_AUX);
  }

  /**
   * Detects files in the directory represented by <code>node</code>
   * and in subdirectories recursively:
   * <ul>
   * <li>
   * those which are in various graphic formats incompatible with LaTeX
   * are converted into formats which can be inputed or included directly
   * into a latex file.
   * <li>
   * returns the set of descriptions of latex main files.
   * </ul>
   * <p>
   * Logging:
   * <ul>
   * <li>WFU03: cannot close
   * <li>WPP02: tex file may be latex main file
   * <li>WPP03: Skipped processing of files with suffixes ...
   * <li>WPP05: Included tex files which are no latex main files 
   * <li>WPP06: Included tex files which are no latex main files 
   * <li>WPP07: inluded/excluded files not identified by their names.
   * <li>EEX01, EEX02, EEX03, WEX04, WEX05:
   * if running graphic processors failed.
   * <li>EFU07, EFU08, EFU079: if filtering a file fails.
   * </ul>
   *
   * @param dir
   *    represents the tex source directory or a subdirectory.
   * @param node
   *    a node associated with <code>dir</code>.
   * @return
   *    the collection of descriptions of latex main files.
   * @throws BuildFailureException
   *    TEX01 invoking
   * {@link #processGraphicsSelectMain(File, DirNode, Collection, Collection)}
   */
  // used in LatexProcessor.create()
  // and in LatexProcessor.processGraphics() only
  // where 'node' represents the tex source directory
  Collection<LatexMainDesc> processGraphicsSelectMain(File dir, DirNode node)
      throws BuildFailureException {

    Collection<String> skippedSuffixes = new TreeSet<String>();
    Collection<LatexMainDesc> latexMainDescs = new TreeSet<LatexMainDesc>();
    if (this.settings.getReadTexSrcProcDirRec()) {
      // may throw BuildFailureException TEX01,
      // may log EEX01, EEX02, EEX03,
      // WEX04, WEX05, WFU03, WPP02, EFU06
      processGraphicsSelectMainRec(dir, node, skippedSuffixes, latexMainDescs);
    } else {
      // may throw BuildFailureException TEX01,
      // may log EEX01, EEX02, EEX03,
      // WEX04, WEX05, WFU03, WPP02, EFU07, EFU08, EFU09
      processGraphicsSelectMain(dir, node, skippedSuffixes, latexMainDescs);
    }

    if (!skippedSuffixes.isEmpty()) {
      this.log.warn("WPP03: Skipped processing of files with suffixes "
          + skippedSuffixes + ". ");
    }

    Map<String, LatexMainDesc> name2desc = new HashMap<String, LatexMainDesc>();
    LatexMainDesc oldValue;
    Set<String> keysOverwritten = new HashSet<String>();
    String key;
    for (LatexMainDesc mainDesc : latexMainDescs) {
      key = mainDesc.xxxFile.getName();
      oldValue = name2desc.put(key, mainDesc);
      if (oldValue != null) {
        keysOverwritten.add(key);
      }
    }

    // original latexMainFiles, not modified by inclusion and exclusion 
    Set<String> keySetOrg = new HashSet<String>(name2desc.keySet());

    Set<String> mainFilesIncluded = this.settings.getMainFilesIncluded();
    if (!mainFilesIncluded.isEmpty()) {
      // check that mainFilesIncluded is a subset of name2file.keySet()
      if (!keySetOrg.containsAll(mainFilesIncluded)) {
        Set<String> includedNotMainFiles =
            new HashSet<String>(mainFilesIncluded);
        includedNotMainFiles.removeAll(keySetOrg);
        assert !includedNotMainFiles.isEmpty();
        this.log
            .warn("WPP05: Included latex files which are not latex main files: "
                + includedNotMainFiles + ". ");
      }
      name2desc.keySet().retainAll(mainFilesIncluded);
    }


    Set<String> mainFilesExcluded = this.settings.getMainFilesExcluded();
    // check that mainFilesIncluded is a subset of name2file.keySet()
    if (!keySetOrg.containsAll(mainFilesExcluded)) {
      Set<String> excludedNotMainFiles = new HashSet<String>(mainFilesExcluded);
      excludedNotMainFiles.removeAll(keySetOrg);
      assert !excludedNotMainFiles.isEmpty();
      this.log.warn("WPP06: Excluded latex files "
      + "which are not latex main files: " + excludedNotMainFiles + ". ");
    }
    name2desc.keySet().removeAll(mainFilesExcluded);

    if (!(mainFilesIncluded.isEmpty() && mainFilesExcluded.isEmpty())) {
      Set<String> inclExcl = new HashSet<String>(mainFilesIncluded);
      inclExcl.addAll(mainFilesExcluded);
      inclExcl.retainAll(keysOverwritten);
      if (!inclExcl.isEmpty()) {
        this.log.warn("WPP07: Included/Excluded latex main files "
        + "not identified by their name: " + inclExcl + ". ");
      }
      this.log.info("After inclusion/exclusion latex main files are "
          + name2desc.keySet() + ". ");
    }

    return name2desc.values();
  }

  /**
   * <p>
   * Logging:
   * <ul>
   * <li>WFU03: cannot close file
   * <li>EFU07, EFU08, EFU09: if filtering of a file fails (svg)
   * <li>WPP02: tex file may be latex main file
   * <li>EEX01, EEX02, EEX03, WEX04, WEX05:
   * if applications for preprocessing graphic files failed.
   * </ul>
   *
   * @param dir
   *    represents the tex source directory or a subdirectory.
   * @param node
   *    a node associated with <code>dir</code>.
   * @param skippedSuffixes
   *    the collection of suffixes of files with handling skipped so far
   *    because there is no handler.
   *    FIXME: interesting for files without suffix or for hidden files.
   * @param latexMainDescs
   *    the collection of descriptions of latex main files found so far.
   * @throws BuildFailureException
   *    TEX01 invoking
   *    {@link LatexPreProcessor.SuffixHandler#procSrc(File, LatexPreProcessor)}
   *    only for {@link LatexPreProcessor.SuffixHandler#fig},
   *    {@link LatexPreProcessor.SuffixHandler#gp} and
   *    {@link LatexPreProcessor.SuffixHandler#mp}
   *    because these invoke external programs.
   */
  private void processGraphicsSelectMain(File dir, DirNode node,
      Collection<String> skippedSuffixes,
      Collection<LatexMainDesc> latexMainDescs) throws BuildFailureException {

    assert node.isValid();// i.e. node.regularFile != null
    // FIXME: processing of the various graphic files
    // may lead to overwrite
    // FIXME: processing of the latex main files
    // may lead to overwrite of graphic files or their targets

    File file;
    String suffix;
    SuffixHandler handler;
    Collection<LatexMainDesc> latexMainDescsLocal =
        new TreeSet<LatexMainDesc>();
    Map<File, SuffixHandler> file2handler = new TreeMap<File, SuffixHandler>();
    for (String fileName : node.getRegularFileNames()) {
      file = new File(dir, fileName);
      if (file.isHidden()) {
        this.log.debug("Skipping hidden file '" + file + "'. ");
        continue;
      }
      // TBD: treat discrepancy between .filename in unix (hidden) and windows... normal 
      // Maybe getSuffix is empty, if the rest of the name would be empty. 
      suffix = TexFileUtils.getSuffix(file);
      handler = SUFFIX2HANDLER.get(suffix);
      if (handler == null) {
        this.log.debug("Skipping file '" + file + "' without handler. ");
        // warning on skipped files if not hidden.
        skippedSuffixes.add(suffix);
        continue;
      }
      // Either performs transformation now
      // or schedule for later (latex main files)
      // or do nothing if no targets like bib-files
      // or tex-files to be inputted.

      // may throw BuildFailureException TEX01,
      // may log EEX01, EEX02, EEX03, WEX04, WEX05
      // WFU03, WPP02
      handler.scheduleProcSrc(file, file2handler, this, latexMainDescsLocal);
    } // for

    latexMainDescs.addAll(latexMainDescsLocal);

    // From now on it is about processing graphic files 
    // and to message bib-files (for bibliography and for glossary)
    // The latter is required only for create 

    // remove sources from file2handler.keySet()
    // if created by local latex main files
    FileFilter filter;
    for (LatexMainDesc lmDesc : latexMainDescsLocal) {
      File lmFile = lmDesc.texFile;
      filter = TexFileUtils.getFileFilter(lmFile,
          this.settings.getPatternCreatedFromLatexMain(), false);
      Iterator<File> iter = file2handler.keySet().iterator();
      File src;
      while (iter.hasNext()) {
        src = iter.next();
        if (filter.accept(src)) {
          // FIXME: maybe this is too much:
          // better just one warning per latex main file
          // or just suffixes, i.e. handlers
          this.log.warn("WPP04: Skip processing '" + src
              + "': interpreted as target of '" + lmFile + "'. ");
          iter.remove();
          continue;
        }
        // Here, src is not overwritten processing lmFile
        // FIXME: to be checked, whether this is also true
        // for targets of src
      }
    }

    // Here process file, except tex (bib at least info)
    // with associated handler
    // FIXME: How to ensure, that nothing is overwritten?
    // NO: if a file is overwritten, then it is no source
    // and needs no processing
    for (Map.Entry<File, SuffixHandler> entry : file2handler.entrySet()) {
      // procSrc may throw BuildFailureException TEX01
      // and may log WFU03, WPP02,
      // EEX01, EEX02, EEX03, WEX04, WEX05 and EFU07, EFU08, EFU09
      entry.getValue().procSrc(entry.getKey(), this);
    }
  }

  /**
   * Like
   * {@link #processGraphicsSelectMain(File,DirNode,Collection,Collection)}
   * but with recursion to subdirectories.
   */
  private void processGraphicsSelectMainRec(File dir, DirNode node,
      Collection<String> skipped, Collection<LatexMainDesc> latexMainDescs)
      throws BuildFailureException {
    processGraphicsSelectMain(dir, node, skipped, latexMainDescs);

    // go on recursively with subdirectories
    for (Map.Entry<String, DirNode> entry : node.getSubdirs().entrySet()) {
      // may throw BuildFailureException TEX01,
      // may log EEX01, EEX02, EEX03, WEX04, WEX05, WPP03
      // WFU03, WPP02, EFU06
      processGraphicsSelectMainRec(new File(dir, entry.getKey()),
          entry.getValue(), skipped, latexMainDescs);
    }
  }

  /**
   * Deletes all created files
   * in the directory represented by <code>texDir</code>
   * tracing subdirectories recursively.
   * For details of deletions within a single directory
   * see {@link #clearCreated(File, DirNode)}.
   * <p>
   * Logging:
   * <ul>
   * <li>WPP02: tex file may be latex main file
   * <li>WFU01: Cannot read directory...
   * <li>WFU03: cannot close tex file
   * <li>EFU05: Failed to delete file
   * </ul>
   *
   * @param texDir
   *    represents the tex source directory or a subdirectory.
   */
  // invoked in LatexProcessor.clearAll() only
  void clearCreated(File texDir) {
    clearCreated(texDir, new DirNode(texDir, this.fileUtils));
  }

  /**
   * Deletes all created files
   * in the directory represented by <code>node</code>, recursively.
   * In each directory, the sub-directories are not deleted themselves
   * but cleaned recursively, depth first.
   * The other files are cleaned, i.e.
   * their targets are deleted in an ordering reverse to creation
   * proceeding in the following steps:
   * <ul>
   * <li>
   * First the targets of the latex main files are deleted,
   * whereas the targets of the graphic (source) files
   * are just scheduled for deletion.
   * For details see
   * {@link LatexPreProcessor.SuffixHandler#clearTarget(File, LatexPreProcessor, Map)}.
   * FIXME: what about deletion of a graphic source file in this course?
   * <li>
   * Then the graphic source files scheduled are un-scheduled
   * if deleted by some latex main file.
   * <li>
   * Finally, the targets of the graphic souce files are deleted.
   * FIXME: what if this results in deletion of a graphic source file?
   * </ul>
   * Then the files with handler
   * If a file has a prefix without handler,
   * (see {@link SuffixHandler#getSuffix()}) it is ignored.
   * Else its target is cleared as described in
   * {@link SuffixHandler#clearTarget(File, LatexPreProcessor, Map)}.
   * <p>
   * Logging:
   * <ul>
   * <li>WPP02: tex file may be latex main file
   * <li>WFU01: Cannot read directory...
   * <li>WFU03: cannot close tex file
   * <li>EFU05: Failed to delete file
   * </ul>
   *
   * @param dir
   *    represents the tex source directory or a subdirectory.
   * @param node
   *    a node associated with <code>dir</code>.
   */
  private void clearCreated(File dir, DirNode node) {
    assert dir.isDirectory() : "Expected existing directory " + dir
        + " to be cleared. ";
    // Clearing depth first is mandatory; else the above assertion is invalid 

    // clear recursively depth first 
    for (Map.Entry<String, DirNode> entry : node.getSubdirs().entrySet()) {
      // may log WPP02, WFU01, WFU03, EFU05
      clearCreated(new File(dir, entry.getKey()), entry.getValue());
    }

    File file;
    SuffixHandler handler;
    Map<File, SuffixHandler> file2handler = new TreeMap<File, SuffixHandler>();
    for (String fileName : node.getRegularFileNames()) {
      file = new File(dir, fileName);
      handler = SUFFIX2HANDLER.get(TexFileUtils.getSuffix(file));
      if (handler != null) {
        // either clear targets now or schedule for clearing
        // (in particular do nothing if no target)
        // may log WPP02, WFU01, WFU03, EFU05
        handler.clearTarget(file, this, file2handler);
      }
    }
    // clear targets of all still existing files
    // which just scheduled for clearing
    for (Map.Entry<File, SuffixHandler> entry : file2handler.entrySet()) {
      file = entry.getKey();
      if (file.exists()) {
        entry.getValue().clearTarget(file, this);
      }
    }
  }

  // FIXME: suffix for tex files containing text and including pdf
}
