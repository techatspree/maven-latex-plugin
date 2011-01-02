package org.m2latex.mojo;

import java.io.File;

import org.apache.maven.plugin.logging.Log;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;
import org.codehaus.plexus.util.cli.CommandLineUtils.StringStreamConsumer;

public class CommandExecutorImpl
    implements CommandExecutor
{
    private final Log log;

    public CommandExecutorImpl( Log log )
    {
        this.log = log;
    }

    public final String execute( File workingDir, File pathToExecutable, String executable, String[] args )
        throws CommandLineException
    {
        String command = new File( pathToExecutable, executable ).getPath();
        Commandline cl = new Commandline( command );
        cl.addArguments( args );
        cl.setWorkingDirectory( workingDir.getPath() );
        StringStreamConsumer output = new StringStreamConsumer();
        log.debug( "Executing: " + cl + " in: " + workingDir );
        CommandLineUtils.executeCommandLine( cl, output, output );
        log.debug( "Output:\n" + output.getOutput() + "\n" );
        return output.getOutput();
    }
}
