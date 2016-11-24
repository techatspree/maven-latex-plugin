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

import java.util.Collection;

import java.io.File;
import java.io.FileFilter;

/**
 * Required for mock tests only. 
 */

interface TexFileUtils {

    Collection<File> getFilesRec(File dir) throws BuildExecutionException;

    File getTargetDirectory(File sourceFile,
			    File sourceBaseDir,
			    File targetBaseDir)
	throws BuildExecutionException, BuildFailureException;

    FileFilter getFileFilter(File texFile, String pattern);

    void copyOutputToTargetFolder(File texFile, 
				  FileFilter fileFilter, 
				  File targetDir)
        throws BuildExecutionException, BuildFailureException;

    String getFileNameWithoutSuffix(File file);

    String getSuffix(File file);

    File replaceSuffix(File file, String suffix);

    Collection<File> getFilesWithSuffix(Collection<File> files, String suffix);

    boolean matchInFile(File file, String pattern) 
	throws BuildExecutionException;

    void deleteX(File texFile, FileFilter filter) 
	throws BuildExecutionException;

    void cleanUp(Collection<File> sourceFiles, File texDir)
	throws BuildExecutionException;

}
