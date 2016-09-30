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
import java.io.FileFilter;
import java.io.IOException;

import java.util.Collection;
import java.util.ArrayList;

import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;

import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;

public class TexFileUtilsImpl implements TexFileUtils {

    /**
     * The pattern which identifies a latex main file. 
     */
   private final static String MAIN_TEX_PATTERN = 
	".*\\\\begin\\s*\\{document\\}.*";

    private final Log log;

    private final Settings settings;

    public TexFileUtilsImpl(Log log, Settings settings) {
        this.log = log;
	this.settings = settings;
    }

    /**
     * Returns a wildcard file filter containing all files 
     * replacing teh suffix of <code>texFile</code> 
     * with the pattern given by <code>filesPatterns</code>. 
     *
     * @param texFile
     *    the tex-file to derive filenames from. 
     * @param filesPatterns
     *    patterns of the form <code>.&lt;suffix&gt;</code> 
     *    or <code>*.&lt;suffix&gt;</code> 
     * @return
     *    A file filter given by the wildcard 
     *    replacing the suffix of <code>texFile</code> 
     *    with the pattern given by <code>filesPatterns</code>. 
     */
    public FileFilter getFileFilter(final File texFile, 
				    final String[] filesPatterns)
    {
        String texFilePrefix = getFileNameWithoutSuffix( texFile );
        String[] fileNames = new String[filesPatterns.length];
        for ( int i = 0; i < filesPatterns.length; i++ )
        {
            fileNames[i] = texFilePrefix + filesPatterns[i];
        }

	return new WildcardFileFilter(fileNames);
    }

    /**
     * Invoked only by AbstractLatexMojo#execute()
     * <code></code>
     * @BuildExecutionException
     *    wraps IOException when copying. 
     */
    public void copyOutputToTargetFolder(FileFilter fileFilter, 
					 File texFile, 
					 File targetDir )
	throws BuildExecutionException {

	File texFileDir = texFile.getParentFile();
        File[] outputFiles = texFileDir.listFiles();

        if (outputFiles == null) {
	    log.error("File " + texFileDir + 
		      " is not a directory as expected! " );
	}

        if (outputFiles.length == 0) {
            log.warn( "LaTeX file " + texFile + 
		      " did not generate any output in " + 
		      texFileDir + "!" );
        }

	File file;
	for (int idx = 0; idx < outputFiles.length; idx++) {
	    file = outputFiles[idx];
	    if (fileFilter.accept(file)) {
		log.info("Copying " + file.getName() + " to " + targetDir);
		try {
		    FileUtils.copyFileToDirectory(file, targetDir);
		} catch ( IOException e ) {
		    throw new BuildExecutionException
			("Error copying file " + file + 
			 " to directory " + targetDir,
			 e);
		}
	    }
	}
    }

    /**
     * Returns the directory containing <code>sourceFile</code> 
     * with the prefix <code>sourceBaseDir</code> 
     * replaced by <code>targetBaseDir</code>. 
     * E.g. <code>sourceFile=/tmp/adir/afile</code>, 
     * <code>sourceBaseDir=/tmp</code>, <code>targetBaseDir=/home</code> 
     * returns <code>/home/adir/</code>. 
     *
     * @param sourceFile
     *    the source file the parent directory of which 
     *    shall be converted to the target. 
     * @param sourceBaseDir
     *    the base directory of the source. 
     *    Immediately or not, 
     *    <code>sourceFile</code> shall be in <code>sourceBaseDir</code>. 
     * @param targetBaseDir
     *    the base directory of the target. 
     * @return
     *    the directory below <code>targetBaseDir</code>
     *    which corresponds to the parent directory of <code>sourceFile</code> 
     *    which is below <code>sourceBaseDir</code>. 
     * @throws BuildExecutionException
     *    If the canonical path of <code>sourceFile</code> 
     *    or of <code>sourceBaseDir</code> cannot be determined. 
     * @throws MojoFailureException
     *    if <code>sourceFile</code> is not below <code>sourceBaseDir</code>. 
     */
    public File getTargetDirectory(File sourceFile,
				   File sourceBaseDir,
				   File targetBaseDir)
    throws BuildExecutionException, MojoFailureException {
        String sourceParentPath;
        String sourceBasePath;
        try
        {
	    // getCanonicalPath may throw IOException 
	    sourceParentPath = sourceFile.getParentFile().getCanonicalPath();
	    // getCanonicalPath may throw IOException 
            sourceBasePath = sourceBaseDir.getCanonicalPath();
         }
        catch ( IOException e )
        {
            throw new BuildExecutionException
		("Error getting canonical path", e);
        }

        if (!sourceParentPath.startsWith(sourceBasePath)) {
            throw new MojoFailureException
		( "File " + sourceFile + 
		  " is expected to be somewhere under directory "
		  + sourceBasePath + ". ");
        }

	return new File(targetBaseDir, 
			sourceParentPath.substring(sourceBasePath.length()));
    }

    /**
     * Copy <code>texDirectory</code> to <code>tempDirectory</code>, 
     * the latter deleting before copy if it exists. 
     *
     * @throws BuildExecutionException
     *    if either deleting or copying fails. 
     * @see TexFileUtils#copyLatexSrcToTempDir(File, File)
     */
    // used in AbstractLatexMojo.execute() only. 
    public void copyLatexSrcToTempDir(File texDirectory, File tempDirectory)
        throws BuildExecutionException
    {
        try {
            if ( tempDirectory.exists() )
            {
                log.info("Deleting existing directory '" 
			 + tempDirectory.getPath() + "'. ");
		// may throw IOException 
                FileUtils.deleteDirectory( tempDirectory );
            }
	} catch (IOException e) {
            throw new BuildExecutionException
		("Failure deleting the temporary directory '" + 
		 tempDirectory.getPath() + "'. ", e );
        }

	log.debug("Copying TeX source directory '" + texDirectory.getPath()
		  + "' to temporary directory '" + tempDirectory + "'. " );
	try {
 	    // may throw IOException 
	    FileUtils.copyDirectory( texDirectory, tempDirectory );
	} catch (IOException e) {
            throw new BuildExecutionException
		("Failure copying the TeX directory '" + texDirectory.getPath()
		 + "' to a temporary directory '" + tempDirectory.getPath() 
		 + "'. ", e );
        }
    }

    /**
     * Return the name of the given file without the suffix. 
     * 
     * @see TexFileUtils#getFileNameWithoutSuffix(File)
     */
    public String getFileNameWithoutSuffix( File texFile )
    {
        String nameTexFile = texFile.getName();
        String namePrefixTexFile = nameTexFile
	    .substring(0, nameTexFile.lastIndexOf( "." ));
        return namePrefixTexFile;
    }

    /**
     * Return a collection of xfig-files in the given directory. 
     * 
     * @see TexFileUtils#getFileNameWithoutSuffix(File)
     */
    public Collection<File> getXFigDocuments( File directory ) {
	return FileUtils
	    .listFiles(directory,
		       FileFilterUtils.suffixFileFilter( ".fig" ),
		       TrueFileFilter.INSTANCE );
    }

    /*
     * (non-Javadoc)
     * 
     * @see TexFileUtils#getLatexMainDocuments(File)
     */
    // used in AbstractLatexMojo.execute() only. 
    public Collection<File> getLatexMainDocuments( File directory )
        throws BuildExecutionException {

        Collection<File> mainFiles = new ArrayList<File>();

        Collection<File> texFiles = FileUtils
	    .listFiles(directory,
		       FileFilterUtils.suffixFileFilter(".tex"),
		       TrueFileFilter.INSTANCE );
	for (File file : texFiles) {
	    if ( isTexMainFile( file ) )
		{
		    mainFiles.add( file );
		}
        }
        return mainFiles;
    }
 
   private boolean isTexMainFile(File file) throws BuildExecutionException {
 
        try
        {
            return fileContainsPattern(file, MAIN_TEX_PATTERN);

        }
        catch (FileNotFoundException e)
        {
            throw new BuildExecutionException("The TeX file '" + file.getPath()
                + "' was removed while running this goal. ", e );
        }
        catch (IOException e)
        {
            throw new BuildExecutionException
		("Problems reading the file '" + file.getPath()
                + "' while checking if it is a TeX main file. ", e);
        }
    }

    // logFile may be .log or .blg or something 
    /**
     *
     * @throws BuildExecutionException
     *    if the file <code>logFile</code> does not exist 
     *    or cannot be read. 
     */
    // used in LatexProcessor only: 
    // only in methods processLatex2pdf, runLatex2html, runLatex2odt, 
    // needAnotherLatexRun, needBibtexRun, 
    // runMakeindex, runBibtex, runLatex
    public boolean matchInLogFile(File logFile, String pattern)
        throws BuildExecutionException
    {
        if (!logFile.exists())
	    {
		throw new BuildExecutionException
		    ("File " + logFile.getPath() 
		     + " does not exist after running LaTeX. ");
	    }
       
	try {
	    return fileContainsPattern( logFile, pattern );
	} catch (FileNotFoundException e) {
	    throw new BuildExecutionException
		("Log file " + logFile.getPath() + " not found. ",
		 e);
	} catch (IOException e) {
	    throw new BuildExecutionException
		("Error reading log file " + logFile.getPath() + ". ", e);
	}
    }

    /**
     * Return whether <code>file</code> contains <code>regex</code>. 
     *
     * @throws FileNotFoundException
     *    if <code>file</code> does not exist. 
     * @throws IOException
     *    if <code>file</code> could not be read. 
     */
    // used by isTexMainFile(File file) 
    // and by matchInLogFile(File logFile, String pattern)
    private boolean fileContainsPattern(File file, String regex)
	throws FileNotFoundException, IOException {

        Pattern pattern = Pattern.compile( regex );
	// may throw FileNotFoundException
	FileReader fileReader = new FileReader(file);
	BufferedReader bufferedReader = new BufferedReader(fileReader);
	try {
	    String line = bufferedReader.readLine();// may throw IOException
	    for (; 
		 line != null;
		 line = bufferedReader.readLine()) {// may throw IOException
                if (pattern.matcher( line ).find()) {
                   return true;
                }
            }
	   return false;
        } finally {
	    // Here, an IOException occurred 
	    try {
		bufferedReader.close();
	    } catch ( IOException e ) {
		log.warn("Cannot close the file '" + file.getPath() + "'.",
			 e);
	    }
        }
    }

    public File replaceSuffix( File file, String suffix )
    {
        return new File(file.getParentFile(),
			getFileNameWithoutSuffix( file ) + "." + suffix );
    }

 }
