package org.m2latex.mojo;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

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
