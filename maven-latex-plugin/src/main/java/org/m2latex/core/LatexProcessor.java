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

package org.m2latex.core;

import java.io.File;
import java.io.FileFilter;

import java.util.Collection;
import java.util.Arrays;

// idea: use latex2rtf and unoconv
// idea: targets for latex2html, latex2man, latex2png and many more. 
/**
 * The latex processor creates various output from latex sources 
 * including also preprocessing of graphic files in several formats. 
 * This is the core class of this piece of software. 
 * The main method is {@link #create()} which is executed by the ant task 
 * and by the maven plugin. 
 * It does preprocessing of the graphic files 
 * and main processing of the latex file according to the target(s) 
 * given by the parameters. 
 */
public class LatexProcessor {

    // For pattern matching, it is vital to avoid false positive matches. 
    // e.g. Overfull \box with text '! ' is not an error 
    // and Overfull \box with text 'Warning' is no warning. 

    static final String PATTERN_NEED_BIBTEX_RUN = "^\\\\bibdata";

    // Note that two \\ represent a single \ in the string. 
    // Thus \\\\ represents '\\' in the pattern, 
    // which in turn represents a single \. 
    static final String PATTERN_OUFULL_HVBOX = 
	"^(Ov|Und)erfull \\\\[hv]box \\(";


    // LaTeX 
    final static String SUFFIX_TOC = ".toc";
    final static String SUFFIX_LOF = ".lof";
    final static String SUFFIX_LOT = ".lot";
    final static String SUFFIX_AUX = ".aux";


    // home-brewed ending to represent tex including postscript 
    private final static String SUFFIX_PTX = ".ptx";
    final static String SUFFIX_PDF = ".pdf";
    final static String SUFFIX_PSTEX = ".pstex";
    final static String SUFFIX_PDFTEX = ".pdf_tex";

    // for LaTeX but also for mpost 
    final static String SUFFIX_LOG = ".log";
 
    // suffix for xfig
    private final static String SUFFIX_FIG = ".fig";
    // suffix for svg
    private final static String SUFFIX_SVG = ".svg";
    // suffix for gnuplot
    private final static String SUFFIX_PLT = ".plt";
    // suffix for metapost
    private final static String SUFFIX_MP  = ".mp";
    // from xxx.mp creates xxx1.mps, xxx.log and xxx.mpx 
    private final static String SUFFIX_MPS = ".mps";
    private final static String SUFFIX_MPX = ".mpx";

    // odt2doc 
    private final static String SUFFIX_ODT = ".odt";

    // makeindex for index 
    // unsorted and not unified index created by latex 
    final static String SUFFIX_IDX = ".idx";
    // log file created by makeindex 
    final static String SUFFIX_ILG = ".ilg";

    // makeindex for glossary 
    // unsorted and not unified glossary created by latex 
    final static String SUFFIX_GLO = ".glo";
    // index style, better glossary style created by latex for makeindex. 
    final static String SUFFIX_IST = ".ist";
     // index style, better glossary style created by latex for xindy. 
    final static String SUFFIX_XDY = ".xdy";
    // sorted and unified glossary created by makeindex 
    final static String SUFFIX_GLS = ".gls";
    // logging file for makeindex used with glossaries 
    final static String SUFFIX_GLG = ".glg";

    // bibtex 
    final static String SUFFIX_BLG = ".blg";

    // needed by makeglossaries 
    final static String SUFFIX_VOID = "";


    private final Settings settings;

    private final CommandExecutor executor;

    private final LogWrapper log;

    private final TexFileUtils fileUtils;

    private final ParameterAdapter paramAdapt;

    // also for tests 
    LatexProcessor(Settings settings, 
		   CommandExecutor executor, 
		   LogWrapper log, 
		   TexFileUtils fileUtils,
		   ParameterAdapter paramAdapt) {
        this.settings = settings;
        this.log = log;
        this.executor = executor;
        this.fileUtils = fileUtils;
	this.paramAdapt = paramAdapt;
    }

    /**
     * Creates a LatexProcessor with parameters given by <code>settings</code> 
     * which logs onto <code>log</code> and used by <code>paramAdapt</code>. 
     *
     * @param settings
     *    the settings controlling latex processing 
     * @param log
     *    the logger to write on events while processing 
     * @param paramAdapt
     *    the parameter adapter, refers to maven-plugin or ant-task. 
     */
    public LatexProcessor(Settings settings, 
			  LogWrapper log, 
			  ParameterAdapter paramAdapt) {
	this(settings, new CommandExecutorImpl(log), log, 
	     new TexFileUtilsImpl(log), paramAdapt);
    }

    /**
     * Defines creational ant-task and the maven plugin. 
     * This consists in reading the parameters 
     * via {@link ParameterAdapter#initialize()} 
     * processing graphic-files via {@link #processGraphics(File)} 
     * and processing the tex main files 
     * via {@link Target#processSource(LatexProcessor, File)}. 
     * The resulting files are identified by its suffixes 
     * via  {@link Target#getOutputFileSuffixes()} 
     * and copied to the target folder. 
     * Finally, by default a cleanup is performed 
     * invoking {@link TexFileUtils#cleanUp(Collection, File)}. 
     * <p>
     * Logging: 
     * <ul>
     * <li>
     * If the tex directory does not exist. 
     * <li>
     * FIXME: logging of invoked methods. 
     * </ul>
     *
     * FIXME: exceptions not really clear. 
     * @throws BuildExecutionException 
     *    <ul>
     *    <li>
     *    if copying the latex-directory to the temporary directory 
     *    or copying the results from the temporary directory 
     *    into the target directory fails. 
     *    <li>
     *    if processing xfig-files or gnuplot files or latex-files fail. 
     *    <li>
     *    if latex main documents could not be identified. 
     *    <li>
     *    if processing xfig-files or gnuplot files fail. 
     *    </ul>
     */
    public void create() throws BuildExecutionException, BuildFailureException {

        this.paramAdapt.initialize();
        this.log.debug("Settings: " + this.settings.toString() );

        File texDirectory = this.settings.getTexSrcDirectoryFile();

        if (!texDirectory.exists()) {
            this.log.info("No tex directory - skipping LaTeX processing. ");
            return;
        }

	// may throw BuildExecutionException 
	Collection<File> orgFiles = this.fileUtils.getFilesRec(texDirectory);

	try {
	    processGraphics(texDirectory);

	    // process latex main files 
	    // may throw BuildExecutionException 
	    Collection<File> latexMainFiles = this.fileUtils
		.getLatexMainDocuments(texDirectory, 
				       this.settings.getPatternLatexMainFile());
	    for (File texFile : latexMainFiles) {
		// may throw BuildExecutionException, BuildFailureException 
		File targetDir = this.fileUtils.getTargetDirectory
		    (texFile, 
		     texDirectory,
		     this.settings.getOutputDirectoryFile());

		for (Target target : this.paramAdapt.getTargetSet()) {
		    // may throw BuildExecutionException 
		    target.processSource(this, texFile);
		    FileFilter fileFilter = this.fileUtils
			.getFileFilter(texFile, 
				       target.getOutputFileSuffixes());
		    // may throw BuildExecutionException, BuildFailureException
		    this.fileUtils.copyOutputToTargetFolder(texFile,
							    fileFilter,
							    targetDir);

		} // target 
	    } // texFile 
	} finally {
	    if (this.settings.isCleanUp()) {
		// may throw BuildExecutionException
                this.fileUtils.cleanUp(orgFiles, texDirectory);
            }
        }
    }

    /**
     * Converts files in various graphic formats incompatible with LaTeX 
     * into formats which can be inputted or included directly 
     * into a latex file: 
     *
     * <ul>
     * <li>
     * {@link #runFig2Dev(File, LatexDev)} converts a fig-file into pdf, 
     * except the text which is converted into latex-file including the pdf-file
     * <li>
     * {@link #runGnuplot2Dev(File, LatexDev)} converts a gnuplot-file into pdf,
     * except the text which is converted into latex-file including the pdf-file
     * <li>
     * {@link #runMetapost2mps(File)} converts a metapost-file into mps-format. 
     * </ul>
     *
     * @throws BuildExecutionException
     */
    private void processGraphics(File texDirectory) 
	throws BuildExecutionException {

	// process xfig files 
	// may throw BuildExecutionException 
	Collection<File> figFiles = this.fileUtils
	    .getFilesWithSuffix(texDirectory, SUFFIX_FIG);
	for (File figFile : figFiles) {
	    this.log.info("Processing fig-file " + figFile + ". ");
	    // may throw BuildExecutionException 
	    runFig2Dev(figFile, LatexDev.pdf);
	}

	// process gnuplot files 
	// may throw BuildExecutionException 
	Collection<File> pltFiles = this.fileUtils
	    .getFilesWithSuffix(texDirectory, SUFFIX_PLT);
	for (File pltFile : pltFiles) {
	    this.log.info("Processing gnuplot-file " + pltFile + ". ");
	    // may throw BuildExecutionException 
	    runGnuplot2Dev(pltFile, LatexDev.pdf);
	}

	// process metapost files 
	// may throw BuildExecutionException 
	Collection<File> mpFiles = this.fileUtils
	    .getFilesWithSuffix(texDirectory, SUFFIX_MP);
	for (File mpFile : mpFiles) {
	    this.log.info("Processing metapost-file " + mpFile + ". ");
	    // may throw BuildExecutionException 
	    runMetapost2mps(mpFile);
	}
    }

    private void clearGraphics(File texDirectory) 
	throws BuildExecutionException {

	// delete targets of xfig files 
	// may throw BuildExecutionException 
	Collection<File> figFiles = this.fileUtils
	    .getFilesWithSuffix(texDirectory, SUFFIX_FIG);
	for (File figFile : figFiles) {
	    this.log.info("Deleting targets of fig-file " + figFile + ". ");
	    // may throw BuildExecutionException 
	    this.fileUtils.replaceSuffix(figFile, SUFFIX_PTX).delete();
	    this.fileUtils.replaceSuffix(figFile, SUFFIX_PDF).delete();
	}

	// delete targets of gnuplot files 
	// may throw BuildExecutionException 
	Collection<File> pltFiles = this.fileUtils
	    .getFilesWithSuffix(texDirectory, SUFFIX_PLT);
	for (File pltFile : pltFiles) {
	    this.log.info("Deleting targets of gnuplot-file " + pltFile + ". ");
	    // may throw BuildExecutionException 
	    this.fileUtils.replaceSuffix(pltFile, SUFFIX_PTX).delete();
	    this.fileUtils.replaceSuffix(pltFile, SUFFIX_PDF).delete();
	}

	// delete targets of metapost files 
	// may throw BuildExecutionException 
	Collection<File> mpFiles = this.fileUtils
	    .getFilesWithSuffix(texDirectory, SUFFIX_MP);
	FileFilter filter;
	for (File mpFile : mpFiles) {
	    this.log.info("Deleting targets of metapost-file " + mpFile + ". ");
	    // may throw BuildExecutionException 
	    this.fileUtils.replaceSuffix(mpFile, SUFFIX_LOG).delete();
	    this.fileUtils.replaceSuffix(mpFile, SUFFIX_MPX).delete();
	    // delete files xxxNumber.mps 
	    String name1 = mpFile.getName();
	    final String root = name1.substring(0, name1.lastIndexOf("."));
	    filter = new FileFilter() {
		    public boolean accept(File file) {
			return !file.isDirectory()
			    &&  file.getName().matches(root+"\\d+"+SUFFIX_MPS);
		    }
		};

	    // may throw BuildExecutionException 
	    this.fileUtils.delete(mpFile, filter);
	}

	// delete targets of svg files 
	// may throw BuildExecutionException 
	Collection<File> svgFiles = this.fileUtils
	    .getFilesWithSuffix(texDirectory, SUFFIX_SVG);
	for (File svgFile : svgFiles) {
	    this.log.info("Deleting targets of svg-file " + svgFile + ". ");
	    // may throw BuildExecutionException 
	    this.fileUtils.replaceSuffix(svgFile, SUFFIX_PDFTEX).delete();
	    this.fileUtils.replaceSuffix(svgFile, SUFFIX_PDF   ).delete();
	}
    }

    /**
     * Clear all files in <code>texDirectory</code> and folders therein 
     * with name starting as the name of a latex main file without suffix 
     * (which is called the root) but do not clear the latex file itself. 
     * Clear also the eps file <code>zz&lt;root>.eps</code>. 
     */
    private void clearFromLatexMain(File texDirectory) 
	throws BuildExecutionException {

	// may throw BuildExecutionException 
	Collection<File> texMainFiles = this.fileUtils
	    .getLatexMainDocuments(texDirectory, 
				   this.settings.getPatternLatexMainFile());
	FileFilter filter;
	for (final File texFile : texMainFiles) {	    
	    // filter to delete 
	    String name1 = texFile.getName();
	    final String root = name1.substring(0, name1.lastIndexOf("."));
	    filter = new FileFilter() {
		    public boolean accept(File file) {
			return !file.isDirectory()
			    &&  file.getName().matches(root+".+")
			    && !file.equals(texFile);
		    }
		};
	    this.log.info("Deleting files " + root + "... . ");
	    // may throw BuildExecutionException 
	    this.fileUtils.delete(texFile, filter);

	    this.log.info("Deleting file zz" + root + ".eps. ");
	    new File(texFile.getParent(), "zz" + root + ".eps").delete();
	}
    }


    /**
     * Defines clearing ant-task and the maven plugin. 
     * Consists in clearing created graphic files 
     * and created files derived from latex main file. 
     *
     * @see #clearGraphics(File)
     * @see #clearFromLatexMain(File)
     */
    public void clearAll() throws BuildExecutionException {
        this.paramAdapt.initialize();
        this.log.debug("Settings: " + this.settings.toString());

        File texDirectory = this.settings.getTexSrcDirectoryFile();
	clearGraphics     (texDirectory);
	clearFromLatexMain(texDirectory);
    }


    // FIXME: use the -recorder option to resolve dependencies. 
    // With that option, a file xxx.fls is generated with form 
    // PWD /home/ernst/OpenSource/maven-latex-plugin/maven-latex-plugin.git/trunk/maven-latex-plugin/src/site/tex
    // INPUT /usr/local/texlive/2014/texmf.cnf
    // INPUT /usr/local/texlive/2014/texmf-dist/web2c/texmf.cnf
    // INPUT /usr/local/texlive/2014/texmf-var/web2c/pdftex/pdflatex.fmt
    // INPUT manualLatexMavenPlugin.tex
    // OUTPUT manualLatexMavenPlugin.log
    // INPUT /usr/local/texlive/2014/texmf-dist/tex/latex/base/article.cls
    // INPUT /usr/local/texlive/2014/texmf-dist/tex/latex/base/article.cls
    // INPUT /usr/local/texlive/2014/texmf-dist/tex/latex/base/size12.clo
    //
    // The first line starts has the form 'PWD <working directory>' 
    // The other lines have the form '(INPUT|OUTPUT) <file>' 
    // We distinguishe those in the installation, 
    // here '/usr/local/texlive/2014/...' which do not change ever 
    // and others. 
    // In this example, the others are (unified and sorted): 

// INPUT manualLatexMavenPlugin.tex

// OUTPUT manualLatexMavenPlugin.log
// INPUT  manualLatexMavenPlugin.aux
// OUTPUT manualLatexMavenPlugin.aux
// INPUT  manualLatexMavenPlugin.out
// OUTPUT manualLatexMavenPlugin.out
// INPUT  manualLatexMavenPlugin.toc
// OUTPUT manualLatexMavenPlugin.toc
// INPUT  manualLatexMavenPlugin.lof
// OUTPUT manualLatexMavenPlugin.lof
// INPUT  manualLatexMavenPlugin.lot
// OUTPUT manualLatexMavenPlugin.lot

// OUTPUT manualLatexMavenPlugin.idx
// INPUT  manualLatexMavenPlugin.ind

// OUTPUT manualLatexMavenPlugin.pdf

// INPUT 1fig2dev.ptx
// INPUT 1fig2dev.pdf
// INPUT 2plt2pdf.ptx
// INPUT 2plt2pdf.pdf
// INPUT 4tex2pdf.ptx
// INPUT 5aux2bbl.ptx
// INPUT 5aux2bbl.pdf
// INPUT 6idx2ind.ptx
// INPUT 6idx2ind.pdf
// INPUT 7tex2xml.ptx
// INPUT 7tex2xml.pdf
// what is missing is all to do with bibliography, i.e. the bib-file. 

    // FIXME: determine whether to use latexmk makes sense 

    /**
     * Runs LaTeX on <code>texFile</code> at once, 
     * runs BibTeX, MakeIndex and MakeGlossaries by need 
     * and returns whether a second LaTeX run is required. 
     * The latter also holds, if a table of contents, a list of figures 
     * or a list of tables is specified. 
     * <p>
     * A warning is logged if the LaTeX, a BibTeX run a MakeIndex 
     * or a MakeGlossaries run fails 
     * or if a BibTeX run or a MakeIndex or a MakeGlossary run issues a warning
     * in the according methods {@link #runLatex2pdf(File, File)}, 
     * {@link #runBibtexByNeed(File)}, {@link #runMakeIndexByNeed(File)} and 
     * {@link #runMakeGlossaryByNeed(File)}. 
     *
     * @param texFile
     *    the latex-file to be processed. 
     * @param logFile
     *    the log-file after processing <code>texFile</code>. 
     * @return
     *    whether a second LaTeX run is required 
     *    because bibtex, makeindex or makeglossaries had been run 
     *    or to update a table of contents or a list figures or tables. 
     */
    // FIXME: Maybe more than one second latex run is needed 
    private boolean preProcessLatex2pdf(final File texFile, File logFile)
	throws BuildExecutionException {

	// initial latex run 
        runLatex2pdf(texFile, logFile);

	// create a bibliography by need 
	boolean needLatexReRun = runBibtexByNeed(texFile);

	// create an index by need 
	needLatexReRun |= runMakeIndexByNeed(texFile);

	// create a glossary by need 
	needLatexReRun |= runMakeGlossaryByNeed(texFile);

	// rerun LaTeX at least once if bibtex or makeindex had been run 
	// or if a toc, a lof or a lot exists. 
	needLatexReRun |= this.fileUtils.replaceSuffix(texFile, SUFFIX_TOC)
	    .exists();
	needLatexReRun |= this.fileUtils.replaceSuffix(texFile, SUFFIX_LOF)
	    .exists();
	needLatexReRun |= this.fileUtils.replaceSuffix(texFile, SUFFIX_LOT)
	    .exists();

	return needLatexReRun;
    }

    /**
     * Runs LaTeX on <code>texFile</code> at once, 
     * runs BibTeX, MakeIndex and MakeGlossaries by need 
     * according to {@link #preProcessLatex2pdf(File, File)} 
     * and reruns latex as long as needed to get all links 
     * or as threshold {@link Settings#maxNumReruns} specifies. 
     * <p>
     * The result of the LaTeX run is typically some pdf-file, 
     * but it is also possible to specify the dvi-format 
     * (no longer recommended but still working). 
     * <p>
     * Note that no warnings are issued by the latex run. 
     *
     * @param texFile
     *    the latex-file to be processed. 
     * @param logFile
     *    the log-file after processing <code>texFile</code>. 
     * @see #processLatex2pdf(File)
     * @see #processLatex2txt(File)
      */
    public void processLatex2pdfCore(final File texFile, File logFile) 
	throws BuildExecutionException {

	boolean needLatexReRun = preProcessLatex2pdf(texFile, logFile);
	if (needLatexReRun) {
	    log.debug("Rerun LaTeX to update table of contents, ... " + 
		      "including bibliography, index, or that like. ");
	    runLatex2pdf(texFile, logFile);
	}

	// rerun latex by need 
        int retries = 0;
	int maxNumReruns = this.settings.getMaxNumReruns();
       while ((maxNumReruns == -1 || retries < maxNumReruns)
	       && (needLatexReRun = needAnotherLatexRun(logFile))) {
            log.debug("Latex must be rerun. ");
            runLatex2pdf(texFile, logFile);
            retries++;
        }
	if (needLatexReRun) {
	    log.warn("Max rerun reached although LaTeX demands another run. ");
	}
    }

    /**
     * Runs LaTeX on <code>texFile</code> 
     * BibTeX, MakeIndex and MakeGlossaries 
     * and again LaTeX creating a pdf-file 
     * as specified by {@link #preProcessLatex2pdf(File, File)} 
     * and issues a warning if 
     * <ul>
     * <li>
     * another LaTeX rerun is required beyond {@link Settings#maxNumReruns}, 
     * <li>
     * bad boxes or warnings occurred. 
     * For details see {@link #logWarns(File, String)}. 
     * </ul>
     *
     * @param texFile
     *    the tex file to be processed. 
     * @see #needAnotherLatexRun(File)
     */
    public void processLatex2pdf(File texFile) throws BuildExecutionException {

        log.info("Converting into pdf: LaTeX file " + texFile + ". ");
	File logFile = this.fileUtils.replaceSuffix(texFile, SUFFIX_LOG);
	processLatex2pdfCore(texFile, logFile);

	// emit warnings (errors are emitted by runLatex2pdf and that like.)
	logWarns(logFile, this.settings.getTexCommand());
    }

   /**
     * Logs errors detected in the according log file: 
     * The log file is by replacing the ending of <code>texFile</code> 
     * by <code>log</code>. 
     * If the log file exists, a <em>warning</em> is logged 
     * if the error pattern given by {@link Settings#getPatternErrLatex()} 
     * occurs in the log file. 
     * If the log file does not exist, an <em>error</em> is logged. 
     * In both cases, the message logged refers to the <code>command</code> 
     * which failed. 
     */
    private void logErrs(File logFile, String command) 
	throws BuildExecutionException {
	logErrs(logFile, command, this.settings.getPatternErrLatex());
    }

    private void logErrs(File logFile, String command, String pattern) 
	throws BuildExecutionException {

	if (logFile.exists()) {
	    // matchInFile may throw BuildExecutionException
	    boolean errorOccurred = this.fileUtils.matchInFile(logFile,pattern);
	    if (errorOccurred) {
		log.warn("Running " + command + " failed. For details see " + 
			 logFile.getName() + ". ");
	    }
	} else {
	    this.log.error("Running " + command + " failed: no log file " + 
			   logFile.getName() + " found. ");
	}
    }

    /**
     * Logs warnings detected in the according log file: 
     * The log file is by replacing the ending of <code>texFile</code> 
     * by <code>log</code>. 
     * Before logging warnings, 
     * errors are logged via {@link #logErrs(File, String)}. 
     * So, if the log-file does not exist, 
     * an error was already shown and so nothing is to be done here. 
     * If the log file exists, a <em>warning</em> is logged if 
     * <ul>
     * <li>
     * another LaTeX rerun is required beyond {@link Settings#maxNumReruns}, 
     * <li>
     * bad boxes occurred and shall be logged 
     * according to {@link Settings#getDebugBadBoxes()} 
     * <li>
     * warnings occurred and shall be logged 
     * according to {@link Settings#getDebugWarnings()} 
     * </ul>
     * Both criteria are based on pattern recognized in the log file: 
     * {@link #PATTERN_OUFULL_HVBOX} for bad boxes is fixed, 
     * whereas {@link Settings#getPatternWarnLatex()} is configurable. 
     * The message logged refers to the <code>command</code> which failed. 
     */
    private void logWarns(File logFile, String command) 
	throws BuildExecutionException {

	if (!logFile.exists()) {
	    return;
	}

	if (this.settings.getDebugBadBoxes() && 
	    this.fileUtils.matchInFile(logFile, PATTERN_OUFULL_HVBOX)) {
	    log.warn("Running " + command + " created bad boxes. ");
	}
	if (this.settings.getDebugWarnings() && 
	    this.fileUtils.matchInFile(logFile, 
				       this.settings.getPatternWarnLatex())) {
	    log.warn("Running " + command + " emited warnings. ");
	}
    }

    private void logWarns(File logFile, String command, String pattern) 
	throws BuildExecutionException {
	if (!logFile.exists()) {
	    return;
	}

	if (this.fileUtils.matchInFile(logFile, pattern)) {
	    log.warn("Running " + command + 
		     " emitted warnings. For details see " + 
		     logFile.getName() + ". ");
	}
    }



    /**
     * Runs conversion of <code>texFile</code> to html or xhtml 
     * after processing latex to set up the references, 
     * bibliography, index and that like. 
     *
     * @param texFile
     *    the tex file to be processed. 
     * @see #preProcessLatex2pdf(File)
     * @see #runLatex2html(File)
     */
    public void processLatex2html(File texFile)
	throws BuildExecutionException {
	log.info("Converting into html: LaTeX file " + texFile + ". ");
	File logFile = this.fileUtils.replaceSuffix(texFile, SUFFIX_LOG);
	preProcessLatex2pdf(texFile, logFile);
        runLatex2html      (texFile);
    }

    /**
     * Runs conversion of <code>texFile</code> 
     * to odt or other open office formats 
     * after processing latex to set up the references, 
     * bibliography, index and that like. 
     *
     * @param texFile
     *    the tex file to be processed. 
     * @see #preProcessLatex2pdf(File)
     * @see #runLatex2odt(File)
     */
    public void processLatex2odt(File texFile) throws BuildExecutionException {
	log.info("Converting into odt: LaTeX file " + texFile + ". ");
	File logFile = this.fileUtils.replaceSuffix(texFile, SUFFIX_LOG);
        preProcessLatex2pdf(texFile, logFile);
        runLatex2odt       (texFile);
    }

    /**
     * Runs conversion of <code>texFile</code> 
     * to docx or other MS word formats 
     * after processing latex to set up the references, 
     * bibliography, index and that like. 
     *
     * @param texFile
     *    the tex file to be processed. 
     * @see #preProcessLatex2pdf(File)
     * @see #runLatex2odt(File)
     * @see #runOdt2doc(File)
     */
    public void processLatex2docx(File texFile) throws BuildExecutionException {
	log.info("Converting into doc(x): LaTeX file " + texFile + ". ");
	File logFile = this.fileUtils.replaceSuffix(texFile, SUFFIX_LOG);
	preProcessLatex2pdf(texFile, logFile);
        runLatex2odt       (texFile);
        runOdt2doc         (texFile);
    }

    /**
     * Runs direct conversion of <code>texFile</code> to rtf format. 
     * <p>
     * FIXME: Maybe prior invocation of LaTeX MakeIndex and BibTeX 
     * after set up the references, bibliography, index and that like 
     * would be better. 
     *
     * @param texFile
     *    the tex file to be processed. 
     * @see #runLatex2rtf(File)
     */
    public void processLatex2rtf(File texFile) throws BuildExecutionException {
	log.info("Converting into rtf: LaTeX file " + texFile + ". ");
	runLatex2rtf(texFile);
    }

    /**
     * Runs conversion of <code>texFile</code> to txt format via pdf. 
     *
     * @param texFile
     *    the tex file to be processed. 
     * @see #preProcessLatex2pdf(File)
     * @see #runPdf2txt(File)
     */
    public void processLatex2txt(File texFile) throws BuildExecutionException {
	log.info("Converting into txt: LaTeX file " + texFile + ". ");
	File logFile = this.fileUtils.replaceSuffix(texFile, SUFFIX_LOG);
        processLatex2pdfCore(texFile, logFile);
	// warnings emitted by LaTex are ignored 
	// (errors are emitted by runLatex2pdf and that like.)
	runPdf2txt      (texFile);
    }

    private boolean update(File source, File target) {
	if (!target.exists()) {
	    return true;
	}
	assert source.exists();

	return source.lastModified() > target.lastModified();
    }

    /**
     * Converts a gnuplot file into a tex-file with ending ptx 
     * including a pdf-file. 
     *
     * @param pltFile 
     *    the plt-file (gnuplot format) to be converted to pdf. 
     * @throws BuildExecutionException
     *    if running the ptx/pdf-conversion built in in gnuplot fails. 
     * @see #create()
     */
    // used in execute() only 
     public void runGnuplot2Dev(File pltFile, LatexDev dev) 
	throws BuildExecutionException {
	String command = "gnuplot";
	File pdfFile = this.fileUtils.replaceSuffix(pltFile, SUFFIX_PDF);
	File ptxFile = this.fileUtils.replaceSuffix(pltFile, SUFFIX_PTX);

	String[] args = new String[] {
	    "-e",   // run a command string "..." with commands sparated by ';' 
	    // 
	    "set terminal cairolatex " + dev.getGnuplotInTexLanguage() + 
	    ";set output \"" + ptxFile.getName() + 
	    "\";load \"" + pltFile.getName() + "\""
	};
	// FIXME: include options. 
// set terminal cairolatex
// {eps | pdf}
// {standalone | input}
// {blacktext | colortext | colourtext}
// {header <header> | noheader}
// {mono|color}
// {{no}transparent} {{no}crop} {background <rgbcolor>}
// {font <font>} {fontscale <scale>}
// {linewidth <lw>} {rounded|butt|square} {dashlength <dl>}
// {size <XX>{unit},<YY>{unit}}


//	if (update(pltFile, ptxFile)) {
	    log.debug("Running " + command + 
		      " -e...  on file " + pltFile.getName() + ". ");
	    // may throw BuildExecutionException 
	    this.executor.execute(pltFile.getParentFile(), //workingDir 
				  this.settings.getTexPath(), //**** 
				  command, 
				  args);
//	}
	// no check: just warning that no output has been created. 
    }

    // suffix for tex files containing text and including pdf 


    /**
     * Runs fig2dev on fig-files to generate pdf and pdf_t files. 
     * This is a quite restricted usage of fig2dev. 
     *
     * @param figFile
     *    the fig file to be processed. 
     * @throws BuildExecutionException
     *    if running the fig2dev command 
     *    returned by {@link Settings#getFig2devCommand()} failed. 
     *    This is invoked twice: once for creating the pdf-file 
     *    and once for creating the pdf_t-file. 
     * @see #create()
     */
    // used in execute() only 
    public void runFig2Dev(File figFile, LatexDev dev) 
	throws BuildExecutionException {

	String command = this.settings.getFig2devCommand();
	File workingDir = figFile.getParentFile();
	String[] args;

	//File pdfFile   = this.fileUtils.replaceSuffix(figFile, SUFFIX_PDF);
	File pdf_tFile = this.fileUtils.replaceSuffix(figFile, SUFFIX_PTX);

	//String pdf   = pdfFile  .toString();
	String pdf_t = pdf_tFile.toString();

	//if (update(figFile, pdfFile)) {
	    args = new String[] {
		"-L", // language 
		dev.getXFigInTexLanguage(),
		figFile.getName(), // source 
		dev.getXFigInTexFile(this.fileUtils, figFile) // target 
	    };
	    log.debug("Running " + command + 
		      " -Lpdftex  ... on file " + figFile.getName() + ". ");
	    // may throw BuildExecutionException 
	    this.executor.execute(workingDir, 
				  this.settings.getTexPath(), //**** 
				  command, 
				  args);
	    //}

	    //if (update(figFile, pdf_tFile)) {
	    args = new String[] {
		"-L",// language 
		dev.getXFigTexLanguage(),
		"-p",// portrait (-l for landscape), next argument not ignored 
		dev.getXFigInTexFile(this.fileUtils, figFile),
		figFile.getName(), // source 
		pdf_t // target 
	    };
	    log.debug("Running " + command + 
		      " -Lpdftex_t... on file " + figFile.getName() + ". ");
	    // may throw BuildExecutionException 
	    this.executor.execute(workingDir, 
				  this.settings.getTexPath(), //**** 
				  command, 
				  args);
	    //}
	// no check: just warning that no output has been created. 
    }

 
    /**
     * Runs mpost on mp-files to generate mps-files. 
     *
     * @param mpFile
     *    the metapost file to be processed. 
     * @throws BuildExecutionException
     *    if running the mpost command failed. 
     * @see #create()
     */
    // used in execute() only 
    private void runMetapost2mps(File mpFile) throws BuildExecutionException {
	String command = "mpost";
	File workingDir = mpFile.getParentFile();
	// for more information just type mpost --help 
	String[] args = new String[] {
	    "-interaction=nonstopmode",
	    mpFile.getName()
	};
	log.debug("Running " + command + " on " + mpFile.getName() + ". ");
	// may throw BuildExecutionException 
	this.executor.execute(workingDir, 
			      this.settings.getTexPath(), //**** 
			      command, 
			      args);
	// from xxx.mp creates xxx1.mps, xxx.log and xxx.mpx 
	// FIXME: what is xxx.mpx for? 
	File logFile = this.fileUtils.replaceSuffix(mpFile, SUFFIX_LOG);
	logErrs(logFile, command, this.settings.getPatternErrMPost());
	// FIXME: what about warnings?
    }

    /**
     * Runs the latex2rtf command 
     * given by {@link Settings#getLatex2rtfCommand()} 
     * on <code>texFile</code> 
     * in the directory containing <code>texFile</code> 
     * with arguments given by {@link #buildLatex2rtfArguments(String, File)}. 
     *
     * @param texFile
     *    the latex file to be processed. 
     * @throws BuildExecutionException
     *    if running the latex2rtf command 
     *    returned by {@link Settings#getLatex2rtfCommand()} failed. 
     */
    private void runLatex2rtf(File texFile)
            throws BuildExecutionException {

	String command = this.settings.getLatex2rtfCommand();
        log.debug("Running " + command + 
		  " on file " + texFile.getName() + ". ");

        String[] args = buildLatex2rtfArguments(texFile);
	// may throw BuildExecutionException 
        this.executor.execute(texFile.getParentFile(), // workingDir
			      this.settings.getTexPath(), 
			      command, 
			      args);

	// FIXME: no check: just warning that no output has been created. 
	// Warnings and error messages are output to stderr 
	// and by default listed in the console window. 
	// aThey can be redirected to a file “latex2rtf.log” by
	// appending 2>latex2rtf.log to the command line.

    }

    // FIXME: take arguments for latex2rtf into account 
    private String[] buildLatex2rtfArguments( File texFile )
    {
	return new String[] {texFile.getName()};
    }

    /**
     * Runs the tex4ht command given by {@link Settings#getTex4htCommand()} 
     * on <code>texFile</code> 
     * in the directory containing <code>texFile</code> 
     * with arguments given by {@link #buildHtlatexArguments(String, File)}. 
     * FIXME: document about errors and warnings. 
     *
     * @param texFile
     *    the latex file to be processed. 
     * @throws BuildExecutionException
     *    if running the tex4ht command 
     *    returned by {@link Settings#getTex4htCommand()} failed. 
     */
    private void runLatex2html(File texFile)
	throws BuildExecutionException {

	String command = this.settings.getTex4htCommand();
        log.debug("Running " + command + 
		  " on file " + texFile.getName() + ". ");

        String[] args = buildHtlatexArguments(texFile);
	// may throw BuildExecutionException 
        this.executor.execute(texFile.getParentFile(), // workingDir 
			      this.settings.getTexPath(), 
			      command, 
			      args);

	// logging errors and warnings 
	File logFile = this.fileUtils.replaceSuffix(texFile, SUFFIX_LOG);
	logErrs (logFile, command);
	logWarns(logFile, command);
    }

    private String[] buildHtlatexArguments(File texFile)
	throws BuildExecutionException {
        return new String[] {
	    texFile.getName(),
	    this.settings.getTex4htStyOptions(),
	    this.settings.getTex4htOptions(),
	    this.settings.getT4htOptions(),
	    this.settings.getTexCommandArgs()
	};
    }

    /**
     * Runs conversion from latex to odt 
     * executing {@link Settings#getTex4htCommand()} 
     * on <code>texFile</code> 
     * in the directory containing <code>texFile</code> 
     * with arguments given by {@link #buildLatexArguments(String, File)}. 
     * <p>
     * Logs a warning or an error if the latex run failed 
     * invoking {@link #logErrs(File, String)}
     * but not if bad boxes ocurred or if warnings occurred. 
     * This is done in {@link #processLatex2pdf(File)} 
     * after the last LaTeX run only. 
     *
     * @param texFile
     *    the latex file to be processed. 
     * @throws BuildExecutionException
     *    if running the tex4ht command 
     *    returned by {@link Settings#getTex4htCommand()} failed. 
     */
    private void runLatex2odt(File texFile)
            throws BuildExecutionException {

	String command = this.settings.getTex4htCommand();
        log.debug("Running " + command + 
		  " on file " + texFile.getName() + ". ");

        String[] args = new String[] {
	    texFile.getName(),
	    "xhtml,ooffice", // there is no choice here 
	    "ooffice/! -cmozhtf",// ooffice/! represents a font direcory 
	    "-coo -cvalidate"// -coo is mandatory, -cvalidate is not 
	};
	// may throw BuildExecutionException 
        this.executor.execute(texFile.getParentFile(), 
			      this.settings.getTexPath(), 
			      command, 
			      args);

	// FIXME: logging refers to latex only, not to tex4ht or t4ht script 
	File logFile = this.fileUtils.replaceSuffix(texFile, SUFFIX_LOG);
	logErrs (logFile, command);
	logWarns(logFile, command);
    }


    // FIXME: missing options. 
    // above all (input) doctype: -ddoc, -ddocx 
    // and (output) doctype: -fdoc, -fdocx, 
    // available: odt2doc --show. 
    // among those also: latex and rtf !!!!!! 
    // This is important to define the copy filter accordingly 
    /**
     * Runs conversion from odt to doc or docx-file  
     * executing {@link Settings#getOdt2docCommand()} 
     * on an odt-file created from <code>texFile</code> 
     * in the directory containing <code>texFile</code> 
     * with arguments given by {@link #buildLatexArguments(String, File)}. 
     *
     * @param texFile
     *    the latex file to be processed. 
     * @throws BuildExecutionException
     *    if running the odt2doc command 
     *    returned by {@link Settings#getOdt2docCommand()} failed. 
     */
    private void runOdt2doc( File texFile)
            throws BuildExecutionException {

	File odtFile = this.fileUtils.replaceSuffix(texFile, SUFFIX_ODT);
	String command = this.settings.getOdt2docCommand();
	log.debug("Running " + command + 
		  " on file " + odtFile.getName() + ". ");

	String[] args = buildArguments(this.settings.getOdt2docOptions(),
				       odtFile);

	// may throw BuildExecutionException 
	this.executor.execute(texFile.getParentFile(), 
			      this.settings.getTexPath(), 
			      command, 
			      args);
    }

    /**
     * Runs conversion from pdf to txt-file  
     * executing {@link Settings#getPdf2txtCommand()} 
     * on a pdf-file created from <code>texFile</code> 
     * in the directory containing <code>texFile</code> 
     * with arguments given by {@link #buildLatexArguments(String, File)}. 
     *
     * @param texFile
     *    the latex file to be processed. 
     * @throws BuildExecutionException
     *    if running the pdf2txt command 
     *    returned by {@link Settings#getPdf2txtCommand()} failed. 
     */
    private void runPdf2txt(File texFile) throws BuildExecutionException {

	File pdfFile = this.fileUtils.replaceSuffix(texFile, SUFFIX_PDF);
	String command = this.settings.getPdf2txtCommand();
	log.debug("Running " + command + 
		  " on file " + pdfFile.getName() + ". ");

	String[] args = buildArguments(this.settings.getPdf2txtOptions(),
				       pdfFile);
	// may throw BuildExecutionException 
	this.executor.execute(texFile.getParentFile(), 
			      this.settings.getTexPath(), 
			      command, 
			      args);
	// FIXME: what about error logging? 
	// Seems not to create a log-file. 
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
    static String[] buildArguments(String options, File file) {
	if (options.isEmpty()) {
	    return new String[] {file.getName()};
	}
        String[] optionsArr = options.split(" ");
        String[] args = Arrays.copyOf(optionsArr, optionsArr.length + 1);
        args[optionsArr.length] = file.getName();
	
	return args;
     }

    /**
     * Returns whether another LaTeX run is necessary 
     * based on a pattern matching in the log file. 
     *
     * @see Settings#getPatternLatexNeedsReRun()
     */
    private boolean needAnotherLatexRun(File logFile)
	throws BuildExecutionException {
        String reRunPattern = this.settings.getPatternLatexNeedsReRun();
	// may throw a BuildExecutionException
        boolean needRun = this.fileUtils.matchInFile(logFile, reRunPattern);
        log.debug( "Another LaTeX run? " + needRun );
        return needRun;
    }

    /**
     * Runs the makeindex command 
     * given by {@link Settings#getMakeinexCommand()} 
     * on the idx-file corresponding with <code>texFile</code> 
     * in the directory containing <code>texFile</code> 
     * provided that the existence of an idx-file indicates 
     * that an index shall be created. 
     *
     * @return
     *    whether makeindex is run. 
     *    Equivalently, whether LaTeX has to be rerun because of makeindex. 
     */
    private boolean runMakeIndexByNeed(File texFile)
	throws BuildExecutionException {

	File idxFile = this.fileUtils.replaceSuffix(texFile, SUFFIX_IDX);
	boolean needRun = idxFile.exists();
	log.debug("MakeIndex run required? " + needRun);
	if (!needRun) {
	    return false;
	}

	log.debug("Running " + this.settings.getMakeIndexCommand() + 
		  " on file " + idxFile.getName() + ". ");

	String[] args = buildArguments(this.settings.getMakeIndexOptions(),
				       idxFile);
	// may throw BuildExecutionException 
	this.executor.execute(idxFile.getParentFile(), //workingDir 
			      this.settings.getTexPath(), 
			      this.settings.getMakeIndexCommand(), 
			      args);

	// detect errors and warnings 

 	File logFile = this.fileUtils.replaceSuffix(idxFile, SUFFIX_ILG);
	logErrs (logFile, 
		 this.settings.getMakeIndexCommand(), 
		 this.settings.getPatternErrMakeindex());
	logWarns(logFile, 
		 this.settings.getMakeIndexCommand(), 
		 this.settings.getPatternWarnMakeindex());
	return true;
    }

    private boolean runMakeGlossaryByNeed(File texFile)
	throws BuildExecutionException {

	// file name without ending 
	File xxxFile = this.fileUtils.replaceSuffix(texFile, SUFFIX_VOID);
	File gloFile = this.fileUtils.replaceSuffix(texFile, SUFFIX_GLO);
	File istFile = this.fileUtils.replaceSuffix(texFile, SUFFIX_IST);
	File xdyFile = this.fileUtils.replaceSuffix(texFile, SUFFIX_XDY);
	boolean needRun = gloFile.exists();
	assert ( gloFile.exists() && ( istFile.exists() ^   xdyFile.exists()))
	    || (!gloFile.exists() && (!istFile.exists() && !xdyFile.exists()));
	log.debug("MakeGlossary run required? " + needRun);
	if (!needRun) {
	    return false;
	}

	log.debug("Running " + this.settings.getMakeGlossariesCommand() + 
		  " on file " + xxxFile.getName()+ ". ");

	String[] args = buildArguments(this.settings.getMakeGlossariesOptions(),
				       xxxFile);

	// may throw BuildExecutionException 
	this.executor.execute(texFile.getParentFile(), //workingDir 
			      this.settings.getTexPath(), 
			      this.settings.getMakeGlossariesCommand(), 
			      args);

	// detect errors and warnings 
	File glgFile = this.fileUtils.replaceSuffix(texFile, SUFFIX_GLG);
	logErrs (glgFile, 
		 this.settings.getMakeGlossariesCommand(), 
		 this.settings.getPatternMakeGlossariesErr());
	logWarns(glgFile, 
		 this.settings.getMakeGlossariesCommand(), 
		 this.settings.getPatternWarnMakeindex() + "|" + 
		 this.settings.getPatternWarnXindy());
	return true;
    }

    /**
     * Runs the bibtex command given by {@link Settings#getBibtexCommand()} 
     * on the aux-file corresponding with <code>texFile</code> 
     * in the directory containing <code>texFile</code> 
     * provided an according pattern in the aux-file indicates 
     * that a bibliography shall be created. 
     *
     * @return
     *    whether bibtex is run. 
     *    Equivalently, whether LaTeX has to be rerun because of bibtex. 
     */
    private boolean runBibtexByNeed(File texFile) 
	throws BuildExecutionException {

	File auxFile =  this.fileUtils.replaceSuffix(texFile, SUFFIX_AUX);
        boolean needRun = this.fileUtils.matchInFile(auxFile, 
						     PATTERN_NEED_BIBTEX_RUN);
	log.debug("BibTeX run required? " + needRun);
	if (!needRun) {
	    return false;
	}

	log.debug("Running " + this.settings.getBibtexCommand() + 
		  " on file " + auxFile.getName() + ". ");

	String[] args = new String[] {auxFile.getName()};
	// may throw BuildExecutionException 
        this.executor.execute(texFile.getParentFile(), // workingDir 
			      this.settings.getTexPath(), 
			      this.settings.getBibtexCommand(), 
			      args);

	File logFile = this.fileUtils.replaceSuffix(texFile, SUFFIX_BLG);
	if (logFile.exists()) {
	    // FIXME: Could be further improved: 1 error but more warnings: 
	    // The latter shall be displayed. (maybe)
	    boolean errOccurred = this.fileUtils
		.matchInFile(logFile, this.settings.getPatternErrBibtex());
	    if (errOccurred) {
		log.warn("Running BibTeX on " + texFile + 
			 " failed: For details see " + 
			 logFile.getName() + ". ");
	    }
	    boolean warnOccurred = this.fileUtils
		.matchInFile(logFile, this.settings.getPatternWarnBibtex());
	    if (warnOccurred) {
		log.warn("Running BibTeX on " + texFile + 
			 " emitted warnings: For details see " + 
			 logFile.getName() + ". ");
	    }
	} else {
	    this.log.error("Running BibTeX on " + texFile + 
			   " failed: no log file found. ");
	}
	return true;
    }

    /**
     * Runs the LaTeX command given by {@link Settings#getLatexCommand()} 
     * on <code>texFile</code> 
     * in the directory containing <code>texFile</code> 
     * with arguments given by {@link #buildLatexArguments(File)}. 
     * <p>
     * Logs a warning or an error if the latex run failed 
     * invoking {@link #logErrs(File, String)}
     * but not if bad boxes occurred or if warnings occurred. 
     * This is done in {@link #processLatex2pdf(File)} 
     * after the last LaTeX run only. 
     *
     * @param texFile
     *    the latex-file to be processed. 
     * @param logFile
     *    the log-file after processing <code>texFile</code>. 
     * @throws BuildExecutionException
     *    if running the latex command 
     *    returned by {@link Settings#getLatexCommand()} failed. 
     */
    private void runLatex2pdf(File texFile, File logFile)
	throws BuildExecutionException {

	String command = this.settings.getTexCommand();
        log.debug("Running " + command + 
		  " on file " + texFile.getName() + ". ");

	String[] args = buildArguments(this.settings.getTexCommandArgs(), 
				       texFile);
	// may throw BuildExecutionException 
        this.executor.execute(texFile.getParentFile(), // workingDir 
			      this.settings.getTexPath(), 
			      command, 
			      args);

	// logging errors (warnings are done in processLatex2pdf)
	logErrs(logFile, command);
    }

 }


