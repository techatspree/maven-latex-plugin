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

import eu.simuline.m2latex.mojo.MavenLogWrapper;
import java.io.File;
import java.io.IOException;

//import junit.framework.TestCase;

import org.apache.maven.plugin.logging.SystemStreamLog;

//import static org.junit.Assert.assertEquals;

import org.junit.Test;
//import org.junit.Ignore;
import org.junit.Before;
import org.junit.After;

public class CommandExecutorTest {
    private final static File WORKING_DIR = 
	new File(System.getProperty("unitTestResourcesDir"));

    // FIXME: occurs also in other testclasses: 
    // to be unified. 
    private static void cleanWorkingDir() {
	assert WORKING_DIR.isDirectory() : "Expected directory. ";
	File[] files = WORKING_DIR.listFiles();
	assert files != null : "Working directory is not readable. ";
	for (File file : files) {
	    if (!file.isHidden()) {
		file.delete();
	    }
	}
    }

    @Before public void setUp() throws IOException {
	cleanWorkingDir();
    }

    @After public void tearDown() throws IOException {
	cleanWorkingDir();
    }

    @Test public void testExecute() throws BuildFailureException {
        CommandExecutor executor = 
	    new CommandExecutor(new MavenLogWrapper(new SystemStreamLog()));
	//String touchFile = "cmdLineExe.touch";
	//String output = executor.execute(WORKING_DIR, 
	//				 null, 
	///				 "touch", 
	//				 new String[] {touchFile},
	//				 new File(WORKING_DIR, touchFile)).output;
        //assertEquals(echoText, output.subSequence(0, echoText.length()));
    }

    // void mockExecute() throws BuildFailureException {
    // 	File res = new File("/tmp/exists.latexPlugin");
    // 	res.delete();
    //     this.executor.execute(new File("/tmp/"),
    // 			      null,
    // 			      "touch",
    // 			      new String[] {res.getName()},
    // 			      res);
    //     executorCtrl.setMatcher(MockControl.ARRAY_MATCHER);
    //     executorCtrl.setReturnValue(null);
    // }
}
