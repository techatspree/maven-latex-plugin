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
import java.io.FileFilter;

import java.util.Collection;

interface TexFileUtils
{
    FileFilter getFileFilter(final File texFile, final String[] filesPatterns);

    void copyOutputToTargetFolder(FileFilter fileFilter, 
				  File texFile, 
				  File targetDir)
        throws BuildExecutionException, BuildFailureException;

    void copyLatexSrcToTempDir(File texDirectory, File tempDirectory)
        throws BuildExecutionException;

    String getFileNameWithoutSuffix(File texFile);

    File replaceSuffix(File file, String suffix);

    Collection<File> getXFigDocuments(File directory);
    Collection<File> getGnuplotDocuments(File directory);
    Collection<File> getMetapostDocuments(File directory);

    /*
     * @param tempDir
     * 
     * @return
     *    A List of java.io.File objects 
     *    denoting the LaTeX documents to process.
     * @throws BuildExecutionException
     */
    Collection<File> getLatexMainDocuments(File tempDir)
        throws BuildExecutionException;

    boolean matchInFile(File file, String pattern) 
	throws BuildExecutionException;

    File getTargetDirectory(File sourceFile,
			    File sourceBaseDir,
			    File targetBaseDir)
	throws BuildExecutionException, BuildFailureException;

    public void cleanUp();

}
