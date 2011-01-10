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
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.logging.Log;

public abstract class AbstractLatexMojo
    extends AbstractMojo
{

    /**
     * Location of the maven base dir.
     * 
     * @parameter expression="${basedir}"
     * @readonly
     */
    protected File baseDirectory;

    /**
     * Location of the target dir.
     * 
     * @parameter expression="${project.build.directory}"
     * @readonly
     */
    protected File targetDirectory;

    /**
     * Location of the target/site dir.
     * 
     * @parameter expression="${project.reporting.outputDirectory}"
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
        getLog().debug( "Deleting temporary directory " + settings.getTempDirectory().getPath() );
        try
        {
            FileUtils.deleteDirectory( settings.getTempDirectory() );
        }
        catch ( IOException e )
        {
            getLog().warn( "The temporary directory '" + settings.getTempDirectory() + "' could be deleted.", e );
        }
    }

    protected void initialize()
    {
        if ( settings == null )
        {
            // no configuration is defined in pom, i.e. object is not created by Maven
            settings = new Settings();
        }
        settings.setBaseDirectory( baseDirectory ).setTargetSiteDirectory( targetSiteDirectory )
            .setTargetDirectory( targetDirectory );

        log = getLog();
        fileUtils = new TexFileUtilsImpl( log );
        latexProcessor = new LatexProcessor( settings, new CommandExecutorImpl( log ), log, fileUtils );
    }
}