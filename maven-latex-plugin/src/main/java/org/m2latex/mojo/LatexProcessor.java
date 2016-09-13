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
public class LatexProcessor
{

    private final Settings settings;

    private final Log log;

    private final CommandExecutor executor;

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
     * @see #needLatexRun(File)
     * @see #needBibtexRun(File)
     */
    public void processLatex(final File texFile)
            throws CommandLineException, MojoExecutionException
    {
        log.info("Processing LaTeX file " + texFile + ". ");

        runLatex( texFile );
        if ( needBibtexRun( texFile ) )
        {
            log.debug("Bibtex must be run. ");
            runBibtex( texFile );
        }
        int retries = 0;
	boolean needAnotherLatexRun = true;
	int maxNumReruns = this.settings.getMaxNumReruns();
        while ((maxNumReruns == -1 || retries < maxNumReruns)
	       && (needAnotherLatexRun = needAnotherLatexRun( texFile )) )
        {
            log.debug("Latex must be rerun. ");
            runLatex( texFile );
            retries++;
        }
	if (needAnotherLatexRun) {
	    log.warn("Max rerun reached although " + texFile +
		     " needs another run. ");
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
     * Runs the tex4ht command given by {@link Settings#getTex4htCommand()} 
     * on <code>texFile</code> in the directory containing <code>texFile</code> 
     * with arguments given by {@link #buildHtlatexArguments(File)}. 
     */
    private void runTex4ht( File texFile )
            throws CommandLineException, MojoExecutionException
    {
        log.debug( "Running " + settings.getTex4htCommand() + 
		   " on file " + texFile.getName() + ". ");
        File workingDir = texFile.getParentFile();
        String[] args = buildHtlatexArguments( texFile );
        this.executor.execute( workingDir, 
			       this.settings.getTexPath(), 
			       this.settings.getTex4htCommand(), 
			       args );
    }

    private String[] buildHtlatexArguments( File texFile )
            throws MojoExecutionException
    {
        File tex4htOutdir = fileUtils
	    .createTex4htOutputDir( settings.getTempDirectory() );

        final String argOutputDir = " -d" 
	    + tex4htOutdir.getAbsolutePath() + File.separatorChar;
        String[] tex4htCommandArgs = settings.getTex4htCommandArgs();

        String htlatexOptions = getTex4htArgument( tex4htCommandArgs, 0 );
        String tex4htOptions  = getTex4htArgument( tex4htCommandArgs, 1 );
        String t4htOptions    = getTex4htArgument( tex4htCommandArgs, 2 ) 
	    + argOutputDir;
        String latexOptions   = getTex4htArgument( tex4htCommandArgs, 3 );

        return new String[] {
	    texFile.getName(),
	    htlatexOptions,
	    tex4htOptions,
	    t4htOptions,
	    latexOptions
	};
    }

    private String getTex4htArgument(String[] args, int index)
    {
        boolean returnEmptyArg = 
	    args == null 
	    || args.length <= index
	    || StringUtils.isEmpty( args[index] );
        return returnEmptyArg ? "" : args[index];
    }

    // FIXME: Is this the right criterion? 
    private boolean needAnotherLatexRun(File texFile)
            throws MojoExecutionException
    {
        String reRunPattern = this.settings.getPatternNeedAnotherLatexRun();
        boolean needRun = fileUtils.matchInCorrespondingLogFile(texFile, 
								reRunPattern);
        log.debug( "Another Latex run? " + needRun );
        return needRun;
    }

    private boolean needBibtexRun(File texFile)
            throws MojoExecutionException
    {
        String namePrefixTexFile = fileUtils.getFileNameWithoutSuffix(texFile);
        String pattern = "No file " + namePrefixTexFile + ".bbl";
        return fileUtils.matchInCorrespondingLogFile( texFile, pattern );
    }

    /**
     * Runs the bibtex command given by {@link Settings#getBibtexCommand()} 
     * on the aux-file corresponding with <code>texFile</code> 
     * in the directory containing <code>texFile</code>. 
     */
    private void runBibtex(File texFile)
            throws CommandLineException
    {
        log.debug( "Running BibTeX on file " + texFile.getName() + ". ");
        File workingDir = texFile.getParentFile();

        String[] args = new String[] {
	    fileUtils.getCorrespondingAuxFile( texFile ).getName()
	};
        this.executor.execute( workingDir, 
			       this.settings.getTexPath(), 
			       this.settings.getBibtexCommand(), 
			       args );
    }

    /**
     * Runs the tex4ht command given by {@link Settings#getLatexCommand()} 
     * on <code>texFile</code> in the directory containing <code>texFile</code> 
     * with arguments given by {@link #buildLatexArguments(File)}. 
     */
    private void runLatex(File texFile)
            throws CommandLineException
    {
        log.debug("Running " + settings.getTexCommand() + 
		  " on file " + texFile.getName() + ". ");
        File workingDir = texFile.getParentFile();

	String[] args = buildLatexArguments( texFile );
        this.executor.execute( workingDir, 
			       this.settings.getTexPath(), 
			       this.settings.getTexCommand(), 
			       args );
    }

    private String[] buildLatexArguments( File texFile )
    {
        String[] texCommandArgs = settings.getTexCommandArgs();
        String[] args = new String[texCommandArgs.length + 1];
        System.arraycopy( texCommandArgs, 0, args, 0, texCommandArgs.length );
        args[texCommandArgs.length] = texFile.getName();
	return args;
    }


}


