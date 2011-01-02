package org.m2latex.mojo;

import java.io.File;
import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

public interface TexFileUtils
{

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

}