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
import java.io.Closeable;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;

import java.nio.file.Path;

import java.util.Collection;
import java.util.TreeSet;

import java.util.regex.Pattern;

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
     * <p>
     * Logging: 
     * WFU01 texDir not readable 
     *
     * @param texDir
     *    the tex-source directory. 
     * @return
     *    the ordered collection of files without directories 
     *    in folder <code>texDir</code> and subfolder. 
     */
    // used in 
    // - cleanUp 
    // - LatexPreProcessor.clearCreated 
    // - LatexProcessor.create()
    // - LatexProcessor.processGraphics()
    public Collection<File> getFilesRec(File texDir) {
	assert texDir.exists() && texDir.isDirectory();

	// FIXME: FileUtils.listFiles must not be used, 
	// because in case of IO-problems silently skips directories 
	// FIXME: skip hidden files 
	// because they make problems with getSuffix(File)
	Collection<File> res = new TreeSet<File>();
	// may log warning WFU01 texDir not readable 
	innerListFiles(res, texDir);
	return res;
    }

    // FIXME: copied from org.apache.commons.io.FileUtils 
    /**
     * Finds files within a given directory <code>dir</code> 
     * and its subdirectories and adds them to <code>files</code>. 
     * <p>
     * Logging: 
     * WFU01 if <code>dir</code> is not readable. 
     *
     * @param files
     *    the collection of files found so far.
     * @param dir
     *    the directory to search in. 
     *    An assertion assures that this is indeed a directory. 
     */
    private void innerListFiles(Collection<File> files, File dir) {
	assert dir.isDirectory();
        File[] found = dir.listFiles();
	// found is null if directory is not a directory (which is exlcuded) 
	// or if an io-error occurs. 
	// Thus in these cases, the failure is ignored silently 
        if (found == null) {
	    this.log.warn("WFU01: Cannot read directory '" + dir + "'. ");
	}
	File file;
	for (int i = 0; i < found.length; i++) {
	    file = found[i];
	    if (file.isDirectory()) {
		// may log warning WFU01 file not readable 
		innerListFiles(files, file);
	    } else {
		// FIXME: skip hidden files 
		files.add(file);
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
     *    TFU01 if the target directory that would be returned 
     *    exists already as a regular file. 
     */
    // used by LatexProcessor.create() only 
    public File getTargetDirectory(File srcFile,
				   File srcBaseDir,
				   File targetBaseDir) 
	throws BuildFailureException {

	Path srcParentPath = srcFile.getParentFile().toPath();
	Path srcBasePath = srcBaseDir.toPath();

	// FIXME: really ok? what if strings but not paths are prefixes? 
	// I think should be via Path: toPath. 
	assert srcParentPath.startsWith(srcBasePath);
	srcParentPath = srcBasePath.relativize(srcParentPath);

	// FIXME: CAUTION: this may exist and be no directory!
	File res = new File(targetBaseDir, srcParentPath.toString());
	if (res.exists() && !res.isDirectory()) {
	    throw new BuildFailureException
		("TFU01: Required target directory '" + res + 
		 "' exists already as regular file. ");
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
     * Copies output of the current goal to target folder. 
     * The source is the parent folder of <code>texFile</code>, 
     * all its files passing <code>fileFilter</code> 
     * are considered as output files and 
     * are copied to <code>targetDir</code>. 
     * This is invoked by {@link #LatexProcessor#execute()} only. 
     * <p>
     * Logging: 
     * <ul>
     * <li> WFU02: LaTeX file did not generate any output. (DEPRECATED)
     * <li> WFU03: Cannot close 
     * </ul>
     *
     * @param texFile
     *    the latex main file which was processed. 
     *    Its parent directory 
     *    is the working directory of the compilation process 
     *    in which the output files are created. 
     *    Thus it must be readable (in fact it must also be writable; 
     *    otherwise the output files could not have been created). 
     * @param fileFilter
     *    the filter accepting the files (and best only the files) 
     *    which are the result of the processing. 
     * @param targetDir
     *    the target directory the output files have to be copied to. 
     *    If this exists already, it must be a directory 
     *    and it must be writable. 
     *    If it does not exist, it must be creatable. 
     * @throws BuildFailureException
     *    <ul>
     *    <li>TFU02 if 
     *    the source directory (containing <code>texFile</code>) 
     *    is not readable. 
     *    <li>TFU03 if 
     *    the destination directory does not exist and cannot be created. 
     *    <li>TFU04, TFU05 if 
     *    the destination file exists 
     *    and is either a directory (TFU04) or is not writable (TFU05). 
     *    <li>TFU06 if 
     *    an IO-error orrurs when copying: opening streams, reading or writing. 
     *    </ul>
     */
    // used in LatexProcessor.create() only 
    public void copyOutputToTargetFolder(File texFile, 
					 FileFilter fileFilter, 
					 File targetDir)
	throws BuildFailureException {

	assert !targetDir.exists() || targetDir.isDirectory();

	File texFileDir = texFile.getParentFile();
        File[] outputFiles = texFileDir.listFiles();

        if (outputFiles == null) {
	    // since texFileDir is a directory 
	    throw new BuildFailureException
		("TFU02: Error reading directory '" + texFileDir + "'! " );
	}
	assert outputFiles != null;

	// Hm,... this means even that there is no latex file. 
	// Also, there may be no file created although outputFiles is not empty
	// FIXME: I think this is superfluous: 
	// The software indicates when a latex main file is found 
	// when it is processed and also, later, 
	// if it produces not the expected output. 
        // if (outputFiles.length == 0) {
        //     this.log.warn("WFU02: LaTeX file '" + texFile + 
	// 		  "' did not generate any output in '" + 
	// 		  texFileDir + "'! ");
        // }

	File srcFile, destFile;
	for (int idx = 0; idx < outputFiles.length; idx++) {
	    srcFile = outputFiles[idx];
	    assert srcFile.exists();
	    if (!fileFilter.accept(srcFile)) {
		continue;
	    }
	    assert srcFile.exists() && !srcFile.isDirectory();
	    // since !targetDir.exists() || targetDir.isDirectory() 
	    assert !srcFile.equals(targetDir);

	    this.log.info("Copying '" + srcFile.getName() + 
			  "' to '" + targetDir + "'. ");
	    // FIXME: fileFilter shall not accept directories 
	    // and shall not accept texFile 

	    // FIXME: what if a parent is a regular file? 
	    if (!targetDir.exists() && !targetDir.mkdirs()) {
		throw new BuildFailureException
		    ("TFU03: Destination directory '" + targetDir + 
		     "' cannot be created. ");
	    }
	    assert targetDir.isDirectory();

	    destFile = new File(targetDir, srcFile.getName());

	    if (destFile.exists()) {
		if (destFile.isDirectory()) {
		    throw new BuildFailureException
			("TFU04: Destination file '" + destFile + 
			 "' exists but is a directory. ");
		}
		if (!destFile.canWrite()) {
		    throw new BuildFailureException
			("TFU05: Destination file '" + destFile + 
			 "' exists and is read-only. ");
		}
	    }

	    try {
		// may throw IOException: opening streams, read/write 
		// may log warning WFU03: Cannot close 
		doCopyFile(srcFile, destFile);
	    } catch (IOException e) {
		throw new BuildFailureException
		    ("TFU06: Error copying '" + srcFile.getName() + 
		     "' to '" + targetDir + "'. ",
		     e);
	    }
	} // for 
    }

    // FIXME: copied from FileUtils 
    /**
     * Internal copy file method. 
     * <p>
     * Logging: 
     * WFU03: Cannot close 
     * 
     * @param srcFile   
     *    the source file. 
     * @param destFile   
     *    the destination file. 
     * @throws IOException  
     *    if an error occurs: opening input/output streams, 
     *    reading from file/writing to file. 
     */
    private void doCopyFile(File srcFile, File destFile) throws IOException {
	// may throw FileNotFoundException <= IOException 
	// if cannot be opened for reading: e.g. not exists, is a directory,...
        FileInputStream input = new FileInputStream(srcFile);
        try {
	    // may throw FileNotFoundException <= IOException 
            FileOutputStream output = new FileOutputStream(destFile);
	    // if cannot be opened for writing: 
	    // e.g. not exists, is a directory,...
	    try {
		// may throw IOException if an I/O-error occurs 
		// when reading or writing 
                copyLarge(input, output);
            } finally {
		// may log warning WFU03
                closeQuietly(output);
            }
	} finally {
	    // may log warning WFU03
 	    closeQuietly(input);
	}

	// FIXME: what about SecurityExceptions? 
	destFile.setLastModified(srcFile.lastModified());
    }

    /**
     * The default buffer size ({@value}) to use for 
     * {@link #copyLarge(InputStream, OutputStream)}
     * and
     * {@link #copyLarge(Reader, Writer)}
     */
    private static final int DEFAULT_BUFFER_SIZE = 1024 * 4;

    /**
     * Copy bytes from a large (over 2GB) <code>InputStream</code> to an
     * <code>OutputStream</code>.
     * <p>
     * This method uses the provided buffer, so there is no need to use a
     * <code>BufferedInputStream</code>.
     *
     * @param input
     *    the <code>InputStream</code> to read from
     * @param output
     *    the <code>OutputStream</code> to write to
     * @throws IOException 
     *    if an I/O error occurs while reading or writing 
     */
    public static void copyLarge(InputStream input, 
				 OutputStream output) throws IOException {
	byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
        int n = 0;
	// may throw IOException 
        while (-1 != (n = input.read(buffer))) {
	    // may throw IOException 
            output.write(buffer, 0, n);
        }
    }

    // FIXME: almost copy from IOUtils 
    /**
     * Unconditionally close a <code>Closeable</code>.
     * <p>
     * Equivalent to {@link Closeable#close()}, 
     * except any exceptions will be ignored. FIXME 
     * This is typically used in finally blocks.
     * <p>
     * Example code:
     * <pre>
     *   Closeable closeable = null;
     *   try {
     *       closeable = new FileReader("foo.txt");
     *       // process closeable
     *       closeable.close();
     *   } catch (Exception e) {
     *       // error handling
     *   } finally {
     *       IOUtils.closeQuietly(closeable);
     *   }
     * </pre>
     * <p>
     * Logging: 
     * WFU03: Cannot close 
     *
     * @param closeable 
     * the object to close, may be null or already closed
     */
   private void closeQuietly(Closeable closeable) {
        try {
	    closeable.close();
	} catch (IOException ioe) {
	    this.log.warn("WFU03: Cannot close '" + closeable + "'. ", ioe);
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
    // used only by 
    // LatexPreProcessor.processGraphicsSelectMain(Collection) 
    // LatexPreProcessor.clearCreated(File) 2x
    // FIXME: problem if filename starts with . and has no further . 
    // then we have a hidden file and the suffix is all but the . 
    // This is not appropriate. 
    // One may ensure that this does not happen via an assertion 
    // and by modifying getFilesRec in a way that hidden files are skipped 
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
     * <p>
     * Logging: 
     * WFU03 cannot close 
     *
     * @param file
     *    an existing proper file, not a folder. 
     * @param pattern
     *    the pattern (regular expression) to look for in <code>file</code>. 
     * @throws BuildFailureException
     *    <ul>
     *    <li> TFU07 if the file <code>file</code> does not exist 
     *    <li> TFU08 if the file <code>file</code> cannot be read. 
     *    </ul>
     */
    // used only in 
    // LatexPreProcessor.isLatexMainFile(File)
    // LatexProcessor.needRun(...)
    // AbstractLatexProcessor.hasErrsWarns(File, String)
    //
    // FIXME: the two exceptions are always caught. 
    // An alternative: 3-valued logic: 
    // true, false, unknown if file not found or not readable 
    public boolean matchInFile(File file, 
			       String pattern) throws BuildFailureException {
	try {
	    // may log warning WFU03 
	    return fileContainsPattern(file, pattern);
	} catch (FileNotFoundException e) {
	    throw new BuildFailureException
		("TFU07: File '" + file.getPath() + "' not found. ", e);
	} catch (IOException e) {
	    throw new BuildFailureException
		("TFU08: Error reading file '" + file.getPath() + "'. ", e);
	}
    }

    /**
     * Return whether <code>file</code> contains <code>regex</code>. 
     * <p>
     * Logging: 
     * WFU03 cannot close 
     *
     * @throws FileNotFoundException
     *    if <code>file</code> does not exist. 
     * @throws IOException
     *    if <code>file</code> could not be read. 
     */
    // used by matchInFile(File logFile, String pattern) only 
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
	    // may log warning WFU03
 	    closeQuietly(bufferedReader);
        }
    }

    // used in LatexPreProcessor and in LatexProcessor and in LatexDec
    // at numerous places 
    public File replaceSuffix(File file, String suffix) {
        return new File(file.getParentFile(),
			getFileNameWithoutSuffix(file) + suffix );
    }

    /**
     * Deletes all files in the same folder as <code>pFile</code> directly, 
     * i.e. not in subfolders, which are accepted by <code>filter</code>. 
     * <p>
     * Logging: 
     * <ul>
     * <li> WFU04: Cannot delete from directory: is not readable. 
     * <li> WFU05: Failed to delete file 
     * </ul>
     *
     * @param pFile
     *    a file in a folder to be deleted from. 
     * @param filter
     *    a filter which decides which files 
     *    from the parent directory of <code>pFile</code> to delete. 
     */
    // used in LatexPreProcessor.clearTargetMp
    // used in LatexPreProcessor.clearTargetTex only 
    public void deleteX(File pFile, FileFilter filter) {
	File dir = pFile.getParentFile();
	assert dir.isDirectory();
	File[] files = dir.listFiles();
	if (files == null) {
	    // Here, dir is not readable because a directory 
	    this.log.warn("WFU04: Cannot delete from directory '" + dir + 
			  "': is not readable. ");
	}
	boolean isDeleted;
	for (File delFile : files) {
	    assert delFile.exists();
	    if (filter.accept(delFile)) {
		assert delFile.exists() && !delFile.isDirectory();
		isDeleted = delFile.delete();
		if (!isDeleted) {
		    this.log.warn("WFU05: Failed to delete file '" + 
				  delFile + "'. ");
		}
	    }
	}
    }

    /**
     * Deletes all files in <code>texDir</code> including subdirectories 
     * which are not in <code>orgFiles</code>. 
     * The background is, that <code>orFiles</code> are the files 
     * originally in <code>texDir</code>. 
     */
    // used in LatexProcessor.create() only 
    // FIXME: warn if deletion failed. 
    public void cleanUp(Collection<File> orgFiles, File texDir) {

	this.log.debug("Clearing set of sources. ");
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
