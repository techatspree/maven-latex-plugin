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
 * Build PDF or DVI documents from LaTeX sources.
 * 
 * @goal latex
 * @phase site
 */
public class LatexMojo
    extends AbstractLatexMojo
{
    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        initialize();

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
                latexProcessor.processLatex( texFile );
                fileUtils.copyLatexOutputToOutputFolder( texFile, settings.getTempDirectory(), settings
                    .getOutputDirectory() );
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
