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