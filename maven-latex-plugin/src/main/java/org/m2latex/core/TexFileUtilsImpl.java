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

import java.nio.file.Path;

import java.util.Collection;
import java.util.ArrayList;
import java.util.TreeSet;

import java.util.regex.Pattern;

import static org.apache.commons.io.FileUtils.listFiles;
import static org.apache.commons.io.FileUtils.copyFileToDirectory;
import        org.apache.commons.io.filefilter.TrueFileFilter;// with listFiles only
// FIXME: jdee bug: delete static imports: does not find superfluous 

/**
 * Sole interface to <code>org.apache.commons.io.</code>. 
 */
class TexFileUtilsImpl implements TexFileUtils {

    private final static String PREFIX_HIDDEN = ".";

    private final static String PATTERN_INS_LATEX_MAIN = "T\\$T";

    private final LogWrapper log;

    TexFileUtilsImpl(LogWrapper log) {
        this.log = log;
    }

    /**
     * Returns the ordered collection of files 
     * in folder <code>texDir</code> and subfolder. 
     *
     * @param texDir
     *    the tex-source directory. 
     * @return
     *    the ordered collection of files without directories 
     *    in folder <code>texDir</code> and subfolder. 
     * @throws BuildFailureException
     *    if <code>texDir</code> is not a folder or not readable. 
     */
    // used in 
    // - cleanUp 
    // - LatexPreProcessor.clearCreated 2x: delete and check 
    // - LatexProcessor.create()
    // - LatexProcessor.processGraphics()
    public Collection<File> getFilesRec(File texDir) 
	throws BuildFailureException {

	// FIXME: FileUtils.listFiles must not be used, 
	// because in case of IO-problems silently skips directories 
        Collection<File> res1 = listFiles(texDir,
					  TrueFileFilter.INSTANCE,
					  TrueFileFilter.INSTANCE);
	if (res1 == null) {
	    throw new BuildFailureException
		("File '" + texDir + "' is not readable or no directory. ");
	}
	Collection<File> res = new TreeSet<File>();
	res.addAll(res1);
	return res;
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
     * @throws BuildFailureException
     *    if <code>sourceFile</code> is not below <code>sourceBaseDir</code>. 
     */
    // used by LatexProcessor.create() only 
    public File getTargetDirectory(File srcFile,
				   File srcBaseDir,
				   File targetBaseDir) 
	throws BuildFailureException {

	// getCanonicalPath may throw IOException 
	Path srcParentPath = srcFile.getParentFile().toPath();
	// getCanonicalPath may throw IOException 
	Path srcBasePath = srcBaseDir.toPath();

	// FIXME: really ok? what if strings but not paths are prefixes? 
	// I think should be via Path: toPath. 
	assert srcParentPath.startsWith(srcBasePath);
	srcParentPath = srcBasePath.relativize(srcParentPath);

	// FIXME: CAUTION: this may exist and be no directory!
	File res = new File(targetBaseDir, srcParentPath.toString());
	if (res.exists() && !res.isDirectory()) {
	    throw new BuildFailureException
		("Require target directory '" + res + 
		 "' which exists already as regular file. ");
	}
	return res;
    }

    /**
     * Returns a file filter matching neither directories 
     * nor <code>texFile</code> 
     * but else all files with names matching <code>pattern</code>, 
     * where the special sequence {@link #PATTERN_INS_LATEX_MAIN} 
     * is replaced by the prefix of <code>texFile</code>. 
     *
     * @param texFile
     *    a latex main file for which a file filter has to be created. 
     * @param pattern
     *    a pattern 
     *    for which the special sequence {@link #PATTERN_INS_LATEX_MAIN} 
     *    is replaced by the prefix of <code>texFile</code> 
     *    before a file filter is created from it. 
     * @return
     *    a non-null file filter matching neither directories 
     *    nor <code>texFile</code> 
     *    but else all files with names matching <code>pattern</code>, 
     *    where the special sequence {@link #PATTERN_INS_LATEX_MAIN} 
     *    is replaced by the prefix of <code>texFile</code>. 
     */
    // used only: in methods 
    // - LatexProcessor.create on tex-file to determine output files. 
    // - LatexPreProcessor.clearTargetTex to clear also intermediate files. 
    public FileFilter getFileFilter(File texFile, String pattern) {
	final String patternAccept = pattern
	    .replaceAll(PATTERN_INS_LATEX_MAIN, 
			getFileNameWithoutSuffix(texFile));
	return new FileFilter() {
	    public boolean accept(File file) {
		// the second is superfluous for copying 
		// and only needed for deletion. 
		if (file.isDirectory() || file.equals(texFile)) {
		    return false;
		}
		return file.getName().matches(patternAccept);
	    }
	};
    }

    /**
     * Copies output to target folder. 
     * The source is the parent folder of <code>texFile</code>, 
     * all its files passing <code>fileFilter</code> 
     * are copied to <code>targetDir</code>. 
     * This is invoked by {@link #LatexProcessor#execute()} only. 
     *
     * @throws BuildFailureException
     *    either target directory is not readable 
     *    or wraps IOException when reading <code>texFile</code>'s folder 
     *    or when copying. 
     */
    // used in LatexProcessor.create() only 
    public void copyOutputToTargetFolder(File texFile, 
					 FileFilter fileFilter, 
					 File targetDir)
	throws BuildFailureException {

	File texFileDir = texFile.getParentFile();
        File[] outputFiles = texFileDir.listFiles();

        if (outputFiles == null) {
	    // since texFileDir is a directory 
	    throw new BuildFailureException
		("Error reading directory '" + texFileDir + "'! " );
	}
	assert outputFiles != null;

	// Hm,... this means even that there is no latex file. 
	// Also, there may be no file created although outputFiles is not empty
        if (outputFiles.length == 0) {
            log.warn("LaTeX file '" + texFile + 
		     "' did not generate any output in '" + texFileDir + "'! ");
        }

	File file;
	for (int idx = 0; idx < outputFiles.length; idx++) {
	    file = outputFiles[idx];
	    if (fileFilter.accept(file)) {
		log.info("Copying '" + file.getName() + 
			 "' to '" + targetDir + "'. ");
		// FIXME: fileFilter shall not accept directories 
		// and shall not accept texFile 
		try {
		    // may throw IOException: 
		    // - if source is invalid: not existing or is a directory 
		    // - if destination is invalid: 
		    //   same as source (cannot occur)
		    //    parent if needed and non-existent cannot be created, 
		    //    exists and cannot be written to 
		    //    (not: or is a directory) 
		    // - error opening streams, read/write 
		    // FileUtils.
		    copyFileToDirectory(file, targetDir);
		    // FIXME: catch IllegalArgumentException 
		    // destination exists and is no dir 
		} catch (IOException e) {
		    throw new BuildFailureException
			("Error copying '" + file.getName() + 
			 "' to '" + targetDir + "'. ",
			 e);
		}
	    }
	}
    }

    /**
     * Return the name of the given file without the suffix. 
     * If the suffix is empty, this is just the name of that file. 
     *
     * @see #getSuffix(File)
     */
    public String getFileNameWithoutSuffix(File file) {
        String nameFile = file.getName();
	int idxDot = nameFile.lastIndexOf(".");
	return idxDot == -1
	    ? nameFile
	    : nameFile.substring(0, idxDot);
    }

    /**
     * Return the suffix of the name of the given file 
     * including the <code>.</code>, 
     * except there is no <code>.</code>. 
     * Then the suffix is empty. 
     *
     * @see #getFileNameWithoutSuffix(File)
     */
    public String getSuffix(File file) {
        String nameFile = file.getName();
	int idxDot = nameFile.lastIndexOf(".");
	return idxDot == -1 
	    ? "" 
	    : nameFile.substring(idxDot, nameFile.length());
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
     * @throws BuildFailureException
     *    if the file <code>file</code> does not exist or cannot be read. 
     */
    // used in LatexProcessor only: 
    // only in methods processLatex2pdf, runLatex2html, runLatex2odt, 
    // needAnotherLatexRun, needBibtexRun, 
    // runMakeindex, runBibtex, runLatex
    public boolean matchInFile(File file, String pattern)
        throws BuildFailureException {

	try {
	    return fileContainsPattern(file, pattern);
	} catch (FileNotFoundException e) {
	    throw new BuildFailureException
		("File '" + file.getPath() + "' not found. ", e);
	} catch (IOException e) {
	    throw new BuildFailureException
		("Error reading file '" + file.getPath() + "'. ", e);
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
    public void deleteX(File pFile, FileFilter filter) 
	throws BuildFailureException {

	File dir = pFile.getParentFile();
	assert dir.isDirectory();
	File[] files = dir.listFiles();
	if (files == null) {
	    // Here, dir is not readable because a directory 
	    throw new BuildFailureException
		("Directory '" + dir + "' is not readable. ");
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
     * @throws BuildFailureException
     *    if <code>texDir</code> is no directory or not readable. 
     */
    public void cleanUp(Collection<File> orgFiles, File texDir) 
	throws BuildFailureException {

	log.debug("Clearing set of sources. ");
	// may throw BuildFailureException 
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
