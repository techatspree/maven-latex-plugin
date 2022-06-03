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
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;

import eu.simuline.m2latex.antTask.LatexCfgTask;
import eu.simuline.m2latex.antTask.LatexClrTask;
import eu.simuline.m2latex.mojo.CfgLatexMojo;
import eu.simuline.m2latex.mojo.ChkMojo;
import eu.simuline.m2latex.mojo.ClearMojo;
import eu.simuline.m2latex.mojo.GraphicsMojo;

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
 * {@link #processLatex2rtf(File)}, {@link #processLatex2dvi(File)},
 * {@link #processLatex2pdf(File)},
 * {@link #processLatex2html(File)}, {@link #processLatex2odt(File)},
 * {@link #processLatex2docx(File)} and {@link #processLatex2txt(File)}.
 */
public class LatexProcessor extends AbstractLatexProcessor {

    static final String PATTERN_NEED_BIBTEX_RUN = "^\\\\bibdata";

    // Note that two \\ represent a single \ in the string.
    // Thus \\\\ represents '\\' in the pattern,
    // which in turn represents a single \.
    static final String PATTERN_OUFULL_HVBOX = "^(Ov|Und)erfull \\\\[hv]box \\(";

    // LaTeX (notably, .tex is not needed )
    final static String SUFFIX_TOC = ".toc";
    final static String SUFFIX_LOF = ".lof";
    final static String SUFFIX_LOT = ".lot";
    final static String SUFFIX_AUX = ".aux";
    final static String SUFFIX_DVI = ".dvi";

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

    // ChkTeX: log file
    private final static String SUFFIX_CLG = ".clg";

    /**
     * The shape of the entries of an index file
     * with explicit identifier of the index.
     * If not explicitly given, this is just <code>idx</code>.
     * Note that this regular expression has three groups
     * as in the specification of <code>splitindex</code>.
     */
    private final static String IDX_EXPL = "^(\\\\indexentry)\\[([^]]*)\\](.*)$";

    /**
     * Index of the group in {@link #IDX_EXPL}
     * containing the identifier of the index.
     */
    private final static int GRP_IDENT_IDX = 2;

    /**
     * The implicit default identifier of an index
     * hardcoded into the package <code>splitidx</code>
     * and into the program <code>splitindex</code>.
     */
    private final static String IMPL_IDENT_IDX = "idx";

    /**
     * Separator <code>-</code> of the index identifier
     * used in files <code>xxx-yy.idx</code>, <code>xxx-yy.ind</code>
     * and <code>xxx-yy.ilg</code>.
     * This is hardcoded by the package <code>splitidx</code>
     * when reading <code>xxx-yy.ind</code> files
     * but is configurable as an option in the program <code>splitindex</code>.
     */
    private final static String SEP_IDENT_IDX = "-";

    private final ParameterAdapter paramAdapt;

    /**
     * The graphics preprocessor used by this processor.
     */
    private final LatexPreProcessor preProc;

    /**
     * The meta info provider used by this processor.
     */
    private final MetaInfo metaInfo;

    // also for tests
    LatexProcessor(Settings settings,
            CommandExecutor executor,
            LogWrapper log,
            TexFileUtils fileUtils,
            ParameterAdapter paramAdapt) {
        super(settings, executor, log, fileUtils);
        this.paramAdapt = paramAdapt;
        this.preProc = new LatexPreProcessor(this.settings, this.executor, this.log, this.fileUtils);
        this.metaInfo = new MetaInfo(this.settings, this.executor, this.log);
    }

    /**
     * Creates a LatexProcessor with parameters given by <code>settings</code>
     * which logs onto <code>log</code> and used by <code>paramAdapt</code>.
     *
     * @param settings
     *                   the settings controlling latex processing
     * @param log
     *                   the logger to write on events while processing
     * @param paramAdapt
     *                   the parameter adapter, refers to maven-plugin or ant-task.
     */
    public LatexProcessor(Settings settings,
            LogWrapper log,
            ParameterAdapter paramAdapt) {
        this(settings, new CommandExecutor(log), log,
                new TexFileUtils(log), paramAdapt);
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
     * <li> WFU03: cannot close file 
     * <li> EFU05: Cannot delete file 
     * <li> EFU07, EFU08, EFU09: if filtering a file fails. 
     * <li> WPP02: tex file may be latex main file 
     * <li> WPP03: Skipped processing of files with suffixes ... 
     * <li> EEX01, EEX02, EEX03, WEX04, WEX05: 
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
     *    <li> TSS02 if 
     *    the tex source processing directory does either not exist 
     *    or is not a directory. 
     *    <li> TSS03 if 
     *    the output directory exists and is no directory. 
     *    <li> TSS04 if
     *    the target set is not a subset of the set given by {@link Target}.
     *    <li> TEX01 if 
     *    invocation of applications for preprocessing graphic files 
     *    or processing a latex main file fails 
     *    <li> TFU01 if 
     *    the target directory that would be returned 
     *    exists already as a regular file. 
     *    <li> TFU03, TFU04, TFU05, TFU06 if 
     *    copy of output files to target folder fails. 
     *    For details see 
     * {@link TexFileUtils#copyOutputToTargetFolder(File, FileFilter, File)}
     *    <li>TLP01 if difference check is specified in settings and if 
     *    the artifact could not be reproduced (currently for pdf only). 
     *    </ul>
     */
    public void create() throws BuildFailureException {

        this.paramAdapt.initialize();
        this.log.info("-----------create-------------");
        this.log.debug("Settings: " + this.settings.toString());

        // may throw BuildFailureException TSS01
        File texDir = this.settings.getTexSrcDirectoryFile();
        assert texDir.exists() && texDir.isDirectory()
                : "Expected existing tex folder " + texDir;

        // may throw BuildFailureException TSS02
        File texProcDir = this.settings.getTexSrcProcDirectoryFile();
        assert texProcDir.exists() && texProcDir.isDirectory()
                : "Expected existing tex processing folder " + texDir;

        // constructor DirNode may log warning WFU01 Cannot read directory
        DirNode node = new DirNode(texProcDir, this.fileUtils);

        try {
            // process graphics and determine latexMainFiles
            // may throw BuildFailureException TEX01,
            // log warning WFU03, WPP02, WPP03,
            // EEX01, EEX02, EEX03, WEX04, WEX05, EFU07, EFU08, EFU09: if filtering a file
            // fails.
            Collection<File> latexMainFiles = this.preProc
                    .processGraphicsSelectMain(texProcDir, node);

            for (File texFile : latexMainFiles) {
                // throws BuildFailureException TFU01
                // if targetDir would be an existing non-directory
                File targetDir = this.fileUtils.getTargetDirectory(texFile,
                        texDir,
                        // throws BuildFailureException TSS03
                        // if exists and is no dir
                        this.settings.getOutputDirectoryFile());
                assert !targetDir.exists() || targetDir.isDirectory()
                        : "Expected target folder " + targetDir + " folder if exists. ";

                // may throw BuildFailureException TSS04
                for (Target target : this.paramAdapt.getTargetSet()) {
                    // may throw BuildFailureException TEX01,
                    // log warning EEX01, EEX02, EEX03, WEX04, WEX05
                    target.processSource(this, texFile);
                    FileFilter fileFilter = TexFileUtils.getFileFilter(texFile,
                            target.getPatternOutputFiles(this.settings));
                    // may throw BuildFailureException
                    // TFU03, TFU04, TFU05, TFU06
                    // may log warning WFU01 Cannot read directory
                    Set<File> targetFiles = this.fileUtils.copyOutputToTargetFolder(texFile,
                            fileFilter,
                            targetDir);

                            
                    this.log.debug(String.format("target %s has difftool %s", 
                        target, target.hasDiffTool()));
                    if (!target.hasDiffTool()) {
                        this.log.debug(String.format("target %s has no difftool", target));
                        continue;
                    }

                    if (!this.settings.isChkDiff()) {
                        this.log.debug("no artifact diff specified.");
                        continue;
                    }
                    this.log.debug("Preprare verification by diffing: ");
                    File diffRootDir = this.settings.getDiffDirectoryFile().getAbsoluteFile();
                    File artifactBaseDir = this.settings.getOutputDirectoryFile();
                    assert targetFiles.size() == 1;
                    File pdfFileAct = targetFiles.iterator().next();
                    this.log.debug(String.format("act file %s", pdfFileAct));
                    File pdfFileCmp = TexFileUtils.getPdfFileDiff(pdfFileAct, artifactBaseDir, diffRootDir);
                    this.log.debug(String.format("cmp file %s", pdfFileCmp));
                    assert pdfFileCmp.exists();
                    assert pdfFileAct.exists();
                    boolean coincide = runDiffPdf(pdfFileCmp, pdfFileAct);
                    if (coincide) {
                        this.log.info("checked: coincides with expected artifact. ");
                        continue;
                    }
                    throw new BuildFailureException
                        ("TLP01: Artifact '" + pdfFileAct.getName() + 
                        "' of '" + texFile + "' could not be reproduced. ");

                } // target
            } // texFile
        } finally {
            if (this.settings.isCleanUp()) {
                // may log warning WFU01, EFU05
                this.fileUtils.cleanUp(node, texProcDir);
            }
            this.log.debug(this.settings.isCleanUp() ? ("cleanup: " + texProcDir) : "No cleanup");
        }
    }

    /**
     * Defines check goal of the maven plugin in {@link ChkMojo}.
     * This includes also creation of graphic files.
     * <p>
     * TBD: logging
     *
     * @throws BuildFailureException
     *                               <ul>
     *                               <li>
     *                               TSS02 if the tex source processing directory
     *                               does either not exist
     *                               or is not a directory.
     *                               <li>
     *                               TEX01 invoking FIXME
     *                               </ul>
     */
    // used in ChkMojo.execute() only
    // FIXME: maybe sufficient not to create graphics, if no \input.
    // Also, maybe good not to remove log file
    // maybe good to make suffix configurable rather than hardcoded.
    public void checkAll() throws BuildFailureException {

        this.paramAdapt.initialize();
        this.log.debug("Settings: " + this.settings.toString());

        // may throw BuildFailureException TSS02
        File texProcDir = this.settings.getTexSrcProcDirectoryFile();
        assert texProcDir.exists() && texProcDir.isDirectory()
                : "Expected existing tex processing folder " + texProcDir;

        // constructor DirNode may log warning WFU01 Cannot read directory
        DirNode node = new DirNode(texProcDir, this.fileUtils);

        try {
            // may throw BuildFailureException TEX01,
            // log warning WFU03, WPP02, WPP03,
            // EEX01, EEX02, EEX03, WEX04, WEX05, EFU07, EFU08, EFU09
            Collection<File> latexMainFiles = this.preProc
                    .processGraphicsSelectMain(texProcDir, node);

            for (File latexMain : latexMainFiles) {
                runCheck(latexMain);
            }
        } finally {
            // FIXME: also removes the clg-files
            if (this.settings.isCleanUp()) {
                // may log warning WFU01, EFU05
                this.fileUtils.cleanUp(node, texProcDir);
            }
        }
    }

    /**
     * Defines graphics goal of the maven plugin in {@link GraphicsMojo}.
     * <p>
     * Logging:
     * <ul>
     * <li>WFU01: Cannot read directory
     * <li>WFU03: cannot close file
     * <li>EFU07, EFU08, EFU09: if filtering a file fails.
     * <li>WPP02: tex file may be latex main file
     * <li>WPP03: Skipped processing of files with suffixes ...
     * <li>EEX01, EEX02, EEX03, WEX04, WEX05:
     * if running graphic processors failed.
     * </ul>
     *
     * @throws BuildFailureException
     *                               <ul>
     *                               <li>
     *                               TSS02 if the tex source processing directory
     *                               does either not exist
     *                               or is not a directory.
     *                               <li>
     *                               TEX01 invoking FIXME
     *                               </ul>
     */
    // used in GraphicsMojo.execute() only
    public void processGraphics() throws BuildFailureException {
        // may throw BuildFailureException TSS02
        File texProcDir = this.settings.getTexSrcProcDirectoryFile();
        assert texProcDir.exists() && texProcDir.isDirectory()
                : "Expected existing tex processing folder " + texProcDir;

        // constructor DirNode may log warning WFU01 Cannot read directory
        DirNode node = new DirNode(texProcDir, this.fileUtils);
        // may throw BuildFailureException TEX01,
        // log warning WFU03, WPP02, WPP03,
        // EEX01, EEX02, EEX03, WEX04, WEX05, EFU07, EFU08, EFU09
        this.preProc.processGraphicsSelectMain(texProcDir, node);
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
     * {@link Settings#getTexSrcProcDirectoryFile()}
     * <li>
     * {@link Settings#getPatternLatexMainFile()}
     * <li>
     * {@link Settings#getPatternCreatedFromLatexMain()}
     * </ul>
     * <p>
     * Logging:
     * <ul>
     * <li>WPP02: tex file may be latex main file
     * <li>WFU01: Cannot read directory...
     * <li>WFU03: cannot close tex file
     * <li>EFU05: Failed to delete file
     * </ul>
     *
     * @throws BuildFailureException
     *                               TSS02 if the tex source processing directory
     *                               does either not exist
     *                               or is not a directory.
     */
    public void clearAll() throws BuildFailureException {
        this.paramAdapt.initialize();
        this.log.debug("Settings: " + this.settings.toString());

        // may throw BuildFailureException TSS02
        File texProcDir = this.settings.getTexSrcProcDirectoryFile();
        assert texProcDir.exists() && texProcDir.isDirectory()
                : "Expected existing tex processing folder " + texProcDir;

        // constructor DirNode may log warning WFU01 Cannot read directory
        // clearCreated may log warnings WPP02, WFU01, WFU03, EFU05
        this.preProc.clearCreated(texProcDir);
    }

    // FIXME: use the -recorder option to resolve dependencies.
    // With that option, a file xxx.fls is generated with form
    // PWD
    // /home/ernst/OpenSource/maven-latex-plugin/maven-latex-plugin.git/trunk/maven-latex-plugin/src/site/tex
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
    // INPUT manualLatexMavenPlugin.aux
    // OUTPUT manualLatexMavenPlugin.aux
    // INPUT manualLatexMavenPlugin.out
    // OUTPUT manualLatexMavenPlugin.out
    // INPUT manualLatexMavenPlugin.toc
    // OUTPUT manualLatexMavenPlugin.toc
    // INPUT manualLatexMavenPlugin.lof
    // OUTPUT manualLatexMavenPlugin.lof
    // INPUT manualLatexMavenPlugin.lot
    // OUTPUT manualLatexMavenPlugin.lot

    // OUTPUT manualLatexMavenPlugin.idx
    // INPUT manualLatexMavenPlugin.ind

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
     * Container which comprises, besides the latex main file
     * also several files creation of which shall be done once for ever.
     */
    static class LatexMainDesc {
        private final File texFile;
        final File pdfFile;
        final File dviFile;

        private final File logFile;

        private final File idxFile;
        private final File indFile;
        private final File ilgFile;

        private final File glsFile;
        private final File gloFile;
        private final File glgFile;
        private final File xxxFile;

        // TBC: does not depend on dev
        LatexMainDesc(File texFile, TexFileUtils fileUtils, LatexDev dev) {
            this.texFile = texFile;
            // FIXME: easier to create xxxFile first
            this.xxxFile = TexFileUtils.replaceSuffix(texFile, SUFFIX_VOID);
            this.pdfFile = TexFileUtils.replaceSuffix(texFile, SUFFIX_PDF);
            this.dviFile = TexFileUtils.replaceSuffix(texFile, SUFFIX_DVI);
            this.logFile = TexFileUtils.replaceSuffix(texFile, SUFFIX_LOG);

            this.idxFile = TexFileUtils.replaceSuffix(texFile, SUFFIX_IDX);
            this.indFile = TexFileUtils.replaceSuffix(texFile, SUFFIX_IND);
            this.ilgFile = TexFileUtils.replaceSuffix(texFile, SUFFIX_ILG);

            this.glsFile = TexFileUtils.replaceSuffix(texFile, SUFFIX_GLS);
            this.gloFile = TexFileUtils.replaceSuffix(texFile, SUFFIX_GLO);
            this.glgFile = TexFileUtils.replaceSuffix(texFile, SUFFIX_GLG);
        }
    } // class LatexMainDesc

    private LatexMainDesc getLatexMainDesc(File texFile) {
        return new LatexMainDesc(texFile, this.fileUtils, this.settings.getPdfViaDvi());
    }

    /**
     * Runs LaTeX on on the latex main file <code>texFile</code>
     * described by <code>desc</code> once,
     * runs BibTeX, MakeIndex and MakeGlossaries by need
     * and returns whether a second LaTeX run is required.
     * The latter also holds, if a table of contents, a list of figures
     * or a list of tables is specified.
     * The output format of the LaTeX run is given by <code>dev</code>,
     * to be more precise by {@link LatexDev#getLatexOutputFormat()}.
     * <p>
     * A warning is logged if the LaTeX, a BibTeX run a MakeIndex
     * or a MakeGlossaries run fails
     * or if a BibTeX run or a MakeIndex or a MakeGlossary run issues a warning
     * in the according methods
     * {@link #runLatex2dev(LatexProcessor.LatexMainDesc, LatexDev)},
     * {@link #runBibtexByNeed(File)},
     * {@link #runMakeIndexByNeed(LatexMainDesc)} and
     * {@link #runMakeGlossaryByNeed(LatexMainDesc)}.
     * <p>
     * Logging:
     * <ul>
     * <li>EAP01: Running <code>command</code> failed. For details...
     * <li>EAP02: Running <code>command</code> failed. No log file
     * <li>WAP04: if <code>logFile</code> is not readable.
     * <li>WLP04: Cannot read idx file; skip creation of index
     * <li>WLP05: Use package 'splitidx' without option 'split'
     * <li>WLP02: Cannot read blg file: BibTeX run required?
     * <li>WFU03: cannot close log file
     * <li>EEX01, EEX02, EEX03, WEX04, WEX05:
     * if one of the commands mentioned in the throws-tag fails
     * </ul>
     *
     * @param desc
     *             the description of a latex main file <code>texFile</code>
     *             to be processed.
     * @param dev
     *             the device describing the output format which is either pdf or
     *             dvi.
     *             See {@link LatexDev#getLatexOutputFormat()}.
     * @return
     *         the number of LaTeX runs required
     *         because bibtex, makeindex or makeglossaries had been run
     *         or to update a table of contents or a list figures or tables.
     *         <ul>
     *         <li>
     *         If neither of these are present, no rerun is required.
     *         <li>
     *         If a bibliography, an index and a glossary is included
     *         and a table of contents,
     *         we assume that these are in the table of contents.
     *         Thus two reruns are required:
     *         one to include the bibliography or that like
     *         and the second one to make it appear in the table of contents.
     *         <li>
     *         In all other cases, a single rerun suffices
     *         </ul>
     * @throws BuildFailureException
     *                               TEX01 if invocation one of the following
     *                               commands fails: the
     *                               <ul>
     *                               <li>latex2pdf command from
     *                               {@link Settings#getLatex2pdfCommand()}
     *                               <li>BibTeX command from
     *                               {@link Settings#getBibtexCommand()}
     *                               <li>makeindex command from
     *                               {@link Settings#getMakeIndexCommand()}
     *                               <li>makeglossaries command
     *                               from
     *                               {@link Settings#getMakeGlossariesCommand()}
     *                               </ul>
     * @see #processLatex2devCore(LatexProcessor.LatexMainDesc, LatexDev)
     * @see #processLatex2html(File)
     * @see #processLatex2odt(File)
     * @see #processLatex2docx(File)
     */
    private int preProcessLatex2dev(LatexMainDesc desc, LatexDev dev)
            throws BuildFailureException {

        // initial latex run
        // may throw BuildFailureException TEX01
        // may log warnings EEX01, EEX02, EEX03, WEX04, WEX05,
        // EAP01, EAP02, WAP04, WFU03
        runLatex2dev(desc, dev);
        File texFile = desc.texFile;

        // create bibliography, index and glossary by need
        // may throw BuildFailureException TEX01
        // may log warnings EEX01, EEX02, EEX03, WEX04, WEX05,
        // EAP01, EAP02, WAP03, WAP04, WLP02, WFU03
        boolean hasBib = runBibtexByNeed(texFile);
        // may both throw BuildFailureException, both TEX01
        // may both log warnings EEX01, EEX02, EEX03, WEX04, WEX05,
        // EAP01, EAP02, WLP04, WLP05, WAP03, WAP04, WFU03
        boolean hasIdxGls = runMakeIndexByNeed(desc)
                | runMakeGlossaryByNeed(desc);

        // rerun LaTeX at least once if bibtex or makeindex had been run
        // or if a toc, a lof or a lot exists.
        if (hasBib) {
            // one run to include the bibliography from xxx.bbl into the pdf
            // and the lables into the aux file
            // and another run to put the labels from the aux file
            // to the locations of the \cite commands.

            // This suffices also to include a bib in a toc
            return 2;
        }

        boolean hasToc = TexFileUtils.replaceSuffix(texFile, SUFFIX_TOC).exists();
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
                || TexFileUtils.replaceSuffix(texFile, SUFFIX_LOF).exists()
                || TexFileUtils.replaceSuffix(texFile, SUFFIX_LOT).exists();

        return needLatexReRun ? 1 : 0;
    }

    /**
     * Runs LaTeX on the latex main file <code>texFile</code>
     * described by <code>desc</code> once,
     * runs BibTeX, MakeIndex and MakeGlossaries by need
     * according to {@link #preProcessLatex2dev(LatexMainDesc, LatexDev)}
     * and reruns MakeIndex, MakeGlossaries and LaTeX
     * as often as needed to get all links satisfied
     * or as threshold {@link Settings#maxNumReRunsLatex} specifies.
     * <p>
     * Note that still no logging of warnings from a latex run is done.
     * This is done
     * in {@link #processLatex2dev(LatexProcessor.LatexMainDesc, LatexDev)}.
     * The exclusion of logging of warnings is indicated by the name part
     * 'Core'.
     * Processing without logging of warnings
     * is required by {@link #processLatex2txt(File)}.
     * <p>
     * The output format of the LaTeX run is given by <code>dev</code>,
     * to be more precise by {@link LatexDev#getLatexOutputFormat()}.
     * <p>
     * Logging:
     * WLP01: if another rerun is required but the maximum number of runs
     * {@link Settings#getMaxNumReRunsLatex()} is reached.
     * Further logging is inherited by invoked methods:
     * <ul>
     * <li>WLP04: Cannot read idx file; skip creation of index
     * <li>WLP05: Use package 'splitidx' without option 'split'
     * <li>EAP01: Running <code>command</code> failed. For details...
     * <li>EAP02: Running <code>command</code> failed. No log file
     * <li>WAP04: if <code>logFile</code> is not readable.
     * <li>WLP02: Cannot read log file: run BibTeX/
     * (re)run MakeIndex/LaTeX required?
     * <li>WFU03: cannot close
     * <li>EEX01, EEX02, EEX03, WEX04, WEX05: as for
     * {@link #preProcessLatex2dev(LatexProcessor.LatexMainDesc, LatexDev)}
     * maybe caused by subsequent runs.
     * </ul>
     *
     * @param desc
     *             the description of a latex main file <code>texFile</code>
     *             to be processed.
     * @param dev
     *             the device describing the output format which is either pdf or
     *             dvi.
     *             See {@link LatexDev#getLatexOutputFormat()}.
     * @throws BuildFailureException
     *                               TEX01 as for
     *                               {@link #preProcessLatex2dev(LatexProcessor.LatexMainDesc, LatexDev)}
     *                               maybe caused by subsequent runs.
     * @see #processLatex2dvi(File)
     * @see #processLatex2txt(File)
     */
    private void processLatex2devCore(LatexMainDesc desc, LatexDev dev)
            throws BuildFailureException {

        // may throw BuildFailureException TEX01,
        // log warning WLP04, WLP05, EAP01, EAP02, WAP04, WLP02, WFU03,
        // EEX01, EEX02, EEX03, WEX04, WEX05
        int numLatexReRuns = preProcessLatex2dev(desc, dev);

        String latexCmd = this.settings.getCommand(ConverterCategory.LaTeX);

        assert numLatexReRuns == 0
                || numLatexReRuns == 1
                || numLatexReRuns == 2;
        if (numLatexReRuns > 0) {
            // rerun LaTeX without makeindex and makeglossaries
            this.log.debug("Rerun " + latexCmd +
                    " to update table of contents, ... " +
                    "bibliography, index, or that like. ");
            // may throw BuildFailureException TEX01
            // may log warnings EEX01, EEX02, EEX03, WEX04, WEX05,
            // EAP01, EAP02, WAP04, WFU03
            runLatex2dev(desc, dev);
            numLatexReRuns--;
        }
        assert numLatexReRuns == 0 || numLatexReRuns == 1;

        // rerun latex by need patternRerunMakeIndex
        boolean needMakeIndexReRun;
        boolean needLatexReRun = (numLatexReRuns == 1)
                || needRun(true, latexCmd, desc.logFile,
                        this.settings.getPatternReRunLatex());

        int maxNumReruns = this.settings.getMaxNumReRunsLatex();
        for (int num = 0; maxNumReruns == -1 || num < maxNumReruns; num++) {
            needMakeIndexReRun = needRun(true,
                    this.settings.getCommand(ConverterCategory.MakeIndex),
                    desc.logFile,
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
                // may log warnings EEX01, EEX02, EEX03, WEX04, WEX05,
                // EAP01, EAP02, WLP04, WLP05, WAP03, WAP04, WFU03
                runMakeIndexByNeed(desc);
            }

            // may throw BuildFailureException TEX01
            // may log warnings EEX01, EEX02, EEX03, WEX04, WEX05,
            // EAP01, EAP02, WAP04, WFU03
            runLatex2dev(desc, dev);
            needLatexReRun = needRun(true, latexCmd, desc.logFile,
                    this.settings.getPatternReRunLatex());
        }
        this.log.warn("WLP01: LaTeX requires rerun but maximum number " +
                maxNumReruns + " reached. ");
    }

    /**
     * Returns whether a(n other) run (see <code>another</code>)
     * of the application <code>application</code> is necessary
     * based on a pattern <code>pattern</code>
     * matching in the log file <code>logFile</code>.
     * Note that only <code>logFile</code> and <code>pattern</code>
     * are required unless a warning needs to be issued.
     * <p>
     * Logging:
     * <ul>
     * <li>WLP02: Cannot read log file: (re)run required?
     * <li>WFU03: cannot close log file
     * </ul>
     *
     * @param another
     *                whether it is requested whether another run (a 'rerun') is
     *                required.
     *                If false, just a run is required
     * @param cmdStr
     *                Determines the command string of the application to be rerun.
     *                This may be of category {@link ConverterCategory#LaTeX},
     *                {@link ConverterCategory#MakeIndex}
     *                but also {@link ConverterCategory#BibTeX}.
     * @param logFile
     *                the log file which determines
     *                whether to rerun <code>cmdStr</code>.
     * @param pattern
     *                the pattern in the <code>logFile</code>
     *                which determines whether to rerun <code>cmdStr</code>.
     * @return
     *         whether <code>cmdStr</code> needs to be rerun
     *         based on a pattern <code>pattern</code>
     *         matching in the log file <code>logFile</code>.
     * @see TexFileUtils#matchInFile(File, String)
     */
    // used in processLatex2devCore and in runBibtexByNeed only
    // TBD: eliminate Converter again and replace by ConverterCategory
    // including also the rerun pattern.
    private boolean needRun(boolean another,
            String cmdStr,
            File logFile,
            String pattern) {
        // may log warning WFU03: cannot close
        Boolean needRun = this.fileUtils.matchInFile(logFile, pattern);
        if (needRun == null) {
            this.log.warn("WLP02: Cannot read log file '" +
                    logFile.getName() + "'; " +
                    cmdStr + " may require " +
                    (another ? "re" : "") + "run. ");
            return false;
        }
        return needRun;
    }

    /**
     * Runs LaTeX, BibTeX, MakeIndex and MakeGlossaries
     * on the latex main file <code>texFile</code>
     * described by <code>desc</code>
     * repeatedly as described for
     * {@link #processLatex2devCore(LatexProcessor.LatexMainDesc, LatexDev)}
     * and issue a warning if the last LaTeX run issued a warning.
     * </ul>
     * <p>
     * Logging:
     * <ul>
     * <li>WFU03: cannot close
     * <li>WAP04: if <code>logFile</code> is not readable.
     * <li>WLP01: if another rerun is required
     * but the maximum number of runs is reached.
     * <li>WLP03: <code>command</code> created bad boxes
     * <li>WLP04: <code>command</code> emitted warnings
     * <li>WLP04: Cannot read idx file; skip creation of index
     * <li>WLP05: Use package 'splitidx' without option 'split'
     * <li>EEX01, EEX02, EEX03, WEX04, WEX05:
     * {@link #processLatex2devCore(LatexProcessor.LatexMainDesc, LatexDev)}
     * if running an exernal command fails.
     * </ul>
     *
     * @param desc
     *             the description of a latex main file <code>texFile</code>
     *             to be processed.
     * @param dev
     *             the device describing the output format which is either pdf or
     *             dvi.
     *             See {@link LatexDev#getLatexOutputFormat()}.
     * @throws BuildFailureException
     *                               TEX01 as for
     *                               {@link #processLatex2devCore(LatexProcessor.LatexMainDesc, LatexDev)}.
     * @see #needRun(boolean, String, File, String)
     * @see Target#pdf
     */
    private void processLatex2dev(LatexMainDesc desc, LatexDev dev)
            throws BuildFailureException {
        // may throw BuildFailureException TEX01,
        // log warning EAP01, EAP02, WAP04, WLP02, WFU03, WLP04, WLP05,
        // EEX01, EEX02, EEX03, WEX04, WEX05
        processLatex2devCore(desc, dev);

        // emit warnings (errors are emitted by runLatex2dev and that like.)
        // may log warnings WFU03, WAP04, WLP03, WLP04
        logWarns(desc.logFile, this.settings.getCommand(ConverterCategory.LaTeX));
    }

    void processLatex2dvi(File texFile) throws BuildFailureException {
        this.log.info("Converting into dvi:  LaTeX file '" + texFile + "'. ");
        LatexMainDesc desc = getLatexMainDesc(texFile);
        // may throw BuildFailureException TEX01,
        // log warning EEX01, EEX02, EEX03, WEX04, WEX05
        // WFU03, WAP04, WLP03, WLP04
        processLatex2dev(desc, LatexDev.dvips);
    }

    void processLatex2pdf(File texFile) throws BuildFailureException {
        this.log.info("Converting into pdf:  LaTeX file '" + texFile + "'. ");
        LatexMainDesc desc = getLatexMainDesc(texFile);
        LatexDev dev = this.settings.getPdfViaDvi();

        // may throw BuildFailureException TEX01,
        // log warning EEX01, EEX02, EEX03, WEX04, WEX05, WLP04, WLP05
        processLatex2dev(desc, dev);
        // FIXME: certain figures are invisible in the intermediate dvi file,
        // but converstion to pdf shows that the figures are present.

        if (dev.isViaDvi()) {
            // may throw BuildFailureException TEX01,
            // may log warning EEX01, EEX02, EEX03, WEX04, WEX05
            runDvi2pdf(desc);
        }
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
     * <li>EAP01: Running <code>command</code> failed. For details...
     * <li>EAP02: Running <code>command</code> failed. No log file
     * <li>WAP04: if <code>logFile</code> is not readable.
     * <li>WFU03: cannot close
     * </ul>
     */
    private void logErrs(File logFile, String command) {
        // may log warnings WFU03, EAP01, EAP02, WAP04
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
     * another LaTeX rerun is required
     * beyond {@link Settings#maxNumReRunsLatex},
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
     * <li>WFU03: cannot close
     * <li>WAP04: if <code>logFile</code> is not readable.
     * <li>WLP03: <code>command</code> created bad boxes
     * <li>WLP04: <code>command</code> emitted warnings
     * </ul>
     *
     * @param logFile
     *                the log-file to detect warnings in.
     * @param command
     *                the command which created <code>logFile</code>
     *                and which maybe created warnings.
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
     * <li>EAP01: Running <code>command</code> failed. For details...
     * <li>EAP02: Running <code>command</code> failed. No log file
     * <li>WAP04: if <code>logFile</code> is not readable.
     * <li>WLP02: Cannot read blg file: BibTeX run required?
     * <li>WFU03: cannot close log file
     * <li>WLP04: Cannot read idx file; skip creation of index
     * <li>WLP05: Use package 'splitidx' without option 'split'
     * <li>EEX01, EEX02, EEX03, WEX04, WEX05:
     * if running an exernal command fails.
     * </ul>
     *
     * @param texFile
     *                the tex file to be processed.
     * @throws BuildFailureException
     *                               TEX01 as for
     *                               {@link #preProcessLatex2dev(LatexProcessor.LatexMainDesc, LatexDev)}
     *                               but also as for
     *                               {@link #runLatex2html(LatexProcessor.LatexMainDesc)}.
     * @see #preProcessLatex2dev(LatexProcessor.LatexMainDesc, LatexDev)
     * @see #runLatex2html(LatexProcessor.LatexMainDesc)
     * @see Target#html
     */
    void processLatex2html(File texFile) throws BuildFailureException {
        this.log.info("Converting into html: LaTeX file '" + texFile + "'. ");
        LatexMainDesc desc = getLatexMainDesc(texFile);
        // may throw BuildFailureException TEX01,
        // log warning EAP01, EAP02, WLP04, WLP05, WAP04, WLP02, WFU03,
        // EEX01, EEX02, EEX03, WEX04, WEX05
        preProcessLatex2dev(desc, this.settings.getPdfViaDvi());
        // may throw BuildFailureException TEX01,
        // log warning EEX01, EEX02, EEX03, WEX04, WEX05
        runLatex2html(desc);
    }

    /**
     * Runs conversion of <code>texFile</code>
     * to odt or other open office formats
     * after processing latex to set up the references,
     * bibliography, index and that like.
     * <p>
     * Logging: FIXME: incomplete
     * <ul>
     * <li>EAP01: Running <code>command</code> failed. For details...
     * <li>EAP02: Running <code>command</code> failed. No log file
     * <li>WAP04: if <code>logFile</code> is not readable.
     * <li>WLP02: Cannot read blg file: BibTeX run required?
     * <li>WFU03: cannot close log file
     * <li>WLP04: Cannot read idx file; skip creation of index
     * <li>WLP05: Use package 'splitidx' without option 'split'
     * <li>EEX01, EEX02, EEX03, WEX04, WEX05:
     * if running an exernal command fails.
     * </ul>
     *
     * @param texFile
     *                the tex file to be processed.
     * @throws BuildFailureException
     *                               TEX01 as for
     *                               {@link #preProcessLatex2dev(LatexProcessor.LatexMainDesc, LatexDev)}
     *                               but also as for
     *                               {@link #runLatex2odt(LatexProcessor.LatexMainDesc)}.
     * @see #preProcessLatex2dev(LatexProcessor.LatexMainDesc, LatexDev)
     * @see #runLatex2odt(LatexProcessor.LatexMainDesc)
     * @see Target#odt
     */
    void processLatex2odt(File texFile) throws BuildFailureException {
        this.log.info("Converting into odt:  LaTeX file '" + texFile + "'. ");
        LatexMainDesc desc = getLatexMainDesc(texFile);
        // may throw BuildFailureException TEX01,
        // log warning EAP01, EAP02, WAP04, WLP02, WFU03, WLP04, WLP05
        // EEX01, EEX02, EEX03, WEX04, WEX05
        preProcessLatex2dev(desc, this.settings.getPdfViaDvi());
        // may throw BuildFailureException TEX01,
        // log warning EEX01, EEX02, EEX03, WEX04, WEX05
        runLatex2odt(desc);
    }

    /**
     * Runs conversion of <code>texFile</code>
     * to docx or other MS word formats
     * after processing latex to set up the references,
     * bibliography, index and that like.
     * <p>
     * Logging: FIXME: incomplete
     * <ul>
     * <li>EAP01: Running <code>command</code> failed. For details...
     * <li>EAP02: Running <code>command</code> failed. No log file
     * <li>WAP04: if <code>logFile</code> is not readable.
     * <li>WLP02: Cannot read blg file: BibTeX run required?
     * <li>WFU03: cannot close log file
     * <li>WLP04: Cannot read idx file; skip creation of index
     * <li>WLP05: Use package 'splitidx' without option 'split'
     * <li>EEX01, EEX02, EEX03, WEX04, WEX05:
     * if running an exernal command fails.
     * </ul>
     *
     * @param texFile
     *                the latex main file to be processed.
     * @throws BuildFailureException
     *                               TEX01 as for
     *                               {@link #preProcessLatex2dev(LatexProcessor.LatexMainDesc, LatexDev)}
     *                               but also as for
     *                               {@link #runLatex2odt(LatexProcessor.LatexMainDesc)}
     *                               and for {@link #runOdt2doc(File)}.
     * @see #preProcessLatex2dev(LatexMainDesc, LatexDev)
     * @see #runLatex2odt(LatexProcessor.LatexMainDesc)
     * @see #runOdt2doc(File)
     * @see Target#docx
     */
    void processLatex2docx(File texFile) throws BuildFailureException {
        this.log.info("Converting into doc(x): LaTeX file '" + texFile + "'. ");
        LatexMainDesc desc = getLatexMainDesc(texFile);
        // may throw BuildFailureException TEX0,
        // log warning EAP01, EAP02, WAP04, WLP02, WFU03, WLP04, WLP05
        // EEX01, EEX02, EEX03, WEX04, WEX05
        preProcessLatex2dev(desc, this.settings.getPdfViaDvi());
        // may throw BuildFailureException TEX0,
        // log warning EEX01, EEX02, EEX03, WEX04, WEX05
        runLatex2odt(desc);
        // may throw BuildFailureException TEX01,
        // log warning EEX01, EEX02, EEX03, WEX04, WEX05
        runOdt2doc(texFile);
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
     * <li>EEX01, EEX02, EEX03, WEX04, WEX05:
     * if running an exernal command fails.
     * </ul>
     *
     * @param texFile
     *                the tex file to be processed.
     * @throws BuildFailureException
     *                               TEX01 if running the latex2rtf command
     *                               returned by
     *                               {@link Settings#getLatex2rtfCommand()} failed.
     * @see #runLatex2rtf(File)
     * @see Target#rtf
     */
    void processLatex2rtf(File texFile) throws BuildFailureException {
        this.log.info("Converting into rtf:  LaTeX file '" + texFile + "'. ");
        // may throw BuildFailureException TEX01,
        // log warning EEX01, EEX02, EEX03, WEX04, WEX05
        runLatex2rtf(texFile);
    }

    /**
     * Runs conversion of <code>texFile</code> to txt format via pdf.
     * <p>
     * Logging: FIXME: incomplete
     * <ul>
     * <li>WLP04: Cannot read idx file; skip creation of index
     * <li>WLP05: Use package 'splitidx' without option 'split'
     * <li>EEX01, EEX02, EEX03, WEX04, WEX05:
     * if running an exernal command fails.
     * </ul>
     *
     * @param texFile
     *                the tex file to be processed.
     * @throws BuildFailureException
     *                               TEX01 as for
     *                               {@link #processLatex2devCore(LatexProcessor.LatexMainDesc, LatexDev)}
     *                               and for {@link #runPdf2txt(File)}.
     * @see #processLatex2devCore(LatexProcessor.LatexMainDesc, LatexDev)
     * @see #runPdf2txt(File)
     * @see Target#txt
     */
    void processLatex2txt(File texFile) throws BuildFailureException {
        this.log.info("Converting into txt:  LaTeX file '" + texFile + "'. ");
        LatexMainDesc desc = getLatexMainDesc(texFile);
        LatexDev dev = this.settings.getPdfViaDvi();

        // may throw BuildFailureException TEX01,
        // log warning EAP01, EAP02, WAP04, WLP02, WFU03, WLP04, WLP05,
        // EEX01, EEX02, EEX03, WEX04, WEX05
        processLatex2devCore(desc, dev);
        if (dev.isViaDvi()) {
            // may throw BuildFailureException TEX01,
            // may log warning EEX01, EEX02, EEX03, WEX04, WEX05
            runDvi2pdf(desc);
        }

        // warnings emitted by LaTex are ignored
        // (errors are emitted by runLatex2dev and that like.)
        // may throw BuildFailureException TEX01,
        // log warning EEX01, EEX02, EEX03, WEX04, WEX05
        runPdf2txt(texFile);
    }

    /**
     * Prints meta information, mainly version information
     * on this software and on the converters used.
     * <p>
     * WMI01: If the version string of a converter cannot be read.
     * WMI02: If the version of a converter is not as expected.
     *
     * @param includeVersionInfo
     *                           whether to include plain version info; else
     *                           warnings only.
     * @return
     *         whether a warning has been issued.
     * @throws BuildFailureException
     *                               <ul>
     *                               <li>TMI01: if the stream to either the manifest
     *                               file
     *                               or to a property file, either
     *                               {@LINK #VERSION_PROPS_FILE}
     *                               or
     *                               {@link MetaInfo.GitProperties#GIT_PROPS_FILE}
     *                               could not be created.</li>
     *                               <li>TMI02: if the properties could not be read
     *                               from one of the two property files mentioned
     *                               above.</li>
     *                               <li>TSS05: if converters are excluded in the
     *                               pom which are not known.</li>
     *                               </ul>
     */
    public boolean printMetaInfo(boolean includeVersionInfo)
            throws BuildFailureException {
        return this.metaInfo.printMetaInfo(includeVersionInfo);
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
     * <li>EAP01: Running <code>bibtex</code> failed. For details...
     * <li>EAP02: Running <code>bibtex</code> failed. No log file
     * <li>WAP03: Running <code>bibtex</code> emitted warnings.
     * <li>WAP04: if <code>logFile</code> is not readable.
     * <li>WLP02: Cannot read log file: run required?
     * <li>WFU03: cannot close
     * <li>EEX01, EEX02, EEX03, WEX04, WEX05:
     * if running the BibTeX command failed.
     * </ul>
     *
     * @param texFile
     *                the latex main file BibTeX is to be processed for.
     * @return
     *         whether BibTeX has been run.
     *         Equivalently, whether LaTeX has to be rerun because of BibTeX.
     * @throws BuildFailureException
     *                               TEX01 if invocation of the BibTeX command
     *                               returned by {@link Settings#getBibtexCommand()}
     *                               failed.
     */
    private boolean runBibtexByNeed(File texFile) throws BuildFailureException {
        File auxFile = TexFileUtils.replaceSuffix(texFile, SUFFIX_AUX);
        String command = this.settings.getCommand(ConverterCategory.BibTeX);
        if (!needRun(false, command, auxFile, PATTERN_NEED_BIBTEX_RUN)) {
            return false;
        }

        this.log.debug("Running " + command +
                " on '" + auxFile.getName() + "'. ");
        String[] args = buildArguments(this.settings.getBibtexOptions(),
                auxFile);
        // may throw BuildFailureException TEX01,
        // may log warning EEX01, EEX02, EEX03, WEX04, WEX05
        this.executor.execute(texFile.getParentFile(), // workingDir
                this.settings.getTexPath(),
                command,
                args,
                TexFileUtils.replaceSuffix(texFile, SUFFIX_BBL));

        File logFile = TexFileUtils.replaceSuffix(texFile, SUFFIX_BLG);
        // may log EAP01, EAP02, WAP04, WFU03
        logErrs(logFile, command, this.settings.getPatternErrBibtex());
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
     * Note that {@link Settings#getMakeIndexCommand()}
     * is invoked either directly, or, in case of a multiple index,
     * via {@link Settings#getSplitIndexCommand()}.
     * <p>
     * Logging:
     * <ul>
     * <li>WLP04: Cannot read idx file; skip creation of index
     * <li>WLP05: Use package 'splitidx' without option 'split'
     * <li>EAP01: Running <code>makeindex</code> failed. For details...
     * <li>EAP02: Running <code>makeindex</code> failed. No log file
     * <li>WAP03: Running <code>makeindex</code> emitted warnings.
     * <li>WAP04: .ilg-file is not readable.
     * <li>WFU03: cannot close .ilg-file
     * <li>EEX01, EEX02, EEX03, WEX04, WEX05:
     * if running the makeindex command failed.
     * </ul>
     *
     * @param desc
     *             the description of a latex main file <code>dviFile</code>
     *             including the idx-file MakeIndex is to be run on.
     * @return
     *         whether MakeIndex had been run.
     *         Equivalently, whether LaTeX has to be rerun because of MakeIndex.
     * @throws BuildFailureException
     *                               TEX01 if invocation of the makeindex command
     *                               returned by
     *                               {@link Settings#getMakeIndexCommand()} failed.
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

        // determine the explicit given identifiers of indices
        Collection<String> explIdxIdent = null;
        if (needRun) {
            explIdxIdent = this.fileUtils
                    .collectMatches(desc.idxFile, IDX_EXPL, GRP_IDENT_IDX);
            if (explIdxIdent == null) {
                this.log.warn("WLP04: Cannot read idx file '" +
                        desc.idxFile.getName() +
                        "'; skip creation of index. ");
                return false;
            }
        }
        assert (explIdxIdent != null) == needRun;
        // Here, explIdxIdent contains the explicit identifiers of all indices
        // The identifier idx may be missing or not.

        // package splitidx is used with option split
        // is in general not allowed. The criteria are:
        // - if \jobname-xxx.idx exists for some xxx
        // whereas \jobname.idx does not:
        // This occurs only for option split
        // and does not allow applying splitindex
        // as intended in this software.
        // This would require applying makeindex separately
        // to all \jobname-xxx.idx
        // - if \jobname-xxx.idx exists for some xxx
        // and also \jobname.idx exists but has no entry
        // \indexentry[xxx]{...}{..}:
        // This occurs only for option split
        // and applying splitindex yields the wrong result.
        // This would require applying makeindex separately
        // to all \jobname-xxx.idx and to \jobname.idx
        // - if \jobname-xxx.idx does not exist for any xxx
        // then all is ok, whether \jobname.idx exists or not.
        // If it exists, even splitidx with option split is ok.

        // so algorithm:
        // determine list of these xxx for which \jobname-xxx.idx exists
        // if (\jobname-xxx.idx exists for some xxx) {
        // if (!(\jobname.idx exists &&
        // \jobname.idx matches some \indexentry[xxx]{...}{.. )) {
        // log.error(cannot handle splitidx with option split)
        // return false;
        // }
        // // For second condition,
        // // determine list of all yyy matching \indexentry[yyy]{...}{..}
        // // and make sure that it is non-empty.
        // }

        // filter for extended raw idx-files: \jobname-xxx.idx
        FileFilter filter = this.fileUtils
                .getFileFilterReplace(desc.idxFile, SEP_IDENT_IDX + ".+");
        // may cause WFU01: Cannot read directory
        File[] idxFilesExtInDir = this.fileUtils
                .listFilesOrWarn(desc.idxFile.getParentFile(), filter);

        // If the directory compising idxFile is not readable,
        // idxFilesExtInDir == null
        // Then the check for option split cannot be done.

        if (idxFilesExtInDir != null && idxFilesExtInDir.length > 0) {
            // Here, idxFilesExtInDir contains the idx-files \jobname-xxx.idx
            if (!needRun || explIdxIdent.isEmpty()) {
                // Here, either \jobname.idx does not exist at all
                // or does not contain an entry \indexentry[yyy]{...}{..}

                this.log.warn("WLP05: Use package 'splitidx' " +
                        "without option 'split' in '" +
                        desc.texFile.getName() + "'. ");
                // this.log.warn("WLP05: Found extended idx-file " +
                // " without (according entry in) '" +
                // desc.idxFile.getName() +
                // "': use package 'splitidx' " +
                // "without option 'split'. ");
            }
        }

        if (needRun) {
            // Here, runMakeIndex or runSplitIndex must be performed

            // check whether more than one index has to be created
            if (explIdxIdent.isEmpty()) {
                // may throw BuildFailureException TEX01
                // may log warnings EEX01, EEX02, EEX03, WEX04, WEX05,
                // EAP01, EAP02, WAP03, WAP04, WFU03
                runMakeIndex(desc);
            } else {
                // may throw BuildFailureException TEX01
                // may log warnings EEX01, EEX02, EEX03, WEX04, WEX05,
                // EAP01, EAP02, WAP03, WAP04, WFU03
                runSplitIndex(desc, explIdxIdent);
            }
        }
        return needRun;
    }

    /**
     * Runs the MakeIndex command
     * given by {@link Settings#getMakeIndexCommand()}.
     * <p>
     * Logging:
     * <ul>
     * <li>EAP01: Running <code>makeindex</code> failed. For details...
     * <li>EAP02: Running <code>makeindex</code> failed. No log file
     * <li>WAP03: Running <code>makeindex</code> emitted warnings.
     * <li>WAP04 .ilg-file is not readable.
     * <li>WFU03: cannot close .ilg-file
     * <li>EEX01, EEX02, EEX03, WEX04, WEX05:
     * if running the makeindex command failed.
     * </ul>
     *
     * @param desc
     *             the description of a latex main file <code>texFile</code>
     *             including the idx-file MakeIndex is to be run on.
     * @throws BuildFailureException
     *                               TEX01 if invocation of the MakeIndex command
     *                               returned by
     *                               {@link Settings#getMakeIndexCommand()} failed.
     */
    private void runMakeIndex(LatexMainDesc desc)
            throws BuildFailureException {

        String command = this.settings.getCommand(ConverterCategory.MakeIndex);
        File idxFile = desc.idxFile;
        this.log.debug("Running " + command +
                " on '" + idxFile.getName() + "'. ");
        String[] args = buildArguments(this.settings.getMakeIndexOptions(),
                idxFile);
        // may throw BuildFailureException TEX01,
        // may log warning EEX01, EEX02, EEX03, WEX04, WEX05
        this.executor.execute(idxFile.getParentFile(), // workingDir
                this.settings.getTexPath(),
                command,
                args,
                desc.indFile);

        // detect errors and warnings makeindex wrote into xxx.ilg
        // may log EAP01, EAP02, WAP04, WFU03
        logErrs(desc.ilgFile, command, this.settings.getPatternErrMakeIndex());
        // may log warnings WFU03, WAP03, WAP04
        logWarns(desc.ilgFile, command, this.settings.getPatternWarnMakeIndex());
    }

    /**
     * Combines an array of files from a file prefix <code>filePrefix</code>,
     * a collection of intermediate strings <code>variant</code>
     * and of the suffix <code>suffix</code>.
     *
     * @param filePrefix
     *                   prefix of file name; in practice of index file without
     *                   suffix
     * @param variant
     *                   collection of strings; in practice set of identifiers of
     *                   indices
     * @param suffix
     *                   the suffix of a file; in practice {@link #SUFFIX_IDX}.
     * @return
     *         an array of file names of the form
     *         <code>&lt;filePrefix&gt;&lt;ident&gt;&lt;suffix&gt;</code>,
     *         where <code>ident</code> runs in <code>variant</code>.
     */
    private File[] files(String filePrefix,
            Collection<String> variant,
            String suffix) {
        File[] res = new File[variant.size()];
        int idx = 0;
        StringBuilder strb;
        for (String idxIdent : variant) {
            strb = new StringBuilder();
            strb.append(filePrefix);
            strb.append(idxIdent);
            strb.append(suffix);

            res[idx++] = new File(strb.toString());
        }
        return res;
    }

    /**
     * Runs the SplitIndex command
     * given by {@link Settings#getSplitIndexCommand()}.
     * <p>
     * Logging:
     * Note that <code>splitindex</code> neither writes a log file
     * nor may it fail in itself but invoking <code>makeindex</code>
     * or whatever program it uses.
     * <ul>
     * <li>EAP01: Running <code>splitindex</code> failed. For details...
     * <li>EAP02: Running <code>splitindex</code> failed. No log file
     * <li>WAP03: Running <code>splitindex</code> emitted warnings.
     * <li>WAP04 .ilg-file is not readable.
     * <li>WFU03: cannot close .ilg-file
     * <li>EEX01, EEX02, EEX03, WEX04, WEX05:
     * if running the splitindex command failed.
     * </ul>
     *
     * @param desc
     *                     the description of a latex main file <code>texFile</code>
     *                     including the idx-file SplitIndex is to be run on.
     * @param explIdxIdent
     *                     the set of identifiers of indices,
     *                     whether explicitly given or not in the idx file.
     * @throws BuildFailureException
     *                               TEX01 if invocation of the SplitIndex command
     *                               returned by
     *                               {@link Settings#getSplitIndexCommand()} failed.
     */
    private void runSplitIndex(LatexMainDesc desc,
            Collection<String> explIdxIdent)
            throws BuildFailureException {

        String splitInxCmd = this.settings.getCommand(ConverterCategory.SplitIndex);
        File idxFile = desc.idxFile;
        this.log.debug("Running " + splitInxCmd +
                " on '" + idxFile.getName() + "'. ");
        // buildArguments(this.settings.getMakeIndexOptions(), idxFile);
        String[] argsDefault = new String[] {
                "-m " + this.settings.getCommand(ConverterCategory.MakeIndex),
                // **** no splitindex.tlu
                // This is hardcoded by splitidx when writing xxx.ind
                "-i " + IDX_EXPL,
                // This is hardcoded by makeindex when writing xxx.ind
                "-r $1$3", // groups in IDX_EXPL: \indexentry{...}
                // -s -$2 is hardcoded by splitidx when readin in the -xxx.ind-files
                "-s " + SEP_IDENT_IDX + "$" + GRP_IDENT_IDX // groups in IDX_EXPL
                // **** Here, only -V may occur in addition.
                // "-V",
                // desc.xxxFile.getName()
        };

        String argsOption = this.settings.getMakeIndexOptions();
        String[] args = argsOption.isEmpty()
                ? new String[argsDefault.length + 1]
                : new String[argsDefault.length + 2];
        System.arraycopy(argsDefault, 0, args, 0, argsDefault.length);
        if (!argsOption.isEmpty()) {
            args[args.length - 2] = argsOption;
        }
        args[args.length - 1] = desc.xxxFile.getName();

        String optionsMakeIndex = this.settings.getMakeIndexOptions();
        if (!optionsMakeIndex.isEmpty()) {
            String[] optionsMake_IndexArr = optionsMakeIndex.split(" ");
            String[] optionsSplitIndexArr = args;
            args = new String[optionsMake_IndexArr.length + 1 +
                    optionsSplitIndexArr.length];
            System.arraycopy(optionsSplitIndexArr, 0, args, 0,
                    optionsSplitIndexArr.length);
            args[optionsSplitIndexArr.length] = "--";
            System.arraycopy(optionsMake_IndexArr, 0, args,
                    optionsSplitIndexArr.length + 1,
                    optionsMake_IndexArr.length);
        }

        // determine the resulting ind-files
        explIdxIdent.add(IMPL_IDENT_IDX);
        String filePrefix = desc.xxxFile.toString() + SEP_IDENT_IDX;
        File[] indFiles = files(filePrefix, explIdxIdent, SUFFIX_IND);

        // may throw BuildFailureException TEX01,
        // may log warning EEX01, EEX02, EEX03, WEX04, WEX05
        this.executor.execute(idxFile.getParentFile(), // workingDir
                this.settings.getTexPath(),
                splitInxCmd,
                args,
                indFiles);

        // detect errors and warnings splitindex,
        // aka makeindex wrote into xxx.ilg
        File[] ilgFiles = files(filePrefix, explIdxIdent, SUFFIX_ILG);
        splitInxCmd = this.settings.getCommand(ConverterCategory.MakeIndex);
        for (int idx = 0; idx < explIdxIdent.size(); idx++) {
            // may log EAP01, EAP02, WAP04, WFU03
            logErrs(ilgFiles[idx], splitInxCmd,
                    this.settings.getPatternErrMakeIndex());
            // may log warnings WFU03, WAP03, WAP04
            logWarns(ilgFiles[idx], splitInxCmd,
                    this.settings.getPatternWarnMakeIndex());
        }
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
     * <li>EAP01: Running <code>makeglossaries</code> failed. For details...
     * <li>EAP02 Running <code>makeglossaries</code> failed. No log file
     * <li>WAP03: Running <code>makeglossaries</code> emitted warnings.
     * <li>WAP04: .glg-file is not readable.
     * <li>WFU03: cannot close .glg-file
     * <li>EEX01, EEX02, EEX03, WEX04, WEX05:
     * if running the makeglossaries command failed.
     * </ul>
     *
     * @param desc
     *             the description of a latex main file <code>texFile</code>
     *             including the idx-file MakeGlossaries is to be run on.
     * @return
     *         whether MakeGlossaries had been run.
     *         Equivalently,
     *         whether LaTeX has to be rerun because of MakeGlossaries.
     * @throws BuildFailureException
     *                               TEX01 if invocation of the makeglossaries
     *                               command
     *                               returned by
     *                               {@link Settings#getMakeGlossariesCommand()}
     *                               failed.
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
        String command = this.settings.getCommand(ConverterCategory.MakeGlossaries);
        this.log.debug("Running " + command +
                " on '" + xxxFile.getName() + "'. ");
        String[] args = buildArguments(this.settings.getMakeGlossariesOptions(),
                xxxFile);
        // may throw BuildFailureException TEX01,
        // may log warning EEX01, EEX02, EEX03, WEX04, WEX05
        this.executor.execute(xxxFile.getParentFile(), // workingDir
                this.settings.getTexPath(),
                command,
                args,
                desc.glsFile);

        // detect errors and warnings makeglossaries wrote into xxx.glg
        File glgFile = desc.glgFile;
        // may log EAP01, EAP02, WAP04, WFU03
        logErrs(glgFile, command, this.settings.getPatternErrMakeGlossaries());
        // may log warnings WFU03, WAP03, WAP04
        logWarns(glgFile, command, this.settings.getPatternWarnMakeIndex()
                + "|" + this.settings.getPatternWarnXindy());
        return true;
    }

    /**
     * Runs the LaTeX command given by {@link Settings#getLatex2pdfCommand()}
     * on the latex main file <code>texFile</code>
     * described by <code>desc</code>
     * in the directory containing <code>texFile</code> with arguments
     * given by {@link #buildLatexArguments(Settings, LatexDev, File)}.
     * The output format of the LaTeX run is given by <code>dev</code>,
     * to be more precise by {@link LatexDev#getLatexOutputFormat()}.
     * <p>
     * Logs a warning or an error if the latex run failed
     * invoking {@link #logErrs(File, String)}
     * but not if bad boxes occurred or if warnings occurred.
     * This is done in
     * {@link #processLatex2dev(LatexProcessor.LatexMainDesc, LatexDev)}
     * after the last LaTeX run only.
     * <p>
     * Logging:
     * <ul>
     * <li>EAP01: Running <code>latex2pdf</code> failed. For details...
     * <li>EAP02: Running <code>latex2pdf</code> failed. No log file
     * <li>WAP04: .log-file is not readable.
     * <li>WFU03: cannot close .log-file
     * <li>EEX01, EEX02, EEX03, WEX04, WEX05:
     * if running the latex2pdf command failed.
     * </ul>
     *
     * @param desc
     *             the description of a latex main file <code>texFile</code>
     *             to be processed.
     * @param dev
     *             the device describing the output format which is either pdf or
     *             dvi.
     *             See {@link LatexDev#getLatexOutputFormat()}.
     * @throws BuildFailureException
     *                               TEX01 if invocation of the latex2pdf command
     *                               returned by
     *                               {@link Settings#getLatex2pdfCommand()} failed.
     */
    private void runLatex2dev(LatexMainDesc desc, LatexDev dev)
            throws BuildFailureException {

        File texFile = desc.texFile;
        // FIXME: wrong name; better is latex2dev
        String command = this.settings.getCommand(ConverterCategory.LaTeX);
        this.log.debug("Running " + command +
                " on '" + texFile.getName() + "'. ");
        String[] args = buildLatexArguments(this.settings, dev, texFile);
        // may throw BuildFailureException TEX01,
        // may log warning EEX01, EEX02, EEX03, WEX04, WEX05
        this.executor.execute(texFile.getParentFile(), // workingDir
                this.settings.getTexPath(),
                command,
                args,
                dev.latexTargetFile(desc));

        // logging errors (warnings are done in processLatex2pdf)
        // may log EAP01, EAP02, WAP04, WFU03
        logErrs(desc.logFile, command);

        // FIXME: documentation that in the dvi file,
        // png, jpg and svg are not visible, but present.
    }

    // also for tests
    protected static String[] buildLatexArguments(Settings settings,
            LatexDev dev,
            File texFile) {
        // FIXME: hack with literal
        return buildArguments(settings.getLatex2pdfOptions() +
                " -output-format=" + dev.getLatexOutputFormat(),
                texFile);
    }

    /**
     * Runs conversion from dvi to pdf-file
     * executing {@link Settings#getDvi2pdfCommand()}
     * on a dvi-file covered by <code>desc</code> with arguments
     * given by {@link #buildLatexArguments(Settings, LatexDev, File)}.
     * <p>
     * Logging:
     * <ul>
     * <li>EEX01, EEX02, EEX03, WEX04, WEX05:
     * if running the dvi2pdf command failed.
     * </ul>
     *
     * @param desc
     *             the description of a latex main file <code>dviFile</code>
     *             including the dvi-file dvi2pdf is to be run on.
     * @throws BuildFailureException
     *                               TEX01 if invocation of the dvi2pdf command
     *                               returned by
     *                               {@link Settings#getDvi2pdfCommand()} failed.
     */
    // used in processLatex2pdf(File) and processLatex2txt(File) only
    private void runDvi2pdf(LatexMainDesc desc) throws BuildFailureException {
        assert this.settings.getPdfViaDvi().isViaDvi();

        String command = this.settings.getCommand(ConverterCategory.Dvi2Pdf);
        this.log.debug("Running " + command +
                " on '" + desc.dviFile.getName() + "'. ");
        String[] args = buildArguments(this.settings.getDvi2pdfOptions(),
                desc.dviFile);
        // may throw BuildFailureException TEX01,
        // may log warning EEX01, EEX02, EEX03, WEX04, WEX05
        this.executor.execute(desc.texFile.getParentFile(), // workingDir
                this.settings.getTexPath(),
                command,
                args,
                desc.pdfFile);
        // FIXME: what about error logging?
        // Seems not to create a log-file.
    }

    /**
     * Runs the tex4ht command given by {@link Settings#getTex4htCommand()}
     * on <code>texFile</code> described by <code>desc</code>
     * in the directory containing <code>texFile</code>
     * with arguments given by {@link #buildHtlatexArguments(Settings, File)}.
     * <p>
     * Logging:
     * <ul>
     * <li>EAP01: Running <code>htlatex</code> failed. For details...
     * <li>EAP02: Running <code>htlatex</code> failed. No log file
     * <li>WLP03: <code>htlatex</code> created bad boxes
     * <li>WLP04: <code>htlatex</code> emitted warnings
     * <li>WAP04: log file is not readable.
     * <li>WFU03: cannot close log file
     * <li>EEX01, EEX02, EEX03, WEX04, WEX05:
     * if running the tex4ht command failed.
     * </ul>
     *
     * @param desc
     *             the description of a latex main file <code>texFile</code>
     *             to be processed.
     * @throws BuildFailureException
     *                               TEX01 if invocation of the tex4ht command
     *                               returned by {@link Settings#getTex4htCommand()}
     *                               failed.
     */
    private void runLatex2html(LatexMainDesc desc)
            throws BuildFailureException {

        File texFile = desc.texFile;
        String command = this.settings.getTex4htCommand();
        this.log.debug("Running " + command +
                " on '" + texFile.getName() + "'. ");
        String[] args = buildHtlatexArguments(this.settings, texFile);
        // may throw BuildFailureException TEX01,
        // may log warning EEX01, EEX02, EEX03, WEX04, WEX05
        this.executor.execute(texFile.getParentFile(), // workingDir
                this.settings.getTexPath(),
                command,
                args,
                TexFileUtils.replaceSuffix(texFile, SUFFIX_HTML));

        // logging errors and warnings
        // may log EAP01, EAP02, WAP04, WFU03
        logErrs(desc.logFile, command);
        // may log warnings WFU03, WAP04, WLP03, WLP04
        logWarns(desc.logFile, command);
    }

    protected static String[] buildHtlatexArguments(Settings settings,
            File texFile) {
        return new String[] {
                texFile.getName(),
                settings.getTex4htStyOptions(),
                settings.getTex4htOptions(),
                settings.getT4htOptions(),
                settings.getLatex2pdfOptions()
        };
    }

    /**
     * Runs the latex2rtf command
     * given by {@link Settings#getLatex2rtfCommand()}
     * on <code>texFile</code>
     * in the directory containing <code>texFile</code>
     * with arguments given by {@link #buildArguments(String, File)}.
     * <p>
     * Logging:
     * <ul>
     * <li>EEX01, EEX02, EEX03, WEX04, WEX05:
     * if running the latex2rtf command failed.
     * </ul>
     *
     * @param texFile
     *                the latex file to be processed.
     * @throws BuildFailureException
     *                               TEX01 if invocation of the latex2rtf command
     *                               returned by
     *                               {@link Settings#getLatex2rtfCommand()} failed.
     */
    private void runLatex2rtf(File texFile) throws BuildFailureException {
        String command = this.settings.getCommand(ConverterCategory.LaTeX2Rtf);
        this.log.debug("Running " + command +
                " on '" + texFile.getName() + "'. ");
        String[] args = buildArguments(this.settings.getLatex2rtfOptions(),
                texFile);
        // may throw BuildFailureException TEX01,
        // may log warning EEX01, EEX02, EEX03, WEX04, WEX05
        this.executor.execute(texFile.getParentFile(), // workingDir
                this.settings.getTexPath(),
                command,
                args,
                TexFileUtils.replaceSuffix(texFile, SUFFIX_RTF));

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
     * in the directory containing <code>texFile</code> with arguments
     * given by {@link #buildLatexArguments(Settings, LatexDev, File)}.
     * <p>
     * Logs a warning or an error if the latex run failed
     * invoking {@link #logErrs(File, String)}
     * but not if bad boxes ocurred or if warnings occurred.
     * This is done in
     * {@link #processLatex2dev(LatexProcessor.LatexMainDesc, LatexDev)}
     * after the last LaTeX run only.
     * <p>
     * Logging:
     * <ul>
     * <li>EAP01: Running <code>htlatex</code> failed. For details...
     * <li>EAP02: Running <code>htlatex</code> failed. No log file
     * <li>WLP03: <code>htlatex</code> created bad boxes
     * <li>WLP04: <code>htlatex</code> emitted warnings
     * <li>WAP04: log file is not readable.
     * <li>WFU03: cannot close log file
     * <li>EEX01, EEX02, EEX03, WEX04, WEX05:
     * if running the tex4ht command failed.
     * </ul>
     *
     * @param desc
     *             the descriptor of the latex main file to be processed.
     * @throws BuildFailureException
     *                               TEX01 if invocation of the tex4ht command
     *                               returned by {@link Settings#getTex4htCommand()}
     *                               failed.
     */
    private void runLatex2odt(LatexMainDesc desc) throws BuildFailureException {
        File texFile = desc.texFile;
        String command = this.settings.getTex4htCommand();
        this.log.debug("Running " + command +
                " on '" + texFile.getName() + "'. ");
        String[] args = new String[] {
                texFile.getName(),
                "xhtml,ooffice", // there is no choice here
                "ooffice/! -cmozhtf", // ooffice/! represents a font direcory
                "-coo -cvalidate"// -coo is mandatory, -cvalidate is not
        };
        // may throw BuildFailureException TEX01,
        // may log warning EEX01, EEX02, EEX03, WEX04, WEX05
        this.executor.execute(texFile.getParentFile(),
                this.settings.getTexPath(),
                command,
                args,
                TexFileUtils.replaceSuffix(texFile, SUFFIX_ODT));

        // FIXME: logging refers to latex only, not to tex4ht or t4ht script
        // may log EAP01, EAP02, WAP04, WFU03
        logErrs(desc.logFile, command);
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
     * in the directory containing <code>texFile</code> with arguments
     * given by {@link #buildLatexArguments(Settings, LatexDev, File)}.
     * <p>
     * Logging:
     * <ul>
     * <li>EEX01, EEX02, EEX03, WEX04, WEX05:
     * if running the odt2doc command failed.
     * </ul>
     *
     * @param texFile
     *                the latex file to be processed.
     * @throws BuildFailureException
     *                               TEX01 if invocation of the odt2doc command
     *                               returned by
     *                               {@link Settings#getOdt2docCommand()} failed.
     */
    private void runOdt2doc(File texFile) throws BuildFailureException {
        File odtFile = TexFileUtils.replaceSuffix(texFile, SUFFIX_ODT);
        String command = this.settings.getCommand(ConverterCategory.Odt2Doc);
        this.log.debug("Running " + command +
                " on '" + odtFile.getName() + "'. ");
        String[] args = buildArguments(this.settings.getOdt2docOptions(),
                odtFile);
        String suffix = null;
        for (int idx = 0; idx < args.length - 1; idx++) {
            // FIXME: -f is hardcoded
            if (args[idx].startsWith("-f")) {
                assert suffix == null;// -f comes once only
                // without leading '-f'
                suffix = args[idx].substring(2, args[idx].length());
            }
        }
        assert suffix != null;
        // may throw BuildFailureException TEX01,
        // may log warning EEX01, EEX02, EEX03, WEX04, WEX05
        this.executor.execute(texFile.getParentFile(),
                this.settings.getTexPath(),
                command,
                args,
                TexFileUtils.replaceSuffix(texFile, suffix));
        // FIXME: what about error logging?
        // Seems not to create a log-file.
    }

    /**
     * Runs conversion from pdf to txt-file
     * executing {@link Settings#getPdf2txtCommand()}
     * on a pdf-file created from <code>texFile</code>
     * in the directory containing <code>texFile</code> with arguments
     * given by {@link #buildLatexArguments(Settings, LatexDev, File)}.
     * <p>
     * Logging:
     * <ul>
     * <li>EEX01, EEX02, EEX03, WEX04, WEX05:
     * if running the pdf2txt command failed.
     * </ul>
     *
     * @param texFile
     *                the latex-file to be processed.
     * @throws BuildFailureException
     *                               TEX01 if invocation of the pdf2txt command
     *                               returned by
     *                               {@link Settings#getPdf2txtCommand()} failed.
     */
    private void runPdf2txt(File texFile) throws BuildFailureException {
        File pdfFile = TexFileUtils.replaceSuffix(texFile, SUFFIX_PDF);
        String command = this.settings.getCommand(ConverterCategory.Pdf2Txt);
        this.log.debug("Running " + command +
                " on '" + pdfFile.getName() + "'. ");
        String[] args = buildArguments(this.settings.getPdf2txtOptions(),
                pdfFile);
        // may throw BuildFailureException TEX01,
        // may log warning EEX01, EEX02, EEX03, WEX04, WEX05
        this.executor.execute(texFile.getParentFile(),
                this.settings.getTexPath(),
                command,
                args,
                TexFileUtils.replaceSuffix(texFile, SUFFIX_TXT));
        // FIXME: what about error logging?
        // Seems not to create a log-file.
    }

    /**
     * Runs the check command given by {@link Settings#getChkTexCommand()}
     * on the latex main file <code>texFile</code>
     * in the directory containing <code>texFile</code>
     * creating a log file with ending {@link #SUFFIX_CLG}
     * in that directory.
     * <p>
     * Logging:
     * <ul>
     * <li>EEX01, EEX02, EEX03, WEX04, WEX05:
     * if running the ChkTeX command failed.
     * </ul>
     *
     * @param texFile
     *                the latex main file to be checked for.
     * @throws BuildFailureException
     *                               TEX01 if invocation of the check command
     *                               returned by {@link Settings#getChkTexCommand()}
     *                               failed.
     */
    private void runCheck(File texFile) throws BuildFailureException {
        //
        File clgFile = TexFileUtils.replaceSuffix(texFile, SUFFIX_CLG);
        String command = this.settings.getCommand(ConverterCategory.LatexChk);
        this.log.debug("Running " + command +
                " on '" + texFile.getName() + "'. ");
        String[] args = buildChkTexArguments(this.settings.getChkTexOptions(),
                texFile,
                clgFile);
        // may throw BuildFailureException TEX01,
        // may log warning EEX01, EEX02, EEX03, WEX04, WEX05
        this.executor.execute(texFile.getParentFile(),
                this.settings.getTexPath(),
                command,
                args,
                clgFile);
        if (!clgFile.exists()) {
            // Here, chktex could not perform the check
            // but the failure is already logged.
            return;
        }
        // assert !clgFile.isDirectory();
        if (clgFile.length() != 0) {
            // FIXME: maybe we shall distinguish errors/warnings/messages
            this.log.warn("WLP06: Running " + command +
                    " found issues logged in '" +
                    texFile.getName() + "'. ");
        }
    }

    /**
     * Returns an array of strings,
     * each entry with a single option given by <code>options</code>
     * except the last three which represent <code>-o clgFile texFile</code>.
     *
     * @param options
     *                the options string. The individual options
     *                are expected to be separated by a single blank.
     * @param texFile
     *                the latex main file to be checked.
     * @param clgFile
     *                the log-file with the result of the check of
     *                <code>texFile</code>.
     * @return
     *         An array of strings:
     *         If <code>options</code> is not empty,
     *         the first entries are the options in <code>options</code>.
     *         The last three entries are
     *         <code>-o</code>, <code>clgFile</code> and <code>texFile</code>.
     */
    protected static String[] buildChkTexArguments(String options,
            File texFile,
            File clgFile) {
        if (options.isEmpty()) {
            return new String[] {
                    "-o",
                    clgFile.getName(),
                    texFile.getName()
            };
        }
        String[] optionsArr = options.split(" ");
        String[] args = Arrays.copyOf(optionsArr, optionsArr.length + 3);
        args[optionsArr.length] = "-o";
        args[optionsArr.length + 1] = clgFile.getName();
        args[optionsArr.length + 2] = texFile.getName();

        return args;
    }

    /**
     * Returns whether the given pdf files coincide
     * running the diff tool specified
     * by {@link Settings#getCommand(ConverterCategory)}
     * with {@link ConverterCategory#DiffPdf}.
     *
     * Logging:
     * <ul>
     * <li>EEX01, EEX02, EEX03, WEX04, WEX05:
     * if running the ChkTeX command failed.
     * </ul>
     * 
     * 
     * @param pdfFileCmp
     *                   the pdf file for comparison
     * @param pdfFileAct
     *                   the pdf file actually created.
     * @return
     *         whether <code>pdfFileAct</code> coincides with
     *         <code>pdfFileCmp</code>.
     * @throws BuildFailureException
     *                               TEX01 if invocation of the check command
     *                               failed.
     */
    boolean runDiffPdf(File pdfFileCmp, File pdfFileAct) throws BuildFailureException {
        //
        // File clgFile = TexFileUtils.replaceSuffix(texFile, SUFFIX_CLG);
        String command = this.settings.getCommand(ConverterCategory.DiffPdf);
        this.log.debug("Running " + command +
                " diffing '" + pdfFileCmp.getName() + "' and '" + pdfFileAct.getName() + "''. ");
        // String[] args = buildChkTexArguments(this.settings.getChkTexOptions(),
        // texFile,
        // clgFile);
        String[] args = new String[] { pdfFileCmp.toString(), pdfFileAct.toString() };

        // may throw BuildFailureException TEX01,
        // may log warning EEX01, EEX02, EEX03, WEX04, WEX05
        return this.executor.execute(null, // texFile.getParentFile(),
                this.settings.getTexPath(),
                command,
                args).success;
    }

}
