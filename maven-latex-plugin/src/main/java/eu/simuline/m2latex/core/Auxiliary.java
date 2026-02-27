
package eu.simuline.m2latex.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import com.florianingerl.util.regex.Matcher;
import com.florianingerl.util.regex.Pattern;


/**
 * Very bad name: 
 * Represents some aspect of postprocessing 
 * performed in {@link LatexProcessor#processLatex2devCore(LatexMainDesc, LatexDev)}. 
 * No it is auxiliary processing, 
 * because it is alternating auxiliary tools and latex compiler. 
 * <p>
 * Auxiliary processing consists of two parts: 
 * <ul>
 * <li>the run of a latex compiler, typically via a package loaded, 
 * writes (into) a file. 
 * This may be a specific file or a file common for more auxiliary processings. 
 * At time of this writing this is the AUX file. 
 * In that case, the latex run writes lines into the file 
 * which are specific for the auxiliary processing. 
 * <li>the subsequent run of an auxiliary program, 
 * triggered if that specific file is present, 
 * or if the specific lines in a common file are present. 
 * The auxiliary program reads the lines of that file 
 * and writes some file, typically another one, 
 * which is read by the next run of the latex compiler. 
 * </ul>
 * 
 * As described above, the initial run of the latex compiler 
 * writes (lines into) a file, 
 * the presence of which triggers invocation of the corresponding auxiliary program 
 * which writes some file which is read by the next run of the latex compiler. 
 * If the compiler writes a different file, 
 * of course the auxiliary program must be rerun and so on. 
 * There are really cases where this does not terminate 
 * so to ensure termination a maximal number of runs of the latex compiler is specified. 
 * <p>
 * Creating a bibliography with <code>bibtex</code> or equivalent, 
 * is one of the auxiliary processings {@link #BibTex}. 
 * It is a bit special in that the latex compiler does not need a special package 
 * to write the bibliographical information into the AUX file. 
 * The auxiliary program <code>bibtex</code> or that like extracts and sorts the entries 
 * into a BBL file which is read back by the latex compiler 
 * to create the bibliography. 
 * CAUTION: the lines of the AUX file read by <code>bibtex</code> 
 * may be altered by subsequent calls of a latex compiler or of an auxiliary program 
 * as shown by https://tex.stackexchange.com/questions/724138/is-backreference-possible-with-bibtex-maybe-in-conjunction-with-biblatex. 
 * <p>
 * At time of this writing, bibliography with package <code>biblatex</code> 
 * in conjunction with auxiliary program <code>biber</code> is not yet supported. 
 * <p>
 * Writing an index is only supported via package <code>idxindex</code> 
 * and auxiliary program <code>makeindex</code>. 
 * This includes <code>splitindex</code> for multiple indices 
 * invoking <code>makeindex</code> internally. 
 * This is {@link #Idx}. 
 * Clearly a subsequent run of the latex compiler changes the index 
 * if the index entry moves to another page number. 
 * <p>
 * At time of this writing, indices with package <code>???</code> 
 * in conjunction with auxiliary program <code>xindy</code> is not yet supported. 
 * 
 */
enum Auxiliary {

  /**
   * Bibliography processing with bibtex or related. 
   * TBC: in conjunction with biblatex package also? 
   * Maybe this shall not be included, because no rerun needed. 
   * Then the process we want to introduce is superluous. 
   */
  BibTex {
    // CAUTION: it is not .bib, it is .aux. 
    // TBD: it is only part of .aux and also: 
    // for includes the files referenced therein are also to be taken into account. 
    String extension() {
      return ".aux";
    }

    boolean doesFitAuxiliary(File file) {
      return doesFitAuxiliary(file, PATTERN_NEED_BIBTEX_RUN);
    }


    int numRunsAfter() {
      return 2;
    }

    boolean process(LatexMainDesc desc, LatexProcessor proc)
        throws BuildFailureException {
      return proc.runBibtex(desc);
    }

    /**
     * Takes not only the AUX file <code>file</code> 
     * into account, but also files included 
     * via command {@link #PATTERN_INPUT}. 
     */
    FileId getIdent(File file) throws IOException {
      //System.out.println("update:Bibtex");
      return updateIdent(file, new FileId(), PATTERN_BIBTEX);
    }


  },
  // /**
  //  * Bibliography processing with biblatex and biber. 
  //  * TBC: is this really the sole case with backrefence 
  //  * which may force rerun? 
  //  */
  // BibEr {
  //   // CAUTION: it is not .bib, it is 
  //   String extension() {
  //     return ".bcf";
  //   }
  //},
  // TBD: index with xindy 

  /**
   * Index processing with makeindex and possibly splitindex. 
   */
  Idx {
    String extension() {
      return ".idx";
    }

    boolean process(LatexMainDesc desc, LatexProcessor proc)
        throws BuildFailureException {
      return proc.runMakeSplitIndex(desc);
    }
  },
  Glo {
    String extension() {
      return ".glo";
    }

    boolean process(LatexMainDesc desc, LatexProcessor proc)
        throws BuildFailureException {
      return proc.runMakeGlossary(desc);
    }
  },
  Pyt {
    String extension() {
      return ".pytxcode";
    }

    boolean mayBeEntryInToc() {
      return false;
    }

    boolean process(LatexMainDesc desc, LatexProcessor proc)
        throws BuildFailureException {
      return proc.runPythontex(desc);
    }
  };

  // maybe this is bad performance, scanning twice 
  /**
   * The pattern signifying bibdata 
   * indicating that <code>bibtex</code> must be run. 
   */
  private static final Pattern PATTERN_NEED_BIBTEX_RUN =
      Pattern.compile("^\\\\bibdata");

  private static final Pattern PATTERN_NEED_MAKEGLOSSARIES_RUN =
      Pattern.compile("^\\\\@istfilename");

  // filename with .aux
  /**
   * The pattern for iputting another AUX file 
   * with a group with name {@link #GRP_INPUT} 
   * comprising the name of the file with ending. 
   * 
   */
  private static final Pattern PATTERN_INPUT =
      Pattern.compile("^\\\\@input\\{(?<fileName>.*)\\}");

  /**
   * The name of the group in pattern {@link #PATTERN_INPUT} 
   * comprising the filename with ending. 
   */
  private static final String GRP_INPUT = "fileName";

  /**
   * The pattern of the aux file 
   * read by <code>bibtex</code>. 
   * This does not include 
   * the pattern {@link #PATTERN_INPUT} 
   * for inputting other aux files. 
   * In fact only those patterns contributing to hashing. 
   */
  static final Pattern PATTERN_BIBTEX =
      Pattern.compile("^\\\\(citation|bibstyle|bibdata)");

  /**
   * The file extension <code>.ext</code> triggering the action. 
   * If <code>xxx.tex</code> is the latex main file, 
   * then <code>xxx.ext</code> is the triggering file. 
   * This file is created fromt the TEX file, 
   * in some cases due to some package. 
   * 
   * @return
   *    The extension (with dot) as a string. 
   */
  // TBD: without dot would be more elegant 
  abstract String extension();

  boolean doesFitAuxiliary(File file) {
    return file.exists();
  }

  // currently true except for Pyt 
  boolean mayBeEntryInToc() {
    return true;
  }

  // must be non-negative 
  int numRunsAfter() {
    return 1;
  }

  /**
   * Try to process the file associated with this auxiliary. 
   * @return
   *    whether a processor has been invoked. 
   */
  abstract boolean process(LatexMainDesc desc, LatexProcessor proc)
      throws BuildFailureException;

  // overwritten for bibtex and one time also for bib2gls
  /**
   * Yields the identifier 
   * corresponding with the given text file. 
   * It takes only the lines into account 
   * which are relevant for this this {@link Auxiliary}. 
   * Note that for some {@link Auxiliary}s, 
   * typically tied to AUX files, 
   * other files referred to are taken into account 
   * as for {@link Auxiliary#BibTex}
   * 
   * @param file
   *   a text file written by the part of the {@link Auxiliary} 
   *   given by the run of the latex compiler 
   *   and at least partially read by the corresponding auxiliary program. 
   * @return
   *   The file identifier taking all lines of <code>file</code> into account 
   *   which are relevant for this {@link Auxiliary}. 
   * @throws IOException
   *    if the file or files referred to within it 
   *    could not be read completely. 
   *    This degrades rerun detection. 
   */
  FileId getIdent(File file) throws IOException {
    return updateIdent(file, new FileId());
  }

  FileId updateIdent(File file, FileId fileId) throws IOException {
    //System.out.println("update:gen");
    try (BufferedReader bufferedReader =
        new BufferedReader(new FileReader(file))) {
      for (String line = bufferedReader.readLine(); line != null;
          // readLine may thr. IOException
          line = bufferedReader.readLine()) {
        fileId.update(line);
      }
    }
    return fileId;
  }

  FileId updateIdent(File file, FileId fileId, Pattern patternAux) throws IOException {
    File parent = file.getParentFile();
    String inFile;
    try (BufferedReader bufferedReader =
        new BufferedReader(new FileReader(file))) {
      for (String line = bufferedReader.readLine(); line != null;
          // readLine may thr. IOException
          line = bufferedReader.readLine()) {
        if (patternAux.matcher(line).find()) {
          fileId.update(line);
          continue;
        }
        Matcher matcher = PATTERN_INPUT.matcher(line);
        if (matcher.find()) {
          inFile = matcher.group(GRP_INPUT);
          assert inFile.endsWith(this.extension());
          // ignore return value 
          updateIdent(new File(parent, inFile), fileId, patternAux);
        }
      } // for 
    } // try 
    return fileId;
  }

  boolean doesFitAuxiliary(File file, Pattern pattern) {
    if (!file.exists()) {
      return false;
    }

    try (BufferedReader bufferedReader =
        new BufferedReader(new FileReader(file))) {
      for (String line = bufferedReader.readLine(); line != null;
          // readLine may thr. IOException
          line = bufferedReader.readLine()) {
        if (pattern.matcher(line).find()) {
          return true;
        }
      } // for 
      return false;
    } catch (IOException e) {
      // TBD: add warning 
      return true;
    }
  }

}
