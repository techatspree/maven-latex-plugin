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
import java.util.TreeMap;

/**
 * The latex pre-processor is for preprocessing graphic files 
 * in formats which cannot be included directly into a latex-file 
 * and in finding the latex main files 
 * which is done in {@link #processGraphicsSelectMain(DirNode)} 
 * and in clearing the created files from the latex source directory 
 * in {@link #clearCreated(DirNode)}. 
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

    LatexPreProcessor(Settings settings, 
		      CommandExecutor executor, 
		      LogWrapper log, 
		      TexFileUtils fileUtils) {
	super(settings, executor, log, fileUtils);
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
	    // TEX01, WEX01, WEX02, WEX03, WEX04, WEX05 
	    void transformSrc(File file, 
			      LatexPreProcessor proc, 
			      Collection<File> lmFiles) 
		throws BuildFailureException {
		proc.runFig2Dev(file, LatexDev.pdf);
	    }
	    void clearTarget(File file, LatexPreProcessor proc) {
		// may log warning WFU05 
		proc.clearTargetFig(file);
	    }
	    String getSuffix() {
		return LatexPreProcessor.SUFFIX_FIG;
	    }
	},
	gp {
	    // converts a gnuplot-file into pdf 
	    // invoking {@link #runGnuplot2Dev(File, LatexDev)} 
	    // TEX01, WEX01, WEX02, WEX03, WEX04, WEX05 
	    void transformSrc(File file, 
			      LatexPreProcessor proc, 
			      Collection<File> lmFiles) 
		throws BuildFailureException {
		proc.runGnuplot2Dev(file, LatexDev.pdf);
	    }
	    void clearTarget(File file, LatexPreProcessor proc) {
		// may log warning WFU05 
		proc.clearTargetGp(file);
	    }
	    String getSuffix() {
		return LatexPreProcessor.SUFFIX_GP;
	    }
	},
	mp {
	    // converts a metapost-file into mps-format 
	    // invoking {@link #runMetapost2mps(File)} 
	    // TEX01, WEX01, WEX02, WEX03, WEX04, WEX05 
	    void transformSrc(File file, 
			      LatexPreProcessor proc, 
			      Collection<File> lmFiles) 
		throws BuildFailureException {
		proc.runMetapost2mps(file);
	    }
	    void clearTarget(File file, LatexPreProcessor proc) {
		// may log warning WFU01, WFU05 
		proc.clearTargetMp(file);
	    }
	    String getSuffix() {
		return LatexPreProcessor.SUFFIX_MP;
	    }
	},
	svg {
	    void transformSrc(File file, 
			      LatexPreProcessor proc, 
			      Collection<File> lmFiles) {
		proc.log.info("Processing svg-file '" + file + 
		 	      "' deferred to latex run. ");
	    }
	    void clearTarget(File file, LatexPreProcessor proc) {
		// may log warning WFU05 
		proc.clearTargetSvg(file);
	    }
	    String getSuffix() {
		return LatexPreProcessor.SUFFIX_SVG;
	    }
	},
	jpg {
	    void transformSrc(File file, 
			      LatexPreProcessor proc, 
			      Collection<File> lmFiles) {
		proc.log.info("Jpg-file '" + file + "' needs no processing. ");
	    }
	    void clearTarget(File file, 
			     LatexPreProcessor proc, 
			     Map<File, SuffixHandler> file2handler) {
		// do not add to file2handler 
	    }
	    void clearTarget(File file, LatexPreProcessor proc) {
		throw new IllegalStateException
		    ("File '" + file + "' has no targets to be cleared. ");
	    }
	    String getSuffix() {
		return LatexPreProcessor.SUFFIX_JPG;
	    }
	},
	png {
	    void transformSrc(File file, 
			      LatexPreProcessor proc, 
			      Collection<File> lmFiles) {
		proc.log.info("Png-file '" + file + "' needs no processing. ");
	    }
	    void clearTarget(File file, 
			     LatexPreProcessor proc, 
			     Map<File, SuffixHandler> file2handler) {
		// do not add to file2handler 
	    }
	    void clearTarget(File file, LatexPreProcessor proc) {
		throw new IllegalStateException
		    ("File '" + file + "' has no targets to be cleared. ");
	    }
	    String getSuffix() {
		return LatexPreProcessor.SUFFIX_PNG;
	    }
	},
	tex {
	    void transformSrc(File file, 
			      LatexPreProcessor proc, 
			      Collection<File> lmFiles) {
		// may log warnings WFU03, WPP02 
		proc.addMainFile(file, lmFiles);
	    }
	    void clearTarget(File file, 
			     LatexPreProcessor proc, 
			     Map<File, SuffixHandler> file2handler) {
		// may log warnings WPP02, WFU01, WFU03, WFU05 
		proc.clearTargetTex(file);
	    }
	    void clearTarget(File file, LatexPreProcessor proc) {
		throw new IllegalStateException
		    ("Clearing targets of '" + file + 
		     "' should have been done already. ");
	    }

	    String getSuffix() {
		return LatexPreProcessor.SUFFIX_TEX;
	    }
	},
	bib {
	    void transformSrc(File file, 
			      LatexPreProcessor proc, 
			      Collection<File> lmFiles) {
		proc.log.info("Found bibliography file '" + file + "'. ");
	    }
	    void clearTarget(File file, 
			     LatexPreProcessor proc, 
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

	/**
	 * Does the transformation of the file <code>file</code> 
	 * using <code>proc</code>, except for latex main files: 
	 * These are just added to <code>latexMainFiles</code>. 
	 * <p>
	 * Logging: 
	 * <ul>
	 * <li> WFU03: cannot close 
	 * <li> WPP02: tex file may be latex main file 
	 * <li> WEX01, WEX02, WEX03, WEX04, WEX05: 
	 * if applications for preprocessing graphic files failed. 
	 * </ul>
	 *
	 * @param file
	 *    a file with ending given by {@link #getSuffix()}. 
	 * @param proc
	 *    a latex pre-processor. 
	 * @param latexMainFiles
	 *    the collection of latex main files found so far. 
	 * @throws BuildFailureException
	 *    TEX01 only for {@link #fig}, {@link #gp} and {@link #mp} 
	 *    because these invoke external programs. 
	 */
	abstract void transformSrc(File file, 
				   LatexPreProcessor proc, 
				   Collection<File> latexMainFiles)
	throws BuildFailureException;

	/**
	 * Typically just associates <code>file</code> 
	 * with this hanlder in <code>file2handler</code> except for 
	 * <ul>
	 * <li>
	 * jpg-files, png-files and bib-files 
	 * for which there are no targets 
	 * and so the association is not added.  
	 * <li>
	 * tex-files for which the target is cleared immediately. 
	 * </ul>
	 * <p>
	 * Logging: 
	 * <ul>
	 * <li> WPP02: tex file may be latex main file 
	 * <li> WFU01: Cannot read directory...
	 * <li> WFU03: cannot close tex file 
	 * <li> WFU05: Failed to delete file 
	 * <ul>
	 */
	// overwritten for tex, jpg, png and for bib 
	// appropriate for svg although file may be removed from map later 
	// used in clearCreated only 
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
	 * <li> WFU01: Cannot read directory...
	 * <li> WFU05: Failed to delete file 
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
	 */
	// used in clearCreated only 
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
     * <li> WEX01, WEX02, WEX03, WEX04, WEX05: 
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
     * @see #create()
     */
    // used in processGraphicsSelectMain(DirNode) only 
    private void runFig2Dev(File figFile, LatexDev dev) 
	throws BuildFailureException {

	this.log.info("Processing fig-file '" + figFile + "'. ");
	String command = this.settings.getFig2devCommand();
	File workingDir = figFile.getParentFile();
	String[] args;

	// create pdf-file (graphics without text) 
	// embedded in some tex-file 

	//if (update(figFile, pdfFile)) {
 	    args = buildArgumentsFig2Pdf(dev, 
					 this.settings.getFig2devGenOptions(), 
					 this.settings.getFig2devPdfOptions(), 
					 figFile);
	    this.log.debug("Running " + command + 
			   " -Lpdftex  ... on '" + figFile.getName() + "'. ");
	    // may throw BuildFailureException TEX01, 
	    // may log warning WEX01, WEX02, WEX03, WEX04, WEX05 
  	    this.executor.execute(workingDir, 
				  this.settings.getTexPath(), //**** 
				  command, 
				  args,
				  this.fileUtils.replaceSuffix(figFile, 
							       SUFFIX_PDF));
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
	    // may throw BuildFailureException TEX01, 
	    // may log warning WEX01, WEX02, WEX03, WEX04, WEX05 
 	    this.executor.execute(workingDir, 
				  this.settings.getTexPath(), //**** 
				  command, 
				  args,
				  this.fileUtils.replaceSuffix(figFile, 
							       SUFFIX_PTX));
	    //}
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
     * <li> WEX01, WEX02, WEX03, WEX04, WEX05: 
     * if running the ptx/pdf-conversion built-in in gnuplot fails. 
     * </ul>
     *
     * @param gpFile 
     *    the gp-file (gnuplot format) to be converted to pdf. 
     * @throws BuildFailureException
     *    TEX01 if invocation of the ptx/pdf-conversion built-in 
     *    in gnuplot fails. 
     * @see #create()
     */
    // used in processGraphicsSelectMain(DirNode) only 
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
	    // may throw BuildFailureException TEX01, 
	    // may log warning WEX01, WEX02, WEX03, WEX04, WEX05 
	    this.executor.execute(gpFile.getParentFile(), //workingDir 
				  this.settings.getTexPath(), //**** 
				  command, 
				  args, 
				  pdfFile, ptxFile);
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
     * <li> WFU03: cannot close log file 
     * <li> WAP01: Running <code>command</code> failed. For details...
     * <li> WAP02: Running <code>command</code> failed. No log file 
     * <li> WAP04: if log file is not readable. 
     * <li> WEX01, WEX02, WEX03, WEX04, WEX05: 
     * if running the mpost command failed. 
     * </ul>
     *
     * @param mpFile
     *    the metapost file to be processed. 
     * @throws BuildFailureException
     *    TEX01 if invocation of the mpost command failed. 
     * @see #processGraphics(File)
     */
    // used in processGraphicsSelectMain(DirNode) only 
    private void runMetapost2mps(File mpFile) throws BuildFailureException {
	this.log.info("Processing metapost-file '" + mpFile + "'. ");
	String command = this.settings.getMetapostCommand();
	File workingDir = mpFile.getParentFile();
	// for more information just type mpost --help 
	String[] args = buildArguments(this.settings.getMetapostOptions(), 
				       mpFile);
	this.log.debug("Running " + command + 
		       " on '" + mpFile.getName() + "'. ");
	// FIXME: not check on all created files, 
	// but this is not worse than with latex 

	// may throw BuildFailureException TEX01, 
	// may log warning WEX01, WEX02, WEX03, WEX04, WEX05 
  	this.executor.execute(workingDir, 
			      this.settings.getTexPath(), //**** 
			      command, 
			      args,
			      this.fileUtils.replaceSuffix(mpFile, 
							   "1"+SUFFIX_MPS));

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
     * <ul>
     * <li> WFU03: cannot close 
     * <li> WPP02: tex file may be latex main file 
     * <ul>
     *
     * @return
     *    whether <code>texFile</code> is definitively a latex main file. 
     *    If this is not readable, <code>false</code>. 
     */
    // used by addMainFile and by clearTargetTex 
    private boolean isLatexMainFile(File texFile) {
	assert texFile.exists();
	// may log warning WFU03 cannot close 
	Boolean res = this.fileUtils.matchInFile
	    (texFile, this.settings.getPatternLatexMainFile());
	if (res == null) {
	    this.log.warn("WPP02: Cannot read tex file '" + texFile + 
			  "'; may bear latex main file. ");
	    return false;
	}
	return res;
    }

    /**
     * If the tex-file <code>texFile</code> is a latex main file, 
     * add it to {@link #latexMainFiles}. 
     * <p>
     * Logging: 
     * <ul>
     * <li> WFU03: cannot close 
     * <li> WPP02: tex file may be latex main file 
     * <ul>
     *
     * @param texFile
     *    the tex-file to be added to {@link #latexMainFiles} 
     *    if it is a latex main file. 
     * @param latexMainFiles
     *    the collection of latex main files found so far. 
     */
    private void addMainFile(File texFile, Collection<File> latexMainFiles) {
	// may log warnings WFU03, WPP02 
	if (isLatexMainFile(texFile)) {
	    this.log.info("Detected latex-main-file '" + texFile + "'. ");
	    latexMainFiles.add(texFile);
	}
    }

    /**
     * Deletes the files 
     * created from the tex-file <code>texFile</code>, 
     * if that is a latex main file. 
     * <p>
     * Logging: 
     * <ul>
     * <li> WPP02: tex file may be latex main file 
     * <li> WFU01: Cannot read directory...
     * <li> WFU03: cannot close tex file 
     * <li> WFU05: Failed to delete file 
     * </ul>
     *
     * @param texFile
     *    the tex-file of which the created files shall be deleted 
     *    if it is a latex main file. 
     */
    private void clearTargetTex(File texFile) {
	// exclude files which are no latex main files 
	// may log warnings WFU03, WPP02 
	if (!isLatexMainFile(texFile)) {
	    return;
	}
	this.log.info("Deleting targets of latex main file '" + 
		      texFile + "'. ");
	FileFilter filter = this.fileUtils.getFileFilter
	    (texFile, this.settings.getPatternClearFromLatexMain());
	// may log warning WFU01, WFU05 
	this.fileUtils.deleteX(texFile, filter);
    }

    /**
     * Detects files in the directory represented by <code>texNode</code> 
     * and in subdirectories recursively: 
     * <ul>
     * <li>
     * those which are in various graphic formats incompatible with LaTeX 
     * are converted into formats which can be inputted or included directly 
     * into a latex file. 
     * <li>
     * returns the set of latex main files. 
     * </ul>
     * <p>
     * Logging: 
     * <ul>
     * <li> WFU03: cannot close 
     * <li> WPP02: tex file may be latex main file 
     * <li> WPP03: Skipped processing of files with suffixes ... 
     * <li> WEX01, WEX02, WEX03, WEX04, WEX05: 
     *      if running graphic processors failed. 
     * </ul>
     *
     * @param texNode
     *    a node representing the tex source directory. 
     * @return
     *    the collection of latex main files. 
     * @throws BuildFailureException
     *    TEX01 invoking 
     * {@link LatexPreProcessor.SuffixHandler#transformSrc(File, LatexPreProcessor)}
     *    only for {@link LatexPreProcessor.SuffixHandler#fig}, 
     *    {@link LatexPreProcessor.SuffixHandler#gp} and 
     *    {@link LatexPreProcessor.SuffixHandler#mp} 
     *    because these invoke external programs. 
     */
    // used in LatexProcessor.create() 
    // and in LatexProcessor.processGraphics() only 
    // where 'node' represents the tex source directory 
    Collection<File> processGraphicsSelectMain(File dir, DirNode texNode) 
    	throws BuildFailureException {
    	Collection<String> skipped = new TreeSet<String>();
    	Collection<File> latexMainFiles = new TreeSet<File>();
	// may throw BuildFailureException TEX01, 
	// log warning WEX01, WEX02, WEX03, WEX04, WEX05, WFU03, WPP02 
      	processGraphicsSelectMain(dir, texNode, skipped, latexMainFiles);

    	if (!skipped.isEmpty()) {
    	    this.log.warn("WPP03: Skipped processing of files with suffixes " + 
    			  skipped + ". ");
    	}

    	return latexMainFiles;
    }

    /**
     * <p>
     * Logging: 
     * <ul>
     * <li> WFU03: cannot close 
     * <li> WPP02: tex file may be latex main file 
     * <li> WEX01, WEX02, WEX03, WEX04, WEX05: 
     * if applications for preprocessing graphic files failed. 
     * </ul>
     *
     * @param node
     *    a node representing the tex source directory 
     *    or a subdirectory recursively. 
     * @throws BuildFailureException
     *    TEX01 invoking 
     * {@link LatexPreProcessor.SuffixHandler#transformSrc(File, LatexPreProcessor)}
     *    only for {@link LatexPreProcessor.SuffixHandler#fig}, 
     *    {@link LatexPreProcessor.SuffixHandler#gp} and 
     *    {@link LatexPreProcessor.SuffixHandler#mp} 
     *    because these invoke external programs. 
     */
    private void processGraphicsSelectMain(File dir, 
					   DirNode node, 
    					   Collection<String> skipped, 
    					   Collection<File> latexMainFiles) 
    	throws BuildFailureException {

   	assert node.isValid();
    	// i.e. node.regularFile != null

	File file;
    	String suffix;
    	SuffixHandler handler;
	Collection<File> latexMainFilesLocal = new TreeSet<File>();
   	for (String fileName : node.getRegularFileNames()) {
	    file = new File(dir, fileName);
    	    suffix = this.fileUtils.getSuffix(file);
    	    handler = SUFFIX2HANDLER.get(suffix);
    	    if (handler == null) {
    		this.log.debug("Skipping processing of file '" + file + "'. ");
    		skipped.add(suffix);
    	    } else {
    		// may throw BuildFailureException TEX01, 
    		// log warning WEX01, WEX02, WEX03, WEX04, WEX05 
		// WFU03, WPP02 
    		handler.transformSrc(file, this, latexMainFilesLocal);
    	    }
    	}
	latexMainFiles.addAll(latexMainFilesLocal);

    	for (Map.Entry<String,DirNode> entry : node.getSubdirs().entrySet()) {
	    // may throw BuildFailureException TEX01, 
	    // log warning WEX01, WEX02, WEX03, WEX04, WEX05, WPP03 
	    // WFU03, WPP02 
     	    processGraphicsSelectMain(new File(dir, entry.getKey()),
				      entry.getValue(), 
				      skipped, 
				      latexMainFiles);
    	}
    }

    /**
     * Deletes all created files 
     * in the directory represented by <code>node</code>, recursively. 
     * <p>
     * Logging: 
     * <ul>
     * <li> WPP02: tex file may be latex main file 
     * <li> WFU01: Cannot read directory...
     * <li> WFU03: cannot close tex file 
     * <li> WFU05: Failed to delete file 
     * </ul>
     *
     * @param texNode
     *    a node representing the tex source directory 
     *    or a subdirectory. 
     */
    // invoked recursively; except that used only in 
    // LatexProcessor.clearAll()
    void clearCreated(File texDir) {
	clearCreated(texDir, new DirNode(texDir, this.fileUtils));
    }

    /**
     * Deletes all created files 
     * in the directory represented by <code>node</code>, recursively. 
     * <p>
     * Logging: 
     * <ul>
     * <li> WPP02: tex file may be latex main file 
     * <li> WFU01: Cannot read directory...
     * <li> WFU03: cannot close tex file 
     * <li> WFU05: Failed to delete file 
     * </ul>
     *
     * @param node
     *    a node associated with <code>dir</code>. 
     */
    private void clearCreated(File dir, DirNode node) {
	assert dir.isDirectory();
	File file;
	SuffixHandler handler;
	Map<File, SuffixHandler> file2handler = 
	    new TreeMap<File, SuffixHandler>();
   	for (String fileName : node.getRegularFileNames()) {
	    file = new File(dir, fileName);
	    handler = SUFFIX2HANDLER.get(this.fileUtils.getSuffix(file));
	    if (handler != null) {
		// may log warning WPP02, WFU01, WFU03, WFU05 
		handler.clearTarget(file, this, file2handler);
	    }
	}
  	for (Map.Entry<File,SuffixHandler> entry : file2handler.entrySet()) {
	    file = entry.getKey();
	    if (file.exists()) {
		entry.getValue().clearTarget(file, this);
	    }
	}

    	for (Map.Entry<String,DirNode> entry : node.getSubdirs().entrySet()) {
	    // may log warning WPP02, WFU01, WFU03, WFU05 
    	    clearCreated(new File(dir, entry.getKey()), entry.getValue());
    	}
    }

    // FIXME: suffix for tex files containing text and including pdf 
 }
