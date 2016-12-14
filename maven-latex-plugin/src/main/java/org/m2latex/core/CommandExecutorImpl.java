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
     * Logging: 
     * <ul>
     * <li> WEX01: return code other than 0. 
     * <li> WEX02: no target file 
     * <li> WEX03: target file not updated 
     * <li> WEX04: cannot read target file 
     * <li> WEX05: may emit false warnings
     * </ul>
     */
    public String execute(File workingDir, 
			  File pathToExecutable, 
			  String command, 
			  String[] args, 
			  File... resFile) throws BuildFailureException {
	// analyze old result files 
	assert resFile.length > 0;
	boolean[] existsTarget = new boolean[resFile.length];
	long[] lastModifiedTarget = new long[resFile.length];
	long currentTime = System.currentTimeMillis();
	long minTimePast = Long.MAX_VALUE;
	for (int idx = 0; idx < resFile.length; idx++) {
	    existsTarget      [idx] = resFile[idx].exists();
	    lastModifiedTarget[idx] = resFile[idx].lastModified();
	    assert lastModifiedTarget[idx] <= currentTime;
	    // correct even if lastModifiedTarget[idx]==0 
	    minTimePast = Math.min(minTimePast, 
				   currentTime-lastModifiedTarget[idx]);
	}

	// FIXME: this is based on a file system 
	// with modification time in steps of seconds, i.e. 1000ms 
	if (minTimePast < 1001) {
	    try {
		// 1001 is the minimal span of time to change modification time 
		Thread.sleep(1001-minTimePast);// for update control of target 
	    } catch (InterruptedException ie) {
		this.log.warn
		    ("WEX05: Update control may emit false warnings. ");
	    }
	}

	// Proper execution 
	// may throw BuildFailureException TEX01, log warning WEX01 
	String res = execute(workingDir, pathToExecutable, command, args);

	// may log WEX02, WEX03, WEX04 
	for (int idx = 0; idx < resFile.length; idx++) {
	    isUpdatedOrWarn(command, resFile[idx], 
			    existsTarget[idx], lastModifiedTarget[idx]);
	}

	return res;
    }


    // returns whether this method logged a warning 
    // FIXME: return value nowhere used 
    /**
     * Logging: 
     * <ul>
     * <li> WEX02: no target file 
     * <li> WEX03: target file not updated 
     * <li> WEX04: cannot read target file 
     * </ul>
     */
    private boolean isUpdatedOrWarn(String command, 
				    File target, 
				    boolean existedBefore,
				    long lastModifiedBefore) {
	if (!target.exists()) {
	    this.log.warn("WEX02: Running " + command + 
			  " failed: No target file '" + 
			  target.getName() + "' written. ");
	    return false;
	}
	assert target.exists();
	if (!existedBefore) {
	    return true;
	}
	assert existedBefore && target.exists();

	long lastModifiedAfter = target.lastModified();
	if (lastModifiedBefore == 0 || lastModifiedAfter == 0) {
	    this.log.warn("WEX04: Cannot read target file '" + 
			  target.getName() + "'; may be outdated. ");
	    return false;
	}
	assert lastModifiedBefore > 0 && lastModifiedAfter > 0;

	if (lastModifiedAfter <= lastModifiedBefore) {
	    assert lastModifiedAfter == lastModifiedBefore;
	    this.log.warn("WEX03: Running " + command + 
			  " failed: Target file '" + 
			  target.getName() + "' is not updated. ");
	    return false;
	}
	return true;
    }

    /**
     * Execute <code>command</code> with arguments <code>args</code> 
     * in the working directory <code>workingDir</code>. 
     * Here, <code>pathToExecutable</code> is the path 
     * to the executable. May be null? 
     * <p>
     * Logging: 
     * WEX01 for return code other than 0. 
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
    private String execute(File workingDir, 
			   File pathToExecutable, 
			   String command, 
			   String[] args) throws BuildFailureException {

	// prepare execution 
	String executable = new File(pathToExecutable, command).getPath();
	Commandline cl = new Commandline(executable);
	cl.getShell().setQuotedArgumentsEnabled(false);
	cl.addArguments(args);
	cl.setWorkingDirectory(workingDir.getPath());
	StringStreamConsumer output = new StringStreamConsumer();
	log.debug("Executing: " + cl + " in: " + workingDir + ". ");

	// perform execution and collect results 
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
