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

// mport eu.simuline.m2latex.core.BuildExecutionException;
// import eu.simuline.m2latex.core.BuildFailureException;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
// import org.junit.Ignore;
import org.junit.Before;
import org.junit.After;

public class TexFileUtilsTest {
	private final static File WORKING_DIR =
			new File(System.getProperty("unitTestResourcesDir"));
	//private final static File KEEP_ME = 
	//new File(System.getProperty("keepMe"));

	// FIXME: occurs also in other testclasses: 
	// to be unified. 
	private static void cleanWorkingDir() {
		cleanDirRec(WORKING_DIR);
	}

	// does not work for hidden directories 
	private static void cleanDirRec(File dir) {
		assert dir.isDirectory() : "Expected directory. ";
		File[] files = dir.listFiles();
		assert files != null : "Directory is not readable. ";
		boolean proof;
		for (File file : files) {
			assert file.exists();
			if (file.isDirectory()) {
				assert !file.isHidden();
				cleanDirRec(file);
				assert file.listFiles().length == 0;
			}
			if (!file.isHidden()) {
				proof = file.delete();
				assert proof;
			}
		}
	}

	@Before
	public void setUp() throws IOException {
		cleanWorkingDir();
	}

	@After
	public void tearDown() throws IOException {
		cleanWorkingDir();
	}

	@Test
	public void testGetTargetDir() throws BuildFailureException {

		File expected = new File(WORKING_DIR, "dir2/subdir");
		TexFileUtils utils =
			new TexFileUtils(new MavenLogWrapper(this.getClass()));
		// may throw BuildFailureException 
		File actual =
				utils.getTargetDirectory(new File(WORKING_DIR, "dir1/subdir/file"),
						new File(WORKING_DIR, "dir1"), new File(WORKING_DIR, "dir2"));
		assertEquals(expected, actual);
	}
}
