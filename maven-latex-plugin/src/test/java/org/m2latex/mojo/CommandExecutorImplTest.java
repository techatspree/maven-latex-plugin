package org.m2latex.mojo;

import java.io.File;

import junit.framework.TestCase;

import org.apache.maven.plugin.logging.SystemStreamLog;

public class CommandExecutorImplTest
    extends TestCase
{
    public void testExecute()
        throws Exception
    {
        CommandExecutorImpl executor = new CommandExecutorImpl( new SystemStreamLog() );
        String echoText = "LaTeX";
        String output = executor.execute( new File( "." ), null, "echo", new String[] { echoText } );
        assertEquals( echoText, output.subSequence( 0, echoText.length() ) );
    }
}
