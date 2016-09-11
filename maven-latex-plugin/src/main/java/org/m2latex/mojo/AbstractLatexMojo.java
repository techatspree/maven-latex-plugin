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

import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.logging.Log;

/**
 * Abstract base class for all late mojos. 
 */
public abstract class AbstractLatexMojo
    extends AbstractMojo
{

    /**
     * Location of the maven base dir.
     * 
     * @parameter property="basedir"
     * @readonly
     */
    protected File baseDirectory;

    /**
     * Location of the target dir.
     * 
     * @parameter property="project.build.directory"
     * @readonly
     */
    protected File targetDirectory;

    /**
     * Location of the target/site dir.
     * 
     * @parameter property="project.reporting.outputDirectory"
     * @readonly
     */
    protected File targetSiteDirectory;

    /**
     * The Settings.
     * 
     * @parameter
     */
    protected Settings settings;

    protected LatexProcessor latexProcessor;

    protected TexFileUtils fileUtils;

    protected Log log;

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
        if ( settings == null )
        {
            // no configuration is defined in pom, 
	    // i.e. object is not created by Maven
            settings = new Settings();
        }
        settings.setBaseDirectory( baseDirectory )
	    .setTargetSiteDirectory( targetSiteDirectory )
            .setTargetDirectory( targetDirectory );

        log = getLog();
        this.fileUtils = new TexFileUtilsImpl( log, settings );
        latexProcessor = new LatexProcessor( settings, new CommandExecutorImpl( log ), log, this.fileUtils );
    }
}
