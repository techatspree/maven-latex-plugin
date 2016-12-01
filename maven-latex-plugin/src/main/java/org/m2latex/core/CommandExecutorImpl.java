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

import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.Commandline;// constructor
import static org.codehaus.plexus.util.cli.CommandLineUtils.executeCommandLine;
import org.codehaus.plexus.util.cli.CommandLineUtils.StringStreamConsumer;

/**
 * Execution of an executable with given arguments 
 * in a given working directory logging on {@link #log}. 
 * Sole interface to <code>org.codehaus.plexus.util.cli</code>. 
 */
class CommandExecutorImpl implements CommandExecutor {

    private final LogWrapper log;

    CommandExecutorImpl(LogWrapper log) {
        this.log = log;
    }

    /**
     * Execute <code>command</code> with arguments <code>args</code> 
     * in the working directory <code>workingDir</code>. 
     * Here, <code>pathToExecutable</code> is the path 
     * to the executable. May be null? 
     * <p>
     * Logs a warning, if the <code>command</code> returns 
     * with return code other than <code>0</code>. 
     *
     * @param workingDir
     *    the working directory. 
     *    The shell changes to that directory 
     *    before invoking <code>command</code> 
     *    with arguments <code>args</code>. 
     * @param pathToExecutable
     *    the path to the executable <code>command</code>. 
     *    This may be <code>null</code> if <code>command</code> 
     *    is on the execution path 
     * @param command
     *    the name of the program to be executed 
     * @param args
     *    the list of arguments, 
     *    each containing a blank enclosed in double quotes. 
     * @throws BuildFailureException
     *    TEX01 if invocation of <code>command</code> fails very basically: 
     *    <ul>
     *    <li><!-- see Commandline.execute() -->
     *    the file expected to be the working directory 
     *    does not exist or is not a directory. 
     *    <li><!-- see Commandline.execute() -->
     *    {@link Runtime#exec(String, String[], File)} fails 
     *    throwing an {@link IOException}. 
     *    <li> <!-- see CommandLineCallable.call() -->
     *    an error inside systemOut parser occurs 
     *    <li> <!-- see CommandLineCallable.call() -->
     *    an error inside systemErr parser occurs 
     *    <li> Wrapping an {@link InterruptedException} 
     *    on the process to be executed thrown by {@link Process#waitFor()}. 
     *    </ul>
     */
    public final String execute(File workingDir, 
				File pathToExecutable, 
				String command, 
				String[] args) throws BuildFailureException {
	String executable = new File(pathToExecutable, command).getPath();
	Commandline cl = new Commandline(executable);
	cl.getShell().setQuotedArgumentsEnabled(false);
	cl.addArguments(args);
	cl.setWorkingDirectory(workingDir.getPath());
	StringStreamConsumer output = new StringStreamConsumer();
	log.debug("Executing: " + cl + " in: " + workingDir + ". ");

	try {
	    // may throw CommandLineException 
	    int returnCode = executeCommandLine(cl, output, output);
	    if (returnCode != 0) {
		this.log.warn("WEX01: Running " + command + 
			      " failed with return code " + returnCode + ". ");
	    }
	} catch (CommandLineException e) {
	    throw new BuildFailureException
		("TEX01: Error running " + command +  ". ", e);
        }

	log.debug("Output:\n" + output.getOutput() + "\n");
	return output.getOutput();
    }
}
