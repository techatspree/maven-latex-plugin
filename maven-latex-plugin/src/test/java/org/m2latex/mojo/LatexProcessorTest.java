package org.m2latex.mojo;

import java.io.File;

import junit.framework.TestCase;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugin.logging.SystemStreamLog;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.easymock.MockControl;

public class LatexProcessorTest
    extends TestCase
{
    private MockControl executorCtrl = MockControl.createStrictControl( CommandExecutor.class );

    private CommandExecutor executor = (CommandExecutor) executorCtrl.getMock();

    private MockControl fileUtilsCtrl = MockControl.createStrictControl( TexFileUtils.class );

    private TexFileUtils fileUtils = (TexFileUtils) fileUtilsCtrl.getMock();

    private Settings settings = new Settings();

    private Log log = new SystemStreamLog();

    private LatexProcessor processor = new LatexProcessor( settings, executor, log, fileUtils );

    private File texFile = new File( System.getProperty( "tmp.dir" ), "test.tex" );

    private File auxFile = new File( System.getProperty( "tmp.dir" ), "test.aux" );

    private String[] latexArgsExpected = new String[] { "-interaction=nonstopmode", "--src-specials", texFile.getName() };

    private String[] tex4htArgsExpected = new String[] {
        texFile.getName(),
        "html,2",
        "",
        " -d" + new File( settings.getTempDirectory(), LatexProcessor.TEX4HT_OUTPUT_DIR ).getAbsolutePath()
            + File.separatorChar,
        "-interaction=nonstopmode --src-specials" };

    public void testProcessLatexSimple()
        throws Exception
    {
        mockRunLatex();
        mockNeedBibtexRun( false );
        mockNeedAnotherLatexRun( false );

        replay();

        processor.processLatex( texFile );

        verify();
    }

    public void testProcessLatexWithBibtex()
        throws Exception
    {
        mockRunLatex();
        mockNeedBibtexRun( true );
        mockRunBibtex();
        mockNeedAnotherLatexRun( true );
        mockRunLatex();
        mockNeedAnotherLatexRun( true );
        mockRunLatex();
        mockNeedAnotherLatexRun( false );

        replay();

        processor.processLatex( texFile );

        verify();
    }

    public void testProcessTex4ht()
        throws Exception
    {
        mockRunLatex();
        mockNeedBibtexRun( false );
        mockNeedAnotherLatexRun( false );
        mockRunTex4ht();

        replay();

        processor.processTex4ht( texFile );

        verify();
    }

    private void mockNeedAnotherLatexRun( boolean returnValue )
        throws MojoExecutionException
    {
        fileUtils.matchInCorrespondingLogFile( texFile, LatexProcessor.PATTERN_NEED_ANOTHER_LATEX_RUN );
        fileUtilsCtrl.setReturnValue( returnValue );
    }

    private void mockNeedBibtexRun( boolean returnValue )
        throws MojoExecutionException
    {
        fileUtils.getFileNameWithoutSuffix( texFile );
        fileUtilsCtrl.setReturnValue( texFile.getName().split( "\\." )[0] );

        fileUtils.matchInCorrespondingLogFile( texFile, "No file test.bbl" );
        fileUtilsCtrl.setReturnValue( returnValue );
    }

    private void mockRunBibtex()
        throws CommandLineException
    {
        fileUtils.getCorrespondingAuxFile( texFile );
        fileUtilsCtrl.setReturnValue( auxFile );

        executor.execute( texFile.getParentFile(), settings.getTexPath(), settings.getBibtexCommand(),
                          new String[] { auxFile.getPath() } );
        executorCtrl.setMatcher( MockControl.ARRAY_MATCHER );
        executorCtrl.setReturnValue( null );
    }

    private void mockRunLatex()
        throws CommandLineException
    {
        executor.execute( texFile.getParentFile(), settings.getTexPath(), settings.getTexCommand(), latexArgsExpected );
        executorCtrl.setMatcher( MockControl.ARRAY_MATCHER );
        executorCtrl.setReturnValue( null );
    }

    private void mockRunTex4ht()
        throws CommandLineException
    {
        executor.execute( texFile.getParentFile(), settings.getTexPath(), settings.getTex4htCommand(),
                          tex4htArgsExpected );
        executorCtrl.setMatcher( MockControl.ARRAY_MATCHER );
        executorCtrl.setReturnValue( null );
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
