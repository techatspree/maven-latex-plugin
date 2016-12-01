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
import java.util.TreeSet;
import java.util.Map;
import java.util.HashMap;

/**
 * The latex pre-processor is for preprocessing graphic files 
 * in formats which cannot be included directly into a latex-file 
 * and in finding the latex main files 
 * which is done in {@link #processGraphicsSelectMain(Collection)} 
 * and in clearing the created files from the latex source directory 
 * in {@link #clearCreated(File)}. 
 */
public class LatexPreProcessor extends AbstractLatexProcessor {

    /**
     * Maps the suffix to the according handler. 
     * If the handler is <code>null</code>, there is no handler. 
     */
    private final static Map<String, SuffixHandler> SUFFIX2HANDLER = 
	new HashMap<String, SuffixHandler>();

    static {
	for (SuffixHandler handler : SuffixHandler.values()) {
	    SUFFIX2HANDLER.put(handler.getSuffix(), handler);
	}
    } // static 

 
    // used in preprocessing only 
    private final static String SUFFIX_TEX = ".tex";

    // home-brewed ending to represent tex including postscript 
    private final static String SUFFIX_PTX = ".ptx";
    // the next two for preprocessing and in LatexDev only 
    final static String SUFFIX_PSTEX = ".pstex";
    final static String SUFFIX_PDFTEX = ".pdf_tex";

    // suffix for xfig
    private final static String SUFFIX_FIG = ".fig";
    // suffix for svg
    private final static String SUFFIX_SVG = ".svg";
    // suffix for gnuplot
    // FIXME: to be made configurable 
    private final static String SUFFIX_GP = ".gp";
    // suffix for metapost
    private final static String SUFFIX_MP  = ".mp";
    // from xxx.mp creates xxx1.mps, xxx.log and xxx.mpx 
    private final static String SUFFIX_MPS = ".mps";
    private final static String SUFFIX_MPX = ".mpx";

    // just for message 
    private final static String SUFFIX_JPG = ".jpg";
    private final static String SUFFIX_PNG = ".png";
    // just for silently skipping 
    private final static String SUFFIX_BIB = ".bib";
    // for latex main file creating html 
    private final static String SUFFIX_EPS = ".eps";

    private final Collection<File> latexMainFiles;

    LatexPreProcessor(Settings settings, 
		      CommandExecutor executor, 
		      LogWrapper log, 
		      TexFileUtils fileUtils) {
	super(settings, executor, log, fileUtils);
	this.latexMainFiles = new TreeSet<File>();
     }

    /**
     * Handler for each suffix of a souce file. 
     * Mostly, these represent graphic formats 
     * but also {@link #SUFFIX_TEX} is required 
     * to detect the latex main files 
     * and {@link #SUFFIX_TEX} and {@link #SUFFIX_BIB} 
     * are needed for proper cleaning of the tex souce directory. 
     */
    enum SuffixHandler {
	fig {
	    // converts a fig-file into pdf 
	    // invoking {@link #runFig2Dev(File, LatexDev)}
	    // TEX01, WEX01
	    void transformSrc(File file, LatexPreProcessor proc) 
		throws BuildFailureException {
		proc.runFig2Dev(file, LatexDev.pdf);
	    }
	    void clearTarget(File file, LatexPreProcessor proc) {
		proc.clearTargetFig(file);
	    }
	    String getSuffix() {
		return LatexPreProcessor.SUFFIX_FIG;
	    }
	},
	gp {
	    // converts a gnuplot-file into pdf 
	    // invoking {@link #runGnuplot2Dev(File, LatexDev)} 
	    // TEX01, WEX01
	    void transformSrc(File file, LatexPreProcessor proc) 
		throws BuildFailureException {
		proc.runGnuplot2Dev(file, LatexDev.pdf);
	    }
	    void clearTarget(File file, LatexPreProcessor proc) {
		proc.clearTargetGp(file);
	    }
	    String getSuffix() {
		return LatexPreProcessor.SUFFIX_GP;
	    }
	},
	mp {
	    // converts a metapost-file into mps-format 
	    // invoking {@link #runMetapost2mps(File)} 
	    // TEX01, WEX01
	    void transformSrc(File file, LatexPreProcessor proc) 
		throws BuildFailureException {
		proc.runMetapost2mps(file);
	    }
	    void clearTarget(File file, LatexPreProcessor proc) {
		proc.clearTargetMp(file);
	    }
	    String getSuffix() {
		return LatexPreProcessor.SUFFIX_MP;
	    }
	},
	svg {
	    void transformSrc(File file, LatexPreProcessor proc) {
		proc.log.info("Processing svg-file '" + file + 
		 	      "' done implicitly in latex run. ");
	    }
	    void clearTarget(File file, LatexPreProcessor proc) {
		proc.clearTargetSvg(file);
	    }
	    String getSuffix() {
		return LatexPreProcessor.SUFFIX_SVG;
	    }
	},
	jpg {
	    void transformSrc(File file, LatexPreProcessor proc) {
		proc.log.info("No processing for jpg-file '" + file + 
			      "' needed. ");
	    }
	    void clearTarget(File file, LatexPreProcessor proc) {
	    }
	    String getSuffix() {
		return LatexPreProcessor.SUFFIX_JPG;
	    }
	},
	png {
	    void transformSrc(File file, LatexPreProcessor proc) {
		proc.log.info("No processing for png-file '" + file + 
			      "' needed. ");
	    }
	    void clearTarget(File file, LatexPreProcessor proc) {
	    }
	    String getSuffix() {
		return LatexPreProcessor.SUFFIX_PNG;
	    }
	},
	tex {
	    void transformSrc(File file, LatexPreProcessor proc) {
		proc.addMainFile(file);
	    }
	    void clearTarget(File file, LatexPreProcessor proc) {
		proc.clearTargetTex(file);
	    }
	    String getSuffix() {
		return LatexPreProcessor.SUFFIX_TEX;
	    }
	},
	bib {
	    void transformSrc(File file, LatexPreProcessor proc) {
	    }
	    void clearTarget(File file, LatexPreProcessor proc) {
	    }
	    String getSuffix() {
		return LatexPreProcessor.SUFFIX_BIB;
	    }
	};

	/**
	 * Does the transformation of the file <code>file</code> 
	 * using <code>proc</code>. 
	 * <p>
	 * Logging: 
	 * WEX01 applications for preprocessing graphic files failed. 
	 *
	 * @param file
	 *    a file with ending given by {@link #getSuffix()}. 
	 * @param proc
	 *    a latex pre-processor.
	 * @throws BuildFailureException
	 *    TEX01 only for {@link #fig}, {@link #gp} and {@link #mp} 
	 *    because these invoke external programs. 
	 */
	abstract void transformSrc(File file, LatexPreProcessor proc)
	throws BuildFailureException;

	/**
	 * Deletes the files potentially 
	 * created from the source file <code>file</code> 
	 * using <code>proc</code>. 
	 *
	 * @param file
	 *    a file with ending given by {@link #getSuffix()}. 
	 * @param proc
	 *    a latex pre-processor.
	 */
	abstract void clearTarget(File file, LatexPreProcessor proc);

	/**
	 * Returns the suffix of the file type 
	 * of the file type, this is the handler for. 
	 */
	abstract String getSuffix();
    } // enum SuffixHandler 

    /**
     * Runs fig2dev on fig-files to generate pdf and pdf_t files. 
     * This is a quite restricted usage of fig2dev. 
     * <p>
     * Logging: 
     * <ul>
     * <li> WEX01 if running the fig2dev command failed. 
     * </ul>
     *
     * @param figFile
     *    the fig file to be processed. 
     * @throws BuildFailureException
     *    TEX01 if invocation of the fig2dev command 
     *    returned by {@link Settings#getFig2devCommand()} failed. 
     *    This is invoked twice: once for creating the pdf-file 
     *    and once for creating the pdf_t-file. 
     * @see #create()
     */
    // used in processGraphics(File) only 
    private void runFig2Dev(File figFile, LatexDev dev) 
	throws BuildFailureException {

	this.log.info("Processing fig-file '" + figFile + "'. ");
	String command = this.settings.getFig2devCommand();
	File workingDir = figFile.getParentFile();
	String[] args;

	//File pdfFile   = this.fileUtils.replaceSuffix(figFile, SUFFIX_PDF);


	// create pdf-file (graphics without text) 
	// embedded in some tex-file 

	//if (update(figFile, pdfFile)) {
 	    args = buildArgumentsFig2Pdf(dev, 
					 this.settings.getFig2devGenOptions(), 
					 this.settings.getFig2devPdfOptions(), 
					 figFile);
	    this.log.debug("Running " + command + 
			   " -Lpdftex  ... on '" + figFile.getName() + "'. ");
	    // may throw BuildFailureException TEX01, log warning WEX01 
	    this.executor.execute(workingDir, 
				  this.settings.getTexPath(), //**** 
				  command, 
				  args);
	    //}

	    // create tex-file (text without grapics) 
	    // enclosing the pdf-file above 

  	    //if (update(figFile, pdf_tFile)) {
 	    args = buildArgumentsFig2Ptx(dev, 
					 this.settings.getFig2devGenOptions(), 
					 this.settings.getFig2devPtxOptions(), 
					 figFile);
	    this.log.debug("Running " + command + 
			   " -Lpdftex_t... on '" + figFile.getName() + "'. ");
	    // may throw BuildFailureException TEX01, log warning WEX01 
	    this.executor.execute(workingDir, 
				  this.settings.getTexPath(), //**** 
				  command, 
				  args);
	    //}
	// no check: just warning that no output has been created. 
    }

    private String[] buildArgumentsFig2Pdf(LatexDev dev, 
					   String optionsGen, 
					   String optionsPdf, 
					   File figFile) {
	String[] optionsGenArr = optionsGen.isEmpty() 
	    ? new String[0] : optionsGen.split(" ");
	String[] optionsPdfArr = optionsPdf.isEmpty() 
	    ? new String[0] : optionsPdf.split(" ");
	int lenSum = optionsGenArr.length +optionsPdfArr.length;

	String[] args = new String[lenSum + 4];
	// language 
	args[0] = "-L";
	args[1] = dev.getXFigInTexLanguage();
	// general options 
	System.arraycopy(optionsGenArr, 0, args, 2, optionsGenArr.length);
	// language specific options 
	System.arraycopy(optionsPdfArr, 0, 
			 args, 2+optionsGenArr.length, optionsPdfArr.length);
	// input: fig-file 
        args[2+lenSum] = figFile.getName();
	// output: pdf-file 
	args[3+lenSum] = dev.getXFigInTexFile(this.fileUtils, figFile);
	return args;
    }

    private String[] buildArgumentsFig2Ptx(LatexDev dev, 
					   String optionsGen,
					   String optionsPtx,
					   File figFile) {
	String[] optionsGenArr = optionsGen.isEmpty() 
	    ? new String[0] : optionsGen.split(" ");
	String[] optionsPtxArr = optionsPtx.isEmpty() 
	    ? new String[0] : optionsPtx.split(" ");
	int lenSum = optionsGenArr.length +optionsPtxArr.length;

	String[] args = new String[lenSum + 6];
	// language 
	args[0] = "-L";
	args[1] = dev.getXFigTexLanguage();
	// general options 
	System.arraycopy(optionsGenArr, 0, 
			 args, 2, optionsGenArr.length);
	// language specific options 
	System.arraycopy(optionsPtxArr, 0, 
			 args, 2+optionsGenArr.length, optionsPtxArr.length);
	// -p pdf-file in ptx-file 
	args[2+lenSum] = "-p";
        args[3+lenSum] = dev.getXFigInTexFile(this.fileUtils, figFile);
	// input: fig-file 
        args[4+lenSum] = figFile.getName();
	// output: ptx-file 
	args[5+lenSum] = this.fileUtils.replaceSuffix(figFile, SUFFIX_PTX)
	    .getName();
	return args;
    }

    /**
     * Deletes the graphic files 
     * created from the fig-file <code>figFile</code>. 
     * <p>
     * Logging: 
     * WFU05: Failed to delete file
     *
     * @param figFile
     *    a fig file. 
     */
    private void clearTargetFig(File figFile) {
	this.log.info("Deleting targets of fig-file '" + figFile + "'. ");
	// may log warning WFU05 
	deleteIfExists(figFile, SUFFIX_PTX);
	deleteIfExists(figFile, SUFFIX_PDF);
    }

    /**
     * Converts a gnuplot file into a tex-file with ending ptx 
     * including a pdf-file. 
     * <p>
     * Logging: 
     * <ul>
     * <li> WEX01 if running the ptx/pdf-conversion built-in in gnuplot fails. 
     * </ul>
      *
     * @param gpFile 
     *    the gp-file (gnuplot format) to be converted to pdf. 
     * @throws BuildFailureException
     *    TEX01 if invocation of the ptx/pdf-conversion built-in 
     *    in gnuplot fails. 
     * @see #create()
     */
    // used in processGraphics(Collection) only 
    private void runGnuplot2Dev(File gpFile, LatexDev dev) 
	throws BuildFailureException {

	this.log.info("Processing gnuplot-file '" + gpFile + "'. ");
	String command = this.settings.getGnuplotCommand();
	File pdfFile = this.fileUtils.replaceSuffix(gpFile, SUFFIX_PDF);
	File ptxFile = this.fileUtils.replaceSuffix(gpFile, SUFFIX_PTX);

	String[] args = new String[] {
	    "-e",   // run a command string "..." with commands sparated by ';' 
	    // 
	    "set terminal cairolatex " + dev.getGnuplotInTexLanguage() + 
	    " " + this.settings.getGnuplotOptions() + 
	    ";set output \"" + ptxFile.getName() + 
	    "\";load \"" + gpFile.getName() + "\""
	};
	// FIXME: include options. 
// set terminal cairolatex
// {eps | pdf} done before. 
// {standalone | input}
// {blacktext | colortext | colourtext}
// {header <header> | noheader}
// {mono|color}
// {{no}transparent} {{no}crop} {background <rgbcolor>}
// {font <font>} {fontscale <scale>}
// {linewidth <lw>} {rounded|butt|square} {dashlength <dl>}
// {size <XX>{unit},<YY>{unit}}


//	if (update(gpFile, ptxFile)) {
	    this.log.debug("Running " + command + 
			   " -e...  on '" + gpFile.getName() + "'. ");
	    // may throw BuildFailureException TEX01, log warning WEX01 
	    this.executor.execute(gpFile.getParentFile(), //workingDir 
				  this.settings.getTexPath(), //**** 
				  command, 
				  args);
//	}
	// no check: just warning that no output has been created. 
    }

    /**
     * Deletes the graphic files 
     * created from the gnuplot-file <code>gpFile</code>. 
     * <p>
     * Logging: 
     * WFU05: Failed to delete file
     */
    private void clearTargetGp(File gpFile) {
	this.log.info("Deleting targets of gnuplot-file '" + gpFile + "'. ");
	// may log warning WFU05 
	deleteIfExists(gpFile, SUFFIX_PTX);
	deleteIfExists(gpFile, SUFFIX_PDF);
    }

    /**
     * Runs mpost on mp-files to generate mps-files. 
     * <p>
     * Logging: 
     * <ul>
     * <li> WFU03 cannot close log file 
     * <li> WAP01 Running <code>command</code> failed. For details...
     * <li> WAP02 Running <code>command</code> failed. No log file 
     * <li> WAP04 if log file is not readable. 
     * <li> WEX01 if running the mpost command failed. 
     * </ul>
     *
     * @param mpFile
     *    the metapost file to be processed. 
     * @throws BuildFailureException
     *    TEX01 if invocation of the mpost command failed. 
     * @see #processGraphics(File)
     */
    // used in processGraphics(Collection) only 
    private void runMetapost2mps(File mpFile) throws BuildFailureException {
	this.log.info("Processing metapost-file '" + mpFile + "'. ");
	String command = this.settings.getMetapostCommand();
	File workingDir = mpFile.getParentFile();
	// for more information just type mpost --help 
	String[] args = buildArguments(this.settings.getMetapostOptions(), 
				       mpFile);
	this.log.debug("Running " + command + 
		       " on '" + mpFile.getName() + "'. ");
	// may throw BuildFailureException TEX01, log warning WEX01 
	this.executor.execute(workingDir, 
			      this.settings.getTexPath(), //**** 
			      command, 
			      args);
	// from xxx.mp creates xxx1.mps, xxx.log and xxx.mpx 
	// FIXME: what is xxx.mpx for? 
	File logFile = this.fileUtils.replaceSuffix(mpFile, SUFFIX_LOG);
	// may log warnings WFU03, WAP01, WAP02, WAP04
	logErrs(logFile, command, this.settings.getPatternErrMPost());
	// FIXME: what about warnings?
    }

    /**
     * Deletes the graphic files 
     * created from the metapost-file <code>mpFile</code>. 
     * <p>
     * Logging: 
     * <ul>
     * <li> WFU01: Cannot read directory ...
     * <li> WFU05: Failed to delete file 
     * </ul>
     *
     * @param mpFile
     *    a metapost file. 
     */
    private void clearTargetMp(File mpFile) {
	this.log.info("Deleting targets of metapost-file '" + mpFile + "'. ");
	// may log warning WFU05 
	deleteIfExists(mpFile, SUFFIX_LOG);
	deleteIfExists(mpFile, SUFFIX_FLS);
	deleteIfExists(mpFile, SUFFIX_MPX);
	// delete files xxxNumber.mps 
	String name1 = mpFile.getName();
	final String root = name1.substring(0, name1.lastIndexOf("."));
	FileFilter filter = new FileFilter() {
		public boolean accept(File file) {
		    return !file.isDirectory()
			&&  file.getName().matches(root + "\\d+" + SUFFIX_MPS);
		}
	    };
	// may log warning WFU01, WFU05 
	this.fileUtils.deleteX(mpFile, filter);
    }

    /**
     * Deletes the graphic files 
     * created from the svg-file <code>svgFile</code>. 
     * <p>
     * Logging: 
     * WFU05: Failed to delete file
     */
    private void clearTargetSvg(File svgFile) {
       this.log.info("Deleting targets of svg-file '" + svgFile + "'. ");
       // may log warning WFU05 
       deleteIfExists(svgFile, SUFFIX_PDFTEX);
//     deleteIfExists(svgFile, SUFFIX_PSTEX );
       deleteIfExists(svgFile, SUFFIX_PDF   );
    }

    /**
     *
     * <p>
     * Logging: 
     * WFU05: Failed to delete file
     */
    private void deleteIfExists(File file, String suffix) {
	File delFile = this.fileUtils.replaceSuffix(file, suffix);
	if (!delFile.exists()) {
	    return;
	}
	// may log warning WFU05 
	this.fileUtils.deleteOrWarn(delFile);
    }

    /**
     * Returns whether <code>texFile</code> is a latex main file, 
     * provided it is readable. 
     * Otherwise logs a warning and returns <code>false</code>. 
     * <p>
     * Logging: 
     * WFU03 cannot close 
     *
     * @return
     *    whether <code>texFile</code> is definitively a latex main file. 
     *    If this is not readable, <code>false</code>. 
     */
    private boolean isLatexMainFile(File texFile) {
	assert texFile.exists();
	try {
	    // may throw BuildFailureException
	    // TFU07 (FileNotFound may not occur), TFU08 
	    // may log warning WFU03 cannot close 
	    return this.fileUtils.matchInFile
		(texFile, this.settings.getPatternLatexMainFile());
	} catch (BuildFailureException e) {
	    this.log.warn("WPP02: File '" + texFile + "' is not readable; " + 
			  "assume that it is no latex main file. ");
	    return false;
	}
    }

    /**
     * If the tex-file <code>texFile</code> is a latex main file, 
     * add it to {@link #latexMainFiles}. 
     *
     * @param texFile
     *    the tex-file to be added to {@link #latexMainFiles} 
     *    if it is a latex main file. 
     */
    private void addMainFile(File texFile) {
	if (isLatexMainFile(texFile)) {
	    this.log.info("Detected latex-main-file '" + texFile + "'. ");
	    this.latexMainFiles.add(texFile);
	}
    }

    /**
     * Deletes the files 
     * created from the tex-file <code>texFile</code>, 
     * if that is a latex main file. 
     * <p>
     * Logging: 
     * <ul>
     * <li> WFU01: Cannot read directory...
     * <li> WFU05: Failed to delete file 
     * </ul>
     *
     * @param texFile
     *    the tex-file of which the created files shall be deleted 
     *    if it is a latex main file. 
     */
    private void clearTargetTex(File texFile) {
	// exclude files which are no latex main files 
	if (!isLatexMainFile(texFile)) {
	    return;
	}
	this.log.info("Deleting latex main file '" + texFile + "'s targets. ");
	FileFilter filter = this.fileUtils.getFileFilter
	    (texFile, this.settings.getPatternClearFromLatexMain());
	// may log warning WFU01, WFU05 
	this.fileUtils.deleteX(texFile, filter);
    }

    /**
     * Converts files in various graphic formats incompatible with LaTeX 
     * into formats which can be inputted or included directly 
     * into a latex file and returns the latex main files. 
     * <p>
     * Logging: 
     * <ul>
     * <li> WPP03: Skipped processing of files with suffixes ... 
     * <li> WEX01 if running graphic processors failed. 
     * </ul>
     *
     * @throws BuildFailureException
     *    TEX01 invoking 
     * {@link LatexPreProcessor.SuffixHandler#transformSrc(File, LatexPreProcessor)}
     *    only for {@link LatexPreProcessor.SuffixHandler#fig}, 
     *    {@link LatexPreProcessor.SuffixHandler#gp} and 
     *    {@link LatexPreProcessor.SuffixHandler#mp} 
     *    because these invoke external programs. 
     */
    // used in LatexProcessor.create() only 
    // where files is the set of all files found in the tex source directory 
    // found with getFilesRec and thus without directories. 
    Collection<File> processGraphicsSelectMain(Collection<File> files) 
    	throws BuildFailureException {

	this.latexMainFiles.clear();
	String suffix;
	SuffixHandler handler;
	Collection<String> skipped = new TreeSet<String>();
	for (File file : files) {
	    suffix = this.fileUtils.getSuffix(file);
	    handler = SUFFIX2HANDLER.get(suffix);
	    if (handler == null) {
		this.log.debug("Skipping processing of file '" + file + "'. ");
		skipped.add(suffix);
	    } else {
		// may throw BuildFailureException TEX01, log warning WEX01 
		handler.transformSrc(file, this);
	    }
	}
	if (!skipped.isEmpty()) {
	    this.log.warn("WPP03: Skipped processing of files with suffixes " + 
			  skipped + ". ");
	}
	return this.latexMainFiles;
    }

    /**
     * Deletes all created files in <code>texDirectory</code>. 
     * <p>
     * Logging: 
     * WFU01 texDir not readable 
     *
     * @param texDir
     *    the tex-source directory. 
     */
    void clearCreated(File texDir) {
	// try to clear targets 
	// may log warning WFU01 texDir not readable 
	Collection<File> files = this.fileUtils.getFilesRec(texDir);
	SuffixHandler handler;
	for (File file : files) {
	    handler = SUFFIX2HANDLER.get(this.fileUtils.getSuffix(file));
	    if (handler != null) {
		handler.clearTarget(file, this);
	    }
	}
   }
 
    // FIXME: suffix for tex files containing text and including pdf 
 }
