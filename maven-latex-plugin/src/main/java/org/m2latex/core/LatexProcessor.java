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
 * The main method is {@link #execute()} which is executed by the ant task 
 * and by the maven plugin. 
 * It does preprocessing of the graphic files 
 * and main processing of the latex file according to the target(s) 
 * given by the parameters. 
 */
public class LatexProcessor {

    // For pattern matching, it is vital to avoid false positive matches. 
    // e.g. Overfull \box with text '! ' is not an error 
    // and Overfull \box with text 'Warning' is no warning. 

    static final String PATTERN_NEED_BIBTEX_RUN = "^\\bibdata";

    // Note that two \\ represent a single \ in the string. 
    // Thus \\\\ represents '\\' in the pattern, 
    // which in turn represents a single \. 
    static final String PATTERN_OUFULL_HVBOX = 
	"^(Ov|Und)erfull \\\\[hv]box \\(";

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
	     new TexFileUtilsImpl(log, settings), paramAdapt);
    }

    /**
     * Executes the ant-task or the maven plugin. 
     * This consists in reading the parameters 
     * via {@link ParameterAdapter#initialize()} 
     * copying LaTeX source directory recursively into a temporary folder, 
     * processing xfig-files via {@link #runFig2Dev(File)} 
     * and processing the tex main files 
     * via {@link ParameterAdapter#processSource(File)}. 
     * The resulting files are identified by its suffixes 
     * via  {@link ParameterAdapter#getOutputFileSuffixes()} 
     * and copied to the target folder. 
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
    public void execute() 
	throws BuildExecutionException, BuildFailureException {

        this.paramAdapt.initialize();
        this.log.debug("Settings: " + this.settings.toString() );

        File texDirectory = this.settings.getTexSrcDirectoryFile();

        if (!texDirectory.exists()) {
            this.log.info("No tex directory - skipping LaTeX processing");
            return;
        }

	try {
	    File tempDir = this.settings.getTempDirectoryFile();
	    // copy sources to tempDir 
	    // may throw BuildExecutionException 
	    this.fileUtils.copyLatexSrcToTempDir(texDirectory, tempDir);

	    // process xfig files 
	    Collection<File> figFiles = this.fileUtils
		.getXFigDocuments(tempDir);
	    for (File figFile : figFiles) {
		this.log.info("Processing fig-file " + figFile + ". ");
		// may throw BuildExecutionException 
		runFig2Dev(figFile, LatexDev.pdf);
	    }

	    // process gnuplot files 
	    Collection<File> pltFiles = this.fileUtils
		.getGnuplotDocuments(tempDir);
	    for (File pltFile : pltFiles) {
		this.log.info("Processing gnuplot-file " + pltFile + ". ");
		// may throw BuildExecutionException 
		runGnuplot2Dev(pltFile, LatexDev.pdf);
	    }

	    // process latex main files 
	    // may throw BuildExecutionException 
	    Collection<File> latexMainFiles = this.fileUtils
		.getLatexMainDocuments(tempDir);
	    for (File texFile : latexMainFiles) {
		// may throw BuildExecutionException, BuildFailureException 
		File targetDir = this.fileUtils.getTargetDirectory
		    (texFile, 
		     tempDir, 
		     this.settings.getOutputDirectoryFile());

		for (Target target : this.paramAdapt.getTargetSet()) {
		    // may throw BuildExecutionException 
		    target.processSource(this, texFile);
		    FileFilter fileFilter = this.fileUtils
			.getFileFilter(texFile, 
				       target.getOutputFileSuffixes());
		    // may throw BuildExecutionException, BuildFailureException
		    this.fileUtils.copyOutputToTargetFolder(fileFilter,
							    texFile,
							    targetDir);

		} // target 
	    } // texFile 
	} finally {
	    if ( this.settings.isCleanUp() ) {
                this.fileUtils.cleanUp();
            }
        }
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
     * Runs LaTeX on <code>texFile</code> at least once, 
     * runs BibTeX and MakeIndex by need 
     * and reruns latex as long as needed to get all links 
     * or as threshold {@link Settings#maxNumReruns} specifies. 
     * <p>
     * The result of the LaTeX run is typically some pdf-file, 
     * but it is also possible to specify the dvi-format 
     * (no longer recommended but still working). 
     * <p>
     * A warning is logged if 
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
     * A warning is logged each time a LaTeX run fails, 
     * a BibTeX run or MakeIndex fails 
     * or if a BibTeX run or a MakeIndex run issues a warning
     * in the according methods {@link #runLatex2pdf(File)}, 
     * {@link #runBibtex(File)} and {@link #runMakeindex(File)}. 
     *
     * @param texFile
     *    the tex file to be processed. 
     * @see #runLatex2pdf(File)
     * @see #runBibtex(File)
     * @see #runMakeindex(File)
     * @see #needAnotherLatexRun(File)
     * @see #needBibtexRun(File)
     */
    // FIXME: what about glossaries and things like that? 
    public void processLatex2pdf(final File texFile)
	throws BuildExecutionException {

        log.info("Processing LaTeX file " + texFile + ". ");

	// initial latex run 
        runLatex2pdf(texFile);

	// run bibtex by need 
	boolean needLatexReRun = runBibtexByNeed(texFile);

	// run makeindex by need 
	needLatexReRun |= runMakeindexByNeed(texFile);

	// rerun LaTeX at least once if bibtex or makeindex had been run 
	// or if a toc, a lof or a lot exists. 
	needLatexReRun |= this.fileUtils.replaceSuffix(texFile, "toc").exists();
	needLatexReRun |= this.fileUtils.replaceSuffix(texFile, "lof").exists();
	needLatexReRun |= this.fileUtils.replaceSuffix(texFile, "lot").exists();
	if (needLatexReRun) {
	    log.debug("Rerun LaTeX to update table of contents, ... " + 
		      " including bibliography, index, or that like. ");
	    runLatex2pdf(texFile);
	}

	// rerun latex by need 
        int retries = 0;
	int maxNumReruns = this.settings.getMaxNumReruns();
	File logFile = this.fileUtils.replaceSuffix(texFile, "log");
        while ((maxNumReruns == -1 || retries < maxNumReruns)
	       && (needLatexReRun = needAnotherLatexRun(logFile))) {
            log.debug("Latex must be rerun. ");
            runLatex2pdf(texFile);
            retries++;
        }
	if (needLatexReRun) {
	    log.warn("Max rerun reached although LaTeX demands another run. ");
	}
	// emit warnings 
	if (this.settings.getDebugBadBoxes() && 
	    this.fileUtils.matchInFile(logFile, PATTERN_OUFULL_HVBOX)) {
	    log.warn("LaTeX created bad boxes. ");
	}
	if (this.settings.getDebugWarnings() && 
	    this.fileUtils
	    .matchInFile(logFile, 
			 this.settings.getPatternNeedLatexReRun())) {
	    log.warn("LaTeX emited warnings. ");
	}
    }


    /**
     * Runs conversion of <code>texFile</code> to html or xhtml 
     * after processing latex to set up the references, 
     * bibliography, index and that like. 
     *
     * @param texFile
     *    the tex file to be processed. 
     * @see #processLatex2pdf(File)
     * @see #runLatex2html(File)
     */
    public void processLatex2html(File texFile)
	throws BuildExecutionException {
	log.info("Processing LaTeX file " + texFile + ". ");
	//processLatex2pdf(texFile);
        runLatex2html   (texFile);
    }

    /**
     * Runs conversion of <code>texFile</code> 
     * to odt or other open office formats 
     * after processing latex to set up the references, 
     * bibliography, index and that like. 
     *
     * @param texFile
     *    the tex file to be processed. 
     * @see #processLatex2pdf(File)
     * @see #runLatex2odt(File)
     */
    public void processLatex2odt(File texFile) throws BuildExecutionException {
	log.info("Processing LaTeX file " + texFile + ". ");
        //processLatex2pdf(texFile);
        runLatex2odt    (texFile);
    }

    /**
     * Runs conversion of <code>texFile</code> 
     * to docx or other MS word formats 
     * after processing latex to set up the references, 
     * bibliography, index and that like. 
     *
     * @param texFile
     *    the tex file to be processed. 
     * @see #processLatex2pdf(File)
     * @see #runOdt2doc(File)
     */
    public void processLatex2docx(File texFile) throws BuildExecutionException {
	log.info("Processing LaTeX file " + texFile + ". ");
        //processLatex2pdf(texFile);
        runLatex2odt    (texFile);
        runOdt2doc      (texFile);
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
     * @see #processLatex2pdf(File)
     * @see #runLatex2rtf(File)
     */
    public void processLatex2rtf(File texFile) throws BuildExecutionException {
	log.info("Processing LaTeX file " + texFile + ". ");
	runLatex2rtf(texFile);
    }

    /**
     * Runs direct conversion of <code>texFile</code> to txt format via pdf. 
     *
     * @param texFile
     *    the tex file to be processed. 
     * @see #processLatex2pdf(File)
     * @see #runPdf2txt(File)
     */
    public void processLatex2txt(File texFile) throws BuildExecutionException {
        processLatex2pdf(texFile);
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
     */
    public void runGnuplot2Dev(File pltFile, LatexDev dev) 
	throws BuildExecutionException {
	String command = "gnuplot";
	File pdfFile = this.fileUtils.replaceSuffix(pltFile, "pdf");
	File ptxFile = this.fileUtils.replaceSuffix(pltFile, "ptx");

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
     * @see AbstractLatexMojo#execute()
     */
    // used in AbstractLatexMojo.execute() only 
    public void runFig2Dev(File figFile, LatexDev dev) 
	throws BuildExecutionException {

	String command = this.settings.getFig2devCommand();
	File workingDir = figFile.getParentFile();
	String[] args;

	//File pdfFile   = this.fileUtils.replaceSuffix(figFile, "pdf");
	File pdf_tFile = this.fileUtils.replaceSuffix(figFile, "ptx");

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

	// logging errors 
	logErrs(texFile, command);

	// missing: warnings: should be displayed configurable. 
    }

    private String[] buildHtlatexArguments( File texFile )
            throws BuildExecutionException
    {
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
     * Logs a warning if the latex run failed 
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
	logErrs(texFile, command);

	// Maybe missing: warnings and bad boxes; 
	// should be displayed configurable. 
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

	File odtFile = this.fileUtils.replaceSuffix(texFile, "odt");
	String command = this.settings.getOdt2docCommand();
	log.debug( "Running " + command + 
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

	File pdfFile = this.fileUtils.replaceSuffix(texFile, "pdf");
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
     * @see Settings#getPatternNeedLatexReRun()
     */
    private boolean needAnotherLatexRun(File logFile)
	throws BuildExecutionException {
        String reRunPattern = this.settings.getPatternNeedLatexReRun();
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
    private boolean runMakeindexByNeed(File texFile)
	throws BuildExecutionException {

	File idxFile = this.fileUtils.replaceSuffix(texFile, "idx");
	boolean needRun = idxFile.exists();
	log.debug("MakeIndex run required? " +needRun);
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

	// detect errors 
 	File logFile = this.fileUtils.replaceSuffix( idxFile, "ilg" );
	if (logFile.exists()) {
	    boolean errOccurred = this.fileUtils.matchInFile
		(logFile, this.settings.getPatternErrMakeindex());
	    if (errOccurred) {
		log.warn("Running MakeIndex on " + idxFile + 
			 " failed. For details see " + 
			 logFile.getName() + ". ");
	    }
	    boolean warnOccurred = this.fileUtils.matchInFile
		(logFile, this.settings.getPatternWarnMakeindex());
	    if (warnOccurred) {
		log.warn("Running MakeIndex on " + idxFile + 
			 " emitted warnings. For details see " + 
			 logFile.getName() + ". ");
	    }
	} else {
	    this.log.error("Makeindex failed: no log file found. ");
	}
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

	File auxFile =  this.fileUtils.replaceSuffix(texFile, "aux");
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

	File logFile = this.fileUtils.replaceSuffix( texFile, "blg" );
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
     * Logs a warning if the latex run failed 
     * but not if bad boxes occurred or if warnings occurred. 
     * This is done in {@link #processLatex2pdf(File)} 
     * after the last LaTeX run only. 
     *
     * @param texFile
     *    the latex file to be processed. 
     * @throws BuildExecutionException
     *    if running the latex command 
     *    returned by {@link Settings#getLatexCommand()} failed. 
     */
    private void runLatex2pdf(File texFile)
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

	// logging errors 
	logErrs(texFile, command);
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
    private void logErrs(File texFile, String command) 
	throws BuildExecutionException {

	File logFile = this.fileUtils.replaceSuffix(texFile, "log");
	if (logFile.exists()) {
	    // matchInFile may throw BuildExecutionException
	    boolean errorOccurred = this.fileUtils
		.matchInFile(logFile, this.settings.getPatternErrLatex());
	    if (errorOccurred) {
		log.warn("Running " + command + " on " + texFile.getName() + 
			 " failed. For details see " + 
			 logFile.getName() + ". ");
	    }
	} else {
	    this.log.error("Running " + command + " on " + texFile.getName() + 
			   " failed: no log file found. ");
	}
    }
}


