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

// idea: use latex2rtf and unoconv
// idea: targets for latex2html, latex2man, latex2png and many more. 
/**
 * The latex processor used by both the ant task and the maven plugin. 
 * This is the core class of this piece of software. 
 */
public class LatexProcessor
{

    private final Settings settings;

    private final CommandExecutor executor;

    private final LogWrapper log;

    private final TexFileUtils fileUtils;

    private final ParameterAdapter paramAdapt;

    // for tests only
    LatexProcessor(Settings settings, 
		   CommandExecutor executor, 
		   LogWrapper log, 
		   TexFileUtils fileUtils,
		   ParameterAdapter paramAdapt)
    {
        this.settings = settings;
        this.log = log;
        this.executor = executor;
        this.fileUtils = fileUtils;
	this.paramAdapt = paramAdapt;
    }

    /**
     * Creates a LatexProcessor with parameters given by <code>settings</code> 
     * which logs onto <code>log</code> and used by <code>paramAdapt</code>. 
     */
    public LatexProcessor( Settings settings, 
			   LogWrapper log, 
			   ParameterAdapter paramAdapt)
    {
        this.settings = settings;
        this.log = log;
        this.executor = new CommandExecutorImpl(this.log);
        this.fileUtils = new TexFileUtilsImpl( this.log, this.settings );
	this.paramAdapt = paramAdapt;
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
     */
    public void execute() 
	throws BuildExecutionException, BuildFailureException {
        this.paramAdapt.initialize();
        this.log.debug("Settings: " + this.settings.toString() );

        File texDirectory = this.settings.getTexSrcDirectoryFile();

        if ( !texDirectory.exists() ) {
            this.log.info( "No tex directory - skipping LaTeX processing" );
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
		this.log.info("Processing " + figFile + ". ");
		// may throw BuildExecutionException 
		runFig2Dev(figFile);
	    }

	    // process latex main files 
	    // may throw BuildExecutionException 
	    Collection<File> latexMainFiles = this.fileUtils
		.getLatexMainDocuments(tempDir);
	    for (File texFile : latexMainFiles) {
		// may throw BuildExecutionException  
		this.paramAdapt.processSource(texFile);
		// may throw BuildExecutionException, BuildFailureException 
		File targetDir = this.fileUtils.getTargetDirectory
		    (texFile, 
		     tempDir, 
		     this.settings.getOutputDirectoryFile());
		FileFilter fileFilter = this.fileUtils
		    .getFileFilter(texFile, 
				   this.paramAdapt.getOutputFileSuffixes());
		// may throw BuildExecutionException, BuildFailureException
		this.fileUtils.copyOutputToTargetFolder(fileFilter,
							texFile,
							targetDir);
	    }
	} finally {
	    if ( this.settings.isCleanUp() ) {
                this.fileUtils.cleanUp();
            }
        }
    }



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
     * A warning is logged if a LaTeX run, a BibTeX run or MakeIndex fails 
     * or if BibTeX run or a MakeIndex run issues a warning
     * in the according methods {@link #runLatex(File)}, 
     * {@link #runBibtex(File)} and {@link #runMakeindex(File)}. 
     *
     * @param texFile
     *    the tex file to be processed. 
     * @see #runLatex(File)
     * @see #runBibtex(File)
     * @see #runMakeindex(File)
     * @see #needAnotherLatexRun(File)
     * @see #needBibtexRun(File)
     */
    // FIXME: what about glossaries and things like that? 
    public void processLatex2pdf(final File texFile)
	throws BuildExecutionException
    {
        log.info("Processing LaTeX file " + texFile + ". ");

        runLatex( texFile );
	File logFile = this.fileUtils.replaceSuffix(texFile, "log");
        if ( needBibtexRun( logFile ) )
        {
            log.debug("Bibtex must be run. ");
            runBibtex( texFile );
        }

	File idxFile = this.fileUtils.replaceSuffix(texFile, "idx");
	if ( idxFile.exists() )
        {
            log.debug("Makeindex must be run. ");
            runMakeindex( idxFile );
        }

        int retries = 0;
	boolean needAnotherLatexRun = true;
	int maxNumReruns = this.settings.getMaxNumReruns();
        while ((maxNumReruns == -1 || retries < maxNumReruns)
	       && (needAnotherLatexRun = needAnotherLatexRun( logFile )) )
        {
            log.debug("Latex must be rerun. ");
            runLatex( texFile );
            retries++;
        }
	if (needAnotherLatexRun) {
	    log.warn("Max rerun reached although " + texFile +
		     " needs another run. ");
	}

	if (this.settings.getDebugBadBoxes() && 
	    this.fileUtils.matchInLogFile(logFile, 
					  "(Und|Ov)erful \\[hv]box")) {
	    log.warn("Bad Boxes in " + texFile + ". ");
	}
	if (this.settings.getDebugWarnings() && 
	    this.fileUtils.matchInLogFile(logFile, 
					  "Warning ")) {
	    log.warn("Warnings running LaTeX on " + texFile + ". ");
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
    public void processLatex2html( File texFile )
            throws BuildExecutionException 
    {
        processLatex2pdf(texFile);
        runLatex2html     (texFile);
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
    public void processLatex2odt(File texFile) throws BuildExecutionException
    {
        processLatex2pdf( texFile );
        runLatex2odt( texFile );
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
    public void processLatex2docx(File texFile) throws BuildExecutionException
    {
        processLatex2pdf(texFile);
        runLatex2odt(texFile);
        runOdt2doc(texFile);
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
     * Runs direct conversion of <code>texFile</code> to txt format 
     * via pdf. 
     *
     * @param texFile
     *    the tex file to be processed. 
     * @see #processLatex2pdf(File)
     * @see #runPdf2txt(File)
     */
   public void processLatex2txt(File texFile) throws BuildExecutionException {
        processLatex2pdf(texFile);
	runPdf2txt(texFile);
    }

    private boolean update(File source, File target)
    {
	if (!target.exists()) {
	    return true;
	}
	assert source.exists();

	return source.lastModified() > target.lastModified();
    }

    /**
     * Runs fig2dev on fig-files to generate pdf and pdf_t files. 
     * This is a quite restricted usage of fig2dev. 
     *
     * @param figFile
     *    the fig file to be processed. 
     * @see AbstractLatexMojo#execute()
     */
    // used in AbstractLatexMojo.execute() only 
    public void runFig2Dev(File figFile) throws BuildExecutionException {
	String command = this.settings.getFig2devCommand();
	log.debug( "Running " + command + 
		   " on file " + figFile.getName() + ". ");
	File workingDir = figFile.getParentFile();
	String[] args;

	File pdfFile   = this.fileUtils.replaceSuffix(figFile, "pdf");
	File pdf_tFile = this.fileUtils.replaceSuffix(figFile, "pdf_t");

	String pdf   = pdfFile  .toString();
	String pdf_t = pdf_tFile.toString();

	if (update(figFile, pdfFile)) {
	    args = new String[] {
		"-L", // language 
		"pdftex",
		figFile.getName(), // source 
		pdf // target 
	    };
	    // may throw BuildExecutionException 
	    this.executor.execute(workingDir, 
				  this.settings.getTexPath(), //**** 
				  command, 
				  args);
	}

	if (update(figFile, pdf_tFile)) {
	    args = new String[] {
		"-L",// language 
		"pdftex_t",
		"-p",// portrait (-l for landscape), next argument ignored 
		pdf,
		figFile.getName(), // source 
		pdf_t // target 
	    };
	    // may throw BuildExecutionException 
	    this.executor.execute(workingDir, 
				  this.settings.getTexPath(), //**** 
				  command, 
				  args);
	}
	// no check: just warning that no output has been created. 
    }

    /**
     * Runs the latex2rtf command 
     * given by {@link Settings#getLatex2rtfCommand()} 
     * on <code>texFile</code> 
     * in the directory containing <code>texFile</code> 
     * with arguments given by {@link #buildLatex2rtfArguments(File)}. 
     */
    private void runLatex2rtf( File texFile )
            throws BuildExecutionException
    {
	String command = this.settings.getLatex2rtfCommand();
        log.debug( "Running " +command + 
		   " on file " + texFile.getName() + ". ");

        File workingDir = texFile.getParentFile();
        String[] args = buildLatex2rtfArguments( texFile );
	// may throw BuildExecutionException 
        this.executor.execute(workingDir, 
			      this.settings.getTexPath(), 
			      command, 
			      args);

	// FIXME: no check: just warning that no output has been created. 
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
     * with arguments given by {@link #buildHtlatexArguments(File)}. 
     */
    private void runLatex2html( File texFile )
            throws BuildExecutionException
    {
	String command = this.settings.getTex4htCommand();
        log.debug( "Running " + command + 
		   " on file " + texFile.getName() + ". ");

        File workingDir = texFile.getParentFile();
        String[] args = buildHtlatexArguments( texFile );
	// may throw BuildExecutionException 
        this.executor.execute(workingDir, 
			      this.settings.getTexPath(), 
			      command, 
			      args);

	File logFile = this.fileUtils.replaceSuffix( texFile, "log" );
	boolean errorOccurred = this.fileUtils
	    .matchInLogFile(logFile, this.settings.getPatternErrLatex());
	if (errorOccurred) {
	    log.warn("LaTeX failed to run on " + texFile + ". ");
	}
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
     * with arguments given by {@link #buildLatexArguments(File)}. 
     * <p>
     * Logs a warning if the latex run failed 
     * but not if bad boxes ocurred or if warnings occurred. 
     * This is done in {@link #processLatex2pdf(File)} 
     * after the last LaTeX run only. 
     */
    private void runLatex2odt( File texFile)
            throws BuildExecutionException
    {
	String command = this.settings.getTex4htCommand();
        log.debug( "Running " + command + 
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
	File logFile = this.fileUtils.replaceSuffix( texFile, "log" );
	if (logFile.exists()) {
	    boolean errorOccurred = this.fileUtils
		.matchInLogFile(logFile, this.settings.getPatternErrLatex());
	    if (errorOccurred) {
		log.warn("LaTeX failed when running on " + texFile + 
			 ". For details see " + logFile.getName() + ". ");
	    }
	} else {
	    this.log.error("LaTeX failed: no log file found. ");
	}

	// Maybe missing: warnings and bad boxes; 
	// should be displayed configurable. 
    }


    // FIXME: missing options. 
    // above all doctype: -ddoc, -ddocx 
    // available: odt2doc --show. 
    // among those also: latex and rtf !!!!!! 
    // This is important to define the copy filter accordingly 
    private void runOdt2doc( File texFile)
            throws BuildExecutionException
    {
	File odtFile = this.fileUtils.replaceSuffix(texFile, "odt");
	String command = this.settings.getOdt2docCommand();
	log.debug( "Running " + command + 
		   " on file " + odtFile.getName() + ". ");

	String[] args = new String[] {odtFile.getName()};
	// may throw BuildExecutionException 
	this.executor.execute(texFile.getParentFile(), 
			      this.settings.getTexPath(), 
			      command, 
			      args);
    }

    private void runPdf2txt( File texFile)
            throws BuildExecutionException
    {
	File pdfFile = this.fileUtils.replaceSuffix(texFile, "pdf");
	String command = this.settings.getPdf2txtCommand();
	log.debug( "Running " + command + 
		   " on file " + pdfFile.getName() + ". ");

	String[] args = new String[] {pdfFile.getName()};
	// may throw BuildExecutionException 
	this.executor.execute(texFile.getParentFile(), 
			      this.settings.getTexPath(), 
			      command, 
			      args);
    }

    // FIXME: Is this the right criterion? 
    private boolean needAnotherLatexRun(File logFile)
	throws BuildExecutionException
    {
        String reRunPattern = this.settings.getPatternNeedAnotherLatexRun();
        boolean needRun =  this.fileUtils.matchInLogFile(logFile, reRunPattern);
        log.debug( "Another Latex run? " + needRun );
        return needRun;
    }

    // FIXME: the right criterion? 
    private boolean needBibtexRun(File logFile)
	throws BuildExecutionException
    {
        String namePrefixLogFile = this.fileUtils
	    .getFileNameWithoutSuffix(logFile);
        String pattern = "No file " + namePrefixLogFile + ".bbl";
        return this.fileUtils.matchInLogFile( logFile, pattern );
    }


    private void runMakeindex(File idxFile)
	throws BuildExecutionException
    {
	log.debug( "Running " + this.settings.getMakeIndexCommand() + 
		   " on file " + idxFile.getName() + ". ");

	File workingDir = idxFile.getParentFile();
	String[] args = new String[] {idxFile.getName()};
	// may throw BuildExecutionException 
	this.executor.execute(workingDir, 
			      this.settings.getTexPath(), 
			      this.settings.getMakeIndexCommand(), 
			      args);

	// detect errors 
 	File logFile = this.fileUtils.replaceSuffix( idxFile, "ilg" );
	if (logFile.exists()) {
	    boolean errOccurred = this.fileUtils.matchInLogFile
		(logFile, this.settings.getPatternErrMakeindex());
	    if (errOccurred) {
		log.warn("MakeIndex failed when running on " + idxFile + 
			 ". For details see " + logFile.getName() + ". ");
	    }
	    // FIXME: what about warnings? 
	} else {
	    this.log.error("Makeindex failed: no log file found. ");
	}
    }

     /**
     * Runs the bibtex command given by {@link Settings#getBibtexCommand()} 
     * on the aux-file corresponding with <code>texFile</code> 
     * in the directory containing <code>texFile</code>. 
     */
    private void runBibtex(File texFile)
	throws BuildExecutionException
    {
	File auxFile =  this.fileUtils.replaceSuffix( texFile, "aux" );
        log.debug( "Running BibTeX on file " + auxFile.getName() + ". ");

        File workingDir = texFile.getParentFile();
        String[] args = new String[] {
	    auxFile.getName()
	};
	// may throw BuildExecutionException 
        this.executor.execute(workingDir, 
			      this.settings.getTexPath(), 
			      this.settings.getBibtexCommand(), 
			      args);

	File logFile = this.fileUtils.replaceSuffix( texFile, "blg" );
	if (logFile.exists()) {
	    // FIXME: Could be further improved: 1 error but more warnings: 
	    // The latter shall be displayed. (maybe)
	    boolean errOccurred = this.fileUtils
		.matchInLogFile(logFile, "Error");
	    if (errOccurred) {
		log.warn("BibTeX failed when running on " + texFile + ". ");
	    }
	    boolean warnOccurred = this.fileUtils
		.matchInLogFile(logFile, "Warning");
	    if (warnOccurred) {
		log.warn("BibTeX warning when running on " + texFile + ". ");
	    }
	    if (errOccurred || warnOccurred) {
		log.warn("For details see " + logFile.getName() + ". ");
	    }
	} else {
	    this.log.error("BibTeX failed: no log file found. ");
	}
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
     */
    private void runLatex(File texFile)
	throws BuildExecutionException
    {
        log.debug("Running " + settings.getTexCommand() + 
		  " on file " + texFile.getName() + ". ");

        File workingDir = texFile.getParentFile();
	String[] args = buildLatexArguments( texFile );
	// may throw BuildExecutionException 
        this.executor.execute(workingDir, 
			      this.settings.getTexPath(), 
			      this.settings.getTexCommand(), 
			      args);

	// logging errors 
	File logFile = this.fileUtils.replaceSuffix( texFile, "log" );
	if (logFile.exists()) {
	    // matchInLogFile may throw BuildExecutionException
	    boolean errorOccurred = this.fileUtils
		.matchInLogFile(logFile, this.settings.getPatternErrLatex());
	    if (errorOccurred) {
		log.warn("LaTeX conversion to odt failed when running on " 
			 + texFile + 
			 ". For details see " + logFile.getName() + ". ");
	    }
	} else {
	    this.log.error("LaTeX conversion to odt failed: " + 
			   "no log file found. ");
	}
    }

    // used in runLatex only 
    private String[] buildLatexArguments( File texFile )
    {
        String[] texCommandArgs = this.settings.getTexCommandArgs().split(" ");
        String[] args = new String[texCommandArgs.length + 1];
        System.arraycopy( texCommandArgs, 0, args, 0, texCommandArgs.length );
        args[texCommandArgs.length] = texFile.getName();
	return args;
     }
}


