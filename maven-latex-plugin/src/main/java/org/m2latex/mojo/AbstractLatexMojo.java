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

import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;

import org.codehaus.plexus.util.cli.CommandLineException;

/**
 * Abstract base class for all mojos. 
 */
public abstract class AbstractLatexMojo
    extends AbstractMojo
{

    /**
     * Location of the maven base dir.
     * Reinitializes {@link Settings#baseDirectory} via {@link #initialize()}. 
     * 
     * @parameter property="basedir"
     * @readonly
     */
    protected File baseDirectory;

    /**
     * Location of the target dir.
     * Reinitializes {@link Settings#targetDirectory} 
     * via {@link #initialize()}. 
     * 
     * @parameter property="project.build.directory"
     * @readonly
     */
    protected File targetDirectory;

    /**
     * Location of the target/site dir. 
     * Reinitializes {@link Settings#baseDirectory} via {@link #initialize()}. 
     * 
     * @parameter property="project.reporting.outputDirectory"
     * @readonly
     */
    protected File targetSiteDirectory;

    /**
     * The Settings. 
     * If not set prior to execution, is set in {@link #initialize()}. 
     * 
     * @parameter
     */
    protected Settings settings;

    // set by {@link #initialize()}. 
    protected LatexProcessor latexProcessor;

    // set by {@link #initialize()}. 
    protected TexFileUtils fileUtils;

    // set by {@link #initialize()}. 
    protected Log log;// really needed? what about getLog()? 


    // depends on abstract methods processSource(File), 
    // getOutputDir() and getFileFilter(File)
    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        initialize();
        log.debug( "Settings: " + settings.toString() );
        log.info( "settings.getOutputDirectory(): " + 
		  settings.getOutputDirectory() );

        File texDirectory = settings.getTexDirectory();

        if ( !texDirectory.exists() ) {
            log.info( "No tex directory - skipping LaTeX processing" );
            return;
        }

        try {
            fileUtils.copyLatexSrcToTempDir(texDirectory, 
					    settings.getTempDirectory());
            List<File> latexMainFiles = fileUtils
		.getLatexMainDocuments( settings.getTempDirectory() );
	    File outputDir = getOutputDir();
	    for (File texFile : latexMainFiles) {
		processSource(texFile);
		File targetDir = fileUtils.getTargetDirectory
		    (texFile, 
		     settings.getTempDirectory(), 
		     settings.getOutputDirectory());
		FileFilter fileFilter = getFileFilter(texFile);
                fileUtils.copyOutputToTargetFolder(fileFilter,
						   texFile,
						   outputDir,
						   targetDir);
            }
        } catch ( CommandLineException e ) {
            throw new MojoExecutionException( "Error executing command", e );
        } finally {
            if ( settings.isCleanUp() ) {
                cleanUp();
            }
        }
    }


    abstract void processSource(File texFile) 
	throws CommandLineException, MojoExecutionException;
    abstract File getOutputDir();
    abstract FileFilter getFileFilter(File texFile);

    protected void cleanUp()
    {
        getLog().debug( "Deleting temporary directory " + 
			settings.getTempDirectory().getPath() );
        try
        {
            FileUtils.deleteDirectory( settings.getTempDirectory() );
        }
        catch ( IOException e )
        {
            getLog().warn( "The temporary directory '" + 
			   settings.getTempDirectory() + 
			   "' could be deleted.", e );
        }
    }

    protected void initialize()
    {
        if ( this.settings == null )
        {
            // no configuration is defined in pom, 
	    // i.e. object is not created by Maven
            this.settings = new Settings();
        }
        this.settings.setBaseDirectory( this.baseDirectory )
	    .setTargetSiteDirectory( this.targetSiteDirectory )
            .setTargetDirectory( this.targetDirectory );

        this.log = getLog();
        this.fileUtils = new TexFileUtilsImpl( log, settings );
        this.latexProcessor = new LatexProcessor( settings, new CommandExecutorImpl( this.log ), this.log, this.fileUtils );
    }
}
