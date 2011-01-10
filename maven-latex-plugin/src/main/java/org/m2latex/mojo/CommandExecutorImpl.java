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
