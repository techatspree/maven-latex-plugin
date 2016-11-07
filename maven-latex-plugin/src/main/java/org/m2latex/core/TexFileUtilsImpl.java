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

package org.m2latex.core;

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
import static org.apache.commons.io.filefilter.FileFilterUtils.suffixFileFilter;
import static org.apache.commons.io.filefilter.FileFilterUtils.prefixFileFilter;
import static org.apache.commons.io.filefilter.FileFilterUtils.notFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;

class TexFileUtilsImpl implements TexFileUtils {

    private final static String SUFFIX_TEX = ".tex";
    private final static String PREFIX_HIDDEN = ".";

    private final LogWrapper log;

    TexFileUtilsImpl(LogWrapper log) {
        this.log = log;
    }

    /**
     * Returns the collection of files 
     * in folder <code>dir</code> and subfolder. 
     *
     * @param dir
     *    a directory. 
     * @return
     *    the collection of files in folder <code>dir</code> and subfolder. 
     * @throws BuildExecutionException
     *    if <code>dir</code> is not a folder or not readable. 
     */
    public Collection<File> getFilesRec(File dir) 
	throws BuildExecutionException {

        Collection<File> res = FileUtils.listFiles(dir,
						   TrueFileFilter.INSTANCE,
						   TrueFileFilter.INSTANCE);
	if (res == null) {
	    throw new BuildExecutionException
		("File " + dir + " is not readable or no directory. ");
	}
	return res;
    }

    /*
     * (non-Javadoc)
     * 
     * @see TexFileUtils#getLatexMainDocuments(File, String)
     */
    // used in only 
    // LatexProcessor#create() and 
    // in LatexProcessor#clearFromLatexMain(File)
    public Collection<File> getLatexMainDocuments(File directory, 
						  String patternLatexMainFile)
	throws BuildExecutionException {

        Collection<File> mainFiles = new ArrayList<File>();
	//IOFileFilter ff = FileFilterUtils.suffixFileFilter(SUFFIX_TEX);
        Collection<File> latexFiles = FileUtils
	    .listFiles(directory,
		       FileFilterUtils
		       .and(/*          */suffixFileFilter(SUFFIX_TEX),
			    notFileFilter(prefixFileFilter(PREFIX_HIDDEN))),
		       TrueFileFilter.INSTANCE);
	// FIXME: may be null 
	for (File file : latexFiles) {
	    // may throw BuildExecutionException 
	    if (matchInFile(file, patternLatexMainFile)) {
		mainFiles.add(file);
	    }
        }
        return mainFiles;
    }
 
    /**
     * Returns the directory containing <code>sourceFile</code> 
     * with the prefix <code>sourceBaseDir</code> 
     * replaced by <code>targetBaseDir</code>. 
     * E.g. <code>sourceFile=/tmp/adir/afile</code>, 
     * <code>sourceBaseDir=/tmp</code>, <code>targetBaseDir=/home</code> 
     * returns <code>/home/adir/</code>. 
     *
     * @param srcFile
     *    the source file the parent directory of which 
     *    shall be converted to the target. 
     * @param srcBaseDir
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
     * @throws BuildFailureException
     *    if <code>sourceFile</code> is not below <code>sourceBaseDir</code>. 
     */
    public File getTargetDirectory(File srcFile,
				   File srcBaseDir,
				   File targetBaseDir)
	throws BuildExecutionException, BuildFailureException {

	try {
	    // getCanonicalPath may throw IOException 
	    String srcParentPath = srcFile.getParentFile().getCanonicalPath();
	    // getCanonicalPath may throw IOException 
            String srcBasePath = srcBaseDir.getCanonicalPath();

	    if (!srcParentPath.startsWith(srcBasePath)) {
		throw new BuildFailureException
		    ( "File " + srcFile + 
		      " is expected to be somewhere under directory "
		      + srcBasePath + ". ");
	    }

	    return new File(targetBaseDir, 
			    srcParentPath.substring(srcBasePath.length()));
	} catch (IOException e) {
            throw new BuildExecutionException
		("Error getting canonical path. ", e);
        }
    }

    /**
     * Returns a wildcard file filter containing all files 
     * replacing the suffix of <code>file</code> 
     * with the pattern given by <code>filesPatterns</code>. 
     *
     * @param file
     *    the file to derive filenames from. 
     *    Typically, this is used for tex-files but also for mp-files. 
     * @param filesPatterns
     *    patterns of the form <code>.&lt;suffix&gt;</code> 
     *    or <code>*.&lt;suffix&gt;</code> 
     * @return
     *    A file filter given by the wildcard 
     *    replacing the suffix of <code>file</code> 
     *    with the pattern given by <code>filesPatterns</code>. 
     */
    // used in LatexProcessorOnly: in methods 
    // - create on tex-file to determine output files. 
    // - clearGraphics to clear xxx1.pmx-files 
    // - clearFromLatexMain to clear files 
    public FileFilter getFileFilter(File file, String[] filesPatterns) {
        String filePrefix = getFileNameWithoutSuffix(file);
        String[] fileNames = new String[filesPatterns.length];
        for (int i = 0; i < filesPatterns.length; i++) {
            fileNames[i] = filePrefix + filesPatterns[i];
        }

	return new WildcardFileFilter(fileNames);
    }

    /**
     * Copies output to target folder. 
     * The source is the parent folder of <code>texFile</code>, 
     * all its files passing <code>fileFilter</code> 
     * are copied to <code>targetDir</code>. 
     * This is invoked by {@link #LatexProcessor#execute()} only. 
     *
     * @BuildExecutionException
     *    wraps IOException when reading <code>texFile</code>'s folder 
     *    or when copying. 
     */
    public void copyOutputToTargetFolder(File texFile, 
					 FileFilter fileFilter, 
					 File targetDir)
	throws BuildExecutionException {

	File texFileDir = texFile.getParentFile();
        File[] outputFiles = texFileDir.listFiles();

        if (outputFiles == null) {
	    // since outputFiles is a directory 
	    throw new BuildExecutionException
		("Error reading directory " + texFileDir + "! " );
	}
	assert outputFiles != null;

	// Hm,... this means even that there is no latex file. 
	// Also, there may be no file created although outputFiles is not empty
        if (outputFiles.length == 0) {
            log.warn("LaTeX file " + texFile + 
		     " did not generate any output in " + texFileDir + "! " );
        }

	File file;
	for (int idx = 0; idx < outputFiles.length; idx++) {
	    file = outputFiles[idx];
	    if (fileFilter.accept(file)) {
		log.info("Copying " + file.getName() + 
			 " to " + targetDir + ". ");
		try {
		    FileUtils.copyFileToDirectory(file, targetDir);
		} catch (IOException e) {
		    throw new BuildExecutionException
			("Error copying " + file.getName() + 
			 " to " + targetDir + ". ",
			 e);
		}
	    }
	}
    }

    /**
     * Return the name of the given file without the suffix. 
     * 
     * @see TexFileUtils#getFileNameWithoutSuffix(File)
     */
    public String getFileNameWithoutSuffix(File texFile) {
        String nameTexFile = texFile.getName();
        String namePrefixTexFile = nameTexFile
	    .substring(0, nameTexFile.lastIndexOf("."));
        return namePrefixTexFile;
    }

    /**
     * Return a collection of files in directory <code>dir</code> 
     * with suffix <code>suffix</code>. 
     *
     * @param dir
     *    the directory the listing of which is requested. 
     * @param suffix
     *    the suffix of the files to be returned. 
     * @return
     *    The collection of files in <code>dir</code>. 
     *    This may never be <code>null</code>. 
     * @throws BuildExecutionException
     *    if <code>dir</code> is not a directory or if an IO-error occurs. 
     * @see TexFileUtils#getFileNameWithoutSuffix(File, String)
     */
    public Collection<File> getFilesWithSuffix(File dir, String suffix) 
	throws BuildExecutionException {
	Collection<File> res = FileUtils
	    .listFiles(dir,
		       suffixFileFilter(suffix),
		       TrueFileFilter.INSTANCE);
	if (res == null) {
	    throw new BuildExecutionException
		("File " + dir + " is not readable or no directory. ");
	}
	return res;
    }



    // logFile may be .log or .blg or something 
    /**
     * Returns whether the given file <code>file</code> (which shall exist) 
     * contains the given pattern <code>pattern</code>. 
     * This is typically applied to log files, 
     * but also to latex-files to find the latex main files. 
     *
     * @param file
     *    an existing proper file, not a folder. 
     * @param pattern
     *    the pattern (regular expression) to look for in <code>file</code>. 
     * @throws BuildExecutionException
     *    if the file <code>file</code> does not exist or cannot be read. 
     */
    // used in LatexProcessor only: 
    // only in methods processLatex2pdf, runLatex2html, runLatex2odt, 
    // needAnotherLatexRun, needBibtexRun, 
    // runMakeindex, runBibtex, runLatex
    public boolean matchInFile(File file, String pattern)
        throws BuildExecutionException {

	try {
	    return fileContainsPattern(file, pattern);
	} catch (FileNotFoundException e) {
	    throw new BuildExecutionException
		("File " + file.getPath() + " not found. ", e);
	} catch (IOException e) {
	    throw new BuildExecutionException
		("Error reading file " + file.getPath() + ". ", e);
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

        Pattern pattern = Pattern.compile(regex);
	// may throw FileNotFoundException
	FileReader fileReader = new FileReader(file);
	BufferedReader bufferedReader = new BufferedReader(fileReader);
	try {
	    for (String line = bufferedReader.readLine();// may thr. IOException
		 line != null;
		 line = bufferedReader.readLine()) {// may throw IOException
                if (pattern.matcher(line).find()) {
		    return true;
                }
            }
	   return false;
        } finally {
	    // Here, an IOException may have occurred 
	    try {
		bufferedReader.close();
	    } catch (IOException e) {
		log.warn("Cannot close the file '" + file.getPath() + "'.", e);
	    }
        }
    }

    public File replaceSuffix(File file, String suffix) {
        return new File(file.getParentFile(),
			getFileNameWithoutSuffix(file) + suffix );
    }

    /**
     * Deletes all files in the same folder as <code>pFile</code> directly, 
     * i.e. not in subfolders, which are accepted by <code>filter</code>. 
     */
    public void delete(File pFile, FileFilter filter) 
	throws BuildExecutionException {

	File dir = pFile.getParentFile();
	assert dir.isDirectory();
	File[] files = dir.listFiles();
	if (files == null) {
	    // Here, dir is not readable because a directory 
	    throw new BuildExecutionException
		("Directory " + dir + " is not readable. ");
	}
	for (File delFile : files) {
	    if (filter.accept(delFile)) {
		delFile.delete();
	    }
	}
    }

    /**
     * Deletes all files in <code>texDir</code> including subdirectories 
     * which are not in <code>orgFiles</code>. 
     * The background is, that <code>orFiles</code> are the files 
     * originally in <code>texDir</code>. 
     *
     * @throws BuildExecutionException
     *    if <code>texDir</code> is no directory or not readable. 
     */
    public void cleanUp(Collection<File> orgFiles, File texDir) 
	throws BuildExecutionException {

	log.debug("Clearing set of sources. ");
	// may throw BuildExecutionException 
	Collection<File> currFiles = getFilesRec(texDir);
	currFiles.removeAll(orgFiles);
	for (File file : currFiles) {
	    file.delete();
	}
    }

    public static void main(String[] args) {
	Pattern pattern = Pattern.compile("^! ");
	System.out.println("res: "+pattern.matcher(args[0]).find());
	
    }

 }
