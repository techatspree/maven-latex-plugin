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
import java.io.IOException;
import java.io.FileFilter;

import java.util.Collection;

import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;

/**
 * Abstract base class for all mojos. 
 */
public abstract class AbstractLatexMojo extends AbstractMojo {

    /**
     * The base directory of this maven project. 
     * Reinitializes {@link Settings#baseDirectory} via {@link #initialize()}. 
     * 
     * @parameter property="basedir"
     * @readonly
     */
    protected File baseDirectory;

    /**
     * The target directory of this maven project. 
     * Reinitializes {@link Settings#targetDirectory} 
     * via {@link #initialize()}. 
     * 
     * @parameter property="project.build.directory"
     * @readonly
     */
    protected File targetDirectory;

    /**
     * The target site directory of this maven project. 
     * Reinitializes {@link Settings#baseDirectory} via {@link #initialize()}. 
     * 
     * @parameter property="project.reporting.outputDirectory"
     * @readonly
     */
    protected File targetSiteDirectory;

    /**
     * The Settings. 
     * If not set in the pom prior to execution, 
     * is set in {@link #initialize()}. 
     * 
     * @parameter
     */
    protected Settings settings;

    // set by {@link #initialize()}. 
    protected LatexProcessor latexProcessor;

    // set by {@link #initialize()}. 
    protected TexFileUtils fileUtils;


    // depends on abstract methods processSource(File), 
    // getOutputDir() and getFileFilter(File)
    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        initialize();
        getLog().debug("Settings: " + this.settings.toString() );

        File texDirectory = this.settings.getTexSrcDirectoryFile();

        if ( !texDirectory.exists() ) {
            getLog().info( "No tex directory - skipping LaTeX processing" );
            return;
        }

	try {
	    File tempDir = this.settings.getTempDirectoryFile();
	    // copy sources to tempDir 
	    // may throw BuildExecutionException 
	    this.fileUtils.copyLatexSrcToTempDir(texDirectory, tempDir);

	    // process xfig files 
	    Collection<File> figFiles = this.fileUtils
		.getXFigDocuments(tempDir);
	    for (File figFile : figFiles) {
		getLog().info("Processing " + figFile + ". ");
		// may throw BuildExecutionException 
		this.latexProcessor.runFig2Dev(figFile);
	    }

	    // process latex main files 
	    // may throw BuildExecutionException 
	    Collection<File> latexMainFiles = this.fileUtils
		.getLatexMainDocuments(tempDir);
	    for (File texFile : latexMainFiles) {
		// may throw BuildExecutionException  
		processSource(texFile);
		// may throw BuildExecutionException, BuildFailureException 
		File targetDir = this.fileUtils.getTargetDirectory
		    (texFile, 
		     tempDir, 
		     this.settings.getOutputDirectoryFile());
		FileFilter fileFilter = this.fileUtils
		    .getFileFilter(texFile, getOutputFileSuffixes());
		// may throw BuildExecutionException, BuildFailureException
		this.fileUtils.copyOutputToTargetFolder(fileFilter,
							texFile,
							targetDir);
	    }
	} catch (BuildExecutionException e) {
	    throw new MojoExecutionException(e.getMessage(), e.getCause());
	} catch (BuildFailureException e) {
	    throw new MojoFailureException(e.getMessage(), e.getCause());
	} finally {
	    if ( this.settings.isCleanUp() ) {
                cleanUp();
            }
        }
    }

    /**
     * Processes the source file <code>texFile</code> 
     * according to the concrete Mojo. 
     */
    abstract void processSource(File texFile) 
	throws BuildExecutionException;

    /**
     * Returns the suffixes and wildcards of the output files. 
     * For example if creating pdf and postscript, 
     * this is just <code>.pdf, .ps</code> 
     * but if various html files are created, it is <code>*.html</code>, 
     * the asterisk representing a wildcard. 
     */
    abstract String[] getOutputFileSuffixes();

    protected void cleanUp()
    {
	File tempDir = this.settings.getTempDirectoryFile();
        getLog().debug( "Deleting temporary directory " + 
			tempDir.getPath() );
        try
        {
            FileUtils.deleteDirectory(tempDir);
        }
        catch ( IOException e )
        {
            getLog().warn( "The temporary directory '" + tempDir + 
			   "' could be deleted.", e );
        }
    }

    protected void initialize()
    {
	if ( this.settings == null )
	    {
		// Here, no configuration is defined in pom, 
		// i.e. object is not created by Maven
		this.settings = new Settings();
	    }
	this.settings.setBaseDirectory( this.baseDirectory );
	this.settings.setTargetSiteDirectory( this.targetSiteDirectory );
	this.settings.setTargetDirectory( this.targetDirectory );

        Log log = getLog();
        this.fileUtils = new TexFileUtilsImpl( log, this.settings );
        this.latexProcessor = new LatexProcessor(this.settings, 
						 new CommandExecutorImpl(log), 
						 log, 
						 this.fileUtils);
    }
}
