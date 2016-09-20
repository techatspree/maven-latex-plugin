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

import java.io.File;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugin.logging.SystemStreamLog;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.easymock.MockControl;

import org.junit.Test;

public class LatexProcessorTest
{
 
    private MockControl executorCtrl = MockControl
	.createStrictControl( CommandExecutor.class );

    private CommandExecutor executor = (CommandExecutor) executorCtrl.getMock();

    private MockControl fileUtilsCtrl = MockControl
	.createStrictControl( TexFileUtils.class );

    private TexFileUtils fileUtils = (TexFileUtils) fileUtilsCtrl.getMock();

    private Settings settings = new Settings();

    private Log log = new SystemStreamLog();

    private LatexProcessor processor = new LatexProcessor
	( settings, executor, log, fileUtils );

    private File texFile = new File(System.getProperty("tmp.dir"), "test.tex");
    private File auxFile = new File(System.getProperty("tmp.dir"), "test.aux");
    private File logFile = new File(System.getProperty("tmp.dir"), "test.log");
    private File blgFile = new File(System.getProperty("tmp.dir"), "test.blg");
    private File idxFile = new File(System.getProperty("tmp.dir"), "test.idx");


    private String[] latexArgsExpected = new String[] {
	 "-interaction=nonstopmode", 
	 "-src-specials", 
	 //"-interaction=nonstopmode -src-specials", 
	this.texFile.getName() 
    };

    private String[] tex2htmlArgsExpected = new String[] {
        this.texFile.getName(),
        "html,2",
        "",
        "",
        "-interaction=nonstopmode -src-specials"
    };

    @Test public void testProcessLatexSimple()
        throws Exception
    {
	// run latex 
        mockRunLatex();

	// determine from log whether bibtex shall be run: no 
	fileUtils.replaceSuffix( texFile, "log" );
	fileUtilsCtrl.setReturnValue( logFile );
        mockNeedBibtexRun( false );

	// determine from presence of idx, whether to run makeindex: no 
	fileUtils.replaceSuffix( texFile, "idx" );
	fileUtilsCtrl.setReturnValue( idxFile );

	// determine whether to rerun latex: no 
        mockNeedAnotherLatexRun( false );

	// detect bad boxes and warnings: none 
	fileUtils.matchInLogFile(logFile, "(Und|Ov)erful \\[hv]box");
	fileUtilsCtrl.setReturnValue( false );
	fileUtils.matchInLogFile(logFile, "Warning ");
	fileUtilsCtrl.setReturnValue( false );

        replay();

        processor.processLatex2pdf( this.texFile );

        verify();
    }

    @Test public void testProcessLatexWithBibtex()
        throws Exception
    {
	// run latex 
        mockRunLatex();

	// determine from log whether bibtex shall be run: yes and run it 
	fileUtils.replaceSuffix( texFile, "log" );
	fileUtilsCtrl.setReturnValue( logFile );
        mockNeedBibtexRun( true );
        mockRunBibtex();

	// determine from presence of idx, whether to run makeindex: no 
	fileUtils.replaceSuffix( texFile, "idx" );
	fileUtilsCtrl.setReturnValue( idxFile );

	// determine whether to rerun latex and run until no 
        mockNeedAnotherLatexRun( true );
        mockRunLatex();
        mockNeedAnotherLatexRun( true );
        mockRunLatex();
        mockNeedAnotherLatexRun( false );

	// detect bad boxes and warnings: none 
	fileUtils.matchInLogFile(logFile, "(Und|Ov)erful \\[hv]box");
	fileUtilsCtrl.setReturnValue( false );
	fileUtils.matchInLogFile(logFile, "Warning ");
	fileUtilsCtrl.setReturnValue( false );

        replay();

        processor.processLatex2pdf( this.texFile );

        verify();
    }

   @Test public void testProcessTex2html()
        throws Exception
    {
 	// run latex 
        mockRunLatex();

	// determine from log whether bibtex shall be run: no 
	fileUtils.replaceSuffix( texFile, "log" );
	fileUtilsCtrl.setReturnValue( logFile );
	mockNeedBibtexRun( false );

	// determine from presence of idx, whether to run makeindex: no 
	fileUtils.replaceSuffix( texFile, "idx" );
	fileUtilsCtrl.setReturnValue( idxFile );

	// determine whether to rerun latex: no 
        mockNeedAnotherLatexRun( false );

	// detect bad boxes and warnings: none 
	fileUtils.matchInLogFile(logFile, "(Und|Ov)erful \\[hv]box");
	fileUtilsCtrl.setReturnValue( false );
	fileUtils.matchInLogFile(logFile, "Warning ");
	fileUtilsCtrl.setReturnValue( false );

        mockRunTex2html();

        replay();

        processor.processTex2html(this. texFile );

        verify();
    }

    private void mockNeedAnotherLatexRun( boolean returnValue )
        throws MojoExecutionException
    {
        fileUtils.matchInLogFile(logFile, 
				 this.settings.getPatternNeedAnotherLatexRun());
        fileUtilsCtrl.setReturnValue( returnValue );
    }

    private void mockNeedBibtexRun( boolean returnValue )
        throws MojoExecutionException
    {
        fileUtils.getFileNameWithoutSuffix( logFile );
        fileUtilsCtrl.setReturnValue( logFile.getName().split( "\\." )[0] );

        fileUtils.matchInLogFile( logFile, "No file test.bbl" );
        fileUtilsCtrl.setReturnValue( returnValue );
    }

    private void mockRunBibtex()
        throws CommandLineException, MojoExecutionException
    {
        fileUtils.replaceSuffix( texFile, "aux" );
        fileUtilsCtrl.setReturnValue( auxFile );

        executor.execute(texFile.getParentFile(),
			 settings.getTexPath(),
			 settings.getBibtexCommand(),
			 new String[] { auxFile.getPath() } );
        executorCtrl.setMatcher( MockControl.ARRAY_MATCHER );
        executorCtrl.setReturnValue( null );

	fileUtils.replaceSuffix( texFile, "blg" );
	fileUtilsCtrl.setReturnValue( blgFile );

	// fileUtils.matchInLogFile(blgFile, "Error");
	// fileUtilsCtrl.setReturnValue( false );

	// fileUtils.matchInLogFile(blgFile, "Warning");
	// fileUtilsCtrl.setReturnValue( false );
    }

    private void mockRunLatex()
        throws CommandLineException, MojoExecutionException
    {
        executor.execute(texFile.getParentFile(),
			 settings.getTexPath(),
			 settings.getTexCommand(),
			 latexArgsExpected );
        executorCtrl.setMatcher( MockControl.ARRAY_MATCHER );
        executorCtrl.setReturnValue( null );

	fileUtils.replaceSuffix( texFile, "log" );
	fileUtilsCtrl.setReturnValue( logFile );

	// fileUtils.matchInLogFile(logFile, this.settings.getPatternErrLatex());
	// fileUtilsCtrl.setReturnValue( false );
    }

    private void mockRunTex2html()
            throws CommandLineException, MojoExecutionException
    {
        executor.execute(texFile.getParentFile(),
			 settings.getTexPath(),
			 settings.getTex4htCommand(),
			 tex2htmlArgsExpected );
        executorCtrl.setMatcher( MockControl.ARRAY_MATCHER );
        executorCtrl.setReturnValue( null );

	fileUtils.replaceSuffix( texFile, "log" );
	fileUtilsCtrl.setReturnValue( logFile );

	fileUtils.matchInLogFile(logFile, this.settings.getPatternErrLatex());
	fileUtilsCtrl.setReturnValue( false );
    }

    private void replay()
    {
        executorCtrl.replay();
        fileUtilsCtrl.replay();
    }

    private void verify()
    {
        executorCtrl.verify();
        fileUtilsCtrl.verify();
    }
}
