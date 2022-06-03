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

package eu.simuline.m2latex.core;

import java.io.File;

import        org.codehaus.plexus.util.cli.CommandLineException;
import        org.codehaus.plexus.util.cli.Commandline;// constructor
import static org.codehaus.plexus.util.cli.CommandLineUtils.executeCommandLine;
import        org.codehaus.plexus.util.cli.CommandLineUtils.StringStreamConsumer;

/**
 * Execution of an executable with given arguments 
 * in a given working directory logging on {@link #log}. 
 * Sole interface to <code>org.codehaus.plexus.util.cli</code>. 
 */
class CommandExecutor {

    static class CmdResult {
        final String output;
        final boolean success;
        CmdResult(String output, boolean success) {
            this.output = output;
            this.success = success;
        }
    } // class CmdResult 

    private final LogWrapper log;

    CommandExecutor(LogWrapper log) {
        this.log = log;
    }


    /**
     * Executes <code>command</code> in <code>workingDir</code>
     * with list of arguments given by <code>args</code> 
     * and logs if one of the expected target files 
     * given by <code>resFile</code> is not newly created, 
     * i.e. if it does not exist or is not updated. 
     * Logging: 
     * <ul>
     * <li> EEX01: return code other than 0. Provided resFiles is not empty. 
     * In fact, there are two use cases: either result is resFiles, 
     * or, if this is empty, it is the return value. 
     * <li> EEX02: no target file
     * <li> EEX03: target file not updated
     * <li> WEX04: cannot read target file
     * <li> WEX05: may emit false warnings
     * </ul>
     *
     * @param workingDir
     *    the working directory or <code>null</code>. 
     *    The shell changes to that directory 
     *    before invoking <code>command</code> 
     *    with arguments <code>args</code> if this is not <code>null</code>. 
     *    Argument <code>null</code> is allowed only 
     *    if no result files are given by <code>resFile</code>. 
     *    Essentially this is just needed to determine the version. 
     * @param pathToExecutable
     *    the path to the executable <code>command</code>. 
     *    This may be <code>null</code> if <code>command</code> 
     *    is on the execution path 
     * @param command
     *    the name of the program to be executed 
     * @param args
     *    the list of arguments, 
     *    each containing a blank enclosed in double quotes. 
     * @param resFiles
     *    optional result files, i.e. target files which shall be updated 
     *    by this command. 
     * @return
     *    the output of the command which comprises the output stream 
     *    and whether the return code is nonzero, i.e. the command succeeded. 
     *    The io stream is used in tests only whereas the return code is used for pdfdiffs. 
     * @throws BuildFailureException
     *    TEX01 if invocation of <code>command</code> fails very basically: 
     *    <ul>
     *    <li><!-- see Commandline.execute() -->
     *    the file expected to be the working directory 
     *    does not exist or is not a directory. 
     *    <li><!-- see Commandline.execute() -->
     *    {@link Runtime#exec(String, String[], File)} fails 
     *    throwing an {@link java.io.IOException}. 
     *    <li> <!-- see CommandLineCallable.call() -->
     *    an error inside systemOut parser occurs 
     *    <li> <!-- see CommandLineCallable.call() -->
     *    an error inside systemErr parser occurs 
     *    <li> Wrapping an {@link InterruptedException} 
     *    on the process to be executed thrown by {@link Process#waitFor()}. 
     *    </ul>
     */
    CmdResult execute(File workingDir, 
		   File pathToExecutable, 
		   String command, 
		   String[] args, 
		   File... resFiles) throws BuildFailureException {
	// analyze old result files 
	//assert resFile.length > 0;
	boolean[] existsTarget = new boolean[resFiles.length];
	long[] lastModifiedTarget = new long[resFiles.length];
	long currentTime = System.currentTimeMillis();
	long minTimePast = Long.MAX_VALUE;
	for (int idx = 0; idx < resFiles.length; idx++) {
	    existsTarget      [idx] = resFiles[idx].exists();
	    lastModifiedTarget[idx] = resFiles[idx].lastModified();
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

	if (workingDir == null && resFiles.length != 0) {
	    throw new IllegalStateException
	    ("Working directory shall be determined but was null. ");
	}

	// Proper execution 
	// may throw BuildFailureException TEX01, log warning EEX01 
	CmdResult res = execute(workingDir, pathToExecutable, command, resFiles.length > 0, args);

	// may log EEX02, EEX03, WEX04 
	for (int idx = 0; idx < resFiles.length; idx++) {
	    isUpdatedOrWarn(command, resFiles[idx], 
			    existsTarget[idx], lastModifiedTarget[idx]);
	}

	return res;
    }

    // returns whether this method logged an error or a warning 
    // FIXME: return value nowhere used 
    /**
     * @param command
     *    the name of the program to be executed 
     *
     * Logging: 
     * <ul>
     * <li> EEX02: no target file 
     * <li> EEX03: target file not updated 
     * <li> WEX04: cannot read target file 
     * </ul>
     */
    private boolean isUpdatedOrWarn(String command, 
				    File target, 
				    boolean existedBefore,
				    long lastModifiedBefore) {
	if (!target.exists()) {
	    this.log.error("EEX02: Running " + command + 
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
	    this.log.error("EEX03: Running " + command + 
			   " failed: Target file '" + 
			   target.getName() + "' is not updated. ");
	    return false;
	}
	return true;
    }

    /**
     * Execute <code>command</code> with arguments <code>args</code> 
     * in the working directory <code>workingDir</code> 
     * and return the output. 
     * Here, <code>pathToExecutable</code> is the path 
     * to the executable. It may be null. 
     * <p>
     * Logging: 
     * EEX01 for return code other than 0. 
     *
     * @param workingDir
     *    the working directory or <code>null</code>.
     *    The shell changes to that directory 
     *    before invoking <code>command</code> 
     *    with arguments <code>args</code> if this is not <code>null</code>.
     * @param pathToExecutable
     *    the path to the executable <code>command</code>. 
     *    This may be <code>null</code> if <code>command</code> 
     *    is on the execution path 
     * @param command
     *    the name of the program to be executed 
     * @param args
     *    the list of arguments, 
     *    each containing a blank enclosed in double quotes. 
     * @return
     *    the output of the command which comprises the output stream 
     *    and whether the return code is nonzero, i.e. the command succeeded. 
     * @throws BuildFailureException
     *    TEX01 if invocation of <code>command</code> fails very basically: 
     *    <ul>
     *    <li><!-- see Commandline.execute() -->
     *    the file expected to be the working directory 
     *    does not exist or is not a directory. 
     *    <li><!-- see Commandline.execute() -->
     *    {@link Runtime#exec(String, String[], File)} fails 
     *    throwing an {@link java.io.IOException}. 
     *    <li> <!-- see CommandLineCallable.call() -->
     *    an error inside systemOut parser occurs 
     *    <li> <!-- see CommandLineCallable.call() -->
     *    an error inside systemErr parser occurs 
     *    <li> Wrapping an {@link InterruptedException} 
     *    on the process to be executed thrown by {@link Process#waitFor()}. 
     *    </ul>
     */
    private CmdResult execute(File workingDir,
	    File pathToExecutable,
	    String command,
        boolean checkReturnCode,
	    String[] args) throws BuildFailureException {

	// prepare execution 
	String executable = new File(pathToExecutable, command).getPath();
	Commandline cl = new Commandline(executable);
	cl.getShell().setQuotedArgumentsEnabled(true);
	cl.addArguments(args);
	if (workingDir != null) {
	    cl.setWorkingDirectory(workingDir.getPath());
	}
	StringStreamConsumer output = new StringStreamConsumer();
	log.debug("Executing: " + cl + " in: " + workingDir + ". ");

	// perform execution and collect results 
    int returnCode = -1;
	try {
	    // may throw CommandLineException 
	    returnCode = executeCommandLine(cl, output, output);
	    if (returnCode != 0 && checkReturnCode) {
		this.log.error("EEX01: Running " + command + 
			       " failed with return code " + returnCode + ". ");
	    }
	} catch (CommandLineException e) {
	    throw new BuildFailureException
		("TEX01: Error running " + command +  ". ", e);
        }

	log.debug("Output:\n" + output.getOutput() + "\n");
	return new CmdResult(output.getOutput(), returnCode == 0);
    }
}
