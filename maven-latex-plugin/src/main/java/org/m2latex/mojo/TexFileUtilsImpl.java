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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;

public class TexFileUtilsImpl
    implements TexFileUtils
{
    private static final String[] LATEX_OUTPUT_FILES = new String[] { "%n.pdf", "%n.dvi", "%n.ps" };

    private final Log log;

    public TexFileUtilsImpl( Log log )
    {
        this.log = log;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.m2latex.mojo.TexFileUtils#copyOutputToSiteFolder(java.io.File, java.io.File, java.io.File)
     */
    public void copyLatexOutputToOutputFolder( File texFile, File tempDirectory, File outputDirectory )
        throws MojoExecutionException, MojoFailureException
    {
        WildcardFileFilter fileFilter = new WildcardFileFilter( getFilesToCopy( texFile, LATEX_OUTPUT_FILES ) );
        copyLatexOutputToOutputFolder( texFile, tempDirectory, outputDirectory, fileFilter );
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.m2latex.mojo.TexFileUtils#copyOutputToSiteFolder(java.io.File, java.io.File, java.io.File)
     */
    public void copyTex4htOutputToOutputFolder( File texFile, File tempDirectory, File tex4htOutputDirectory,
                                                File outputDirectory )
        throws MojoExecutionException, MojoFailureException
    {
        File[] outputFiles = tex4htOutputDirectory.listFiles();

        if ( outputFiles == null || outputFiles.length == 0 )
        {
            log.warn( "LaTeX file " + texFile + " did not generate any output in " + tex4htOutputDirectory + "!" );
        }
        else
        {
            File targetDirectory = getTargetDirectory( texFile, tempDirectory, outputDirectory );
            copyFilesToDirectory( outputFiles, targetDirectory );
        }
    }

    private void copyFilesToDirectory( File[] files, File targetDirectory )
        throws MojoExecutionException
    {
        for ( int i = 0; i < files.length; i++ )
        {
            try
            {
                FileUtils.copyFileToDirectory( files[i], targetDirectory );
            }
            catch ( IOException e )
            {
                throw new MojoExecutionException(
                                                  "Error copying file " + files[i] + " to directory " + targetDirectory,
                                                  e );
            }
        }
    }

    private void copyLatexOutputToOutputFolder( File texFile, File tempDirectory, File outputDirectory,
                                                IOFileFilter fileFilter )
        throws MojoFailureException, MojoExecutionException
    {
        File targetDir = getTargetDirectory( texFile, tempDirectory, outputDirectory );
        try
        {
            Collection filesToCopy = FileUtils.listFiles( texFile.getParentFile(), fileFilter, null );
            for ( Iterator iterator = filesToCopy.iterator(); iterator.hasNext(); )
            {
                File file = (File) iterator.next();
                copyFileToDirectory( file, targetDir );

            }
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "File " + texFile + " could not be copied to the target directory: "
                + targetDir, e );
        }
    }

    /**
     * E.g. sourceFile /tmp/adir/afile, sourceBaseDir /tmp, targetBaseDir /home returns /home/adir/
     */
    File getTargetDirectory( File sourceFile, File sourceBaseDir, File targetBaseDir )
        throws MojoExecutionException, MojoFailureException
    {
        String filePath;
        String tempPath;
        try
        {
            filePath = sourceFile.getParentFile().getCanonicalPath();
            tempPath = sourceBaseDir.getCanonicalPath();
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "Error getting canonical path", e );
        }

        if ( !filePath.startsWith( tempPath ) )
        {
            throw new MojoFailureException( "File " + sourceFile
                + " is expected to be somewhere under the following directory: " + tempPath );
        }

        File targetDir = new File( targetBaseDir, filePath.substring( tempPath.length() ) );
        return targetDir;
    }

    private String[] getFilesToCopy( final File texFile, final String[] filesPatterns )
    {
        String texFilePrefix = getFileNameWithoutSuffix( texFile );
        String[] fileNames = new String[filesPatterns.length];
        for ( int i = 0; i < filesPatterns.length; i++ )
        {
            fileNames[i] = filesPatterns[i].replaceAll( "%n", texFilePrefix );
        }
        return fileNames;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.m2latex.mojo.TexFileUtils#copyLatexSrcToTempDir(java.io.File, java.io.File)
     */
    public void copyLatexSrcToTempDir( File texDirectory, File tempDirectory )
        throws MojoExecutionException
    {
        try
        {
            if ( tempDirectory.exists() )
            {
                log.info( "Deleting existing directory " + tempDirectory.getPath() );
                FileUtils.deleteDirectory( tempDirectory );
            }

            log.debug( "Copying TeX source directory (" + texDirectory.getPath() + ") to temporary directory ("
                + tempDirectory + ")" );
            FileUtils.copyDirectory( texDirectory, tempDirectory );
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "Failure copying the TeX directory (" + texDirectory.getPath()
                + ") to a temporary directory (" + tempDirectory.getPath() + ").", e );
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.m2latex.mojo.TexFileUtils#getCorrespondingAuxFile(java.io.File)
     */
    public File getCorrespondingAuxFile( File texFile )
    {
        return getFileWithDifferentSuffix( texFile, "aux" );
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.m2latex.mojo.TexFileUtils#getCorrespondingDviFile(java.io.File)
     */
    public File getCorrespondingDviFile( File texFile )
    {
        return getFileWithDifferentSuffix( texFile, "dvi" );
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.m2latex.mojo.TexFileUtils#getCorrespondingLogFile(java.io.File)
     */
    public File getCorrespondingLogFile( File texFile )
    {
        return getFileWithDifferentSuffix( texFile, "log" );
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.m2latex.mojo.TexFileUtils#getCorrespondingPdfFile(java.io.File)
     */
    public File getCorrespondingPdfFile( File texFile )
    {
        return getFileWithDifferentSuffix( texFile, "pdf" );
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.m2latex.mojo.TexFileUtils#getFileNameWithoutSuffix(java.io.File)
     */
    public String getFileNameWithoutSuffix( File texFile )
    {
        String nameTexFile = texFile.getName();
        String namePrefixTexFile = nameTexFile.substring( 0, nameTexFile.lastIndexOf( "." ) );
        return namePrefixTexFile;
    }

    /*
     * 
     * @param tempDir
     * 
     * @return A List of java.io.File objects denoting the LaTeX documents to process.
     * 
     * @throws MojoExecutionException
     */
    /*
     * (non-Javadoc)
     * 
     * @see org.m2latex.mojo.TexFileUtils#getLatexMainDocuments(java.io.File)
     */
    public List getLatexMainDocuments( File directory )
        throws MojoExecutionException
    {
        ArrayList mainFiles = new ArrayList();

        Collection texFiles = FileUtils.listFiles( directory, FileFilterUtils.suffixFileFilter( ".tex" ),
                                                   TrueFileFilter.INSTANCE );
        for ( Iterator iterator = texFiles.iterator(); iterator.hasNext(); )
        {
            File file = (File) iterator.next();
            if ( isTexMainFile( file ) )
            {
                mainFiles.add( file );
            }
        }
        return mainFiles;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.m2latex.mojo.TexFileUtils#searchInLogFile(java.io.File, java.lang.String)
     */
    public boolean matchInCorrespondingLogFile( File texFile, String pattern )
        throws MojoExecutionException
    {
        File logFile = getCorrespondingLogFile( texFile );
        if ( logFile.exists() )
        {
            try
            {
                return fileContainsPattern( logFile, pattern );
            }
            catch ( FileNotFoundException e )
            {
                throw new MojoExecutionException( "File " + logFile.getPath() + " does not exist after running LaTeX.",
                                                  e );
            }
            catch ( IOException e )
            {
                throw new MojoExecutionException( "Error reading file " + logFile.getPath(), e );
            }
        }
        else
        {
            throw new MojoExecutionException( "File " + logFile.getPath() + " does not exist after running LaTeX." );
        }
    }

    public File createTex4htOutputDir( File tempDir ) throws MojoExecutionException
    {
        File tex4htOutdir = new File( tempDir, TEX4HT_OUTPUT_DIR );
        if ( tex4htOutdir.exists() )
        {
            try
            {
                FileUtils.cleanDirectory( tex4htOutdir );
            }
            catch ( IOException e )
            {
                throw new MojoExecutionException( "Could not clean TeX4ht output dir: " + tex4htOutdir, e );
            }
        }
        else
        {
            tex4htOutdir.mkdirs();
        }
        return tex4htOutdir;
    }

    private void copyFileToDirectory( File file, File targetDir )
        throws IOException
    {
        log.info( "Copying " + file.getName() + " to " + targetDir );
        FileUtils.copyFileToDirectory( file, targetDir );
    }

    private boolean fileContainsPattern( File file, String regex )
        throws FileNotFoundException, IOException
    {
        Pattern pattern = Pattern.compile( regex );
        BufferedReader bufferedReader = null;
        try
        {
            FileReader fileReader = new FileReader( file );
            bufferedReader = new BufferedReader( fileReader );
            for ( String line = bufferedReader.readLine(); line != null; line = bufferedReader.readLine() )
            {
                if ( pattern.matcher( line ).find() )
                {
                    return true;
                }
            }
            return false;
        }
        finally
        {
            if ( bufferedReader != null )
                try
                {
                    bufferedReader.close();
                }
                catch ( IOException e )
                {
                    log.warn( "Cannot close the file '" + file.getPath() + "'.", e );
                }
        }
    }

    private File getFileWithDifferentSuffix( File file, String suffix )
    {
        return new File( file.getParentFile(), getFileNameWithoutSuffix( file ) + "." + suffix );
    }

    private boolean isTexMainFile( File file )
        throws MojoExecutionException
    {
        String pattern = ".*\\\\begin\\s*\\{document\\}.*";

        try
        {
            return fileContainsPattern( file, pattern );

        }
        catch ( FileNotFoundException e )
        {
            throw new MojoExecutionException( "The TeX file '" + file.getPath()
                + "' was removed while running this goal", e );
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "Problems reading the file '" + file.getPath()
                + "' while checking if it is a TeX main file", e );
        }
    }
}
