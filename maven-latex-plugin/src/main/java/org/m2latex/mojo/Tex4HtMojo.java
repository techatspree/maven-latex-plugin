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
import java.util.Iterator;
import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.plexus.util.cli.CommandLineException;

/**
 * Build HTML documents from LaTeX sources.
 * 
 * @goal tex4ht
 * @phase site
 */
public class Tex4HtMojo
    extends AbstractLatexMojo
{
    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        initialize();
        log.debug( "Settings: " + settings.toString() );
        log.info( "settings.getOutputDirectory(): " + settings.getOutputDirectory() );

        File texDirectory = settings.getTexDirectory();

        if ( !texDirectory.exists() )
        {
            log.info( "No tex directory - skipping LaTeX processing" );
            return;
        }

        try
        {
            fileUtils.copyLatexSrcToTempDir( texDirectory, settings.getTempDirectory() );
            List latexMainFiles = fileUtils.getLatexMainDocuments( settings.getTempDirectory() );
            for ( Iterator iterator = latexMainFiles.iterator(); iterator.hasNext(); )
            {
                File texFile = (File) iterator.next();
                latexProcessor.processTex4ht( texFile );
                // TODO move to Settings
                File tex4htOutputDir = new File( settings.getTempDirectory(), TexFileUtils.TEX4HT_OUTPUT_DIR );
                fileUtils.copyTex4htOutputToOutputFolder( texFile, settings.getTempDirectory(), tex4htOutputDir,
                                                          settings.getOutputDirectory() );
            }
        }
        catch ( CommandLineException e )
        {
            throw new MojoExecutionException( "Error executing command", e );
        }
        finally
        {
            if ( settings.isCleanUp() )
            {
                cleanUp();
            }
        }
    }
}
