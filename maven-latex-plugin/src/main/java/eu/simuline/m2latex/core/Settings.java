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
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

import java.util.Map;
import java.util.LinkedHashMap;
// import java.util.TreeMap;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.HashSet;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// import java.lang.annotation.Annotation;

import org.apache.maven.plugins.annotations.Parameter;
// import org.apache.maven.plugin.descriptor.Parameter;

// is AbstractLatexMojo but not public
import eu.simuline.m2latex.mojo.CfgLatexMojo;// for javadoc only

/**
 * The settings for a maven plugin and for an ant task.
 * These are the elements of the maven pom in element <code>settings</code>
 * and accordingly for the ant build file.
 * <p>
 * For the options we have the contract,
 * that in the initial value they are trimmed and separated by a single blank.
 * The setter methods are so that before setting the new value is trimmed
 * and multiple whitespaces are replaced by a single blank.
 */
public class Settings {

  // static initializer 

  /**
   * On unix <code>src/site/tex</code>, 
   * on other operating systems accordingly. 
   */
  final static String SST;

  static {
    String fs = System.getProperty("file.separator");
    SST = "src" + fs + "site" + fs + "tex";
  }

  // readonly parameters 

  /**
   * The base directory of this maven project. 
   * This shall be set only once through {@link #setBaseDirectory(File)} 
   * in {@link eu.simuline.m2latex.mojo.AbstractLatexMojo#initialize()}. 
   * TBD: clarify: what about ant task? 
   * TBD: improve design here. 
   *
   * @see CfgLatexMojo#baseDirectory
   */
  private File baseDirectory;

  /**
   * The target directory of this maven project. 
   * By default this is <code>{@link #baseDirectory}/target</code> 
   * on Unix systems. 
   *
   * @see CfgLatexMojo#targetDirectory
   */
  private File targetDirectory;

  /**
   * The target site directory of this maven project. 
   * By default this is <code>{@link #targetDirectory}/site</code> 
   * on Unix systems. 
   *
   * @see CfgLatexMojo#targetSiteDirectory
   */
  private File targetSiteDirectory;

  // read/write parameters and related. 
  // If a parameter represents a relative path, this is a string 
  // and there is an according field of type File. 

  /**
   * The latex source directory as a string 
   * relative to {@link #baseDirectory}, 
   * containing {@link #texSrcProcDirectory}. 
   * This directory determines also the subdirectory of 
   * {@link #outputDirectory} to lay down the generated artifacts. 
   * The according file is given by {@link #texSrcDirectoryFile}. 
   * The default value is {@link #SST}. 
   */
  @RuntimeParameter
  @Parameter(name = "texSrcDirectory",
      defaultValue = "src${file.separator}site${file.separator}tex")
  private String texSrcDirectory = SST;

  /**
   * File for {@link #texSrcDirectory} based on {@link #baseDirectory}. 
   */
  private File texSrcDirectoryFile =
      new File(this.baseDirectory, this.texSrcDirectory);

  /**
   * The latex source processing directory as a string 
   * relative to {@link #texSrcDirectory}
   * containing all tex main documents 
   * and the graphic files to be processed 
   * and also to be cleaned. 
   * Whether this is done recursively in subfolders 
   * is specified by {@link #readTexSrcProcDirRec}. 
   * The according file is given by {@link #texSrcProcDirectoryFile}. 
   * The default value is <code>.</code>, 
   * i.e. the latex souce processing directory is the latex source directory. 
   */
  @RuntimeParameter
  @Parameter(name = "texSrcProcDirectory", defaultValue = ".")
  private String texSrcProcDirectory = ".";

  /**
   * File for {@link #texSrcProcDirectory} based on {@link #texSrcDirectory}. 
   */
  private File texSrcProcDirectoryFile =
      new File(this.texSrcDirectoryFile, this.texSrcProcDirectory);

  /**
   * Whether the tex source directory {@link #texSrcProcDirectory} 
   * shall be read recursively for creation of graphic files, 
   * i.e. including the subdirectories recursively. 
   * This is set to <code>false</code> only during information development. 
   * The default value is <code>true</code>. 
   */
  // FIXME: maybe in the long run: only latex main files. 
  @RuntimeParameter
  @Parameter(name = "readTexSrcProcDirRec", defaultValue = "true")
  private boolean readTexSrcProcDirRec = true;

  /**
   * The artifacts generated by {@link #latex2pdfCommand} 
   * will be copied to this folder 
   * which is given relative to {@link #targetSiteDirectory}. 
   * The default value is <code>.</code>. 
   * The according file is given by {@link #outputDirectoryFile}. 
   *
   * @see #texSrcDirectory
   */
  @RuntimeParameter
  @Parameter(name = "outputDirectory", defaultValue = ".")
  private String outputDirectory = ".";

  /**
   * File for {@link #outputDirectory} based on {@link #targetSiteDirectory}. 
   */
  private File outputDirectoryFile =
      new File(this.targetSiteDirectory, this.outputDirectory);

  /**
   * Diff directory relative to {@link #baseDirectory} 
   * used for diffing actually created artifact against prescribed one inthis directory. 
   * This is relevant only if {@link #chkDiff} is set. 
   * The according file is given by {@link #diffDirectoryFile}. 
   * The default value is <code>.</code>. 
   */
  @RuntimeParameter
  @Parameter(name = "diffDirectory", defaultValue = ".")
  private String diffDirectory = ".";

  /**
   * File for {@link #diffDirectory} based on @link #baseDirectory}. 
   */
  private File diffDirectoryFile =
      new File(this.baseDirectory, this.diffDirectory);

  /**
   * A comma separated list of targets without blanks 
   * returned as a set by {@link #getTargets()}. 
   * For allowed values see {@link Target}. 
   * <p>
   * Independent of the order given, the given targets are created 
   * in an internal ordering. 
   * <p>
   * Caution: These targets are the default targets for any latex main file, 
   * but depending on the document class, there may be further restrictions 
   * given by {@link #docClassesToTargets}. 
   * Currently, only the class <code>beamer</code> has restrictions. 
   * <p>
   * The default value is <code>chk,pdf,html</code>. 
   */
  @RuntimeParameter
  @Parameter(name = "targets", defaultValue = "chk,pdf,html",
      property = "latex.targets") //
  //private SortedSet<Target> targets;
  private String targets = "chk,pdf,html";
  // TBD: clarify why the following initialization causes that no goal descriptors are found. 
  // = new TreeSet<Target>(Arrays.asList(new Target[] {Target.chk, Target.pdf, Target.html}));
  // TBD: publish that giving the default value in the annotation works only within the lifecycle. 
  // In contrast, if running on the command line as mvn latex:cfg, this does not work: no init: nullpointer. 
  // maybe then we need as latex.targets 
  // TBD: clarify whether it isn't better to specify the init value by annotation
  // In old times targets was just a string and conversion to enum set was done internally. 
  // TBD: clarify why latex.targets does not work either. 
  // Thus the user cannot command `mvn latex:cfg -Dlatex.targets=pdf`: also nullpointer 
  // maybe because inside the Settings. 

  /**
   * A comma separated list of excluded {@link Converter}s 
   * given by their command, i.e. by {@link Converter#getCommand()}
   * returned as a set by {@link #getConvertersExcluded()}. 
   * Excluded converters need not be installed but their names must be known. 
   * They don't show up in the version check of target 'vrs' 
   * and of course they are not allowed to be used. 
   * By default, this list is empty. 
   */
  @RuntimeParameter
  @Parameter(name = "convertersExcluded", defaultValue = "")
  private String convertersExcluded = "";

  /**
   * The pattern to be applied to the beginning of the contents of TEX-files 
   * which identifies a latex main file and which extracts the document class 
   * if the file is really a latex main file. 
   * The default value is chosen to match quite exactly the start of 
   * the latex main files. 
   * Here we assume that the latex main file should contain 
   * the declaration `\documentclass' 
   * or the old fashioned `\documentstyle' 
   * preceeded by a few constructs and followed by the documen class. 
   * <p>
   * Strictly speaking, a tight match is not necessary, 
   * only separation of latex main files from other files is 
   * and so is extraction of the document class. 
   * For a more thorough discussion, 
   * and for an alternative approach, consult the manual. 
   * <p>
   * Since the pattern is chosen 
   * according to documentation collected from the internet, 
   * one can never be sure whether the pattern is perfect. 
   * <p>
   * If the current default value is not appropriate, 
   * please overwrite it in the configuration 
   * and notify the developer of this plugin of the deficiency. 
   * In any case, matching of the group named <code>class</code> must be retained 
   * so that the document class is matched. 
   */
  // FIXME: not only on this pattern: 
  // Matching is line by line which is inappropriate. 
  // pattern is to be applied to the start of the tex-file 
  // FIXME: I have the impression, that the concept we use is not very good. 
  // Maybe one has to use a magic comment to identify latex main files. 
  // There is a tendency to allow even more in the header with coming releases of latex 
  @RuntimeParameter
  @Parameter(name = "patternLatexMainFile")
  private String patternLatexMainFile =
  """
  \\A\
  (%! LMP( docClass=(?<docClassMagic>[^} ]+))?( targets=(?<targetsMagic>(\\p{Lower}|,)+))?\\R)?\
  (\\\\RequirePackage\\s*(\\[(\\s|\\w|,)*\\])?\\s*\\{(\\w|-)+\\}\\s*(\\[(\\d|\\.)+\\])?|\
  %.*$|\
  \\\\PassOptionsToPackage\\s*\\{\\w+\\}\\s*\\{(\\w|-)+\\}|\
  \\\\input\\s*\\{[^{}]*\\}|\
  \\s)*\
  \\\\(documentstyle|documentclass)\\s*(\\[[^]]*\\])?\\s*\\{(?<docClass>[^} ]+)\\}\
  """;
  //"\\\\newbool\\s*\\{(\\w)+\\}\\s*|" + // newbool
  //"\\\\setbool\\s*\\{(\\w)+\\}\\{(true|false)\\}\\s*|" + // newbool only with literal values 

  /**
   * Assigns to document classes their allowed {@link #targets}. 
   * The map expression is a list of chunks separated by a single blank. 
   * Each chunk is divided by a single colon 
   * in a comma separated list of document classes, 
   * and a comma separated list of targets. 
   * <p>
   * A chunk means that all given document classes are compiled for the given targets. 
   * Thus, the set of document classes may not be empty, 
   * i.e. the colon may not be at the first place of its chunk. 
   * In contrast, a colon at the last place of a chunk indicates an empty target set, 
   * meaning that documents of the given class are not processed at all. 
   * <p>
   * The document classes of the chunks may not overlap. 
   * A document of a class is compiled for a target if this is specified so by a chunk. 
   * <p>
   * As a side effect, compilation of document classes cause warnings if not registered here. 
   * The default value consists of two chunks: 
   * <ul>
   * <li><tt>article,book:chk,dvi,pdf,html,odt,docx,rtf,txt</tt> 
   * ensures that article and book allow all targets. </li>
   * <li><tt>beamer:chk,pdf,txt</tt> beamer allows mainly pdf and derived from that txt. 
   * Checking with chk does not depend on the document class. 
   * </ul>
   */
  @RuntimeParameter
  @Parameter(name = "docClassesToTargets")
  private String docClassesToTargets =
      "article,book:chk,dvi,pdf,html,odt,docx,rtf,txt beamer:chk,pdf,txt";

  /**
   * The list of names of latex main files 
   * without extension <code>.tex</code> 
   * separated by whitespace 
   * which shall be included for creating targets, 
   * except if this is empty in which cases all are included. 
   * It is assumed that the names of the latex main files 
   * do not contain whitespace. 
   * Note that leading and trailing whitespace are trimmed. 
   * Currently, 
   * names of latex main files should better have pairwise different names, 
   * even if in different directories. 
   * <p>
   * The empty string is the default, i.e. including all. 
   * 
   * @see #mainFilesExcluded
   */
  @RuntimeParameter
  @Parameter(name = "mainFilesIncluded", defaultValue = "")
  private String mainFilesIncluded = "";


  /**
   * The list of names of latex main files 
   * without extension <code>.tex</code> 
   * separated by whitespace 
   * which shall be excluded for creating targets. 
   * It is assumed that the names of the latex main files 
   * do not contain whitespace. 
   * Note that leading and trailing whitespace are trimmed. 
   * Currently, 
   * names of latex main files should better have pairwise different names, 
   * even if in different directories. 
   * <p>
   * Together with {@link #mainFilesIncluded}, 
   * this is used for document development 
   * to build the pdf of a subset of documents 
   * and e.g. because for a site one needs all documents, 
   * but with the software only the manual is shipped. 
   * The empty string is the default, i.e. excluding no file. 
   * 
   * @see #mainFilesIncluded
   */
  @RuntimeParameter
  @Parameter(name = "mainFilesExcluded", defaultValue = "")
  private String mainFilesExcluded = "";


  // texPath, commands and arguments 

  /**
   * Path to the TeX scripts or <code>null</code>. 
   * In the latter case, the scripts must be on the system path. 
   * Note that in the pom, <code>&lt;texPath/&gt;</code> 
   * and even <code>&lt;texPath&gt;    &lt;/texPath&gt;</code> 
   * represent the <code>null</code>-File. 
   * The default value is <code>null</code>. 
   */
  // TBD: clarify whether null as defaultValue works properly 
  @RuntimeParameter
  @Parameter(name = "texPath", defaultValue = "null")
  private File texPath = null;

  /**
   * Indicates whether after creating artifacts 
   * and copying them to the output directory {@link #outputDirectoryFile} 
   * the artifacts are checked by diffing them against preexisting artifacts 
   * in {@link #diffDirectoryFile} 
   * using the diff command given by {@link #diffPdfCommand}. 
   * Note that currently, only pdf files are checkd. 
   * This is <code>false</code> by default and is set to <code>true</code> only 
   * in the context of tests. 
   */
  @RuntimeParameter
  @Parameter(name = "chkDiff", defaultValue = "false")
  private boolean chkDiff = false;

  /**
   * Clean up the working directory in the end? 
   * May be used for debugging when setting to <code>false</code>. 
   * The default value is <code>true</code>. 
   */
  @RuntimeParameter
  @Parameter(name = "cleanUp", defaultValue = "true")
  private boolean cleanUp = true;

  /**
   * This pattern is applied to file names 
   * and matching shall accept all the files 
   * which were created from a latex main file <code>xxx.tex</code>. 
   * It is neither applied to directories 
   * nor to <code>xxx.tex</code> itself. 
   * It shall not comprise neither graphic files to be processed 
   * nor files created from those graphic files. 
   * <p>
   * This pattern is applied 
   * in the course of processing graphic files 
   * to decide which graphic files should be processed 
   * (those rejected by this pattern) 
   * and to log warnings if there is a risk, 
   * that graphic files to be processed 
   * are skipped or that processing a latex main file overwrites 
   * the result of graphic preprocessing. 
   * <p>
   * When clearing the tex source directory {@link #texSrcProcDirectory}, 
   * i.e. all generated files should be removed, 
   * first those created from latex main files. 
   * As an approximation, 
   * those are removed which match this pattern. 
   * <p>
   * The sequence <code>T$T</code> 
   * is replaced by the prefix <code>xxx</code>. 
   * The sequence <code>T$T</code> must always be replaced: 
   * The symbol <code>$</code> occurs as end-sign as <code>)$</code> 
   * or as literal symbol as <code>\$</code>. 
   * Thus <code>T$T</code> is no regular occurrence 
   * and must always be replaced with <code>xxx</code>. 
   * <p>
   * Spaces and newlines are removed 
   * from that pattern before matching. 
   * <p>
   * This pattern may never be ensured to be complete, 
   * because any package 
   * may create files with names matching its own patterns 
   * and so any new package may break completeness. 
   * <p>	 
   * If the current default value is not appropriate, 
   * please overwrite it in the configuration 
   * and notify the developer of this plugin of the deficiency. 
   * The default value is given below. 
   */
  @RuntimeParameter
  @Parameter(name = "patternCreatedFromLatexMain")
  private String patternCreatedFromLatexMain =
      // besides T$T.xxx, with xxx not containing ., 
      // we allow T$T.synctex.gz and T$T.out.ps 
      "^(T$T(\\.([^.]*|synctex(\\(busy\\))?(\\.gz)?|" + // synctex
          "out\\.ps|run\\.xml|\\d+\\.vrb|depytx(\\\\.tex)?)|" + // out? beamer, pythontex 
          // tex4ht creates files T$Tyy.(x)htm(l)... 
          "(-|ch|se|su|ap|li)?\\d+\\.x?html?|" +
          // ... and T$Tddx.(x)bb, T$Tddx.png and T$T-dd.svg... 
          "\\d+x\\.x?bb|" + "\\d+x?\\.png|" + "-\\d+\\.svg|" +
          // by (splitidx and) splitindex 
          // TBD: check: formerly was ...ilg)| which allows also T$T itself! 
          // If a file test.tex is a latex main file and there is a folder with the same name, 
          // then the folder is deleted even if not empty. 
          // Thus removed the trailing '|'
          "-.+\\.(idx|ind|ilg)" + ")|" + // end all patterns starting with T$T
          // created by pythontex
          "pythontex-files-T$T|" + // folders from package pythontex
          // ... and xxT$T.eps... 
          "zzT$T\\.e?ps|" +
          // ... and scripts cmsy....png 
          "(cmsy)\\d+(-c)?-\\d+c?\\.png|" +
          // The following occurs sporadic when using latexmk 
          "(pdf|xe|lua)?latex\\d+\\.fls|" +
          // Seemingly for errors 
          "texput\\.(fls|log))$";


  // parameters for graphics preprocessing 


  /**
   * The fig2dev command for conversion of fig-files 
   * into various formats. 
   * Currently only pdf combined with pdf_t is supported. 
   * Note that preprocessing one fig-file 
   * requires two invocations of {@link #fig2devCommand}, 
   * one for each part. 
   * The default value is <code>fig2dev</code>. 
   *
   * @see #fig2devGenOptions
   * @see #fig2devPtxOptions
   * @see #fig2devPdfEpsOptions
   */
  @RuntimeParameter
  @Parameter(name = "fig2devCommand", defaultValue = "fig2dev")
  private String fig2devCommand = "fig2dev";

  /**
   * The options for the command {@link #fig2devCommand} 
   * common to both output languages. 
   * For the options specific for the two output langugages 
   * <code>pdftex</code> and <code>pdftex_t</code>, 
   * see {@link #fig2devPtxOptions} and {@link #fig2devPdfEpsOptions}, 
   * respectively. 
   * The default value is the empty string. 
   * <p>
   * Possible are the following options: 
   * <ul>
   * <li><code>-D +/-rangelist</code> 
   * Export layers selectively (<code>+</code>) 
   * or exclude layers from export (<code>-</code>). 
   * E.g. -D +10,40,55:70,80  means  keep 
   * only layers 10, 40, 55 through 70, and 80.
   * <li><code>-j</code> 
   * i18n (internationalization feature)
   * <li><code>-m mag</code> 
   * Set the magnification at which the figure is rendered 
   * to <code>mag</code>.
   * The default is <code>1.0</code>. 
   * This is not usable within latex; not even <code>1.0</code>. 
   * <li><code>-s fsize</code> 
   * Set the default font size (in points) 
   * for text objects to <code>fsize</code>.
   * Refers to the latex-fonts only. 
   * <li><code>-b width</code> 
   * specify width of blank border around figure (1/72 inch). 
   * </ul>
   * Except for the option <code>-j</code>, 
   * all these options take parameters 
   * and it may make sense to use them with different parameters 
   * for the two output languages. 
   * In this case include them in 
   * {@link #fig2devPtxOptions} and in {@link #fig2devPdfEpsOptions}. 
   */
  @RuntimeParameter
  @Parameter(name = "fig2devGenOptions", defaultValue = "")
  private String fig2devGenOptions = "";

  /**
   * The options for the command {@link #fig2devCommand} 
   * specific for the output languages <code>pdftex_t</code> 
   * and <code>pstex_t</code> which are the same. 
   * Note that in addition to these options, 
   * the option <code>-L pdftex_t</code> specifies the language, 
   * {@link #fig2devGenOptions} specifies the options 
   * common for the two output langugages 
   * <code>pdftex</code> and <code>pdftex_t</code> 
   * and <code>-p xxx</code> specifies the full path 
   * of the pdf/eps-file to be included without extension. 
   * <p>
   * The default value for this option is the empty string. 
   * <p>
   * Possible options are the following: 
   * (These seem to work for tex only 
   * although according to documentation for all languages. )
   * <ul>
   * <li> options specified for {@link #fig2devGenOptions} 
   * <li> <code>-E num</code>
   * Set encoding for latex text translation 
   * (0 no translation, 1 ISO-8859-1, 2 ISO-8859-2), 
   * others allowed also, effect not clear. 
   * <li> <code>-F</code>  
   * don't set font family/series/shape, 
   * so you can set it from latex. 
   * <li> <code>-v</code>
   * Verbose mode.
   * </ul>
   */
  // Note that several options do not make sense as global options, 
  // better as individual options. 
  // Maybe it makes sense, to include those options 
  // in the fig-file and use a wrapper around fig2dev 
  // instead of fig2dev itself, 
  // which invokes fig2dev with the according options. 
  // Problem is that xfig does not support this. 
  @RuntimeParameter
  @Parameter(name = "fig2devPtxOptions", defaultValue = "")
  private String fig2devPtxOptions = "";

  /**
   * The options for the command {@link #fig2devCommand} 
   * specific for the output language <code>pdftex</code>. 
   * Note that in addition to these options, 
   * the option <code>-L pdftex</code> specifies the language and 
   * {@link #fig2devGenOptions} specifies the options 
   * common for the two output langugages 
   * <code>pdftex</code> and <code>pdftex_t</code>. 
   * The default value for this option is the empty string. 
   * <p>
   * Possible options are the following: 
   * (These seem to work specifically for pdf 
   * although according to documentation for all languages. )
   * <ul>
   * <li> options specified for {@link #fig2devGenOptions} 
   * <li> <code>-G minor[:major][unit]</code>
   * Draws a grid on the page.  
   *    e.g. "-G .25:1cm" draws a thin line every .25 cm 
   *    and a thicker line every 1 cm. 
   *    Default unit is in.  
   *    Allowable units are: 
   *    i, in, inch, f, ft, feet, c, cm, mm, and m. 
   * <li> <code>-A</code>
   * Add an ASCII (EPSI) preview.
   * <li> <code>-c</code>
   * centers the figure on the page.  (default)
   * seems not to have an effect...
   * <li> <code>-e</code>
   * puts the  figure against the edge (not centered) of the page. 
   * seems not to have an effect...
   * <li> <code>-F</code>
   * Use correct font sizes (points) instead of the traditional  size
   * <li> <code>-g color</code>
   * Use color for the background. 
   * FIXME: Not clear how to specify the color. 
   * <li> <code>-N</code>
   * Convert all colors to grayscale. (not available for latex fonts)
   * <li> <code>-n name</code>
   * Set  the /Title(xxx) of the PostScript output to <code>name</code>. 
   * without it is just the filename <code>xxx.fig</code>. 
   */
  // Note that several options do not make sense as global options, 
  // better as individual options. 
  // Maybe it makes sense, to include those options 
  // in the fig-file and use a wrapper around fig2dev 
  // instead of fig2dev itself, 
  // which invokes fig2dev with the according options. 
  // Problem is that xfig does not support this. 
  @RuntimeParameter
  @Parameter(name = "fig2devPdfEpsOptions", defaultValue = "")
  private String fig2devPdfEpsOptions = "";

  /**
   * The command for conversion of gnuplot-files 
   * into various formats. 
   * Currently only pdf (graphics) 
   * combined with pdf_t (latex-texts) is supported. 
   * The default value is <code>gnuplot</code>. 
   */
  @RuntimeParameter
  @Parameter(name = "gnuplotCommand", defaultValue = "gnuplot")
  private String gnuplotCommand = "gnuplot";

  /**
   * The options specific for {@link #gnuplotCommand}'s 
   * output terminal <code>cairolatex</code>, 
   * used for mixed latex/pdf-creation. 
   * <p>
   * Possible values are: 
   * <ul>
   * <li><code>{standalone | input}</code>
   * <li><code>{blacktext | colortext | colourtext}</code>
   * Specifies whether for text colors are taken into account or not. 
   * For all but text see separate options. 
   * <li><code>{header <header> | noheader}</code>
   * <li><code>{mono|color}</code>
   * Specifies whether colors are taken into account or not. 
   * Refers to all but text (for text see separate options)
   * <li><code>{{no}transparent} {{no}crop} {background <rgbcolor>}</code>
   * <li><code>{font <font>}</code>
   * <li><code>{fontscale <scale>}</code>
   * <li><code>{linewidth <lw>} {rounded|butt|square} {dashlength <dl>}</code>
   * <li><code>{size <XX>{unit},<YY>{unit}}</code>
   * The size of this picture. 
   * This is not usable, because it imposes deformation. 
   * Default unit is inch (<code>in</code>). 
   * </ul>
   * Note that the option <code>pdf|eps</code> 
   * of the terminal <code>cairolatex</code> is not available, 
   * because it is set internally. 
   * The default option string is empty. 
   */
  @RuntimeParameter
  @Parameter(name = "gnuplotOptions", defaultValue = "")
  private String gnuplotOptions = "";

  /**
   * The command for conversion of gnuplot-files 
   * into metapost's postscript. 
   * The default value is <code>mpost</code>. 
   */
  @RuntimeParameter
  @Parameter(name = "metapostCommand", defaultValue = "mpost")
  private String metapostCommand = "mpost";

  /**
   * The options for the command {@link #metapostCommand}. 
   * Leading and trailing blanks are ignored. 
   * A sequence of at least one blank separate the proper options. 
   * The default value comprises the following options: 
   * <ul>
   * <li><code>-interaction=nonstopmode</code> 
   * prevents metapost from stopping at the first error. 
   * <li><code>-recorder</code> 
   * makes metapost create an fls-file specifying all inputted files. 
   * <li><code>-s prologues=2</code> 
   * makes metapost create a postscript file 
   * which is viewable by ghostscript viewer.
   * </ul>
   * 
   * -debug creates intermediate files mp3mnuvD.dvi and mp3mnuvD.tex 
   * No info available about the details. 
   */
  @RuntimeParameter
  @Parameter(name = "metapostOptions",
      defaultValue = "-interaction=nonstopmode -recorder "
          + "-s prologues=2 -s outputtemplate=\"%j.mps\"")
  private String metapostOptions =
      "-interaction=nonstopmode -recorder -s prologues=2 -s outputtemplate=\"%j.mps\"";

  /**
   * The pattern is applied line by line to the log-file of mpost 
   * and matching indicates an error 
   * emitted by the command {@link #metapostCommand}. 
   * <p>
   * The default value is chosen to match quite exactly 
   * the latex errors in the log file, no more no less. 
   * Since no official documentation was found, 
   * the default pattern may be incomplete. 
   * In fact, it presupposes, that {@link #metapostOptions} 
   * does not contain `<code>-file-line-error-style</code>'.   
   * <p>
   * If the current default value is not appropriate, 
   * please overwrite it in the configuration 
   * and notify the developer of this plugin of the deficiency. 
   * The default value is `<code>(^! )</code>' (note the space). 
   */
  // FIXME: Problem with line error style 
  @RuntimeParameter
  @Parameter(name = "patternErrMPost", defaultValue = "(^! )")
  private String patternErrMPost = "(^! )";

  /**
   * The pattern is applied line by line to the log-file of mpost 
   * and matching indicates a warning 
   * emitted by the command {@link #metapostCommand}. 
   * <p>
   * This pattern may never be ensured to be complete, 
   * because any library may indicate a warning 
   * with its own pattern any new package may break completeness. 
   * Nevertheless, the default value aims completeness 
   * while be restrictive enough 
   * not to indicate a warning where none was emitted. 
   * <p>
   * If the current default value is not appropriate, 
   * please overwrite it in the configuration 
   * and notify the developer of this plugin of the deficiency. 
   * The default value is given below. 
   */
  // mpost --no-parse-first-line yields 
  // warning: mpost: unimplemented option
  @RuntimeParameter
  @Parameter(name = "patternWarnMPost", defaultValue = "^([Ww]arning: )")
  private String patternWarnMPost = "^([Ww]arning: )";

  /**
   * The command for conversion of svg-files 
   * into a mixed format FIXME, synchronize with fig2devCommand. 
   * The default value is <code>inkscape</code>. 
   */
  @RuntimeParameter
  @Parameter(name = "svg2devCommand", defaultValue = "inkscape")
  private String svg2devCommand = "inkscape";

  /**
   * The options for the command {@link #svg2devCommand} 
   * for exporting svg-figures into latex compatible files. 
   * <p>
   * The following options are mandatory: 
   * <ul>
   * <li><code>-D</code> or <code>--export-area-drawing</code> 
   * Export the drawing (not the page)
   * <li><code>--export-latex</code> 
   * Export PDF/PS/EPS without text. 
   * Besides the PDF/PS/EPS, a LaTeX file is exported,
   * putting the text on top of the PDF/PS/EPS file. 
   * Include the result in LaTeX like: \input{latexfile.tex}. 
   * Note that the latter option is necessary, 
   * to create the expected files. 
   * It is also conceivable to export text as pdf/eps 
   * </ul>
   * <p>
   * The following options are prohibited, 
   * because they are automatically added by the software 
   * or are in conflict with automatically added options: 
   * <ul>
   * <li><code>--export-filename=FILENAME</code>
   * <li><code>--export-type=type</code> 
   * <ul>
   *
   * The default value is the minimal value, 
   * <code>--export-area-drawing --export-latex</code>. 
   */
  @RuntimeParameter
  @Parameter(name = "svg2devOptions",
      defaultValue = "--export-area-drawing --export-latex")
  private String svg2devOptions = "--export-area-drawing --export-latex";

  /**
   * Whether for pixel formats like jpg and png 
   * command {@link #ebbCommand} is invoked to determine the bounding box. 
   * This is relevant, if at all, only in dvi mode. 
   * Note that the package <code>bmpsize</code> is an alternative 
   * to invoking <code>ebb</code>, 
   * which seems not to work for xelatex. 
   * Moreover, all seems to work fine with neither of these techniques. 
   * The {@link #dvi2pdfCommand} given by the default, <code>dvipdfmx</code>, 
   * seems the only which yields the picture sizes as in PDF mode 
   * which fit well. 
   * Note also that miktex does not offer neither package <code>bmpsize</code> 
   * nor <code>ebb</code>. 
   * This alone requires to switch off invocation of <code>ebb</code> by default. 
   * So the default value is <code>false</code>. 
   */
  @RuntimeParameter
  @Parameter(name = "createBoundingBoxes", defaultValue = "false")
  private boolean createBoundingBoxes = false;

  /**
   * The command to create bounding box information 
   * from jpg-files and from png-files. 
   * This is run twice: 
   * once with parameter <code>-m</code> 
   * to create <code>.bb</code>-files for driver <code>dvipdfm</code> and 
   * once with parameter <code>-x</code> 
   * to create <code>.xbb</code>-files for driver <code>dvipdfmx</code>. 
   * The default value is <code>ebb</code>. 
   */
  @RuntimeParameter
  @Parameter(name = "ebbCommand", defaultValue = "ebb")
  private String ebbCommand = "ebb";

  /**
   * The options for the command {@link #ebbCommand} 
   * except <code>-m</code> and <code>-x</code> 
   * which are added automatically. 
   * The default value is <code>-v</code> to make <code>ebb<code> verbose. 
   */
  // without -x and -m 
  @RuntimeParameter
  @Parameter(name = "ebbOptions", defaultValue = "-v")
  private String ebbOptions = "-v";

  // parameters for latex2pdf-conversion 

  /**
   * The LaTeX command to create above all a pdf-file with, 
   * but also dvi and other formats based on these. 
   * Expected values are 
   * <code>lualatex</code> <code>xelatex</code>, and <code>pdflatex</code>. 
   * <p>
   * Note that for <code>xelatex</code> dvi mode 
   * (creating xdv-files instead of dvi-files) is not supported, 
   * even not creating pdf or other formats via xdv. 
   * See also the according options {@link #latex2pdfOptions} 
   * and {@link #pdfViaDvi}. 
   * In particular, for <code>xelatex</code> 
   * this maven plugin does not allow goal <code>dvi</code> and related. 
   * Consequently, {@link #targets} may not contain any of these goals. 
   * The default value (for which this software is also tested) 
   * is <code>lualatex</code>.
   */
  @RuntimeParameter
  @Parameter(name = "latex2pdfCommand", defaultValue = "lualatex")
  private String latex2pdfCommand = "lualatex";

  // // TBD: this may well be null 
  // // used in LatexProcessor.runLatex2dev to find out whether 
  // // - xelatex (option -no-pdf) or 
  // // - lualatex or pdflatex (option -output-format=dvi or pdf)
  // // - something unknown. 
  // // depends on the 
  // Converter latex2pdfType = Converter.cmd2Conv(latex2pdfCommand);

  // enum LatexConverterType {
  //   Xdv, Dvi, Invalid;
  //   // String target2Option(Target target) {
  //   //   switch (target)
  //   // }
  // }

  /**
   * The options for the command {@link #latex2pdfCommand}. 
   * Leading and trailing blanks are ignored. 
   * The setter method {@link #setLatex2pdfOptions(String)} ensures, 
   * that exactly one blank separate the proper options. 
   * <p>
   * The default value comprises the following options: 
   * <ul>
   * <li><code>-interaction=nonstopmode</code> 
   * prevents latex from stopping at the first error. 
   * <li><code>-synctex=1</code> 
   * makes latex create a pdf file 
   * which synchronizes with an editor supporting synchtex. 
   * <li><code>-recorder</code> 
   * makes latex create an fls-file specifying all inputted files. 
   * <li><code>-shell-escape</code> 
   * allows to use write18-mechanism for shell commands (why needed?)
   * </ul>
   * Note that several options offered by some latex converters 
   * are not allowed for this software. 
   * For details consult the manual. 
   */
  // useful also: -file-line-error
  @RuntimeParameter
  @Parameter(name = "latex2pdfOptions",
      defaultValue = "-interaction=nonstopmode " + // 
          "-synctex=1 " + "-recorder " + "-shell-escape")
  private String latex2pdfOptions = "-interaction=nonstopmode " + // 
      "-synctex=1 " + "-recorder " + "-shell-escape";

  /**
   * The pattern is applied line by line to the log-file 
   * and matching indicates an error 
   * emitted by the command {@link #latex2pdfCommand}. 
   * <p>
   * The default value is chosen to match quite exactly 
   * the latex errors in the log file, no more no less. 
   * Since no official documentation was found, 
   * the default pattern may be incomplete. 
   * In fact, it presupposes, that {@link #latex2pdfOptions} 
   * does not contain `<code>-file-line-error-style</code>'.   
   * <p>
   * If the current default value is not appropriate, 
   * please overwrite it in the configuration 
   * and notify the developer of this plugin of the deficiency. 
   * The default value is `<code>(^! )</code>' (note the space). 
   */
  // FIXME: Problem with line error style 
  @RuntimeParameter
  @Parameter(name = "patternErrLatex", defaultValue = "(^! )")
  private String patternErrLatex = "(^! )";

  /**
   * The pattern is applied line by line to the log-file 
   * and matching indicates a warning 
   * emitted by the command {@link #latex2pdfCommand}, 
   * disragarding warnings on bad boxes 
   * provided {@link #debugWarnings} is set. 
   * <p>
   * This pattern may never be ensured to be complete, 
   * because any package may indicate a warning 
   * with its own pattern any new package may break completeness. 
   * Nevertheless, the default value aims completeness 
   * while be restrictive enough 
   * not to indicate a warning where none was emitted. 
   * <p>
   * If the current default value is not appropriate, 
   * please overwrite it in the configuration 
   * and notify the developer of this plugin of the deficiency. 
   * The default value is given below. 
   *
   * @see #debugBadBoxes
   */
  @RuntimeParameter
  @Parameter(name = "patternWarnLatex", defaultValue = "^(LaTeX Warning: |"
      + "LaTeX Font Warning: |" + "(Package|Class) .+ Warning: |" +
      // pdftex warning (ext4): destination with the same identifier
      // pdfTeX warning (dest): ... has been referenced ...
      // pdfTeX warning: pdflatex (file pdftex.map): cannot open font map file 
      // pdfTeX warning: Found pdf version 1.5, allowed maximum 1.4 
      // pdfTeX warning: pdflatex (file ./Carlito-Bold.pfb): glyph `index130' undefined
      "pdfTeX warning( \\((\\d|\\w)+\\))?: |" + "\\* fontspec warning: |"
      + "Missing character: There is no .* in font .*!$|"
      + "A space is missing\\. (No warning)\\.)")
  private String patternWarnLatex = "^(LaTeX Warning: |"
      + "LaTeX Font Warning: |" + "(Package|Class) .+ Warning: |"
      + "pdfTeX warning( \\((\\d|\\w)+\\))?: |" + "\\* fontspec warning: |"
      + "Missing character: There is no .* in font .*!$|"
      + "A space is missing\\. (No warning)\\.)";

  /**
   * Whether debugging of overfull/underfull hboxes/vboxes is on: 
   * If so, a bad box occurs in the last LaTeX run, a warning is displayed. 
   * For details, set $cleanUp to false, 
   * rerun LaTeX and have a look at the log-file. 
   * The default value is <code>true</code>. 
   */
  @RuntimeParameter
  @Parameter(name = "debugBadBoxes", defaultValue = "true")
  private boolean debugBadBoxes = true;

  /**
   * Whether debugging of warnings is on: 
   * If so, a warning in the last LaTeX run is displayed. 
   * For details, set $cleanUp to false, 
   * rerun LaTeX and have a look at the log-file. 
   * The default value is <code>true</code>. 
   */
  @RuntimeParameter
  @Parameter(name = "debugWarnings", defaultValue = "true")
  private boolean debugWarnings = true;

  /**
   * Whether creation of pdf-files from latex-files goes via dvi-files. 
   * <p>
   * If <code>pdfViaDvi</code> is set 
   * and the latex processor needs repetitions, 
   * these are all done creating dvi 
   * and then pdf is created in a final step 
   * invoking the command {@link #dvi2pdfCommand}. 
   * If <code>pdfViaDvi</code> is not set, 
   * latex is directly converted into pdf. 
   * <p>
   * Currently, not only conversion of latex-files is affected, 
   * but also conversion of graphic files 
   * into graphic formats which allow inclusion in the tex-file. 
   * If it goes via latex, 
   * then the formats are more based on (encapsulated) postscript; 
   * else on pdf. 
   * <p>
   * In the dvi-file for jpg, png and svg 
   * only some space is visible and only in the final step 
   * performed by $dvi2pdfCommand, 
   * the pictures are included using the bounding boxes 
   * given by the .bb or the .xbb-file. 
   * These are both created by $ebbCommand. 
   * <p>
   * Of course, the target dvi is not affected: 
   * This uses always the dvi-format. 
   * What is also affected are the tasks 
   * creating html, odt or docs: 
   * Although these are based on htlatex which is always dvi-based, 
   * the preprocessing is done in dvi or in pdf. 
   * Also the task txt is affected. 
   * <p>
   * As indicated in {@link #latex2pdfCommand}, 
   * the processor <code>xelatex</code> does not create <code>dvi</code> 
   * but <code>xdv</code> files. 
   * In a sense, the <code>xdv</code> format is an extension of <code>dvi</code>} 
   * but as for the <code>xdv</code> format there is no viewer, 
   * no way <code>htlatex</code> or other applications 
   * (except the \xelatex-internal <code>xdvidpfmx<code>) 
   * and also no according mime type, 
   * we refrained from subsumming this under ``kind of dvi''. 
   * Thus, with <code>xelatex<code> the flag {@link #pdfViaDvi} may not be set. 
   * <p>
   * The default value is <code>false</code>. 
   */
  // if false: directly 
  @RuntimeParameter
  @Parameter(name = "pdfViaDvi", defaultValue = "false")
  private boolean pdfViaDvi = false;

  /**
   * The driver to convert dvi into pdf-files. 
   * Note that this must fit the options 
   * of the packages <code>xcolor</code>, <code>graphicx</code> 
   * and, provided no autodetection, <code>hyperref</code>. 
   * Sensible values are 
   * <code>dvipdf</code>, <code>dvipdfm</code>, <code>dvipdfmx</code>, 
   * and <code>dvipdft</code> 
   * (which is <code>dvipdfm</code> with option <code>-t</code>). 
   * Note that <code>dvipdf</code> is just a script 
   * around <code>dvips</code> using <code>gs</code> 
   * but does not provide proper options; so not allowed. 
   * The default value is <code>dvipdfmx</code>. 
   */
  @RuntimeParameter
  @Parameter(name = "dvi2pdfCommand", defaultValue = "dvipdfmx")
  private String dvi2pdfCommand = "dvipdfmx";

  /**
   * The options for the command {@link #dvi2pdfCommand}. 
   * The default value is <code>-V1.7<code> specifying the pdf version to be created. 
   * The default version for pdf format for {@link #dvi2pdfCommand} is version 1.5. 
   * The reason for using version 1.7 is <code>fig2dev</code> 
   * which creates pdf figures in version 1.7 
   * and forces {@link #latex2pdfCommand} in dvi mode to include pdf version 1.7 
   * and finally {@link #dvi2pdfCommand} to use that also to avoid warnings. 
   * <p>
   * Using {@link #latex2pdfCommand} if used to create pdf directly, 
   * by default also pdf version 1.5 is created. 
   * For sake of uniformity, it is advisable to create pdf version 1.7 also. 
   * In future this will be done uniformly through <code>\DocumentMetadata</code> command. 
   * TThe default value is <code>-V1.7</code> but will in future be the empty string again. 
  
   */
  @RuntimeParameter
  @Parameter(name = "dvi2pdfOptions", defaultValue = "")
  private String dvi2pdfOptions = "-V1.7";

  /**
   * The pattern is applied line by line to the log-file 
   * and matching triggers rerunning {@link #latex2pdfCommand} 
   * if {@link #maxNumReRunsLatex} is not yet reached 
   * to ensure termination. 
   * <p>
   * This pattern may never be ensured to be complete, 
   * because any package 
   * may indicate the need to rerun {@link #latex2pdfCommand} 
   * with its own pattern any new package may break completeness. 
   * Nevertheless, the default value aims completeness 
   * while be tight enough not to trigger a superfluous rerun. 
   * <p>
   * If the current default value is not appropriate, 
   * please overwrite it in the configuration 
   * and notify the developer of this plugin of the deficiency. 
   * The default value is given below. 
   */
  // FIXME: default? to be replaced by an array of strings? **** 
  // FIXME: explicit tests required for each pattern. 
  // Not only those but all patterns. 
  // FIXME: seems a problem with the pattern spreading over two lines 
  @RuntimeParameter
  @Parameter(name = "patternReRunLatex", defaultValue =
  // general message 
  "^(LaTeX Warning: Label\\(s\\) may have changed\\. "
      + "Rerun to get cross-references right\\.$|" +
      // default message in one line for packages 
      "Package \\w+ Warning: .*Rerun( .*|\\.)$|" +
      // works for 
      // Package totcount Warning: Rerun to get correct total counts
      // Package longtable Warning: Table widths have changed. Rerun LaTeX ...
      // Package hyperref Warning: Rerun to get outlines right (old hyperref)
      // Package rerunfilecheck Warning: File `...' has changed. Rerun.
      // ... 
      // default message in two lines for packages 
      // FIXME: would require parsing of more than one line 
      "Package \\w+ Warning: .*$" + "^\\(\\w+\\) .*Rerun .*$|" +
      // works for 
      // Package natbib Warning: Citation\\(s\\) may have changed.
      // (natbib)                Rerun to get citations correct.
      // Package Changebar Warning: Changebar info has changed.
      // (Changebar)                Rerun to get the bars right
      // Package rerunfilecheck Warning: File `foo.out' has changed.
      // (rerunfilecheck)                Rerun to get outlines right"
      // (rerunfilecheck)                or use package `bookmark'.
      // but not for 
      // Package biblatex Warning: Please (re)run Biber on the file:
      // (biblatex)                test
      // (biblatex)                and rerun LaTeX afterwards. 
      //
      // messages specific to various packages 
      "LaTeX Warning: Etaremune labels have changed\\.$|" +
      // 'Rerun to get them right.' is on the next line
      //
      // from package rerunfilecheck used by other packages like new hyperref 
      // Package rerunfilecheck Warning: File `foo.out' has changed.
      "\\(rerunfilecheck\\)                Rerun to get outlines right$)"
  //  (rerunfilecheck)                or use package `bookmark'.
  )
  private String patternReRunLatex =
      // general message 
      "^(LaTeX Warning: Label\\(s\\) may have changed. "
          + "Rerun to get cross-references right\\.$|" +
          // default message in one line for packages 
          "Package \\w+ Warning: .*Rerun .*$|" +
          // works for 
          // Package totcount Warning: Rerun to get correct total counts
          // Package longtable Warning: Table widths have changed. Rerun LaTeX ...
          // Package hyperref Warning: Rerun to get outlines right (old hyperref)
          // ... 
          // default message in two lines for packages 
          "Package \\w+ Warning: .*$" + "^\\(\\w+\\) .*Rerun .*$|" +
          // works for 
          // Package natbib Warning: Citation\\(s\\) may have changed.
          // (natbib)                Rerun to get citations correct.
          // Package Changebar Warning: Changebar info has changed.
          // (Changebar)                Rerun to get the bars right
          //
          // messages specific to various packages 
          "LaTeX Warning: Etaremune labels have changed\\.$|" +
          // 'Rerun to get them right.' is on the next line
          //
          // from package rerunfilecheck used by other packages like new hyperref 
          // Package rerunfilecheck Warning: File `foo.out' has changed.
          "\\(rerunfilecheck\\)                Rerun to get outlines right$)";
  // (rerunfilecheck)                or use package `xxx'.

  /**
   * The maximal allowed number of reruns of {@link #latex2pdfCommand}. 
   * This is to avoid endless repetitions. 
   * The default value is 5. 
   * This shall be non-negative 
   * or <code>-1</code> which signifies that there is no threshold. 
   */
  @RuntimeParameter
  @Parameter(name = "maxNumReRunsLatex", defaultValue = "5")
  private int maxNumReRunsLatex = 5;

  // parameters for bibliography 

  /**
   * The BibTeX command to create a bbl-file 
   * from an aux-file and a bib-file 
   * (using a bst-style file). 
   * The default value is <code>bibtex</code>. 
   */
  @RuntimeParameter
  @Parameter(name = "bibtexCommand", defaultValue = "bibtex")
  private String bibtexCommand = "bibtex";

  // FIXME: Any parameters for bibtex? 
  // Usage: bibtex [OPTION]... AUXFILE[.aux]
  //   Write bibliography for entries in AUXFILE to AUXFILE.bbl,
  //   along with a log file AUXFILE.blg.
  // -min-crossrefs=NUMBER  include item after NUMBER cross-refs; default 2
  // -terse                 do not print progress reports
  // -help                  display this help and exit
  // -version               output version information and exit

  // how to detect errors/warnings??? 
  //Process exited with error(s)

  /**
   * The options for the command {@link #bibtexCommand}. 
   * The default value is the empty string. 
   */
  @RuntimeParameter
  @Parameter(name = "bibtexOptions", defaultValue = "")
  private String bibtexOptions = "";

  /**
   * The Pattern in the blg-file 
   * indicating that {@link #bibtexCommand} failed. 
   * The default value is chosen 
   * according to the <code>bibtex</code> documentation. 
   */
  @RuntimeParameter
  @Parameter(name = "patternErrBibtex", defaultValue = "error message")
  private String patternErrBibtex = "error message";

  /**
   * The Pattern in the blg-file 
   * indicating a warning {@link #bibtexCommand} emitted. 
   * The default value is chosen 
   * according to the <code>bibtex</code> documentation. 
   */
  @RuntimeParameter
  @Parameter(name = "patternWarnBibtex", defaultValue = "^Warning--")
  private String patternWarnBibtex = "^Warning--";

  // parameters for index 

  /**
   * The MakeIndex command to create an ind-file 
   * from an idx-file logging on an ilg-file. 
   * The default value is <code>makeindex</code>. 
   */
  @RuntimeParameter
  @Parameter(name = "makeIndexCommand", defaultValue = "makeindex")
  private String makeIndexCommand = "makeindex";

  /**
   * The options for the command {@link #makeIndexCommand}. 
   * Note that the option <code>-o xxx.ind</code> to specify the output file 
   * is not allowed because this plugin 
   * expects the output for the latex main file <code>xxx.tex</code> 
   * <code>xxx.ind</code>. 
   * Likewise, the option <code>-t xxx.ilg</code> 
   * to specify the logging file is not allowed, 
   * because this software uses the standard logging file 
   * to detect failures processing the idx-file. 
   * Also the option <code>-i</code> 
   * which specifies reading the raw index from standard input 
   * is not allowed. 
   * Specifying a style file with option <code>-s yyy.ist</code> 
   * is possible if only an index is used but no glossary. 
   * FIXME: rethink what about multiple indices. 
   * <p>
   * Note that the options specified here 
   * are also used to create glossaries. 
   * In addition for glossaries, the options 
   * <code>-s</code>, <code>-o</code> and <code>-t</code> are used. 
   * Thus also these options should not be used. 
   * The default value is the empty string. 
   * Useful options in this context are 
   * <ul>
   * <li><code>-c</code> remove blanks from index entries 
   * <li><code>-g</code> german ordering
   * <li><code>-l</code> letter ordering
   * <li><code>-r</code> without collecting index entries 
   * on 3 or more successive pages. 
   * </ul>
   */
  @RuntimeParameter
  @Parameter(name = "makeIndexOptions", defaultValue = "")
  private String makeIndexOptions = "";

  /**
   * The Pattern in the ilg-file 
   * indicating that {@link #makeIndexCommand} failed. 
   * The default value <code>(!! Input index error )</code> 
   * is chosen according to the <code>makeindex</code> documentation. 
   */
  @RuntimeParameter
  @Parameter(name = "patternErrMakeIndex",
      defaultValue = "(!! Input index error )")
  private String patternErrMakeIndex = "(!! Input index error )";

  /**
   * The Pattern in the ilg-file 
   * indicating a warning {@link #makeIndexCommand} emitted. 
   * The default value <code>(## Warning )</code> 
   * is chosen according to the <code>makeindex</code> documentation. 
   */
  @RuntimeParameter
  @Parameter(name = "patternWarnMakeIndex", defaultValue = "(## Warning )")
  private String patternWarnMakeIndex = "(## Warning )";

  /**
   * The pattern in the log-file which triggers 
   * rerunning {@link #makeIndexCommand} 
   * followed by {@link #latex2pdfCommand}. 
   * This pattern only occurs, if package <code>rerunfilecheck</code> 
   * is used with option <code>index</code>. 
   * The default value 
   * is chosen according to the package documentation. 
   * If the user finds that default value is not appropriate, 
   * (s)he is asked to contribute 
   * and to notify the developer of this plugin. 
   */
  @RuntimeParameter
  @Parameter(name = "patternReRunMakeIndex", defaultValue =
  //"^Package rerunfilecheck Warning: File `.*\\.idx' has changed\\.$" //+
  "^\\(rerunfilecheck\\) +Rerun LaTeX/makeindex to get index right\\.$")
  // FIXME: should be included the full pattern. 
  // First part works second also but not together. 
  // Also did not find any way to connect the two parts. 
  // This gives rise to the conjecture 
  // that also other patterns do not work properly. 
  private String patternReRunMakeIndex =
      //"^Package rerunfilecheck Warning: File `.*\\.idx' has changed\\.$" //+
      "^\\(rerunfilecheck\\) +Rerun LaTeX/makeindex to get index right\\.$";

  /**
   * The SplitIndex command to create ind-files 
   * from an idx-file logging on ilg-files. 
   * This command invokes {@link #makeIndexCommand}. 
   * The default value is <code>splitindex</code>. 
   */
  @RuntimeParameter
  @Parameter(name = "splitIndexCommand", defaultValue = "splitindex")
  private String splitIndexCommand = "splitindex";

  /**
   * The options for {@link #splitIndexCommand}. 
   * Here, one has to distinguish between the options 
   * processed by {@link #splitIndexCommand} 
   * and those passed to {@link #makeIndexCommand}. 
   * The second category cannot be specified here, 
   * it is already given by {@link #makeIndexOptions}. 
   * In the first category is the option <code>-m</code> 
   * to specify the {@link #makeIndexCommand}. 
   * This is used automatically and cannot be specified here. 
   * Since {@link #splitIndexCommand} is used 
   * in conjunction with package <code>splitidx</code>, 
   * which hardcodes various parameters 
   * which are the default values for {@link #splitIndexCommand} 
   * and because the option may not alter certain interfaces, 
   * the only option which may be given explicitly 
   * is <code>-V</code>, the short cut for <code>--verbose</code>. 
   * Do not use <code>--verbose</code> either for sake of portability. 
   * The default value is <code>-V</code>; it could also be empty. 
   */
  @RuntimeParameter
  @Parameter(name = "splitIndexOptions", defaultValue = "-V")
  private String splitIndexOptions = "-V";

  // parameters for glossary 

  /**
   * The MakeGlossaries command to create a gls-file 
   * from a glo-file (invoked without file ending) 
   * also taking ist-file or xdy-file 
   * into account logging on a glg-file. 
   * The default value is <code>makeglossaries</code>. 
   */
  @RuntimeParameter
  @Parameter(name = "makeGlossariesCommand", defaultValue = "makeglossaries")
  private String makeGlossariesCommand = "makeglossaries";

  /**
   * The options for the command {@link #makeGlossariesCommand}. 
   * These are the options for <code>makeindex</code> 
   * (not for {@link #makeIndexCommand}) 
   * and for <code>xindy</code> (also hardcoded). 
   * The aux-file decides on whether program is executed 
   * and consequently which options are used. 
   * <p>
   * The default value is the empty option string. 
   * Nevertheless, <code>xindy</code> is invoked as 
   * <code>xindy -L english  -I xindy -M ...</code>. 
   * With option <code>-L german</code>, this is added. 
   * Options <code>-M</code> for <code>xindy</code> 
   * <code>-s</code> for <code>makeindex</code> and 
   * <code>-t</code> and <code>-o</code> for both, 
   * <code>xindy</code> and <code>makeindex</code>. 
   */
  @RuntimeParameter
  @Parameter(name = "makeGlossariesOptions", defaultValue = "")
  private String makeGlossariesOptions = "";

  /**
   * The Pattern in the glg-file 
   * indicating that {@link #makeGlossariesCommand} failed. 
   * The default value is <code>(^\*\*\* unable to execute: )</code>. 
   * If this is not appropriate, please modify 
   * and notify the developer of this plugin. 
   */
  @RuntimeParameter
  @Parameter(name = "patternErrMakeGlossaries",
      defaultValue = "^\\*\\*\\* unable to execute: ")
  private String patternErrMakeGlossaries = "^\\*\\*\\* unable to execute: ";

  /**
   * The pattern in the glg-file 
   * indicating that running <code>xindy</code> 
   * via {@link #makeGlossariesCommand} failed. 
   * The default value is <code>(^ERROR: )</code> (note the space). 
   * If this is not appropriate, please modify 
   * and notify the developer of this plugin. 
   */
  // FIXME: This is not used. 
  @RuntimeParameter
  @Parameter(name = "patternErrXindy", defaultValue = "(^ERROR: )")
  private String patternErrXindy = "(^ERROR: )";

  /**
   * The pattern in the glg-file 
   * indicating a warning when running <code>xindy</code> 
   * via {@link #makeGlossariesCommand}. 
   * The default value is <code>(^WARNING: )</code> 
   * (note the space and the brackets). 
   * If this is not appropriate, please modify 
   * and notify the developer of this plugin. 
   */
  @RuntimeParameter
  @Parameter(name = "patternWarnXindy", defaultValue = "(^WARNING: )")
  private String patternWarnXindy = "(^WARNING: )";

  /**
   * The pattern in the log-file which triggers 
   * rerunning {@link #makeGlossariesCommand} 
   * followed by {@link #latex2pdfCommand}. 
   * This pattern only occurs, if package <code>rerunfilecheck</code> 
   * is used with option <code>glossary</code>. 
   * The default value 
   * is chosen according to the package documentation. 
   * If the user finds that default value is not appropriate, 
   * (s)he is asked to contribute 
   * and to notify the developer of this plugin. 
   */
  @RuntimeParameter
  @Parameter(name = "patternReRunMakeGlossaries", defaultValue =
  //"^Package rerunfilecheck Warning: File `.*\\.glo' has changed\\.$" +
  // FIXME: really MAKEINDEX! 
  // Problem: package glossaries redefines makeglossary 
  // which breaks this solution with rerunfilecheck 
  "^\\(rerunfilecheck\\) +Rerun LaTeX/makeindex to get glossary right\\.$")
  private String patternReRunMakeGlossaries =
      //"^Package rerunfilecheck Warning: File `.*\\.glo' has changed\\.$" +
      // FIXME: really MAKEINDEX! 
      // Problem: package glossaries redefines makeglossary 
      // which breaks this solution with rerunfilecheck 
      "^\\(rerunfilecheck\\) +Rerun LaTeX/makeindex to get glossary right\\.$";

  // parameters for pythontex

  /**
   * The Pythontex command which creates a folder <code>pythontex-files-xxx</code> 
   * with various files inside 
   * from a pytxcode-file (invoked without file ending) 
   * and logging in a plg-file. 
   * The default value is <code>pythontex</code> 
   * but as long as this does not write a log file this software really needs, 
   * we have to configure it with <code>pythontexW</code> 
   * which is a simple wrapper of <code>pythontex</code> writing a log file. 
   * CAUTION: Since <code>pythontexW</code> is not registered with this software, 
   * one has to specify it with its category as <code>pythontexW:pythontex</code>. 
   */
  @RuntimeParameter
  @Parameter(name = "pythontexCommand", defaultValue = "pythontex")
  private String pythontexCommand = "pythontex";

  /**
   * The options for the command {@link #pythontexCommand}. 
   * <p> 
   * For the possibilities see the manual of the pythontex package 
   * or the help dialog of <code>pythontex</code>. 
   * CAUTION: <code>--rerun</code> and <code>--runall</code> cannot be specified both in one invocation. 
   * In the context of this software, the option
   * <code>--interactive</code> is not appropriate. 
   * CAUTION: For many options of the command line tool, 
   * there is an according package option and the latter overrides the former. 
   * CAUTION: This software overwrites settings <code>--rerun</code> and <code>--runall</code> anyway, 
   * and forces setting <code>--rerun=always</code>. 
   * The default value is <code>--rerun=always</code>. 
   */
  @RuntimeParameter
  @Parameter(name = "pythontexOptions", defaultValue = "--rerun=always")
  private String pythontexOptions = "--rerun=always";


  /**
   * The pattern in the plg-file 
   * indicating that running <code>pythontex</code>, resp. <code>pythontexW</code> 
   * via {@link #pythontexCommand} failed. 
   * The default value is essentially 
   * <code>(PythonTeX:  .+ -|    - Current: ) [1-9][0-9]* error\\(s\\), [0-9]+ warning\\(s\\)</code> (note the spaces)
   * but due to a bug in <code>pythontex</code> it is slightly more complicated. 
   * If this is not appropriate, please modify 
   * and notify the developer of this plugin. 
   */
  @RuntimeParameter
  @Parameter(name = "patternErrPyTex",
      defaultValue = "\\* PythonTeX error|(PythonTeX:  .+ -|    - Current: ) [1-9][0-9]* error\\(s\\), [0-9]+ warning\\(s\\)")
  private String patternErrPyTex =
      "\\* PythonTeX error|(PythonTeX:  .+ -|    - Current: ) [1-9][0-9]* error\\(s\\), [0-9]+ warning\\(s\\)";

  /**
   * The pattern in the plg-file 
   * indicating a warning when running <code>pythontex</code>, resp. <code>pythontexW</code> 
   * via {@link #pythontexCommand}. 
   * The default value is <code>(PythonTeX:  .+ -|    - Current: ) [0-9]+ error\\(s\\), [1-9][0-9]* warning\\(s\\)</code> 
   * (note the space and the brackets). 
   * If this is not appropriate, please modify 
   * and notify the developer of this plugin. 
   */
  @RuntimeParameter
  @Parameter(name = "patternWarnPyTex",
      defaultValue = "(PythonTeX:  .+ -|    - Current: ) [0-9]+ error\\(s\\), [1-9][0-9]* warning\\(s\\)")
  private String patternWarnPyTex =
      "(PythonTeX:  .+ -|    - Current: ) [0-9]+ error\\(s\\), [1-9][0-9]* warning\\(s\\)";

  /**
   * The Depythontex command invoked with no file ending 
   * to create a file <code>xxx.depytx.tex</code> file
   * from a tex-file, a depytx-file taking the output of <code>pythontex</code> into account 
   * and logging on a dplg-file. 
   * The default value is <code>depythontex</code> 
   * but as long as this does not write a log file this software really needs, 
   * we have to configure it with <code>depythontexW</code> 
   * which is a simple wrapper of <code>depythontex</code> writing a log file. 
   * CAUTION: Since <code>depythontexW</code> is not registered with this software, 
   * one has to specify it with its category as <code>depythontexW:depythontex</code>. 
   */
  @RuntimeParameter
  @Parameter(name = "depythontexCommand", defaultValue = "depythontex")
  private String depythontexCommand = "depythontex";

  /**
   * The additional options for the command {@link #depythontexCommand}. 
   * To run <code>depythontex</code> in the context of this software, 
   * the options <code>--overwrite --output file</code> are mandatory 
   * to create an output file at all and to overwrite if it already exists 
   * avoiding that <code>depythontex</code> enters interactive mode. 
   * Thus these options are added silently. 
   * This setting is the additional options. 
   * <p>
   * The default value is the empty option string. 
   * For the possibilites see the manual of the pythontex package 
   * or the help dialog of code>depythontex</code>. 
   */
  @RuntimeParameter
  @Parameter(name = "depythontexOptions", defaultValue = "")
  private String depythontexOptions = "";

  // parameters for latex2html-conversion 

  /**
   * The tex4ht command. 
   * Possible values are e.g. 
   * <code>htlatex</code> and <code>htxelatex</code>. 
   * The default value (for which this software is also tested) 
   * is <code>htlatex</code>. 
   */
  @RuntimeParameter
  @Parameter(name = "tex4htCommand", defaultValue = "htlatex")
  private String tex4htCommand = "htlatex";

  /**
   * The options for the <code>tex4ht</code>-style 
   * which creates a dvi-file or a pdf-file 
   * with information to create sgml, 
   * e.g. html or odt or something like that. 
   * The default value is <code>html,2</code>. 
   */
  @RuntimeParameter
  @Parameter(name = "tex4htStyOptions", defaultValue = "html,2")
  private String tex4htStyOptions = "html,2";

  /**
   * The options for <code>tex4ht</code> which extracts information 
   * from a dvi-file or from a pdf-file 
   * into the according lg-file and idv-file producing html-files 
   * and by need and if configured accordingly 
   * svg-files, 4ct-files and 4tc-files and a css-file and a tmp-file.
   * The former two are used by <code>t4ht</code> 
   * which is configured via {@link #t4htOptions}. 
   */
  @RuntimeParameter
  @Parameter(name = "tex4htOptions", defaultValue = "")
  private String tex4htOptions = "";

  /**
   * The options for <code>t4ht</code> which converts idv-file and lg-file 
   * into css-files, tmp-file and, 
   * by need and if configured accordingly into png files. 
   * The value <code>-p</code> prevents creation of png-pictures.
   * The default value is the empty string. 
   */
  @RuntimeParameter
  @Parameter(name = "t4htOptions", defaultValue = "")
  private String t4htOptions = "";

  /**
   * The pattern for the target files of goal {@link Target#html} 
   * for a given latex main file <code>xxx.tex</code>. 
   * The patterns for the other targets 
   * are hardcoded and take the form 
   * <code>^T$T\.yyy$</code>, where <code>yyy</code> 
   * may be an ending or an alternative of endings. 
   * <p>
   * For an explanation of the pattern <code>T$T</code>, 
   * see {@link #patternCreatedFromLatexMain}. 
   * Spaces and newlines are removed 
   * from that pattern before processing. 
   * <p>
   * The default value has the following components: 
   * <ul>
   * <li><code>^T$T\.x?html?$</code> 
   * is the main file. 
   * <li><code>^T$Tli\d+\.x?html?$</code> 
   * are lists: toc, lof, lot, indices, glossaries, NOT the bibliography. 
   * <li><code>^T$T(ch|se|su|ap)\d+\.x?html?$</code> 
   * are chapters, sections and subsections or below 
   * and appendices. 
   * <li><code>^T$T\d+\.x?html?$</code> 
   * are footnotes. 
   * <li><code>^T$T\.css$</code> 
   * are cascaded stylesheets. 
   * <li><code>^T$T-\d+\.svg$</code> and <code>^T$T\d+x\.png$</code>
   * are svg/png-files representing figures. 
   * <li><code>^(cmsy)\d+(-c)?-\d+c?\.png$</code> 
   * represents special symbols. 
   * </ul>
   * Note that the patterns for the html-files 
   * can be summarized as <code>^T$T((ch|se|su|ap|li)?\d+)?\.x?html?$</code>. 
   * Adding the patterns for the css-file and the svg-files, we obtain 
   * <pre>
   * ^T$T(((ch|se|su|ap|li)?\d+)?\.x?html?|
   * \.css|
   * \d+x\.x?bb|
   * \d+x\.png|
   * -\d+\.svg)$
   * </pre>. 
   * <p>
   * The pattern is designed to match quite exactly 
   * the files to be copied to {@link #targetSiteDirectory}, 
   * for the goal {@link Target#html}, 
   * not much more and at any case not less. 
   * since {@link #tex4htCommand} is not well documented, 
   * and still subject to development, 
   * this pattern cannot be guaranteed to be final. 
   * If the user finds an extension, (s)he is asked to contribute 
   * and to notify the developer of this plugin. 
   * Then the default value will be extended. 
   */
  @RuntimeParameter
  @Parameter(name = "patternT4htOutputFiles", defaultValue = "")
  private String patternT4htOutputFiles =
      "^(T$T(((ch|se|su|ap|li)?\\d+)?\\.x?html?|" +
      /*   */"\\.css|" +
      /*   */"\\d+x\\.x?bb|" +
      /*   */"\\d+x\\.png|" +
      /*   */"-\\d+\\.svg)|" + "(cmsy)\\d+(-c)?-\\d+c?\\.png)$";

  // parameters for further conversions 

  /**
   * The latex2rtf command to create rtf from latex directly. 
   * The default value is <code>latex2rtf</code>. 
   */
  @RuntimeParameter
  @Parameter(name = "latex2rtfCommand", defaultValue = "latex2rtf")
  private String latex2rtfCommand = "latex2rtf";

  /**
   * The options of the command {@link #latex2rtfCommand}. 
   * The default value is the empty string. 
   */
  @RuntimeParameter
  @Parameter(name = "latex2rtfOptions", defaultValue = "")
  private String latex2rtfOptions = "";

  /**
   * The odt2doc command 
   * to create MS word-formats from otd-files. 
   * The default value is <code>odt2doc</code>; 
   * equivalent here is <code>unoconv</code>. 
   * Note that <code>odt2doc</code> just calls <code>unoconv</code> 
   * with odt-files as input and doc-file as default output. 
   *
   * @see #odt2docOptions
   */
  @RuntimeParameter
  @Parameter(name = "odt2docCommand", defaultValue = "odt2doc")
  private String odt2docCommand = "odt2doc";

  /**
   * The options of the command {@link #odt2docCommand}. 
   * Above all specification of output format 
   * via the option <code>-f</code>. 
   * Invocation is <code>odt2doc -f&lt;format&gt; &lt;file&gt;.odt</code>. 
   * All output formats are shown by <code>odt2doc --show</code> 
   * but the formats interesting in this context 
   * are <code>doc, doc6, doc95,docbook, docx, docx7, ooxml, rtf</code>. 
   * Interesting also the verbosity options <code>-v, -vv, -vvv</code> 
   * the timeout <code>-T=secs</code> and <code>--preserve</code> 
   * to keep permissions and timestamp of the original document. 
   * The default value is <code>-fdocx</code>. 
   *
   * @see #odt2docCommand
   */
  @RuntimeParameter
  @Parameter(name = "odt2docOptions", defaultValue = "-fdocx")
  private String odt2docOptions = "-fdocx";

  /**
   * The pdf2txt-command for converting pdf-files into plain text files. 
   * The default value is <code>pdftotext</code>. 
   *
   * @see #pdf2txtOptions
   */
  @RuntimeParameter
  @Parameter(name = "pdf2txtCommand", defaultValue = "pdftotext")
  private String pdf2txtCommand = "pdftotext";

  /**
   * The options of the command {@link #pdf2txtCommand}. 
   * The default value is the empty string. 
   *
   * @see #pdf2txtCommand
   */
  // TBD: check 
  @RuntimeParameter
  @Parameter(name = "pdf2txtOptions", defaultValue = "-q")
  private String pdf2txtOptions = "-q";



  /**
   * The chktex-command for checking latex main files. 
   * The default value is <code>chktex</code>. 
   *
   * @see #chkTexOptions
   */
  @RuntimeParameter
  @Parameter(name = "chkTexCommand", defaultValue = "chktex")
  private String chkTexCommand = "chktex";


  /**
   * The options of the command {@link #chkTexCommand}, 
   * except <code>-o output-file</code> 
   * specifying the output file which is added automatically. 
   * <p>
   * Here is a list of options useful in this context. 
   * The first group of these are muting options: 
   * <ul>
   * <li><code>-w</code>, <code>-e</code>, <code>-m</code>, 
   * Make the message number passed as parameter 
   * a warning/an error/a message and turns it on. 
   * Messages are not counted. 
   * <li><code>-n</code>
   * Turns the warning/error number passed as a parameter off. 
   * <li><code>-L</code>
   * Turns off suppression of messages on a per line basis. 
   * </ul>
   * The next group of interesting options are for output control: 
   * <ul>
   * <li><code>-q</code>
   * Shuts up about copyright information.
   * <li><code>-o output-file</code>
   * Specifies the output file. This is added automatically 
   * and shall thus not be specified by the user. 
   * <li><code>-b[0|1]</code>
   * If you use the -o switch, and the named outputfile exists,
   * it will be renamed to <code>filename.bak</code>.
   * <li><code>-f format</code>
   * Specifies the format of the output 
   * via a format similar to <code>printf()</code>. 
   * For details consult the manual. 
   * <li><code>-vd</code>
   * Verbosity level followed by a number <code>d</code> 
   * specifying the format of the output. 
   * The verbosity number is resolved as a pattern 
   * as if given by the option <code>-f format</code>. 
   * Thus the option <code>-v</code> is ignored 
   * if the option <code>-f format</code> is specified. 
   * </ul>
   * The default value is <code>-q -b0</code> 
   * avoiding verbose output and backing up the output log-file. 
   *
   * @see #chkTexCommand
   */
  // -v: verbosity: 
  //     - 0 File:Line:Column:Warning number:Warning message
  //         No specification on the kind of the entry 
  //     - 1 1st line: (Error|Warning|Message) in <File> line <Line>: message 
  //         2nd line: according line of the source 
  //         3rd line: cursor ^ pointing to the place where the problem is 
  //     - 2 1st line as for level 1 
  //         2nd line: line of source with pointer for the problem 
  //                   has shape: [7m [0m
  //     - 3 "File", line Line: Warning message 
  //     - 4 1st line as for 3, 
  //         2nd line as for 1 
  //         3rd line as for 1 
  // -f format: this allows to create more flexible formats as with -vxxx 
  //         to determine the kind of entry (Error|Warning|Message) 
  //         if kind is given, it must be at the beginning of the line 
  // -q: no copyright information 
  // -b: toggle creation of backup file: with -o: yes, additional -b: no 
  //     explicitly as -b0 and -b1, respectively. 
  @RuntimeParameter
  @Parameter(name = "chkTexOptions", defaultValue = "-q -b0")
  private String chkTexOptions = "-q -b0";

  /**
   * The diff-command for diffing pdf-files strictly or just visually 
   * to check that the created pdf files are equivalent with prescribed ones. 
   * CAUTION: there are two philisophies: 
   * Either the latex source files are created in a way that they reproduce strictly. 
   * Then a strict diff command like <code>diff</code> is appropriate. 
   * Else another diff command is required which checks for a kind of visual equality. 
   * The default value is a mere <code>diff</code>. 
   * Alternatives are <code>diff-pdf</code> and <code>diff-pdf-visually</code> 
   * both implementing a visual diff. 
   * Note that unlike for other tools, no options can be passed in this case explicitly. 
   * CAUTION: Expected return value 0 means same, 1 normal difference, all other values: failure. 
   * Thus <code>diff-pdf-visually</code> is not allowed, 
   * because uses different return code: exchanging 1 and 2. 
   * Thus usable for a wrapper only. 
   * TBD: work into this addition. 
   */
  @RuntimeParameter
  @Parameter(name = "diffPdfCommand", defaultValue = "diff")
  private String diffPdfCommand = "diff";

  //TBD: add options; 
  // diff: no sensible options are available. 
  // diff-pdf same
  // diff-pdf-visually same
  // getter methods partially implementing default values. 


  // private File getBaseDirectory() throws BuildFailureException {
  // 	if (!(this.baseDirectory.exists() && 
  // 	      this.baseDirectory.isDirectory())) {
  // 	    throw new BuildFailureException
  // 		("The base directory '" + this.baseDirectory + 
  // 		 "' should be an existing directory, but is not. ");
  // 	}
  // 	return this.baseDirectory;
  // }

  // private File getTargetDirectory() {
  //     return this.targetDirectory;
  // }

  // private File getTargetSiteDirectory() {
  //     return this.targetSiteDirectory;
  // }

  /**
   *
   * @throws BuildFailureException 
   *    TSS01 if the tex source directory does either not exist 
   *    or is not a directory. 
   */
  // used in LatexProcessor only: 
  // .create() to determine the output directory of the created files 
  public File getTexSrcDirectoryFile() throws BuildFailureException {
    if (!(this.texSrcDirectoryFile.exists()
        && this.texSrcDirectoryFile.isDirectory())) {
      throw new BuildFailureException(
          "TSS01: The tex source directory '" + this.texSrcDirectoryFile
              + "' should be an existing directory, but is not. ");
    }
    return this.texSrcDirectoryFile;
  }

  /**
   *
   * @throws BuildFailureException 
   *    TSS02 if the tex source processing directory does either not exist 
   *    or is not a directory. 
   */
  // used in LatexProcessor only: 
  // .create() to determine which directories to be processed 
  // .processGraphics() to get all graphics files 
  // .clearAll() 
  public File getTexSrcProcDirectoryFile() throws BuildFailureException {
    if (!(this.texSrcProcDirectoryFile.exists()
        && this.texSrcProcDirectoryFile.isDirectory())) {
      throw new BuildFailureException(
          "TSS02: The tex source processing directory '"
              + this.texSrcProcDirectoryFile
              + "' should be an existing directory, but is not. ");
    }

    return this.texSrcProcDirectoryFile;
  }

  public boolean getReadTexSrcProcDirRec() {
    return this.readTexSrcProcDirRec;
  }

  /**
   *
   * @throws BuildFailureException 
   *    TSS03 if the output directory exists and is no directory. 
   */
  public File getOutputDirectoryFile() throws BuildFailureException {
    if (/**/this.outputDirectoryFile.exists()
        && !this.outputDirectoryFile.isDirectory()) {
      throw new BuildFailureException(
          "TSS03: The output directory '" + this.outputDirectoryFile
              + "' should be a directory if it exists, but is not. ");
    }
    return this.outputDirectoryFile;
  }

  /**
   *
   * @throws BuildFailureException 
   *    TSS09 if the diff directory exists and is no directory. 
   */
  public File getDiffDirectoryFile() throws BuildFailureException {
    if (/**/this.diffDirectoryFile.exists()
        && !this.diffDirectoryFile.isDirectory()) {
      throw new BuildFailureException(
          "TSS09: The diff directory '" + this.diffDirectoryFile
              + "' should be a directory if it exists, but is not. ");
    }
    return this.diffDirectoryFile;
  }

  /**
   * Returns the set of targets. 
   *
   * @return
   *     The set of targets.
   * @throws BuildFailureException
   *    TSS04 if the target set is not a subset
   *    of the set given by {@link Target}.
   */
  // TBD: ordering must be clarified 
  public SortedSet<Target> getTargets() throws BuildFailureException {
    return getTargets(this.targets, TargetsContext.targetsSetting);
  }

  /**
   * Returns the targets given by the string <code>targetsChunkStr</code> 
   * as a set of {@link Target}s. 
   * If the target string is invalid an exception is thrown 
   * with a message influenced by <code>targetContext</code> 
   * which depends on the context in which this method is invoked. 
   * 
   * @param targetsChunksStr
   *    The target string of all targets. 
   *    TBD: simplify name. 
   * @param targetContext
   *    specifies the context in which this method is invoked 
   *    and contributes this context to the message of a thrown exception, if any. 
   *    Else this value has no effect. 
   * @return
   *     The set of targets given by <code>targetsChunksStr</code>.
   * @throws BuildFailureException
   *    <ul>
   *    <li>TSS04 if <code>targetsChunkStr</code> is invalid 
   *    <li>TSS11 if a target in <code>targetsChunkStr</code> occurs more than once 
   *    </ul>
   * @see #getTargets()
   * @see #getDocClassesToTargets()
   * @see LatexProcessor#create(SortedSet)
   */
  static SortedSet<Target> getTargets(String targetsChunksStr, TargetsContext targetContext)
      throws BuildFailureException {

    // TreeSet is sorted. maybe this determines ordering of targets. 
    SortedSet<Target> targetsSet = new TreeSet<Target>();
    if (targetsChunksStr.isEmpty()) {
      return targetsSet;
    }
    String[] targetSeq = targetsChunksStr.split(",");
    for (int idx = 0; idx < targetSeq.length; idx++) {
      assert targetSeq[idx] != null;
      String targetStr = targetSeq[idx];
      // may throw BuildFailureException TSS04 and TSS11 
      readTargetChecked(targetStr, targetsSet, targetsChunksStr, targetContext);
    } // for 
    return targetsSet;
  }

  /**
   * Converts <code>targetStr</code> into a {@link Target} 
   * and adds it to <code>targetSet</code> if possible. 
   * An exception is thrown if <code>targetStr</code> is either no valid target 
   * or if it is a target already in <code>targetSet</code>. 
   * Parameter <code>targetsStr</code> is only used 
   * to create the message of the exceptions thrown. 
   * 
   * @param targetStr
   *    The target to be added 
   * @param targetsSet
   *    The target set the target to <code>targetStr</code> must be added. 
   * @param targetsChunksStr
   *    The target string of all targets: <code>targetStr</code> is part. 
   *    This is needed for the message of the exception. 
   * @param targetsContext
   *    specifies the context in which this method is invoked 
   *    and contributes this context to the message of a thrown exception, if any. 
   *    Else this value has no effect. 
   * @throws BuildFailureException
   *    <ul>
   *    <li>TSS04 if targetStr is invalid 
   *    <li>TSS11 if target for targetStr already in targetsSet
   *    </ul>
   */
  private static void readTargetChecked(String targetStr,
                                        Set<Target> targetsSet, 
                                        String targetsChunksStr, 
                                        TargetsContext targetsContext)
                throws BuildFailureException {
    try {
      Target target = Target.valueOf(targetStr);
      // may not throw an IllegalArgumentException
      boolean isNew = targetsSet.add(target);
      if (!isNew) {
        throw new BuildFailureException("TSS11: The target '" + targetsChunksStr
          + "' in " + targetsContext.context()
          + " repeats target '" + target + "'. ");
      }
    } catch (IllegalArgumentException ae) {
      // Here, targetStr does not contain the name of a Target 
      assert Target.class.isEnum();
      throw new BuildFailureException("TSS04: The target '" + targetsChunksStr
          + "' in " + targetsContext.context()
          + " contains the invalid target '" + targetStr + "'. ");
    } // catch 
  }

  // public SortedSet<Target> getTargets() throws BuildFailureException {
  //   return this.targets;
  // }

  // TBD: maybe better: store allowed converters. 
  // TBD: maybe better: cache converters read. 
  /**
   * Returns the set of converters excluded from usage. 
   *
   * @return
   *     The set of converters excluded from usage. 
   * @throws BuildFailureException 
   *    TSS05 if set of converters excluded from usage 
   *    is not a subset of the set given by {@link Converter}. 
   */
  public SortedSet<Converter> getConvertersExcluded()
      throws BuildFailureException {
    SortedSet<Converter> convSet = new TreeSet<Converter>();
    if (this.convertersExcluded.isEmpty()) {
      return convSet;
    }
    String[] convSeq = this.convertersExcluded.split(" *, *");
    for (int idx = 0; idx < convSeq.length; idx++) {
      Converter conv = Converter.cmd2Conv(convSeq[idx]);
      if (conv == null) {
        // Here, the converter given is unknown.
        throw new BuildFailureException(
            "TSS05: The excluded converters '" + this.convertersExcluded
                + "' should form a subset of the registered converters '"
                + Converter.toCommandsString() + "'. ");
      }
      convSet.add(conv);
    }
    return convSet;
  }

  /**
   * Returns the converter name which is typically <code>convStr</code> 
   * and throws an exception if the converter given is invalid. 
   *
   * @param convStr
   *    the name of a converter as a string as given in the configuration. 
   *    If this converter is registered with this software, 
   *    i.e. if it corresponds with an instance of {@link Converter}, 
   *    then it is given just by {@link Converter#getCommand()}. 
   *    Else, it must be given in the form <code>commandName:category</code>, 
   *    where <code>category</code> is given by <code>cat</code> 
   *    via {@link ConverterCategory#getExtName()}. 
   * @throws BuildFailureException
   *    In case the converter is given directly, as if registered: 
   *    <ul>
   *    <li>TSS06 if 
   *    tried to use converter not registered. </li>
   *    <li>TSS05 if 
   *    the set of converters excluded from usage 
   *    is not a subset of the set given by {@link Converter}. </li>
   *    <li>TSS07 if 
   *    tried to use converter which is among the excluded ones. </li>
   *    <li>TSS08 if 
   *    tried to use converter within wrong category. </li>
   *     <li>
   *    tried to use converter within wrong category. </li>
   *    </ul>
   *    TSS10 if the converter is given in the form 
   *    <code>&lt;cat1Command&gt;commandName:cat2&lt;/cat1Command&gt;</code> 
   *    with <code>cat2</code> not coinciding with <code>cat1</code>. 
   * @return
   *    the proper name of the converter. 
   *    If the converter is registered with this software, 
   *    then just <code>convStr</code> is returned. 
   *    Else <code>convStr</code> has the form <code>commandName:category</code> 
   *    and what is returned is the proper name <code>commandName</code>. 
   */
  private String checkConverterName(String convStr, ConverterCategory cat)
      throws BuildFailureException {
    int idxLastCol = convStr.lastIndexOf(':');
    if (idxLastCol != -1) {
      // Here, the converter is not registered 
      // and so it is given in the form
      // converterName:category
      String catStr = convStr.substring(idxLastCol + 1);
      String convStrProper = convStr.substring(0, idxLastCol);
      if (!cat.getExtName().equals(catStr)) {
        throw new BuildFailureException(
            "TSS10: Specified unregistered converter '" + convStrProper
                + "' with invalid category '" + catStr + "'; should be '"
                + cat.getExtName() + "''. ");
      }
      return convStrProper;
    }

    // Here, no colon occurs and the converter is registered.

    Converter conv = Converter.cmd2Conv(convStr);
    if (conv == null) {
      throw new BuildFailureException("TSS06: Tried to use converter '"
          + convStr + "' although not among the registered converters '"
          + Converter.toCommandsString() + "' as expected. ");
    }
    // may throw BuildFailureException TSS05
    SortedSet<Converter> convertersExcluded = getConvertersExcluded();
    if (convertersExcluded.contains(conv)) {
      throw new BuildFailureException("TSS07: Tried to use converter '"
          + convStr + "' although among the excluded converters '"
          + Converter.toCommandsString(convertersExcluded) + "'. ");
    }
    if (conv.getCategory() != cat) {
      throw new BuildFailureException(
          "TSS08: Tried to use converter '" + convStr + "' in configuration '"
              + cat.getCommandFieldname() + "' instead of configuration '"
              + conv.getCategory().getCommandFieldname() + "'. ");
    }
    return convStr;
  }

  public String getPatternLatexMainFile() {
    return this.patternLatexMainFile;
  }

  // TBD: invocation at the wrong place: thus is invoked for each target anew. 
  /**
   * 
   * @return
   * @throws BuildFailureException
   *    TSS01, TSS11
   */
  public Map<String, Set<Target>> getDocClassesToTargets()
      throws BuildFailureException {

    Map<String, Set<Target>> result = new TreeMap<String, Set<Target>>();
    String[] chunks = this.docClassesToTargets.trim().split("\\s");
    int idxCol1, idxCol2;
    String classesStr;
    Set<Target> targetsSet, oldTargetSet;
    for (String chunk : chunks) {
      idxCol1 = chunk.indexOf(':');
      idxCol2 = chunk.lastIndexOf(':');
      if (idxCol1 == -1 || idxCol1 == 0 || idxCol1 != idxCol2) {
        // Here, either 
        // - no colon exists (idxCol1 == -1 (which implies also idxCol2 == -1)) 
        // - or more than one colon exists (idxCol1 != idxCol2)
        // - or the colon is at the the 0th place (idxCol1 == 0)
        // Note that the colon may be at the last place 
        // indicating that the given document classes (preceding the colon) 
        // are not processed at all. 
        throw new BuildFailureException("TSS12: Invalid mapping '" + chunk
            + "' of document classes to targets. ");
      }

      String targetsStr = chunk.substring(idxCol1 + 1);
      targetsSet = getTargets(targetsStr, TargetsContext.inChunkSetting);


      classesStr = chunk.substring(0, idxCol1);
      //System.out.println("classesStr: '" + classesStr + "'");
      // TBD: check maven bug MNG-7927 still present. 
      // For details search through the manual. 
      for (String cls : classesStr.split(",")) {
        oldTargetSet = result.put(cls, targetsSet);
        if (oldTargetSet != null) {
          throw new BuildFailureException
          ("TSS13: For document class '" + cls
              + "' target set is not unique. ");
        }
      }
     } // chunks 

    return result;
  }

  public Set<String> getMainFilesIncluded() {
    return this.mainFilesIncluded.isEmpty() ? new HashSet<String>()
        : new HashSet<String>(Arrays.asList(this.mainFilesIncluded.split(" ")));
  }

  public Set<String> getMainFilesExcluded() {
    return this.mainFilesExcluded.isEmpty() ? new HashSet<String>()
        : new HashSet<String>(Arrays.asList(this.mainFilesExcluded.split(" ")));
  }

  // texPath, commands and arguments 

  public File getTexPath() {
    return this.texPath;
  }

  public boolean isCleanUp() {
    return this.cleanUp;
  }

  public boolean isChkDiff() {
    return this.chkDiff;
  }

  public String getPatternCreatedFromLatexMain() {
    return this.patternCreatedFromLatexMain;
  }

  // for ant task only 
  public String getFig2devCommand() throws BuildFailureException {
    return getCommand(ConverterCategory.Fig2Dev);
  }

  public String getFig2devGenOptions() {
    return this.fig2devGenOptions;
  }

  public String getFig2devPtxOptions() {
    return this.fig2devPtxOptions;
  }

  public String getFig2devPdfEpsOptions() {
    return this.fig2devPdfEpsOptions;
  }

  // for ant task only 
  public String getGnuplotCommand() throws BuildFailureException {
    return getCommand(ConverterCategory.Gnuplot2Dev);
  }

  public String getGnuplotOptions() {
    return this.gnuplotOptions;
  }

  // for ant task only 
  public String getMetapostCommand() throws BuildFailureException {
    return getCommand(ConverterCategory.MetaPost);
  }

  public String getMetapostOptions() {
    return this.metapostOptions;
  }

  // same pattern as for latex 
  public String getPatternErrMPost() {
    return this.patternErrMPost;
  }

  // same pattern as for latex 
  // FIXME: counterexample
  //Preloading the plain mem file, version 1.005) ) (./F4_05someMetapost.mp
  // Warning: outputtemplate=0: value has the wrong type, assignment ignored.
  public String getPatternWarnMPost() {
    return this.patternWarnMPost;
  }

  // for ant task only 
  public String getSvg2devCommand() throws BuildFailureException {
    return getCommand(ConverterCategory.Svg2Dev);
  }

  public String getSvg2devOptions() {
    return this.svg2devOptions;
  }

  public boolean getCreateBoundingBoxes() {
    return this.createBoundingBoxes;
  }

  // for ant task only 
  public String getEbbCommand() throws BuildFailureException {
    return getCommand(ConverterCategory.EbbCmd);
  }

  public String getEbbOptions() {
    return this.ebbOptions;
  }

  // for ant task only 
  public String getLatex2pdfCommand() throws BuildFailureException {
    return getCommand(ConverterCategory.LaTeX);
  }

  // FIXME: to be renamed: texOptions 
  public String getLatex2pdfOptions() {
    return this.latex2pdfOptions;
  }

  public String getPatternErrLatex() {
    return this.patternErrLatex;
  }

  public String getPatternWarnLatex() {
    return this.patternWarnLatex;
  }

  public boolean getDebugBadBoxes() {
    return this.debugBadBoxes;
  }

  public boolean getDebugWarnings() {
    return this.debugWarnings;
  }

  public LatexDev getPdfViaDvi() {
    return LatexDev.devViaDvi(this.pdfViaDvi);
  }

  // for ant task only 
  public String getDvi2pdfCommand() throws BuildFailureException {
    return getCommand(ConverterCategory.Dvi2Pdf);
  }

  public String getDvi2pdfOptions() {
    return this.dvi2pdfOptions;
  }

  public String getPatternReRunLatex() {
    return this.patternReRunLatex;
  }

  public int getMaxNumReRunsLatex() {
    return this.maxNumReRunsLatex;
  }

  // TBD: check category 

  // TBD: refer to annotation, not to field name. 
  /**
   * TBD: add docs 
   *
   * @throws BuildFailureException 
   *    <ul>
   *    <li>TSS06 if 
   *    tried to use converter not registered. </li>
   *    <li>TSS05 if 
   *    set of converters excluded from usage 
   *    is not a subset of the set given by {@link Converter}. </li>
   *    <li>TSS07 if 
   *    tried to use converter which is among the excluded ones. </li>
   *    <li>TSS08 if 
   *    tried to use converter within wrong category. </li>
   *    </ul>
   */
  public String getCommand(ConverterCategory cat) throws BuildFailureException {
    String cmdName;
    try {
      cmdName = (String) this.getClass()
          .getDeclaredField(cat.getCommandFieldname()).get(this);
    } catch (NoSuchFieldException nsfe) {
      throw new IllegalStateException("Could not find field '"
          + cat.getCommandFieldname() + "' in Settings. ");
    } catch (IllegalAccessException iace) {
      throw new IllegalStateException(
          "Parameter '" + cat.getCommandFieldname() + "' not readable. ");
    } catch (IllegalArgumentException iage) {
      throw new IllegalStateException("Settings class mismatch. ");
    }
    // may throw BuildFailureException TSS05-08

    cmdName = checkConverterName(cmdName, cat);// replace by checked cmdName
    return cmdName;
  }

  // for ant task only 
  public String getBibtexCommand() throws BuildFailureException {
    return getCommand(ConverterCategory.BibTeX);
  }

  public String getBibtexOptions() {
    return this.bibtexOptions;
  }

  public String getPatternErrBibtex() {
    return this.patternErrBibtex;
  }

  public String getPatternWarnBibtex() {
    return this.patternWarnBibtex;
  }

  // for ant task only 
  public String getMakeIndexCommand() throws BuildFailureException {
    return getCommand(ConverterCategory.MakeIndex);
  }

  public String getMakeIndexOptions() {
    return this.makeIndexOptions;
  }

  public String getPatternErrMakeIndex() {
    return this.patternErrMakeIndex;
  }

  public String getPatternWarnMakeIndex() {
    return this.patternWarnMakeIndex;
  }

  public String getPatternReRunMakeIndex() {
    return this.patternReRunMakeIndex;
  }

  // for ant task only 
  public String getSplitIndexCommand() throws BuildFailureException {
    return getCommand(ConverterCategory.SplitIndex);
  }

  public String getSplitIndexOptions() {
    return this.splitIndexOptions;
  }

  // for ant task only 
  public String getMakeGlossariesCommand() throws BuildFailureException {
    return getCommand(ConverterCategory.MakeGlossaries);
  }

  public String getMakeGlossariesOptions() {
    return this.makeGlossariesOptions;
  }

  public String getPatternErrMakeGlossaries() {
    return this.patternErrMakeGlossaries;
  }

  public String getPatternWarnXindy() {
    return this.patternWarnXindy;
  }

  // TBD: clarify: never used. 
  public String getPatternReRunMakeGlossaries() {
    return this.patternReRunMakeGlossaries;
  }

  // for ant task only 
  public String getPythontexCommand() throws BuildFailureException {
    return getCommand(ConverterCategory.Pythontex);
  }

  public String getPythontexOptions() {
    return this.pythontexOptions;
  }

  public String getPatternErrPyTex() {
    return this.patternErrPyTex;
  }

  public String getPatternWarnPyTex() {
    return this.patternWarnPyTex;
  }

  // TBD: check category . shall be replaced by getCommand(ConverterCategory)
  public String getTex4htCommand() {
    //Converter conv = Converter.cmd2Conv(this.tex4htCommand, ConverterCategory.LaTeX2Html);
    // TBD: check: this has two categories: tex2html and tex2odt 
    return this.tex4htCommand;
  }

  public String getTex4htStyOptions() {
    return this.tex4htStyOptions;
  }

  public String getTex4htOptions() {
    return this.tex4htOptions;
  }

  public String getT4htOptions() {
    return this.t4htOptions;
  }

  public String getPatternT4htOutputFiles() {
    return this.patternT4htOutputFiles;
  }

  // for ant task only 
  public String getLatex2rtfCommand() throws BuildFailureException {
    return getCommand(ConverterCategory.LaTeX2Rtf);
  }

  public String getLatex2rtfOptions() {
    return this.latex2rtfOptions;
  }

  public String getOdt2docCommand() throws BuildFailureException {
    return getCommand(ConverterCategory.Odt2Doc);
  }

  public String getOdt2docOptions() {
    return this.odt2docOptions;
  }

  // for ant task only 
  public String getPdf2txtCommand() throws BuildFailureException {
    return getCommand(ConverterCategory.Pdf2Txt);
  }

  public String getPdf2txtOptions() {
    return this.pdf2txtOptions;
  }

  // for ant task only 
  public String getChkTexCommand() throws BuildFailureException {
    return getCommand(ConverterCategory.LatexChk);
  }

  public String getChkTexOptions() {
    return this.chkTexOptions;
  }

  // for ant task only if needed TBD
  public String getDiffPdfCommand() throws BuildFailureException {
    return getCommand(ConverterCategory.DiffPdf);
  }

  // setter methods 

  /**
    * Sets {@link #baseDirectory} and updates 
    * {@link #texSrcDirectoryFile} and {@link #texSrcProcDirectoryFile}. 
    */
  public void setBaseDirectory(File baseDirectory) {
    this.baseDirectory = baseDirectory;
    this.texSrcDirectoryFile =
        new File(this.baseDirectory, this.texSrcDirectory);
    this.texSrcProcDirectoryFile =
        new File(this.texSrcDirectoryFile, this.texSrcProcDirectory);
    this.texSrcProcDirectoryFile =
        new File(this.texSrcDirectoryFile, this.texSrcProcDirectory);
    this.diffDirectoryFile = new File(this.baseDirectory, this.diffDirectory);

  }

  /**
   * Sets {@link #targetDirectory}. 
   */
  public void setTargetDirectory(File targetDirectory) {
    this.targetDirectory = targetDirectory;
  }

  /**
   * Sets {@link #targetSiteDirectory} and updates 
   * {@link #outputDirectoryFile}. 
   */
  public void setTargetSiteDirectory(File targetSiteDirectory) {
    this.targetSiteDirectory = targetSiteDirectory;
    this.outputDirectoryFile =
        new File(this.targetSiteDirectory, this.outputDirectory);
  }

  /**
   * Sets {@link #texSrcDirectory} and updates 
   * {@link #texSrcDirectoryFile} and {@link #texSrcProcDirectoryFile}. 
   */
  public void setTexSrcDirectory(String texSrcDirectory) {
    this.texSrcDirectory = texSrcDirectory;
    this.texSrcDirectoryFile =
        new File(this.baseDirectory, this.texSrcDirectory);
    this.texSrcProcDirectoryFile =
        new File(this.texSrcDirectoryFile, this.texSrcProcDirectory);
  }

  /**
   * Sets {@link #texSrcProcDirectory} and updates 
   * {@link #texSrcProcDirectoryFile}. 
   */
  public void setTexSrcProcDirectory(String texSrcProcDirectory) {
    this.texSrcProcDirectory = texSrcProcDirectory;
    this.texSrcProcDirectoryFile =
        new File(this.texSrcDirectoryFile, this.texSrcProcDirectory);
  }

  public void setReadTexSrcProcDirRec(boolean readTexSrcProcDirRec) {
    this.readTexSrcProcDirRec = readTexSrcProcDirRec;
  }

  /**
   * Sets {@link #outputDirectory} and updates {@link #outputDirectoryFile}. 
   */
  public void setOutputDirectory(String outputDirectory) {
    this.outputDirectory = outputDirectory;
    this.outputDirectoryFile =
        new File(this.targetSiteDirectory, this.outputDirectory);
  }

  public void setDiffDirectory(String diffDirectory) {
    this.diffDirectory = diffDirectory;
    this.diffDirectoryFile = new File(this.baseDirectory, this.diffDirectory);
  }

  // TBD: check which of these setters are really necessary 
  public void setTargets(String targets) {
    this.targets = targets.trim();
  }
  // public void setTargets(SortedSet<Target> targets) {
  //   this.targets = targets;
  // }

  public void setConvertersExcluded(String convertersExcluded) {
    this.convertersExcluded = convertersExcluded.trim();
  }

  // setter method for patternLatexMainFile in maven 
  // trims parameter before setting 
  public void setPatternLatexMainFile(String patternLatexMainFile) {
    this.patternLatexMainFile =
        patternLatexMainFile.replaceAll("(\t|\n)+", "").trim();
  }

  // method introduces patternLatexMainFile in ant 
  public PatternLatexMainFile createPatternLatexMainFile() {
    return new PatternLatexMainFile();
  }

  // defines patternLatexMainFile element with text in ant 
  public class PatternLatexMainFile {
    // FIXME: this is without property resolution.
    // to add this need pattern = getProject().replaceProperties(pattern)
    // with Task.getProject()
    public void addText(String pattern) {
      Settings.this.setPatternLatexMainFile(pattern);
    }
  } // class PatternLatexMainFile

  public void setDocClassesToTargets(String docClassesToTargets) {
    this.docClassesToTargets = docClassesToTargets;
  }

  public void setMainFilesIncluded(String mainFilesIncluded) {
    this.mainFilesIncluded =
        mainFilesIncluded.replaceAll("(\t|\n| )+", " ").trim();
  }

  public void setMainFilesExcluded(String mainFilesExcluded) {
    this.mainFilesExcluded =
        mainFilesExcluded.replaceAll("(\t|\n| )+", " ").trim();
  }

  public void setTexPath(File texPath) {
    this.texPath = texPath;
  }

  public void setCleanUp(boolean cleanUp) {
    this.cleanUp = cleanUp;
  }

  public void setChkDiff(boolean chkDiff) {
    this.chkDiff = chkDiff;
  }

  // FIXME: as patternCreatedFromLatexMain 
  // replace "\n" (canonical newline in xml) also for other patterns by ""

  // setter method for patternCreatedFromLatexMain in maven 
  // eliminates tab, newline and blanks and trims parameter before setting 
  public void setPatternCreatedFromLatexMain(String pattern) {
    this.patternCreatedFromLatexMain =
        pattern.replaceAll("(\t|\n| )+", "").trim();
  }

  // method introduces patternCreatedFromLatexMain in ant 
  public PatternCreatedFromLatexMain createPatternCreatedFromLatexMain() {
    return new PatternCreatedFromLatexMain();
  }

  // defines patternCreatedFromLatexMain element with text in ant 
  public class PatternCreatedFromLatexMain {
    // FIXME: this is without property resolution. 
    // to add this need  pattern = getProject().replaceProperties(pattern)
    // with Task.getProject() 
    public void addText(String pattern) {
      Settings.this.setPatternCreatedFromLatexMain(pattern);
    }
  } // class PatternCreatedFromLatexMain

  // note: setters are required for ant tasks 
  public void setFig2devCommand(String fig2devCommand) {
    this.fig2devCommand = fig2devCommand;
  }

  private static String beautifyOptions(String rawOption) {
    return rawOption.replaceAll("(\t|\n| )+", " ").trim();
  }

  public void setFig2devGenOptions(String fig2devGenOptions) {
    this.fig2devGenOptions = beautifyOptions(fig2devGenOptions);
  }

  public void setFig2devPtxOptions(String fig2devPtxOptions) {
    this.fig2devPtxOptions = beautifyOptions(fig2devPtxOptions);
  }

  public void setFig2devPdfEpsOptions(String fig2devPdfEpsOptions) {
    this.fig2devPdfEpsOptions = beautifyOptions(fig2devPdfEpsOptions);
  }

  public void setGnuplotCommand(String gnuplotCommand) {
    this.gnuplotCommand = gnuplotCommand;
  }

  public void setGnuplotOptions(String gnuplotOptions) {
    this.gnuplotOptions = beautifyOptions(gnuplotOptions);
  }

  public void setMetapostCommand(String metapostCommand) {
    this.metapostCommand = metapostCommand;
  }

  // setter method for metapostOptions in maven 
  public void setMetapostOptions(String metapostOptions) {
    this.metapostOptions = beautifyOptions(metapostOptions);
  }

  // method introduces metapostOptions in ant 
  public MetapostOptions createMetapostOptions() {
    return new MetapostOptions();
  }

  // defines e element with text in ant 
  public class MetapostOptions {
    // FIXME: this is without property resolution. 
    // to add this need  pattern = getProject().replaceProperties(pattern)
    // with Task.getProject() 
    public void addText(String args) {
      Settings.this.setMetapostOptions(args);
    }
  }

  // same pattern as for latex 
  public void setPatternErrMPost(String patternErrMPost) {
    this.patternErrMPost = patternErrMPost;
  }

  // not same pattern as for latex. 
  // in particular, not dependent on library, hm.. mostly? 
  // Example: 
  // Preloading the plain mem file, version 1.005) ) (./F4_05someMetapost.mp
  // Warning: outputtemplate=0: value has the wrong type, assignment ignored.
  public void setPatternWarnMPost(String patternWarnMPost) {
    this.patternWarnMPost = patternWarnMPost;
  }

  public void setSvg2devCommand(String svg2devCommand) {
    this.svg2devCommand = svg2devCommand;
  }

  public void setSvg2devOptions(String svg2devOptions) {
    this.svg2devOptions = beautifyOptions(svg2devOptions);
  }

  public void setCreateBoundingBoxes(boolean createBoundingBoxes) {
    this.createBoundingBoxes = createBoundingBoxes;
  }

  public void setEbbCommand(String ebbCommand) {
    this.ebbCommand = ebbCommand;
  }

  public void setEbbOptions(String ebbOptions) {
    this.ebbOptions = beautifyOptions(ebbOptions);
  }

  public void setLatex2pdfCommand(String latex2pdfCommand) {
    this.latex2pdfCommand = latex2pdfCommand;
  }

  /**
   * Sets the argument string of the latex command 
   * given by {@link #latex2pdfCommand}. 
   * It is ensured that {@link #latex2pdfOptions} 
   * consist of proper options separated by a single blank. 
   *
   * @param args
   *    The arguments string to use when calling LaTeX 
   *    via {@link #latex2pdfCommand}. 
   *    Leading and trailing blank and newline are ignored. 
   *    Proper arguments are separated by blank and newline. 
   */
  // setter method for latex2pdfOptions in maven 
  public void setLatex2pdfOptions(String args) {
    this.latex2pdfOptions = beautifyOptions(args);
  }

  // method introduces latex2pdfOptions in ant 
  public Latex2pdfOptions createLatex2pdfOptions() {
    return new Latex2pdfOptions();
  }

  // defines e element with text in ant 
  public class Latex2pdfOptions {
    // FIXME: this is without property resolution. 
    // to add this need  pattern = getProject().replaceProperties(pattern)
    // with Task.getProject() 
    public void addText(String args) {
      Settings.this.setLatex2pdfOptions(args);
    }
  }

  // setter method for patternErrLatex in maven 
  public void setPatternErrLatex(String patternErrLatex) {
    this.patternErrLatex = patternErrLatex;
  }

  // method introduces patternErrLatex in ant 
  public PatternErrLatex createPatternErrLatex() {
    return new PatternErrLatex();
  }

  // defines patternErrLatex element with text in ant 
  public class PatternErrLatex {
    // FIXME: this is without property resolution. 
    // to add this need  pattern = getProject().replaceProperties(pattern)
    // with Task.getProject() 
    public void addText(String pattern) {
      Settings.this.setPatternErrLatex(pattern);
    }
  }

  // setter method for patternWarnLatex in maven 
  public void setPatternWarnLatex(String patternWarnLatex) {
    this.patternWarnLatex = patternWarnLatex.replaceAll("(\t|\n)+", "").trim();
  }

  // method introduces patternWarnLatex in ant 
  public PatternWarnLatex createPatternWarnLatex() {
    return new PatternWarnLatex();
  }

  // defines patternWarnLatex element with text in ant 
  public class PatternWarnLatex {
    // FIXME: this is without property resolution. 
    // to add this need  pattern = getProject().replaceProperties(pattern)
    // with Task.getProject() 
    public void addText(String pattern) {
      Settings.this.setPatternWarnLatex(pattern);
    }
  }

  public void setDebugBadBoxes(boolean debugBadBoxes) {
    this.debugBadBoxes = debugBadBoxes;
  }

  public void setDebugWarnings(boolean debugWarnings) {
    this.debugWarnings = debugWarnings;
  }

  public void setPdfViaDvi(boolean pdfViaDvi) {
    this.pdfViaDvi = pdfViaDvi;
  }

  public void setDvi2pdfCommand(String dvi2pdfCommand) {
    this.dvi2pdfCommand = dvi2pdfCommand;
  }

  public void setDvi2pdfOptions(String dvi2pdfOptions) {
    this.dvi2pdfOptions = dvi2pdfOptions.replaceAll("(\t|\n| )+", " ").trim();
  }

  // setter method for patternReRunLatex in maven 
  public void setPatternReRunLatex(String patternReRunLatex) {
    this.patternReRunLatex =
        patternReRunLatex.replaceAll("(\t|\n)+", "").trim();
  }

  // method introduces patternReRunLatex in ant 
  public PatternReRunLatex createPatternReRunLatex() {
    return new PatternReRunLatex();
  }

  // defines patternNeedAnotherLatexRun element with text in ant 
  public class PatternReRunLatex {
    // FIXME: this is without property resolution. 
    // to add this need  pattern = getProject().replaceProperties(pattern)
    // with Task.getProject() 
    public void addText(String pattern) {
      Settings.this.setPatternReRunLatex(pattern);
    }
  }

  // FIXME: real check needed. also in other locations. 
  public void setMaxNumReRunsLatex(int maxNumReRunsLatex) {
    assert maxNumReRunsLatex >= 1
        || maxNumReRunsLatex == -1 : "Found illegal max number of reruns "
            + maxNumReRunsLatex + ". ";
    this.maxNumReRunsLatex = maxNumReRunsLatex;
  }

  public void setBibtexCommand(String bibtexCommand) {
    this.bibtexCommand = bibtexCommand;
  }

  public void setBibtexOptions(String bibtexOptions) {
    this.bibtexOptions = beautifyOptions(bibtexOptions);
  }

  // setter method for patternErrBibtex in maven 
  public void setPatternErrBibtex(String patternErrBibtex) {
    this.patternErrBibtex = patternErrBibtex;
  }

  // method introduces patternErrBibtex in ant 
  public PatternErrBibtex createPatternErrBibtex() {
    return new PatternErrBibtex();
  }

  // defines patternErrBibtex element with text in ant 
  public class PatternErrBibtex {
    // FIXME: this is without property resolution. 
    // to add this need  pattern = getProject().replaceProperties(pattern)
    // with Task.getProject() 
    public void addText(String pattern) {
      Settings.this.setPatternErrBibtex(pattern);
    }
  }

  // setter method for patternWarnBibtex in maven 
  public void setPatternWarnBibtex(String patternWarnBibtex) {
    this.patternWarnBibtex = patternWarnBibtex;
  }

  // method introduces patternWarnBibtex in ant 
  public PatternWarnBibtex createPatternWarnBibtex() {
    return new PatternWarnBibtex();
  }

  // defines patternWarnBibtex element with text in ant 
  public class PatternWarnBibtex {
    // FIXME: this is without property resolution. 
    // to add this need  pattern = getProject().replaceProperties(pattern)
    // with Task.getProject() 
    public void addText(String pattern) {
      Settings.this.setPatternWarnBibtex(pattern);
    }
  }

  public void setMakeIndexCommand(String makeIndexCommand) {
    this.makeIndexCommand = makeIndexCommand;
  }

  public void setMakeIndexOptions(String makeIndexOptions) {
    this.makeIndexOptions = beautifyOptions(makeIndexOptions);
  }

  // setter method for patternErrMakeIndex in maven 
  public void setPatternErrMakeIndex(String patternErrMakeIndex) {
    this.patternErrMakeIndex = patternErrMakeIndex.replaceAll("\n+", "").trim();
  }

  // method introduces patternErrMakeIndex in ant 
  public PatternErrMakeIndex createPatternErrMakeIndex() {
    return new PatternErrMakeIndex();
  }

  // defines patternErrMakeIndex element with text in ant 
  public class PatternErrMakeIndex {
    // FIXME: this is without property resolution. 
    // to add this need  pattern = getProject().replaceProperties(pattern)
    // with Task.getProject() 
    public void addText(String pattern) {
      Settings.this.setPatternErrMakeIndex(pattern);
    }
  }

  // FIXME: MakeIndex
  // setter method for patternWarnMakeIndex in maven 
  public void setPatternWarnMakeIndex(String patternWarnMakeIndex) {
    this.patternWarnMakeIndex =
        patternWarnMakeIndex.replaceAll("\n+", "").trim();
  }

  // method introduces patternWarnMakeIndex in ant 
  public PatternWarnMakeIndex createPatternWarnMakeIndex() {
    return new PatternWarnMakeIndex();
  }

  // defines patternWarnMakeIndex element with text in ant 
  public class PatternWarnMakeIndex {
    // FIXME: this is without property resolution. 
    // to add this need  pattern = getProject().replaceProperties(pattern)
    // with Task.getProject() 
    public void addText(String pattern) {
      Settings.this.setPatternWarnMakeIndex(pattern);
    }
  }

  // setter method for patternReRunMakeIndex in maven 
  public void setPatternReRunMakeIndex(String pattern) {
    this.patternReRunMakeIndex = pattern.replaceAll("\n+", "").trim();
  }

  // method introduces patternMakeIndex in ant 
  public PatternReRunMakeIndex createPatternReRunMakeIndex() {
    return new PatternReRunMakeIndex();
  }

  // defines patternReRunMakeIndex element with text in ant 
  public class PatternReRunMakeIndex {
    // FIXME: this is without property resolution. 
    // to add this need  pattern = getProject().replaceProperties(pattern)
    // with Task.getProject() 
    public void addText(String pattern) {
      Settings.this.setPatternReRunMakeIndex(pattern);
    }
  }

  public void setSplitIndexCommand(String splitIndexCommand) {
    this.splitIndexCommand = splitIndexCommand;
  }

  public void setSplitIndexOptions(String splitIndexOptions) {
    this.splitIndexOptions = beautifyOptions(splitIndexOptions);
  }

  public void setMakeGlossariesCommand(String makeGlossariesCommand) {
    this.makeGlossariesCommand = makeGlossariesCommand;
  }

  public void setMakeGlossariesOptions(String makeGlossariesOptions) {
    this.makeGlossariesOptions = beautifyOptions(makeGlossariesOptions);
  }

  public void setPatternErrMakeGlossaries(String patternErrMakeGlossaries) {
    this.patternErrMakeGlossaries =
        patternErrMakeGlossaries.replaceAll("\n+", "").trim();
  }

  public void setPatternWarnXindy(String patternWarnXindy) {
    this.patternWarnXindy = patternWarnXindy.replaceAll("\n+", "").trim();
  }

  // setter method for patternReRunMakeGlossaries in maven 
  public void setPatternReRunMakeGlossaries(String pattern) {
    this.patternReRunMakeGlossaries = pattern.replaceAll("\n+", "").trim();
  }

  // method introduces patternReRunMakeGlossaries in ant 
  public PatternReRunMakeGlossaries createPatternReRunMakeGlossaries() {
    return new PatternReRunMakeGlossaries();
  }

  public void setPythontexCommand(String pythontexCommand) {
    this.pythontexCommand = pythontexCommand;
  }

  public void setPythontexOptions(String pythontexOptions) {
    this.pythontexOptions = beautifyOptions(pythontexOptions);
  }

  public void setPatternErrPyTex(String patternErrPyTex) {
    this.patternErrPyTex = patternErrPyTex.replaceAll("(\t|\n| )+", " ").trim();
  }

  public void setPatternWarnPyTex(String patternWarnPyTex) {
    this.patternWarnPyTex =
        patternWarnPyTex.replaceAll("(\t|\n| )+", " ").trim();
  }

  // defines patternReRunMakeGlossaries element with text in ant 
  public class PatternReRunMakeGlossaries {
    // FIXME: this is without property resolution. 
    // to add this need  pattern = getProject().replaceProperties(pattern)
    // with Task.getProject() 
    public void addText(String pattern) {
      Settings.this.setPatternReRunMakeGlossaries(pattern);
    }
  }

  public void setTex4htCommand(String tex4htCommand) {
    this.tex4htCommand = tex4htCommand;
  }

  public void setTex4htStyOptions(String tex4htStyOptions) {
    this.tex4htStyOptions = beautifyOptions(tex4htStyOptions);
  }

  public void setTex4htOptions(String tex4htOptions) {
    this.tex4htOptions = tex4htOptions.replaceAll("(\t|\n| )+", " ").trim();
  }

  public void setT4htOptions(String t4htOptions) {
    this.t4htOptions = beautifyOptions(t4htOptions);
  }

  // setter method for patternT4htOutputFiles in maven 
  public void setPatternT4htOutputFiles(String patternT4htOutputFiles) {
    this.patternT4htOutputFiles =
        patternT4htOutputFiles.replaceAll("(\t|\n| )+", "").trim();
  }

  // method introduces patternT4htOutputFiles in ant 
  public PatternT4htOutputFiles createPatternT4htOutputFiles() {
    return new PatternT4htOutputFiles();
  }

  // defines patternT4htOutputFiles element with text in ant 
  public class PatternT4htOutputFiles {
    // FIXME: this is without property resolution. 
    // to add this need  pattern = getProject().replaceProperties(pattern)
    // with Task.getProject() 
    public void addText(String pattern) {
      Settings.this.setPatternT4htOutputFiles(pattern);
    }
  }

  public void setLatex2rtfCommand(String latex2rtfCommand) {
    this.latex2rtfCommand = latex2rtfCommand;
  }

  // FIXME: replaceAll: should be unified. 
  public void setLatex2rtfOptions(String latex2rtfOptions) {
    this.latex2rtfOptions = beautifyOptions(latex2rtfOptions);
  }

  public void setOdt2docCommand(String odt2docCommand) {
    this.odt2docCommand = odt2docCommand;
  }

  public void setOdt2docOptions(String odt2docOptions) {
    this.odt2docOptions = beautifyOptions(odt2docOptions);
  }

  public void setPdf2txtCommand(String pdf2txtCommand) {
    this.pdf2txtCommand = pdf2txtCommand;
  }

  // getter commands: for ant task only. 
  public void setPdf2txtOptions(String pdf2txtOptions) {
    this.pdf2txtOptions = beautifyOptions(pdf2txtOptions);
  }

  public void setChkTexCommand(String chkTexCommand) {
    this.chkTexCommand = chkTexCommand;
  }

  public void setChkTexOptions(String chkTexOptions) {
    this.chkTexOptions = beautifyOptions(chkTexOptions);
  }

  public void setDiffPdfCommand(String diffPdfCommand) {
    this.diffPdfCommand = diffPdfCommand;
  }

  /**
   * Returns the parameters defined in this class as a map from their names to their values. 
   * Parameters are marked by annotations of type {@link Parameter}. 
   * Since this is not runtime visible, we mark parameters with another annotation, {@link RuntimeParameter}. 
   * Currently, their names are the names of the field (TBD: add check, see changes). 
   * There is one case where the default value is <code>null</code>. 
   * The string representation is 'null'. 
   * In the long run, the {@link RuntimeParameter} shall be added automatically 
   * while performing check of names. 
   * Currently it is checked that the parameter is private and not static. 
   * 
   * The ordering of the parameters is the ordering of the according fields in the class. 
   * To that end, we use {@link LinkedHashMap}. 
   * 
   * @return
   *   A map from names of parameters to their current values as a string. 
   *   If <code>null</code> use the string 'null'. 
   */
  public Map<String, String> getProperties() {
    // keys are never null, but values may be null
    Map<String, String> res = new LinkedHashMap<String, String>();
    Field[] fields = this.getClass().getDeclaredFields();
    String name;
    Object value;
    int mod;
    for (Field field : fields) {
      // TBD: in the long run maybe Parameter
      RuntimeParameter annot = field.getAnnotation(RuntimeParameter.class);
      //System.out.println(Arrays.asList(field.getDeclaredAnnotations()));
      if (annot == null) {
        // Here, the field is no parameter. 
        continue;
      }
      // Here, the field is a parameter 
      // TBD: check for right name and default value 
      mod = field.getModifiers();
      if (Modifier.isStatic(mod)) {
        continue;
      }
      assert !Modifier.isStatic(mod) : "found parameter which is static. ";
      assert !Modifier.isFinal(mod) : "found parameter which is final. ";
      assert Modifier.isPrivate(mod) : "found parameter which is not private. ";

      name = field.getName();
      //assert annot.name().equals(name) : "Parameter name shall be fieldname. ";
      field.setAccessible(true);
      try {
        value = field.get(this);
        res.put(name, value == null ? null : value.toString());
      } catch (IllegalArgumentException iare) {
        throw new IllegalStateException(
            "Found no field '" + name + "' in setting. ");
      } catch (IllegalAccessException iace) {
        throw new IllegalStateException(
            "Illegal access to field '" + name + "' although set accessible. ");
      }
    }
    return res;
  }

  /**
   * Returns the file assoicated with the resource <code>fileNameResource</code>. 
   *
   * @param fileNameResource
   *   The name of the resource which is also the (short) file name returned. 
   * @return
   *   the file in directory {@link #texSrcDirectory} with name <code>fileNameResource</code>. 
   */
  File rcResourceToFile(String fileNameResource) {
    return new File(this.texSrcDirectory, fileNameResource);
  }

  /**
   * Filters a resource given by <code>inStream</code> 
   * into a <code>writer</code> similar to the maven resources plugin: 
   * Replace the settings given by name in the form <code>${&lt;name%gt;}</code> 
   * by the current value of the setting with the given name. 
   * In contrast to the resources plugin, 
   * the names refer to settings not to parameters, e.g. given in the pom. 
   * <p>
   * This is applied e.g. to record files, 
   * <code>.latexmkrc</code> and <code>.chktexrc</code>. 
   * That way, <code>latexmk</code> and <code>chktex</code> 
   * run with the according configuration 
   * are aligned with the current settings of this plugin. 
   *
   * @param inStream
   *   The stream of the file to be filtered. 
   *   This refers to the source file which is loaded 
   *   as a resource with a certain filename. 
   * @param writer
   *   Refers to the file in folder 
   *   given by the parameter {@link #texSrcDirectory} 
   *   with the same name as the resource defining <code>inStream</code>. 
   * @param version
   *   the version of this softwaregoing into the headline.
   * @param inj
   *   The injection which determines the comment indentifier 
   *   and whether there is a hashbanb. 
   * @throws IOException
   *   May occur if reading a line but not if writing a line. 
   */
  public void filterInjection(InputStream inStream, PrintStream writer,
      String version, Injection inj) throws IOException {//
    BufferedReader bufReader =
        new BufferedReader(new InputStreamReader(inStream));

    Pattern pattern = Pattern.compile("\\$\\{(\\w+)\\}");
    Map<String, String> props = this.getProperties();

    String strLine;
    if (inj.hasShebang()) {
      strLine = bufReader.readLine();
      // this is because strLine is the shebang line. 
      assert strLine != null;
      // write shebang line as is 
      writer.println(strLine);
    }
    // the headline shows that the file is generated 
    // and may thus be overwritten and erased. 
    // throws IOExeption if an IO error occurs 
    writer.println(inj.commentStr() + TexFileUtils.HEADLINE_GEN + version);

    // Read File Line By Line
    Matcher matcher;

    // throws IOExeption if an IO error occurs
    while ((strLine = bufReader.readLine()) != null) {
      // filter until no variable in strLine found 
      while (true) {
        matcher = pattern.matcher(strLine);
        if (matcher.find()) {
          assert matcher.groupCount() >= 1;

          // group zero is the whole, and it is not in the goup count 
          assert props.containsKey(matcher.group(1)) : "Key '"
              + matcher.group(1) + "' not found. ";
          strLine = matcher.replaceFirst(props.get(matcher.group(1)));
        } else {
          // Here, no variable in strLine found 
          writer.println(strLine);
          break;
        }
        // filter next line 
      }
    }

    // flush and close the streams 
    writer.flush();
    writer.close();
  }

  public String toString() {
    List<String> res = new ArrayList<String>();
    //String name, value;
    // name = "baseDirectory";
    // value = this.baseDirectory.toString();
    // res.add(name + "=" + value + "");
    // name = "targetDirectory";
    // value = this.targetDirectory.toString();
    // res.add(name + "=" + value + "");
    // name = "targetSiteDirectory";
    // value = this.targetSiteDirectory.toString();
    // res.add(name + "=" + value + "");
    Map<String, String> name2value = this.getProperties();

    for (Map.Entry<String, String> entry : name2value.entrySet()) {
      res.add(entry.getKey() + "='" + entry.getValue() + "'");
    }
    return res.toString();
  }

  public static void main(String[] args) {
    System.out.println("texpath: " + new Settings().getTexPath());
  }
}
