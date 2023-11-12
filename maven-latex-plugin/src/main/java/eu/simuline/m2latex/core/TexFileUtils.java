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

package eu.simuline.m2latex.core;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;

// FIXME: jdee bug: delete static imports: does not find superfluous

/**
 * Sole interface to <code>org.apache.commons.io.</code>. 
 * A collection of utility methods for file manipulation. 
 */
class TexFileUtils {

  //private final static String PREFIX_HIDDEN = ".";

  private final static String PATTERN_INS_LATEX_MAIN = "T\\$T";

  // TBD: make configurable 
  // folder created by auxiliary program pythontex 
  // holding all its output files by default 
  final static String PREFIX_PYTEX_OUT_FOLDER = "pythontex-files-";

  private final LogWrapper log;

  TexFileUtils(LogWrapper log) {
    this.log = log;
  }

  /**
   * Returns the listing of the directory <code>dir</code> 
   * or <code>null</code> if it is not readable 
   * and emit an according warning if so. 
   * <p>
   * Logging: 
   * WFU01: Cannot read directory 
   *
   * @param dir
   *    an existing directory. 
   * @return
   *    the list of entries of <code>dir</code> 
   *    or <code>null</code> if it is not readable. 
   */
  // used only in 
  // constructor of DirNode 
  // copyOutputToTargetFolder, deleteX
  File[] listFilesOrWarn(File dir) {
    assert dir != null && dir.isDirectory() : "Expected folder found " + dir;
    File[] files = dir.listFiles();
    warnIfNull(files, dir);
    return files;
  }

  /**
   * Returns the listing of the directory <code>dir</code> 
   * filtered by <code>filter</code> 
   * or <code>null</code> if <code>dir</code> is not readable 
   * and emit an according warning if so. 
   * <p>
   * Logging: 
   * WFU01: Cannot read directory 
   *
   * @param dir
   *    an existing directory. 
   * @param filter
   *    a file filter 
   * @return
   *    the list of entries of <code>dir</code> 
   *    accepted by <code>filter</code>
   *    or <code>null</code> if <code>dir</code> is not readable. 
   */
  // used by LatexProcessor.runMakeIndexByNeed only 
  File[] listFilesOrWarn(File dir, FileFilter filter) {
    assert dir != null && dir.isDirectory() : "Expected folder found " + dir;
    File[] files = dir.listFiles(filter);
    warnIfNull(files, dir);
    return files;
  }

  private void warnIfNull(File[] files, File dir) {
    if (files == null) {
      this.log.warn("WFU01: Cannot read directory '" + dir
          + "'; build may be incomplete. ");
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
   *    TFU01: if the target directory that would be returned 
   *    exists already as a regular file. 
   */
  // used by LatexProcessor.create() only 
  File getTargetDirectory(File srcFile, File srcBaseDir, File targetBaseDir)
      throws BuildFailureException {
    Path srcParentPath = srcFile.getParentFile().toPath();
    Path srcBasePath = srcBaseDir.toPath();

    assert srcParentPath.startsWith(srcBasePath);
    srcParentPath = srcBasePath.relativize(srcParentPath);

    // this may exist but if so it is a directory because holds srcFile 
    File targetDir = new File(targetBaseDir, srcParentPath.toString());

    targetDir.mkdirs();

    if (!targetDir.isDirectory()) {
      throw new BuildFailureException(
          "TFU01: Cannot create destination directory '" + targetDir + "'. ");
    }
    assert targetDir.isDirectory();
    return targetDir;
  }

  /**
   * Returns the pdf file which serves as a blue print for <code>pdfFileAct</code>. 
   * The relative path of <code>pdfFileAct</code> to its ancestor <code>artifactBaseDir</code> 
   * is the same as the blueprint to its ancestor <code>diffRootDir</code>. 
   *
   * @param pdfFileAct
   *     the artifact to be checked agains a blueprint in <code>diffRootDir</code>. 
   *     It must be contained in <code>artifactBaseDir</code>, immediately or not. 
   * @param artifactBaseDir
   *     the base directory for all artifacts, in particular of <code>pdfFileAct</code>. 
   * @param diffBaseDir
   *     the root directory of all blue prints corresponding with <code>diffRootDir</code>. 
   */
  static File getPdfFileDiff(File pdfFileAct, File artifactBaseDir,
      File diffBaseDir) {
    Path pdfFileActPath = pdfFileAct.toPath();
    Path artifactBasePath = artifactBaseDir.toPath();

    assert pdfFileActPath.startsWith(artifactBasePath);
    pdfFileActPath = artifactBasePath.relativize(pdfFileActPath);

    // this may exist but if so it is a directory because holds srcFile
    return new File(diffBaseDir, pdfFileActPath.toString());
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
   * @param allowsDirs
   *    Whether the filter returned accepts also directories. 
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
  static FileFilter getFileFilter(File texFile, String pattern,
      boolean allowsDirs) {
    final String patternAccept = pattern.replaceAll(PATTERN_INS_LATEX_MAIN,
        getFileNameWithoutSuffix(texFile));
    return new FileFilter() {
      public boolean accept(File file) {
        // the second is superfluous for copying
        // and only needed for deletion.
        if ((file.isDirectory() && !allowsDirs) || file.equals(texFile)) {
          return false;
        }
        return file.getName().matches(patternAccept);
      }
    };
  }

  /**
   * Returns a file filter matching no directories 
   * but else all files with names matching <code>xxx<pattern>.idx</code>, 
   * where <code>idxFile</code> has the form <code>xxx.idx</code>. 
   *
   * @param idxFile
   *    an idx file for which a file filter has to be created. 
   * @param pattern
   *    a pattern which is inserted in the name of <code>idxFile</code> 
   *    right before the suffix. 
   * @return
   *    a non-null file filter matching no directories 
   *    but else all files matching <code>xxx<pattern>.idx</code>. 
   */
  // used by LatexProcessor.runMakeIndexByNeed only 
  FileFilter getFileFilterReplace(File idxFile, String pattern) {
    final String patternAccept =
        getFileNameWithoutSuffix(idxFile) + pattern + getSuffix(idxFile);
    return new FileFilter() {
      public boolean accept(File file) {
        if (file.isDirectory()) {
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
   * <p>
   * Logging: 
   * <ul>
   * <li> WFU01: Cannot read directory... 
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
   * @return
   *    The set of all target files. 
   * @throws BuildFailureException
   *    <ul>
   *    <li>TFU04, TFU05 if 
   *    the destination file exists 
   *    and is either a directory (TFU04) or is not writable (TFU05). 
   *    <li>TFU06 if 
   *    an IO-error orrurs when copying: opening streams, reading or writing. 
   *    </ul>
   */
  // used in LatexProcessor.create() only 
  Set<File> copyOutputToTargetFolder(File texFile, FileFilter fileFilter,
      File targetDir) throws BuildFailureException {
    Set<File> targetFiles = new HashSet<File>();
    assert texFile.exists()
        && !texFile.isDirectory() : "Expected existing (regular) tex file "
            + texFile;
    assert !targetDir.exists() || targetDir
        .isDirectory() : "Expected existing target folder " + targetDir;

    File texFileDir = texFile.getParentFile();
    // may log warning WFU01
    File[] outputFiles = listFilesOrWarn(texFileDir);
    if (outputFiles == null) {
      // Here, logging WFU01 already done
      return targetFiles;
    }
    assert outputFiles != null;

    File srcFile, destFile;
    for (int idx = 0; idx < outputFiles.length; idx++) {
      srcFile = outputFiles[idx];
      assert srcFile.exists() : "Missing " + srcFile;
      if (!fileFilter.accept(srcFile)) {
        continue;
      }
      assert srcFile.exists()
          && !srcFile.isDirectory() : "Expected existing (regular) tex file "
              + texFile;
      // since !targetDir.exists() || targetDir.isDirectory()
      assert !srcFile.equals(targetDir);
      assert !srcFile.equals(texFile);

      destFile = new File(targetDir, srcFile.getName());

      if (destFile.isDirectory()) {
        throw new BuildFailureException(
            "TFU04: Cannot overwrite directory '" + destFile + "'. ");
      }

      this.log.debug(
          "Copying '" + srcFile.getName() + "' to '" + targetDir + "'. ");
      try {
        // may throw IOException: opening streams, read/write
        // may log warning WFU03: Cannot close
        doCopyFile(srcFile, destFile);
        targetFiles.add(destFile);
      } catch (IOException e) {
        throw new BuildFailureException("TFU06: Cannot copy '"
            + srcFile.getName() + "' to '" + targetDir + "'. ", e);
      }
    } // for
    return targetFiles;
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
        copyStream(input, output);
      } finally {
        // may log warning WFU03
        closeQuietly(output);
      }
    } finally {
      // may log warning WFU03
      closeQuietly(input);
    }

    assert !destFile.isDirectory() && destFile
        .canWrite() : "Expected existing (regular) writable file " + destFile;
    destFile.setLastModified(srcFile.lastModified());
  }

  /**
   * The default buffer size ({@value}) to use for 
   * {@link #copyStream(InputStream, OutputStream)}
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
  private static void copyStream(InputStream input, OutputStream output)
      throws IOException {
    byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
    int n;
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

  // TBD: move elsewhere because this is specific for inkscape
  // TBD: better even to eliminate. 
  /**
   * The new preamble of the tex file originally created by inkscape 
   * with ending <code>eps_tex</code>.
   * FIXME: version to be included. 
   */
  private final static String INKSCAPE_PREAMBLE =
      "%% LatexMavenPlugin (version unknown) modified "
          + "two of the following lines\n";

  /**
   * This is just a workaround because of inkscape's current flaw. 
   * It reads file <code>srcFile</code> 
   * which is expected to have name with ending <code>eps_tex</code> 
   * and writes a file with same name 
   * replacing ending by <code>tex</code> with following modifications: 
   * <ul>
   * <li>Adds line {@link #INKSCAPE_PREAMBLE} atop </li>
   *    <li>Replaces line '%%Accompanies ...' by 
   *     '%% Accompanies image files 'xxx.pdf/eps/ps'</li>
   *    <li>Replaces line 
   *     '... \includegraphics[width=\\unitlength]{xxx.eps}...' 
   *     by 
   *     '... \includegraphics[width=\\unitlength]{xxx}...'</li>
   * </ul>
   * <p>
   * Logging: 
   * EFU07, EFU08, EFU09: cannot fiter
   *
   * @param srcFile
   *    A file created by inkscape with ending <code>eps_tex</code> 
   *    containing a lines 
   *    <code>
   *    %% Accompanies image file 'xxx.eps' (pdf, eps, ps)</code> and 
   *    <code>\put(0,0){\includegraphics[width=\\unitlength]{xxx.eps}}</code>
   *    with variable <code>xxx</code> and leading blanks\
   */
  public void filterInkscapeIncludeFile(File srcFile,
          File destFile,
          String bareFileName,
          String epsSuffix) {
    // assert LatexPreProcessor.SUFFIX_EPSTEX
    //     .equals(getSuffix(srcFile)) : "Expected suffix '"
    //         + LatexPreProcessor.SUFFIX_EPSTEX + "' found '" + getSuffix(srcFile)
    //         + "'";
    // File destFile = replaceSuffix(srcFile, LatexPreProcessor.SUFFIX_PTX);
    // File bareFile = replaceSuffix(srcFile, LatexPreProcessor.SUFFIX_VOID);
    // //FileReader reader = null;
    BufferedReader bufferedReader = null;
    FileWriter writer = null;
    try {
      // may throw FileNotFoundException < IOExcption 
      FileReader reader = new FileReader(srcFile);
      // BufferedReader for performance and to be able to read a line
      bufferedReader = new BufferedReader(reader);

      // may throw IOExcption 
      writer = new FileWriter(destFile);
      //BufferedWriter bufferedWriter = new BufferedWriter(writer);
      String line;
      // write preamble
      // readLine may throw IOException 
      writer.write(INKSCAPE_PREAMBLE);
      // first two lines: write as read 
      line = bufferedReader.readLine();
      writer.write(line + "\n");
      line = bufferedReader.readLine();
      writer.write(line + "\n");

      // third line must be changed. 
      line = bufferedReader.readLine();
      line = line.replace(
        bareFileName + epsSuffix + "' (pdf, eps, ps)",
        bareFileName + ".pdf/eps/ps'\n");
      writer.write(line);

      // readLine may throw IOException
      // TBD: eliminate magic numbers 
      for (int idx = 4; idx < 56; idx++) {
        line = bufferedReader.readLine();
        writer.write(line + "\n");
      }

      // readLine may throw IOException 
      line = bufferedReader.readLine();
      line = line.replace(
          bareFileName + epsSuffix + "}}%",
          bareFileName + "}}%\n");
      writer.write(line);

      line = bufferedReader.readLine();
      do {
        writer.write(line + "\n");
        // readLine may thr. IOException
        line = bufferedReader.readLine();
      } while (line != null);
    } catch (IOException e) {
      if (bufferedReader == null) {
        // Here, FileNotFoundException on srcFile
        this.log.error(
            "EFU07: File '" + srcFile + "' to be filtered cannot be read. ");
        return;
      }
      if (writer == null) {
        this.log.error("EFU08: Destination file '" + destFile
            + "' for filtering cannot be written. ");
        return;
      }
      this.log.error("EFU09: Cannot filter file '" + srcFile + "' into '"
          + destFile + "'. ");
    } finally {
      // Here, an IOException may have occurred 
      // may log warning WFU03
      // TBD: what if null? 
      closeQuietly(bufferedReader);
      closeQuietly(writer);
    }
  }

  /**
   * Return the name of the given file without the suffix. 
   * If the suffix is empty, this is just the name of that file. 
   *
   * @see #getSuffix(File)
   */
  static String getFileNameWithoutSuffix(File file) {
    String nameFile = file.getName();
    int idxDot = nameFile.lastIndexOf(".");
    return idxDot == -1 ? nameFile : nameFile.substring(0, idxDot);
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
  // LatexPreProcessor.clearCreated(DirNode) 
  // FIXME: problem if filename starts with . and has no further . 
  // then we have a hidden file and the suffix is all but the . 
  // This is not appropriate. 
  // One may ensure that this does not happen via an assertion 
  // and by modifying getFilesRec in a way that hidden files are skipped 
  static String getSuffix(File file) {
    return getSuffix(file, true);
  }

  static String getSuffix(File file, boolean withDot) {
    String nameFile = file.getName();
    int idxDot = nameFile.lastIndexOf(".");
    if (idxDot == -1) {
      return "";
    }
    if (!withDot) {
      idxDot++;
    }
    return nameFile.substring(idxDot, nameFile.length());
  }

  // logFile may be .log or .blg or something 
  /**
   * Returns whether the given file <code>file</code> (which shall exist) 
   * contains the given pattern <code>pattern</code> 
   * or <code>null</code> in case of problems reading <code>file</code>. 
   * This is typically applied to log files, 
   * but also to latex-files to find the latex main files. 
   * <p>
   * Logging: 
   * WFU03 cannot close <br>
   * Note that in case <code>null</code> is returned, 
   * no error/warning is logged. 
   * This must be done by the invoking method. 
   *
   * @param file
   *    an existing proper file, not a folder. 
   * @param regex
   *    the pattern (regular expression) to look for in <code>file</code>. 
   * @return
   *    whether the given file <code>file</code> (which shall exist) 
   *    contains the given pattern <code>pattern</code>. 
   *    If the file does not exist or an IOException occurs 
   *    while reading, <code>null</code> is returned. 
   */
  // used only in 
  // LatexPreProcessor.isLatexMainFile(File)
  // LatexProcessor.needRun(...)
  // AbstractLatexProcessor.hasErrsWarns(File, String)
  // CAUTION: only in tests 
  Boolean matchInFile(File file, String regex) {
    FileMatch fileMatch = getMatchInFile(file, regex, null);// TBD: eliminate null 
    // Idea is to allow more than one group name... could be an array. 
    if (fileMatch.isFileReadable()) {
      return fileMatch.doesExprMatch();
    }
    // TBD: eliminate hack: just to avoid warnings. 
    //return null;
    return false;
  }

  FileMatch getMatchInFile(File file, String regex, String groupName) {
    Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE);//
    boolean fromStart = regex.startsWith("\\A");
    String lines = "";

    try {
      // may throw FileNotFoundException < IOExcption 
      FileReader fileReader = new FileReader(file);
      // BufferedReader for performance and to be able to read a line
      BufferedReader bufferedReader = new BufferedReader(fileReader);
      //CharBuffer chars = CharBuffer.allocate(1000);
      try {
        // may throw IOException 
        // int numRead = bufferedReader.read(chars);
        // System.out.println("file: "+file);
        // System.out.println("numRead: "+numRead);
        // System.out.println("chars: '"+chars+"'");


        // FIXME: seemingly, 
        // find may not terminate in case ^(\s*)* but with ^s* 
        // but this seems a bug in java's regex engine 
        //		return pattern.matcher(chars).find();


        // readLine may throw IOException 
        for (String line = bufferedReader.readLine(); line != null;
            // readLine may thr. IOException
            line = bufferedReader.readLine()) {
          // FIXME: linewise matching is not appropriate 
          // for further patterns line patternReRunLatex 
          // FIXME: seemingly, find may not terminate in case ^(\s*)* but with ^s* 
          // but this seems a bug in java's regex engine 

          lines = fromStart ? lines += "\n" + line : line;
          Matcher matcher = pattern.matcher(lines);
          if (matcher.find()) {
            // if (groupName != null) {
            //   return FileMatch.matches(matcher.group(groupName));
            // }
            // return FileMatch.matches(true);
            return FileMatch.matches(matcher);
          }
        }
        //return FileMatch.matches(false);
        return FileMatch.noMatch();
      } catch (IOException ioe) {
        // Error/Warning must be issued by invoking method 
        return FileMatch.unreadable();
      } finally {
        // Here, an IOException may have occurred 
        // may log warning WFU03
        closeQuietly(bufferedReader);
      }
    } catch (FileNotFoundException ffe) {
      // Error/Warning must be issued by invoking method 
      return FileMatch.unreadable();
    }
  }

  /**
   * Returns the set of strings representing the <code>idxGroup</code> 
   * of the pattern <code>regex</code> matching a line 
   * in file <code>file</code> or returns <code>null</code> 
   * in case of problems reading <code>file</code>. 
   * <p>
   * This is used only to collect the identifiers 
   * of explicitly given indices in an idx-file. 
   * 
   * <p>
   * Logging:
   * WFU03 cannot close <br>
   * 
   * @param file
   *    an existing proper file, not a folder. 
   *    In practice this is an idx file. 
   * @param regex
   *    the pattern (regular expression) to look for in <code>file</code>. 
   * @param idxGroup
   *    the number of a group of the pattern <code>regex</code>. 
   * @return
   *    the set of strings representing the <code>idxGroup</code> 
   *    of the pattern <code>regex</code> matching a line 
   *    in file <code>file</code> or returns <code>null</code> 
   *    in case of problems reading <code>file</code>. 
   */
  // used in LatexProcessor.runMakeIndexByNeed only 
  // **** a lot of copying from method matchInFile 
  Collection<String> collectMatches(File file, String regex, int idxGroup) {
    Collection<String> res = new TreeSet<String>();
    Pattern pattern = Pattern.compile(regex);

    try {
      // may throw FileNotFoundException < IOExcption 
      FileReader fileReader = new FileReader(file);
      // BufferedReader for performance 
      BufferedReader bufferedReader = new BufferedReader(fileReader);

      try {
        // readLine may throw IOException 
        Matcher matcher;
        for (String line = bufferedReader.readLine(); line != null;
            // readLine may thr. IOException
            line = bufferedReader.readLine()) {

          matcher = pattern.matcher(line);
          if (matcher.find()) {
            // Here, a match has been found 
            res.add(matcher.group(idxGroup));
          }
        } // for 

        return res;
      } catch (IOException ioe) {
        // Error/Warning must be issued by invoking method 
        return null;
      } finally {
        // Here, an IOException may have occurred 
        // may log warning WFU03
        closeQuietly(bufferedReader);
      }
    } catch (FileNotFoundException ffe) {
      // Error/Warning must be issued by invoking method 
      return null;
    }
  }

  // used in LatexPreProcessor and in LatexProcessor and in LatexDec
  // at numerous places 
  // TBD: clarify what is wrong with mock that we cannot make this static
  static File replaceSuffix(File file, String suffix) {
    return new File(file.getParentFile(),
        getFileNameWithoutSuffix(file) + suffix);
  }

  // is assumed to be without suffix
  static File replacePrefix(String prefix, File file) {
    return new File(file.getParentFile(), prefix + file.getName());
  }

  static File appendSuffix(File file, String suffix) {
    return new File(file.getParentFile(), file.getName() + suffix);
  }


  /**
   * Deletes all files in the same folder as <code>pFile</code> directly, 
   * i.e. not in subfolders, which are accepted by <code>filter</code>. 
   * <p>
   * Logging: 
   * <ul>
   * <li> WFU01: Cannot read directory...
   * <li> EFU05: Failed to delete file 
   * </ul>
   *
   * @param pFile
   *    a file in a folder to be deleted from. 
   *    This is either a metapost file or a latex main file. 
   * @param filter
   *    a filter which decides which files 
   *    from the parent directory of <code>pFile</code> to delete. 
   * @param allowsDirs
   *    Whether deletion also allows directories. 
   */
  // used in LatexPreProcessor.clearTargetMp
  // used in LatexPreProcessor.clearTargetTex only 
  void deleteX(File pFile, FileFilter filter, boolean allowsDirs) {
    // FIXME: not true for clear target. 
    // Required: cleanup in order reverse to creation. 
    assert pFile.exists()
        && !pFile.isDirectory() : "Expected existing (regular) file " + pFile;
    File dir = pFile.getParentFile();
    // may log warning WFU01 
    File[] found = listFilesOrWarn(dir);
    if (found == null) {
      // Here, logging WFU01 already done 
      return;
    }
    for (File delFile : found) {
      // FIXME: not true for clear target. 
      // Required: cleanup in order reverse to creation. 
      assert delFile.exists();
      if (filter.accept(delFile)) {
        assert delFile.exists() && (!delFile.isDirectory()
            || allowsDirs) : "Expected existing (regular) file " + delFile;
        // may log EFU05: failed to delete 
        deleteOrError(delFile, allowsDirs);
      }
    }
  }

  /**
   * Deletes <code>delFile</code> or logs a warning. 
   * <p>
   * Logging: 
   * EFU05: failed to delete 
   *
   * @param delFile
   *    the existing file to be deleted. 
   *    This must not be a directory. 
   * @param allowsDirs
   *    Whether deletion also allows directories. 
   */
  void deleteOrError(File delFile, boolean allowsDirs) {
    assert delFile.exists() && (!delFile.isDirectory() || allowsDirs)
      : "Expected existing (regular) file " + delFile;
    if (!FileUtils.deleteQuietly(delFile)) {
      this.log.error("EFU05: Cannot delete file '" + delFile + "'. ");
    }
  }

  /**
   * Moves file <code>fromFile</code> to <code>toFile</code> 
   * or logs a warning. 
   * <p>
   * Logging: 
   * EFU06: failed to move. 
   *
   * @param fromFile
   *    the existing file to be moved. 
   *    This must not be a directory. 
   * @param toFile
   *    the file to be moved to 
   *    This must not be a directory. 
   */
  void moveOrError(File fromFile, File toFile) {
    assert fromFile.exists()
        && !fromFile.isDirectory() : "Expected existing (regular) source file "
            + fromFile;
    assert !toFile.isDirectory() : "Expected (regular) target file " + toFile;
    boolean success = fromFile.renameTo(toFile);
    if (!success) {
      this.log.error(
          "EFU06: Cannot move file '" + fromFile + "' to '" + toFile + "'. ");
    }
  }

  /**
   * Deletes all files in <code>texDir</code> including subdirectories 
   * which are not in <code>orgNode</code>. 
   * The background is, that <code>orgNode</code> represents the files 
   * originally in <code>texDir</code>. 
   * <p>
   * Logging: 
   * <ul>
   * <li> WFU01: Cannot read directory 
   * <li> EFU05: Cannot delete... 
   * </ul>
   *
   * @param orgNode
   *    
   * @param texDir
   *    
   */
  // used in LatexProcessor.create() only 
  // FIXME: warn if deletion failed. 
  void cleanUp(DirNode orgNode, File texDir) {
    // constructor DirNode may log warning WFU01 Cannot read directory 
    // cleanUpRec may log warning EFU05 Cannot delete... 
    cleanUpRec(texDir, orgNode, new DirNode(texDir, this));
  }

  /**
   * Deletes all files in <code>currNode</code> 
   * which are not in <code>orgNode</code> recursively 
   * including subdirectories. 
   * The background is, that <code>orgNode</code> represents the files 
   * originally in the directory and <code>currNode</code> 
   * the current ones at the end of the creating goal. 
   * <p>
   * Logging: 
   * EFU05: Cannot delete... 
   *
   * @param dir
   *    the directory where to cleanup. 
   * @param origNode
   *    the node representing the original files. 
   *    This is the latex source directory or a subdirectory. 
   * @param currNode
   *    the node representing the current files. 
   *    This is the latex source directory or a subdirectory. 
   */
  // used in cleanUp only 
  private void cleanUpRec(File dir, DirNode origNode, DirNode currNode) {
    Set<String> origSubdirs = origNode.getSubdirs().keySet();
    Set<String> currSubdirs =
        new TreeSet<String>(currNode.getSubdirs().keySet());
    boolean containsAll = currSubdirs.containsAll(origSubdirs);
    assert containsAll;
    currSubdirs.removeAll(origSubdirs);
    Set<String> diffSet = currSubdirs;
    String regex = PREFIX_PYTEX_OUT_FOLDER + ".+";// represents file name
    for (String name : diffSet) {
      assert name.matches(regex);
      System.out.println("del: " + new File(dir, name));
      deleteOrError(new File(dir, name), true);
    }
    File file;
    for (String key : origNode.getSubdirs().keySet()) {
      file = new File(dir, key);
      cleanUpRec(file, origNode.getSubdirs().get(key),
          currNode.getSubdirs().get(key));
    }
    Collection<String> currFileNames = currNode.getRegularFileNames();
    currFileNames.removeAll(origNode.getRegularFileNames());

    for (String fileName : currFileNames) {
      file = new File(dir, fileName);
      // may log error EFU05: Cannot delete file
      deleteOrError(file, false);
    }
  }

  // TBD: clarify whether this hack is really needed.
  /**
   * Temporarily generated file to be passed to {@link Converter#Makeindex}
   * to allow to determine the version of the tool.
   */
  private static File EMPTY_IDX;

  // unsorted and not unified index created by latex
  final static String SUFFIX_IDX = ".idx";
  // sorted and unified index created by makeindex
  final static String SUFFIX_IND = ".ind";
  // log file created by makeindex
  final static String SUFFIX_ILG = ".ilg";



  static File getEmptyIdx() {
    if (EMPTY_IDX == null) {
      try {
        EMPTY_IDX = File.createTempFile("forMakeindex", SUFFIX_IDX);
        EMPTY_IDX.deleteOnExit();
        replaceSuffix(EMPTY_IDX, SUFFIX_IND).deleteOnExit();
        replaceSuffix(EMPTY_IDX, SUFFIX_ILG).deleteOnExit();
      } catch (Exception e) {
        // TBD: eliminate: shall be a warning or even less than that.
        // But to that end, this must not be static. 
        throw new IllegalStateException("Could not create temp file.");
      }
    }
    return EMPTY_IDX;
  }


  /**
   * The part of the headline of generated files for injections 
   * after the comment symbol. 
   * Used e.g. 
   * for <code>.latexmkrc</code> and for <code>.chktex</code>. 
   * This headline signifies, 
   * that the file was created by this software. 
   * As a consequence, 
   * it may be deleted or overwritten by this software. 
   * Else this is not done. 
   * Note that the headline is the first line, 
   * except the file has a shebang like code>.latexmkrc</code>. 
   * Since the shebang must be in the first line and must be preserved, 
   * the headline is the second line if a shebang is present. 
   */
  static final String HEADLINE_GEN = " injection file written by latex plugin ";

  // Could be in LatexProcessor or here in TexFileUtils. 
  // Since it does logging and LatexProcessor does none so far 
  // and since this fits layered architecture, we decided to put it here. 
  /**
   * Returns whether the given file is created by this software. 
   * This is assumed if the comment character followed by {@link #HEADLINE_GEN} 
   * is the first line if no shebang is expected else the second line. 
   * It is assumed that the file exists. 
   * 
   * Warnings: 
   * WFU10: if the file has not been created by this software 
   * WFU11: if it cannot be ensured that the file has been created by this software 
   * or if the reader to read to decide cannot be closed. 
   *
   * @param aFile
   *   the file to be considered. 
   * @param inj
   *   the injection for which the file is created. 
   *   What is used is merely the comment character and whether there is a shebang line. 
   * @return
   *   whether the given file is created by this software. 
   *   This is assumed if it can be proved that 
   *   the first line starts with the comment symbol 
   *   followed by {@link #HEADLINE_GEN}, 
   *   except if the file has a shebang in the first line, 
   *   then the second line takes the role of the headline. 
   *   If and only if false, an warning is emitted. 
   */
  boolean isCreatedByMyself(File aFile, Injection inj) {
    assert aFile.exists() : "File " + aFile + " expected to exist. ";
    // to be checked whether it shall be overwritten 
    try {
      if (!aFile.isDirectory()) {
        // constructor of FileReader may throw 
        // FileNotFoundException which is an IOException: 
        // Since it does exist and is not a directory, 
        // it is unreadable for some other reason 
        BufferedReader reader =
            new BufferedReader(new FileReader(aFile));
        // TBD: treat IOException better 
        // may throw IOException if an IO error occurs 
        String headline = reader.readLine();
        if (inj.hasShebang()) {
          headline = reader.readLine();
        }
        // may throw IOException 
        reader.close();
        // headline is null iff the aFile is empty 
        if (headline != null && headline.startsWith(inj.commentStr() + HEADLINE_GEN)) {
          return true;
        }
      }
      // Here, the file was not written by this software 
      // so it shall not be overwritten 
      this.log.warn("WFU10: Cannot overwrite/clean file '" + aFile
          + "' because it is not self-created. ");
      return false;

    } catch (IOException ioe) {
      // In both cases: could not read headline 
      this.log.warn("WFU11: Refuse to overwrite/clean file '" + aFile
          + "' because it may be not self-created or has dangling reader. ");
      return false;
    }
  }



  public static void main(String[] args) {
    String regex = args[0];
    String text = args[1];
    text = "xx\nyzzz";
    System.out.println("regex: " + regex);
    System.out.println("text: " + text);
    System.out.println("len: " + text.length());


    Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE);
    java.util.regex.Matcher matcher = pattern.matcher(text);
    matcher.useAnchoringBounds(true);
    System.out.println("find:   " + matcher.find());
    System.out.println("hitEnd: " + matcher.hitEnd());
    System.out.println("hitEnd: " + matcher.end());
  }
}
