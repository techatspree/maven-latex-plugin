package eu.simuline.m2latex.core;

import java.io.File;

import java.util.Optional;

import com.florianingerl.util.regex.Matcher;

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

  // private static boolean matches(Matcher matcher,
  //     LatexMainParameterNames name) {
  //     return matcher.group(name.toString()) != null;
  // }

  // private static void printGroups(Matcher matcher) {
  //   List<String> names = Arrays.asList(LatexMainParameterNames.values())
  //   .stream().map(val -> val.toString()).collect(Collectors.toList());;
  //   for (String name : names) {
  //      System.out.println(name + " |" + matcher.group(name) + "|");
  //   }
  // }

  // static Optional<LatexMainDesc> getLatexMain(File texFile, Matcher matcher) {
  //   printGroups(matcher);

  //   return (matches(matcher, LatexMainParameterNames.programMagic)
  //        || matches(matcher, LatexMainParameterNames.targetsMagic)
  //        || matches(matcher, LatexMainParameterNames.docClassMagic)
  //        || matches(matcher, LatexMainParameterNames.docClass))
  //           ? Optional.of(new LatexMainDesc(texFile, matcher))
  //           : Optional.empty();
  // }

  // // TBD: eliminate
  // // For tests only. 
  // LatexMainDesc(File texFile) {
  //   this(texFile, null);
  // }

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
   *    the text matched in the group as an optional, 
   *    where empty optional indicates 
   *    that the pattern has the group but nothing matches, 
   *    e.g. because of an alternative like <code>(?&lt;name&gt;x)?</code>. 
   * @throws IllegalArgumentException
   *    If the matching pattern has no capturing group with the given name. 
   */
  Optional<String> groupMatch(LatexMainParameterNames groupName) {
    // formally this may throw a IllegalStateException, but this is excluded. 
    return Optional.ofNullable(this.matcher.group(groupName.toString()));
  }

  // Currently, document class is always defined. 
  String getDocClass() {
    String res = this.matcher.group(LatexMainParameterNames.docClass.toString());
    assert res != null;
    return res;
  }

  // Optional<String> getDocClass(LogWrapper log) {
  //   String docClassMagic = groupMatch(LatexMainParameterNames.docClassMagic);
  //   String docClass = groupMatch(LatexMainParameterNames.docClass);
  //   if (docClassMagic != null) {
  //     if (docClass != null) {
  //       log.warn("XXXX: Doc class in magic comment '" + docClassMagic
  //           + "'' overwrites '" + docClass + "' in documentclass/style. ");
  //     }
  //     return Optional.of(docClassMagic);
  //   }
  //   return Optional.ofNullable(docClass);
  // }

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
