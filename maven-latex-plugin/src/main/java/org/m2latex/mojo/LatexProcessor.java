/*
 * The akquinet maven-latex-plugin project
 *
 * Copyright (c) 2011 by akquinet tech@spree GmbH
 *
 * The maven-latex-plugin is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * The maven-latex-plugin is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with the maven-latex-plugin. If not, see <http://www.gnu.org/licenses/>.
 */

package org.m2latex.mojo;

import org.apache.commons.lang.StringUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.codehaus.plexus.util.cli.CommandLineException;

import java.io.File;

public class LatexProcessor
{
    static final String PATTERN_NEED_ANOTHER_LATEX_RUN = "(Rerun (LaTeX|to get cross-references right)|There were undefined references|Package natbib Warning: Citation\\(s\\) may have changed)";

    private final Settings settings;

    private final Log log;

    private final CommandExecutor executor;

    private TexFileUtils fileUtils;

    public LatexProcessor( Settings settings, CommandExecutor executor, Log log, TexFileUtils fileUtils )
    {
        this.settings = settings;
        this.executor = executor;
        this.log = log;
        this.fileUtils = fileUtils;
    }

    public void processLatex( File texFile )
            throws CommandLineException, MojoExecutionException
    {
        log.info( "Processing LaTeX file " + texFile );

        runLatex( texFile );
        if ( needBibtexRun( texFile ) )
        {
            runBibtex( texFile );
        }
        int retries = 0;
        while ( retries < 5 && needAnotherLatexRun( texFile ) )
        {
            log.debug( "Latex must be rerun" );
            runLatex( texFile );
            retries++;
        }
    }

    public void processTex4ht( File texFile )
            throws MojoExecutionException, CommandLineException
    {
        processLatex( texFile );
        runTex4ht( texFile );
    }

    private void runTex4ht( File texFile )
            throws CommandLineException, MojoExecutionException
    {
        log.debug( "Running " + settings.getTex4htCommand() + " on file " + texFile.getName() );
        File workingDir = texFile.getParentFile();
        String[] args = buildHtlatexArguments( texFile );
        executor.execute( workingDir, settings.getTexPath(), settings.getTex4htCommand(), args );
    }

    private String[] buildHtlatexArguments( File texFile )
            throws MojoExecutionException
    {
        File tex4htOutdir = fileUtils.createTex4htOutputDir( settings.getTempDirectory() );

        final String argOutputDir = " -d" + tex4htOutdir.getAbsolutePath() + File.separatorChar;
        String[] tex4htCommandArgs = settings.getTex4htCommandArgs();

        String htlatexOptions = getTex4htArgument( tex4htCommandArgs, 0 );
        String tex4htOptions = getTex4htArgument( tex4htCommandArgs, 1 );
        String t4htOptions = getTex4htArgument( tex4htCommandArgs, 2 ) + argOutputDir;
        String latexOptions = getTex4htArgument( tex4htCommandArgs, 3 );

        String[] args = new String[5];
        args[0] = texFile.getName();
        args[1] = htlatexOptions;
        args[2] = tex4htOptions;
        args[3] = t4htOptions;
        args[4] = latexOptions;

        return args;
    }

    private String getTex4htArgument( String[] args, int index )
    {
        boolean returnEmptyArg = args == null || args.length < index + 1 || StringUtils.isEmpty( args[index] );
        return returnEmptyArg ? "" : args[index];
    }

    private boolean needAnotherLatexRun( File texFile )
            throws MojoExecutionException
    {
        String reRunPattern = PATTERN_NEED_ANOTHER_LATEX_RUN;
        boolean needRun = fileUtils.matchInCorrespondingLogFile( texFile, reRunPattern );
        log.debug( "Another Latex run? " + needRun );
        return needRun;
    }

    private boolean needBibtexRun( File texFile )
            throws MojoExecutionException
    {
        String namePrefixTexFile = fileUtils.getFileNameWithoutSuffix( texFile );
        String pattern = "No file " + namePrefixTexFile + ".bbl";
        return fileUtils.matchInCorrespondingLogFile( texFile, pattern );
    }

    private void runBibtex( File texFile )
            throws CommandLineException
    {
        log.debug( "Running BibTeX on file " + texFile.getName() );
        File workingDir = texFile.getParentFile();

        String[] args = new String[]{fileUtils.getCorrespondingAuxFile( texFile ).getPath()};
        executor.execute( workingDir, settings.getTexPath(), settings.getBibtexCommand(), args );
    }

    private void runLatex( File texFile )
            throws CommandLineException
    {
        log.debug( "Running " + settings.getTexCommand() + " on file " + texFile.getName() );
        File workingDir = texFile.getParentFile();

        String[] texCommandArgs = settings.getTexCommandArgs();
        String[] args = new String[texCommandArgs.length + 1];
        System.arraycopy( texCommandArgs, 0, args, 0, texCommandArgs.length );
        args[texCommandArgs.length] = texFile.getName();
        executor.execute( workingDir, settings.getTexPath(), settings.getTexCommand(), args );
    }
}
