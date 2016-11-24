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

import org.m2latex.mojo.CfgLatexMojo;
import org.m2latex.mojo.GraphicsMojo;
import org.m2latex.mojo.ClearMojo;

import java.io.File;
import java.io.FileFilter;

import java.util.Collection;

// idea: use latex2rtf and unoconv
// idea: targets for latex2html, latex2man, latex2png and many more. 

/**
 * The latex processor creates various output from latex sources 
 * including also preprocessing of graphic files in several formats. 
 * This is the core class of this piece of software. 
 * The main method is {@link #create()} which is executed by the ant task 
 * and by the maven plugin given by {@link CfgLatexMojo}. 
 * Also important are {@link #clearAll()} which is executed by 
 * the maven plugin given by {@link ClearMojo}. 
 * also {@link #processGraphics()} which is executed by 
 * the maven plugin given by {@link GraphicsMojo} 
 * which is helpful for information development. 
 * <p>
 * This class delegates preprocessing of the graphic files, 
 * selection of the latex main files and deleting their targets 
 * to {@link LatexPreProcessor}. 
 * Processing of the latex main files is done in {@link #create()} 
 * according to the target(s) given by the parameters. 
 * The elements of the enumeration {@link Target} 
 * use methods {@link #processLatex2rtf(File)}, 
 * {@link #processLatex2rtf(File)}, {@link #processLatex2pdf(File)}, 
 * {@link #processLatex2html(File)}, {@link #processLatex2odt(File)}, 
 * {@link #processLatex2docx(File)} and {@link #processLatex2txt(File)}. 
 */
public class LatexProcessor extends AbstractLatexProcessor {

    static final String PATTERN_NEED_BIBTEX_RUN = "^\\\\bibdata";

    // Note that two \\ represent a single \ in the string. 
    // Thus \\\\ represents '\\' in the pattern, 
    // which in turn represents a single \. 
    static final String PATTERN_OUFULL_HVBOX = 
	"^(Ov|Und)erfull \\\\[hv]box \\(";


    // LaTeX (notably, .tex is not needed )
    final static String SUFFIX_TOC = ".toc";
    final static String SUFFIX_LOF = ".lof";
    final static String SUFFIX_LOT = ".lot";
    final static String SUFFIX_AUX = ".aux";

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
    // sorted and unified glossary created by makeindex 
    final static String SUFFIX_GLS = ".gls";
    // logging file for makeindex used with glossaries 
    final static String SUFFIX_GLG = ".glg";

    // bibtex 
    final static String SUFFIX_BLG = ".blg";

    // needed by makeglossaries 
    final static String SUFFIX_VOID = "";

    private final ParameterAdapter paramAdapt;

    private final LatexPreProcessor preProc;

    // also for tests 
    LatexProcessor(Settings settings, 
		   CommandExecutor executor, 
		   LogWrapper log, 
		   TexFileUtils fileUtils,
		   ParameterAdapter paramAdapt) {
	super(settings, executor, log, fileUtils);
	this.paramAdapt = paramAdapt;
	this.preProc = new LatexPreProcessor
	    (this.settings, this.executor, this.log, this.fileUtils);
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
     * Defines creational ant-task and the maven plugin 
     * in {@link CfgLatexMojo} and subclasses. 
     * This consists in reading the parameters 
     * via {@link ParameterAdapter#initialize()} 
     * processing graphic-files 
     * via {@link LatexPreProcessor#processGraphicsSelectMain(Collection)} 
     * and processing the tex main files 
     * via {@link Target#processSource(LatexProcessor, File)}. 
     * The resulting files are identified by its suffixes 
     * via  {@link Target#getPatternOutputFiles(Settings)} 
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
	// should fail without finally cleanup 
	Collection<File> orgFiles = this.fileUtils.getFilesRec(texDirectory);

	try {
	    // process graphics and determine latexMainFiles 
	    // may throw BuildExecutionException 
	    Collection<File> latexMainFiles = this.preProc
		.processGraphicsSelectMain(orgFiles);
	    for (File texFile : latexMainFiles) {
		// may throw BuildExecutionException, BuildFailureException 
		File targetDir = this.fileUtils.getTargetDirectory
		    (texFile, 
		     texDirectory,
		     this.settings.getOutputDirectoryFile());

		for (Target target : this.paramAdapt.getTargetSet()) {
		    // may throw BuildExecutionException 
		    target.processSource(this, texFile);
		    FileFilter fileFilter = this.fileUtils.getFileFilter
			(texFile, target.getPatternOutputFiles(this.settings));
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
     * Used by {@link GraphicsMojo}. 
     */
    public void processGraphics() throws BuildExecutionException {
	File texDirectory = this.settings.getTexSrcDirectoryFile();

	if (!texDirectory.exists()) {
	    this.log.info("No tex directory - " + 
			  "skipping graphics processing. ");
	    return;
	}

	// may throw BuildExecutionException 
	Collection<File> orgFiles = this.fileUtils.getFilesRec(texDirectory);
	this.preProc.processGraphicsSelectMain(orgFiles);
    }

    /**
     * Defines clearing ant-task and the maven plugin 
     * in {@link ClearMojo}. 
     * Consists in clearing created graphic files 
     * and created files derived from latex main file. 
     *
     * @throws BuildExecutionException 
     */
    public void clearAll() throws BuildExecutionException {
        this.paramAdapt.initialize();
        this.log.debug("Settings: " + this.settings.toString());

        File texDirectory = this.settings.getTexSrcDirectoryFile();
	// may throw BuildExecutionException 
	this.preProc.clearCreated(texDirectory);
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
     *    the number of LaTeX runs required 
     *    because bibtex, makeindex or makeglossaries had been run 
     *    or to update a table of contents or a list figures or tables. 
     *    <ul>
     *    <li>
     *    If neither of these are present, no rerun is required. 
     *    <li>
     *    If a bibliography, an index and a glossary is included 
     *    and a table of contents, 
     *    we assume that these are in the table of contents. 
     *    Thus two reruns are required: 
     *    one to include the bibliography or that like 
     *    and the second one to make it appear in the table of contents. 
     *    <li>
     *    In all other cases, a single rerun suffices 
     *    </ul>
     * @see #processLatex2pdfCore(File, File)
     * @see #processLatex2html(File)
     * @see #processLatex2odt(File)
     * @see #processLatex2docx(File)
     */
    private int preProcessLatex2pdf(final LatexMainDesc desc)
	throws BuildExecutionException {

	// initial latex run 
        runLatex2pdf(desc);
	File texFile = desc.texFile;

	// create bibliography, index and glossary by need 
	boolean hasBib    = runBibtexByNeed      (texFile);
	boolean hasIdxGls = runMakeIndexByNeed   (desc)
	    |               runMakeGlossaryByNeed(desc);

	// rerun LaTeX at least once if bibtex or makeindex had been run 
	// or if a toc, a lof or a lot exists. 
	if (hasBib) {
	    // on run to include the bibliography from xxx.bbl into the pdf 
	    // and the lables into the aux file 
	    // and another run to put the lables from the aux file 
	    // to the locations of the \cite commands. 

	    // This suffices also to include a bib in a toc 
	    return 2;
	}

	boolean hasToc = 
	    this.fileUtils.replaceSuffix(texFile, SUFFIX_TOC)  .exists();
	if (hasIdxGls) {
	    // Here, an index or a glossary exists 
	    // This requires at least one LaTeX run. 

	    // if one of these has to be included in a toc, 
	    // a second run is needed. 
	    return hasToc ? 2 : 1;
	}
	// Here, no bib, index or glossary exists. 
	// The result is either 0 or 1, 
	// depending on whether a toc, lof or lot exists 

	boolean needLatexReRun = hasToc 
	    || this.fileUtils.replaceSuffix(texFile, SUFFIX_LOF).exists()
	    || this.fileUtils.replaceSuffix(texFile, SUFFIX_LOT).exists();

	return needLatexReRun ? 1 : 0;
    }

    /**
     * Runs LaTeX on <code>texFile</code> at once, 
     * runs BibTeX, MakeIndex and MakeGlossaries by need 
     * according to {@link #preProcessLatex2pdf(File, File)} 
     * and reruns latex as long as needed to get all links 
     * or as threshold {@link Settings#maxNumReRunsLatex} specifies. 
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
    private void processLatex2pdfCore(final LatexMainDesc desc) 
	throws BuildExecutionException {

	int numLatexReRuns = preProcessLatex2pdf(desc);
				      
	assert numLatexReRuns == 0 
	    || numLatexReRuns == 1 
	    || numLatexReRuns == 2;
	if (numLatexReRuns > 0) {
	    // rerun LaTeX without makeindex and makeglossaries 
	    log.debug("Rerun LaTeX to update table of contents, ... " + 
		      "bibliography, index, or that like. ");
	    runLatex2pdf(desc);
	    numLatexReRuns--;
	}
	assert numLatexReRuns == 0 || numLatexReRuns == 1;

	// rerun latex by need patternRerunMakeIndex
	boolean needMakeIndexReRun;
	boolean needLatexReRun = (numLatexReRuns == 1)
	    || needAnotherLatexRun(desc.logFile);

	int maxNumReruns = this.settings.getMaxNumReRunsLatex();
	for (int num = 0; maxNumReruns == -1 || num < maxNumReruns; num++) {
	    needMakeIndexReRun = needAnotherMakeIndexRun(desc.logFile);
	    // FIXME: superfluous since pattern rerunfileckeck 
	    // triggering makeindex also fits rerun of LaTeX 
	    needLatexReRun |= needMakeIndexReRun;
	    if (!needLatexReRun) {
		return;
	    }
            log.debug("Latex must be rerun. ");
	    if (needMakeIndexReRun) {
		// FIXME: not by need 
		runMakeIndexByNeed(desc);
	    }

            runLatex2pdf(desc);

	    needLatexReRun = needAnotherLatexRun(desc.logFile);
        }
	log.warn("Max rerun reached although LaTeX demands another run. ");
    }

    /**
     * Returns whether another LaTeX run is necessary 
     * based on a pattern matching in the log file. 
     *
     * @see Settings#getPatternReRunMakeIndex()
     */
    // FIXME: unification with needAnotherLatexRun? 
    private boolean needAnotherMakeIndexRun(File logFile)
	throws BuildExecutionException {
        String reRunPattern = this.settings.getPatternReRunMakeIndex();
	// may throw a BuildExecutionException
        boolean needRun = this.fileUtils.matchInFile(logFile, reRunPattern);
        log.debug("Another MakeIndex run? " + needRun);
        return needRun;
    }

    /**
     * Returns whether another LaTeX run is necessary 
     * based on a pattern matching in the log file. 
     *
     * @see Settings#getPatternReRunLatex()
     */
    private boolean needAnotherLatexRun(File logFile)
	throws BuildExecutionException {
        String reRunPattern = this.settings.getPatternReRunLatex();
	// may throw a BuildExecutionException
        boolean needRun = this.fileUtils.matchInFile(logFile, reRunPattern);
        log.debug("Another LaTeX run? " + needRun);
        return needRun;
    }

    /**
     * Container which comprises, besides the latex main file 
     * also several files creation of which shall be done once for ever. 
     */
    static class LatexMainDesc {

	private final File texFile;
	private final File logFile;
	private final File idxFile;
	private final File gloFile;
	private final File xxxFile;
	private final File glgFile;

	LatexMainDesc(File texFile, TexFileUtils fileUtils) {
	    this.texFile = texFile;
	    // FIXME: easier to create xxxFile first 
	    this.xxxFile = fileUtils.replaceSuffix(texFile, SUFFIX_VOID);
	    this.logFile = fileUtils.replaceSuffix(texFile, SUFFIX_LOG);
	    this.idxFile = fileUtils.replaceSuffix(texFile, SUFFIX_IDX);
	    this.gloFile = fileUtils.replaceSuffix(texFile, SUFFIX_GLO);
	    this.glgFile = fileUtils.replaceSuffix(texFile, SUFFIX_GLG);
	}
    } // class LatexMainDesc 

    /**
     * Runs LaTeX on <code>texFile</code> 
     * BibTeX, MakeIndex and MakeGlossaries 
     * and again LaTeX creating a pdf-file 
     * as specified by {@link #preProcessLatex2pdf(File, File)} 
     * and issues a warning if 
     * <ul>
     * <li>
     * another LaTeX rerun is required 
     * beyond {@link Settings#maxNumReRunsLatex}, 
     * <li>
     * bad boxes or warnings occurred. 
     * For details see {@link #logWarns(File, String)}. 
     * </ul>
     *
     * @param texFile
     *    the tex file to be processed. 
     * @see #needAnotherLatexRun(File)
     * @see Target#pdf
     */
    void processLatex2pdf(File texFile) throws BuildExecutionException {
        log.info("Converting into pdf: LaTeX file '" + texFile + "'. ");
	LatexMainDesc desc = new LatexMainDesc(texFile, this.fileUtils);
	processLatex2pdfCore(desc);

	// emit warnings (errors are emitted by runLatex2pdf and that like.)
	logWarns(desc.logFile, this.settings.getLatex2pdfCommand());
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
	// may throw BuildExecutionException 
	logErrs(logFile, command, this.settings.getPatternErrLatex());
    }

    /**
     * Logs warnings detected in the according log-file <code>logFile</code>: 
     * Before logging warnings, 
     * errors are logged via {@link #logErrs(File, String)}. 
     * So, if the log-file does not exist, 
     * an error was already shown and so nothing is to be done here. 
     * If the log-file exists, a <em>warning</em> is logged if 
     * <ul>
     * <li>
     * another LaTeX rerun is required beyond {@link Settings#maxNumReruns}, 
     * <li>
     * bad boxes occurred and shall be logged 
     * according to {@link Settings#getDebugBadBoxes()}. 
     * <li>
     * warnings occurred and shall be logged 
     * according to {@link Settings#getDebugWarnings()}. 
     * </ul>
     * Both criteria are based on pattern recognized in the log file: 
     * {@link #PATTERN_OUFULL_HVBOX} for bad boxes is fixed, 
     * whereas {@link Settings#getPatternWarnLatex()} is configurable. 
     * The message logged refers to the <code>command</code> which failed. 
     *
     * @param logFile
     *    the log-file to detect warnings in. 
     * @param command
     *    the command which created <code>logFile</code> 
     *    and which maybe created warnings. 
     */
    private void logWarns(File logFile, String command) 
    	throws BuildExecutionException {

	if (!logFile.exists()) {
	    return;
	}

	if (this.settings.getDebugBadBoxes() && 
	    // may throw BuildExecutionException: not really 
	    this.fileUtils.matchInFile(logFile, PATTERN_OUFULL_HVBOX)) {
	    log.warn("Running " + command + " created bad boxes. ");
	}
	if (this.settings.getDebugWarnings() && 
	    // may throw BuildExecutionException: not really 
	    this.fileUtils.matchInFile(logFile, 
				       this.settings.getPatternWarnLatex())) {
	    log.warn("Running " + command + " emited warnings. ");
	}
    }

    /**
     * Runs conversion of <code>texFile</code> to html or xhtml 
     * after processing latex to set up the references, 
     * bibliography, index and that like. 
     *
     * @param texFile
     *    the tex file to be processed. 
     * @see #preProcessLatex2pdf(File, File)
     * @see #runLatex2html(File)
     * @see Target#html
     */
    void processLatex2html(File texFile)
	throws BuildExecutionException {
	log.info("Converting into html: LaTeX file '" + texFile + "'. ");
	LatexMainDesc desc = new LatexMainDesc(texFile, this.fileUtils);
	preProcessLatex2pdf(desc);
	// may throw BuildExecutionException 
        runLatex2html      (desc);
    }

    /**
     * Runs conversion of <code>texFile</code> 
     * to odt or other open office formats 
     * after processing latex to set up the references, 
     * bibliography, index and that like. 
     *
     * @param texFile
     *    the tex file to be processed. 
     * @see #preProcessLatex2pdf(File, File)
     * @see #runLatex2odt(File)
     * @see Target#odt
     */
    void processLatex2odt(File texFile) throws BuildExecutionException {
	log.info("Converting into odt: LaTeX file '" + texFile + "'. ");
	LatexMainDesc desc = new LatexMainDesc(texFile, this.fileUtils);
        preProcessLatex2pdf(desc);
	// may throw BuildExecutionException 
        runLatex2odt       (desc);
    }

    /**
     * Runs conversion of <code>texFile</code> 
     * to docx or other MS word formats 
     * after processing latex to set up the references, 
     * bibliography, index and that like. 
     *
     * @param texFile
     *    the tex file to be processed. 
     * @see #preProcessLatex2pdf(File, File)
     * @see #runLatex2odt(File)
     * @see #runOdt2doc(File)
     * @see Target#docx
     */
    void processLatex2docx(File texFile) throws BuildExecutionException {
	log.info("Converting into doc(x): LaTeX file '" + texFile + "'. ");
	LatexMainDesc desc = new LatexMainDesc(texFile, this.fileUtils);
	preProcessLatex2pdf(desc);
	// may throw BuildExecutionException 
        runLatex2odt       (desc);
	// may throw BuildExecutionException 
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
     * @see Target#rtf
     */
    void processLatex2rtf(File texFile) throws BuildExecutionException {
	log.info("Converting into rtf: LaTeX file '" + texFile + "'. ");
	// may throw BuildExecutionException 
	runLatex2rtf(texFile);
    }

    /**
     * Runs conversion of <code>texFile</code> to txt format via pdf. 
     *
     * @param texFile
     *    the tex file to be processed. 
     * @see #processLatex2pdfCore(File, File)
     * @see #runPdf2txt(File)
     * @see Target#rtf
     */
    void processLatex2txt(File texFile) throws BuildExecutionException {
	log.info("Converting into txt: LaTeX file '" + texFile + "'. ");
	LatexMainDesc desc = new LatexMainDesc(texFile, this.fileUtils);
	processLatex2pdfCore(desc);
	// warnings emitted by LaTex are ignored 
	// (errors are emitted by runLatex2pdf and that like.)
	// may throw BuildExecutionException 
	runPdf2txt      (texFile);
    }

    /**
     * Runs the BibTeX command given by {@link Settings#getBibtexCommand()} 
     * on the aux-file corresponding with <code>texFile</code> 
     * in the directory containing <code>texFile</code> 
     * provided an according pattern in the aux-file indicates 
     * that a bibliography shall be created. 
     *
     * @param texFile
     *    the latex-file BibTeX is to be processed for. 
     * @return
     *    whether BibTeX has been run. 
     *    Equivalently, whether LaTeX has to be rerun because of BibTeX. 
     */
    private boolean runBibtexByNeed(File texFile) 
	throws BuildExecutionException {

	File auxFile    = this.fileUtils.replaceSuffix(texFile, SUFFIX_AUX);
        boolean needRun = this.fileUtils.matchInFile(auxFile, 
						     PATTERN_NEED_BIBTEX_RUN);
	log.debug("BibTeX run required? " + needRun);
	if (!needRun) {
	    return false;
	}

	String command = this.settings.getBibtexCommand();
	log.debug("Running " + command + " on '" + auxFile.getName() + "'. ");

	String[] args = buildArguments(this.settings.getBibtexOptions(), 
				       auxFile);
	// may throw BuildExecutionException 
        this.executor.execute(texFile.getParentFile(), // workingDir 
			      this.settings.getTexPath(), 
			      command, 
			      args);

	File logFile = this.fileUtils.replaceSuffix(texFile, SUFFIX_BLG);
	logErrs (logFile, command, this.settings.getPatternErrBibtex());
	logWarns(logFile, command, this.settings.getPatternWarnBibtex());
	return true;
    }

    /**
     * Runs the MakeIndex command 
     * given by {@link Settings#getMakeIndexCommand()} 
     * on the idx-file corresponding with <code>texFile</code> 
     * in the directory containing <code>texFile</code> 
     * provided that the existence of an idx-file indicates 
     * that an index shall be created. 
     *
     * @param texFile
     *    the latex-file MakeIndex is to be processed for. 
     * @return
     *    whether MakeIndex had been run. 
     *    Equivalently, whether LaTeX has to be rerun because of MakeIndex. 
     */
    // FIXME: bad name since now there are reruns. 
    // Suggestion: runMakeIndexInitByNeed 
    // Other methods accordingly. 
    // maybe better: eliminate altogether 
    private boolean runMakeIndexByNeed(LatexMainDesc desc)
	throws BuildExecutionException {

	// raw index file written by pdflatex 
	boolean needRun = desc.idxFile.exists();
	log.debug("MakeIndex run required? " + needRun);
	if (needRun) {
	    // may throw BuildExecutionException 
	    runMakeIndex(desc);
	}
	return needRun;
    }

    /**
     * Runs the MakeIndex command 
     * given by {@link Settings#getMakeIndexCommand()}. 
     *
     * @param idxFile
     *    the idx-file MakeIndex is to be run on. 
     */
    // FIXME: strange:  runMakeIndexByNeed based on tex-file 
    // and this one based on idx-file. 
    // no longer appropriate to create idx-file 
    // because makeIndex is rerun. 
    private void runMakeIndex(LatexMainDesc desc) 
	throws BuildExecutionException {
	String command = this.settings.getMakeIndexCommand();
	File idxFile = desc.idxFile;
	log.debug("Running " + command  + " on '" + idxFile.getName() + "'. ");
	String[] args = buildArguments(this.settings.getMakeIndexOptions(),
				       idxFile);
	// may throw BuildExecutionException 
	this.executor.execute(idxFile.getParentFile(), //workingDir 
			      this.settings.getTexPath(), 
			      command, 
			      args);

	// detect errors and warnings makeindex wrote into xxx.ilg 
 	File logFile = this.fileUtils.replaceSuffix(idxFile, SUFFIX_ILG);
	logErrs (logFile, command, this.settings.getPatternErrMakeIndex());
	logWarns(logFile, command, this.settings.getPatternWarnMakeIndex());
    }

   /**
     * Runs the MakeGlossaries command 
     * given by {@link Settings#getMakeGlossariesCommand()} 
     * on the aux-file corresponding with <code>texFile</code> 
     * in the directory containing <code>texFile</code> 
     * provided that the existence of an glo-file indicates 
     * that a glossary shall be created. 
     * The MakeGlossaries command is just a wrapper 
     * arround the programs <code>makeindex</code> and <code>xindy</code>. 
     *
     * @param texFile
     *    the latex-file MakeGlossaries is to be processed for. 
     * @return
     *    whether MakeGlossaries had been run. 
     *    Equivalently, 
     *    whether LaTeX has to be rerun because of MakeGlossaries. 
     */
     private boolean runMakeGlossaryByNeed(LatexMainDesc desc)
	throws BuildExecutionException {

	// raw glossaries file created by pdflatex 
	boolean needRun = desc.gloFile.exists();
	log.debug("MakeGlossaries run required? " + needRun);
	if (!needRun) {
	    return false;
	}

	// file name without ending: parameter for makeglossaries 
	File xxxFile = desc.xxxFile;
	String command = this.settings.getMakeGlossariesCommand();
	log.debug("Running " + command + " on '" + xxxFile.getName()+ "'. ");
	String[] args = buildArguments(this.settings.getMakeGlossariesOptions(),
				       xxxFile);
	// may throw BuildExecutionException 
	this.executor.execute(xxxFile.getParentFile(), //workingDir 
			      this.settings.getTexPath(), 
			      command, 
			      args);

	// detect errors and warnings makeglossaries wrote into xxx.glg 
	File glgFile = desc.glgFile;
	logErrs (glgFile, command, this.settings.getPatternErrMakeGlossaries());
	logWarns(glgFile, command, this.settings.getPatternWarnMakeIndex() 
		 +           "|" + this.settings.getPatternWarnXindy());
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
    private void runLatex2pdf(LatexMainDesc desc)
	throws BuildExecutionException {

	File texFile = desc.texFile;
	String command = this.settings.getLatex2pdfCommand();
	log.debug("Running " + command + " on '" + texFile.getName() + "'. ");

	String[] args = buildArguments(this.settings.getLatex2pdfOptions(), 
				       texFile);
	// may throw BuildExecutionException 
        this.executor.execute(texFile.getParentFile(), // workingDir 
			      this.settings.getTexPath(), 
			      command, 
			      args);

	// logging errors (warnings are done in processLatex2pdf)
	logErrs(desc.logFile, command);
    }

    /**
     * Runs the tex4ht command given by {@link Settings#getTex4htCommand()} 
     * on <code>texFile</code> 
     * in the directory containing <code>texFile</code> 
     * with arguments given by {@link #buildHtlatexArguments(String, File)}. 
     * FIXME: document about errors and warnings. 
     *
     * @param texFile
     *    the latex-file to be processed. 
     * @throws BuildExecutionException
     *    if running the tex4ht command 
     *    returned by {@link Settings#getTex4htCommand()} failed. 
     */
    private void runLatex2html(LatexMainDesc desc)
	throws BuildExecutionException {

	File texFile = desc.texFile;
	String command = this.settings.getTex4htCommand();
        log.debug("Running " + command + " on '" + texFile.getName() + "'. ");

        String[] args = buildHtlatexArguments(texFile);
	// may throw BuildExecutionException 
        this.executor.execute(texFile.getParentFile(), // workingDir 
			      this.settings.getTexPath(), 
			      command, 
			      args);

	// logging errors and warnings 
	logErrs (desc.logFile, command);
	logWarns(desc.logFile, command);
    }

    private String[] buildHtlatexArguments(File texFile)
	throws BuildExecutionException {

        return new String[] {
	    texFile.getName(),
	    this.settings.getTex4htStyOptions(),
	    this.settings.getTex4htOptions(),
	    this.settings.getT4htOptions(),
	    this.settings.getLatex2pdfOptions()
	};
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
        log.debug("Running " + command + " on '" + texFile.getName() + "'. ");

        String[] args = buildArguments(this.settings.getLatex2rtfOptions(), 
				       texFile);
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
    private void runLatex2odt(LatexMainDesc desc)
	throws BuildExecutionException {

	File texFile = desc.texFile;
	String command = this.settings.getTex4htCommand();
        log.debug("Running " + command + " on '" + texFile.getName() + "'. ");

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
	logErrs (desc.logFile, command);
	logWarns(desc.logFile, command);
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
    private void runOdt2doc(File texFile)
            throws BuildExecutionException {

	File odtFile = this.fileUtils.replaceSuffix(texFile, SUFFIX_ODT);
	String command = this.settings.getOdt2docCommand();
	log.debug("Running " + command + " on '" + odtFile.getName() + "'. ");

	String[] args = buildArguments(this.settings.getOdt2docOptions(),
				       odtFile);

	// may throw BuildExecutionException 
	this.executor.execute(texFile.getParentFile(), 
			      this.settings.getTexPath(), 
			      command, 
			      args);
 	// FIXME: what about error logging? 
	// Seems not to create a log-file. 
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
	log.debug("Running " + command + " on '" + pdfFile.getName() + "'. ");

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
 }
