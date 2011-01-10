/*
 * The akquinet maven-latex-plugin project
 *
 * Copyright (c) 2011 by akquinet tech@spree GmbH
 *
 * The maven-latex-plugin is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * The maven-latex-plugin is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with the maven-latex-plugin. If not, see <http://www.gnu.org/licenses/>.
 */

package org.m2latex.mojo;

import java.io.File;
import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

public interface TexFileUtils
{

    String TEX4HT_OUTPUT_DIR = "m2latex_tex4ht_out";

    void copyLatexOutputToOutputFolder( File texFile, File tempDirectory, File targetSiteDirectory )
        throws MojoExecutionException, MojoFailureException;

    void copyLatexSrcToTempDir( File texDirectory, File tempDirectory )
        throws MojoExecutionException;

    void copyTex4htOutputToOutputFolder( File texFile, File baseDirectory, File outputDirectory,
                                         File targetSiteDirectory )
        throws MojoFailureException, MojoExecutionException;

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
    List getLatexMainDocuments( File directory )
        throws MojoExecutionException;

    boolean matchInCorrespondingLogFile( File texFile, String pattern )
        throws MojoExecutionException;

    File createTex4htOutputDir( File tempDir ) throws MojoExecutionException;
}