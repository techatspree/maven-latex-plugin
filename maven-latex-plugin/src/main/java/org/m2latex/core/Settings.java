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


/**
 * The settings for this maven plugin and for this ant task. 
 * These are the elements of the pom in element <code>settings</code>
 */
public class Settings
{

    // static initializer 

    /**
     * On unix <code>src/site/tex</code>, 
     * on other operating systems accordingly. 
     */
    final static String SST;

    static {
	String fs = System.getProperty("file.separator");
	SST = "src" + fs + "site" + fs + "tex";
    }

    // readonly parameters 

    /**
     * The base directory of this maven project. 
     *
     * @see AbstractLatexMojo#baseDirectory
     */
    private File baseDirectory;

    /**
     * The target directory of this maven project. 
     * By default this is <code>{@link #baseDirectory}/target</code> 
     * on Unix systems. 
     *
     * @see AbstractLatexMojo#targetDirectory
     */
    private File targetDirectory;

    /**
     * The target site directory of this maven project. 
     * By default this is <code>{@link #targetDirectory}/site</code> 
     * on Unix systems. 
     *
     * @see AbstractLatexMojo#targetSiteDirectory
     */
    private File targetSiteDirectory;

    // read/write parameters and related. 
    // If a parameter represents a relative path, this is a string 
    // and there is an according field of type File. 

    /**
     * The tex source directory as a string, containing 
     * all tex main documents (including subfolders) to be processed
     * relative to {@link #baseDirectory}. 
     * The default value is {@link #SST}. 
     * The according file is given by {@link #texSrcDirectoryFile}. 
     *
     * @parameter
     */
    private String texSrcDirectory = SST;

    /**
     * File for {@link #texSrcDirectory} based on {@link #baseDirectory}. 
     */
    private File texSrcDirectoryFile = new File(this.baseDirectory, 
						this.texSrcDirectory);

    /**
     * The working directory as a string, 
     * for temporary files and LaTeX processing 
     * relative to {@link #targetDirectory}. 
     * <p>
     * First, the tex-souces are copied recursively 
     * from {@link #texSrcDirectory} to this directory, 
     * then they are processed, the results stored in this directory 
     * and finally, the resulting files are copied to {@link #outputDirectory}.
     * <p>
     * The default value is <code>m2latex</code>. 
     * The according file is given by {@link #tempDirectoryFile}. 
     *
     * @parameter
     */
    private String tempDirectory = "m2latex";

    /**
     * File for {@link #tempDirectory} based on {@link #targetDirectory}. 
     */
    private File tempDirectoryFile = new File(this.targetDirectory,
					      this.tempDirectory);

    /**
     * The artifacts generated by {@link #texCommand} 
     * will be copied to this folder 
     * which is given relative to {@link #targetSiteDirectory}. 
     * The default value is <code>.</code>. 
     * The according file is given by {@link #outputDirectoryFile}. 
     *
     * @parameter
     */
    private String outputDirectory = ".";

    /**
     * File for {@link #outputDirectory} based on {@link #targetSiteDirectory}. 
     */
    private File outputDirectoryFile = new File(this.targetSiteDirectory, 
						this.outputDirectory);

    // texPath, commands and arguments 

    /**
     * Path to the TeX scripts or <code>null</code>. 
     * In the latter case, the scripts must be on the system path. 
     * Note that in the pom, <code>&lt;texPath/&gt;</code> 
     * and even <code>&lt;texPath&gt;    &lt;/texPath&gt;</code> 
     * represent the <code>null</code>-File. 
     * The default value is <code>null</code>. 
     *
     * @parameter
     */
    private File texPath = null;

    /**
     * The fig2dev command for conversion of fig-files 
     * into various formats. 
     * Currently only pdf combined with pdf_t is supported. 
     * The default value is <code>fig2dev</code>. 
     *
     * @parameter
     */
    private String fig2devCommand = "fig2dev";


    /**
     * The LaTeX command. The default value is <code>pdflatex</code>. 
     * FIXME: The goal shall not be latex but pdf. 
     * Thus maybe it is a bad idea to make the executable configurable. 
     *
     * @parameter
     */
    private String texCommand = "pdflatex";

    /**
     * The arguments string to use when calling latex via {@link #texCommand}. 
     * Leading and trailing blanks are ignored. 
     * The setter method {@link #setTexCommandArgs(String)} ensures, 
     * that exactly one blank separate the proper options. 
     * The default value is 
     * <code>-interaction=nonstopmode -src-specials</code>. 
     *
     * @parameter
     */
    private String texCommandArgs = "-interaction=nonstopmode -src-specials";

    /**
     * The pattern in the <code>log</code> file 
     * indicating a failure when running {@link #texCommand}. 
     * The default value is 
     * <code>Fatal error|LaTeX Error|Emergency stop</code>. 
     * If this is not sufficient, please extend 
     * and notify the developer of this plugin. 
     *
     * @parameter
     */
    private String patternErrLatex = "Fatal error|LaTeX Error|Emergency stop";

    /**
     * Whether debugging of overfull/underfull hboxes/vboxes is on: 
     * If so, a bad box occurs in the last LaTeX run, a warning is displayed. 
     * For details, set $cleanUp to false, 
     * rerun LaTeX and have a look at the log-file. 
     * The default value is <code>true</code>. 
     *
     * @parameter
     */
    private boolean debugBadBoxes = true;

    /**
     * Whether debugging of warnings is on: 
     * If so, a warning in the last LaTeX run is displayed. 
     * For details, set $cleanUp to false, 
     * rerun LaTeX and have a look at the log-file. 
     * The default value is <code>true</code>. 
     *
     * @parameter
     */
    private boolean debugWarnings = true;

    /**
     * The BibTeX command. The default value is <code>bibtex</code>. 
     *
     * @parameter
     */
    private String bibtexCommand = "bibtex";

    // FIXME: Any parameters for bibtex? 


    /**
     * The MakeIndex command. The default value is <code>makeindex</code>. 
     *
     * @parameter
     */
    private String makeIndexCommand = "makeindex";

    /**
     * The Pattern in the ilg-file 
     * indicating that {@link #makeIndexCommand} failed. 
     * The default value is chosen 
     * according to the <code>makeindex</code> documentation 
     * but seems to be incomplete. 
     * If this is not complete, please extend 
     * and notify the developer of this plugin. 
     *
     * @parameter
     */
    private String patternErrMakeindex = 
	// FIXME: List is incomplete 
	"Extra |" + 
	"Illegal null field|" + 
	"Argument |" + 
	"Illegal null field|" + 
	"Unmatched |" + 
	"Inconsistent page encapsulator |" + 
	"Conflicting entries";


    /**
     * The tex4ht command. The default value is <code>htlatex</code>. 
     *
     * @parameter
     */
    private String tex4htCommand = "htlatex";

    /**
     * The options for the <code>tex4ht</code>-style 
     * which creates a dvi-file or a pdf-file 
     * with information to create sgml, 
     * e.g. html or odt or something like that. 
     *
     * @parameter
     */
    private String tex4htStyOptions = "html,2";

    /**
     * The options for <code>tex4ht</code> which extracts information 
     * from a dvi-file or from a pdf-file 
     * into the according lg-file and idv-file producing html-files 
     * and by need and if configured accordingly 
     * svg-files, 4ct-files and 4tc-files and a css-file and a tmp-file.
     * The former two are used by <code>t4ht</code> 
     * which is configured via {@link #t4htOptions}. 
     *
     * @parameter
     */
    private String tex4htOptions = "";

    /**
     * The options for <code>t4ht</code> which converts idv-file and lg-file 
     * into css-files, tmp-file and, 
     * by need and if configured accordingly into png files. 
     *
     * @parameter
     */
    private String t4htOptions = "";

    /**
     * The latex2rtf command. Default is <code>latex2rtf</code>. 
     *
     * @parameter
     */
    private String latex2rtfCommand = "latex2rtf";

    /**
     * The odt2doc command. Default is <code>odt2doc</code>. 
     *
     * @parameter
     */
    private String odt2docCommand = "odt2doc";

    // FIXME: provide parameters for latex2rtf 


    /**
     * The pdf2txt command converting pdf into plain text. 
     * Default is <code>pdftotext</code>. 
     *
     * @parameter
     */
    private String pdf2txtCommand = "pdftotext";


    // rerunning latex 

    /**
     * The pattern in the log file which triggers rerunning latex. 
     * This pattern may never be ensured to be complete, 
     * because any new package may break completeness. 
     * Nevertheless, the default value aims completeness 
     * while be restrictive enough not to trigger another latex run 
     * if not needed. 
     * To ensure termination, let {@link #maxNumReruns} 
     * specify the maximum number of latex runs. 
     * If the user finds an extension, (s)he is asked to contribute 
     * and to notify the developer of this plugin. 
     * Then the default value will be extended. 
     * FIXME: default? to be replaced by an array of strings? **** 
     *
     * @parameter
     */
   private String patternNeedAnotherLatexRun = 
       "(Rerun (LaTeX|to get cross-references right)|" + 
       "There were undefined references|" + 
       "\\(rerunfilecheck\\)                Rerun to get outlines right|" +
       "Package longtable Warning: Table widths have changed. Rerun LaTeX.|" +
       "Package natbib Warning: Citation\\(s\\) may have changed)";

    /**
     * The maximal allowed number of reruns of the latex process. 
     * This is to avoid endless repetitions. 
     * The default value is 5. 
     * This shall be non-negative 
     * or <code>-1</code> which signifies that there is no threshold. 
     *
     * @parameter
     */
    private int maxNumReruns = 5;


    // cleanup 

    /**
     * Clean up the working directory in the end? 
     * May be used for debugging when setting to <code>false</code>. 
     * The default value is <code>true</code>. 
     *
     * @parameter
     */
    private boolean cleanUp = true;

    // errors and warnings 



    // getter methods partially implementing default values. 


    private File getBaseDirectory() {
        return this.baseDirectory;
    }

    private File getTargetDirectory() {
        return this.targetDirectory;
    }

    private File getTargetSiteDirectory() {
        return this.targetSiteDirectory;
    }


    public File getTexSrcDirectoryFile() {
	return this.texSrcDirectoryFile;
    }

     public File getTempDirectoryFile() {
	return this.tempDirectoryFile;
    }

     public File getOutputDirectoryFile() {
       return this.outputDirectoryFile;
    }




    // texPath, commands and arguments 

    public File getTexPath() {
        return this.texPath;
    }

    public String getFig2devCommand() {
        return this.fig2devCommand;
    }

    public String getTexCommand() {
        return this.texCommand;
    }

    public String getTexCommandArgs() {
        return this.texCommandArgs;
    }

    public String getPatternErrLatex() {
	return this.patternErrLatex;
    }

    public boolean getDebugBadBoxes() {
	return this.debugBadBoxes;
    }

    public boolean getDebugWarnings() {
	return this.debugWarnings;
    }



    public String getBibtexCommand() {
        return this.bibtexCommand;
    }

    public String getMakeIndexCommand() {
	return this.makeIndexCommand;
    }

    public String getPatternErrMakeindex() {
	return this.patternErrMakeindex;
    }

    public String getTex4htCommand() {
        return this.tex4htCommand;
    }

    public String getTex4htOptions() {
        return this.tex4htOptions;
    }

    public String getTex4htStyOptions() {
        return this.tex4htStyOptions;
    }

    public String getT4htOptions() {
        return this.t4htOptions;
    }

    public String getLatex2rtfCommand() {
        return this.latex2rtfCommand;
    }

    public String getOdt2docCommand() {
        return this.odt2docCommand;
    }

    public String getPdf2txtCommand() {
        return this.pdf2txtCommand;
    }

    public boolean isCleanUp() {
        return this.cleanUp;
    }


    public String getPatternNeedAnotherLatexRun() {
	return this.patternNeedAnotherLatexRun;
    }

    public int getMaxNumReruns() {
	return this.maxNumReruns;
    }

    // setter methods 

   /**
     * Setter method for {@link #baseDirectory} 
     * influencing also {@link #texSrcDirectoryFile}. 
     */
    public void setBaseDirectory(File baseDirectory) {
        this.baseDirectory = baseDirectory;
	this.texSrcDirectoryFile = new File(this.baseDirectory, 
					    this.texSrcDirectory);
    }

    /**
     * Setter method for {@link #targetDirectory} 
     * influencing also {@link #tempDirectoryFile}. 
     */
    public void setTargetDirectory(File targetDirectory) {
        this.targetDirectory = targetDirectory;
	this.tempDirectoryFile = new File(this.targetDirectory,
					  this.tempDirectory);
    }

    /**
     * Setter method for {@link #targetSiteDirectory} 
     * influencing also {@link #outputDirectoryFile}. 
     */
    public void setTargetSiteDirectory(File targetSiteDirectory) {
        this.targetSiteDirectory = targetSiteDirectory;
	this.outputDirectoryFile = new File(this.targetSiteDirectory, 
					    this.outputDirectory);
    }

    /**
     * Sets {@link #texSrcDirectory} and updates {@link #texSrcDirectoryFile}. 
     */
    public void setTexSrcDirectory(String texSrcDirectory) {
        this.texSrcDirectory = texSrcDirectory;
	this.texSrcDirectoryFile = new File(this.baseDirectory, 
					    this.texSrcDirectory);
    }

    /**
     * Sets {@link #tempDirectory} and updates {@link #tempDirectoryFile}. 
     */
    public void setTempDirectory(String tempDirectory) {
        this.tempDirectory = tempDirectory;
	this.tempDirectoryFile = new File(this.targetDirectory,
					  this.tempDirectory);
    }

    /**
     * Sets {@link #outputDirectory} and updates {@link #outputDirectoryFile}. 
     */
    public void setOutputDirectory(String outputDirectory) {
        this.outputDirectory = outputDirectory;
	this.outputDirectoryFile = new File(this.targetSiteDirectory, 
					    this.outputDirectory);
    }

    public void setTexPath(File texPath) {
        this.texPath = texPath;
    }

    public void setFig2devCommand(String fig2devCommand) {
        this.fig2devCommand = fig2devCommand;
    }

    public void setTexCommand(String texCommand) {
        this.texCommand = texCommand;
    }

    /**
     * Sets the argument string of the latex command 
     * given by {@link #texCommand}. 
     * It is ensured that {@link #texCommandArgs} 
     * consist of proper options separated by a single blank. 
     *
     * @param args
     *    The arguments string to use when calling latex. 
     *    Leading and trailing blank and newline are ignored. 
     *    Proper arguments are separated by blank and newline. 
     */
    public void setTexCommandArgs(String args) {
        this.texCommandArgs = args.replace("( \n)+", " ").trim();
    }



    public void setBibtexCommand(String bibtexCommand) {
        this.bibtexCommand = bibtexCommand;
    }

    public void setMakeIndexCommand(String makeIndexCommand) {
        this.makeIndexCommand = makeIndexCommand;
    }

    // setter method for patternErrMakeindex in maven 
    public void setPatternErrMakeindex(String patternErrMakeindex) {
        this.patternErrMakeindex = patternErrMakeindex;
    }

    // method introduces patternErrMakeindex in ant 
    public PatternErrMakeindex createPatternErrMakeindex() {
   	return new PatternErrMakeindex();
    }

    // defines patternErrMakeindex element with text in ant 
    public class PatternErrMakeindex {
	// FIXME: this is without property resolution. 
	// to add this need  pattern = getProject().replaceProperties(pattern)
	// with Task.getProject() 
   	public void addText(String pattern) {
   	    Settings.this.setPatternErrMakeindex(pattern);
   	}
    }


    public void setCleanUp(boolean cleanUp) {
        this.cleanUp = cleanUp;
    }

    public void setPatternErrLatex(String patternErrLatex) {
	this.patternErrLatex = patternErrLatex;
    }

    public void setDebugBadBoxes(boolean debugBadBoxes) {
	this.debugBadBoxes = debugBadBoxes;
    }

    public void setDebugWarnings(boolean debugWarnings) {
	this.debugWarnings = debugWarnings;
    }


    public void setLatex2rtfCommand(String latex2rtfCommand) {
        this.latex2rtfCommand = latex2rtfCommand;
    }

    public void setOdt2docCommand(String odt2docCommand) {
        this.odt2docCommand = odt2docCommand;
     }

    public void setPdf2txtCommand(String pdf2txtCommand) {
        this.pdf2txtCommand = pdf2txtCommand;
    }

    public void setTex4htCommand(String tex4htCommand) {
        this.tex4htCommand = tex4htCommand;
    }

    public void setTex4htStyOptions(String tex4htStyOptions) {
	this.tex4htStyOptions = tex4htStyOptions;
   }

     public void setTex4htOptions(String tex4htOptions) {
	this.tex4htOptions = tex4htOptions;
    }

     public void setT4htOptions(String t4htOptions) {
	this.t4htOptions = t4htOptions;
    }

    // setter method for patternNeedAnotherLatexRun in maven 
    public void setPatternNeedAnotherLatexRun(String pattern) {
	this.patternNeedAnotherLatexRun = pattern;
    }

    // method introduces patternNeedAnotherLatexRun in ant 
    public PatternNeedAnotherLatexRun createPatternNeedAnotherLatexRun() {
   	return new PatternNeedAnotherLatexRun();
    }

    // defines patternNeedAnotherLatexRun element with text in ant 
    public class PatternNeedAnotherLatexRun {
	// FIXME: this is without property resolution. 
	// to add this need  pattern = getProject().replaceProperties(pattern)
	// with Task.getProject() 
   	public void addText(String pattern) {
   	    Settings.this.setPatternNeedAnotherLatexRun(pattern);
   	}
    }

    public void setMaxNumReruns(int maxNumReruns) {
	assert maxNumReruns >= -1;
	this.maxNumReruns = maxNumReruns;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
	sb.append("[ baseDirectory=")   .append(this.baseDirectory);
	sb.append(", targetSiteDirectory=") .append(this.targetSiteDirectory);
	sb.append(", targetDirectory=") .append(this.targetDirectory);
	sb.append(", texSrcDirectory=") .append(this.texSrcDirectory);
	sb.append(", tempDirectory=")   .append(this.tempDirectory);
 	sb.append(", outputDirectory=") .append(this.outputDirectory);
        sb.append(", texPath=")         .append(this.texPath);
        sb.append(", fig2devCommand=")  .append(this.fig2devCommand);
        sb.append(", texCommand=")      .append(this.texCommand);
	sb.append(", texCommandArgs=")  .append(this.texCommandArgs);
	sb.append(", patternErrLatex=") .append(this.patternErrLatex);
 	sb.append(", debugBadBoxes=")   .append(this.debugBadBoxes);
 	sb.append(", debugWarnings=")   .append(this.debugWarnings);
        sb.append(", bibtexCommand=")   .append(this.bibtexCommand);
        sb.append(", makeIndexCommand=").append(this.makeIndexCommand);
        sb.append(", patternErrMakeindex=").append(this.patternErrMakeindex);
        sb.append(", tex4htCommand=")   .append(this.tex4htCommand);
        sb.append(", tex4htStyOptions=").append(this.tex4htStyOptions);
        sb.append(", tex4htOptions=")   .append(this.tex4htOptions);
	sb.append(", t4htOptions=")     .append(this.t4htOptions);
        sb.append(", latex2rtfCommand=").append(this.latex2rtfCommand);
        sb.append(", odt2docCommand=")  .append(this.odt2docCommand);
        sb.append(", pdf2txtCommand=")  .append(this.pdf2txtCommand);
	sb.append(", patternNeedAnotherLatexRun=")
	    .append(this.patternNeedAnotherLatexRun);
	sb.append(", maxNumReruns=").append(this.maxNumReruns);
	sb.append(", cleanUp=").append(this.cleanUp);
        sb.append(']');
        return sb.toString();
    }

    public static void main(String[] args) {
	System.out.println("texpath: "+new Settings().getTexPath());
	
    }

}
