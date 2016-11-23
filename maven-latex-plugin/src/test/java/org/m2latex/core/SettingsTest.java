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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class SettingsTest {

    @Test public void testSettings() {
        Settings settings = new Settings();

	// invoked from within maven in base directory with pom.xml 
	File baseDirectory = new File(System.getProperty("user.dir"));
	settings.setBaseDirectory(baseDirectory);
	// mvn project has default file structure 
	File targetDirectory = new File(baseDirectory, "target");
	settings.setTargetDirectory(targetDirectory);
	File targetSiteDirectory = new File(targetDirectory, "site");
	settings.setTargetSiteDirectory(targetSiteDirectory);

	// test getTexSrcDirectoryFile() and setTexSrcDirectory(...) 
	assertEquals(new File(baseDirectory, "src/site/tex"),
		     settings.getTexSrcDirectoryFile());
	settings.setTexSrcDirectory("site");
	settings.setBaseDirectory(targetDirectory);
	assertEquals(targetSiteDirectory,
		     settings.getTexSrcDirectoryFile());

	// FIXME: Further tests required. 

    }

    public static void main(String[] args) throws Exception {
	Class cls = SettingsTest.class
	    .getMethod("testSettings")
	    .getAnnotation(Test.class).getClass();
	//System.out.println("cls: "+);
	
    }

}
