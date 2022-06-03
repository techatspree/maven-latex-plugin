package org.codehaus.plexus.util.cli;

/*
 * Copyright The Codehaus Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;
import org.codehaus.plexus.util.Os;
import org.codehaus.plexus.util.StringUtils;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l </a>
 * @version $Id$
 */
public abstract class CommandLineUtils
{
    public static class StringStreamConsumer
        implements StreamConsumer
    {
        private StringBuffer string = new StringBuffer();

        private String ls = System.getProperty( "line.separator" );

        public void consumeLine( String line )
        {
            string.append( line ).append( ls );
        }

        public String getOutput()
        {
            return string.toString();
        }
    }

    private static class ProcessHook extends Thread {
        private final Process process;

        private ProcessHook( Process process )
        {
            super("CommandlineUtils process shutdown hook");
            this.process = process;
            this.setContextClassLoader(  null );
        }

        public void run()
        {
            process.destroy();
        }
    }

    /**
     * @throws CommandLineException 
     *    (without {@link CommandLineTimeOutException}) if 
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
     *    </ul>
     * @throws CommandLineTimeOutException 
     *    Wrapping an {@link InterruptedException} 
     *    on the process to be executed thrown by {@link Process#waitFor()}. 
     */
    // used in org.m2latex.core.CommandExecutorImpl 
    public static int executeCommandLine(Commandline cl, 
					 StreamConsumer systemOut, 
					 StreamConsumer systemErr)
        throws CommandLineException
    {
	// may throw CommandLineException 
        return executeCommandLine(cl, null, systemOut, systemErr, 0);
    }

    // public static int executeCommandLine(Commandline cl, 
    // 					 StreamConsumer systemOut, 
    // 					 StreamConsumer systemErr,
    // 					 int timeoutInSeconds)
    //     throws CommandLineException
    // {
    // 	// may throw CommandLineException 
    //     return executeCommandLine
    // 	    (cl, null, systemOut, systemErr, timeoutInSeconds);
    // }

    // public static int executeCommandLine(Commandline cl, 
    // 					 InputStream systemIn, 
    // 					 StreamConsumer systemOut,
    // 					 StreamConsumer systemErr)
    //     throws CommandLineException
    // {
    // 	// may throw CommandLineException 
    //     return executeCommandLine(cl, systemIn, systemOut, systemErr, 0);
    // }

    /**
     * @param cl               
     *    The command line to execute
     * @param systemIn         
     *    The input to read from, must be thread safe
     * @param systemOut        
     *    A consumer that receives output, must be thread safe
     * @param systemErr        
     *    A consumer that receives system error stream output, 
     *    must be thread safe
     * @param timeoutInSeconds 
     *    Positive integer to specify timeout, 
     *    zero and negative integers for no timeout.
     * @return 
     *    A return value, see {@link Process#exitValue()}
     * @throws CommandLineException 
     *    (without {@link CommandLineTimeOutException}) if 
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
     *    </ul>
     * @throws CommandLineTimeOutException 
     *    Wrapping an {@link InterruptedException} 
     *    on the process to be executed: 
     *    If <code>timeoutInSeconds&lt;=0</code> 
     *    thrown by {@link Process#waitFor()}. 
     *    If <code>timeoutInSeconds &gt; 0</code> thrown 
     *    <ul>
     *    <li>by {@link Thread#sleep(long)} 
     *    <li>if after sleep {@link #isAlive(Process)} holds. 
     *    <li>by 
     *    {@link #waitForAllPumpers(StreamFeeder, StreamPumper, StreamPumper)}
     *     </ul>
     */
    // used but invoked only with sytemIn==null and timeoutInSeconds==0 
    public static int executeCommandLine(Commandline cl, 
					 InputStream systemIn, 
					 StreamConsumer systemOut,
					 StreamConsumer systemErr, 
					 int timeoutInSeconds)
    throws CommandLineException {
	// may throw CommandLineException 
        final CommandLineCallable future = executeCommandLineAsCallable
            (cl, systemIn, systemOut, systemErr, timeoutInSeconds);
	// may throw CommandLineException: 
	// see docs of executeCommandLineAsCallable: 
	// - error inside systemOut/systemErr parser 
	// - wrapping InterruptedException 
        return future.call();
    }

    /**
     * Immediately forks a process, 
     * returns a callable that will block until process is complete.
     * @param cl 
     *    The command line to execute
     * @param systemIn
     *    The input to read from, must be thread safe
     * @param systemOut
     *    A consumer that receives output, must be thread safe
     * @param systemErr
     *    A consumer that receives system error stream output, 
     *    must be thread safe
     * @param timeoutInSeconds
     *     Positive integer to specify timeout, 
     *     zero and negative integers for no timeout.
     * @return 
     *     A CommandLineCallable that provides the process return value, 
     *     see {@link Process#exitValue()}. "call" must be called on
     *     this to be sure the forked process has terminated, 
     *     no guarantees is made about any internal state 
     *     before after the completion of the call statements 
     *     <p>
     *     It is documented in which cases the return value throws exceptions 
     *     if {@link CommandLineCallable#call()} is executed. 
     * @throws CommandLineException 
     *     or CommandLineTimeOutException: 
     *     see {@link Commandline#execute()}. 
     */
    // used but invoked only with sytemIn==null and timeoutInSeconds==0 
    public static CommandLineCallable 
	executeCommandLineAsCallable(final Commandline cl, 
				     final InputStream systemIn,
				     final StreamConsumer systemOut,
				     final StreamConsumer systemErr,
				     final int timeoutInSeconds)
	throws CommandLineException {
        if (cl == null) {
            throw new IllegalArgumentException("Commandline cannot be null." );
        }
	// may throw CommandLineException (sole) 
        final Process proc = cl.execute();

        final StreamFeeder inputFeeder = systemIn != null 
	    ? new StreamFeeder(systemIn, proc.getOutputStream()) : null;

        final StreamPumper outputPumper = new StreamPumper
	    (proc.getInputStream(), systemOut);

        final StreamPumper errorPumper = new StreamPumper
	    (proc.getErrorStream(), systemErr);

        if ( inputFeeder != null )
        {
            inputFeeder.start();
        }

        outputPumper.start();

        errorPumper.start();

        final ProcessHook processHook = new ProcessHook(proc);

        ShutdownHookUtils.addShutDownHook( processHook );

	/**
	 * @throws CommandLineException
	 *    only explicitly (3 occurrences) 
	 *    <ul>
	 *    <li> Error inside systemOut parser 
	 *    <li> Error inside systemErr parser 
	 *    <li> Even CommandLineTimeoutException
	 *    Wrapping an {@link InterruptedException} on the process 
	 *    <code>proc</code> to be executed: 
	 *         <ul>
	 *         <li>timeoutInSeconds <= 0 and {@link Process#waitFor()}
	 *         <li>timeoutInSeconds > 0 and {@link Thread#sleep(int)}
	 *         <li>timeoutInSeconds > 0 and {@link #isAlive(Process)}
	 *         <li>timeoutInSeconds > 0 and 
	 * {@link #waitForAllPumpers(StreamFeeder, StreamPumper, StreamPumper)}
	 *         </ul>
	 *    </ul>
	 */
        return new CommandLineCallable() {
            public Integer call() throws CommandLineException {
		try {
                    int returnValue;
                    if (timeoutInSeconds <= 0) {
			// may throw InterruptedException 
			returnValue = proc.waitFor();
		    } else {
			long now = System.currentTimeMillis();
			long timeoutInMillis = 1000L * timeoutInSeconds;
			long finish = now + timeoutInMillis;
			while (isAlive(proc) && 
			       System.currentTimeMillis() < finish) {
			    // may throw InterruptedException 
			    Thread.sleep(10);
			}
			if (isAlive(proc)) {
			    // caught in catch block 
			    throw new InterruptedException
				("Process timeout out after " + 
				 timeoutInSeconds + " seconds" );
			}
			returnValue = proc.exitValue();
		    }
		    // may throw InterruptedException 
                    waitForAllPumpers(inputFeeder, outputPumper, errorPumper);

                    if (outputPumper.getException() != null) {
			throw new CommandLineException
			    ("Error inside systemOut parser", 
			     outputPumper.getException());
		    }

                    if (errorPumper.getException() != null) {
			throw new CommandLineException
			    ("Error inside systemErr parser", 
			     errorPumper.getException() );
		    }

                    return returnValue;
                } catch (InterruptedException ex) {
                    if (inputFeeder != null) {
                        inputFeeder.disable();
                    }
                    outputPumper.disable();
                    errorPumper .disable();
                    throw new CommandLineTimeOutException
			("Error while executing external command, " + 
			 "process killed.", ex);
                } finally {
                    ShutdownHookUtils.removeShutdownHook(processHook);
                    processHook.run();

                    if (inputFeeder != null) {
                        inputFeeder.close();
                    }

                    outputPumper.close();
                    errorPumper .close();
                }
            }
        };
    }

    /**
     *
     * @throws InterruptedException
     *    if one of the parameters waitUntilDone throws such an exception. 
     */
    private static void waitForAllPumpers(StreamFeeder inputFeeder, 
					  StreamPumper outputPumper,
					  StreamPumper errorPumper)
	throws InterruptedException
    {
        if ( inputFeeder != null )
        {
            inputFeeder.waitUntilDone();// may throw InterruptedException 
        }

        outputPumper.waitUntilDone();// may throw InterruptedException 
        errorPumper .waitUntilDone();// may throw InterruptedException 
    }

    /**
     * Gets the shell environment variables for this process. 
     * Note that the returned mapping from variable names to
     * values will always be case-sensitive regardless of the platform, 
     * i.e. <code>getSystemEnvVars().get("path")</code>
     * and <code>getSystemEnvVars().get("PATH")</code>
     *  will in general return different values. 
     * However, on platforms
     * with case-insensitive environment variables like Windows, 
     * all variable names will be normalized to upper case.
     *
     * @return 
     * The shell environment variables, 
     * can be empty but never <code>null</code>.
     * @see System#getenv() 
     * System.getenv() API, new in JDK 5.0, to get the same result
     *      <b>since 2.0.2 System#getenv() will be used 
     * if available in the current running jvm.</b>
     */
    public static Properties getSystemEnvVars()
    {
        return getSystemEnvVars( !Os.isFamily( Os.FAMILY_WINDOWS ) );
    }

    /**
     * Return the shell environment variables. 
     * If <code>caseSensitive == true</code>, then envar
     * keys will all be upper-case.
     *
     * @param caseSensitive 
     * Whether environment variable keys should be treated case-sensitively.
     * @return 
     * Properties object of (possibly modified) envar keys 
     * mapped to their values.
     * @see System#getenv() 
     * System.getenv() API, new in JDK 5.0, to get the same result
     *      <b>since 2.0.2 System#getenv() will be used 
     * if available in the current running jvm.</b>
     */
    public static Properties getSystemEnvVars( boolean caseSensitive )
    {
        Properties envVars = new Properties();
        Map<String, String> envs = System.getenv();
        for ( String key : envs.keySet() )
        {
            String value = envs.get( key );
            if ( !caseSensitive)
            {
                key = key.toUpperCase( Locale.ENGLISH );
            }
            envVars.put( key, value );
        }
        return envVars;
    }

    public static boolean isAlive( Process p )
    {
        if ( p == null )
        {
            return false;
        }

        try
        {
            p.exitValue();
            return false;
        }
        catch ( IllegalThreadStateException e )
        {
            return true;
        }
    }

    /**
     *
     * @throws CommandLineException
     *    If the quotes are not ballanced 
     */
    public static String[] translateCommandline(String toProcess)
	throws CommandLineException// thrown only once: explicitly 
    {
        if ( ( toProcess == null ) || ( toProcess.length() == 0 ) )
        {
            return new String[0];
        }

        // parse with a simple finite state machine

        final int normal = 0;
        final int inQuote = 1;
        final int inDoubleQuote = 2;
        int state = normal;
        StringTokenizer tok = new StringTokenizer( toProcess, "\"\' ", true );
        Vector<String> v = new Vector<String>();
        StringBuilder current = new StringBuilder();

        while ( tok.hasMoreTokens() )
        {
            String nextTok = tok.nextToken();
            switch ( state )
            {
                case inQuote:
                    if ( "\'".equals( nextTok ) )
                    {
                        state = normal;
                    }
                    else
                    {
                        current.append( nextTok );
                    }
                    break;
                case inDoubleQuote:
                    if ( "\"".equals( nextTok ) )
                    {
                        state = normal;
                    }
                    else
                    {
                        current.append( nextTok );
                    }
                    break;
                default:
                    if ( "\'".equals( nextTok ) )
                    {
                        state = inQuote;
                    }
                    else if ( "\"".equals( nextTok ) )
                    {
                        state = inDoubleQuote;
                    }
                    else if ( " ".equals( nextTok ) )
                    {
                        if ( current.length() != 0 )
                        {
                            v.addElement( current.toString() );
                            current.setLength( 0 );
                        }
                    }
                    else
                    {
                        current.append( nextTok );
                    }
                    break;
            }
        }

        if ( current.length() != 0 )
        {
            v.addElement( current.toString() );
        }

        if ( ( state == inQuote ) || ( state == inDoubleQuote ) )
        {
            throw new CommandLineException
		("Unbalanced quotes in " + toProcess);
        }

        String[] args = new String[v.size()];
        v.copyInto( args );
        return args;
    }

    /**
     * <p>Put quotes around the given String if necessary.</p>
     * <p>If the argument doesn't include spaces or quotes, return it
     * as is. If it contains double quotes, use single quotes - else
     * surround the argument by double quotes.</p>
     *
     * @throws CommandLineException
     *    if the argument contains both, single and double quotes.
     * @deprecated 
     * Use 
     * {@link StringUtils#quoteAndEscape(String, char, char[], char[], char, boolean)},
     * {@link StringUtils#quoteAndEscape(String, char, char[], char, boolean)} 
     * or
     * {@link StringUtils#quoteAndEscape(String, char)} instead.
     */
    public static String quote( String argument )
        throws CommandLineException
    {
	// may throw CommandLineException 
        return quote(argument, false, false, true);
    }

    /**
     * <p>Put quotes around the given String if necessary.</p>
     * <p>If the argument doesn't include spaces or quotes, return it
     * as is. If it contains double quotes, use single quotes - else
     * surround the argument by double quotes.</p>
     *
     * @throws CommandLineException
     *    if the argument contains both, single and double quotes.
     * @deprecated Use 
     * {@link StringUtils#quoteAndEscape(String, char, char[], char[], char, boolean)},
     * {@link StringUtils#quoteAndEscape(String, char, char[], char, boolean)} 
     * or
     * {@link StringUtils#quoteAndEscape(String, char)} instead.
     */
    public static String quote( String argument, boolean wrapExistingQuotes )
        throws CommandLineException
    {
	// may throw CommandLineException 
        return quote(argument, false, false, wrapExistingQuotes);
    }

    /**
     * @deprecated Use 
     * {@link StringUtils#quoteAndEscape(String, char, char[], char[], char, boolean)},
     * {@link StringUtils#quoteAndEscape(String, char, char[], char, boolean)} 
     * or
     * {@link StringUtils#quoteAndEscape(String, char)} instead.
     */
    @SuppressWarnings( { "JavaDoc" } )
    public static String quote(String argument, 
			       boolean escapeSingleQuotes, 
			       boolean escapeDoubleQuotes,
			       boolean wrapExistingQuotes)
	throws CommandLineException// thrown once only: explicitly 
    {
        if ( argument.contains( "\"" ) )
        {
            if ( argument.contains( "\'" ) )
            {
                throw new CommandLineException
		    ("Can't handle single and double quotes in same argument");
            }
            else
            {
                if ( escapeSingleQuotes )
                {
                    return "\\\'" + argument + "\\\'";
                }
                else if ( wrapExistingQuotes )
                {
                    return '\'' + argument + '\'';
                }
            }
        }
        else if ( argument.contains( "\'" ) )
        {
            if ( escapeDoubleQuotes )
            {
                return "\\\"" + argument + "\\\"";
            }
            else if ( wrapExistingQuotes )
            {
                return '\"' + argument + '\"';
            }
        }
        else if ( argument.contains( " " ) )
        {
            if ( escapeDoubleQuotes )
            {
                return "\\\"" + argument + "\\\"";
            }
            else
            {
                return '\"' + argument + '\"';
            }
        }

        return argument;
    }

    public static String toString( String[] line )
    {
        // empty path return empty string
        if ( ( line == null ) || ( line.length == 0 ) )
        {
            return "";
        }

        // path containing one or more elements
        final StringBuilder result = new StringBuilder();
        for ( int i = 0; i < line.length; i++ )
        {
            if ( i > 0 )
            {
                result.append( ' ' );
            }
            try
            {
                result.append( StringUtils.quoteAndEscape( line[i], '\"' ) );
            }
            catch ( Exception e )
            {
                System.err.println("Error quoting argument: " + e.getMessage());
            }
        }
        return result.toString();
    }

}
