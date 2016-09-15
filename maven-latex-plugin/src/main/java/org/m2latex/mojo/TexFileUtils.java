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

package org.m2latex.mojo;

import java.io.File;
import java.io.FileFilter;

import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

public interface TexFileUtils
{

    FileFilter getLatexOutputFileFilter(File texFile);
    FileFilter getTex4htOutputFileFilter(File texFile);
    FileFilter getLatex2rtfOutputFileFilter(File texFile);

    void copyOutputToTargetFolder(FileFilter fileFilter, 
				  File texFile, 
				  File targetDir)
        throws MojoExecutionException, MojoFailureException;

    void copyLatexSrcToTempDir( File texDirectory, File tempDirectory )
        throws MojoExecutionException;

    String getFileNameWithoutSuffix( File texFile );

    File replaceSuffix( File file, String suffix );

    /*
     * @param tempDir
     * 
     * @return A List of java.io.File objects denoting the LaTeX documents to process.
     * 
     * @throws MojoExecutionException
     */
    List<File> getLatexMainDocuments( File directory )
        throws MojoExecutionException;

     boolean matchInLogFile( File logFile, String pattern )
        throws MojoExecutionException;

    File createTex4htOutputDir( File tempDir ) throws MojoExecutionException;

    File getTargetDirectory(File sourceFile,
			    File sourceBaseDir,
			    File targetBaseDir)
	throws MojoExecutionException, MojoFailureException;

}
