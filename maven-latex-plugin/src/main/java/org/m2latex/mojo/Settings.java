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

import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.util.Arrays;

public class Settings
{
    /**
     * @parameter
     * @readonly
     */
    private File baseDirectory;

    /**
     * @parameter
     * @readonly
     */
    private File targetSiteDirectory;

    /**
     * @parameter
     * @readonly
     */
    private File targetDirectory;

    /**
     * @parameter
     * @readonly
     */
    private String tempDirectory = null;

    /**
     * @parameter
     */
    private File texDirectory = null;

    /**
     * @parameter
     */
    private String outputDirectory = null;

    /**
     * @parameter
     */
    private File texPath = null;

    /**
     * @parameter
     */
    private String texCommand = "pdflatex";

    /**
     * @parameter
     */
    private String tex4htCommand = "htlatex";

    /**
     * @parameter
     */
    private String bibtexCommand = "bibtex";

    /**
     * @parameter
     */
    private boolean cleanUp = true;

    /**
     * @parameter
     */
    private String[] texCommandArgs = new String[]{"-interaction=nonstopmode", "--src-specials"};

    /**
     * TODO move to different fields; take latex args from texCommandArgs
     *
     * @parameter
     */
    private String[] tex4htCommandArgs = new String[]{"html,2", "", "", "-interaction=nonstopmode --src-specials"};

    private File outputDirectoryFile = null;

    private File tempDirectoryFile = null;

    public File getBaseDirectory()
    {
        return baseDirectory;
    }

    public String getBibtexCommand()
    {
        return bibtexCommand;
    }

    public File getOutputDirectory()
    {
        if ( outputDirectoryFile == null )
        {
            if ( StringUtils.isEmpty( outputDirectory ) )
            {
                outputDirectoryFile = targetSiteDirectory;
            }
            else
            {
                outputDirectoryFile = new File( targetSiteDirectory, outputDirectory );
            }
        }
        return outputDirectoryFile;
    }

    public File getTargetDirectory()
    {
        return targetDirectory;
    }

    public File getTargetSiteDirectory()
    {
        return targetSiteDirectory;
    }

    public File getTempDirectory()
    {
        if (tempDirectoryFile == null)
        {
            String dirName = StringUtils.isBlank( tempDirectory ) ? "m2latex" : tempDirectory;
            tempDirectoryFile = new File( targetDirectory, dirName );
        }
        return tempDirectoryFile;
    }

    public String getTex4htCommand()
    {
        return tex4htCommand;
    }

    public String[] getTex4htCommandArgs()
    {
        return tex4htCommandArgs;
    }

    public String getTexCommand()
    {
        return texCommand;
    }

    public String[] getTexCommandArgs()
    {
        return texCommandArgs;
    }

    public File getTexDirectory()
    {
        if ( texDirectory == null )
        {
            texDirectory = new File( new File( new File( baseDirectory, "src" ), "site" ), "tex" );
        }
        return texDirectory;
    }

    public File getTexPath()
    {
        return texPath;
    }

    public boolean isCleanUp()
    {
        return cleanUp;
    }

    public Settings setBaseDirectory( File baseDirectory )
    {
        this.baseDirectory = baseDirectory;
        return this;
    }

    public Settings setBibtexCommand( String bibtexCommand )
    {
        this.bibtexCommand = bibtexCommand;
        return this;
    }

    public Settings setCleanUp( boolean cleanUp )
    {
        this.cleanUp = cleanUp;
        return this;
    }

    public Settings setOutputDirectory( String outputDirectory )
    {
        this.outputDirectory = outputDirectory;
        return this;
    }

    public Settings setTargetDirectory( File targetDirectory )
    {
        this.targetDirectory = targetDirectory;
        return this;
    }

    public Settings setTargetSiteDirectory( File targetSiteDirectory )
    {
        this.targetSiteDirectory = targetSiteDirectory;
        return this;
    }

    public Settings setTempDirectory( String tempDirectory )
    {
        this.tempDirectory = tempDirectory;
        return this;
    }

    /**
     * @parameter
     */
    public Settings setTex4htCommand( String tex4htCommand )
    {
        this.tex4htCommand = tex4htCommand;
        return this;
    }

    public Settings setTex4htCommandArgs( String[] tex4htCommandArgs )
    {
        this.tex4htCommandArgs = tex4htCommandArgs;
        return this;
    }

    public Settings setTexCommand( String texCommand )
    {
        this.texCommand = texCommand;
        return this;
    }

    public Settings setTexCommandArgs( String[] args )
    {
        this.texCommandArgs = args;
        return this;
    }

    public Settings setTexDirectory( File texDirectory )
    {
        this.texDirectory = texDirectory;
        return this;
    }

    public Settings setTexPath( File texPath )
    {
        this.texPath = texPath;
        return this;
    }

    public String toString()
    {
        StringBuffer sb = new StringBuffer( super.toString() );
        sb.append( '[' ).append( "tempDirectory=" ).append( tempDirectory );
        sb.append( ",texPath=" ).append( texPath );
        sb.append( ",texCommand=" ).append( texCommand );
        sb.append( ",bibtexCommand=" ).append( bibtexCommand );
        sb.append( ",baseDirectory=" ).append( baseDirectory );
        sb.append( ",targetSiteDirectory=" ).append( targetSiteDirectory );
        sb.append( ",texDirectory=" ).append( texDirectory );
        sb.append( ",texCommandArgs=" ).append( Arrays.asList( texCommandArgs ) ).append( ']' );
        return sb.toString();
    }
}
