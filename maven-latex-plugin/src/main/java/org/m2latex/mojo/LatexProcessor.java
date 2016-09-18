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

package org.m2latex.mojo;

import org.apache.commons.lang.StringUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.codehaus.plexus.util.cli.CommandLineException;

import java.io.File;

// idea: use latex2rtf and unoconv
// idea: targets for latex2html, latex2man, latex2png and many more. 
public class LatexProcessor
{

    private final Settings settings;

    private final CommandExecutor executor;

    private final Log log;

    private TexFileUtils fileUtils;

    public LatexProcessor( Settings settings, 
			   CommandExecutor executor, 
			   Log log, 
			   TexFileUtils fileUtils )
    {
        this.settings = settings;
        this.executor = executor;
        this.log = log;
        this.fileUtils = fileUtils;
    }

    /**
     * Runs latex on <code>texFile</code> at least once, 
     * runs bibtex and reruns latex as long as needed to get all links 
     * or as threshold {@link Settings#maxNumReruns} specifies. 
     *
     * @param texFile
     *    the tex file to be processed. 
     * @see #runLatex(File)
     * @see #runBibtex(File)
     * @see #needAnotherLatexRun(File)
     * @see #needBibtexRun(File)
     */
    public void processLatex(final File texFile)
            throws CommandLineException, MojoExecutionException
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
     * Runs tex4ht on <code>texFile</code> 
     * after processing latex to set up the references. 
     *
     * @param texFile
     *    the tex file to be processed. 
     * @see #processLatex(File)
     * @see #runTex4ht(File)
     */
    public void processTex4ht( File texFile )
            throws MojoExecutionException, CommandLineException
    {
        processLatex( texFile );
        runTex4ht( texFile );
    }

    /**
     * Runs tex4ht on <code>texFile</code>. 
     * FIXME: Maybe prior invocation of latex and bibtex would be better. 
     *
     * @param texFile
     *    the tex file to be processed. 
     * @see #processLatex(File)
     * @see #runLatex2rtf(File)
     */
     public void processLatex2rtf( File texFile )
          throws MojoExecutionException, CommandLineException
    {
	runLatex2rtf(texFile);
    }

    // used in AbstractLatexMojo.execute() only 
    public void runFig2Dev( File figFile )
	throws CommandLineException, MojoExecutionException
    {
       log.debug( "Running " + "fig1dev" + 
		   " on file " + figFile.getName() + ". ");
       File workingDir = figFile.getParentFile();
       String[] args;

       String pdf   = this.fileUtils.replaceSuffix(figFile, "pdf"  ).toString();
       String pdf_t = this.fileUtils.replaceSuffix(figFile, "pdf_t").toString();

       args = new String[] {
	   "-L",
	   "pdftex",
	   figFile.getName(), // source 
	   pdf // target 
       };
       this.executor.execute(workingDir, 
			     this.settings.getTexPath(), //**** 
			     "fig2dev", 
			     args);
      args = new String[] {
	   "-L",
	   "pdftex_t",
	   "-p",// portrait (-l for landscape), next argument ignored 
	   pdf,
	   figFile.getName(), // source 
	   pdf_t // target 
       };
       this.executor.execute(workingDir, 
			     this.settings.getTexPath(), //**** 
			     "fig2dev", 
			     args);



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
            throws CommandLineException, MojoExecutionException
    {
        log.debug( "Running " + settings.getLatex2rtfCommand() + 
		   " on file " + texFile.getName() + ". ");

        File workingDir = texFile.getParentFile();
        String[] args = buildLatex2rtfArguments( texFile );
        this.executor.execute(workingDir, 
			      this.settings.getTexPath(), 
			      this.settings.getLatex2rtfCommand(), 
			      args);

	// no check: just warning that no output has been created. 
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
    private void runTex4ht( File texFile )
            throws CommandLineException, MojoExecutionException
    {
        log.debug( "Running " + settings.getTex4htCommand() + 
		   " on file " + texFile.getName() + ". ");

        File workingDir = texFile.getParentFile();
        String[] args = buildHtlatexArguments( texFile );
        this.executor.execute(workingDir, 
			      this.settings.getTexPath(), 
			      this.settings.getTex4htCommand(), 
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
            throws MojoExecutionException
    {
        return new String[] {
	    texFile.getName(),
	    this.settings.getTex4htStyOptions(),
	    this.settings.getTex4htOptions(),
	    this.settings.getT4htOptions(),
	    this.settings.getTexCommandArgs()
	};
    }

    // FIXME: Is this the right criterion? 
    private boolean needAnotherLatexRun(File logFile)
	throws MojoExecutionException
    {
        String reRunPattern = this.settings.getPatternNeedAnotherLatexRun();
        boolean needRun = fileUtils.matchInLogFile(logFile, reRunPattern);
        log.debug( "Another Latex run? " + needRun );
        return needRun;
    }

    private boolean needBibtexRun(File logFile)
	throws MojoExecutionException
    {
        String namePrefixLogFile = this.fileUtils
	    .getFileNameWithoutSuffix(logFile);
        String pattern = "No file " + namePrefixLogFile + ".bbl";
        return this.fileUtils.matchInLogFile( logFile, pattern );
    }

    private void runMakeindex(File idxFile)
	throws CommandLineException, MojoExecutionException
    {
	log.debug( "Running makeindex on file " + idxFile.getName() + ". ");

	File workingDir = idxFile.getParentFile();
	String[] args = new String[] {idxFile.getName()};
	this.executor.execute(workingDir, 
			      this.settings.getTexPath(), 
			      "makeindex", 
			      args);

	// detect errors 
 	File logFile = this.fileUtils.replaceSuffix( idxFile, "ilg" );
	if (!logFile.exists()) {
	    this.log.error("Makeindex failed: no log file found. ");
	}
	boolean errOccurred = this.fileUtils
	    .matchInLogFile(logFile, 
			    // FIXME: List is incomplete 
			    "Extra |" + 
			    "Illegal null field|" + 
			    "Argument |" + 
			    "Illegal null field" + 
			    "Unmatched |" + 
			    "Inconsistent page encapsulator |" + 
			    "Conflicting entries");
	if (errOccurred) {
	    log.warn("Makeindex failed when running on " + idxFile + ". ");
	}
  }

     /**
     * Runs the bibtex command given by {@link Settings#getBibtexCommand()} 
     * on the aux-file corresponding with <code>texFile</code> 
     * in the directory containing <code>texFile</code>. 
     */
    private void runBibtex(File texFile)
	throws CommandLineException, MojoExecutionException
    {
        log.debug( "Running BibTeX on file " + texFile.getName() + ". ");

        File workingDir = texFile.getParentFile();
        String[] args = new String[] {
	    fileUtils.replaceSuffix( texFile, "aux" ).getName()
	};
        this.executor.execute(workingDir, 
			      this.settings.getTexPath(), 
			      this.settings.getBibtexCommand(), 
			      args);

	File logFile = this.fileUtils.replaceSuffix( texFile, "blg" );
	if (!logFile.exists()) {
	    this.log.error("BibTeX failed: no log file found. ");
	}
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
 
    }

    /**
     * Runs the latex command given by {@link Settings#getLatexCommand()} 
     * on <code>texFile</code> 
     * in the directory containing <code>texFile</code> 
     * with arguments given by {@link #buildLatexArguments(File)}. 
     */
    private void runLatex(File texFile)
	throws MojoExecutionException, CommandLineException
    {
        log.debug("Running " + settings.getTexCommand() + 
		  " on file " + texFile.getName() + ". ");

        File workingDir = texFile.getParentFile();
	String[] args = buildLatexArguments( texFile );
        this.executor.execute(workingDir, 
			      this.settings.getTexPath(), 
			      this.settings.getTexCommand(), 
			      args);

	File logFile = this.fileUtils.replaceSuffix( texFile, "log" );
	boolean errorOccurred = this.fileUtils
	    .matchInLogFile(logFile, this.settings.getPatternErrLatex());
	if (errorOccurred) {
	    log.warn("LaTeX failed to run on " + texFile + ". ");
	}
    }

    private String[] buildLatexArguments( File texFile )
    {
        String[] texCommandArgs = this.settings.getTexCommandArgs()
	    .split(" ");
        String[] args = new String[texCommandArgs.length + 1];
        System.arraycopy( texCommandArgs, 0, args, 0, texCommandArgs.length );
        args[texCommandArgs.length] = texFile.getName();
	return args;
     }
}


