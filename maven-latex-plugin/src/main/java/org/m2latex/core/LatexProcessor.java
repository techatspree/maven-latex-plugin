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

import org.m2latex.antTask.LatexCfgTask;
import org.m2latex.antTask.LatexClrTask;

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
 * The elements of the enumeration {@link Target} use methods 
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

    // bibtex 
    final static String SUFFIX_BLG = ".blg";
    final static String SUFFIX_BBL = ".bbl";

    // makeindex for index 
    // unsorted and not unified index created by latex 
    final static String SUFFIX_IDX = ".idx";
    // sorted and unified index created by makeindex 
    final static String SUFFIX_IND = ".ind";
    // log file created by makeindex 
    final static String SUFFIX_ILG = ".ilg";

    // makeindex for glossary 
    // needed by makeglossaries 
    final static String SUFFIX_VOID = "";
    // unsorted and not unified glossary created by latex 
    final static String SUFFIX_GLO = ".glo";
    // sorted and unified glossary created by makeindex 
    final static String SUFFIX_GLS = ".gls";
    // logging file for makeindex used with glossaries 
    final static String SUFFIX_GLG = ".glg";

    // latex2rtf 
    private final static String SUFFIX_RTF = ".rtf";

    // odt2doc 
    private final static String SUFFIX_ODT = ".odt";

    // tex4ht 
    // FIXME: works for unix only 
    final static String SUFFIX_HTML = ".html";

    // pdftotext 
    private final static String SUFFIX_TXT = ".txt";

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
     * Defines creational ant-task defined in {@link LatexCfgTask} 
     * and the according goals in {@link CfgLatexMojo} 
     * and subclasses of the maven plugin. 
     * <p>
     * This consists in reading the parameters 
     * via {@link ParameterAdapter#initialize()} 
     * processing graphic-files 
     * via {@link LatexPreProcessor#processGraphicsSelectMain(File, DirNode)} 
     * and processing the tex main files 
     * via {@link Target#processSource(LatexProcessor, File)}. 
     * The resulting files are identified by its suffixes 
     * via  {@link Target#getPatternOutputFiles(Settings)} 
     * and copied to the target folder. 
     * Finally, by default a cleanup is performed 
     * invoking {@link TexFileUtils#cleanUp(DirNode, File)}. 
     * <p>
     * Logging: 
     * <ul>
     * <li> WFU01: Cannot read directory... 
     * <li> WFU03: cannot close 
     * <li> WFU05: Cannot delete... 
     * <li> WPP02: tex file may be latex main file 
     * <li> WPP03: Skipped processing of files with suffixes ... 
     * <li> WEX01, WEX02, WEX03, WEX04, WEX05: 
     *      applications for preprocessing graphic files 
     *      or processing a latex main file fails. 
     * </ul>
     *
     * FIXME: exceptions not really clear. 
     * @throws BuildFailureException 
     *    <ul>
     *    <li> TSS01 if 
     *    the tex source directory does either not exist 
     *    or is not a directory. 
     *    <li> TEX01 if 
     *    invocation of applications for preprocessing graphic files 
     *    or processing a latex main file fails 
     *    <li> TFU01 if 
     *    the target directory that would be returned 
     *    exists already as a regular file. 
     *    <li> TSS02 if 
     *    the output directory exists and is no directory. 
     *    <li> TFU03, TFU04, TFU05, TFU06 if 
     *    copy of output files to target folder fails. 
     *    For details see 
     * {@link TexFileUtilsImpl#copyOutputToTargetFolder(File, FileFilter, File)}
     *    </ul>
     */
    public void create() throws BuildFailureException {

        this.paramAdapt.initialize();
        this.log.debug("Settings: " + this.settings.toString() );

	// may throw BuildFailureException TSS01 
	File texDir = this.settings.getTexSrcDirectoryFile();
	assert texDir.exists() && texDir.isDirectory();

	// constructor DirNode may log warning WFU01 Cannot read directory 
	DirNode node = new DirNode(texDir, this.fileUtils);

	try {
	    // process graphics and determine latexMainFiles 
	    // may throw BuildFailureException TEX01, 
	    // log warning WFU03, WPP02, WPP03, 
	    // WEX01, WEX02, WEX03, WEX04, WEX05  
	    Collection<File> latexMainFiles = this.preProc
		.processGraphicsSelectMain(texDir, node);

	    for (File texFile : latexMainFiles) {
		// throws BuildFailureException TFU01 
		// if targetDir would be an existing non-directory 
		File targetDir = this.fileUtils.getTargetDirectory
		    (texFile, 
		     texDir,
		     // throws BuildFailureException TSS02 
		     // if exists and is no dir 
		     this.settings.getOutputDirectoryFile());
		assert !targetDir.exists() || targetDir.isDirectory();

		for (Target target : this.paramAdapt.getTargetSet()) {
		    // may throw BuildFailureException TEX01, 
		    // log warning WEX01, WEX02, WEX03, WEX04, WEX05 
		    target.processSource(this, texFile);
		    FileFilter fileFilter = this.fileUtils.getFileFilter
			(texFile, target.getPatternOutputFiles(this.settings));
		    // may throw BuildFailureException 
		    // TFU03, TFU04, TFU05, TFU06 
		    // may log warning WFU01 Cannot read directory 
		    // FIXME: fileFilter shall not accept directories 
		    // and shall not accept texFile 
		    this.fileUtils.copyOutputToTargetFolder(texFile,
							    fileFilter,
							    targetDir);
		} // target 
	    } // texFile 
	} finally {
	    if (this.settings.isCleanUp()) {
		// may log warning WFU01, WFU05 
		this.fileUtils.cleanUp(node, texDir);
            }
        }
    }

    /**
     * Defines graphics goal of the maven plugin in {@link GraphicsMojo}. 
     * <p>
     * Logging: 
     * <ul>
     * <li> WFU01: Cannot read directory 
     * <li> WFU03: cannot close 
     * <li> WPP02: tex file may be latex main file 
     * <li> WPP03: Skipped processing of files with suffixes ... 
     * <li> WEX01, WEX02, WEX03, WEX04, WEX05: 
     * if running graphic processors failed. 
     * </ul>
     *
     * @throws BuildFailureException
     *    <ul>
     *    <li> 
     *    TSS01 if the tex source directory does either not exist 
     *    or is not a directory. 
     *    <li> 
     *    TEX01 invoking FIXME
     *    </ul>
     */
    public void processGraphics() throws BuildFailureException {
	// may throw BuildFailureException TSS01 
	File texDir = this.settings.getTexSrcDirectoryFile();
	assert texDir.exists() && texDir.isDirectory();

	// constructor DirNode may log warning WFU01 Cannot read directory 
 	DirNode node = new DirNode(texDir, this.fileUtils);
	// may throw BuildFailureException TEX01, 
	// log warning WFU03, WPP02, WPP03, 
	// WEX01, WEX02, WEX03, WEX04, WEX05  
	this.preProc.processGraphicsSelectMain(texDir, node);
    }

    /**
     * Defines clearing ant-task defined in {@link LatexClrTask} 
     * and the according goal in {@link ClearMojo} of the maven plugin. 
     * Consists in clearing created graphic files 
     * and created files derived from latex main file. 
     * <p>
     * The parameters this method depends on are (currently): 
     * <ul>
     * <li>
     * {@link Settings#getTexSrcDirectoryFile()}
     * <li>
     * {@link Settings#getPatternLatexMainFile()}
     * <li>
     * {@link Settings#getPatternClearFromLatexMain()}
     * </ul>
     * <p>
     * Logging: 
     * <ul>
     * <li> WPP02: tex file may be latex main file 
     * <li> WFU01: Cannot read directory...
     * <li> WFU03: cannot close tex file 
     * <li> WFU05: Failed to delete file 
     * </ul>
     *
     * @throws BuildFailureException 
     *    TSS01 if the tex source directory does either not exist 
     *    or is not a directory. 
     */
    public void clearAll() throws BuildFailureException {
        this.paramAdapt.initialize();
        this.log.debug("Settings: " + this.settings.toString());

	// may throw BuildFailureException TSS01 
        File texDir = this.settings.getTexSrcDirectoryFile();
	assert texDir.exists() && texDir.isDirectory();

	// constructor DirNode may log warning WFU01 Cannot read directory 
	// clearCreated may log warnings WPP02, WFU01, WFU03, WFU05 
	this.preProc.clearCreated(texDir);
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
     * in the according methods {@link #runLatex2pdf(LatexMainDesc)}, 
     * {@link #runBibtexByNeed(File)}, 
     * {@link #runMakeIndexByNeed(LatexMainDesc)} and 
     * {@link #runMakeGlossaryByNeed(LatexMainDesc)}. 
     * <p>
     * Logging: 
     * <ul>
     * <li> WAP01: Running <code>command</code> failed. For details...
     * <li> WAP02: Running <code>command</code> failed. No log file 
     * <li> WAP04: if <code>logFile</code> is not readable. 
     * <li> WFU03: cannot close log file 
     * <li> WEX01, WEX02, WEX03, WEX04, WEX05:
     *      if one of the commands mentioned in the throws-tag fails 
     * </ul>
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
     * @throws BuildFailureException
     *    TEX01 if invocation one of the following commands fails: the 
     *    <ul>
     *    <li> latex2pdf command from {@link Settings#getLatexCommand()} 
     *    <li> BibTeX    command from {@link Settings#getBibtexCommand()}
     *    <li> makeindex command from {@link Settings#getMakeIndexCommand()} 
     *    <li> makeglossaries command 
     *         from {@link Settings#getMakeGlossariesCommand()} 
     *    </ul>
     * @see #processLatex2pdfCore(LatexMainDesc)
     * @see #processLatex2html(File)
     * @see #processLatex2odt(File)
     * @see #processLatex2docx(File)
     */
    private int preProcessLatex2pdf(LatexMainDesc desc) 
	throws BuildFailureException {

	// initial latex run 
 	// may throw BuildFailureException TEX01 
	// may log warnings WEX01, WEX02, WEX03, WEX04, WEX05, 
	// WAP01, WAP02, WAP04, WFU03
	runLatex2pdf(desc);
	File texFile = desc.texFile;

	// create bibliography, index and glossary by need 
	// may throw BuildFailureException  TEX01 
	// may log warnings WEX01, WEX02, WEX03, WEX04, WEX05, 
	// WAP01, WAP02, WAP03, WAP04, WFU03
	boolean hasBib    = runBibtexByNeed      (texFile);
	// may both throw BuildFailureException, both TEX01 
	// may both log warnings WEX01, WEX02, WEX03, WEX04, WEX05, 
	// WAP01, WAP02, WAP03, WAP04, WFU03
	boolean hasIdxGls = runMakeIndexByNeed   (desc)
	    |               runMakeGlossaryByNeed(desc);

	// rerun LaTeX at least once if bibtex or makeindex had been run 
	// or if a toc, a lof or a lot exists. 
	if (hasBib) {
	    // on run to include the bibliography from xxx.bbl into the pdf 
	    // and the lables into the aux file 
	    // and another run to put the labels from the aux file 
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
     * according to {@link #preProcessLatex2pdf(LatexMainDesc)} 
     * and reruns latex as long as needed to get all links 
     * or as threshold {@link Settings#maxNumReRunsLatex} specifies. 
     * <p>
     * The result of the LaTeX run is typically some pdf-file, 
     * but it is also possible to specify the dvi-format 
     * (no longer recommended but still working). 
     * <p>
     * Note that no warnings are issued by the latex run. 
     * <p>
     * Logging: 
     * <ul>
     * <li> WAP01: Running <code>command</code> failed. For details...
     * <li> WAP02: Running <code>command</code> failed. No log file 
     * <li> WAP04: if <code>logFile</code> is not readable. 
     * <li> WFU03: cannot close 
     * <li> WEX01, WEX02, WEX03, WEX04, WEX05: 
     *      as for {@link #preProcessLatex2pdf(LatexMainDesc)} 
     *      maybe caused by subsequent runs. 
     * </ul>
     *
     * @param texFile
     *    the latex-file to be processed. 
     * @param logFile
     *    the log-file after processing <code>texFile</code>. 
     * @throws BuildFailureException
     *    TEX01 as for {@link #preProcessLatex2pdf(LatexMainDesc)} 
     *    maybe caused by subsequent runs. 
     * @see #processLatex2pdf(File)
     * @see #processLatex2txt(File)
     */
    private void processLatex2pdfCore(LatexMainDesc desc) 
	throws BuildFailureException {

	// may throw BuildFailureException TEX01, 
	// log warning WEX01, WEX02, WEX03, WEX04, WEX05 
 	int numLatexReRuns = preProcessLatex2pdf(desc);
				      
	assert numLatexReRuns == 0 
	    || numLatexReRuns == 1 
	    || numLatexReRuns == 2;
	if (numLatexReRuns > 0) {
	    // rerun LaTeX without makeindex and makeglossaries 
	    this.log.debug("Rerun LaTeX to update table of contents, ... " + 
			   "bibliography, index, or that like. ");
	    // may throw BuildFailureException TEX01 
	    // may log warnings WEX01, WEX02, WEX03, WEX04, WEX05, 
	    // WAP01, WAP02, WAP04, WFU03
	    runLatex2pdf(desc);
	    numLatexReRuns--;
	}
	assert numLatexReRuns == 0 || numLatexReRuns == 1;

	// rerun latex by need patternRerunMakeIndex
	boolean needMakeIndexReRun;
	boolean needLatexReRun = (numLatexReRuns == 1)
	    || needRun(true, "LaTeX", desc.logFile, 
		       this.settings.getPatternReRunLatex());

	int maxNumReruns = this.settings.getMaxNumReRunsLatex();
	for (int num = 0; maxNumReruns == -1 || num < maxNumReruns; num++) {
	    needMakeIndexReRun = 
		needRun(true, "MakeIndex", desc.logFile, 
			this.settings.getPatternReRunMakeIndex());
	    // FIXME: superfluous since pattern rerunfileckeck 
	    // triggering makeindex also fits rerun of LaTeX 
	    needLatexReRun |= needMakeIndexReRun;
	    if (!needLatexReRun) {
		return;
	    }
            this.log.debug("Latex must be rerun. ");
	    if (needMakeIndexReRun) {
		// FIXME: not by need 
		// may throw BuildFailureException TEX01 
		// may log warnings WEX01, WEX02, WEX03, WEX04, WEX05, 
		// WAP01, WAP02, WAP03, WAP04, WFU03
		runMakeIndexByNeed(desc);
	    }

	    // may throw BuildFailureException TEX01 
	    // may log warnings WEX01, WEX02, WEX03, WEX04, WEX05, 
	    // WAP01, WAP02, WAP04, WFU03
	    runLatex2pdf(desc);
	    needLatexReRun = needRun(true, "LaTeX", desc.logFile, 
				     this.settings.getPatternReRunLatex());
        }
	this.log.warn("WLP01: LaTeX requires rerun but maximum number " + 
		      maxNumReruns + " reached. ");
    }

    /**
     * Returns whether a(n other) LaTeX/MakeIndex run is necessary 
     * based on a pattern matching in the log file. 
     * <p>
     * Logging: 
     * WFU03: cannot close 
     */
    private boolean needRun(boolean another, 
			    String kind, 
			    File logFile, 
			    String pattern) {

	// may log warning WFU03 cannot close 
	Boolean needRun = this.fileUtils.matchInFile(logFile, pattern);
	if (needRun == null) {
	    this.log.warn("WLP02: Cannot read log file '" + logFile.getName() + 
			  "'; " + kind + " may require rerun. ");
	    return false;
	}
	return needRun;
    }

    /**
     * Container which comprises, besides the latex main file 
     * also several files creation of which shall be done once for ever. 
     */
    static class LatexMainDesc {
	private final File texFile;
	private final File pdfFile;
	private final File logFile;

	private final File idxFile;
	private final File indFile;
	private final File ilgFile;

	private final File glsFile;
	private final File gloFile;
	private final File xxxFile;
	private final File glgFile;

	LatexMainDesc(File texFile, TexFileUtils fileUtils) {
	    this.texFile = texFile;
	    // FIXME: easier to create xxxFile first 
	    this.xxxFile = fileUtils.replaceSuffix(texFile, SUFFIX_VOID);
	    this.pdfFile = fileUtils.replaceSuffix(texFile, SUFFIX_PDF);
	    this.logFile = fileUtils.replaceSuffix(texFile, SUFFIX_LOG);

	    this.idxFile = fileUtils.replaceSuffix(texFile, SUFFIX_IDX);
	    this.indFile = fileUtils.replaceSuffix(texFile, SUFFIX_IND);
	    this.ilgFile = fileUtils.replaceSuffix(texFile, SUFFIX_ILG);

	    this.glsFile = fileUtils.replaceSuffix(texFile, SUFFIX_GLS);
	    this.gloFile = fileUtils.replaceSuffix(texFile, SUFFIX_GLO);
	    this.glgFile = fileUtils.replaceSuffix(texFile, SUFFIX_GLG);
	}
    } // class LatexMainDesc 

    /**
     * Runs LaTeX on <code>texFile</code> 
     * BibTeX, MakeIndex and MakeGlossaries 
     * and again LaTeX creating a pdf-file 
     * as specified by {@link #preProcessLatex2pdf(LatexMainDesc)} 
     * and issues a warning if 
     * <ul>
     * <li>
     * another LaTeX rerun is required 
     * beyond {@link Settings#maxNumReRunsLatex}, 
     * <li>
     * bad boxes or warnings occurred. 
     * For details see {@link #logWarns(File, String)}. 
     * </ul>
     * <p>
     * Logging: 
     * <ul>
     * <li> WFU03: cannot close 
     * <li> WAP04: if <code>logFile</code> is not readable. 
     * <li> WLP03: <code>command</code> created bad boxes 
     * <li> WLP04: <code>command</code> emitted warnings 
     * <li> WEX01, WEX02, WEX03, WEX04, WEX05: 
     *      as for {@link #processLatex2pdfCore(LatexMainDesc)} 
     *      if running an exernal command fails. 
     * </ul>
     *
     * @param texFile
     *    the tex file to be processed. 
     * @throws BuildFailureException
     *    TEX01 as for {@link #processLatex2pdfCore(LatexMainDesc)}. 
     * @see #needAnotherLatexRun(File)
     * @see Target#pdf
     */
    void processLatex2pdf(File texFile) throws BuildFailureException {
        this.log.info("Converting into pdf:  LaTeX file '" + texFile + "'. ");
	LatexMainDesc desc = new LatexMainDesc(texFile, this.fileUtils);
	// may throw BuildFailureException TEX01, 
	// log warning WEX01, WEX02, WEX03, WEX04, WEX05 
	processLatex2pdfCore(desc);

	// emit warnings (errors are emitted by runLatex2pdf and that like.)
	// may log warnings WFU03, WAP04, WLP03, WLP04 
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
     * <p>
     * Logging: 
     * <ul>
     * <li> WAP01: Running <code>command</code> failed. For details...
     * <li> WAP02: Running <code>command</code> failed. No log file 
     * <li> WAP04: if <code>logFile</code> is not readable. 
     * <li> WFU03: cannot close 
     * </ul>
     */
    private void logErrs(File logFile, String command) {
	// may log warnings WFU03, WAP01, WAP02, WAP04
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
     * <p>
     * Logging: 
     * <ul>
     * <li> WFU03: cannot close 
     * <li> WAP04: if <code>logFile</code> is not readable. 
     * <li> WLP03: <code>command</code> created bad boxes 
     * <li> WLP04: <code>command</code> emitted warnings 
     * </ul>
     *
     * @param logFile
     *    the log-file to detect warnings in. 
     * @param command
     *    the command which created <code>logFile</code> 
     *    and which maybe created warnings. 
     */
    private void logWarns(File logFile, String command) {
	if (!logFile.exists()) {
	    return;
	}
	// hasErrsWarns may log warnings WFU03 cannot close, WAP04 not readable
	if (this.settings.getDebugBadBoxes() && 
	    hasErrsWarns(logFile, PATTERN_OUFULL_HVBOX)) {
	    this.log.warn("WLP03: Running " + command + 
			  " created bad boxes logged in '" + 
			  logFile.getName() + "'. ");
	}
	if (this.settings.getDebugWarnings() && 
	    hasErrsWarns(logFile, this.settings.getPatternWarnLatex())) {
	    // logs warning WAP03: emitted warnings 
	    logWarn(logFile, command);
 	}
    }

    /**
     * Runs conversion of <code>texFile</code> to html or xhtml 
     * after processing latex to set up the references, 
     * bibliography, index and that like. 
     * <p>
     * Logging: FIXME: incomplete 
     * <ul>
     * <li> WEX01, WEX02, WEX03, WEX04, WEX05: 
     *      if running an exernal command fails. 
     * </ul>
     *
     * @param texFile
     *    the tex file to be processed. 
     * @throws BuildFailureException
     *    TEX01 as for {@link #preProcessLatex2pdf(LatexMainDesc)} 
     *    but also as for {@link #runLatex2html(LatexMainDesc)}. 
     * @see #preProcessLatex2pdf(File, File)
     * @see #runLatex2html(File)
     * @see Target#html
     */
    void processLatex2html(File texFile) throws BuildFailureException {
	this.log.info("Converting into html: LaTeX file '" + texFile + "'. ");
	LatexMainDesc desc = new LatexMainDesc(texFile, this.fileUtils);
	// may throw BuildFailureException TEX01, 
	// log warning WEX01, WEX02, WEX03, WEX04, WEX05 
	preProcessLatex2pdf(desc);
	// may throw BuildFailureException TEX01, 
	// log warning WEX01, WEX02, WEX03, WEX04, WEX05 
        runLatex2html      (desc);
    }

    /**
     * Runs conversion of <code>texFile</code> 
     * to odt or other open office formats 
     * after processing latex to set up the references, 
     * bibliography, index and that like. 
     * <p>
     * Logging: FIXME: incomplete 
     * <ul>
     * <li> WEX01, WEX02, WEX03, WEX04, WEX05: 
     *      if running an exernal command fails. 
     * </ul>
     *
     * @param texFile
     *    the tex file to be processed. 
     * @throws BuildFailureException
     *    TEX01 as for {@link #preProcessLatex2pdf(LatexMainDesc)} 
     *    but also as for {@link #runLatex2odt(LatexMainDesc)}. 
     * @see #preProcessLatex2pdf(File, File)
     * @see #runLatex2odt(File)
     * @see Target#odt
     */
    void processLatex2odt(File texFile) throws BuildFailureException {
	this.log.info("Converting into odt:  LaTeX file '" + texFile + "'. ");
	LatexMainDesc desc = new LatexMainDesc(texFile, this.fileUtils);
	// may throw BuildFailureException TEX01, 
	// log warning WEX01, WEX02, WEX03, WEX04, WEX05 
        preProcessLatex2pdf(desc);
	// may throw BuildFailureException TEX01, 
	// log warning WEX01, WEX02, WEX03, WEX04, WEX05 
        runLatex2odt       (desc);
    }

    /**
     * Runs conversion of <code>texFile</code> 
     * to docx or other MS word formats 
     * after processing latex to set up the references, 
     * bibliography, index and that like. 
     * <p>
     * Logging: FIXME: incomplete 
     * <ul>
     * <li> WEX01, WEX02, WEX03, WEX04, WEX05: 
     *      if running an exernal command fails. 
     * </ul>
     *
     * @param texFile
     *    the tex file to be processed. 
     * @throws BuildFailureException
     *    TEX01 as for {@link #preProcessLatex2pdf(LatexMainDesc)} 
     *    but also as for {@link #runLatex2odt(LatexMainDesc)} 
     *    and for {@link #runodt2doc(File)}. 
     * @see #preProcessLatex2pdf(LatexMainDesc)
     * @see #runLatex2odt(File)
     * @see #runOdt2doc(File)
     * @see Target#docx
     */
    void processLatex2docx(File texFile) throws BuildFailureException {
	this.log.info("Converting into doc(x): LaTeX file '" + texFile + "'. ");
	LatexMainDesc desc = new LatexMainDesc(texFile, this.fileUtils);
	// may throw BuildFailureException TEX0, 
	// log warning WEX01, WEX02, WEX03, WEX04, WEX05 
 	preProcessLatex2pdf(desc);
	// may throw BuildFailureException TEX0, 
	// log warning WEX01, WEX02, WEX03, WEX04, WEX05 
        runLatex2odt       (desc);
	// may throw BuildFailureException TEX01, 
	// log warning WEX01, WEX02, WEX03, WEX04, WEX05 
        runOdt2doc         (texFile);
    }

    /**
     * Runs direct conversion of <code>texFile</code> to rtf format. 
     * <p>
     * FIXME: Maybe prior invocation of LaTeX MakeIndex and BibTeX 
     * after set up the references, bibliography, index and that like 
     * would be better. 
     * <p>
     * Logging: FIXME: incomplete 
     * <ul>
     * <li> WEX01, WEX02, WEX03, WEX04, WEX05: 
     *      if running an exernal command fails. 
     * </ul>
     *
     * @param texFile
     *    the tex file to be processed. 
     * @throws BuildFailureException
     *    TEX01 if running the latex2rtf command 
     *    returned by {@link Settings#getLatex2rtfCommand()} failed. 
     * @see #runLatex2rtf(File)
     * @see Target#rtf
     */
    void processLatex2rtf(File texFile) throws BuildFailureException {
	this.log.info("Converting into rtf:  LaTeX file '" + texFile + "'. ");
	// may throw BuildFailureException TEX01, 
	// log warning WEX01, WEX02, WEX03, WEX04, WEX05 
	runLatex2rtf(texFile);
    }

    /**
     * Runs conversion of <code>texFile</code> to txt format via pdf. 
     * <p>
     * Logging: FIXME: incomplete 
     * <ul>
     * <li> WEX01, WEX02, WEX03, WEX04, WEX05: 
     *      if running an exernal command fails. 
     * </ul>
     *
     * @param texFile
     *    the tex file to be processed. 
     * @throws BuildFailureException
     *    TEX01 as for {@link #processLatex2pdfCore(LatexMainDesc)} 
     *    and for {@link #runPdf2txt(File)}. 
     * @see #processLatex2pdfCore(LatexMainDesc)
     * @see #runPdf2txt(File)
     * @see Target#rtf
     */
    void processLatex2txt(File texFile) throws BuildFailureException {
	this.log.info("Converting into txt:  LaTeX file '" + texFile + "'. ");
	LatexMainDesc desc = new LatexMainDesc(texFile, this.fileUtils);
	// may throw BuildFailureException TEX01, 
	// log warning WEX01, WEX02, WEX03, WEX04, WEX05 
	processLatex2pdfCore(desc);
	// warnings emitted by LaTex are ignored 
	// (errors are emitted by runLatex2pdf and that like.)
	// may throw BuildFailureException TEX01, 
	// log warning WEX01, WEX02, WEX03, WEX04, WEX05 
	runPdf2txt      (texFile);
    }

    /**
     * Runs the BibTeX command given by {@link Settings#getBibtexCommand()} 
     * on the aux-file corresponding with <code>texFile</code> 
     * in the directory containing <code>texFile</code> 
     * provided an according pattern in the aux-file indicates 
     * that a bibliography shall be created. 
     * <p>
     * Logging: 
     * <ul>
     * <li> WAP01: Running <code>bibtex</code> failed. For details...
     * <li> WAP02: Running <code>bibtex</code> failed. No log file 
     * <li> WAP03: Running <code>bibtex</code> emitted warnings. 
     * <li> WAP04: if <code>logFile</code> is not readable. 
     * <li> WFU03: cannot close 
     * <li> WEX01, WEX02, WEX03, WEX04, WEX05: 
     * if running the BibTeX command failed. 
     * </ul>
     *
     * @param texFile
     *    the latex-file BibTeX is to be processed for. 
     * @return
     *    whether BibTeX has been run. 
     *    Equivalently, whether LaTeX has to be rerun because of BibTeX. 
     * @throws BuildFailureException
     *    TEX01 if invocation of the BibTeX command 
     *    returned by {@link Settings#getBibtexCommand()} failed. 
     */
    private boolean runBibtexByNeed(File texFile) throws BuildFailureException {
	File auxFile    = this.fileUtils.replaceSuffix(texFile, SUFFIX_AUX);
	if (!needRun(false, "BibTeX", auxFile, PATTERN_NEED_BIBTEX_RUN)) {
	    return false;
	}

	String command = this.settings.getBibtexCommand();
	this.log.debug("Running " + command + 
		       " on '" + auxFile.getName() + "'. ");
	String[] args = buildArguments(this.settings.getBibtexOptions(), 
				       auxFile);
	// may throw BuildFailureException TEX01, 
	// may log warning WEX01, WEX02, WEX03, WEX04, WEX05 
	this.executor.execute(texFile.getParentFile(), // workingDir 
			      this.settings.getTexPath(), 
			      command, 
			      args,
			      this.fileUtils.replaceSuffix(texFile,SUFFIX_BBL));

	File logFile = this.fileUtils.replaceSuffix(texFile, SUFFIX_BLG);
	// may log warnings WFU03, WAP01, WAP02, WAP04
	logErrs (logFile, command, this.settings.getPatternErrBibtex());
	// may log warnings WFU03, WAP03, WAP04
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
     * <p>
     * Logging: 
     * <ul>
     * <li> WAP01: Running <code>makeindex</code> failed. For details...
     * <li> WAP02: Running <code>makeindex</code> failed. No log file 
     * <li> WAP03: Running <code>makeindex</code> emitted warnings. 
     * <li> WAP04: .ilg-file is not readable. 
     * <li> WFU03: cannot close .ilg-file 
     * <li> WEX01, WEX02, WEX03, WEX04, WEX05: 
     * if running the makeindex command failed. 
     * </ul>
     *
     * @param texFile
     *    the latex-file MakeIndex is to be processed for. 
     * @return
     *    whether MakeIndex had been run. 
     *    Equivalently, whether LaTeX has to be rerun because of MakeIndex. 
     * @throws BuildFailureException
     *    TEX01 if invocation of the makeindex command 
     *    returned by {@link Settings#getMakeIndexCommand()} failed. 
     */
    // FIXME: bad name since now there are reruns. 
    // Suggestion: runMakeIndexInitByNeed 
    // Other methods accordingly. 
    // maybe better: eliminate altogether 
    private boolean runMakeIndexByNeed(LatexMainDesc desc)
	throws BuildFailureException {

	// raw index file written by pdflatex 
	boolean needRun = desc.idxFile.exists();
	this.log.debug("MakeIndex run required? " + needRun);
	if (needRun) {
	    // may throw BuildFailureException TEX01 
	    // may log warnings WEX01, WEX02, WEX03, WEX04, WEX05, 
	    // WAP01, WAP02, WAP03, WAP04, WFU03
	    runMakeIndex(desc);
	}
	return needRun;
    }

    /**
     * Runs the MakeIndex command 
     * given by {@link Settings#getMakeIndexCommand()}. 
     * <p>
     * Logging: 
     * <ul>
     * <li> WAP01: Running <code>makeindex</code> failed. For details...
     * <li> WAP02: Running <code>makeindex</code> failed. No log file 
     * <li> WAP03: Running <code>makeindex</code> emitted warnings. 
     * <li> WAP04 .ilg-file is not readable. 
     * <li> WFU03: cannot close .ilg-file 
     * <li> WEX01, WEX02, WEX03, WEX04, WEX05: 
     * if running the makeindex command failed. 
     * </ul>
     *
     * @param idxFile
     *    the idx-file MakeIndex is to be run on. 
     * @throws BuildFailureException
     *    TEX01 if invocation of the makeindex command 
     *    returned by {@link Settings#getMakeIndexCommand()} failed. 
     */
    private void runMakeIndex(LatexMainDesc desc) 
	throws BuildFailureException {
	String command = this.settings.getMakeIndexCommand();
	File idxFile = desc.idxFile;
	this.log.debug("Running " + command  + 
		       " on '" + idxFile.getName() + "'. ");
	String[] args = buildArguments(this.settings.getMakeIndexOptions(),
				       idxFile);
	// may throw BuildFailureException TEX01, 
	// may log warning WEX01, WEX02, WEX03, WEX04, WEX05 
	this.executor.execute(idxFile.getParentFile(), //workingDir 
			      this.settings.getTexPath(), 
			      command, 
			      args,
			      desc.indFile);

	// detect errors and warnings makeindex wrote into xxx.ilg 
 	// may log warnings WFU03, WAP01, WAP02, WAP04
	logErrs (desc.ilgFile, command,this.settings.getPatternErrMakeIndex());
	// may log warnings WFU03, WAP03, WAP04
	logWarns(desc.ilgFile, command,this.settings.getPatternWarnMakeIndex());
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
     * <p>
     * Logging: 
     * <ul>
     * <li> WAP01: Running <code>makeglossaries</code> failed. For details...
     * <li> WAP02 Running <code>makeglossaries</code> failed. No log file 
     * <li> WAP03: Running <code>makeglossaries</code> emitted warnings. 
     * <li> WAP04: .glg-file is not readable. 
     * <li> WFU03: cannot close .glg-file 
     * <li> WEX01, WEX02, WEX03, WEX04, WEX05: 
     * if running the makeglossaries command failed. 
     * </ul>
     *
     * @param texFile
     *    the latex-file MakeGlossaries is to be processed for. 
     * @return
     *    whether MakeGlossaries had been run. 
     *    Equivalently, 
     *    whether LaTeX has to be rerun because of MakeGlossaries. 
     * @throws BuildFailureException
     *    TEX01 if invocation of the makeglossaries command 
     *    returned by {@link Settings#getMakeGlossariesCommand()} failed. 
     */
     private boolean runMakeGlossaryByNeed(LatexMainDesc desc)
	throws BuildFailureException {

	// raw glossaries file created by pdflatex 
	boolean needRun = desc.gloFile.exists();
	this.log.debug("MakeGlossaries run required? " + needRun);
	if (!needRun) {
	    return false;
	}

	// file name without ending: parameter for makeglossaries 
	File xxxFile = desc.xxxFile;
	String command = this.settings.getMakeGlossariesCommand();
	this.log.debug("Running " + command + 
		       " on '" + xxxFile.getName()+ "'. ");
	String[] args = buildArguments(this.settings.getMakeGlossariesOptions(),
				       xxxFile);
	// may throw BuildFailureException TEX01, 
	// may log warning WEX01, WEX02, WEX03, WEX04, WEX05 
	this.executor.execute(xxxFile.getParentFile(), //workingDir 
			      this.settings.getTexPath(), 
			      command, 
			      args,
			      desc.glsFile);

	// detect errors and warnings makeglossaries wrote into xxx.glg 
	File glgFile = desc.glgFile;
	// may log warnings WFU03, WAP01, WAP02, WAP04
	logErrs (glgFile, command, this.settings.getPatternErrMakeGlossaries());
	// may log warnings WFU03, WAP03, WAP04
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
     * <p>
     * Logging: 
     * <ul>
     * <li> WAP01: Running <code>latex2pdf</code> failed. For details...
     * <li> WAP02: Running <code>latex2pdf</code> failed. No log file 
     * <li> WAP04: .log-file is not readable. 
     * <li> WFU03: cannot close .log-file 
     * <li> WEX01, WEX02, WEX03, WEX04, WEX05: 
     * if running the latex2pdf command failed. 
     * </ul>
     *
     * @param texFile
     *    the latex-file to be processed. 
     * @param logFile
     *    the log-file after processing <code>texFile</code>. 
     * @throws BuildFailureException
     *    TEX01 if invocation of the latex2pdf command 
     *    returned by {@link Settings#getLatexCommand()} failed. 
     */
    private void runLatex2pdf(LatexMainDesc desc)
	throws BuildFailureException {

	File texFile = desc.texFile;
	String command = this.settings.getLatex2pdfCommand();
	this.log.debug("Running " + command + 
		       " on '" + texFile.getName() + "'. ");
	String[] args = buildArguments(this.settings.getLatex2pdfOptions(), 
				       texFile);
	// may throw BuildFailureException TEX01, 
	// may log warning WEX01, WEX02, WEX03, WEX04, WEX05 
        this.executor.execute(texFile.getParentFile(), // workingDir 
			      this.settings.getTexPath(), 
			      command, 
			      args,
			      desc.pdfFile);

	// logging errors (warnings are done in processLatex2pdf)
	// may log warnings WFU03, WAP01, WAP02, WAP04
	logErrs(desc.logFile, command);
    }

    /**
     * Runs the tex4ht command given by {@link Settings#getTex4htCommand()} 
     * on <code>texFile</code> 
     * in the directory containing <code>texFile</code> 
     * with arguments given by {@link #buildHtlatexArguments(String, File)}. 
     * <p>
     * Logging: 
     * <ul>
     * <li> WAP01: Running <code>htlatex</code> failed. For details...
     * <li> WAP02: Running <code>htlatex</code> failed. No log file 
     * <li> WLP03: <code>htlatex</code> created bad boxes 
     * <li> WLP04: <code>htlatex</code> emitted warnings 
     * <li> WAP04: log file is not readable. 
     * <li> WFU03: cannot close log file 
     * <li> WEX01, WEX02, WEX03, WEX04, WEX05: 
     * if running the tex4ht command failed. 
     * </ul>
     *
     * @param texFile
     *    the latex-file to be processed. 
     * @throws BuildFailureException
     *    TEX01 if invocation of the tex4ht command 
     *    returned by {@link Settings#getTex4htCommand()} failed. 
     */
    private void runLatex2html(LatexMainDesc desc)
	throws BuildFailureException {

	File texFile = desc.texFile;
	String command = this.settings.getTex4htCommand();
        this.log.debug("Running " + command + 
		       " on '" + texFile.getName() + "'. ");
        String[] args = buildHtlatexArguments(texFile);
	// may throw BuildFailureException TEX01, 
	// may log warning WEX01, WEX02, WEX03, WEX04, WEX05 
        this.executor.execute(texFile.getParentFile(), // workingDir 
			      this.settings.getTexPath(), 
			      command, 
			      args,
			      this.fileUtils.replaceSuffix(texFile, 
							   SUFFIX_HTML));

	// logging errors and warnings 
	// may log warnings WFU03, WAP01, WAP02, WAP04
	logErrs (desc.logFile, command);
	// may log warnings WFU03, WAP04, WLP03, WLP04 
	logWarns(desc.logFile, command);
    }

    private String[] buildHtlatexArguments(File texFile) {
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
     * <p>
     * Logging: 
     * <ul>
     * <li> WEX01, WEX02, WEX03, WEX04, WEX05: 
     * if running the latex2rtf command failed. 
     * </ul>
     *
     * @param texFile
     *    the latex file to be processed. 
     * @throws BuildFailureException
     *    TEX01 if invocation of the latex2rtf command 
     *    returned by {@link Settings#getLatex2rtfCommand()} failed. 
     */
    private void runLatex2rtf(File texFile) throws BuildFailureException {
	String command = this.settings.getLatex2rtfCommand();
        this.log.debug("Running " + command + 
		       " on '" + texFile.getName() + "'. ");
        String[] args = buildArguments(this.settings.getLatex2rtfOptions(), 
				       texFile);
	// may throw BuildFailureException TEX01, 
	// may log warning WEX01, WEX02, WEX03, WEX04, WEX05 
        this.executor.execute(texFile.getParentFile(), // workingDir
			      this.settings.getTexPath(), 
			      command, 
			      args,
			      this.fileUtils.replaceSuffix(texFile,SUFFIX_RTF));

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
     * <p>
     * Logging: 
     * <ul>
     * <li> WAP01: Running <code>htlatex</code> failed. For details...
     * <li> WAP02: Running <code>htlatex</code> failed. No log file 
     * <li> WLP03: <code>htlatex</code> created bad boxes 
     * <li> WLP04: <code>htlatex</code> emitted warnings 
     * <li> WAP04: log file is not readable. 
     * <li> WFU03: cannot close log file 
     * <li> WEX01, WEX02, WEX03, WEX04, WEX05: 
     * if running the tex4ht command failed. 
     * </ul>
     *
     * @param texFile
     *    the latex file to be processed. 
     * @throws BuildFailureException
     *    TEX01 if invocation of the tex4ht command 
     *    returned by {@link Settings#getTex4htCommand()} failed. 
     */
    private void runLatex2odt(LatexMainDesc desc) throws BuildFailureException {
	File texFile = desc.texFile;
	String command = this.settings.getTex4htCommand();
        this.log.debug("Running " + command + 
		       " on '" + texFile.getName() + "'. ");
        String[] args = new String[] {
	    texFile.getName(),
	    "xhtml,ooffice", // there is no choice here 
	    "ooffice/! -cmozhtf",// ooffice/! represents a font direcory 
	    "-coo -cvalidate"// -coo is mandatory, -cvalidate is not 
	};
	// may throw BuildFailureException TEX01, 
	// may log warning WEX01, WEX02, WEX03, WEX04, WEX05 
        this.executor.execute(texFile.getParentFile(), 
			      this.settings.getTexPath(), 
			      command, 
			      args,
			      this.fileUtils.replaceSuffix(texFile,SUFFIX_ODT));

	// FIXME: logging refers to latex only, not to tex4ht or t4ht script 
	// may log warnings WFU03, WAP01, WAP02, WAP04
	logErrs (desc.logFile, command);
	// may log warnings WFU03, WAP04, WLP03, WLP04 
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
     * <p>
     * Logging: 
     * <ul>
     * <li> WEX01, WEX02, WEX03, WEX04, WEX05: 
     * if running the odt2doc command failed. 
     * </ul>
     *
     * @param texFile
     *    the latex file to be processed. 
     * @throws BuildFailureException
     *    TEX01 if invocation of the odt2doc command 
     *    returned by {@link Settings#getOdt2docCommand()} failed. 
     */
    private void runOdt2doc(File texFile) throws BuildFailureException {
	File odtFile = this.fileUtils.replaceSuffix(texFile, SUFFIX_ODT);
	String command = this.settings.getOdt2docCommand();
	this.log.debug("Running " + command + 
		       " on '" + odtFile.getName() + "'. ");
	String[] args = buildArguments(this.settings.getOdt2docOptions(),
				       odtFile);
	String suffix = null;
	for (int idx = 0; idx < args.length -1; idx++) {
	    // FIXME: -f is hardcoded 
	    if (args[idx].startsWith("-f")) {
		assert suffix == null;// -f comes once only 
		// without leading '-f'
		suffix = args[idx].substring(2, args[idx].length());
	    }
	}
	assert suffix != null;
	// may throw BuildFailureException TEX01, 
	// may log warning WEX01, WEX02, WEX03, WEX04, WEX05 
	this.executor.execute(texFile.getParentFile(), 
			      this.settings.getTexPath(), 
			      command, 
			      args,
			      this.fileUtils.replaceSuffix(texFile, suffix));
 	// FIXME: what about error logging? 
	// Seems not to create a log-file. 
     }

    /**
     * Runs conversion from pdf to txt-file  
     * executing {@link Settings#getPdf2txtCommand()} 
     * on a pdf-file created from <code>texFile</code> 
     * in the directory containing <code>texFile</code> 
     * with arguments given by {@link #buildLatexArguments(String, File)}. 
     * <p>
     * Logging: 
     * <ul>
     * <li> WEX01, WEX02, WEX03, WEX04, WEX05: 
     * if running the pdf2txt command failed. 
     * </ul>
     *
     * @param texFile
     *    the latex file to be processed. 
     * @throws BuildFailureException
     *    TEX01 if invocation of the pdf2txt command 
     *    returned by {@link Settings#getPdf2txtCommand()} failed. 
     */
    private void runPdf2txt(File texFile) throws BuildFailureException {
	File pdfFile = this.fileUtils.replaceSuffix(texFile, SUFFIX_PDF);
	String command = this.settings.getPdf2txtCommand();
	this.log.debug("Running " + command + 
		       " on '" + pdfFile.getName() + "'. ");
	String[] args = buildArguments(this.settings.getPdf2txtOptions(),
				       pdfFile);
	// may throw BuildFailureException TEX01, 
	// may log warning WEX01, WEX02, WEX03, WEX04, WEX05 
	this.executor.execute(texFile.getParentFile(), 
			      this.settings.getTexPath(), 
			      command, 
			      args, 
			      this.fileUtils.replaceSuffix(texFile,SUFFIX_TXT));
	// FIXME: what about error logging? 
	// Seems not to create a log-file. 
    }
 }
