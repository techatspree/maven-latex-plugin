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

    void copyLatexOutputToOutputFolder(FileFilter fileFilter, 
				       File texFile, 
				       File outputDirectory,
				       File targetDir)
        throws MojoExecutionException, MojoFailureException;

    void copyTex4htOutputToOutputFolder(FileFilter fileFilter, 
					File texFile, 
					File tex4htOutputDirectory,
					File targetDir )
        throws MojoFailureException, MojoExecutionException;

    void copyLatexSrcToTempDir( File texDirectory, File tempDirectory )
        throws MojoExecutionException;

    File getCorrespondingAuxFile( File texFile );

    File getCorrespondingDviFile( File texFile );

    File getCorrespondingLogFile( File texFile );

    File getCorrespondingPdfFile( File texFile );

    String getFileNameWithoutSuffix( File texFile );

    /*
     * @param tempDir
     * 
     * @return A List of java.io.File objects denoting the LaTeX documents to process.
     * 
     * @throws MojoExecutionException
     */
    List<File> getLatexMainDocuments( File directory )
        throws MojoExecutionException;

    boolean matchInCorrespondingLogFile( File texFile, String pattern )
        throws MojoExecutionException;

    File createTex4htOutputDir( File tempDir ) throws MojoExecutionException;

    File getTargetDirectory(File sourceFile,
			    File sourceBaseDir,
			    File targetBaseDir)
	throws MojoExecutionException, MojoFailureException;
 
}
