package eu.simuline.m2latex.core;

import java.io.File;

import java.util.regex.Matcher;

/**
 * Container which comprises, besides the latex main file
 * also several files creation of which shall be done once for ever.
 */
class LatexMainDesc implements Comparable<LatexMainDesc> {
  final File texFile;
  final File pdfFile;
  final File dviFile;
  final File xdvFile;

  final File logFile;

  final File idxFile;
  final File indFile;
  final File ilgFile;

  final File glsFile;
  final File gloFile;
  final File glgFile;

  final File xxxFile;

  final File parentDir;

  //final String docClass;

  private final Matcher matcher;

  LatexMainDesc(File texFile, Matcher matcher) {
    this.matcher = matcher;
    this.texFile = texFile;
    this.xxxFile =
        TexFileUtils.replaceSuffix(texFile, LatexProcessor.SUFFIX_VOID);
    this.pdfFile = withSuffix(LatexProcessor.SUFFIX_PDF);
    this.dviFile = withSuffix(LatexProcessor.SUFFIX_DVI);
    this.xdvFile = withSuffix(LatexProcessor.SUFFIX_XDV);
    this.logFile = withSuffix(LatexProcessor.SUFFIX_LOG);

    this.idxFile = withSuffix(LatexProcessor.SUFFIX_IDX);
    this.indFile = withSuffix(LatexProcessor.SUFFIX_IND);
    this.ilgFile = withSuffix(LatexProcessor.SUFFIX_ILG);

    this.glsFile = withSuffix(LatexProcessor.SUFFIX_GLS);
    this.gloFile = withSuffix(LatexProcessor.SUFFIX_GLO);
    this.glgFile = withSuffix(LatexProcessor.SUFFIX_GLG);
    this.parentDir = this.texFile.getParentFile();
  }

  /**
   * Returns the content of the group of the name specified, 
   * if matched, else <code>null</code>. 
   * 
   * @param groupName
   *    a representation of the name of a group in a regular expression. 
   * @return
   *    the text matched in the group or <code>null</code>, 
   *    the latter if the pattern has the group but nothing matches, 
   *    e.g. because of an alternative like <code>(?&lt;name&gt;x)?</code>. 
   * @throws IllegalArgumentException
   *    If the matching pattern has no capturing group with the given name. 
   */
  String groupMatch(LatexMainParameterNames groupName) {
    // formally this may throw a IllegalStateException, but this is excluded. 
    return this.matcher.group(groupName.toString());
  }

  File withSuffix(String suffix) {
    return TexFileUtils.appendSuffix(this.xxxFile, suffix);
  }

  public int compareTo(LatexMainDesc other) {
    return this.texFile.compareTo(other.texFile);
  }

  public String toString() {
    return "<LatexMainDesc>" + this.texFile.getName() + "</LatexMainDesc>";
  }
} // class LatexMainDesc
