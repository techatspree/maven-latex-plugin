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

import org.m2latex.mojo.MavenLogWrapper;

import org.m2latex.core.BuildExecutionException;
import org.m2latex.core.BuildFailureException;

import org.apache.maven.plugin.logging.SystemStreamLog;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.Ignore;
import org.junit.Before;
import org.junit.After;

public class TexFileUtilsTest {
    private final static File WORKING_DIR = 
	new File(System.getProperty("testResourcesDir"));

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


 
    @Test public void testGetTargetDir() throws BuildFailureException {

        File expected = new File(WORKING_DIR, "dir2/subdir");
	TexFileUtils utils = 
	    new TexFileUtils(new MavenLogWrapper(new SystemStreamLog()));
	// may throw BuildFailureException 
        File actual = utils
	    .getTargetDirectory(new File(WORKING_DIR, "dir1/subdir/file"),
				new File(WORKING_DIR, "dir1"),
				new File(WORKING_DIR, "dir2"));
        assertEquals(expected, actual);
    }
}
